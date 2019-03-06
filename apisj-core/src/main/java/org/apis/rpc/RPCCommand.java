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
import org.apis.facade.Apis;
import org.apis.facade.ApisImpl;
import org.apis.facade.SyncStatus;
import org.apis.keystore.*;
import org.apis.listener.BlockReplay;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.adapter.CanvasAdapter;
import org.apis.rpc.listener.LastLogListener;
import org.apis.rpc.listener.LogListener;
import org.apis.rpc.listener.NewBlockListener;
import org.apis.rpc.listener.PendingTransactionListener;
import org.apis.rpc.template.*;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.java_websocket.WebSocket;
import org.spongycastle.util.encoders.DecoderException;

import java.math.BigInteger;
import java.util.*;

import static org.apis.rpc.RPCJsonUtil.createJson;

public class RPCCommand {
    static final String COMMAND_RPC_VERSION = "2.0";

    // method
    private static final String COMMAND_NET_VERSION = "net_version";
    private static final String COMMAND_NET_PEERCOUNT = "net_peerCount";

    private static final String COMMAND_APIS_PROTOCOLVERSION = "apis_protocolVersion";
    private static final String COMMAND_APIS_SYNCING = "apis_syncing";
    private static final String COMMAND_APIS_COINBASE = "apis_coinbase";
    private static final String COMMAND_APIS_MINING = "apis_mining";
    private static final String COMMAND_APIS_ACCOUNTS = "apis_accounts";
    private static final String COMMAND_APIS_BLOCKNUMBER = "apis_blockNumber";
    private static final String COMMAND_APIS_GETBALANCE = "apis_getBalance";
    private static final String COMMAND_TOTAL_COINS = "apis_getTotalCoins";

    private static final String COMMAND_APIS_GETTRANSACTIONCOUNT = "apis_getTransactionCount";
    private static final String COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYHASH = "apis_getBlockTransactionCountByHash";
    private static final String COMMAND_APIS_GETBLOCKTRANSACTIONCOUNTBYNUMBER = "apis_getBlockTransactionCountByNumber";
    private static final String COMMAND_APIS_GETCODE = "apis_getCode";
    private static final String COMMAND_APIS_SIGN = "apis_sign";
    private static final String COMMAND_APIS_SENDTRANSACTION = "apis_sendTransaction";
    private static final String COMMAND_APIS_SENDRAWTRANSACTION = "apis_sendRawTransaction";
    private static final String COMMAND_APIS_CALL = "apis_call";
    private static final String COMMAND_APIS_ESTIMATE_GAS = "apis_estimateGas";
    private static final String COMMAND_APIS_GAS_PRICE = "apis_gasPrice";

    private static final String COMMAND_APIS_GETBLOCKBYHASH = "apis_getBlockByHash";
    private static final String COMMAND_APIS_GETBLOCKBYNUMBER = "apis_getBlockByNumber";
    private static final String COMMAND_APIS_GETTRANSACTIONBYHASH = "apis_getTransactionByHash";
    private static final String COMMAND_APIS_GETTRANSACTIONBYBLOCKHASHANDINDEX = "apis_getTransactionByBlockHashAndIndex";
    private static final String COMMAND_APIS_GETTRANSACTIONBYBLOCKNUMBERANDINDEX = "apis_getTransactionByBlockNumberAndIndex";
    private static final String COMMAND_APIS_GETTRANSACTIONRECEIPT = "apis_getTransactionReceipt";
    private static final String COMMAND_APIS_GETTRANSACTIONBYKEYWORD = "apis_getTransactionsByKeyword";
    private static final String COMMAND_APIS_GET_RECENT_TRANSACTIONS= "apis_getRecentTransactions";
    private static final String COMMAND_APIS_GET_RECENT_BLOCKS= "apis_getRecentBlocks";
    private static final String COMMAND_APIS_GET_TRANSACTION_COUNT_ON_BLOCKS = "apis_getTransactionCountOnBlocks";
    private static final String COMMAND_APIS_GET_TRANSACTION_COUNT_BY_ADDRESS = "apis_getTransactionCountByAddress";
    private static final String COMMAND_APIS_GET_MASTERNODE_COUNT = "apis_getMasternodeCount";

    // apis only
    private static final String COMMAND_APIS_GETWALLETINFO = "apis_getWalletInfo";
    private static final String COMMAND_APIS_GETMNLIST = "apis_getMnList";
    private static final String COMMAND_APIS_GETMNINFO = "apis_getMnInfo";

    private static final String COMMAND_PERSONAL_NEW_ACCOUNT = "personal_newAccount";
    private static final String COMMAND_PERSONAL_SIGN = "personal_sign";
    private static final String COMMAND_PERSONAL_EC_RECOVER = "personal_ecRecover";
    private static final String COMMAND_PERSONAL_SIGN_TRANSACTION = "personal_signTransaction";

    private static final String COMMAND_APIS_SUBSCRIBE = "apis_subscribe";
    private static final String COMMAND_APIS_UNSUBSCRIBE = "apis_unsubscribe";
    private static final String COMMAND_APIS_GET_LOGS = "apis_getLogs";

    // tag
    static final String TAG_ID = "id";
    static final String TAG_METHOD = "method";
    static final String TAG_PARAMS = "params";
    //static final String TAG_JSONRPC = "jsonrpc";
    //static final String TAG_RESULT = "result";

    static final String TAG_AUTHKEY = "authKey"; // header
    static final String TAG_TOKEN = "token";
    static final String TAG_HASH = "hash";
    static final String TAG_CODE = "code";
    static final String TAG_ERROR = "error";
    static final String TAG_PAYLOAD = "payload";

