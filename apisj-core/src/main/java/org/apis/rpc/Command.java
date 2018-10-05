package org.apis.rpc;

import com.google.gson.JsonObject;
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.facade.Ethereum;
import org.apis.keystore.*;
import org.apis.rpc.template.*;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.java_websocket.WebSocket;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apis.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.apis.rpc.JsonUtil.*;

public class Command {
    static final String COMMAND_FLAT = "flat_";
    static final String COMMAND_GETBLOCK_NUMBER = "getblocknumber";
    static final String COMMAND_WALLET_INFO = "walletinfo";
    static final String COMMAND_GETNONCE = "getnonce";
    static final String COMMAND_GETBALANCE = "getbalance";
    static final String COMMAND_GETBALANCE_BY_MASK = "getbalancebymask";
    static final String COMMAND_GETMINERAL = "getmineral";
    static final String COMMAND_GETMINERAL_BY_MASK = "getmineralbymask";

    static final String COMMAND_GETMASK_BY_ADDRESS = "getmaskbyaddress";
    static final String COMMAND_GETADDRESS_BY_MASK = "getaddressbymask";
    static final String COMMAND_GETTRANSACTION = "gettx";
    static final String COMMAND_GETTRANSACTIONRECEIPT = "gettxreceipt";
    static final String COMMAND_SENDTRANSACTION_SIGNNING = "sendtxsignning";
    static final String COMMAND_SENDTRANSACTION = "sendtx";
    static final String COMMAND_SENDRAWTRANSACTION = "sendrawtx";

    static final String COMMAND_GETBLOCK_BY_NUMBER = "getblockbynumber";
    static final String COMMAND_GETBLOCK_BY_HASH = "getblockbyhash";
    static final String COMMAND_GETMASTERNODE_LIST = "getmnlist";
    static final String COMMAND_GETMASTERNODE_INFO = "getmninfo";

    static final String COMMAND_REGISTERKNOWLEDGEKEY = "registerknowledgekey";

    // data type
    static final String DATA_TAG_NONCE = "nonce";
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
    static final String TYPE_KNOWLEDGE_PW = "knowledgepassword";
    static final String TYPE_WALLET_INDEX = "walletIndex";
    static final String TYPE_COUNT = "count";
    static final String TYPE_TX = "tx";
    static final String TYPE_APIS = "APIS";
    static final String TYPE_MNR = "MNR";
    static final String TYPE_NONCE = "nonce";
    static final String TYPE_MESSAGE = "message";


