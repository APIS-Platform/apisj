package org.apis.rpc;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.contract.EstimateTransaction;
import org.apis.contract.EstimateTransactionResult;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TransactionRecord;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.facade.SyncStatus;
import org.apis.keystore.*;
import org.apis.listener.BlockReplay;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.listener.LastLogListener;
import org.apis.rpc.listener.LogListener;
import org.apis.rpc.listener.NewBlockListener;
import org.apis.rpc.listener.PendingTransactionListener;
import org.apis.rpc.template.*;
import org.apis.util.*;
import org.apis.util.blockchain.ApisUtil;
import org.java_websocket.WebSocket;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.DecoderException;

import java.math.BigInteger;
import java.util.*;

import static org.apis.rpc.RPCJsonUtil.createJson;

public class RPCCommand {
    static final String COMMAND_RPC_VERSION = "2.0";

    // method
    static final String COMMAND_NET_VERSION = "net_version";
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
    static final String COMMAND_APIS_ESTIMATE_GAS = "apis_estimateGas";
    static final String COMMAND_APIS_GAS_PRICE = "apis_gasPrice";

    private static final String COMMAND_APIS_GETBLOCKBYHASH = "apis_getBlockByHash";
    private static final String COMMAND_APIS_GETBLOCKBYNUMBER = "apis_getBlockByNumber";
    private static final String COMMAND_APIS_GETTRANSACTIONBYHASH = "apis_getTransactionByHash";
    private static final String COMMAND_APIS_GETTRANSACTIONBYBLOCKHASHANDINDEX = "apis_getTransactionByBlockHashAndIndex";
    private static final String COMMAND_APIS_GETTRANSACTIONBYBLOCKNUMBERANDINDEX = "apis_getTransactionByBlockNumberAndIndex";
    private static final String COMMAND_APIS_GETTRANSACTIONRECEIPT = "apis_getTransactionReceipt";
    private static final String COMMAND_APIS_GETTRANSACTIONBYKEYWORD = "apis_getTransactionsByKeyword";

    // apis only
    public static final String COMMAND_APIS_GETWALLETINFO = "apis_getWalletInfo";
    public static final String COMMAND_APIS_GETMNLIST = "apis_getMnList";
    public static final String COMMAND_APIS_GETMNINFO = "apis_getMnInfo";
    public static final String COMMAND_APIS_REGISTERKNOWLEDGEKEY = "apis_registerKnowledgeKey";

    private static final String COMMAND_PERSONAL_NEW_ACCOUNT = "personal_newAccount";
    private static final String COMMAND_PERSONAL_SIGN = "personal_sign";
    private static final String COMMAND_PERSONAL_EC_RECOVER = "personal_ecRecover";
    private static final String COMMAND_PERSONAL_SIGN_TRANSACTION = "personal_signTransaction";

    private static final String COMMAND_APIS_SUBSCRIBE = "apis_subscribe";
    private static final String COMMAND_APIS_UNSUBSCRIBE = "apis_unsubscribe";
    private static final String COMMAND_APIS_GET_LOGS = "apis_getLogs";

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
    static final String DEFAULT_PARAMETER_BLOCK_LATEST = "latest";
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
    static final String ERROR_MESSAGE_NULL_PARAMETER = "there is no value."; // 조회할 값이 없다
    static final String ERROR_MESSAGE_NULL_KEYSTORE_PW = "The password for the KeyStore file is missing.";


    static final String ERROR_MESSAGE_INVALID_PASSWORD = "Invalid password.";
    static final String ERROR_MESSAGE_INVALID_TX = "The transaction data type is invalid.";

    static final String ERROR_PARAMETER_SIZE = "The number of input parameters for '%s' must be %d.";


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
    static final String ERROR_OUT_OF_INDEXED_TRANSACTION = "The index you entered does not exist in the block. (input : %d, index size: %d).";
    static final String ERROR_NULL_TRANSACTION_BY_HASH = "There is no transaction can be found with the hash.";
    static final String ERROR_NULL_TOADDRESS_OR_TOMASK = "There is no receiving address or mask.";
    static final String ERROR_NULL_BLOCK_BY_NUMBER = "There is no block can be found with the number.";
    static final String ERROR_NULL_BLOCK_BY_HASH = "There is no block can be found with the hash.";
    static final String ERROR_NULL_MASTERNODE_ADDRESS = "There is no address registered as masternode.";
    static final String ERROR_NULL_WALLET_ADDRESS = "There is no address registered as wallet.";
    static final String ERROR_NULL_SENDER = "Sender address does not exist.";


