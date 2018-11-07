package org.apis.rpc;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.crypto.ECKey;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.facade.SyncStatus;
import org.apis.keystore.InvalidPasswordException;
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreManager;
import org.apis.keystore.KeyStoreUtil;
import org.apis.rpc.template.*;
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
    static final String COMMAND_RPC_VERSION = "2.0";

    // method
    static final String COMMAND_NET_PEERCOUNT = "net_peerCount";

    static final String COMMAND_APIS_PROTOCOLVERSION = "apis_protocolVersion";
    static final String COMMAND_APIS_SYNCING = "apis_syncing";
    static final String COMMAND_APIS_COINBASE = "apis_coinbase";
    static final String COMMAND_APIS_MINING = "apis_mining";
    static final String COMMAND_APIS_ACCOUNTS = "apis_accounts";
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
    static final String COMMAND_APIS_GETTRANSACTIONBYHASH = "apis_getTransactionByHash";
    static final String COMMAND_APIS_GETTRANSACTIONBYBLOCKHASHANDINDEX = "apis_getTransactionByBlockHashAndIndex";
    static final String COMMAND_APIS_GETTRANSACTIONBYBLOCKNUMBERANDINDEX = "apis_getTransactionByBlockNumberAndIndex";
    static final String COMMAND_APIS_GETTRANSACTIONRECEIPT = "apis_getTransactionReceipt";

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
    static final String ERROR_MESSAGE_NULL_WALLETINDEX = "wrong (wallet)index.";
    static final String ERROR_MESSAGE_NULL_TOADDRESS = "there is no (to) address.";
    static final String ERROR_MESSAGE_NULL_TOMASK = "there is no (to) mask.";
    static final String ERROR_MESSAGE_NULL_GAS = "there is no gas.";
    static final String ERROR_MESSAGE_NULL_GASPRICE = "there is no gas price.";
    static final String ERROR_MESSAGE_NULL_VALUE = "there is no value.";
    static final String ERROR_MESSAGE_NULL_KEYSTORE_PW = "there is no keyStore password.";


    static final String ERROR_MESSAGE_INVALID_PASSWORD = "Invalid password.";
    static final String ERROR_MESSAGE_INVALID_TX = "The transaction data type is invalid.";


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
    static final String ERROR_NULL_TRANSACTION = "There is no transaction.";
    static final String ERROR_NULL_TRANSACTION_BY_HASH = "There is no transaction can be found with the hash.";
    static final String ERROR_NULL_TOADDRESS_OR_TOMASK = "There is no receiving address or mask.";
    static final String ERROR_NULL_BLOCK_BY_NUMBER = "There is no block can be found with the number.";
    static final String ERROR_NULL_BLOCK_BY_HASH = "There is no block can be found with the hash.";
    static final String ERROR_NULL_MASTERNODE_ADDRESS = "There is no address registered as masternode.";
    static final String ERROR_NULL_WALLET_ADDRESS = "There is no address registered as wallet.";

    static void conduct(Ethereum ethereum, WebSocket conn, String token, String payload, boolean isEncrypt) throws ParseException {
        MessageWeb3 message = new GsonBuilder().create().fromJson(payload, MessageWeb3.class);
        long id = message.getId();
        String method = message.getMethod();
        Object[] params = message.getParams().toArray();

        conduct(ethereum, conn, token, id, method, params, isEncrypt);
    }

    public static void conduct(Ethereum ethereum, WebSocket conn, String token, long id, String method, Object[] params, boolean isEncrypt) throws ParseException {

        String command = null;
        Repository latestRepo = (Repository) ethereum.getLastRepositorySnapshot();

        switch (method) {

            case COMMAND_NET_PEERCOUNT: {
                int count = ethereum.getChannelManager().getActivePeers().size(); // net peer count
                String countHexString = objectToHexString(count);
                command = createJson(id, method, countHexString);
                break;
            }

            case COMMAND_APIS_PROTOCOLVERSION: {
                command = createJson(id, method, COMMAND_RPC_VERSION);
                break;
            }

            case COMMAND_APIS_SYNCING: {
                long startingBlock = SyncStatus.getStartingBlock();
                long currentBlock = ethereum.getSyncStatus().getBlockLastImported();
                long highestBlock = ethereum.getSyncStatus().getBlockBestKnown();

                if (startingBlock == 0 && currentBlock == 0 && highestBlock == 0) {
                    command = createJson(id, method, false);
                }
                else {
                    ResultSyncingBlcokData data = new ResultSyncingBlcokData(startingBlock, currentBlock, highestBlock);
                    command = createJson(id, method, data);
                }
                break;
            }

            case COMMAND_APIS_COINBASE: {
                byte[] coinbase = SystemProperties.getDefault().getCoinbaseKey().getAddress();
                String address = ByteUtil.toHexString(coinbase);
                command = createJson(id, method, address);
                break;
            }

            case COMMAND_APIS_MINING: {
                boolean result = false;
                if (SystemProperties.getDefault().getCoinbaseKey() != null) {
                    result = true;
                }
                command = createJson(id, method, result);
                break;
            }

            case COMMAND_APIS_ACCOUNTS: {
                List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();
                List<String> addressList = new ArrayList<String>();
                for(KeyStoreData keyStoreData: keyStoreDataList) {
                    addressList.add(keyStoreData.address);
                }
                command = createJson(id, method, addressList);
                break;
            }

            case COMMAND_APIS_BLOCKNUMBER: {
                long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                String blockNumberHexString = objectToHexString(blockNumber);
                command = createJson(id, method, blockNumberHexString);
                break;
            }

            case COMMAND_APIS_GETBALANCE: {
                // parameter
                String defaultBlockParameter;
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                } else if (params.length == 1) { // default
                    defaultBlockParameter = DEFAULTBLOCK_PARAMETER_LATEST;
                } else {
                    defaultBlockParameter = (String) params[1];
                }

                // getblocknumber
                long blockNumber = getBlockNumber(ethereum, defaultBlockParameter);
                ConsoleUtil.printlnBlue("[conduct] blocknumber: " + blockNumber);
                if (blockNumber == 0) { // block data null
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }


                // get balance
                String address = (String) params[0];
                try {
                    byte[] addressByte = ByteUtil.hexStringToBytes(address);
                    Repository repository = ((Repository) ethereum.getRepository())
                            .getSnapshotTo(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
                    BigInteger balance = repository.getBalance(addressByte);
                    ConsoleUtil.printlnBlue("[conduct] getBalance: " + balance.toString());
                    String balanceHexString = objectToHexString(balance);

                    command = createJson(id, method, balanceHexString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_GETTRANSACTIONCOUNT: { // blocknumber 조회 불가 (latest만 가능)
                // parameter
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                // get transaction count
                String address = (String) params[0];
                try {
                    byte[] addressByte = ByteUtil.hexStringToBytes(address);
                    BigInteger nonce = latestRepo.getNonce(addressByte);
                    String nonceHexString = objectToHexString(nonce);
                    command = createJson(id, method, nonceHexString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    String hashString = (String) params[0];
                    byte[] hash = ByteUtil.hexStringToBytes(hashString);
                    int transactionCount = ethereum.getBlockchain().getBlockByHash(hash).getTransactionsList().size();
                    String transactionCountToHexString = objectToHexString(transactionCount);
                    command = createJson(id, method, transactionCountToHexString);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                break;
            }

            case COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    String blockNumberString = (String) params[0];
                    if (blockNumberString.startsWith("0x")) {
                        blockNumberString = blockNumberString.replace("0x","");
                    }
                    long blockNumber = new BigInteger(blockNumberString, 16).longValue();
                    ConsoleUtil.printlnPurple("[conduct] blockNumber:"+blockNumberString + "  number:" + blockNumber);
                    int transactionCount = ethereum.getBlockchain().getBlockByNumber(blockNumber).getTransactionsList().size();
                    String transactionCountToHexString = objectToHexString(transactionCount);
                    command = createJson(id, method, transactionCountToHexString);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }

                break;
            }

            case COMMAND_APIS_GETCODE: {
                // parameter
                String defaultBlockParameter;
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
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
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }


                // get code
                String address = (String) params[0];
                try {
                    byte[] addressByte = ByteUtil.hexStringToBytes(address);
                    Repository repository = ((Repository) ethereum.getRepository())
                            .getSnapshotTo(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
                    byte[] code = repository.getCode(addressByte);
                    String codeString = ByteUtil.toHexString(code);
                    ConsoleUtil.printlnBlue("[conduct] getCode: " + codeString);

                    command = createJson(id, method, codeString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                }

                break;
            }

            case COMMAND_APIS_SIGN: {


                break;
            }

            case COMMAND_APIS_SENDTRANSACTION: { // apis 방식
                if (params.length == 0) { // error : (정보 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
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
                            toAddressByte = ByteUtil.hexStringToBytes(toAddressString);
                        }
                    }

                    // check data
                    if (RPCJsonUtil.hasJsonObject(jsonData, TAG_DATA)) {
                        dataMessage = RPCJsonUtil.getDecodeMessage(jsonData, RPCCommand.TAG_DATA);
                    }
                    byte[] dataMessageByte = null;
                    if (dataMessage!=null) {
                        dataMessageByte = ByteUtil.hexStringToBytes(dataMessage);
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
                    command = createJson(id, method, null, ERROR_MESSAGE_INVALID_PASSWORD);
                }

                catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_WALLETINDEX);
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
                    else { command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN); }
                }

                break;
            }

            case COMMAND_APIS_SENDRAWTRANSACTION: {
                if (params.length == 0) { // error : (정보 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String txHashString = (String) params[0];
                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    Transaction tx = new Transaction(txHash);
                    ethereum.submitTransaction(tx);

                    command = createJson(id, method, ByteUtil.toHexString(tx.getHash()));

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_CALL: {
                // 전달된 parameter가 없는 경우
                if (params.length == 0) {
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }
                // 트랜잭션 데이터 형식이 맞지 않았을 경우
                else if(!(params[0] instanceof LinkedTreeMap)) {
                    command = createJson(id, method, null, ERROR_MESSAGE_INVALID_TX);
                    send(conn, token, command, isEncrypt);
                    return;
                }
                Web3ParamTransaction inputTx = new Web3ParamTransaction(params[0]);

                ContractLoader.ContractRunEstimate preRun = ContractLoader.preRunContract((EthereumImpl) ethereum, inputTx.getFrom(), inputTx.getTo(), inputTx.getData());

                byte[] result = preRun.getReceipt().getExecutionResult();

                command = createJson(id, method, ByteUtil.toHexString(result));
                break;
            }

            case COMMAND_APIS_GETBLOCKBYHASH: {
                // parameter
                boolean isFull = false;
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                if (params.length >= 2) {
                    isFull = (boolean) params[1];
                }

                String blockHashString = (String) params[0];
                try {
                    byte[] hash = ByteUtil.hexStringToBytes(blockHashString);
                    Block block = ethereum.getBlockchain().getBlockByHash(hash);

                    BlockData blockData = new BlockData(block, isFull);
                    command = createJson(id, method, blockData);
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
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                if (params.length >= 2) {
                    isFull = (boolean) params[1];
                }


                try {
                    long blocknumber = getBlockNumber(ethereum, (String)params[0]);
                    Block block = ethereum.getBlockchain().getBlockByNumber(blocknumber);

                    BlockData blockData = new BlockData(block, isFull);
                    command = createJson(id, method, blockData);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_NULL_BLOCK_BY_NUMBER);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }

                break;
            }

            // parameter
            // 0: transaction Hash (hex string)
            case COMMAND_APIS_GETTRANSACTIONBYHASH: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String txHashString = (String) params[0];

                if (txHashString.startsWith("0x")) {
                    txHashString = txHashString.replace("0x","");
                }

                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    TransactionInfo txInfo = ethereum.getTransactionInfo(txHash);

                    if (txInfo == null || txInfo.getReceipt() == null) {
                        command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                    }
                    else {
                        TransactionData txData = new TransactionData(txInfo.getReceipt().getTransaction(), ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));

                        String errStr = txInfo.getReceipt().getError();
                        if (errStr.equals("")) { errStr = null; }
                        command = createJson(id, method, txData, errStr);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                }

                break;
            }

            // parameter
            // 0: block Hash (hex string)
            // 1: transaction index position (hex string)
            case COMMAND_APIS_GETTRANSACTIONBYBLOCKHASHANDINDEX: {
                if (params.length < 2) { // error : (parameter 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String blockHashString = (String) params[0];
                String indexPositionHexString = (String) params[1];

                try {
                    byte[] blockHash = ByteUtil.hexStringToBytes(blockHashString);
                    if (indexPositionHexString.startsWith("0x")) {
                        indexPositionHexString = indexPositionHexString.replace("0x","");
                    }
                    int indexPosition = new BigInteger(indexPositionHexString, 16).intValue();

                    Block block = ethereum.getBlockchain().getBlockByHash(blockHash);
                    if (block == null) {
                        command = createJson(id, method, null, ERROR_NULL_BLOCK_BY_HASH);
                        send(conn, token, command, isEncrypt);
                        return;
                    }

                    List<Transaction> txList = block.getTransactionsList();
                    Transaction transaction = txList.get(indexPosition);

                    if (transaction == null) {
                        command = createJson(id, method, null, ERROR_NULL_TRANSACTION);
                    }
                    else {
                        command = createJson(id, method, transaction.toString());
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION);
                }

                break;
            }


            // parameter
            // 0: block number (hex string) or default block parameter (string)
            // 1: transaction index position (hex string)
            case COMMAND_APIS_GETTRANSACTIONBYBLOCKNUMBERANDINDEX: {
                if (params.length < 2) { // error : (parameter 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String blockNumberString = (String) params[0];
                String indexPositionHexString = (String) params[1];

                try {
                    long blockNumber = getBlockNumber(ethereum, blockNumberString);
                    if (indexPositionHexString.startsWith("0x")) {
                        indexPositionHexString = indexPositionHexString.replace("0x","");
                    }
                    int indexPosition = new BigInteger(indexPositionHexString, 16).intValue();

                    Block block = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                    if (block == null) {
                        command = createJson(id, method, null, ERROR_NULL_BLOCK_BY_NUMBER);
                        send(conn, token, command, isEncrypt);
                        return;
                    }

                    List<Transaction> txList = block.getTransactionsList();
                    Transaction transaction = txList.get(indexPosition);

                    if (transaction == null) {
                        command = createJson(id, method, null, ERROR_NULL_TRANSACTION);
                    } else {
                        command = createJson(id, method, transaction.toString());
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION);
                }

                break;
            }

            // parameter
            // 0: transaction Hash (hex string)
            case COMMAND_APIS_GETTRANSACTIONRECEIPT: {
                if (params.length < 1) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String txHashString = (String) params[0];
                if (txHashString.startsWith("0x")) {
                    txHashString = txHashString.replace("0x","");
                }
                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    TransactionInfo txInfo = ethereum.getTransactionInfo(txHash);

                    if (txInfo == null || txInfo.getReceipt() == null) {
                        command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                    }
                    else {
                        TransactionReceiptData txReceiptData
                                = new TransactionReceiptData(txInfo, ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));

                        String errStr = txInfo.getReceipt().getError();
                        if (errStr.equals("")) { errStr = null; }
                        command = createJson(id, method, txReceiptData, errStr);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
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
                    if (blockParameter.startsWith("0x")) {
                        blockParameter = blockParameter.replace("0x","");
                        blockNumber = new BigInteger(blockParameter, 16).longValue();
                    }
                    else {
                        blockNumber = Long.parseLong(blockParameter);
                    }

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
