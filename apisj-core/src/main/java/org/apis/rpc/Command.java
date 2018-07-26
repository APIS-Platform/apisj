package org.apis.rpc;

import com.google.gson.JsonObject;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.crypto.ECKey;
import org.apis.facade.Ethereum;
import org.apis.json.BlockData;
import org.apis.keystore.*;
import org.apis.rpc.template.*;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.blockchain.ApisUtil;
import org.java_websocket.WebSocket;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apis.rpc.JsonUtil.*;

public class Command {
    static final String COMMAND_GETBLOCK_NUMBER = "getblocknumber";
    static final String COMMAND_WALLET_INFO = "walletinfo";
    static final String COMMAND_GETBALANCE = "getbalance";
    static final String COMMAND_GETBALANCE_BY_MASK = "getbalancebymask";

    static final String COMMAND_GETMASK_BY_ADDRESS = "getmaskbyaddress";
    static final String COMMAND_GETADDRESS_BY_MASK = "getaddressbymask";
    static final String COMMAND_GETTRANSACTION = "gettx";
    static final String COMMAND_GETTRANSACTIONRECEIPT = "gettxreceipt";
    static final String COMMAND_SENDTRANSACTION = "sendtx";
    static final String COMMAND_SENDRAWTRANSACTION = "sendrawtx";

    static final String COMMAND_GETBLOCK_BY_NUMBER = "getblockbynumber";
    static final String COMMAND_GETBLOCK_BY_HASH = "getblockbyhash";

    static final String COMMAND_GETMINERAL = "getmineral";

    // data type
    static final String DATA_TAG_TYPE = "type";
    static final String DATA_TAG_AUTH = "auth";
    static final String DATA_TAG_DATA = "data";

    static final String TYPE_LOGIN = "login";
    static final String TYPE_TOKEN = "token";
    static final String TYPE_BLOCK = "block";
    static final String TYPE_BLOCK_NUMBER = "blocknumber";
    static final String TYPE_ADDRESS = "address";
    static final String TYPE_MASK = "mask";
    static final String TYPE_TXHASH = "txhash";
    static final String TYPE_BLOCKHASH = "blockhash";
    static final String TYPE_GASLIMIT = "gaslimit";
    static final String TYPE_GASPRICE = "gasprice";
    static final String TYPE_VALUE = "value";
    static final String TYPE_KEYSTORE_PW = "keystorepassword";
    static final String TYPE_WALLET_INDEX = "walletIndex";
    static final String TYPE_COUNT = "count";
    static final String TYPE_TX = "tx";
    static final String TYPE_APIS = "APIS";
    static final String TYPE_MNR = "MNR";
    static final String TYPE_NONCE = "nonce";