    static final HashMap<BigInteger, EthereumListener> mListeners = new HashMap<>();



    static void conduct(Ethereum ethereum, WebSocket conn, String token, String payload, boolean isEncrypt) throws ParseException {
        MessageWeb3 message = new GsonBuilder().create().fromJson(payload, MessageWeb3.class);
        long id = message.getId();
        String method = message.getMethod();
        Object[] params = message.getParams().toArray();

        if(!isPendingTxListenerRegistered) {
            isPendingTxListenerRegistered = true;
            ethereum.addListener(pendingTxListener);
        }

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
                String address = ByteUtil.toHexString0x(coinbase);
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

            // parameter
            // 0: (optional) address (hex string) or mask
            case COMMAND_APIS_ACCOUNTS: {
                List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();
                List<WalletInfo> walletInfos = new ArrayList<>();
                byte[] targetAddress = null;
                if (params.length > 0) {
                    byte[] address = getAddressByte(latestRepo, (String)params[0]);
                    if (address==null) {
                        command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                        send(conn, token, command, isEncrypt);
                        return;
                    }
                    targetAddress = address;
                }



                try {
                    long lastBlockNumber = getBlockNumber(ethereum, DEFAULT_PARAMETER_BLOCK_LATEST);
                    int index = 0;
                    for (KeyStoreData keyStoreData : keyStoreDataList) {
                        int walletIndex = index++;
                        byte[] address = ByteUtil.hexStringToBytes(keyStoreData.address);
                        if (targetAddress != null) {
                            if (!FastByteComparisons.equal(targetAddress, address)) {
                                continue;
                            }
                        }

                        String mask = latestRepo.getMaskByAddress(address);

                        BigInteger attoAPIS = latestRepo.getBalance(address);
                        BigInteger attoMNR = latestRepo.getMineral(address, lastBlockNumber);
                        //BigInteger nonce = latestRepo.getNonce(address);
                        BigInteger nonce = ethereum.getPendingState().getNonce(address);
                        byte[] proofKey = latestRepo.getProofKey(address);
                        boolean isMasternode = false;

                        AccountState accountState = latestRepo.getAccountState(address);
                        if (accountState != null) {
                            if (accountState.getMnStartBlock().compareTo(BigInteger.ZERO) > 0) {
                                isMasternode = true;
                            }
                        }

                        WalletInfo walletInfo = new WalletInfo(walletIndex, address, mask, attoAPIS, attoMNR, nonce, proofKey, null, isMasternode);
                        walletInfos.add(walletInfo);
                    }

                    if (walletInfos.size() > 0) { command = createJson(id, method, walletInfos); }
                    else { command = createJson(id, method, null, ERROR_NULL_WALLET_ADDRESS); }

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                }

                break;
            }

            case COMMAND_APIS_BLOCKNUMBER: {
                long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                String blockNumberHexString = objectToHexString(blockNumber);
                command = createJson(id, method, blockNumberHexString);
                break;
            }

            // parameter
            // 0: address (hex string) or mask
            // 1: block number (hex string) or default block parameter (string)
            case COMMAND_APIS_GETBALANCE: {
                // parameter
                String blockNumberParam;
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                } else if (params.length == 1) { // default
                    blockNumberParam = DEFAULT_PARAMETER_BLOCK_LATEST;
                } else {
                    blockNumberParam = (String) params[1];
                }

                long blockNumber = getBlockNumber(ethereum, blockNumberParam);
                if (blockNumber == 0) { // block data null
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }


                try {
                    Block block = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                    Repository repository = ((Repository) ethereum.getRepository()).getSnapshotTo(block.getStateRoot());
                    byte[] address = getAddressByte(repository, (String)params[0]);
                    if (address==null) {
                        command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                        send(conn, token, command, isEncrypt);
                        return;
                    }

                    BigInteger balance = repository.getBalance(address);
                    BigInteger mineral = repository.getMineral(address, block.getNumber());
                    BalanceData balanceData = new BalanceData(balance, mineral);
                    command = createJson(id, method, balanceData);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }

                break;
            }