    //private static final String TAG_FROM = "from";
    private static final String TAG_TO = "to";
    private static final String TAG_GAS = "gas";
    private static final String TAG_GASPRICE = "gasPrice";
    private static final String TAG_VALUE = "value";
    private static final String TAG_DATA = "data";
    private static final String TAG_WALLET_INDEX = "walletIndex";
    private static final String TAG_KEYSTORE_PW = "keyStorePassword";
    private static final String TAG_KNOWLEDGE_PW = "knowledgePassword";

    // default block parammeter
    private static final String DEFAULTBLOCK_PARAMETER_EARLIEST = "earliest";
    private static final String DEFAULT_PARAMETER_BLOCK_LATEST = "latest";
    //private static final String DEFAULTBLOCK_PARAMETER_PENDING = "pending";

    // error
    static final int ERROR_CODE_UNNKOWN = 100;
    static final int ERROR_CODE_OVERFLOW_MAXCONNECTION = 101;
    static final int ERROR_CODE_WRONG_ID_PASSWORD = 103;
    static final int ERROR_CODE_WRONG_AUTHKEY = 104;
    static final int ERROR_CODE_WRONG_TOKENKEY = 105;
    static final int ERROR_CODE_WRONG_ID = 106;
    static final int ERROR_CODE_WITHOUT_PERMISSION_CLIENT = 113;
    static final int ERROR_CODE_WITHOUT_PERMISSION_IP = 114;
    static final int ERROR_CODE_NULL_ID = 120;
    //static final int ERROR_CODE_DUPLICATE_IP = 102;
    //static final int ERROR_CODE_WITHOUT_PERMISSION_TYPE = 115;
    //static final int ERROR_CODE_TIMEOUT = 121;

    static final String ERROR_MESSAGE_UNKNOWN = "unknown message.";
    private static final String ERROR_MESSAGE_UNKNOWN_ADDRESS = "unknown Address.";
    private static final String ERROR_MESSAGE_INVALID_ADDRESS = "Invalid Address format.";
    private static final String ERROR_MESSAGE_UNKNOWN_HASH = "unknown Hash.";
    private static final String ERROR_MESSAGE_NULL_BLOCKDATA = "there is no block data.";
    private static final String ERROR_MESSAGE_NULL_WALLETINDEX = "wrong (wallet)index.";
    private static final String ERROR_MESSAGE_NULL_TOADDRESS = "there is no (to) address.";
    private static final String ERROR_MESSAGE_NULL_GAS = "there is no gas.";
    private static final String ERROR_MESSAGE_NULL_GASPRICE = "there is no gas price.";
    private static final String ERROR_MESSAGE_NULL_VALUE = "there is no value.";
    private static final String ERROR_MESSAGE_NULL_PARAMETER = "there is no value."; // 조회할 값이 없다
    private static final String ERROR_MESSAGE_NULL_KEYSTORE_PW = "The password for the KeyStore file is missing.";


    private static final String ERROR_MESSAGE_INVALID_PASSWORD = "Invalid password.";
    private static final String ERROR_MESSAGE_INVALID_TX = "The transaction data type is invalid.";

    private static final String ERROR_PARAMETER_SIZE = "The number of input parameters for '%s' must be %d.";


    static final String ERROR_DEPORT_UNKNOWN = "unknown error.";
    static final String ERROR_DEPORT_OVERFLOW_MAXCONNECTION = "Reached max connection allowance.";
    static final String ERROR_DEPORT_WRONG_ID_PASSWORD = "Wrong ID or password.";
    static final String ERROR_DEPORT_WRONG_AUTHKEY = "Unauthorized key.";
    static final String ERROR_DEPORT_WRONG_TOKENKEY = "Unauthorized token key.";
    static final String ERROR_DEPORT_WRONG_ID = "Wrong ID.";
    static final String ERROR_DEPORT_WITHOUT_PERMISSION_CLIENT = "Unallowed client.";
    static final String ERROR_DEPORT_WITHOUT_PERMISSION_IP = "Unallowed IP address.";
    static final String ERROR_DEPORT_NULL_ID = "Cannot find ID.";
    //static final String ERROR_DEPORT_DUPLICATE_IP = "IP address duplicated.";
    //static final String ERROR_DEPORT_WITHOUT_PERMISSION_TYPE = "Unallowed command.";
    //static final String ERROR_DEPORT_TIMEOUT = "Timeout";


    private static final String ERROR_NULL_MASK_BY_ADDRESS = "There is no address matching the mask you entered.";
    private static final String ERROR_OUT_OF_INDEXED_TRANSACTION = "The index you entered does not exist in the block. (input : %d, index size: %d).";
    private static final String ERROR_NULL_TRANSACTION_BY_HASH = "There is no transaction can be found with the hash.";
    private static final String ERROR_CONNOT_FETCH_RECENT_BLOCKS = "It was not possible to load recent blocks with the input conditions.";
    private static final String ERROR_NULL_BLOCK_BY_NUMBER = "There is no block can be found with the number.";
    private static final String ERROR_NULL_BLOCK_BY_HASH = "There is no block can be found with the hash.";
    private static final String ERROR_NULL_WALLET_ADDRESS = "There is no address registered as wallet.";
    private static final String ERROR_NULL_SENDER = "Sender address does not exist.";
    private static final String ERROR_BLOCKCHAIN_CONFIG_NOT_LOADED = "Failed to load blockchain config.";
    private static final String ERROR_RECOVER_SIGN_NULL = "Unable to generate recovery signatures.";
    /*private static final String ERROR_NULL_TRANSACTION = "There is no transaction.";
    private static final String ERROR_NULL_TOADDRESS_OR_TOMASK = "There is no receiving address or mask.";
    private static final String ERROR_NULL_MASTERNODE_ADDRESS = "There is no address registered as masternode.";*/


