package org.apis.rpc;

import org.apis.contract.ContractLoader;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.crypto.ECKey;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.json.BlockData;
import org.apis.keystore.InvalidPasswordException;
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreManager;
import org.apis.keystore.KeyStoreUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.java_websocket.WebSocket;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apis.rpc.RPCJsonUtil.createJson;

public class RPCCommand {
    static final String COMMAND_RPC_VERSION = "2.0.0";

    // method
    static final String COMMAND_APIS_PROTOCOLVERSION = "apis_protocolVersion";
    static final String COMMAND_APIS_BLOCKNUMBER = "apis_blockNumber";
    static final String COMMAND_APIS_GETBALANCE = "apis_getBalance";

    static final String COMMAND_APIS_GETTRANSACTIONCOUNT = "apis_getTransactionCount";
    static final String COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH = "apis_getBlockTransactionCountByHash";
    static final String COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER = "apis_getBlockTransactionCountByNumber";
    static final String COMMAND_APIS_GETCODE = "apis_getCode";
    static final String COMMAND_APIS_SIGN = "apis_sign";
    static final String COMMAND_APIS_SENDTRANSACTION = "apis_sendTransaction";
    static final String COMMAND_APIS_SENDRAWTRANSACTION = "apis_sendRawTransaction";
    static final String COMMAND_APIS_CALL = "apis_call";

    static final String COMMAND_APIS_GETBLOCKBYHASH = "apis_getBlockByHash";
    static final String COMMAND_APIS_GETBLOCKBYNUMBER = "apis_getBlockByNumber";

    // tag
    static final String TAG_JSONRPC = "jsonrpc";
    static final String TAG_ID = "id";
    static final String TAG_METHOD = "method";
    static final String TAG_PARAMS = "params";
    static final String TAG_RESULT = "result";

    static final String TAG_AUTHKEY = "authKey"; // header
    static final String TAG_TOKEN = "token";
    static final String TAG_HASH = "hash";
    static final String TAG_CODE = "code";
    static final String TAG_ERROR = "error";
    static final String TAG_PAYLOAD = "payload";

    static final String TAG_FROM = "from";
    static final String TAG_TO = "to";
    static final String TAG_GAS = "gas";
    static final String TAG_GASPRICE = "gasPrice";
    static final String TAG_VALUE = "value";
    static final String TAG_DATA = "data";
    static final String TAG_WALLET_INDEX = "walletIndex";
    static final String TAG_KEYSTORE_PW = "keyStorePassword";
    static final String TAG_KNOWLEDGE_PW = "knowledgePassword";

    // default block parammeter
    static final String DEFAULTBLOCK_PARAMETER_EARLIEST = "earliest";
    static final String DEFAULTBLOCK_PARAMETER_LATEST = "latest";
    static final String DEFAULTBLOCK_PARAMETER_PENDING = "pending";

    // error
    static final int ERROR_CODE_UNNKOWN = 100;
    static final int ERROR_CODE_OVERFLOW_MAXCONNECTION = 101;
    static final int ERROR_CODE_DUPLICATE_IP = 102;
    static final int ERROR_CODE_WRONG_ID_PASSWORD = 103;
    static final int ERROR_CODE_WRONG_AUTHKEY = 104;
    static final int ERROR_CODE_WRONG_TOKENKEY = 105;
    static final int ERROR_CODE_WRONG_ID = 106;
    static final int ERROR_CODE_WITHOUT_PERMISSION_CLIENT = 113;
    static final int ERROR_CODE_WITHOUT_PERMISSION_IP = 114;
    static final int ERROR_CODE_WITHOUT_PERMISSION_TYPE = 115;
    static final int ERROR_CODE_NULL_ID = 120;
    static final int ERROR_CODE_TIMEOUT = 121;