            // parameter
            // 0: address (hex string) or mask
            case COMMAND_APIS_GETTRANSACTIONCOUNT: { // blocknumber 조회 불가 (latest만 가능)
                // parameter
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                byte[] address = getAddressByte(latestRepo, (String)params[0]);
                if (address==null) {
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                // get transaction count
                try {
                    BigInteger nonce = ethereum.getPendingState().getNonce(address);
                    String nonceHexString = objectToHexString(nonce);
                    command = createJson(id, method, nonceHexString);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, e);
                }

                break;
            }

            // parameter
            // 0: block Hash (hex string)
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

            // parameter
            // 0: block number (hex string) or default block parameter (string)
            case COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    long blockNumber = getBlockNumber(ethereum, (String) params[0]);
                    if (blockNumber == 0) { // block data null
                        command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                        send(conn, token, command, isEncrypt);
                        return;
                    }

                    int transactionCount = ethereum.getBlockchain().getBlockByNumber(blockNumber).getTransactionsList().size();
                    String transactionCountToHexString = objectToHexString(transactionCount);
                    command = createJson(id, method, transactionCountToHexString);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }

                break;
            }

            // parameter
            // 0: address (hex string) or mask
            // 1: block number (hex string) or default block parameter (string)
            case COMMAND_APIS_GETCODE: {
                // parameter
                String defaultBlockParameter;
                if (params.length == 0) { // error : (주소 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                } else if (params.length == 1) { // default
                    defaultBlockParameter = DEFAULT_PARAMETER_BLOCK_LATEST;
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

                try {
                    Repository repository = ((Repository) ethereum.getRepository())
                            .getSnapshotTo(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
                    byte[] address = getAddressByte(repository, (String)params[0]);
                    if (address==null) {
                        command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                        send(conn, token, command, isEncrypt);
                        return;
                    }

                    // get code
                    byte[] code = repository.getCode(address);
                    String codeString = ByteUtil.toHexString0x(code);

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

                    BigInteger nonce = ethereum.getPendingState().getNonce(senderKey.getAddress());
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
                    BigInteger key = ByteUtil.bytesToBigInteger(tx.getHash());
                    if (!txPendingResults.containsKey(key)) {
                        txPendingResults.put(key, new TransactionPendingResult());
                    }

                    ethereum.submitTransaction(tx);

                    command = createJson(id, method, ByteUtil.toHexString0x(tx.getHash()));

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_CALL: {
                try {
                    EstimateTransactionResult estimateResult = estimateTransaction(params, (EthereumImpl) ethereum);
                    byte[] result = estimateResult.getReceipt().getExecutionResult();
                    command = createJson(id, method, ByteUtil.toHexString0x(result));
                } catch (Exception e) {
                    command = createJson(id, method, null, e.getMessage());
                }
                break;
            }


            case COMMAND_APIS_ESTIMATE_GAS: {
                try {
                    EstimateTransactionResult estimateResult = estimateTransaction(params, (EthereumImpl) ethereum);
                    command = createJson(id, method, ByteUtil.toHexString0x(ByteUtil.longToBytes(estimateResult.getGasUsed())));
                } catch (Exception e) {
                    command = createJson(id, method, null, e.getMessage());
                }
                break;
            }

            case COMMAND_APIS_GAS_PRICE: {
                try {
                    command = createJson(id, method, ByteUtil.toHexString0x(ByteUtil.longToBytes(ethereum.getGasPrice())));
                } catch (Exception e) {
                    command = createJson(id, method, null, e.getMessage());
                }
                break;
            }

            // 0: block Hash (hex string)
            // 1: boolean isFull
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

            // 0: block number
            // 1: boolean isFull
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

                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    TransactionInfo txInfo = ethereum.getTransactionInfo(txHash);

                    if (txInfo == null || txInfo.getReceipt() == null) {
                        //command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                        TransactionPendingResult result = txPendingResults.get(ByteUtil.bytesToBigInteger(txHash));

                        if(!result.getErr().isEmpty()) {
                            command = createJson(id, method, result.getErr());
                        } else {
                            command = createJson(id, method, "null");
                        }
                    }
                    else {
                        TransactionData txData = new TransactionData(txInfo.getReceipt().getTransaction(), ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
                        txData.setTransactionIndex(txInfo.getIndex());

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

            case COMMAND_APIS_GETTRANSACTIONBYKEYWORD: {
                if (params.length == 0) { // error : (hash 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    String txHashString = (String) params[0];
                    if (txHashString.contains("@")) {
                        byte[] address = ethereum.getRepository().getAddressByMask(txHashString);
                        txHashString = ByteUtil.toHexString(address);
                    }

                    long rowCount = 0;
                    long offset = 0;

                    if(params.length > 1) {
                        try {
                            rowCount = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[1]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }
                    if(params.length > 2) {
                        try {
                            offset = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[2]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }

                    DBManager dbManager = DBManager.getInstance();
                    List<TransactionRecord> txRecords = dbManager.selectTransactions(txHashString, rowCount, offset);
                    List<TransactionSearchData> txReceipts = new ArrayList<>();
                    for(TransactionRecord txRecord : txRecords) {
                        byte[] txHash = ByteUtil.hexStringToBytes(txRecord.getHash());
                        TransactionInfo txInfo = ethereum.getTransactionInfo(txHash);
                        Block block = ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash());
                        txReceipts.add(new TransactionSearchData(new TransactionReceiptData(txInfo, block)));
                    }

                    command = createJson(id, method, txReceipts);
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
                byte[] blockHash = ByteUtil.hexStringToBytes(blockHashString);
                Block block = ethereum.getBlockchain().getBlockByHash(blockHash);

                BigInteger indexBi = BIUtil.toBI(ByteUtil.hexStringToBytes((String) params[1]));

                command = getCommandTransactionByBlock(id, method, block, indexBi.intValue());
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

                BigInteger blockNumberBi = BIUtil.toBI(ByteUtil.hexStringToBytes((String) params[0]));
                Block block = ethereum.getBlockchain().getBlockByNumber(blockNumberBi.longValue());

                BigInteger indexBi = BIUtil.toBI(ByteUtil.hexStringToBytes((String) params[1]));

                command = getCommandTransactionByBlock(id, method, block, indexBi.intValue());
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

                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    TransactionInfo txInfo = ethereum.getTransactionInfo(txHash);

                    if (txInfo == null || txInfo.getReceipt() == null) {
                        TransactionPendingResult result = txPendingResults.get(ByteUtil.bytesToBigInteger(txHash));

                        if(result != null && result.getErr() != null && !result.getErr().isEmpty()) {
                            command = createJson(id, method, null, result.getErr());
                        } else {
                            command = createJson(id, method, "null");
                        }
                    }
                    else {
                        TransactionReceiptData txReceiptData = new TransactionReceiptData(txInfo, ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));

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

            case COMMAND_NET_VERSION: {
                command = createJson(id, method, ethereum.getChainIdForNextBlock());
                break;
            }

            // parameter
            // 0: address (hex string) or mask
            case COMMAND_APIS_GETWALLETINFO: {
                if (params.length == 0) { // error : (address or mask 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_PARAMETER);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String paramAddr = (String) params[0];

                byte[] address;
                // is mask : result address
                if (paramAddr.contains("@")) {
                    address = latestRepo.getAddressByMask(paramAddr);

                    if(address == null || address.length == 0) {
                        command = createJson(id, method, null, ERROR_NULL_MASK_BY_ADDRESS);
                        send(conn, token, command, isEncrypt);
                        return;
                    }
                } else {
                    address = ByteUtil.hexStringToBytes(paramAddr);
                }

                if(address == null || address.length == 0) {
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                } else {

                    AccountState state = latestRepo.getAccountState(address);

                    if (state == null) {
                        state = new AccountState(SystemProperties.getDefault());
                    }

                    String mask = state.getAddressMask();

                    BigInteger attoAPIS = state.getBalance();
                    BigInteger attoMNR = state.getMineral(ethereum.getBlockchain().getBestBlock().getNumber());
                    BigInteger nonce = ethereum.getPendingState().getNonce(address);
                    byte[] proofKey = state.getProofKey();
                    String isContract = null;
                    boolean isMasternode = false;
                    byte[] codeHash = state.getCodeHash();
                    if (codeHash != null && !FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH)) {
                        isContract = Boolean.toString(true);
                    }
                    if (state.getMnStartBlock().compareTo(BigInteger.ZERO) > 0) {
                        isMasternode = true;
                    }

                    WalletInfo walletInfo = new WalletInfo(-1, address, mask, attoAPIS, attoMNR, nonce, proofKey, isContract, isMasternode);

                    command = createJson(id, method, walletInfo);
                }
                break;
            }

            case COMMAND_PERSONAL_NEW_ACCOUNT: {
                if (params.length < 1) { // error : (비밀번호를 받지 못했음)
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_KEYSTORE_PW);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                byte[] privateKey = KeyStoreManager.getInstance().createPrivateKey((String) params[0]);
                command = createJson(id, method, ByteUtil.toHexString0x(ECKey.fromPrivate(privateKey).getAddress()));
                break;
            }

            case COMMAND_PERSONAL_SIGN: {
                if (params.length < 3) { // error : (비밀번호를 받지 못했음)
                    command = createJson(id, method, null, String.format(ERROR_PARAMETER_SIZE, COMMAND_PERSONAL_SIGN, 3));
                    send(conn, token, command, isEncrypt);
                    return;
                }

                byte[] dataToSign = ByteUtil.hexStringToBytes((String)params[0]);
                byte[] address = ByteUtil.hexStringToBytes((String)params[1]);
                String password = (String)params[2];

                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();

                try {
                    ECKey key = keyStoreManager.findKeyStoreFile(address, password);
                    if(key == null) {
                        command = createJson(id, method, null, "Input address not found.");
                    } else {
                        byte[] signedMessage = key.sign(HashUtil.sha3(dataToSign)).toByteArray();
                        command = createJson(id, method, ByteUtil.toHexString0x(signedMessage));
                    }
                } catch (InvalidPasswordException e) {
                    command = createJson(id, method, null, ERROR_MESSAGE_INVALID_PASSWORD);
                } catch (KeystoreVersionException e) {
                    command = createJson(id, method, null, "Support on V3 of Keystore");
                } catch (NotSupportKdfException e) {
                    command = createJson(id, method, null, "Not supported KDF");
                } catch (NotSupportCipherException e) {
                    command = createJson(id, method, null, "Not supported Cipher");
                }
                break;
            }

            case COMMAND_PERSONAL_EC_RECOVER: {
                if (params.length < 2) { // error : (비밀번호를 받지 못했음)
                    command = createJson(id, method, null, String.format(ERROR_PARAMETER_SIZE, COMMAND_PERSONAL_EC_RECOVER, 2));
                    send(conn, token, command, isEncrypt);
                    return;
                }

                byte[] dataSigned = ByteUtil.hexStringToBytes((String)params[0]);
                byte[] signatureBytes = ByteUtil.hexStringToBytes((String)params[1]);

                ECKey.ECDSASignature signature = KeyStoreUtil.decodeSignature(signatureBytes);
                ECKey recoveredKey = ECKey.recoverFromSignature(0, signature, HashUtil.sha3(dataSigned));
                command = createJson(id, method, ByteUtil.toHexString0x(recoveredKey.getAddress()));
                break;
            }

            case COMMAND_PERSONAL_SIGN_TRANSACTION: {
                if (params.length < 2) { // error : (비밀번호를 받지 못했음)
                    command = createJson(id, method, null, String.format(ERROR_PARAMETER_SIZE, COMMAND_PERSONAL_SIGN_TRANSACTION, 2));
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String txJson = new GsonBuilder().create().toJson(params[0]);
                String password = (String)params[1];


                TransactionData txData = new GsonBuilder().create().fromJson(txJson, TransactionData.class);

                if(txData.getFrom() == null || txData.getFrom().isEmpty()) {
                    command = createJson(id, method, null, ERROR_NULL_SENDER);
                } else {
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();
                    try {
                        ECKey key = keyStoreManager.findKeyStoreFile(ByteUtil.hexStringToBytes(txData.getFrom()), password);
                        if(key == null) {
                            command = createJson(id, method, null, "The address entered was not found in the stored address list.");
                        } else {
                            if(txData.getNonce() < 0) {
                                txData.setNonce(ethereum.getPendingState().getNonce(key.getAddress()).longValue());
                            }

                            if(txData.isEmptyTo() && !txData.isEmptyToMask()) {
                                txData.setTo(ethereum.getRepository().getAddressByMask(txData.getToMask()));
                            }

                            if(txData.isGasPriceEmpty()) {
                                txData.setGasPrice(ByteUtil.longToBytes(ethereum.getGasPrice()));
                            }

                            if(txData.isEmptyGas()) {
                                EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)ethereum);

                                txData.setGas(BigInteger.valueOf(50_000_000L));
                                Transaction tempTx = txData.getTransaction(ethereum.getChainIdForNextBlock());

                                EstimateTransactionResult estimateResult = estimator.estimate(tempTx);
                                txData.setGas(BigInteger.valueOf(estimateResult.getGasUsed()));
                            }

                            Transaction tx = txData.getTransaction(ethereum.getChainIdForNextBlock());
                            tx.sign(key);

                            TransactionData finalData = new TransactionData(tx, null);
                            command = createJson(id, method, new SignTransactionData(tx.getEncoded(), finalData));
                        }
                    } catch (InvalidPasswordException e) {
                        command = createJson(id, method, null, ERROR_MESSAGE_INVALID_PASSWORD);
                    } catch (KeystoreVersionException e) {
                        command = createJson(id, method, null, "Support on V3 of Keystore");
                    } catch (NotSupportKdfException e) {
                        command = createJson(id, method, null, "Not supported KDF");
                    } catch (NotSupportCipherException e) {
                        command = createJson(id, method, null, "Not supported Cipher");
                    }
                }
                break;
            }

            // parameter
            // 0: block number (hex string) or default block parameter (string)
            case COMMAND_APIS_GETMNLIST: {
                String blockNumberParam;
                if (params.length == 0) { // default
                    blockNumberParam = DEFAULT_PARAMETER_BLOCK_LATEST;
                } else {
                    blockNumberParam = (String) params[0];
                }

                long blockNumber = getBlockNumber(ethereum, blockNumberParam);
                if (blockNumber == 0) { // block data null
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    Block block = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                    Repository repository = ((Repository) ethereum.getRepository()).getSnapshotTo(block.getStateRoot());
                    SystemProperties config = SystemProperties.getDefault();
                    final Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

                    List<byte[]> generalEarlybird = repository.getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_EARLY());
                    List<byte[]> generalNormal = repository.getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_NORMAL());
                    List<byte[]> generalLate = repository.getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_LATE());

                    List<byte[]> majorEarlybird = repository.getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_EARLY_RUN());
                    List<byte[]> majorNormal = repository.getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_NORMAL());
                    List<byte[]> majorLate = repository.getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_LATE());

                    List<byte[]> privateEarlybird = repository.getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN());
                    List<byte[]> privateNormal = repository.getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_NORMAL());
                    List<byte[]> privateLate = repository.getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_LATE());

                    List<byte[]> allGeneral = new ArrayList<>(generalEarlybird);
                    allGeneral.addAll(generalNormal);
                    allGeneral.addAll(generalLate);

                    List<byte[]> allMajor = new ArrayList<>(majorEarlybird);
                    allMajor.addAll(majorNormal);
                    allMajor.addAll(majorLate);

                    List<byte[]> allPrivate = new ArrayList<>(privateEarlybird);
                    allPrivate.addAll(privateNormal);
                    allPrivate.addAll(privateLate);

                    MasterNodeListInfo masterNodeListInfo = new MasterNodeListInfo(allGeneral, allMajor, allPrivate);
                    command = createJson(id, method, masterNodeListInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }
                break;
            }

            // parameter
            // 0: address (hex string) or mask
            case COMMAND_APIS_GETMNINFO: {
                if (params.length == 0) { // error : (address 부재)
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                byte[] address = getAddressByte(latestRepo, (String)params[0]);
                if (address==null) {
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                try {
                    long startBlock = latestRepo.getMnStartBlock(address);
                    long lastBlock = latestRepo.getMnLastBlock(address);
                    byte[] receiptAddress = latestRepo.getMnRecipient(address);
                    BigInteger balance = latestRepo.getMnStartBalance(address);

                    MasterNodeInfo masterNodeInfo = new MasterNodeInfo(
                            startBlock
                            , lastBlock
                            , ByteUtil.toHexString0x(receiptAddress)
                            , ApisUtil.readableApis(balance)
                    );
                    command = createJson(id, method, masterNodeInfo);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                }

            }

            case COMMAND_APIS_SUBSCRIBE: {
                if (params.length < 1) { // error : (비밀번호를 받지 못했음)
                    command = createJson(id, method, null, "You must enter the type to subscribe to.");
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String type = (String)params[0];

                byte[] keyBytes = generateListenerKeyRandom();
                String keyStr = ByteUtil.toHexString0x(keyBytes);
                BigInteger key = ByteUtil.bytesToBigInteger(keyBytes);

                if(type.equalsIgnoreCase("newheads")) {
                    NewBlockListener listener = new NewBlockListener(keyStr, conn, token, isEncrypt);

                    mListeners.put(key, listener);
                    ethereum.addListener(listener);

                    command = createJson(id, method, keyStr);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                else if(type.equalsIgnoreCase("newPendingTransactions")) {
                    PendingTransactionListener listener = new PendingTransactionListener(keyStr, conn, token, isEncrypt);

                    mListeners.put(key, listener);
                    ethereum.addListener(listener);

                    command = createJson(id, method, keyStr);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                else if(type.equalsIgnoreCase("logs")) {
                    if(params.length < 2) {
                        command = createJson(id, method, null, "You must enter the address or topic you want to subscribe to.");
                        send(conn, token, command, isEncrypt);
                        return;
                    }

                    LinkedTreeMap paramsMap = (LinkedTreeMap) params[1];

                    List<byte[]> addresses = getBytesListFromParam(paramsMap.get("address"));
                    List<byte[]> topics = getBytesListFromParam(paramsMap.get("topics"));


                    LogListener listener = new LogListener(keyStr, conn, token, isEncrypt, addresses, topics, ethereum);

                    mListeners.put(key, listener);
                    ethereum.addListener(listener);

                    command = createJson(id, method, keyStr);
                    send(conn, token, command, isEncrypt);
                    return;
                }

                break;
            }

            case COMMAND_APIS_UNSUBSCRIBE: {
                if (params.length < 1) { // error : (비밀번호를 받지 못했음)
                    command = createJson(id, method, null, "You must enter a unique number to unsubscribe.");
                    send(conn, token, command, isEncrypt);
                    return;
                }

                String indexStr = (String)params[0];
                BigInteger index = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(indexStr));

                NewBlockListener listener = (NewBlockListener) mListeners.get(index);
                ethereum.removeListener(listener);
                mListeners.remove(index);
                break;
            }


            case COMMAND_APIS_GET_LOGS: {
                if(params.length < 1) {
                    command = createJson(id, method, null, "You must enter the address or topic you want to subscribe to.");
                    send(conn, token, command, isEncrypt);
                    return;
                }

                LinkedTreeMap paramsMap = (LinkedTreeMap) params[0];

                List<byte[]> addresses = getBytesListFromParam(paramsMap.get("address"));
                List<byte[]> topics = getBytesListFromParam(paramsMap.get("topics"));
                String fromBlock = (String) paramsMap.get("fromBlock");
                String toBlock = (String) paramsMap.get("toBlock");
                long fromBlockNumber = 0;
                long toBlockNumber = Long.MAX_VALUE;
                if(fromBlock != null) {
                    try {
                        fromBlockNumber = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes(fromBlock));
                    } catch (NumberFormatException ignored) {}
                }
                if(toBlock != null) {
                    toBlockNumber = getBlockNumber(ethereum, toBlock);
                }

                LastLogListener listener = new LastLogListener(method, id, conn, token, isEncrypt, addresses, topics, ethereum);

                BlockReplay blockReplay = new BlockReplay(ethereum.getBlockchain().getBlockStore(), ethereum.getBlockchain().getTransactionStore(), listener, fromBlockNumber, toBlockNumber);
                blockReplay.replayAsync();
                break;
            }
        }


        if(command != null) {
            send(conn, token, command, isEncrypt);
        }
    }

    private static byte[] generateListenerKeyRandom() {
        return HashUtil.sha3omit12(HashUtil.randomHash());
    }

    private static List<byte[]> getBytesListFromParam(Object param) {
        List<byte[]> list = new ArrayList<>();

        if(param instanceof String) {
            if(!((String) param).isEmpty()) {
                list.add(ByteUtil.hexStringToBytes((String) param));
            }
        }
        else if(param instanceof ArrayList) {
            for(Object item : (ArrayList)param) {
                if(item instanceof String) {
                    if(!((String) item).isEmpty()) {
                        list.add(ByteUtil.hexStringToBytes((String) item));
                    }
                } else {
                    list.add(null);
                }
            }
        }

        return list;
    }


    private static String getCommandTransactionByBlock(long id, String method, Block block, int index) {

        if (block == null) {
            return createJson(id, method, null, ERROR_NULL_BLOCK_BY_HASH);
        }

        List<Transaction> txList = block.getTransactionsList();
        Transaction tx;
        if(index < txList.size()) {
            tx = txList.get(index);
        } else {
            return createJson(id, method, null, String.format(ERROR_OUT_OF_INDEXED_TRANSACTION, index, txList.size()));
        }

        return createJson(id, method, new TransactionData(tx, block));
    }

    private static EstimateTransactionResult estimateTransaction(Object[] params, EthereumImpl ethereum) throws Exception {
        if (params.length == 0) {
            throw new Exception(ERROR_MESSAGE_UNKNOWN);
        }
        // 트랜잭션 데이터 형식이 맞지 않았을 경우
        else if(!(params[0] instanceof LinkedTreeMap)) {
            throw new Exception(ERROR_MESSAGE_INVALID_TX);
        }
        Web3ParamTransaction inputTx = new Web3ParamTransaction(params[0]);

        EstimateTransaction estimateTransaction = EstimateTransaction.getInstance(ethereum);
        return estimateTransaction.estimate(inputTx.getFrom(), inputTx.getTo(), BigInteger.ZERO, inputTx.getData());
    }


    public static String objectToHexString(Object object) {
        return String.format("0x%08X", object);
    }

    public static byte[] getAddressByte(Repository repository, String addressOrMask) {
        byte[] address = null;
        try {
            if (addressOrMask.contains("@")) { // is mask
                address = repository.getAddressByMask(addressOrMask);
            }
            else {
                address = ByteUtil.hexStringToBytes(addressOrMask);
            }
        } catch (Exception e) {
            e.printStackTrace();
            address = null;
        }

        return address;
    }

    /**
     * 입력된 값에 따라서 알맞은 블록 번호를 반환한다.
     * latest : 가장 최신의 블록 번호로 변환
     * earliest : 1
     * 그 외 : String to Long
     *
     * @param ethereum 코어 객체
     * @param blockParameter 파라미터로 입력받은 값
     * @return 기본 값은 latest
     */
    private static long getBlockNumber(Ethereum ethereum, String blockParameter) {
        long blockNumber;
        long best = ethereum.getBlockchain().getBestBlock().getNumber();

        switch (blockParameter) {
            case DEFAULTBLOCK_PARAMETER_EARLIEST:
                blockNumber = 1;
                break;
            case DEFAULT_PARAMETER_BLOCK_LATEST:
                blockNumber = best;
                break;
            default: // check long
                try {
                    if (blockParameter.startsWith("0x")) {
                        blockNumber = BIUtil.toBI(ByteUtil.hexStringToBytes(blockParameter)).longValue();
                    }
                    else {
                        blockNumber = Long.parseLong(blockParameter);
                    }

                } catch (NumberFormatException | DecoderException e) {
                    e.printStackTrace();
                    return 0;
                }
        }

        if(blockNumber > best) {
            blockNumber = best;
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
        EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)ethereum);
        EstimateTransactionResult estimateResult = estimator.estimate(transaction);

        boolean isSuccessful = estimateResult.isSuccess();
        String preRunError = estimateResult.getReceipt().getError();
        String returnCommand = "";

        // run
        if(isSuccessful) {
            ConsoleUtil.printlnRed("[contractRun] success send transaction");

            ethereum.submitTransaction(transaction); // send
            returnCommand = createJson(id, method, ByteUtil.toHexString0x(transaction.getHash()));
        }
        else {
            ConsoleUtil.printlnRed("[contractRun] fail send transaction");

            returnCommand = createJson(id, method, null, preRunError);
        }

        return returnCommand;
    }


    private static LinkedHashMap<BigInteger, TransactionPendingResult> txPendingResults = new LinkedHashMap<BigInteger, TransactionPendingResult>() {
        final int maxSize= 1000;

        @Override
        protected boolean removeEldestEntry(Map.Entry<BigInteger, TransactionPendingResult> eldest) {
            return size() > maxSize;
        }
    };


    private static boolean isPendingTxListenerRegistered = false;
    private static EthereumListener pendingTxListener = new EthereumListenerAdapter() {
        @Override
        public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
            BigInteger key = ByteUtil.bytesToBigInteger(txReceipt.getTransaction().getHash());
            TransactionPendingResult result = txPendingResults.get(key);

            if(result != null) {
                result.setValid(txReceipt.isValid());
                result.setErr(txReceipt.getError());
                txPendingResults.put(key, result);
            }
        }
    };
}
