package org.apis.contract;

import org.apis.config.BlockchainConfig;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.db.BlockStore;
import org.apis.facade.Ethereum;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.ByteUtil;
import org.apis.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ContractLoader {
    private static Logger logger = LoggerFactory.getLogger("ContractLoader");

    public static final int CONTRACT_ADDRESS_MASKING = 0;
    public static final int CONTRACT_FOUNDATION_WALLET = 1;
    public static final int CONTRACT_CODE_FREEZER = 2;
    public static final int CONTRACT_PROOF_OF_KNOWLEDGE = 3;
    public static final int CONTRACT_BUY_MINERAL = 4;
    public static final int CONTRACT_ERC20 = 5;
    public static final int CONTRACT_MASTERNODE_PLATFORM = 6;
    public static final int CONTRACT_WINK = 7;

    private static final String OWNER_GENESIS_1 = "17ad7cab2f8b48ce2e1c4932390aef0a4e9eea8b";
    private static final String OWNER_GENESIS_2 = "e78bbb7005e646baceb74ac8ed76f17141bfc877";
    private static final String OWNER_GENESIS_3 = "52cb59c122bcc1ce246fb2a3a54ef5d5e8196de2";
    private static final String PLATFORM_WORKER = "792bda21311396ea99676c49f501345a64421f8a";


    private static final SystemProperties config = SystemProperties.getDefault();

    public static void makeABI() {
        try {
            for (int i = 0; i < 8; i++) {
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

                assert config != null;
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

        assert config != null;
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

        assert config != null;
        File keystore = new File(config.abiDir());
        if(!keystore.exists()) {
            if(!keystore.mkdirs()) {
                return "";
            }
        }

        String fileName = config.abiDir() + "/" + getContractName(contractIndex) + ".json";

        File abiFile = new File(fileName);

        try(BufferedReader br = new BufferedReader(new FileReader(abiFile))) {
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


    private static String loadContractSource(int contractType) throws RuntimeException {
        String contractFileName = getContractFileName(contractType);

        // #1 try to find genesis at passed location
        try (InputStream is = ContractLoader.class.getClassLoader().getResourceAsStream("contract/" + contractFileName)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }

            return out.toString();
        } catch (Exception e) {
            logger.error("Problem loading contract file from " + contractFileName);
            e.printStackTrace();
        }

        return null;
    }

    private static String getContractFileName(int contractIndex) {
        switch(contractIndex) {
            case CONTRACT_ADDRESS_MASKING:
                return "AddressMasking.sol";
            case CONTRACT_FOUNDATION_WALLET:
                return "MultiSigWalletGenesis.sol";
            case CONTRACT_CODE_FREEZER:
                return "ContractFreezer.sol";
            case CONTRACT_PROOF_OF_KNOWLEDGE:
                return "ProofOfKnowledge.sol";
            case CONTRACT_BUY_MINERAL:
                return "BuyMineral.sol";
            case CONTRACT_ERC20:
                return "ERC20.sol";
            case CONTRACT_MASTERNODE_PLATFORM:
                return "EarlyBirdManager.sol";
            case CONTRACT_WINK:
                return "Wink.sol";
            default:
                return "";
        }
    }

    private static String getContractName(int contractIndex) {
        switch(contractIndex) {
            case CONTRACT_ADDRESS_MASKING:
                return "AddressMasking";
            case CONTRACT_FOUNDATION_WALLET:
                return "MultisigWallet";
            case CONTRACT_CODE_FREEZER:
                return "ContractFreezer";
            case CONTRACT_PROOF_OF_KNOWLEDGE:
                return "ProofOfKnowledge";
            case CONTRACT_BUY_MINERAL:
                return "BuyMineral";
            case CONTRACT_ERC20:
                return "ERC20";
            case CONTRACT_MASTERNODE_PLATFORM:
                return "EarlyBirdManager";
            case CONTRACT_WINK:
                return "WinkTest";
            default:
                return "";
        }
    }

    private static CallTransaction.Contract winkContract = null;
    public static Wink parseWink(LogInfo info) {
        if(winkContract == null) {
            winkContract = new CallTransaction.Contract(readABI(CONTRACT_WINK));
        }
        CallTransaction.Invocation event = winkContract.parseEvent(info);
        if(event != null && event.function != null && event.function.name != null && event.function.name.equals("Wink")) {
            return new Wink(winkContract.parseEvent(info));
        } else {
            return null;
        }
    }


    public static boolean isContractFrozen(Repository repo, BlockStore blockStore, Block callBlock, BlockchainConfig blockchainConfig, Object ... args) {

        CallTransaction.Contract freezer = new CallTransaction.Contract(readABI(CONTRACT_CODE_FREEZER));
        CallTransaction.Function func = freezer.getByName("isFrozen");

        EstimateTransaction estimator = EstimateTransaction.getInstance();

        byte[] to = blockchainConfig.getConstants().getSMART_CONTRACT_CODE_FREEZER();
        TransactionExecutor executor = estimator.getExecutor(repo, blockStore, callBlock, null, to, func, args);

        return (boolean) func.decodeResult(executor.getResult().getHReturn())[0];
    }



    public static void initAddressMaskingContracts(Ethereum ethereum) {
        assert config != null;
        BigInteger nonce = ethereum.getRepository().getNonce(config.getMinerCoinbase());
        ethereum.submitTransaction(getAddressMaskingContractInitTransaction(nonce, ethereum.getChainIdForNextBlock()));
    }

    public static void initFoundationContracts(Ethereum ethereum) {
        assert config != null;
        BigInteger nonce = ethereum.getRepository().getNonce(config.getMinerCoinbase());
        ethereum.submitTransaction(getFoundationWalletInitTransaction(nonce, ethereum.getChainIdForNextBlock()));
    }

    public static void initBuyMineralContract(Ethereum ethereum) {
        assert config != null;
        BigInteger nonce = ethereum.getRepository().getNonce(config.getMinerCoinbase());
        ethereum.submitTransaction(getBuyMineralContractInitTransaction(nonce, ethereum.getChainIdForNextBlock()));
    }

    public static void initEarlyBirdManagerContracts(Ethereum ethereum) {
        assert config != null;
        BigInteger nonce = ethereum.getRepository().getNonce(config.getMinerCoinbase());
        ethereum.submitTransaction(getEarlyBirdManagerInitTransaction(nonce, ethereum.getChainIdForNextBlock()));
    }



    private static Transaction getAddressMaskingContractInitTransaction(BigInteger nonce, int chainId) {
        String amAbi = readABI(CONTRACT_ADDRESS_MASKING);
        CallTransaction.Contract cont = new CallTransaction.Contract(amAbi);

        List<byte[]> owners = new ArrayList<>();
        owners.add(Hex.decode(OWNER_GENESIS_1));
        owners.add(Hex.decode(OWNER_GENESIS_2));
        owners.add(Hex.decode(OWNER_GENESIS_3));
        BigInteger required = BigInteger.valueOf(2);

        byte[] data = cont.getByName("init").encode(owners, required);

        assert config != null;
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000L),
                config.getBlockchainConfig().getCommonConstants().getADDRESS_MASKING_ADDRESS(),
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                chainId
        );
        tx.sign(config.getCoinbaseKey());
        return tx;
    }

    private static Transaction getFoundationWalletInitTransaction(BigInteger nonce, int chainId) {
        String amAbi = readABI(CONTRACT_FOUNDATION_WALLET);
        CallTransaction.Contract cont = new CallTransaction.Contract(amAbi);

        List<byte[]> owners = new ArrayList<>();
        owners.add(Hex.decode(OWNER_GENESIS_1));
        owners.add(Hex.decode(OWNER_GENESIS_2));
        owners.add(Hex.decode(OWNER_GENESIS_3));
        BigInteger required = BigInteger.valueOf(2);

        byte[] data = cont.getByName("initContract").encode(owners, required);

        assert config != null;
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000L),
                config.getBlockchainConfig().getCommonConstants().getFOUNDATION_STORAGE(),
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                chainId
        );
        tx.sign(config.getCoinbaseKey());
        return tx;
    }

    private static Transaction getBuyMineralContractInitTransaction(BigInteger nonce, int chainId) {
        String amAbi = readABI(CONTRACT_BUY_MINERAL);
        CallTransaction.Contract cont = new CallTransaction.Contract(amAbi);

        List<byte[]> owners = new ArrayList<>();
        owners.add(Hex.decode(OWNER_GENESIS_1));
        owners.add(Hex.decode(OWNER_GENESIS_2));
        owners.add(Hex.decode(OWNER_GENESIS_3));
        BigInteger required = BigInteger.valueOf(2);

        byte[] data = cont.getByName("init").encode(owners, required);

        assert config != null;
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000L),
                config.getBlockchainConfig().getCommonConstants().getBUY_MINERAL(),
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                chainId
        );
        tx.sign(config.getCoinbaseKey());
        return tx;
    }

    private static Transaction getEarlyBirdManagerInitTransaction(BigInteger nonce, int chainId) {
        String amAbi = readABI(CONTRACT_MASTERNODE_PLATFORM);
        CallTransaction.Contract cont = new CallTransaction.Contract(amAbi);

        List<byte[]> owners = new ArrayList<>();
        owners.add(Hex.decode(OWNER_GENESIS_1));
        owners.add(Hex.decode(OWNER_GENESIS_2));
        owners.add(Hex.decode(OWNER_GENESIS_3));
        BigInteger required = BigInteger.valueOf(2);

        byte[] data = cont.getByName("init").encode(owners, required, Hex.decode(PLATFORM_WORKER));

        assert config != null;
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000L),
                config.getBlockchainConfig().getCommonConstants().getMASTERNODE_PLATFORM(),
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                chainId
        );
        tx.sign(config.getCoinbaseKey());
        return tx;
    }
}