    static final String ERROR_MESSAGE_UNKNOWN = "unknown message.";
    static final String ERROR_MESSAGE_UNKNOWN_ADDRESS = "unknown Address.";
    static final String ERROR_MESSAGE_UNKNOWN_HASH = "unknown Hash.";
    static final String ERROR_MESSAGE_UNKNOWN_BLOCKDATA= "unknown block data.";
    static final String ERROR_MESSAGE_NULL_BLOCKDATA = "there is no block data.";
    static final String ERROR_MESSAGE_NULL_WALLETINDEX = "wrong (wallet)index";
    static final String ERROR_MESSAGE_NULL_TOADDRESS = "there is no (to) address.";
    static final String ERROR_MESSAGE_NULL_TOMASK = "there is no (to) mask.";
    static final String ERROR_MESSAGE_NULL_GAS = "there is no gas.";
    static final String ERROR_MESSAGE_NULL_GASPRICE = "there is no gas price.";
    static final String ERROR_MESSAGE_NULL_VALUE = "there is no value.";
    static final String ERROR_MESSAGE_NULL_KEYSTORE_PW = "there is no keyStore password.";

    static final String ERROR_MESSAGE_INVALID_PASSWORD = "Invalid password.";


    static final String ERROR_DEPORT_UNKNOWN = "unknown error.";
    static final String ERROR_DEPORT_OVERFLOW_MAXCONNECTION = "Reached max connection allowance.";
    static final String ERROR_DEPORT_DUPLICATE_IP = "IP address duplicated.";
    static final String ERROR_DEPORT_WRONG_ID_PASSWORD = "Wrong ID or password.";
    static final String ERROR_DEPORT_WRONG_AUTHKEY = "Unauthorized key.";
    static final String ERROR_DEPORT_WRONG_TOKENKEY = "Unauthorized token key.";
    static final String ERROR_DEPORT_WRONG_ID = "Wrong ID.";
    static final String ERROR_DEPORT_WITHOUT_PERMISSION_CLIENT = "Unallowed client.";
    static final String ERROR_DEPORT_WITHOUT_PERMISSION_IP = "Unallowed IP address.";
    static final String ERROR_DEPORT_WITHOUT_PERMISSION_TYPE = "Unallowed command.";
    static final String ERROR_DEPORT_NULL_ID = "Cannot find ID.";
    static final String ERROR_DEPORT_TIMEOUT = "Timeout";


    static final String ERROR_NULL_ADDRESS_BY_MASK = "Address registered to the mask does not exist.";
    static final String ERROR_NULL_MASK_BY_ADDRESS = "There is no mask registered to the address.";
    static final String ERROR_NULL_TRANSACTION_BY_HASH = "There is no transaction can be found with the hash.";
    static final String ERROR_NULL_TOADDRESS_OR_TOMASK = "There is no receiving address or mask.";
    static final String ERROR_NULL_BLOCK_BY_NUMBER = "There is no block can be found with the number.";
    static final String ERROR_NULL_BLOCK_BY_HASH = "There is no block can be found with the hash.";
    static final String ERROR_NULL_MASTERNODE_ADDRESS = "There is no address registered as masternode.";
    static final String ERROR_NULL_WALLET_ADDRESS = "There is no address registered as wallet.";

    public static void conduct(Ethereum ethereum, WebSocket conn, String token,
                               String payload, boolean isEncrypt) throws ParseException {
        long id = RPCJsonUtil.getDecodeMessageId(payload);
        String method = RPCJsonUtil.getDecodeMessageMethod(payload);
        Object[] params = RPCJsonUtil.getDecodeMessageParams(payload);
        conduct(ethereum, conn, token, id, method, params, isEncrypt);
    }