    private static final HashMap<BigInteger, EthereumListener> mListeners = new HashMap<>();



    static void conduct(Apis apis, WebSocket conn, String token, String payload, boolean isEncrypt) {
        MessageApp3 message = parseMessage(payload);
        long id = message.getId();
        String method = message.getMethod();
        Object[] params = message.getParams().toArray();

        if(!isPendingTxListenerRegistered) {
            isPendingTxListenerRegistered = true;
            apis.addListener(pendingTxListener);
        }

        conduct(apis, conn, token, id, method, params, isEncrypt);
    }

    public static String conduct(Apis apis, String payload, CanvasAdapter canvasAdapter) {
        MessageApp3 message = parseMessage(payload);
        long id = message.getId();
        String method = message.getMethod();
        Object[] params = message.getParams().toArray();

        if(!isPendingTxListenerRegistered) {
            isPendingTxListenerRegistered = true;
            apis.addListener(pendingTxListener);
        }

        String command = getCommand(apis, id, method, params);

        if(command != null) {
            return command;
        } else {
            return subscribe(apis, id, method, params, canvasAdapter);
        }
    }

    public static MessageApp3 parseMessage(String payload) {
        ConsoleUtil.printlnCyan("Payload:" + payload);
        return new GsonBuilder().create().fromJson(payload, MessageApp3.class);
    }


    private static void conduct(Apis apis, WebSocket conn, String token, long id, String method, Object[] params, boolean isEncrypt) {
        String command = getCommand(apis, id, method, params);

        if(command != null) {
            send(conn, token, command, isEncrypt);
        } else {
            command = subscribe(apis, conn, token, id, method, params, isEncrypt);
            if(command != null) {
                send(conn, token, command, isEncrypt);
            }
        }
    }