    // RPC 명령어
    public static void conduct(Ethereum ethereum, WebSocket conn, byte[] token, String request, String message) throws ParseException {
        System.out.println("request :" + request);
        String command;
        String data;
        Repository repo = ((Repository)ethereum.getRepository()).getSnapshotTo(ethereum.getBlockchain().getBestBlock().getStateRoot());
        JsonObject jsonObject = new JsonObject();

        switch (request) {

            case COMMAND_GETBLOCK_NUMBER: {
                long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                jsonObject.addProperty(TYPE_BLOCK_NUMBER, blockNumber);
                command = createJson(COMMAND_GETBLOCK_NUMBER, jsonObject);
                send(conn, token, command);
                break;
            }

            case COMMAND_GETBALANCE: {
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                BigInteger balance = null;
                try {
                    ethereum.getRepository().getBalance(Hex.decode(data));
                    command = createJson(COMMAND_GETBALANCE, createApisData(balance, data));
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_GETBALANCE, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_GETBALANCE_BY_MASK: {
                data = getDecodeMessageDataContent(message, TYPE_MASK);
                byte[] addressByMask = repo.getAddressByMask(data);

                if (addressByMask != null) {
                    BigInteger balanceByMask = ethereum.getRepository().getBalance(addressByMask);
                    String address = Hex.toHexString(addressByMask);
                    command = createJson(COMMAND_GETBALANCE_BY_MASK, createApisData(balanceByMask, address));
                } else {
                    ConsoleUtil.printRed("Null address by mask");
                    command = createJson(COMMAND_GETBALANCE_BY_MASK, null, "[" + NullPointerException.class.getSimpleName() + "] Null address by mask");
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_GETMASK_BY_ADDRESS:
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                String maskByAddress = null;
                try {
                    maskByAddress = repo.getMaskByAddress(Hex.decode(data));
                    jsonObject.addProperty(TYPE_MASK, maskByAddress);
                    command = createJson(COMMAND_GETMASK_BY_ADDRESS, jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_GETMASK_BY_ADDRESS, null, e);
                }

                send(conn, token, command);
                break;

            case COMMAND_GETADDRESS_BY_MASK: {
                data = getDecodeMessageDataContent(message, TYPE_MASK);
                byte[] addressByMask = repo.getAddressByMask(data);

                if (addressByMask!=null) {
                    jsonObject.addProperty(TYPE_ADDRESS, ByteUtil.toHexString(addressByMask));
                    command = createJson(COMMAND_GETADDRESS_BY_MASK, jsonObject);
                } else {
                    ConsoleUtil.printRed("Null address by mask");
                    command = createJson(COMMAND_GETADDRESS_BY_MASK, null, "[" +NullPointerException.class.getSimpleName() + "] Null address by mask");
                }
                send(conn, token, command);
                break;
            }

            case COMMAND_GETTRANSACTION: {
                data = getDecodeMessageDataContent(message, TYPE_TXHASH);

                if (data.startsWith("0x")) {
                    data = data.replace("0x","");
                }

                TransactionInfo txInfo = ethereum.getTransactionInfo(Hex.decode(data));

                // 트랜잭션이 실행된 적 없는 경우? TODO (result :  null)
                if(txInfo == null || txInfo.getReceipt() == null) {
                    jsonObject.addProperty(TYPE_TXHASH, data);
                    command = createJson(COMMAND_GETTRANSACTION, null, "[" + NullPointerException.class.getSimpleName() + "] Null transaction");
                } else {
                    TransactionData txData = new TransactionData(txInfo, ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
                    command = createJson(COMMAND_GETTRANSACTION, txData, txInfo.getReceipt().getError());
                }
                send(conn, token, command);
                break;
            }

            case COMMAND_GETTRANSACTIONRECEIPT: {
                data = getDecodeMessageDataContent(message, TYPE_TXHASH);

                if (data.startsWith("0x")) {
                    data = data.substring(2, data.length());
                }

                TransactionInfo txInfo = ethereum.getTransactionInfo(Hex.decode(data));

                // 트랜잭션이 실행된 적 없는 경우? TODO (result :  null)
                if(txInfo == null || txInfo.getReceipt() == null) {
                    jsonObject.addProperty(TYPE_TXHASH, data);
                    command = createJson(COMMAND_GETTRANSACTIONRECEIPT, null, "[" + NullPointerException.class.getSimpleName() + "] Null transaction");
                } else {
                    TransactionReceiptData txReceiptData = new TransactionReceiptData(txInfo, ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
                    command = createJson(COMMAND_GETTRANSACTIONRECEIPT, txReceiptData, txInfo.getReceipt().getError());
                }
                send(conn, token, command);
                break;
            }

            case COMMAND_WALLET_INFO: {
                List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

                int count = keyStoreDataList.size();

                jsonObject.addProperty(TYPE_COUNT, count+"");

                ArrayList<WalletInfo> walletInfos = new ArrayList<>();
                if(count > 0) {

                    try {
                        for (int i = 0; i < count; i++) {
                            String address = keyStoreDataList.get(i).address;
                            long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                            BigInteger apisBalance = ethereum.getRepository().getBalance(Hex.decode(address));
                            BigInteger apisMineral = ethereum.getRepository().getMineral(Hex.decode(address), blockNumber);
                            BigInteger nonce = ethereum.getRepository().getNonce(Hex.decode(address));
                            WalletInfo walletInfo = new WalletInfo(address, apisBalance.toString(), apisMineral.toString(), nonce.toString());
                            walletInfos.add(walletInfo);
                        }

                        command = createJson(COMMAND_WALLET_INFO, walletInfos);

                    } catch (Exception e) {
                        e.printStackTrace();
                        command = createJson(COMMAND_WALLET_INFO, null, e);
                    }
                }
                else {
                    command = createJson(COMMAND_WALLET_INFO, null, "[" + NullPointerException.class.getSimpleName() + "] Null wallet");
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_SENDTRANSACTION: {

                try {
                    long gasLimit = Long.parseLong(getDecodeMessageDataContent(message, TYPE_GASLIMIT));
                    BigInteger gasPrice = new BigInteger(getDecodeMessageDataContent(message, TYPE_GASPRICE));
                    String toAddress = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                    BigInteger value = new BigInteger(getDecodeMessageDataContent(message, TYPE_VALUE));
                    int walletIndex = Integer.parseInt(getDecodeMessageDataContent(message, TYPE_WALLET_INDEX));
                    String keystorePasswordEnc = getDecodeMessageDataContent(message, TYPE_KEYSTORE_PW);
                    String keystorePasswordDec = AESDecrypt(ByteUtil.toHexString(token), keystorePasswordEnc);

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
                            Hex.decode(toAddress),
                            ByteUtil.bigIntegerToBytes(value),
//                            new byte[0],
                            Hex.decode("f3ebff5d3f29e7ee2d031fc03205c89edf63b3a0"),
                            nextBlock);


                    tx.sign(senderKey); // signing

                    ethereum.submitTransaction(tx); // send

                    jsonObject.addProperty(TYPE_TXHASH, ByteUtil.toHexString(tx.getHash()));
                    command = createJson(COMMAND_SENDTRANSACTION, jsonObject);

                } /*catch (NumberFormatException e) { // 파싱 에러
                    catch (IndexOutOfBoundsException e) { //리스트 사이즈 에러
                    catch (DecoderException e) { // 주소에러
                    catch (NullPointerException e) { // 주소에러
                    catch (InvalidPasswordException e) {
                    catch (KeystoreVersionException e) {
                    catch (NotSupportKdfException e) {
                    catch (NotSupportCipherException e) { */

                // unknown
                catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_SENDTRANSACTION, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_SENDRAWTRANSACTION: {
                data = getDecodeMessageDataContent(message, TYPE_TX); // tx.getencoded string

                try {
                    Transaction tx = new Transaction(Hex.decode(data));
                    ethereum.submitTransaction(tx);
                    jsonObject.addProperty(TYPE_TXHASH, ByteUtil.toHexString(tx.getHash()));
                    command = createJson(COMMAND_SENDRAWTRANSACTION, jsonObject);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_SENDRAWTRANSACTION, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_GETBLOCK_BY_NUMBER: {
                long blockNumber = Long.parseLong(getDecodeMessageDataContent(message, TYPE_BLOCK_NUMBER));

                try {
                    Block block = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                    BlockData blockData = new BlockData(block);
                    command = createJson(COMMAND_GETBLOCK_BY_NUMBER, blockData);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_GETBLOCK_BY_NUMBER, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_GETBLOCK_BY_HASH: {
                data = getDecodeMessageDataContent(message, TYPE_BLOCKHASH);

                try {
                    byte[] hash = Hex.decode(data);
                    Block block = ethereum.getBlockchain().getBlockByHash(hash);
                    BlockData blockData = new BlockData(block);
                    command = createJson(COMMAND_GETBLOCK_BY_HASH, blockData);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_GETBLOCK_BY_HASH, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_GETMINERAL: {
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);

                try {
                    byte[] address = Hex.decode(data);
                    long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                    BigInteger mineral = ethereum.getRepository().getMineral(address, blockNumber);
                    command = createJson(COMMAND_GETMINERAL, createMnrData(mineral, data));

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(COMMAND_GETMINERAL, null, e);
                }

                send(conn, token, command);
                break;
            }
        }
    }

    private static APISData createApisData(BigInteger balance, String address) {
        return new APISData(address, balance.toString(), ApisUtil.readableApis(balance));
    }

    private static MNRData createMnrData(BigInteger balance, String address) {
        return new MNRData(address, balance.toString(), ApisUtil.readableApis(balance));
    }

    // 전송시 사용
    public static void send(WebSocket conn, byte[] token,  String text) {
        text = JsonUtil.AESEncrypt(ByteUtil.toHexString(token), text);
        conn.send(text);
    }
}