    public static void conduct(Ethereum ethereum, WebSocket conn, String token,
                               long id, String method, Object[] params, boolean isEncrypt) throws ParseException {

        String command = null;
        Repository latestRepo = (Repository) ethereum.getLastRepositorySnapshot();
//                ((Repository)ethereum.getRepository())
//                .getSnapshotTo(ethereum.getBlockchain().getBestBlock().getStateRoot());
//        JsonObject jsonResultObject = new JsonObject();

        switch (method) {

            case COMMAND_APIS_BLOCKNUMBER: {
                long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                String blockNumberHexString = objectToHexString(blockNumber);
                command = createJson(id, COMMAND_APIS_BLOCKNUMBER, blockNumberHexString);
                break;
            }

            case COMMAND_APIS_GETBALANCE: {
                // parameter
                String defaultBlockParameter;
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, COMMAND_APIS_GETBALANCE, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                } else if (params.length == 1) { // default
                    defaultBlockParameter = DEFAULTBLOCK_PARAMETER_LATEST;
                } else {
                    defaultBlockParameter = (String) params[1];
                }

                // getblocknumber
                long blockNumber = getBlockNumber(ethereum, defaultBlockParameter);
                if (blockNumber == 0) { // block data null
                    command = createJson(id, COMMAND_APIS_GETBALANCE, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }


                // get balance
                String address = (String) params[0];
                try {
                    byte[] addressByte = Hex.decode(address);
                    Repository repository = ((Repository) ethereum.getRepository())
                            .getSnapshotTo(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
                    BigInteger balance = repository.getBalance(addressByte);
                    ConsoleUtil.printlnBlue("[conduct] getBalance: " + balance.toString());
                    String balanceHexString = objectToHexString(balance);

                    command = createJson(id, COMMAND_APIS_GETBALANCE, balanceHexString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_GETBALANCE, null, e);
                }

                break;
            }

            case COMMAND_APIS_GETTRANSACTIONCOUNT: { // blocknumber 조회 불가 (latest만 가능)
                // parameter
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, COMMAND_APIS_GETTRANSACTIONCOUNT, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                // get transaction count
                String address = (String) params[0];
                try {
                    byte[] addressByte = Hex.decode(address);
                    BigInteger nonce = latestRepo.getNonce(addressByte);
                    String nonceHexString = objectToHexString(nonce);
                    command = createJson(id, COMMAND_APIS_GETTRANSACTIONCOUNT, nonceHexString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_GETTRANSACTIONCOUNT, null, e);
                }

                break;
            }

            case COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    String hashString = (String) params[0];
                    byte[] hash = Hex.decode(hashString);
                    int transactionCount = ethereum.getBlockchain().getBlockByHash(hash).getTransactionsList().size();
                    String transactionCountToHexString = objectToHexString(transactionCount);
                    command = createJson(id, COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH, transactionCountToHexString);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                break;
            }

            case COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    String blockNumberString = (String) params[0];
                    blockNumberString = blockNumberString.substring(2, blockNumberString.length());
                    long blockNumber = new BigInteger(blockNumberString, 16).longValue();
                    ConsoleUtil.printlnPurple("[conduct] blockNumber:"+blockNumberString + "  number:" + blockNumber);
                    int transactionCount = ethereum.getBlockchain().getBlockByNumber(blockNumber).getTransactionsList().size();
                    String transactionCountToHexString = objectToHexString(transactionCount);
                    command = createJson(id, COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER, transactionCountToHexString);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }

                break;
            }

            case COMMAND_APIS_GETCODE: {
                // parameter
                String defaultBlockParameter;
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, COMMAND_APIS_GETCODE, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                } else if (params.length == 1) { // default
                    defaultBlockParameter = DEFAULTBLOCK_PARAMETER_LATEST;
                } else {
                    defaultBlockParameter = (String) params[1];
                }