    private static String getCommand(Apis apis, long id, String method, Object[] params) {
        String command = null;
        Repository latestRepo = (Repository) apis.getLastRepositorySnapshot();
        Block bestBlock = apis.getBlockchain().getBestBlock();

        SystemProperties config = SystemProperties.getDefault();
        if(config == null) {
            return createJson(id, method, null, ERROR_BLOCKCHAIN_CONFIG_NOT_LOADED);
        }

        Constants latestConstants = config.getBlockchainConfig().getConfigForBlock(bestBlock.getNumber()).getConstants();

        switch (method) {

            case COMMAND_NET_PEERCOUNT: {
                int count = apis.getChannelManager().getActivePeers().size(); // net peer count
                String countHexString = ByteUtil.toHexString0x(ByteUtil.intToBytes(count));
                command = createJson(id, method, countHexString);
                break;
            }

            case COMMAND_APIS_PROTOCOLVERSION: {
                command = createJson(id, method, COMMAND_RPC_VERSION);
                break;
            }

            case COMMAND_APIS_SYNCING: {
                long startingBlock = SyncStatus.getStartingBlock();
                long currentBlock = apis.getSyncStatus().getBlockLastImported();
                long highestBlock = apis.getSyncStatus().getBlockBestKnown();

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
                ECKey coinbaseKey = config.getCoinbaseKey();
                String address = "";
                if(coinbaseKey != null) {
                    byte[] coinbase = config.getCoinbaseKey().getAddress();
                    address = ByteUtil.toHexString0x(coinbase);
                }
                command = createJson(id, method, address);
                break;
            }

            case COMMAND_APIS_MINING: {
                boolean result = false;
                if (config.getCoinbaseKey() != null) {
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
                    if (address == null) {
                        return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                    }
                    targetAddress = address;
                }

                try {
                    long lastBlockNumber = getBlockNumber(apis, DEFAULT_PARAMETER_BLOCK_LATEST);
                    int index = 0;
                    for (KeyStoreData keyStoreData : keyStoreDataList) {
                        int walletIndex = index++;
                        byte[] address = ByteUtil.hexStringToBytes(keyStoreData.address);
                        if (targetAddress != null) {
                            if (!FastByteComparisons.equal(targetAddress, address)) {
                                continue;
                            }
                        }

                        AccountState accountState = latestRepo.getAccountState(address);
                        if (accountState == null) {
                            accountState = new AccountState(config);
                        }

                        BigInteger nonce = apis.getPendingState().getNonce(address);
                        WalletInfo walletInfo = new WalletInfo(walletIndex, address, accountState, lastBlockNumber, nonce);
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
                long blockNumber = apis.getBlockchain().getBestBlock().getNumber();
                String blockNumberHexString = ByteUtil.toHexString0x(ByteUtil.longToBytes(blockNumber));
                command = createJson(id, method, blockNumberHexString);
                break;
            }

            case COMMAND_TOTAL_COINS: {
                long blockNumber = apis.getBlockchain().getBestBlock().getNumber();
                BigInteger totalCoins = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants().getTotalAPIS(blockNumber);
                command = createJson(id, method, ApisUtil.readableApis(totalCoins, true));
                break;
            }

            // parameter
            // 0: address (hex string) or mask
            // 1: block number (hex string) or default block parameter (string)
            case COMMAND_APIS_GETBALANCE: {
                // parameter
                String blockNumberParam;
                if (params.length == 0) { // error : (주소 부재)
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                } else if (params.length == 1) { // default
                    blockNumberParam = DEFAULT_PARAMETER_BLOCK_LATEST;
                } else {
                    blockNumberParam = (String) params[1];
                }

                long blockNumber = getBlockNumber(apis, blockNumberParam);
                if (blockNumber == 0) { // block data null
                    return createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }


                try {
                    Block block = apis.getBlockchain().getBlockByNumber(blockNumber);
                    Repository repository = ((Repository) apis.getRepository()).getSnapshotTo(block.getStateRoot());
                    byte[] address = getAddressByte(repository, (String)params[0]);
                    if (address == null) {
                        return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                }

                byte[] address = getAddressByte(latestRepo, (String)params[0]);
                if (address == null) {
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                }

                // get transaction count
                try {
                    BigInteger nonce = apis.getPendingState().getNonce(address);
                    String nonceHexString = ByteUtil.toHexString0x(nonce.toByteArray());
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                try {
                    String hashString = (String) params[0];
                    byte[] hash = ByteUtil.hexStringToBytes(hashString);
                    int transactionCount = apis.getBlockchain().getBlockByHash(hash).getTransactionsList().size();
                    String transactionCountToHexString = ByteUtil.toHexString0x(ByteUtil.intToBytes(transactionCount));
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
                    return createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }

                try {
                    long blockNumber = getBlockNumber(apis, (String) params[0]);
                    if (blockNumber == 0) { // block data null
                        return createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                    }

                    int transactionCount = apis.getBlockchain().getBlockByNumber(blockNumber).getTransactionsList().size();
                    String transactionCountToHexString = ByteUtil.toHexString0x(ByteUtil.intToBytes(transactionCount));
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                } else if (params.length == 1) { // default
                    defaultBlockParameter = DEFAULT_PARAMETER_BLOCK_LATEST;
                } else {
                    defaultBlockParameter = (String) params[1];
                }

                // getblocknumber
                long blockNumber = getBlockNumber(apis, defaultBlockParameter);
                if (blockNumber == 0) { // block data null
                    return createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }

                try {
                    Repository repository = ((Repository) apis.getRepository()).getSnapshotTo(apis.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
                    byte[] address = getAddressByte(repository, (String)params[0]);
                    if (address == null) {
                        return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
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

            case COMMAND_APIS_SENDRAWTRANSACTION: {
                if (params.length == 0) { // error : (정보 부재)
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                }

                String txHashString = (String) params[0];
                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    Transaction tx = new Transaction(txHash);
                    BigInteger key = ByteUtil.bytesToBigInteger(tx.getHash());
                    if (!txPendingResults.containsKey(key)) {
                        txPendingResults.put(key, new TransactionPendingResult());
                    }

                    apis.submitTransaction(tx);

                    command = createJson(id, method, ByteUtil.toHexString0x(tx.getHash()));

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, e);
                }

                break;
            }

            case COMMAND_APIS_CALL: {
                try {
                    EstimateTransactionResult estimateResult = estimateTransaction(params, (ApisImpl) apis);
                    byte[] result = estimateResult.getReceipt().getExecutionResult();
                    command = createJson(id, method, ByteUtil.toHexString0x(result));
                } catch (Exception e) {
                    command = createJson(id, method, null, e.getMessage());
                }
                break;
            }


            case COMMAND_APIS_ESTIMATE_GAS: {
                try {
                    EstimateTransactionResult estimateResult = estimateTransaction(params, (ApisImpl) apis);
                    command = createJson(id, method, ByteUtil.toHexString0x(ByteUtil.longToBytes(estimateResult.getGasUsed())));
                } catch (Exception e) {
                    command = createJson(id, method, null, e.getMessage());
                }
                break;
            }

            case COMMAND_APIS_GAS_PRICE: {
                try {
                    command = createJson(id, method, ByteUtil.toHexString0x(ByteUtil.longToBytes(apis.getGasPrice())));
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                if (params.length >= 2) {
                    isFull = (boolean) params[1];
                }

                String blockHashString = (String) params[0];
                try {
                    byte[] hash = ByteUtil.hexStringToBytes(blockHashString);
                    Block block = apis.getBlockchain().getBlockByHash(hash);

                    byte[] coinbase = block.getCoinbase();
                    String coinbaseMask = latestRepo.getMaskByAddress(coinbase);

                    final Constants constants = config.getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants();
                    BlockData blockData = new BlockData(block, isFull, coinbaseMask, constants);
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                }

                if (params.length >= 2) {
                    isFull = (boolean) params[1];
                }


                try {
                    long blockNumber = getBlockNumber(apis, (String)params[0]);
                    Block block = apis.getBlockchain().getBlockByNumber(blockNumber);

                    byte[] coinbase = block.getCoinbase();
                    String coinbaseMask = latestRepo.getMaskByAddress(coinbase);

                    final Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

                    BlockData blockData = new BlockData(block, isFull, coinbaseMask, constants);
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                String txHashString = (String) params[0];

                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    TransactionInfo txInfo = apis.getTransactionInfo(txHash);

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
                        TransactionData txData = new TransactionData(txInfo.getReceipt().getTransaction(), apis.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                try {
                    String txHashString = (String) params[0];
                    if (txHashString.contains("@")) {
                        byte[] address = latestRepo.getAddressByMask(txHashString);
                        txHashString = ByteUtil.toHexString(address);
                    }

                    long rowCount = 0;
                    long offset = 0;

                    if(params.length > 1 && params[1] != null) {
                        try {
                            rowCount = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[1]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }
                    if(params.length > 2 && params[2] != null) {
                        try {
                            offset = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[2]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }

                    DBManager dbManager = DBManager.getInstance();
                    List<TransactionRecord> txRecords = dbManager.selectTransactions(txHashString, rowCount, offset);
                    List<TransactionSearchData> txReceipts = new ArrayList<>();
                    for(TransactionRecord txRecord : txRecords) {
                        byte[] txHash = ByteUtil.hexStringToBytes(txRecord.getHash());
                        TransactionInfo txInfo = apis.getTransactionInfo(txHash);
                        Block block = apis.getBlockchain().getBlockByHash(txInfo.getBlockHash());

                        TransactionReceiptData txReceiptData = new TransactionReceiptData(txInfo, block);
                        txReceiptData.setMask(latestRepo);

                        txReceipts.add(new TransactionSearchData(txReceiptData));
                    }

                    command = createJson(id, method, txReceipts);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                }

                break;
            }

            case COMMAND_APIS_GET_RECENT_TRANSACTIONS: {
                try {
                    long rowCount = 20;
                    long offset = 0;

                    if(params.length > 0 && params[0] != null) {
                        try {
                            rowCount = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[0]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }
                    if(params.length > 1 && params[1] != null) {
                        try {
                            offset = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[1]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }

                    DBManager dbManager = DBManager.getInstance();
                    List<TransactionRecord> txRecords = dbManager.selectRecentTransactions(rowCount, offset);
                    List<TransactionSearchData> txReceipts = new ArrayList<>();
                    for(TransactionRecord txRecord : txRecords) {
                        byte[] txHash = ByteUtil.hexStringToBytes(txRecord.getHash());
                        TransactionInfo txInfo = apis.getTransactionInfo(txHash);
                        Block block = apis.getBlockchain().getBlockByHash(txInfo.getBlockHash());

                        TransactionReceiptData txReceiptData = new TransactionReceiptData(txInfo, block);
                        txReceiptData.setMask(latestRepo);

                        TransactionSearchData txSearchData = new TransactionSearchData(txReceiptData);
                        txReceipts.add(txSearchData);
                    }

                    command = createJson(id, method, txReceipts);
                } catch (Exception ignore) {
                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                }
                break;
            }

            case COMMAND_APIS_GET_RECENT_BLOCKS: {
                try {
                    long rowCount = 20;
                    long offset = 1;

                    if(params.length > 0 && params[0] != null) {
                        try {
                            rowCount = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[0]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }
                    if(params.length > 1 && params[1] != null) {
                        try {
                            offset = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[1]));
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }

                    Block currentBlock = apis.getBlockchain().getBestBlock();

                    for(int i = 0; i < offset && currentBlock.getNumber() > 1; i++) {
                        currentBlock = apis.getBlockchain().getBlockByHash(currentBlock.getParentHash());
                    }

                    final Constants constants = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).getConstants();

                    List<BlockData> blocks = new ArrayList<>();
                    for(int i = 0; i < rowCount && currentBlock.getNumber() > 1; i++) {
                        blocks.add(new BlockData(currentBlock, false, constants));
                        currentBlock = apis.getBlockchain().getBlockByHash(currentBlock.getParentHash());
                    }

                    Collections.reverse(blocks);
                    command = createJson(id, method, blocks);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(id, method, null, ERROR_CONNOT_FETCH_RECENT_BLOCKS);
                }
                break;
            }
            case COMMAND_APIS_GET_TRANSACTION_COUNT_ON_BLOCKS: {
                long fromBlock = 1;
                long toBlock = apis.getBlockchain().getBestBlock().getNumber() - 1;
                long txCount = 0;

                if(params.length > 0 && params[0] != null) {
                    try {
                        fromBlock = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[0]));
                    } catch (NumberFormatException | DecoderException ignored) {}

                    if(params.length > 1 && params[1] != null) {
                        try {
                            long _toBlock = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes((String) params[1]));
                            if(_toBlock > 0) {
                                toBlock = _toBlock;
                            }
                        } catch (NumberFormatException | DecoderException ignored) {}
                    }
                    if(fromBlock < 1) {
                        fromBlock = 1;
                    }
                    if(toBlock <= fromBlock) {
                        toBlock = fromBlock + 1;
                    }

                    Block parentBlock = apis.getBlockchain().getBlockByNumber(toBlock);
                    while(parentBlock.getNumber() > fromBlock) {
                        txCount += parentBlock.getTransactionsList().size();
                        parentBlock = apis.getBlockchain().getBlockByHash(parentBlock.getParentHash());
                    }
                }
                // 인자 없이 조회할 경우, SQL DB에서 카운트를 조회해서 반환한다
                else {
                    txCount = DBManager.getInstance().selectTransactionsAllCount(null);
                }

                command = createJson(id, method, new TransactionCountData(fromBlock, toBlock, txCount));
                break;
            }

            case COMMAND_APIS_GET_TRANSACTION_COUNT_BY_ADDRESS: {
                if(params.length < 1) {
                    // 주소가 없으므로 에러
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                }
                long txCount;

                byte[] address = null;
                try {
                    String addressParam = (String)params[0];
                    if(addressParam.contains("@")) {
                        address = latestRepo.getAddressByMask(addressParam);
                    } else {
                        address = ByteUtil.hexStringToBytes((String) params[0]);
                    }
                } catch (Exception ignored) {}

                String type = "all";
                if(params.length > 1 && params[1] != null) {
                    try {
                        type = (String) params[1];
                    } catch (NumberFormatException | DecoderException ignored) {}
                }


                DBManager dbManager = DBManager.getInstance();
                switch(type.toLowerCase()) {
                    case "send":
                        txCount = dbManager.countOfSendingTransaction(address);
                        break;
                    case "receive":
                        txCount = dbManager.countOfReceivingTransaction(address);
                        break;
                    case "all":
                    default:
                        txCount = dbManager.countOfAllTransaction(address);
                        break;
                }

                command = createJson(id, method, txCount);
                break;
            }

            case COMMAND_APIS_GET_MASTERNODE_COUNT: {
                List<byte[]> mnGeneralEarly = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_GENERAL_BASE_EARLY_RUN()));
                List<byte[]> mnMajorEarly = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_MAJOR_BASE_EARLY_RUN()));
                List<byte[]> mnPrivateEarly = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN()));

                List<byte[]> mnGeneralNormal = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_GENERAL_BASE_NORMAL()));
                List<byte[]> mnMajorNormal = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_MAJOR_BASE_NORMAL()));
                List<byte[]> mnPrivateNormal = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_PRIVATE_BASE_NORMAL()));