    // RPC 명령어
    public static void conduct(Ethereum ethereum, WebSocket conn, byte[] token, String request, String message) throws ParseException {
        System.out.println("request :" + request);
        String command;
        String data;
        Repository repo = ((Repository)ethereum.getRepository()).getSnapshotTo(ethereum.getBlockchain().getBestBlock().getStateRoot());
        JsonObject jsonObject = new JsonObject();
        boolean isFlatString = false;

        switch (request) {

            case COMMAND_FLAT + COMMAND_GETBLOCK_NUMBER:
                isFlatString = true;
            case COMMAND_GETBLOCK_NUMBER: {
                long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                jsonObject.addProperty(TYPE_BLOCK_NUMBER, blockNumber);
                command = createJson(isFlatString, COMMAND_GETBLOCK_NUMBER, jsonObject);
                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETNONCE:
                isFlatString = true;
            case COMMAND_GETNONCE: {
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);

                BigInteger nonce = null;
                try {
                    nonce = ethereum.getRepository().getNonce(Hex.decode(data));

                    jsonObject.addProperty(TYPE_NONCE, nonce.toString());
                    command = createJson(isFlatString, COMMAND_GETNONCE, jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETNONCE, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETBALANCE:
                isFlatString = true;
            case COMMAND_GETBALANCE: {
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                BigInteger balance = null;
                try {
                    balance = ethereum.getRepository().getBalance(Hex.decode(data));
                    command = createJson(isFlatString, COMMAND_GETBALANCE, createApisData(balance, data));
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETBALANCE, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETBALANCE_BY_MASK:
                isFlatString = true;
            case COMMAND_GETBALANCE_BY_MASK: {
                data = getDecodeMessageDataContent(message, TYPE_MASK);
                byte[] addressByMask = repo.getAddressByMask(data);

                if (addressByMask != null) {
                    BigInteger balanceByMask = ethereum.getRepository().getBalance(addressByMask);
                    String address = Hex.toHexString(addressByMask);
                    command = createJson(isFlatString, COMMAND_GETBALANCE_BY_MASK, createApisData(balanceByMask, address));
                } else {
                    ConsoleUtil.printRed("Null address by mask");
                    command = createJson(isFlatString, COMMAND_GETBALANCE_BY_MASK, null, "[" + NullPointerException.class.getSimpleName() + "] Null address by mask");
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETMINERAL:
                isFlatString = true;
            case COMMAND_GETMINERAL: {
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);

                try {
                    byte[] address = Hex.decode(data);
                    long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                    BigInteger mineral = ethereum.getRepository().getMineral(address, blockNumber);
                    command = createJson(isFlatString, COMMAND_GETMINERAL, createMnrData(mineral, data));

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETMINERAL, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETMINERAL_BY_MASK:
                isFlatString = true;
            case COMMAND_GETMINERAL_BY_MASK: {
                data = getDecodeMessageDataContent(message, TYPE_MASK);
                byte[] addressByMask = repo.getAddressByMask(data);
                long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();

                if (addressByMask != null) {
                    BigInteger mineral = ethereum.getRepository().getMineral(addressByMask, blockNumber);
                    String address = Hex.toHexString(addressByMask);
                    command = createJson(isFlatString, COMMAND_GETMINERAL_BY_MASK, createMnrData(mineral, address));
                } else {
                    ConsoleUtil.printRed("Null address by mask");
                    command = createJson(isFlatString, COMMAND_GETMINERAL_BY_MASK, null, "[" + NullPointerException.class.getSimpleName() + "] Null address by mask");
                }

                send(conn, token, command);
                break;

            }

            case COMMAND_FLAT + COMMAND_GETMASK_BY_ADDRESS:
                isFlatString = true;
            case COMMAND_GETMASK_BY_ADDRESS:
                data = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                String maskByAddress = null;
                try {
                    maskByAddress = repo.getMaskByAddress(Hex.decode(data));

                    if (maskByAddress == null || maskByAddress.equals("")) {
                        command = createJson(isFlatString, COMMAND_GETMASK_BY_ADDRESS, null, "[" +NullPointerException.class.getSimpleName() + "] Null mask by address");
                    }
                    else {
                        jsonObject.addProperty(TYPE_MASK, maskByAddress);
                        command = createJson(isFlatString, COMMAND_GETMASK_BY_ADDRESS, jsonObject);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETMASK_BY_ADDRESS, null, e);
                }

                send(conn, token, command);
                break;

            case COMMAND_FLAT + COMMAND_GETADDRESS_BY_MASK:
                isFlatString = true;
            case COMMAND_GETADDRESS_BY_MASK: {
                data = getDecodeMessageDataContent(message, TYPE_MASK);
                byte[] addressByMask = repo.getAddressByMask(data);

                if (addressByMask!=null) {
                    jsonObject.addProperty(TYPE_ADDRESS, ByteUtil.toHexString(addressByMask));
                    command = createJson(isFlatString, COMMAND_GETADDRESS_BY_MASK, jsonObject);
                } else {
                    ConsoleUtil.printRed("Null address by mask");
                    command = createJson(isFlatString, COMMAND_GETADDRESS_BY_MASK, null, "[" +NullPointerException.class.getSimpleName() + "] Null address by mask");
                }
                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETTRANSACTION:
                isFlatString = true;
            case COMMAND_GETTRANSACTION: {
                data = getDecodeMessageDataContent(message, TYPE_TXHASH);

                if (data.startsWith("0x")) {
                    data = data.replace("0x","");
                }

                try {
                    TransactionInfo txInfo = ethereum.getTransactionInfo(Hex.decode(data));

                    // 트랜잭션이 실행된 적 없는 경우? TODO (result :  null)
                    if (txInfo == null || txInfo.getReceipt() == null) {
                        jsonObject.addProperty(TYPE_TXHASH, data);
                        command = createJson(isFlatString, COMMAND_GETTRANSACTION, null, "[" + NullPointerException.class.getSimpleName() + "] Null transaction");
                    } else {
                        TransactionData txData = new TransactionData(txInfo, ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
                        command = createJson(isFlatString, COMMAND_GETTRANSACTION, txData, txInfo.getReceipt().getError());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETTRANSACTION, null, e);
                }
                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETTRANSACTIONRECEIPT:
                isFlatString = true;
            case COMMAND_GETTRANSACTIONRECEIPT: {
                data = getDecodeMessageDataContent(message, TYPE_TXHASH);

                if (data.startsWith("0x")) {
                    data = data.substring(2, data.length());
                }

                try {
                    TransactionInfo txInfo = ethereum.getTransactionInfo(Hex.decode(data));

                    // 트랜잭션이 실행된 적 없는 경우? TODO (result :  null)
                    if (txInfo == null || txInfo.getReceipt() == null) {
                        jsonObject.addProperty(TYPE_TXHASH, data);
                        command = createJson(isFlatString, COMMAND_GETTRANSACTIONRECEIPT, null, "[" + NullPointerException.class.getSimpleName() + "] Null transaction");
                    } else {
                        TransactionReceiptData txReceiptData = new TransactionReceiptData(txInfo, ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash()));
                        command = createJson(isFlatString, COMMAND_GETTRANSACTIONRECEIPT, txReceiptData, txInfo.getReceipt().getError());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETTRANSACTIONRECEIPT, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_WALLET_INFO:
                isFlatString = true;
            case COMMAND_WALLET_INFO: {
                List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

                int count = keyStoreDataList.size();

                jsonObject.addProperty(TYPE_COUNT, count+"");

                ArrayList<WalletInfo> walletInfos = new ArrayList<>();
                if(count > 0) {

                    try {
                        for (int i = 0; i < count; i++) {
                            String address = keyStoreDataList.get(i).address;
                            String mask = repo.getMaskByAddress(Hex.decode(address));

                            long blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
                            BigInteger apisBalance = ethereum.getRepository().getBalance(Hex.decode(address));
                            BigInteger apisMineral = ethereum.getRepository().getMineral(Hex.decode(address), blockNumber);
                            BigInteger nonce = ethereum.getRepository().getNonce(Hex.decode(address));
                            byte[] proofKey = repo.getProofKey(Hex.decode(address));
                            boolean hasProofKey = false;
                            if (proofKey!= null && !FastByteComparisons.equal(proofKey, EMPTY_DATA_HASH)) {
                                hasProofKey = true;
                            }

                            WalletInfo walletInfo = new WalletInfo(address, mask, apisBalance.toString(), apisMineral.toString(), nonce.toString(), hasProofKey);
                            walletInfos.add(walletInfo);
                        }

                        command = createJson(isFlatString, COMMAND_WALLET_INFO, walletInfos);

                    } catch (Exception e) {
                        e.printStackTrace();
                        command = createJson(isFlatString, COMMAND_WALLET_INFO, null, e);
                    }
                }
                else {
                    command = createJson(isFlatString, COMMAND_WALLET_INFO, null, "[" + NullPointerException.class.getSimpleName() + "] Null wallet");
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_SENDTRANSACTION_SIGNNING:
                isFlatString = true;
            case COMMAND_SENDTRANSACTION_SIGNNING: {
                try {
                    long gasLimit = Long.parseLong(getDecodeMessageDataContent(message, TYPE_GASLIMIT));
                    BigInteger gasPrice = new BigInteger(getDecodeMessageDataContent(message, TYPE_GASPRICE));
                    String toAddress = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                    byte[] toAddressByte = null;
                    if (!toAddress.equals("null")) {
                        toAddressByte = Hex.decode(toAddress);
                    }
                    String dataMessage = getDecodeMessageDataContent(message, TYPE_MESSAGE);
                    byte[] dataMessageByte = null;
                    if (!dataMessage.equals("null")) {
                        dataMessageByte = Hex.decode(dataMessage);
                    }

                    int walletIndex = Integer.parseInt(getDecodeMessageDataContent(message, TYPE_WALLET_INDEX));
                    BigInteger value = new BigInteger(getDecodeMessageDataContent(message, TYPE_VALUE));
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
                            toAddressByte,
                            ByteUtil.bigIntegerToBytes(value),
                            dataMessageByte,
                            nextBlock);


                    tx.sign(senderKey); // signing




                    String knowledgePasswordEnc = getDecodeMessageDataContent(message, TYPE_KNOWLEDGE_PW);
                    String knowledgePasswordDec = AESDecrypt(ByteUtil.toHexString(token), knowledgePasswordEnc);

                    if (!knowledgePasswordDec.equals("")) {
                        tx.authorize(knowledgePasswordDec);
                    }

                    jsonObject.addProperty(TYPE_TX, ByteUtil.toHexString(tx.getEncoded()));
                    command = createJson(isFlatString, COMMAND_SENDTRANSACTION_SIGNNING, jsonObject);

                }

                // unknown
                catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_SENDTRANSACTION_SIGNNING, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_SENDTRANSACTION:
                isFlatString = true;
            case COMMAND_SENDTRANSACTION: {

                try {
                    long gasLimit = Long.parseLong(getDecodeMessageDataContent(message, TYPE_GASLIMIT));
                    BigInteger gasPrice = new BigInteger(getDecodeMessageDataContent(message, TYPE_GASPRICE));
                    String toAddress = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                    byte[] toAddressByte = null;
                    if (!toAddress.equals("null")) {

                        // check address mask
                        if (toAddress.contains("@")) {
                            toAddressByte = repo.getAddressByMask(toAddress);
                        } else {
                            toAddressByte = Hex.decode(toAddress);
                        }

                        if (toAddressByte == null) {
                            command = createJson(isFlatString, COMMAND_SENDTRANSACTION,
                                    null, "[" + NullPointerException.class.getSimpleName() + "] Null address by mask");
                            send(conn, token, command);
                            return;
                        }
                    }

                    String dataMessage = getDecodeMessageDataContent(message, TYPE_MESSAGE);
                    byte[] dataMessageByte = null;
                    if (!dataMessage.equals("null")) {
                        dataMessageByte = Hex.decode(dataMessage);
                    }

                    int walletIndex = Integer.parseInt(getDecodeMessageDataContent(message, TYPE_WALLET_INDEX));
                    BigInteger value = new BigInteger(getDecodeMessageDataContent(message, TYPE_VALUE));
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
                            toAddressByte,
                            ByteUtil.bigIntegerToBytes(value),
                            dataMessageByte,
                            nextBlock);


                    tx.sign(senderKey); // signing


                    String knowledgePasswordEnc = getDecodeMessageDataContent(message, TYPE_KNOWLEDGE_PW);
                    String knowledgePasswordDec = AESDecrypt(ByteUtil.toHexString(token), knowledgePasswordEnc);

                    if (!knowledgePasswordDec.equals("")) {
                        tx.authorize(knowledgePasswordDec);
                    }

                    command = contractRun(isFlatString, COMMAND_SENDTRANSACTION, ethereum, tx);
                }

                /*catch (NumberFormatException e) { // 파싱 에러
                    catch (IndexOutOfBoundsException e) { //리스트 사이즈 에러
                    catch (DecoderException e) { // 주소에러
                    catch (NullPointerException e) { // 주소에러
                    catch (InvalidPasswordException e) { // 패스워드 에러
                    catch (KeystoreVersionException e) {
                    catch (NotSupportKdfException e) {
                    catch (NotSupportCipherException e) { */

                // unknown
                catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_SENDTRANSACTION, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_SENDRAWTRANSACTION:
                isFlatString = true;
            case COMMAND_SENDRAWTRANSACTION: {
                data = getDecodeMessageDataContent(message, TYPE_TX); // tx.getencoded string

                try {
                    Transaction tx = new Transaction(Hex.decode(data));





                    command = contractRun(isFlatString, COMMAND_SENDRAWTRANSACTION, ethereum, tx);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_SENDRAWTRANSACTION, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETBLOCK_BY_NUMBER:
                isFlatString = true;
            case COMMAND_GETBLOCK_BY_NUMBER: {
                long blockNumber = Long.parseLong(getDecodeMessageDataContent(message, TYPE_BLOCK_NUMBER));

                try {
                    Block block = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                    BlockData blockData = new BlockData(block);
                    command = createJson(isFlatString, COMMAND_GETBLOCK_BY_NUMBER, blockData);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETBLOCK_BY_NUMBER, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETBLOCK_BY_HASH:
                isFlatString = true;
            case COMMAND_GETBLOCK_BY_HASH: {
                data = getDecodeMessageDataContent(message, TYPE_BLOCKHASH);

                try {
                    byte[] hash = Hex.decode(data);
                    Block block = ethereum.getBlockchain().getBlockByHash(hash);
                    BlockData blockData = new BlockData(block);
                    command = createJson(isFlatString, COMMAND_GETBLOCK_BY_HASH, blockData);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETBLOCK_BY_HASH, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETMASTERNODE_LIST:
                isFlatString = true;
            case COMMAND_GETMASTERNODE_LIST: {
                List<String> generalAddress = new ArrayList<>();
                List<String> majorAddress = new ArrayList<>();
                List<String> privateAddress = new ArrayList<>();
                int isCount = 0;

                for (int i=0; i<3; i++) {
                    List<byte[]> mnList = repo.getMasterNodeList(i);
                    for (byte[] addr : mnList) {
                        if (i==0) {
                            generalAddress.add(Hex.toHexString(addr));
                        } else if(i==1) {
                            majorAddress.add(Hex.toHexString(addr));
                        } else {
                            privateAddress.add(Hex.toHexString(addr));
                        }
                        isCount++;
                    }
                }

                if (isCount > 0) {
                    MasterNodeListInfo masterNodeListInfo = new MasterNodeListInfo(generalAddress, majorAddress, privateAddress);
                    command = createJson(isFlatString, COMMAND_GETMASTERNODE_LIST, masterNodeListInfo);
                }
                else {
                    command = createJson(isFlatString, COMMAND_GETMASTERNODE_LIST,
                            null, "[" + NullPointerException.class.getSimpleName() + "] Null masternode address");
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_GETMASTERNODE_INFO:
                isFlatString = true;
            case COMMAND_GETMASTERNODE_INFO: {
                try {
                    data = getDecodeMessageDataContent(message, TYPE_ADDRESS);
                    byte[] address = Hex.decode(data);


                    long startBlock = repo.getMnStartBlock(address);
                    long lastBlock = repo.getMnLastBlock(address);
                    byte[] receiptAddress = repo.getMnRecipient(address);
                    BigInteger balance = repo.getMnStartBalance(address);

                    MasterNodeInfo masterNodeInfo = new MasterNodeInfo(startBlock, lastBlock,
                            ByteUtil.toHexString(receiptAddress), ApisUtil.readableApis(balance));
                    command = createJson(isFlatString, COMMAND_GETMASTERNODE_INFO, masterNodeInfo);

                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_GETMASTERNODE_INFO, null, e);
                }

                send(conn, token, command);
                break;
            }

            case COMMAND_FLAT + COMMAND_REGISTERKNOWLEDGEKEY:
                isFlatString = true;
            case COMMAND_REGISTERKNOWLEDGEKEY: {
                try {
                    Transaction blankTX = new Transaction(
                            null,
                            null,
                            null,
                            null,
                            ByteUtil.bigIntegerToBytes(new BigInteger("0")),
                            null,
                            null);

                    String knowledgePasswordEnc = getDecodeMessageDataContent(message, TYPE_KNOWLEDGE_PW);
                    String knowledgePasswordDec = AESDecrypt(ByteUtil.toHexString(token), knowledgePasswordEnc);
                    blankTX.authorize(knowledgePasswordDec);

                    byte[] proofcode = blankTX.getProofCode();
                    byte[] registerData = ByteUtil.merge(Hex.decode("d7d72930000000000000000000000000"), proofcode);

                    //// send tx
                    long gasLimit = Long.parseLong(getDecodeMessageDataContent(message, TYPE_GASLIMIT));
                    BigInteger gasPrice = new BigInteger(getDecodeMessageDataContent(message, TYPE_GASPRICE));
                    byte[] toAddressByte = Hex.decode("1000000000000000000000000000000000037452");

                    int walletIndex = Integer.parseInt(getDecodeMessageDataContent(message, TYPE_WALLET_INDEX));
                    BigInteger value = new BigInteger("0");
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
                            toAddressByte,
                            ByteUtil.bigIntegerToBytes(value),
                            registerData,
                            nextBlock);


                    tx.sign(senderKey); // signing

//                    ethereum.submitTransaction(tx); // send
//
//                    jsonObject.addProperty(TYPE_TXHASH, ByteUtil.toHexString(tx.getHash()));
//                    command = createJson(isFlatString, COMMAND_REGISTERKNOWLEDGEKEY, jsonObject);
                    command = contractRun(isFlatString, COMMAND_REGISTERKNOWLEDGEKEY, ethereum, tx);


                } catch (Exception e) {
                    e.printStackTrace();
                    command = createJson(isFlatString, COMMAND_REGISTERKNOWLEDGEKEY, null, e);
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

    // check send tx
    private static String contractRun(boolean isFlatString, String type, Ethereum ethereum, Transaction transaction) {
        ContractLoader.ContractRunEstimate contractRunEstimate = ContractLoader.preRunTransaction(ethereum, transaction);

        boolean isPreRunSuccess = contractRunEstimate.isSuccess();
        String preRunError = contractRunEstimate.getReceipt().getError();
        String returnCommand = "";


        /// run
        if (isPreRunSuccess) {
            ethereum.submitTransaction(transaction); // send
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(TYPE_TXHASH, ByteUtil.toHexString(transaction.getHash()));
            returnCommand = createJson(isFlatString, type, jsonObject);
        }
        else {
            returnCommand = createJson(isFlatString, type, null, preRunError);
        }

        return returnCommand;
    }
}