                // getblocknumber
                long blockNumber = getBlockNumber(ethereum, defaultBlockParameter);
                if (blockNumber == 0) { // block data null
                    command = createJson(id, COMMAND_APIS_GETCODE, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }


                // get code
                String address = (String) params[0];
                try {
                    byte[] addressByte = Hex.decode(address);
                    Repository repository = ((Repository) ethereum.getRepository())
                            .getSnapshotTo(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
                    byte[] code = repository.getCode(addressByte);
                    String codeString = ByteUtil.toHexString(code);
                    ConsoleUtil.printlnBlue("[conduct] getCode: " + codeString);

                    command = createJson(id, COMMAND_APIS_GETCODE, codeString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_GETCODE, null, e);
                }

                break;
            }

            case COMMAND_APIS_SIGN: {


                break;
            }

            case COMMAND_APIS_SENDTRANSACTION: { // apis 방식
                if (params.length == 0) { // error : (정보 부재)
                    command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String jsonData = (String) params[0];

                // check null params
                String gasString = null;
                String gasPriceString = null;
                String toAddressString = null;
                String dataMessage = null;
                int walletIndex = -1;
                String valueString = null;
                String keyStorePW = null;

                boolean isError = false;
                try {

                    gasString = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_GAS);
                    gasPriceString = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_GASPRICE);
                    toAddressString = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_TO);
                    walletIndex = RPCJsonUtil.getDecodeMessageInteger(jsonData, RPCCommand.TAG_WALLET_INDEX);
                    valueString = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_VALUE);
                    keyStorePW = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_KEYSTORE_PW);


                    long gasLimit = Long.parseLong(gasString);
                    BigInteger gasPrice = new BigInteger(gasPriceString);
                    byte[] toAddressByte = null;
                    if (!toAddressString.equals("")) {

                        // check address mask
                        if (toAddressString.contains("@")) {
                            toAddressByte = latestRepo.getAddressByMask(toAddressString);
                        } else {
                            toAddressByte = Hex.decode(toAddressString);
                        }
                    }

                    // check data
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_DATA)) {
                        dataMessage = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_DATA);
                    }
                    byte[] dataMessageByte = null;
                    if (dataMessage!=null) {
                        dataMessageByte = Hex.decode(dataMessage);
                    }

                    BigInteger value = new BigInteger(valueString);
                    String keystorePasswordEnc = keyStorePW;
                    String keystorePasswordDec = RPCJsonUtil.AESDecrypt(token, keystorePasswordEnc);

                    List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();
                    KeyStoreData key = keyStoreDataList.get(walletIndex);
                    byte[] privateKey = KeyStoreUtil.decryptPrivateKey(key.toString(), keystorePasswordDec);


                    ECKey senderKey = ECKey.fromPrivate(privateKey);

                    BigInteger nonce = ethereum.getRepository().getNonce(senderKey.getAddress());
                    int nextBlock = ethereum.getChainIdForNextBlock();

                    Transaction tx = new Transaction(
                            ByteUtil.bigIntegerToBytes(nonce),
                            ByteUtil.bigIntegerToBytes(gasPrice),
                            ByteUtil.longToBytesNoLeadZeroes(gasLimit),
                            toAddressByte,
                            ByteUtil.bigIntegerToBytes(value),
                            dataMessageByte,
                            nextBlock);


                    tx.sign(senderKey); // signing

                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_KNOWLEDGE_PW)) {
                        String knowledgePasswordEnc = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_KNOWLEDGE_PW);
                        String knowledgePasswordDec = RPCJsonUtil.AESDecrypt(token, knowledgePasswordEnc);

                        if (!knowledgePasswordDec.equals("")) {
                            tx.authorize(knowledgePasswordDec);
                        }
                    }

                    command = contractRun(id, method, ethereum, tx);