                List<byte[]> mnGeneralLate = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_GENERAL_BASE_LATE()));
                List<byte[]> mnMajorLate = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_MAJOR_BASE_LATE()));
                List<byte[]> mnPrivateLate = new ArrayList<>(latestRepo.getMasterNodeList(latestConstants.getMASTERNODE_PRIVATE_BASE_LATE()));

                MasternodeCountData countData = new MasternodeCountData(
                        mnGeneralEarly.size(), mnMajorEarly.size(), mnPrivateEarly.size(),
                        mnGeneralNormal.size(), mnMajorNormal.size(), mnPrivateNormal.size(),
                        mnGeneralLate.size(), mnMajorLate.size(), mnPrivateLate.size());

                command = createJson(id, method, countData);
                break;
            }

            // parameter
            // 0: block Hash (hex string)
            // 1: transaction index position (hex string)
            case COMMAND_APIS_GETTRANSACTIONBYBLOCKHASHANDINDEX: {
                if (params.length < 2) { // error : (parameter 부재)
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                }

                String blockHashString = (String) params[0];
                byte[] blockHash = ByteUtil.hexStringToBytes(blockHashString);
                Block block = apis.getBlockchain().getBlockByHash(blockHash);

                BigInteger indexBi = BIUtil.toBI(ByteUtil.hexStringToBytes((String) params[1]));

                command = getCommandTransactionByBlock(id, method, block, indexBi.intValue());
                break;
            }


            // parameter
            // 0: block number (hex string) or default block parameter (string)
            // 1: transaction index position (hex string)
            case COMMAND_APIS_GETTRANSACTIONBYBLOCKNUMBERANDINDEX: {
                if (params.length < 2) { // error : (parameter 부재)
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN);
                }

                BigInteger blockNumberBi = BIUtil.toBI(ByteUtil.hexStringToBytes((String) params[0]));
                Block block = apis.getBlockchain().getBlockByNumber(blockNumberBi.longValue());

                BigInteger indexBi = BIUtil.toBI(ByteUtil.hexStringToBytes((String) params[1]));

                command = getCommandTransactionByBlock(id, method, block, indexBi.intValue());
                break;
            }

            // parameter
            // 0: transaction Hash (hex string)
            case COMMAND_APIS_GETTRANSACTIONRECEIPT: {
                if (params.length < 1) { // error : (hash 부재)
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_HASH);
                }

                String txHashString = (String) params[0];

                try {
                    byte[] txHash = ByteUtil.hexStringToBytes(txHashString);
                    TransactionInfo txInfo = apis.getTransactionInfo(txHash);

                    if (txInfo == null || txInfo.getReceipt() == null) {
                        TransactionPendingResult result = txPendingResults.get(ByteUtil.bytesToBigInteger(txHash));

                        if(result != null && result.getErr() != null && !result.getErr().isEmpty()) {
                            command = createJson(id, method, null, result.getErr());
                        } else {
                            command = createJson(id, method, "null");
                        }
                    }
                    else {
                        TransactionReceiptData txReceiptData = new TransactionReceiptData(txInfo, apis.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
                        txReceiptData.setMask(latestRepo);
                        command = createJson(id, method, txReceiptData);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    command = createJson(id, method, null, ERROR_NULL_TRANSACTION_BY_HASH);
                }

                break;
            }

            case COMMAND_NET_VERSION: {
                command = createJson(id, method, apis.getChainIdForNextBlock());
                break;
            }

            // parameter
            // 0: address (hex string) or mask
            case COMMAND_APIS_GETWALLETINFO: {
                if (params.length == 0) { // error : (address or mask 부재)
                    return createJson(id, method, null, ERROR_MESSAGE_NULL_PARAMETER);
                }

                String paramAddr = (String) params[0];

                byte[] address;
                // is mask : result address
                if (paramAddr.contains("@")) {
                    address = latestRepo.getAddressByMask(paramAddr);

                    if(address == null || address.length == 0) {
                        return createJson(id, method, null, ERROR_NULL_MASK_BY_ADDRESS);
                    }
                } else {
                    try {
                        address = ByteUtil.hexStringToBytes(paramAddr);
                    } catch (DecoderException e) {
                        return createJson(id, method, null, ERROR_MESSAGE_INVALID_ADDRESS);
                    }
                }

                if(address == null || address.length == 0) {
                    command = createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                } else {

                    AccountState state = latestRepo.getAccountState(address);

                    if (state == null) {
                        state = new AccountState(config);
                    }

                    long blockNumber = apis.getBlockchain().getBestBlock().getNumber();
                    BigInteger nonce = apis.getPendingState().getNonce(address);
                    WalletInfo walletInfo = new WalletInfo(-1, address, state, blockNumber, nonce);

                    command = createJson(id, method, walletInfo);
                }
                break;
            }

            case COMMAND_PERSONAL_NEW_ACCOUNT: {
                if (params.length < 1) { // error : (비밀번호를 받지 못했음)
                    return createJson(id, method, null, ERROR_MESSAGE_NULL_KEYSTORE_PW);
                }

                byte[] privateKey = KeyStoreManager.getInstance().createPrivateKey((String) params[0]);
                command = createJson(id, method, ByteUtil.toHexString0x(ECKey.fromPrivate(privateKey).getAddress()));
                break;
            }

            case COMMAND_PERSONAL_SIGN: {
                if (params.length < 3) { // error : (비밀번호를 받지 못했음)
                    return createJson(id, method, null, String.format(ERROR_PARAMETER_SIZE, COMMAND_PERSONAL_SIGN, 3));
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
                    return createJson(id, method, null, String.format(ERROR_PARAMETER_SIZE, COMMAND_PERSONAL_EC_RECOVER, 2));
                }

                byte[] dataSigned = ByteUtil.hexStringToBytes((String)params[0]);
                byte[] signatureBytes = ByteUtil.hexStringToBytes((String)params[1]);

                ECKey.ECDSASignature signature = KeyStoreUtil.decodeSignature(signatureBytes);
                ECKey recoveredKey = ECKey.recoverFromSignature(0, signature, HashUtil.sha3(dataSigned));
                if(recoveredKey == null) {
                    return createJson(id, method, null, ERROR_RECOVER_SIGN_NULL);
                }
                command = createJson(id, method, ByteUtil.toHexString0x(recoveredKey.getAddress()));
                break;
            }

            case COMMAND_PERSONAL_SIGN_TRANSACTION: {
                if (params.length < 2) { // error : (비밀번호를 받지 못했음)
                    return createJson(id, method, null, String.format(ERROR_PARAMETER_SIZE, COMMAND_PERSONAL_SIGN_TRANSACTION, 2));
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
                                txData.setNonce(apis.getPendingState().getNonce(key.getAddress()).longValue());
                            }

                            if(txData.isEmptyTo() && !txData.isEmptyToMask()) {
                                txData.setTo(latestRepo.getAddressByMask(txData.getToMask()));
                            }

                            if(txData.isGasPriceEmpty()) {
                                txData.setGasPrice(ByteUtil.longToBytes(apis.getGasPrice()));
                            }

                            if(txData.isEmptyGas()) {
                                EstimateTransaction estimator = EstimateTransaction.getInstance((ApisImpl) apis);

                                txData.setGas(BigInteger.valueOf(50_000_000L));
                                Transaction tempTx = txData.getTransaction(apis.getChainIdForNextBlock());

                                EstimateTransactionResult estimateResult = estimator.estimate(tempTx);
                                txData.setGas(BigInteger.valueOf(estimateResult.getGasUsed()));
                            }

                            Transaction tx = txData.getTransaction(apis.getChainIdForNextBlock());
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

                long blockNumber = getBlockNumber(apis, blockNumberParam);
                if (blockNumber == 0) { // block data null
                    return createJson(id, method, null, ERROR_MESSAGE_NULL_BLOCKDATA);
                }

                try {
                    Block block = apis.getBlockchain().getBlockByNumber(blockNumber);
                    Repository repository = ((Repository) apis.getRepository()).getSnapshotTo(block.getStateRoot());

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

                    List<byte[]> generalEN = new ArrayList<>(generalEarlybird);
                    generalEN.addAll(generalNormal);

                    List<byte[]> majorEN = new ArrayList<>(majorEarlybird);
                    majorEN.addAll(majorNormal);

                    List<byte[]> privateEN = new ArrayList<>(privateEarlybird);
                    privateEN.addAll(privateNormal);

                    MasterNodeListInfo masterNodeListInfo = new MasterNodeListInfo(generalEN, majorEN, privateEN, generalLate, majorLate, privateLate);
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
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
                }

                byte[] address = getAddressByte(latestRepo, (String)params[0]);
                if (address==null) {
                    return createJson(id, method, null, ERROR_MESSAGE_UNKNOWN_ADDRESS);
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



            case COMMAND_APIS_UNSUBSCRIBE: {
                if (params.length < 1) { // error : (비밀번호를 받지 못했음)
                    return createJson(id, method, null, "You must enter a unique number to unsubscribe.");
                }

                String indexStr = (String)params[0];
                BigInteger index = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(indexStr));

                NewBlockListener listener = (NewBlockListener) mListeners.get(index);
                apis.removeListener(listener);
                mListeners.remove(index);
                break;
            }
        }

        return command;
    }

    /*
     * app3js 라이브러리의 WebsocketProvider를 통해서 구독한 경우를 처리한다.
     */
    private static String subscribe (Apis apis, WebSocket conn, String token, long id, String method, Object[] params, boolean isEncrypt) {
        return subscribe (apis, id, method, params, conn, token, isEncrypt, null);
    }


    /*
     * WebView를 통해 구현된 Smart Contract on Canvas를 통해서 구독한 경우를 처리한다.
     */
    private static String subscribe (Apis apis, long id, String method, Object[] params, CanvasAdapter canvasAdapter) {
        return subscribe(apis, id, method, params, null, null, false, canvasAdapter);
    }

    private static final int SUBSCRIBE_TYPE_WEBSOCKET = 0;
    private static final int SUBSCRIBE_TYPE_SCC = 1;

    private static String subscribe (Apis apis, long id, String method, Object[] params, WebSocket conn, String token, boolean isEncrypt, CanvasAdapter canvasAdapter) {
        int subscribeType;
        if(conn != null && conn.isOpen()) {
            subscribeType = SUBSCRIBE_TYPE_WEBSOCKET;
        }
        else if(canvasAdapter != null) {
            subscribeType = SUBSCRIBE_TYPE_SCC;
        } else {
            return null;
        }

        switch(method) {
            case COMMAND_APIS_SUBSCRIBE: {
                if (params.length < 1) { // error : (비밀번호를 받지 못했음)
                    return createJson(id, method, null, "You must enter the type to subscribe to.");
                }

                String type = (String)params[0];

                byte[] keyBytes = generateListenerKeyRandom();
                String keyStr = ByteUtil.toHexString0x(keyBytes);
                BigInteger key = ByteUtil.bytesToBigInteger(keyBytes);

                if(type.equalsIgnoreCase("newheads")) {
                    NewBlockListener listener;

                    switch(subscribeType) {
                        case SUBSCRIBE_TYPE_WEBSOCKET:
                            listener = new NewBlockListener(keyStr, conn, token, isEncrypt);
                            break;
                        case SUBSCRIBE_TYPE_SCC:
                            listener = new NewBlockListener(keyStr, canvasAdapter);
                            break;
                        default:
                            return null;
                    }

                    mListeners.put(key, listener);
                    apis.addListener(listener);

                    return createJson(id, method, keyStr);
                }

                else if(type.equalsIgnoreCase("newPendingTransactions")) {
                    PendingTransactionListener listener;

                    switch(subscribeType) {
                        case SUBSCRIBE_TYPE_WEBSOCKET:
                            listener = new PendingTransactionListener(keyStr, conn, token, isEncrypt);
                            break;
                        case SUBSCRIBE_TYPE_SCC:
                            listener = new PendingTransactionListener(keyStr, canvasAdapter);
                            break;
                        default:
                            return null;
                    }

                    mListeners.put(key, listener);
                    apis.addListener(listener);

                    return createJson(id, method, keyStr);
                }

                else if(type.equalsIgnoreCase("logs")) {
                    if(params.length < 2) {
                        return createJson(id, method, null, "You must enter the address or topic you want to subscribe to.");
                    }

                    LinkedTreeMap paramsMap = (LinkedTreeMap) params[1];

                    List<byte[]> addresses = getBytesListFromParam(paramsMap.get("address"));
                    List<byte[]> topics = getBytesListFromParam(paramsMap.get("topics"));


                    LogListener listener;
                    switch(subscribeType) {
                        case SUBSCRIBE_TYPE_WEBSOCKET:
                            listener = new LogListener(keyStr, conn, token, isEncrypt, addresses, topics, apis);
                            break;
                        case SUBSCRIBE_TYPE_SCC:
                            listener = new LogListener(keyStr, addresses, topics, apis, canvasAdapter);
                            break;
                        default:
                            return null;
                    }

                    mListeners.put(key, listener);
                    apis.addListener(listener);

                    return createJson(id, method, keyStr);
                }

                break;
            }



            case COMMAND_APIS_GET_LOGS: {
                if(params.length < 1) {
                    return createJson(id, method, null, "You must enter the address or topic you want to subscribe to.");
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
                    toBlockNumber = getBlockNumber(apis, toBlock);
                }

                LastLogListener listener;
                switch(subscribeType) {
                    case SUBSCRIBE_TYPE_WEBSOCKET:
                        listener = new LastLogListener(method, id, conn, token, isEncrypt, addresses, topics, apis);
                        break;
                    case SUBSCRIBE_TYPE_SCC:
                        listener = new LastLogListener(method, id, addresses, topics, apis, canvasAdapter);
                        break;
                    default:
                        return null;
                }

                BlockReplay blockReplay = new BlockReplay(apis.getBlockchain().getBlockStore(), apis.getBlockchain().getTransactionStore(), listener, fromBlockNumber, toBlockNumber);
                blockReplay.replayAsync();
                break;
            }
        }

        return null;
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

    private static EstimateTransactionResult estimateTransaction(Object[] params, ApisImpl ethereum) throws Exception {
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


    private static byte[] getAddressByte(Repository repository, String addressOrMask) {
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
        }

        return address;
    }

    /**
     * 입력된 값에 따라서 알맞은 블록 번호를 반환한다.
     * latest : 가장 최신의 블록 번호로 변환
     * earliest : 1
     * 그 외 : String to Long
     *
     * @param apis 코어 객체
     * @param blockParameter 파라미터로 입력받은 값
     * @return 기본 값은 latest
     */
    private static long getBlockNumber(Apis apis, String blockParameter) {
        long blockNumber;
        long best = apis.getBlockchain().getBestBlock().getNumber();

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

        if (conn.isOpen()) {
            conn.send(text);
        } else {
            ConsoleUtil.printlnRed("HAHAHAHT " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }
    }

    public static void send(WebSocket conn, String token, String text) {
        send(conn, token, text, false);
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
