package org.apis.contract;

import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.db.BlockStore;
import org.apis.facade.EthereumImpl;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;

public class EstimateTransaction {

    private static EstimateTransaction instance = null;

    private static final long DEFAULT_GAS_LIMIT = 5_000_000L;
    private static final BigInteger DEFAULT_VALUE = BigInteger.ZERO;

    private Logger logger = LoggerFactory.getLogger("estimate");

    private EthereumImpl apis;


    public static EstimateTransaction getInstance(EthereumImpl apis) {
        if(instance == null) {
            instance = new EstimateTransaction(apis);
        }

        return instance;
    }

    /**
     * getInstance(apis) 메서드를 먼저 호출하지 않았다면 Null이 반환될 수 있다.
     * @return null or EstimateTransaction object
     */
    public static EstimateTransaction getInstance() {
        return instance;
    }

    private EstimateTransaction(EthereumImpl apis) {
        this.apis = apis;
    }



    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, Transaction tx) {
        Repository track = repo.startTracking();

        if(tx.getSender() == null) {
            tx.sign(ECKey.DUMMY);
        }

        TransactionExecutor executor = new TransactionExecutor(tx, ECKey.DUMMY.getAddress(), track, blockStore, new ProgramInvokeFactoryImpl(), callBlock)
                .setLocalCall(true);

        executor.init();
        executor.execute();
        executor.go();
        executor.finalization();

        track.rollback();

        return executor;
    }


    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, long gasLimit, CallTransaction.Function func, Object ... args) {
        return getExecutor(repo, blockStore, callBlock, from, to, DEFAULT_VALUE, gasLimit, func.encode(args));
    }
    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, BigInteger value, CallTransaction.Function func, Object ... args) {
        return getExecutor(repo, blockStore, callBlock, from, to, value, DEFAULT_GAS_LIMIT, func.encode(args));
    }
    public TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, CallTransaction.Function func, Object ... args) {
        return getExecutor(repo, blockStore, callBlock, from, to, DEFAULT_VALUE, DEFAULT_GAS_LIMIT, func.encode(args));
    }
    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, long gasLimit, byte[] data) {
        return getExecutor(repo, blockStore, callBlock, from, to, DEFAULT_VALUE, gasLimit, data);
    }
    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, BigInteger value, byte[] data) {
        return getExecutor(repo, blockStore, callBlock, from, to, value, DEFAULT_GAS_LIMIT, data);
    }
    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, byte[] data) {
        return getExecutor(repo, blockStore, callBlock, from, to, DEFAULT_VALUE, DEFAULT_GAS_LIMIT, data);
    }
    private TransactionExecutor getExecutor(Repository repo, BlockStore blockStore, Block callBlock, byte[] from, byte[] to, BigInteger value, long gasLimit, byte[] data) {
        Transaction tx = makeTransaction(from, to, gasLimit, value, data);
        return getExecutor(repo, blockStore, callBlock, tx);
    }


    public EstimateTransactionResult estimate(Repository repo, BlockStore blockStore, Block callBlock, Transaction tx) {
        TransactionExecutor executor = getExecutor(repo, blockStore, callBlock, tx);
        TransactionReceipt receipt = executor.getReceipt();

        long newGasLimit = BIUtil.toBI(receipt.getGasUsed()).longValue();
        boolean isSuccess = receipt.isSuccessful();

        long offset = (long)(newGasLimit*0.4f);
        boolean lastSuccess = isSuccess;

        TransactionExecutor lastSuccessExecutor = executor;
        long lastSuccessGasUsed = 0;

        for(int i = 0; i < 50 && isSuccess; i++) {

            executor = getExecutor(repo, blockStore, callBlock, tx.getSender(), tx.getReceiveAddress(), ByteUtil.bytesToBigInteger(tx.getValue()), newGasLimit, tx.getData());
            receipt = executor.getReceipt();

            if(receipt.isSuccessful()) {
                lastSuccessExecutor = executor;
                lastSuccessGasUsed = newGasLimit;

                if(i == 0 || offset < 1_00) {
                    break;
                } else {
                    newGasLimit -= offset;
                }
            } else {
                newGasLimit += offset;
            }


            if (receipt.isSuccessful() != lastSuccess) {
                offset = (long) (offset*0.4);
            }

            lastSuccess = receipt.isSuccessful();
        }

        return new EstimateTransactionResult(lastSuccessExecutor, lastSuccessGasUsed);
    }

    public EstimateTransactionResult estimate(String abi, byte[] from, byte[] to, BigInteger value, String functionName, Object ... args) {
        CallTransaction.Contract contract = new CallTransaction.Contract(abi);

        CallTransaction.Function func;
        if(to == null) {
            func = contract.getConstructor();
        } else {
            func = contract.getByName(functionName);
        }

        return estimate(from, to, value, func.encode(args));
    }

    public EstimateTransactionResult estimate(byte[] from, byte[] to, BigInteger value, byte[] data) {
        Transaction tx = makeTransaction(from, to, value, data);
        return estimate(tx);
    }

    public EstimateTransactionResult estimate(Transaction tx) {
        Block callBlock = apis.getBlockchain().getBestBlock();
        Repository repo = ((Repository)apis.getRepository()).getSnapshotTo(callBlock.getStateRoot());
        BlockStore blockStore = apis.getBlockchain().getBlockStore();

        return estimate(repo, blockStore, callBlock, tx);
    }


    public EstimateTransactionResult estimateDeploy(byte[] from, String soliditySourceCode, String targetContractName, Object ... args) {
        String error;
        try {
            if(soliditySourceCode == null || soliditySourceCode.isEmpty()) {
                error = "The source code to compile is empty.";
                logger.error(error);
                return new EstimateTransactionResult(error);
            }

            SolidityCompiler.Result result = SolidityCompiler.compileOpt(soliditySourceCode.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

            if(result.isFailed()) {
                error = "Contract compilation failed : \n" + result.errors;
                logger.error(error);
                return new EstimateTransactionResult(error);
            }

            CompilationResult res = CompilationResult.parse(result.output);

            CompilationResult.ContractMetadata metadata = res.getContract(targetContractName);

            if(metadata == null) {
                return null;
            }

            CallTransaction.Contract cont = new CallTransaction.Contract(metadata.abi);

            byte[] initParams = new byte[0];
            if(cont.getConstructor() != null){
                cont.getConstructor().encodeArguments(args);
            }
            byte[] data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);

            if(metadata.bin == null || metadata.bin.isEmpty()) {
                error = "Compilation failed, no binary returned:\n" + result.errors;
                logger.error(error);
                return new EstimateTransactionResult(error);
            }

            return estimate(from, null, DEFAULT_VALUE, data);
        } catch (IOException e) {
            e.printStackTrace();

            error = "An exception occurred:\n" + e.getMessage();
            logger.error(error);
            return new EstimateTransactionResult(error);
        }
    }


    private Transaction makeTransaction(byte[] from, byte[] to, BigInteger value, byte[] data) {
        return makeTransaction(from, to, DEFAULT_GAS_LIMIT, value, data);
    }

    private Transaction makeTransaction(byte[] from, byte[] to, long gasLimit, BigInteger value, byte[] data) {
        if(from == null) {
            from = ECKey.DUMMY.getAddress();
        }

        long gasPrice = 0L;

        if(gasLimit == 0) {
            gasLimit = DEFAULT_GAS_LIMIT;
        }

        Transaction tx = CallTransaction.createRawTransaction(
                0,
                gasPrice,
                gasLimit,
                ByteUtil.toHexString(to),
                value,
                data);

        tx.setTempSender(from);

        return tx;
    }
}