//                    command = createJson(id, method, ByteUtil.toHexString(tx.getEncoded()));  // signning txencode
                }

                catch (InvalidPasswordException e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_INVALID_PASSWORD);
                }

                catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_WALLETINDEX);
                }
                // unknown
                catch (Exception e) {
                    e.printStackTrace();
                    isError = true;
                }

                if (isError) {
                    if (gasString == null) { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_GAS); }
                    else if (gasPriceString == null) { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_GASPRICE); }
                    else if (toAddressString == null) { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_TOADDRESS); }
                    else if (walletIndex == -1) { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_WALLETINDEX); }
                    else if (valueString == null) { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_VALUE); }
                    else if (keyStorePW == null) { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_NULL_KEYSTORE_PW); }
                    else { command = createJson(id, COMMAND_APIS_SENDTRANSACTION, null, ERROR_MESSAGE_UNKNOWN); }
                }

                break;
            }

            case COMMAND_APIS_SENDRAWTRANSACTION: {
                if (params.length == 0) { // error : (정보 부재)
                    command = createJson(id, COMMAND_APIS_SENDRAWTRANSACTION, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String txHashString = (String) params[0];
                try {
                    byte[] txHash = Hex.decode(txHashString);
                    Transaction tx = new Transaction(txHash);
                    ethereum.submitTransaction(tx);

                    command = createJson(id, COMMAND_APIS_SENDRAWTRANSACTION, ByteUtil.toHexString(tx.getHash()));

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, COMMAND_APIS_SENDRAWTRANSACTION, null, e);
                }

                break;
            }

            case COMMAND_APIS_CALL: {
                if (params.length == 0) { // error : (정보 부재)
                    command = createJson(id, COMMAND_APIS_CALL, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String jsonData = (String) params[0];

                try {
                    BigInteger nonce = null;
                    BigInteger gasPrice = null;
                    long gasLimit = 0;
                    byte[] toByte = null;
                    BigInteger value = null;
                    byte[] dataByte = null;


                    // check to
                    String toString = RPCJsonUtil.getDecodeMessage(jsonData, TAG_TO);
                    if (!toString.equals("")) {

                        // check address mask
                        if (toString.contains("@")) {
                            toByte = latestRepo.getAddressByMask(toString);
                        } else {
                            toByte = Hex.decode(toString);
                        }
                    }

                    // (optional) check from
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_FROM)) {
                        String fromString = RPCJsonUtil.getDecodeMessage(jsonData, TAG_FROM);
                        byte[] fromByte = null;
                        if (!fromString.equals("")) {

                            // check address mask
                            if (fromString.contains("@")) {
                                fromByte = latestRepo.getAddressByMask(fromString);
                            } else {
                                fromByte = Hex.decode(fromString);
                            }
                        }

                        nonce = ethereum.getRepository().getNonce(fromByte);
                    }

                    // (optional) check gas
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_GAS)) {
                        String gasString = RPCJsonUtil.getDecodeMessage(jsonData, TAG_GAS);

                        try {
                            gasLimit = Long.parseLong(gasString);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    // (optional) check gas price
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_GASPRICE)) {
                        String gasPriceString = RPCJsonUtil.getDecodeMessage(jsonData, TAG_GASPRICE);
                        gasPrice = new BigInteger(gasPriceString);
                    }

                    // (optional) check value
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_VALUE)) {
                        String valueString = RPCJsonUtil.getDecodeMessage(jsonData, TAG_VALUE);
                        value = new BigInteger(valueString);
                    }

                    // (optional) check data
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_DATA)) {
                        String dataString = RPCJsonUtil.getDecodeMessage(jsonData, TAG_DATA);
                        dataByte = Hex.decode(dataString);
                    }


                    int nextBlock = ethereum.getChainIdForNextBlock();
                    Transaction tx = new Transaction(
                            ByteUtil.bigIntegerToBytes(nonce),
                            ByteUtil.bigIntegerToBytes(gasPrice),
                            ByteUtil.longToBytesNoLeadZeroes(gasLimit),
                            toByte,
                            ByteUtil.bigIntegerToBytes(value),
                            dataByte,
                            nextBlock);

                    ConsoleUtil.printlnBlue("[conduct] tx:" + tx.toString());
                    command = createJson(id, method, ByteUtil.toHexString(tx.getEncoded()));

                    //////////
               /*
                    ContractLoader.ContractRunEstimate contractRunEstimate
                            = ContractLoader.preRunContract((EthereumImpl) ethereum, tx);

                    boolean isPreRunSuccess = contractRunEstimate.isSuccess();
                    byte[] preGas = contractRunEstimate.getReceipt().getGasUsed();
                    String preRunError = contractRunEstimate.getReceipt().getError();
                    ConsoleUtil.printlnBlue("is:" + isPreRunSuccess + " pregas:" + preGas + " err" + preRunError);
                 */   //////////

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_GETBLOCKBYHASH: {
                // parameter
                boolean isFull = false;
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, COMMAND_APIS_GETBLOCKBYHASH, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                if (params.length >= 2) {
                    isFull = Boolean.parseBoolean((String)params[1]);
                }

                String blockHashString = (String) params[0];
                try {
                    byte[] hash = Hex.decode(blockHashString);
                    Block block = ethereum.getBlockchain().getBlockByHash(hash);


                    if(isFull) {
                        BlockData blockData = new BlockData(block);
                        command = createJson(id, method, blockData);
                    }
                    else {
                        List<Transaction> txList = block.getTransactionsList();
                        List<String> txHashList = new ArrayList<>();
                        for(Transaction tx : txList) {
                            txHashList.add(ByteUtil.toHexString(tx.getHash()));
                        }
                        ConsoleUtil.printlnBlue("[conduct] blockHash:" + block.getTransactionsList());
                        command = createJson(id, method, txHashList);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_NULL_BLOCK_BY_HASH);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_GETBLOCKBYNUMBER: {
                // parameter
                boolean isFull = false;
                if (params.length == 0) { // error : (blocknumber 부재)
                    command = createJson(id, COMMAND_APIS_GETBLOCKBYNUMBER, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                if (params.length >= 2) {
                    isFull = Boolean.parseBoolean((String)params[1]);
                }


                try {
                    long blocknumber = getBlockNumber(ethereum, (String)params[0]);
                    Block block = ethereum.getBlockchain().getBlockByNumber(blocknumber);

                    if(isFull) {
                        BlockData blockData = new BlockData(block);
                        command = createJson(id, method, blockData);
                    }
                    else {
                        List<Transaction> txList = block.getTransactionsList();
                        List<String> txHashList = new ArrayList<>();
                        for(Transaction tx : txList) {
                            txHashList.add(ByteUtil.toHexString(tx.getHash()));
                        }
                        ConsoleUtil.printlnBlue("[conduct] blockHash:" + block.getTransactionsList());
                        command = createJson(id, method, txHashList);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_NULL_BLOCK_BY_NUMBER);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }


                break;
            }


        }


        send(conn, token, command, isEncrypt);
    }

    public static String objectToHexString(Object object) {
        return String.format("0x%08X", object);
    }

    /**
     * getblocknumber
     * @param ethereum
     * @param blockParameter
     * @return (0 : null block error)
     */
    public static long getBlockNumber(Ethereum ethereum, String blockParameter) {
        long blockNumber = 0;

        switch (blockParameter) {
            case DEFAULTBLOCK_PARAMETER_EARLIEST:
                blockNumber = 1;
                break;

            case DEFAULTBLOCK_PARAMETER_LATEST:
                blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                break;

            default: // check long
                try {
                    if (blockParameter.contains("0x")) {
                        blockParameter = blockParameter.substring(2, blockParameter.length());
                    }
                    blockNumber = new BigInteger(blockParameter, 16).longValue();

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
        }

        return blockNumber;
    }

    // 전송시 사용
    public static void send(WebSocket conn, String token, String text, boolean isEncrypt) {
        if (isEncrypt) {
            text = RPCJsonUtil.AESEncrypt(token, text);
        }
        conn.send(text);
    }

    public static void send(WebSocket conn, String token, String text) {
        send(conn, token, text, false);
    }

    private static String contractRun(long id, String method, Ethereum ethereum, Transaction transaction) {
        ContractLoader.ContractRunEstimate contractRunEstimate = ContractLoader.preRunTransaction(ethereum, transaction);

        boolean isPreRunSuccess = contractRunEstimate.isSuccess();
        String preRunError = contractRunEstimate.getReceipt().getError();
        String returnCommand = "";

        // run
        if(isPreRunSuccess) {
            ConsoleUtil.printlnRed("[contractRun] success send transaction");

            ethereum.submitTransaction(transaction); // send
            returnCommand = createJson(id, method, ByteUtil.toHexString(transaction.getHash()));
        }
        else {
            ConsoleUtil.printlnRed("[contractRun] fail send transaction");

            returnCommand = createJson(id, method, null, preRunError);
        }

        return returnCommand;
    }
}
