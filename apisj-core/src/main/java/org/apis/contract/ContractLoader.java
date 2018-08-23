package org.apis.contract;

import com.google.gson.JsonArray;
import org.apis.config.BlockchainConfig;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.db.BlockStore;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.SolidityFunction;
import org.apis.util.blockchain.StandaloneBlockchain;
import org.apis.vm.program.ProgramResult;
import org.apis.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContractLoader {
    private static Logger logger = LoggerFactory.getLogger("ContractLoader");

    public static final int CONTRACT_ADDRESS_MASKING = 0;
    public static final int CONTRACT_GATE_KEEPER = 1;
    public static final int CONTRACT_MINERAL_CHARGE = 2;
    public static final int CONTRACT_MIXING_SEND = 3;
    public static final int CONTRACT_FOUNDATION_WALLET = 4;
    public static final int CONTRACT_MASTERNODE = 5;
    public static final int CONTRACT_CODE_FREEZER = 6;

    private static final SystemProperties config = SystemProperties.getDefault();

    public static void makeABI() {
        try {
            for (int i = 0; i < 7; i++) {
                String fileName = getContractFileName(i);
                if (fileName.isEmpty()) {
                    continue;
                }

                String contractCode = loadContractSource(i);

                if (contractCode == null || contractCode.isEmpty()) {
                    continue;
                }
                SolidityCompiler.Result result = SolidityCompiler.compile(contractCode.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
                if (result.isFailed()) {
                    logger.error("Contract compilation failed : \n" + result.errors);
                    continue;
                }

                CompilationResult res = CompilationResult.parse(result.output);
                CompilationResult.ContractMetadata metadata = res.getContract(getContractName(i));
                if (metadata == null) {
                    continue;
                }

                fileName = (config.abiDir() + "/" + getContractName(i) + ".json");

                saveABI(fileName, metadata.abi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveABI(String fileName, String abi) {
        if(fileName == null || fileName.isEmpty() || abi == null || abi.isEmpty()) {
            return;
        }

        File keystore = new File(config.abiDir());
        if(!keystore.exists()) {
            if(!keystore.mkdirs()) {
                return;
            }
        }

        // 파일을 저장한다.
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
            writer.print(abi);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String readABI(int contractIndex) {

        String fileName = config.abiDir() + "/" + getContractFileName(contractIndex);

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while(line != null) {
                sb.append(line).append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String loadContractSource(int contractType) throws RuntimeException {
        String contractFileName = getContractFileName(contractType);

        URL contractUrl = ContractLoader.class.getClassLoader().getResource("contract/" + contractFileName);
        if(contractUrl == null) {
            return null;
        }

        // #1 try to find genesis at passed location
        try (InputStream is = new FileInputStream(new File(contractUrl.toURI()))) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }

            return out.toString();
        } catch (Exception e) {
            logger.error("Problem loading contract file from " + contractFileName);
        }

        return null;
    }

    private static String getContractFileName(int contractIndex) {
        switch(contractIndex) {
            case CONTRACT_ADDRESS_MASKING:
                return "AddressMasking.sol";
            case CONTRACT_GATE_KEEPER:
                return "";
            case CONTRACT_MINERAL_CHARGE:
                return "";
            case CONTRACT_MIXING_SEND:
                return "";
            case CONTRACT_FOUNDATION_WALLET:
                return "MultiSigWalletGenesis.sol";
            case CONTRACT_MASTERNODE:
                return "";
            case CONTRACT_CODE_FREEZER:
                return "ContractFreezer.sol";
            default:
                return "";
        }
    }

    private static String getContractName(int contractIndex) {
        switch(contractIndex) {
            case CONTRACT_ADDRESS_MASKING:
                return "AddressMasking";
            case CONTRACT_GATE_KEEPER:
                return "";
            case CONTRACT_MINERAL_CHARGE:
                return "";
            case CONTRACT_MIXING_SEND:
                return "";
            case CONTRACT_FOUNDATION_WALLET:
                return "MultisigWallet";
            case CONTRACT_MASTERNODE:
                return "";
            case CONTRACT_CODE_FREEZER:
                return "ContractFreezer";
            default:
                return "";
        }
    }

    /**
     * 재단 멀티시그 지갑을 초기 설정하는 트랜잭션 데이터를 생성한다.
     * @return Transaction data to initialize foundation wallet
     */
    public static byte[] getInitFoundationWalletData(List<byte[]> owners, long required) throws IOException {
        String contractName = "MultisigWallet";

        String src = loadContractSource(CONTRACT_FOUNDATION_WALLET);
        if(src == null || src.isEmpty()) {
            return null;
        }
        SolidityCompiler.Result result = SolidityCompiler.compile(src.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if(result.isFailed()) {
            logger.error("Contract compilation failed : \n" + result.errors);
            return null;
        }

        CompilationResult res = CompilationResult.parse(result.output);
        CompilationResult.ContractMetadata metadata = res.getContract(contractName);
        if(metadata == null) {
            return null;
        }

        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        byte[] initParams = contract.getByName("initContract").encodeArguments(owners, required);

        return ByteUtil.merge(Hex.decode(metadata.bin), initParams);
    }

    public static boolean isContractFrozen(Repository repo, BlockStore blockStore, ProgramInvokeFactory programInvokeFactory, Block callBlock, BlockchainConfig blockchainConfig, Object ... args) {
        CallTransaction.Contract freezer = new CallTransaction.Contract(readABI(CONTRACT_CODE_FREEZER));

        CallTransaction.Function func = freezer.getByName("isFrozen");

        Transaction tx = CallTransaction.createCallTransaction(0,
                0,
                100000000000000L,
                ByteUtil.toHexString(blockchainConfig.getConstants().getSMART_CONTRACT_CODE_FREEZER()),
                0,
                func,
                convertArgs(args));
        tx.sign(new byte[32]);


        TransactionExecutor executor = new TransactionExecutor
                (tx, callBlock.getCoinbase(), repo, blockStore, programInvokeFactory, callBlock)
                .setLocalCall(true);

        executor.init();
        executor.execute();
        executor.go();
        executor.finalization();

        return (boolean) func.decodeResult(executor.getResult().getHReturn())[0];

    }

    private static Object[] convertArgs(Object[] args) {
        Object[] ret = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof SolidityFunction) {
                SolidityFunction f = (SolidityFunction) args[i];
                ret[i] = ByteUtil.merge(f.getContract().getAddress(), f.getInterface().encodeSignature());
            } else {
                ret[i] = args[i];
            }
        }
        return ret;
    }


    public static Transaction getAddressMaskingContractCreation(BigInteger nonce, int chainId) {
        try {
            String contractSource = loadContractSource(CONTRACT_ADDRESS_MASKING);
            if(contractSource == null) {
                return null;
            }
            SolidityCompiler.Result result = SolidityCompiler.compile(contractSource.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

            if(result.isFailed()) {
                logger.error("Contract compilation failed : \n" + result.errors);
                return null;
            }

            CompilationResult res = CompilationResult.parse(result.output);

            CompilationResult.ContractMetadata metadata = res.getContract("AddressMasking");

            if(metadata == null) {
                return null;
            }

            String iii = metadata.getInterface();
            CallTransaction.Contract cont = new CallTransaction.Contract(metadata.abi);

            //TODO 생성자 인수들을 입력해야 함.
            //constructor (address[] _owners, uint16 _required, uint256 _defaultFee, address _foundationAccount)
            List<byte[]> owners = new ArrayList<>();
            owners.add(Hex.decode("b8129d685750e880ed904a6ecca9b727eaefff9a"));
            BigInteger required = BigInteger.ONE;
            BigInteger defaultFee = new BigInteger("100000000000000000");
            byte[] foundationAccount = Hex.decode("b8129d685750e880ed904a6ecca9b727eaefff9a");

            byte[] initParams = cont.getConstructor().encodeArguments(owners, required, defaultFee, foundationAccount);

            byte[] data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);

            //CompilationResult.ContractMetadata metadata = res.contracts.values().iterator().next();
            if(metadata.bin == null || metadata.bin.isEmpty()) {
                logger.error("Compilation failed, no binary returned:\n" + result.errors);
                return null;
            }

            return new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                    ByteUtil.longToBytesNoLeadZeroes(400_000_000L),
                    new byte[0],
                    ByteUtil.longToBytesNoLeadZeroes(0),
                    data,
                    chainId
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
