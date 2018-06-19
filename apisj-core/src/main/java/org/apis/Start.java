/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apis;

import ch.qos.logback.core.spi.LifeCycle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apis.cli.CLIInterface;
import org.apis.config.BlockchainConfig;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.keystore.*;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.mine.Ethash;
import org.apis.net.eth.message.StatusMessage;
import org.apis.net.message.Message;
import org.apis.net.p2p.HelloMessage;
import org.apis.net.rlpx.Node;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.RewardPointUtil;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    private static Ethereum mEthereum;

    private static boolean synced = false;


    public static void main(String args[]) throws IOException, URISyntaxException {
        CLIInterface.call(args);
        final SystemProperties config = SystemProperties.getDefault();
        Scanner scan = new Scanner(System.in);

        // Coinbase를 생성하기 위해 선택하도록 해야한다.
        // keystore 폴더가 존재하는지, 파일들이 존재하는지 확인한다.
        String keystoreDir = config.databaseDir() + "/" + config.keystoreDir();

        File keystore = new File(keystoreDir);
        if(!keystore.exists()) {
            keystore.mkdir();
        }

        File[] keyList = keystore.listFiles();
        List<KeyStoreData> keyStoreDataList = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        if(keyList != null) {
            for (File file : keyList) {
                if (file.isFile()) {
                    String fileText = readFile(file);
                    KeyStoreData data = gson.fromJson(fileText, KeyStoreData.class);
                    if (data != null) {
                        keyStoreDataList.add(data);
                    }
                }
            }
        }

        if(keyStoreDataList.size() > 0) {
            while(true) {
                System.out.format("There are %d keystore files. Do you want to create a new address?\n", keyStoreDataList.size());
                System.out.println("1. Create a new address");
                System.out.println("2. No. I omit it.");
                System.out.println("3. Exit");

                System.out.print(">> ");
                char choose = scan.next().charAt(0);

                switch (choose) {
                    case '1': {
                        createPrivateKey(null, keystoreDir, keyStoreDataList);
                        continue;
                    }
                    case '2': {
                        System.out.println("Which address will you use as coinbase(miner)?");
                        for(int i = 0; i < keyStoreDataList.size(); i++) {
                            System.out.println(i + ". " + keyStoreDataList.get(i).address);
                        }

                        System.out.print(">> ");
                        char chooseAddress = scan.next().charAt(0);

                        try {
                            int index = Integer.parseInt("" + chooseAddress);

                            if(index >= keyStoreDataList.size()) {
                                System.out.println("Please enter the correct number.");
                                continue;
                            }

                            KeyStoreData data = keyStoreDataList.get(index);
                            if(data == null) {
                                continue;
                            }

                            System.out.println("Please input your password");
                            System.out.print(">> ");
                            String pwd1 = scan.next();

                            try {
                                byte[] privateKey = KeyStoreUtil.decryptPrivateKey(data.toString(), pwd1);
                                config.setCoinbasePrivateKey(privateKey);
                            } catch (Exception e) {
                                System.out.println("You can not extract the private key with the password you entered.");
                                continue;
                            }

                        } catch (NumberFormatException e) {
                            System.out.println();
                            continue;
                        }

                        break;
                    }
                    case '3':
                        System.exit(0);
                        break;
                    default:
                        System.out.println();
                        continue;
                }
                break;
            }
        } else {
            while(true) {
                System.out.println("There are no keystore file");
                System.out.println("1. Create a new address");
                System.out.println("2. Enter the known private key");
                System.out.println("3. Exit");

                System.out.print(">> ");
                char choose = scan.next().charAt(0);

                switch (choose) {
                    case '1': {
                        if(!createPrivateKey(null, keystoreDir, keyStoreDataList)) {
                            continue;
                        };
                        break;
                    }
                    case '2': {
                        System.out.println("Please input private key");
                        System.out.print(">> ");
                        String privateHex = scan.next();
                        byte[] privateKey = Hex.decode(privateHex);

                        if(!createPrivateKey(privateKey, keystoreDir, keyStoreDataList)) {
                            continue;
                        };
                        break;
                    }
                    case '3':
                        System.exit(0);
                        break;
                    default:
                        System.out.println();
                        continue;
                }
                break;
            }
        }


        final boolean actionBlocksLoader = !config.blocksLoader().equals("");

        if (actionBlocksLoader) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }

        mEthereum = EthereumFactory.createEthereum();
        mEthereum.addListener(mListener);

        if (actionBlocksLoader) {
            mEthereum.getBlockLoader().loadBlocks();
        }
    }

    private static boolean createPrivateKey(byte[] privateKey, String keystoreDir, List<KeyStoreData> keyStoreDataList) {
        Scanner scan = new Scanner(System.in);

        if(privateKey == null) {
            privateKey = SecureRandom.getSeed(32);;
        }

        System.out.println("Please input your password");
        System.out.print(">> ");
        String pwd1 = scan.next();

        System.out.println("Please confirm your password");
        System.out.print(">> ");
        String pwd2 = scan.next();

        String alias = "alias";

        if (pwd1.equals(pwd2)) {
            savePrivateKeyStore(privateKey, alias, pwd1, keystoreDir, keyStoreDataList);

            Objects.requireNonNull(SystemProperties.getDefault()).setCoinbasePrivateKey(privateKey);
            return true;
        } else {
            System.out.println("Passwords do not match. Please retry entry");
            return false;
        }
    }

    private static void savePrivateKeyStore(byte[] privateKey, String alias, String password, String keystoreDir, List<KeyStoreData> keyStoreDataList) {
        String keystoreStr = KeyStoreUtil.getEncryptKeyStore(privateKey, alias, password);

        KeyStoreData data = new GsonBuilder().create().fromJson(keystoreStr, KeyStoreData.class);
        if(data == null) {
            return;
        }

        keyStoreDataList.add(data);

        // 파일을 저장한다.
        PrintWriter writer;
        try {
            writer = new PrintWriter(keystoreDir + "/" + KeyStoreUtil.getFileName(data), "UTF-8");
            writer.print(keystoreStr);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private static String readFile(File file) {

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while(line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private static EthereumListener mListener = new EthereumListenerAdapter() {

        boolean isStartGenerateTx = false;

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            System.out.println("SYND DONEDONEDONE");
        }

        long blockCount = 0;

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            if(!FastByteComparisons.equal(block.getCoinbase(), SystemProperties.getDefault().getMinerCoinbase())) {
                System.out.println("OnBlock : " + block.getNumber());
                System.out.println(block.toString());
            }

            System.out.println("Header Size : " + block.getHeader().getEncoded().length + "bytes, BLOCK : " + block.getEncoded().length);
            System.out.println("Header Size : " + block.getHeader().getEncoded().length + "bytes, BLOCK : " + block.getEncoded().length);
            System.out.println("Header Size : " + block.getHeader().getEncoded().length + "bytes, BLOCK : " + block.getEncoded().length);

            SecureRandom rnd = new SecureRandom();

            if(synced) {
                //generateTransactions(rnd.nextInt(100));
            }
        }
    };

    private static void generateTransactions(int num) {
        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));

        for (int i = mEthereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < num; i++, j++) {
            {
                StringBuilder temp = new StringBuilder();
                Random rnd = new Random();
                for (int k = 0; k < 20; k++) {
                    int rIndex = rnd.nextInt(3);
                    switch (rIndex) {
                        case 0:
                            // a-z
                            temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                            break;
                        case 1:
                            // A-Z
                            temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                            break;
                        case 2:
                            // 0-9
                            temp.append((rnd.nextInt(10)));
                            break;
                    }
                }

                byte[] receiverAddr = ECKey.fromPrivate(HashUtil.sha3(temp.toString().getBytes())).getAddress();

                byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(i);
                if (nonce.length == 0) {
                    nonce = new byte[]{0};
                }
                Transaction txs = new Transaction(nonce,
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                        receiverAddr, new BigInteger("1000000000000000000", 10).toByteArray()/*1 APIS*/, new byte[0], mEthereum.getChainIdForNextBlock());
                txs.sign(senderKey);
                //logger.info("<== Submitting tx: " + txs);
                mEthereum.submitTransaction(txs);
            }
        }
    }
}
