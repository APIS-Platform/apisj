package org.apis.cli;

import com.google.common.base.Strings;
import edu.vt.middleware.password.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.apis.config.SystemProperties;
import org.apis.crypto.ECKey;
import org.apis.db.ByteArrayWrapper;
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreManager;
import org.apis.keystore.KeyStoreUtil;
import org.apis.rpc.RPCServerManager;
import org.apis.util.ByteUtil;
import org.apis.util.ChainConfigUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

public class CLIStart {
    private SystemProperties config;
    private Properties daemonProp;
    private Properties rpcProp;
    private String dirDaemon;
    private String dirRpc;

    private static final String DEFAULT_MAX_PEERS = "30";

    private static final String KEY_COINBASE = "coinbase";
    private static final String KEY_MASTERNODE = "masternode";
    private static final String KEY_RECIPIENT = "recipient";

    /**
     * hub.apis.eco를 통해 지갑 정보를 미리 불러와서 이 변수에 저장한다.
     * 중복해서 지갑 정보를 부르지 않도록 하기 위함
     */
    private HashMap<ByteArrayWrapper, String> walletInfoMap = new HashMap<>();

    /**
     * 지갑 정보를 불러오는데 실패했으면, 이후에는 API 서버에 접속할 필요가 없도록 하기 위함
     */
    private boolean isFailedLoadWalletInfo = false;

    private String versionLatest = "";
    private String versionCurrent = "";
    private String updateJar = "";



    public CLIStart() throws IOException {
        config = SystemProperties.getDefault();

        if(config == null) {
            System.out.println("Failed to load config");
            System.exit(0);
        }

        dirDaemon = config.configDir() + "/daemon.properties";
        dirRpc = config.configDir() + "/rpc.properties";

        daemonProp = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        try {
            File configDir = new File(config.configDir());
            if(!configDir.exists()) {
                if(!configDir.mkdirs()) {
                    ConsoleUtil.printlnRed("Failed to create configuration file.");
                    System.exit(1);
                }
            }

            InputStream input = new FileInputStream(dirDaemon);
            daemonProp.load(input);

            String coinbase = daemonProp.getProperty(KEY_COINBASE);
            if(coinbase != null && !coinbase.isEmpty()) {
                config.setCoinbasePrivateKey(Hex.decode(coinbase));
            }

            String masternode = daemonProp.getProperty(KEY_MASTERNODE);
            if(masternode != null && !masternode.isEmpty()) {
                config.setMasternodePrivateKey(Hex.decode(masternode));
            }

            String recipient = daemonProp.getProperty(KEY_RECIPIENT);
            if(recipient != null && !recipient.isEmpty()) {
                config.setMasternodeRecipient(Hex.decode(recipient));
            }

        } catch (IOException e) {
            daemonProp.setProperty(KEY_COINBASE, ""); //
            daemonProp.setProperty(KEY_MASTERNODE, "");
            daemonProp.setProperty(KEY_RECIPIENT, "");
            daemonProp.setProperty("autoStart", "false");

            OutputStream output;
            try {
                output = new FileOutputStream(dirDaemon);
                daemonProp.store(output, null);
                output.close();
            } catch (IOException ignored) {}
        }

        rpcProp = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };
        try {
            InputStream input = new FileInputStream(dirRpc);
            rpcProp.load(input);
        } catch (IOException e) {
            rpcProp.setProperty(RPCServerManager.KEY_AVAILABLE_RPC, String.valueOf(false));
            rpcProp.setProperty(RPCServerManager.KEY_PORT, String.valueOf(new Random().nextInt(10000) + 40000));
            rpcProp.setProperty(RPCServerManager.KEY_ID, ByteUtil.toHexString(SecureRandom.getSeed(16)));
            rpcProp.setProperty(RPCServerManager.KEY_PASSWORD, ByteUtil.toHexString(SecureRandom.getSeed(16)));
            rpcProp.setProperty(RPCServerManager.KEY_MAX_CONNECTION, String.valueOf(1));
            rpcProp.setProperty(RPCServerManager.KEY_ALLOW_IP, "127.0.0.1");
            rpcProp.setProperty(RPCServerManager.KEY_MAX_PEERS, DEFAULT_MAX_PEERS);

            File configDir = new File(config.configDir());
            if(!configDir.exists()) {
                if(!configDir.mkdirs()) {
                    ConsoleUtil.printlnRed("Failed to create configuration file.");
                    System.exit(1);
                }
            }
            OutputStream output = new FileOutputStream(dirRpc);
            rpcProp.store(output, null);
            output.close();
        }
    }


    /**
     * 처음 켜지자마자 대시보드가 나타난 것인지 여부..
     * 10초 뒤 자동 시작시키기 위한 용도로 사용된다.
     */
    private boolean isFirstStart = true;
    /**
     * Dashboard를 표시해서 설정을 변경할 수 있도록 한다.
     * @throws IOException dsfds
     */
    void startDashBoard() throws IOException {

        dashboard:
        while(true) {
            clearConsole();

            printDashboard();

            String input;
            if(isFirstStart) {
                ConsoleUtil.printlnCyan("Or if no key is entered, APIS Core will start automatically after 10 seconds.");
                input = ConsoleUtil.readLineTimeOut(">> ", 10_000L);
                isFirstStart = false;
            } else {
                ConsoleUtil.printlnBlack("");
                input = ConsoleUtil.readLine(">> ");
            }

            switch(input) {
                // Network
                case "0": {
                    String network = getNetworkType(config);

                    if(network.equalsIgnoreCase("mainnet")) {
                        ChainConfigUtil.changeChain(ChainConfigUtil.CHAIN_PREBALANCE);
                    } else {
                        ChainConfigUtil.changeChain(ChainConfigUtil.CHAIN_MAINNET);
                    }
                    // Docker에서 바로 재시작 시킨다
                    System.exit(0);
                }
                // Max Peers
                case "1": {
                    changeMaxPeers();
                    continue;
                }
                // Miner
                case "2": {
                    changeMiner();
                    continue;
                }
                // Masternode
                case "3": {
                    changeMasternode();
                    continue;
                }
                // Reward Recipient
                case "4": {
                    changeRecipient();
                    continue;
                }
                // RPC Enabled
                case "5": {
                    toggleRpcEnabled();
                    continue;
                }
                // RPC Port
                case "6": {
                    changePort();
                    continue;
                }
                // RPC ID
                case "7": {
                    changeId();
                    continue;
                }
                // RPC Password
                case "8": {
                    changePassword();
                    continue;
                }
                // RPC Max Connections
                case "9": {
                    changeMaxConnection();
                    continue;
                }
                // RPC Allowed IP
                case "a":
                case "A": {
                    changeAllowIp();
                    continue;
                }
                // Update check
                case "b":
                case "B": {
                    updateCore();
                    continue;
                }
                default: {
                    break dashboard;
                }
            }
        }
    }

    /**
     * Console 화면을 지운다
     */
    private static void clearConsole()
    {
        try
        {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            }
            else {
                Runtime.getRuntime().exec("clear");
            }
            System.out.print("\033[H\033[2J");
        }
        catch (final Exception ignored) {}
    }

    private void printDashboard() throws IOException {
        File configDir = new File(config.configDir());
        if(!configDir.exists()) {
            if(!configDir.mkdirs()) {
                ConsoleUtil.printlnRed("Failed to create configuration file.");
                System.exit(1);
            }
        }

        // Coinbase
        byte[] coinbasePk = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_COINBASE, ""));
        byte[] coinbase = null;
        if(coinbasePk != null && coinbasePk.length > 0) {
            coinbase = ECKey.fromPrivate(coinbasePk).getAddress();
        }

        String coinbaseStr = getWalletStr(coinbase);
        if(coinbaseStr == null) {
            printDashboard();
            return;
        }


        // Masternode
        byte[] masternodePk = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_MASTERNODE, ""));
        byte[] masternode = null;
        if(masternodePk != null && masternodePk.length > 0) {
            masternode = ECKey.fromPrivate(masternodePk).getAddress();
        }

        String masternodeStr = getWalletStr(masternode);
        if(masternodeStr == null) {
            printDashboard();
            return;
        }

        // Recipient
        byte[] recipient = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_RECIPIENT, ""));
        String recipientStr = getWalletStr(recipient);
        if(recipientStr == null) {
            printDashboard();
            return;
        }



        ConsoleUtil.printlnBRed("APIS Core Settings ==========");
        ConsoleUtil.printlnYellow("v" + config.projectVersion());
        ConsoleUtil.printlnBlack("");
        printSettingRow("0", "Network", getNetworkType(config));
        printSettingRow("1", "Max Peers", rpcProp.getProperty(RPCServerManager.KEY_MAX_PEERS));
        ConsoleUtil.printlnBlack("");
        printSettingRow("2", "Miner", coinbaseStr);
        ConsoleUtil.printlnBlack("");
        printSettingRow("3", KEY_MASTERNODE, masternodeStr);
        printSettingRow("4", "Reward Recipient", recipientStr);
        ConsoleUtil.printlnBlack("");
        printSettingRow("5", "RPC Enabled", rpcProp.getProperty(RPCServerManager.KEY_AVAILABLE_RPC));
        printSettingRow("6", "RPC Port", rpcProp.getProperty(RPCServerManager.KEY_PORT));
        printSettingRow("7", "RPC ID", rpcProp.getProperty(RPCServerManager.KEY_ID));
        printSettingRow("8", "RPC Password", rpcProp.getProperty(RPCServerManager.KEY_PASSWORD));
        printSettingRow("9", "RPC Max Connections", rpcProp.getProperty(RPCServerManager.KEY_MAX_CONNECTION));
        printSettingRow("A", "RPC Allowed IP", rpcProp.getProperty(RPCServerManager.KEY_ALLOW_IP).replaceAll(",", ", "));
        ConsoleUtil.printlnBlack("");
        if(isNeedUpdate()) {
            printSettingRow("B", "Update APIS Core", String.format("%s => %s", versionCurrent, versionLatest));
            ConsoleUtil.printlnBlack("");
        }
        ConsoleUtil.printlnCyan("Input other key to start APIS Core");
    }

    /**
     * hub.apis.eco의 API 호출을 통해 입력된 지갑의 현재 잔고를 확인한다.
     * @param wallet byte array 지갑 주소
     * @return TRUE : 정상적으로 불러온 경우, FALSE : 불러오지 않은 경우(실패, 또는 이미 불러옴)
     */
    private boolean getWalletInfo(byte[] wallet) {
        if(isFailedLoadWalletInfo) {
            return false;
        }
        // 이미 데이터를 불러왔었으면 더 불러올 필요 없음
        if(walletInfoMap.get(new ByteArrayWrapper(wallet)) != null) {
            return false;
        }

        String walletInfoUrl = String.format("https://hub.apis.eco:37770/api/v1.2/getwalletinfo/%s", ByteUtil.toHexString(wallet));

        try {
            URL url = new URL(walletInfoUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            InputStream is = con.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while((cp = rd.read()) != -1) {
                sb.append((char)cp);
            }
            String walletInfoJson = sb.toString();

            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject)parser.parse(walletInfoJson);
            String balance = obj.get("APIS").toString();

            walletInfoMap.put(new ByteArrayWrapper(wallet), balance);
            return true;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            isFailedLoadWalletInfo = true;
        }
        return false;
    }

    private String getWalletStr(byte[] wallet) {
        String walletStr;
        if(wallet == null || wallet.length == 0) {
            walletStr = "-";
        } else {
            // API 서버를 통해 채굴자의 정보를 불러오게 한다.
            // 만약 불러왔다면, 다시 화면에 불려온 정보를 출력한다.
            if(getWalletInfo(wallet)) {
                return null;
            }

            String balance = walletInfoMap.get(new ByteArrayWrapper(wallet));
            if(balance != null && balance.isEmpty() == false) {
                walletStr = String.format("%s (%s APIS)", ByteUtil.toHexString(wallet), balance);
            } else {
                walletStr = ByteUtil.toHexString(wallet);
            }
        }

        return walletStr;
    }

    private static void printSettingRow(String number, String name, String value) {
        if(value == null || value.isEmpty() == false) {
            value = ": " + value;
        }

        String left = ConsoleUtil.colorBGreen(StringUtils.rightPad(String.format("[%s]", number), 5));
        String center = ConsoleUtil.colorGreen(StringUtils.rightPad(name, 20));

        System.out.println(left + center + value);
    }

    private static String getNetworkType(SystemProperties config) {
        switch(config.getBlockchainConfig().getConfigForBlock(0).getChainId()) {
            case 1:
                return "Mainnet";
            case 7:
                return "Testnet";
            default:
                return "";
        }
    }

    private void changeMiner() throws IOException {

        while(true) {
            clearConsole();

            ConsoleUtil.printlnGreen("You can get rewards through APIS Block mining.");
            ConsoleUtil.printlnGreen("You should input Private key of a miner to start mining.");
            ConsoleUtil.printlnGreen("The chance of getting reward goes higher with the registered miner's balance.");
            ConsoleUtil.printlnGreen("--");

            byte[] coinbasePk = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_COINBASE, ""));
            byte[] coinbase = null;
            if(coinbasePk != null && coinbasePk.length > 0) {
                coinbase = ECKey.fromPrivate(coinbasePk).getAddress();
            }

            String minerStr;
            if(coinbase != null && coinbase.length > 0) {
                minerStr = getWalletStr(coinbase);
            } else {
                minerStr = "Not set";
            }
            System.out.println(String.format("%s %s", ConsoleUtil.colorGreen("Miner : "), ConsoleUtil.colorBPurple(minerStr)));
            ConsoleUtil.printlnGreen("--\n");

            ConsoleUtil.printlnGreen("--");
            printSettingRow("1", "Select miner from locked private key file", "");
            printSettingRow("2", "Deactivate mining (Clear miner setting)", "");
            printSettingRow("3", "Done", "");

            switch (readNumber(">> ")) {
                case 1: {
                    byte[] newPk = pickPrivateKey("Which address would you like to mining?", coinbase, TYPE_PK_MINER);
                    if(newPk != null) {
                        daemonProp.setProperty(KEY_COINBASE, ByteUtil.toHexString(newPk));
                        config.setCoinbasePrivateKey(newPk);
                        storeDaemonConfig();
                    }
                    continue;
                }
                case 2: {
                    daemonProp.setProperty(KEY_COINBASE, "");
                    config.setCoinbasePrivateKey(null);
                    storeDaemonConfig();
                    continue;
                }
                case 3: {
                    return;
                }
                default: {}
            }
        }
    }

    private void changeMasternode() throws IOException {

        while(true) {
            clearConsole();

            ConsoleUtil.printlnGreen("You should input Private key of a masternode to staking.");
            ConsoleUtil.printlnGreen("The balance of the Masternode must be exactly 50,000, 200,000, and 500,000 APIS.");
            ConsoleUtil.printlnGreen("--");

            byte[] masternodePk = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_MASTERNODE, ""));
            byte[] masternode = null;
            if(masternodePk != null && masternodePk.length > 0) {
                masternode = ECKey.fromPrivate(masternodePk).getAddress();
            }

            String masternodeStr;
            if(masternode != null && masternode.length > 0) {
                masternodeStr = getWalletStr(masternode);
            } else {
                masternodeStr = "Not set";
            }
            System.out.println(String.format("%s %s", ConsoleUtil.colorGreen("Masternode : "), ConsoleUtil.colorBPurple(masternodeStr)));

            ConsoleUtil.printlnGreen("--");
            ConsoleUtil.printlnBlack("");
            printSettingRow("1", "Select masternode from locked private key file", "");
            printSettingRow("2", "Deactivate masternode (Clear masternode & recipient setting)", "");
            printSettingRow("3", "Done", "");

            switch (readNumber(">> ")) {
                case 1: {
                    byte[] newPk = pickPrivateKey("Which address would you like to masternode?", masternode, TYPE_PK_MASTERNODE);
                    if(newPk != null) {
                        daemonProp.setProperty(KEY_MASTERNODE, ByteUtil.toHexString(newPk));
                        config.setMasternodePrivateKey(newPk);
                        storeDaemonConfig();
                    }
                    continue;
                }
                case 2: {
                    clearMasternode();
                    continue;
                }
                case 3: {
                    return;
                }
                default: {}
            }
        }
    }

    private void clearMasternode() throws IOException {
        daemonProp.setProperty(KEY_MASTERNODE, "");
        daemonProp.setProperty(KEY_RECIPIENT, "");
        config.setMasternodePrivateKey(null);
        config.setMasternodeRecipient(null);
        storeDaemonConfig();
    }

    private void changeRecipient() throws IOException {

        while(true) {
            clearConsole();


            ConsoleUtil.printlnGreen("You should input address of a recipient to receive the Masternode's reward instead.");
            ConsoleUtil.printlnGreen("--");

            byte[] recipientAddr = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_RECIPIENT, ""));

            String recipientStr;
            if(recipientAddr != null && recipientAddr.length > 0) {
                recipientStr = getWalletStr(recipientAddr);
                if(recipientStr == null) {
                    continue;
                }
            } else {
                recipientStr = "Not set";
            }
            System.out.println(String.format("%s %s", ConsoleUtil.colorGreen("Recipient : "), ConsoleUtil.colorBPurple(recipientStr)));

            ConsoleUtil.printlnGreen("--");
            ConsoleUtil.printlnBlack("");
            printSettingRow("1", "Select recipient from locked private key file", "");
            printSettingRow("2", "Deactivate masternode (Clear masternode & recipient setting)", "");
            printSettingRow("3", "Done", "");

            switch (readNumber(">> ")) {
                case 1: {
                    byte[] newAddr = pickPrivateKey("Which address would you like to recipient?", recipientAddr, TYPE_PK_RECIPIENT);
                    if(newAddr != null) {
                        daemonProp.setProperty(KEY_RECIPIENT, ByteUtil.toHexString(newAddr));
                        config.setMasternodeRecipient(newAddr);
                        storeDaemonConfig();
                    }
                    continue;
                }
                case 2: {
                    clearMasternode();
                    continue;
                }
                case 3: {
                    return;
                }
                default: {}
            }
        }
    }

    private final int TYPE_PK_MINER = 0;
    private final int TYPE_PK_MASTERNODE = 1;
    private final int TYPE_PK_RECIPIENT = 2;

    private byte[] pickPrivateKey(String title, final byte[] selectedAddress, final int type) throws IOException {
        String errorMessage = "";

        while(true) {
            clearConsole();

            if(errorMessage.isEmpty() == false) {
                ConsoleUtil.printlnRed(errorMessage);
                ConsoleUtil.printlnRed("");
            }

            ConsoleUtil.printlnGreen(title);
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Generate a new private key", "");
            printSettingRow("B", "Import private key", "");
            printSettingRow("C", "Cancel", "");
            ConsoleUtil.printlnGreen("");

            List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

            byte[] miner = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_COINBASE, ""));
            byte[] masternode = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_MASTERNODE, ""));
            byte[] recipient = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_RECIPIENT, ""));

            switch(type) {
                case TYPE_PK_MINER:
                    if (masternode != null && masternode.length > 0) {
                        keyStoreDataList.removeIf(data -> FastByteComparisons.equal(ByteUtil.hexStringToBytes(data.address), masternode));
                    }
                    break;
                case TYPE_PK_MASTERNODE:
                    if (miner != null && miner.length > 0) {
                        keyStoreDataList.removeIf(data -> FastByteComparisons.equal(ByteUtil.hexStringToBytes(data.address), miner));
                    }
                    if (recipient != null && recipient.length > 0) {
                        keyStoreDataList.removeIf(data -> FastByteComparisons.equal(ByteUtil.hexStringToBytes(data.address), recipient));
                    }
                    break;
                case TYPE_PK_RECIPIENT:
                    if (masternode != null && masternode.length > 0) {
                        keyStoreDataList.removeIf(data -> FastByteComparisons.equal(ByteUtil.hexStringToBytes(data.address), masternode));
                    }
                    break;
            }

            int keyStoreSize = keyStoreDataList.size();
            if(keyStoreSize > 0) {
                for (int i = 0; i < keyStoreSize; i++) {
                    String address = getWalletStr(ByteUtil.hexStringToBytes(keyStoreDataList.get(i).address));
                    if(address == null ){
                        i = i - 1;
                        continue;
                    }
                    if(selectedAddress != null && FastByteComparisons.equal(selectedAddress, ByteUtil.hexStringToBytes(keyStoreDataList.get(i).address))) {
                        address += " (*)";
                    }
                    printSettingRow(String.valueOf(i + 1), address, "");
                }
            }

            String input = ConsoleUtil.readLine(">> ");
            switch(input) {
                case "a":
                case "A": {
                    KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                    errorMessage = "";
                    continue;
                }
                case "b":
                case "B": {
                    ConsoleUtil.printlnGreen("Please input your Private key(Hex).");
                    String privateHex = ConsoleUtil.readLine(">> ");
                    byte[] pk;
                    try {
                        pk = ByteUtil.hexStringToBytes(privateHex);
                        if (pk == null || pk.length == 0) {
                            errorMessage = "The privateKey you've entered is incorrect.";
                            continue;
                        }
                    } catch (Exception ignore) {
                        errorMessage = "The privateKey you've entered is incorrect.";
                        continue;
                    }

                    try {
                        KeyStoreManager.getInstance().createPrivateKeyCLI(pk);
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }
                    continue;
                }
                case "c":
                case "C": {
                    return null;
                }
                default: {
                    int addressIndex;
                    try {
                        addressIndex = Integer.parseInt(input) - 1;
                    } catch (NumberFormatException ignored) {
                        addressIndex = -1;
                    }

                    if(addressIndex >= keyStoreSize) {
                        errorMessage = "Please enter correct number.";
                        continue;
                    }

                    KeyStoreData data;
                    try {
                        data = keyStoreDataList.get(addressIndex);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        errorMessage = "Please enter correct number..";
                        continue;
                    }
                    if(data == null) {
                        errorMessage = "The keystore data is empty.";
                        continue;
                    }

                    if(type == TYPE_PK_RECIPIENT) {
                        return ByteUtil.hexStringToBytes(data.address);
                    }


                    ConsoleUtil.printlnGreen("Please enter the password of [%s]", data.address);
                    char[] password = ConsoleUtil.readPassword(">> ");
                    try {
                        return KeyStoreUtil.decryptPrivateKey(data.toString(), String.valueOf(password));
                    } catch (Exception e) {
                        errorMessage = "The password you've entered is incorrect.";
                    }
                }
            }
        }
    }


    void startKeystoreCheck() throws IOException {
        ConsoleUtil.printlnGreen("You can get rewards through APIS Block mining.");
        ConsoleUtil.printlnGreen("You should input Private key of a miner to start mining.");
        ConsoleUtil.printlnGreen("The chance of getting reward goes higher with the registered miner's balance.");
        ConsoleUtil.printlnGreen("--");

        byte[] coinbasePk = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_COINBASE, ""));
        byte[] coinbase = null;
        if(coinbasePk != null && coinbasePk.length > 0) {
            coinbase = ECKey.fromPrivate(coinbasePk).getAddress();
        }

        if(coinbase != null && coinbase.length > 0) {
            ConsoleUtil.printlnGreen("Stored coinbase : ");
            ConsoleUtil.printlnPurple("[[ %s ]]", ByteUtil.toHexString(coinbase));
            ConsoleUtil.printlnGreen("--\n");
        }



        while(true) {
            List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

            if(keyStoreDataList.size() > 0) {
                keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

                if(keyStoreDataList.size() == 1) {
                    ConsoleUtil.printlnGreen("%d locked Private key is found.", keyStoreDataList.size());
                } else {
                    ConsoleUtil.printlnGreen("%d locked Private keys are found.", keyStoreDataList.size());
                }
                ConsoleUtil.printlnGreen("--");
                ConsoleUtil.printlnCyan("1. Generate a new Private key");
                ConsoleUtil.printlnCyan("2. Import your Private key");
                ConsoleUtil.printlnCyan("3. Select coinbase from locked Private key file");
                ConsoleUtil.printlnCyan("4. Deactivate mining function");
                ConsoleUtil.printlnCyan("5. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                        continue;
                    }

                    case 2: {
                        ConsoleUtil.printlnGreen("Please input your Private key.");
                        String privateHex = ConsoleUtil.readLine(">> ");
                        byte[] pk;
                        try {
                            pk = ByteUtil.hexStringToBytes(privateHex);
                            if (pk == null || pk.length == 0) {
                                continue;
                            }
                        } catch (Exception ignore) {
                            ConsoleUtil.printlnRed("The privateKey you've entered is incorrect.");
                            continue;
                        }

                        KeyStoreManager.getInstance().createPrivateKeyCLI(pk);
                        continue;
                    }

                    case 3: {
                        ConsoleUtil.printlnGreen("Which address would you like to mining and enable mining?");

                        int keyStoreSize = keyStoreDataList.size();
                        int pad = (int) Math.log10(keyStoreSize) + 1;   // left pad
                        for(int i = 0; i < keyStoreSize; i++) {
                            ConsoleUtil.printlnCyan("[%" + pad + "s] %s", i + 1, keyStoreDataList.get(i).address);
                        }

                        int addressIndex = readNumber(">> ") - 1;
                        if(addressIndex >= keyStoreSize) {
                            ConsoleUtil.printlnRed("Please enter correct number.\n");
                            continue;
                        }

                        KeyStoreData data;
                        try {
                            data = keyStoreDataList.get(addressIndex);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            ConsoleUtil.printlnRed("Please enter correct number..\n");
                            continue;
                        }
                        if(data == null) {
                            ConsoleUtil.printlnRed("The keystore data is empty.\n");
                            continue;
                        }

                        ConsoleUtil.printlnGreen("Please enter the password of [%s]", data.address);
                        char[] password = ConsoleUtil.readPassword(">> ");
                        try {
                            byte[] privateKey = KeyStoreUtil.decryptPrivateKey(data.toString(), String.valueOf(password));
                            config.setCoinbasePrivateKey(privateKey);
                            daemonProp.setProperty(KEY_COINBASE, ByteUtil.toHexString(privateKey));
                        } catch (Exception e) {
                            ConsoleUtil.printlnRed("The password you've entered is incorrect.\n");
                            continue;
                        }

                        storeDaemonConfig();
                        ConsoleUtil.printlnGreen("Mining is enabled and saved to settings.");
                        System.exit(0);
                        break;
                    }
                    case 4:
                        // 채굴을 사용하지 않으므로 빠져나간다.
                        daemonProp.setProperty(KEY_COINBASE, "");
                        storeDaemonConfig();
                        ConsoleUtil.printlnCyan("Mining function has been disabled.");
                        System.exit(0);
                        break;
                    case 5:
                        ConsoleUtil.printlnGreen("Bye");
                        System.exit(0);
                        break;
                    default:
                        System.out.println();
                }
            } else {

                ConsoleUtil.printlnGreen("No Private key is found.");
                ConsoleUtil.printlnGreen("--");
                ConsoleUtil.printlnCyan("1. Generate a new Private key");
                ConsoleUtil.printlnCyan("2. Input your Private key");
                ConsoleUtil.printlnCyan("3. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                        continue;
                    }
                    case 2: {
                        ConsoleUtil.printlnGreen("Please input your Private key.");
                        String privateHex = ConsoleUtil.readLine(">> ");
                        byte[] pk;
                        try {
                            pk = ByteUtil.hexStringToBytes(privateHex);
                            if (pk == null || pk.length == 0) {
                                continue;
                            }
                        } catch (Exception ignore) {
                            ConsoleUtil.printlnRed("The privateKey you've entered is incorrect.");
                            continue;
                        }

                        KeyStoreManager.getInstance().createPrivateKeyCLI(pk);
                        continue;
                    }
                    case 3:
                        ConsoleUtil.printlnCyan("Bye");
                        System.exit(0);
                        break;
                    default:
                        System.out.println();
                }
            }
        }
    }


    void startMasternodeSetting() throws IOException {

        List<KeyStoreData> keyStoreExceptMiner = KeyStoreManager.getInstance().loadKeyStoreFiles();

        String coinbasePrivateKey = daemonProp.getProperty(KEY_COINBASE, "");
        byte[] privateKey = ByteUtil.hexStringToBytes(coinbasePrivateKey);
        byte[] coinbase;
        if(privateKey != null && privateKey.length > 0) {
            coinbase = ECKey.fromPrivate(privateKey).getAddress();
            if(coinbase != null && coinbase.length > 0) {
                keyStoreExceptMiner.removeIf(data -> FastByteComparisons.equal(ByteUtil.hexStringToBytes(data.address), coinbase));   // remove miner address
            }
        }




        // 마스터노드 설정을 확인한다.
        ConsoleUtil.printlnGreen("");
        ConsoleUtil.printlnGreen("You should input Private key of a masternode to staking.");
        ConsoleUtil.printlnGreen("The balance of the Masternode must be exactly 50,000, 200,000, and 500,000 APIS.");
        ConsoleUtil.printlnGreen("--");

        daemonProp.setProperty(KEY_MASTERNODE, "");
        daemonProp.setProperty(KEY_RECIPIENT, "");

        byte[] masternodePk = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_MASTERNODE, ""));
        byte[] masternode = null;
        if(masternodePk != null && masternodePk.length > 0) {
            masternode = ECKey.fromPrivate(masternodePk).getAddress();
        }
        byte[] recipient = ByteUtil.hexStringToBytes(daemonProp.getProperty(KEY_RECIPIENT, ""));

        if(masternode != null && masternode.length > 0 && recipient != null && recipient.length > 0) {
            ConsoleUtil.printlnGreen("Stored information : ");
            ConsoleUtil.printlnPurple("Masternode : [[ %s ]]", ByteUtil.toHexString(masternode));
            ConsoleUtil.printlnPurple("Recipient  : [[ %s ]]", ByteUtil.toHexString(recipient));
            ConsoleUtil.printlnGreen("--\n");
        }



        while (true) {
            keyStoreExceptMiner = KeyStoreManager.getInstance().loadKeyStoreFiles();
            int sizeOfKeystoreExceptMiner = keyStoreExceptMiner.size();

            if(sizeOfKeystoreExceptMiner == 0) {
                ConsoleUtil.printlnGreen("No Private key is found.");
                ConsoleUtil.printlnGreen("--");
                ConsoleUtil.printlnCyan("1. Generate a new Private key");
                ConsoleUtil.printlnCyan("2. Input your Private key");
                ConsoleUtil.printlnCyan("3. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                        continue;
                    }
                    case 2: {
                        ConsoleUtil.printlnGreen("Please input your Private key.");
                        String privateHex = ConsoleUtil.readLine(">> ");
                        byte[] pk = ByteUtil.hexStringToBytes(privateHex);

                        KeyStoreManager.getInstance().createPrivateKeyCLI(pk);
                        continue;
                    }
                    case 3:
                        ConsoleUtil.printlnCyan("Bye");
                        System.exit(0);
                        break;
                    default:
                        System.out.println();
                }
            }

            else if (sizeOfKeystoreExceptMiner == 1) {
                ConsoleUtil.printlnGreen("%d locked Private key is found.", keyStoreExceptMiner.size());
            } else {
                ConsoleUtil.printlnGreen("%d locked Private keys are found.", keyStoreExceptMiner.size());
            }
            ConsoleUtil.printlnGreen("--");
            ConsoleUtil.printlnCyan("1. Generate a new Private key");
            ConsoleUtil.printlnCyan("2. Import your Private key");
            ConsoleUtil.printlnCyan("3. Select Masternode from locked Private key and enable masternode");
            ConsoleUtil.printlnCyan("4. Deactivate Masternode function");
            ConsoleUtil.printlnCyan("5. Exit");

            switch (readNumber(">> ")) {
                case 1: {
                    KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                    continue;
                }

                case 2: {
                    ConsoleUtil.printlnGreen("Please input your Private key.");
                    String privateHex = ConsoleUtil.readLine(">> ");
                    byte[] pk;
                    try {
                        pk = ByteUtil.hexStringToBytes(privateHex);
                        if (pk == null || pk.length == 0) {
                            continue;
                        }
                    } catch (Exception ignore) {
                        ConsoleUtil.printlnRed("The privateKey you've entered is incorrect.");
                        continue;
                    }

                    KeyStoreManager.getInstance().createPrivateKeyCLI(pk);
                    continue;
                }

                case 3: {
                    ConsoleUtil.printlnGreen("Which address of Private key would you import?");

                    int pad = (int) Math.log10(sizeOfKeystoreExceptMiner) + 1;   // left pad
                    for(int i = 0; i < sizeOfKeystoreExceptMiner; i++) {
                        ConsoleUtil.printlnCyan("[%" + pad + "s] %s", i + 1, keyStoreExceptMiner.get(i).address);
                    }

                    int addressIndex = readNumber(">> ") - 1;
                    if(addressIndex >= sizeOfKeystoreExceptMiner) {
                        ConsoleUtil.printlnRed("Please enter correct number.\n");
                        continue;
                    }

                    KeyStoreData data;
                    try {
                        data = keyStoreExceptMiner.get(addressIndex);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ConsoleUtil.printlnRed("Please enter correct number..\n");
                        continue;
                    }
                    if(data == null) {
                        ConsoleUtil.printlnRed("The keystore data is empty.\n");
                        continue;
                    }

                    ConsoleUtil.printlnGreen("Please enter the password of [%s]", data.address);
                    char[] password = ConsoleUtil.readPassword(">> ");
                    try {
                        byte[] pk = KeyStoreUtil.decryptPrivateKey(data.toString(), String.valueOf(password));
                        config.setMasternodePrivateKey(pk);
                        daemonProp.setProperty(KEY_MASTERNODE, ByteUtil.toHexString(pk));

                        while(true) {
                            ConsoleUtil.printlnGreen("Please enter the address to receive the Masternode's reward instead.");
                            String recipientInput = ConsoleUtil.readLine(">> ");

                            try {
                                byte[] recipientAddr = Hex.decode(recipientInput);
                                if(recipientAddr.length > 20 || recipientAddr.length < 4) {
                                    ConsoleUtil.printlnRed("The address you've entered is incorrect.");
                                    continue;
                                }

                                config.setMasternodeRecipient(recipientAddr);
                                daemonProp.setProperty(KEY_RECIPIENT, recipientInput);
                            } catch (Exception ignored) {
                                ConsoleUtil.printlnRed("The address you've entered is incorrect.");
                                continue;
                            }
                            break;
                        }

                        ConsoleUtil.printlnBlue("The Masternode function has been activated for the following address : [%s]", data.address);
                        ConsoleUtil.printlnBlue("The recipient address is : [%s]", ByteUtil.toHexString(config.getMasternodeRecipient()));
                        ConsoleUtil.printlnBlue("The Masternode can only work if the balance is exactly 50,000, 200,000, or 500,000 APIS.");
                        ConsoleUtil.readLine("Press [Enter] to exit");
                        storeDaemonConfig();
                        System.exit(1);
                    } catch (Exception e) {
                        ConsoleUtil.printlnRed("The password you've entered is incorrect.\n");
                        continue;
                    }
                    break;
                }
                case 4:
                    // Deactivated Masternode
                    daemonProp.setProperty(KEY_MASTERNODE, "");
                    daemonProp.setProperty(KEY_RECIPIENT, "");
                    storeDaemonConfig();
                    break;
                case 5:
                    ConsoleUtil.printlnCyan("Bye");
                    System.exit(0);
                    break;
                default :
                    System.out.println();
                    continue;
            }
            break;
        }
    }

    private void storeDaemonConfig() throws IOException {
        daemonProp.setProperty("autoStart", "true");
        OutputStream daemonOutput = new FileOutputStream(dirDaemon);
        daemonProp.store(daemonOutput, null);
        daemonOutput.close();
    }



    private static final int SELECT_SERVERCHECK_START_WITHOUTRPC = 0;
    private static final int SELECT_SERVERCHECK_START_WITHRPC = 1;
    private static final int SELECT_SERVERCHECK_CHANGE_PORT = 2;
    private static final int SELECT_SERVERCHECK_CHANGE_ID = 3;
    private static final int SELECT_SERVERCHECK_CHANGE_PASSWORD = 4;
    private static final int SELECT_SERVERCHECK_CHANGE_MAXCONNECTIONS = 5;
    private static final int SELECT_SERVERCHECK_CHANGE_ALLOWIP = 6;
    private static final int SELECT_SERVERCHECK_CHANGE_MAXPEERS = 7;

    void startRpcServerCheck() throws IOException {
        File configDir = new File(config.configDir());
        if(!configDir.exists()) {
            if(!configDir.mkdirs()) {
                ConsoleUtil.printlnRed("Failed to create configuration file.");
                System.exit(1);
            }
        }

        ConsoleUtil.printlnBlue("You can interact with this program remotely through a Web socket-based RPC server.");
        ConsoleUtil.printlnBlue("The settings of the RPC server can be loaded from a file(rpc.properties) or set via interactive input.\n");
        Properties prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        InputStream input = new FileInputStream(dirRpc);
        prop.load(input);


        while(true) {
            ConsoleUtil.printlnBlue("The current setting is as follows.\n");
            printProp(RPCServerManager.KEY_AVAILABLE_RPC, prop);
            printProp(RPCServerManager.KEY_PORT, prop);
            printProp(RPCServerManager.KEY_ID, prop);
            printProp(RPCServerManager.KEY_PASSWORD, prop);
            printProp(RPCServerManager.KEY_MAX_CONNECTION, prop);
            printProp(RPCServerManager.KEY_ALLOW_IP, prop);
            printProp(RPCServerManager.KEY_MAX_PEERS, prop);


            ConsoleUtil.printlnBlue("\nDo you want to change the settings?");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_START_WITHOUTRPC + ". Disable RPC server");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_START_WITHRPC  + ". Enable RPC server with this setting");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_CHANGE_PORT + ". Change port");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_CHANGE_ID + ". Change id");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_CHANGE_PASSWORD + ". Change password");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_CHANGE_MAXCONNECTIONS + ". Change max_rpc_connections");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_CHANGE_ALLOWIP + ". Change allow_ip");
            ConsoleUtil.printlnGreen("\n* About APIS Nodes ----------");
            ConsoleUtil.printlnGreen( SELECT_SERVERCHECK_CHANGE_MAXPEERS + ". Change max_peers\n");
            ConsoleUtil.printlnGreen( "8. Exit");

            switch (readNumber(">> ")) {

                case SELECT_SERVERCHECK_START_WITHOUTRPC:
                    prop.setProperty(RPCServerManager.KEY_AVAILABLE_RPC, String.valueOf(false));
                    break;
                case SELECT_SERVERCHECK_START_WITHRPC:
                    prop.setProperty(RPCServerManager.KEY_AVAILABLE_RPC, String.valueOf(true));
                    break;
                case SELECT_SERVERCHECK_CHANGE_PORT:
                    changePort();
                    break;
                case SELECT_SERVERCHECK_CHANGE_ID:
                    changeId();
                    break;
                case SELECT_SERVERCHECK_CHANGE_PASSWORD:
                    changePassword();
                    break;
                case SELECT_SERVERCHECK_CHANGE_MAXCONNECTIONS:
                    changeMaxConnection();
                    break;
                case SELECT_SERVERCHECK_CHANGE_ALLOWIP:
                    changeAllowIp();
                    break;
                case SELECT_SERVERCHECK_CHANGE_MAXPEERS:
                    changeMaxPeers();
                    break;

                case 8:
                    storeRpcConfig();
                    ConsoleUtil.printlnGreen("Bye");
                    System.exit(0);
                    break;
            }
        }
    }

    private int readNumber(String format, Object... args) throws IOException {
        String choose = ConsoleUtil.readLine(format, args);
        int chooseNum;
        try {
            chooseNum = Integer.parseInt(choose);
        } catch (NumberFormatException e) {
            return -1;
        }
        return chooseNum;
    }

    private void printProp(String key, Properties prop) {
        String value = prop.getProperty(key);
        if(key.equalsIgnoreCase(RPCServerManager.KEY_MAX_PEERS) && value == null) {
            value = DEFAULT_MAX_PEERS;
        }
        ConsoleUtil.printlnPurple(Strings.padEnd(key, 20, ' ') + ": " + value);
    }

    private void changeRpcEnabled(boolean isEnable) {
        rpcProp.setProperty(RPCServerManager.KEY_AVAILABLE_RPC, String.valueOf(isEnable));
    }

    private void toggleRpcEnabled() {
        boolean oldSetting = Boolean.parseBoolean(rpcProp.getProperty(RPCServerManager.KEY_AVAILABLE_RPC));
        changeRpcEnabled(!oldSetting);
    }

    private void changePort() throws IOException {
        String errorMsg = "";
        while(true) {
            clearConsole();

            if(errorMsg.isEmpty() == false) {
                ConsoleUtil.printlnRed(errorMsg);
                ConsoleUtil.printlnRed("");
            }

            System.out.println(String.format(StringUtils.rightPad("RPC Port [%s] ", 30, "="), ConsoleUtil.colorPurple(rpcProp.getProperty(RPCServerManager.KEY_PORT))));
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Which port would you like to open?");
            ConsoleUtil.printlnGreen("Do not enter a number that is aleady in use.");
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Pick random number", "");
            printSettingRow("B", "Done", "");
            printSettingRow("10000 ~ 65535", "", "");

            String input = ConsoleUtil.readLine(">> ");
            String newPort;
            switch (input) {
                case "A":
                case "a":
                    newPort = String.valueOf(new Random().nextInt(10000) + 50000);
                    break;

                case "B":
                case "b":
                    return;

                default: {
                    try {
                        int port = Integer.parseInt(input);

                        if(port < 0 || port > 65535) {
                            ConsoleUtil.printlnRed("Please enter the correct port number.");
                            continue;
                        }

                        newPort = String.valueOf(port);
                    } catch (NumberFormatException e) {
                        errorMsg = "Please enter the correct port number.";
                        continue;
                    }
                }
            }

            rpcProp.put(RPCServerManager.KEY_PORT, newPort);
            storeRpcConfig();
        }
    }

    private void changeId() throws IOException {
        StringBuilder errorMsg = new StringBuilder();
        while(true) {
            clearConsole();

            if(errorMsg.length() != 0) {
                ConsoleUtil.printlnRed(errorMsg.toString());
                errorMsg = new StringBuilder();
            }

            System.out.println(String.format(StringUtils.rightPad("RPC ID [%s] ", 30, "="), ConsoleUtil.colorPurple(rpcProp.getProperty(RPCServerManager.KEY_ID))));
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Which ID would you like to login?");
            ConsoleUtil.printlnGreen("Please enter 8 ~ 32 characters.");
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Generate new random ID", "");
            printSettingRow("B", "Done", "");
            ConsoleUtil.printlnGreen("");

            String input = ConsoleUtil.readLine("Please enter the ID : ");
            String newId;
            switch (input) {
                case "A":
                case "a":
                    newId = ByteUtil.toHexString(SecureRandom.getSeed(16));
                    break;

                case "B":
                case "b":
                    return;

                default: {
                    newId = input;

                    List<Rule> ruleList = new ArrayList<>();
                    ruleList.add(new LengthRule(8, 32));
                    ruleList.add(new WhitespaceRule());
                    ruleList.add(new DigitCharacterRule());
                    ruleList.add(new AlphabeticalCharacterRule());
                    ruleList.add(new AlphabeticalSequenceRule());
                    ruleList.add(new NumericalSequenceRule());
                    ruleList.add(new QwertySequenceRule());

                    PasswordValidator validator = new PasswordValidator(ruleList);
                    PasswordData passwordData = new PasswordData(new Password(newId));
                    RuleResult result = validator.validate(passwordData);

                    if(result.isValid() == false) {
                        errorMsg = new StringBuilder();
                        for (String msg : validator.getMessages(result)) {
                            errorMsg.append(msg).append("\n");
                        }
                        continue;
                    }
                    break;
                }
            }

            rpcProp.put(RPCServerManager.KEY_ID, newId);
            storeRpcConfig();
        }
    }

    private void changePassword() throws IOException {
        StringBuilder errorMsg = new StringBuilder();
        while(true) {
            clearConsole();

            if((errorMsg.length() == 0) == false) {
                ConsoleUtil.printlnRed(errorMsg.toString());
                errorMsg = new StringBuilder();
            }

            System.out.println(String.format(StringUtils.rightPad("RPC Password [%s] ", 30, "="), ConsoleUtil.colorPurple(rpcProp.getProperty(RPCServerManager.KEY_PASSWORD))));
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Which Password would you like to login?");
            ConsoleUtil.printlnGreen("Please enter 8 ~ 32 characters.");
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Generate new random password", "");
            printSettingRow("B", "Done", "");
            ConsoleUtil.printlnGreen("");

            String input = ConsoleUtil.readLine("Please enter the password : ");
            String newPassword;
            switch (input) {
                case "A":
                case "a":
                    newPassword = ByteUtil.toHexString(SecureRandom.getSeed(16));
                    break;

                case "B":
                case "b":
                    return;

                default: {
                    newPassword = input;

                    List<Rule> ruleList = new ArrayList<>();
                    ruleList.add(new LengthRule(8, 32));
                    ruleList.add(new WhitespaceRule());
                    ruleList.add(new DigitCharacterRule());
                    ruleList.add(new AlphabeticalCharacterRule());
                    ruleList.add(new AlphabeticalSequenceRule());
                    ruleList.add(new QwertySequenceRule());
                    //ruleList.add(new NonAlphanumericCharacterRule());

                    PasswordValidator validator = new PasswordValidator(ruleList);
                    PasswordData passwordData = new PasswordData(new Password(newPassword));
                    RuleResult result = validator.validate(passwordData);

                    if(result.isValid() == false) {
                        errorMsg = new StringBuilder();
                        for (String msg : validator.getMessages(result)) {
                            errorMsg.append(msg).append("\n");
                        }
                        continue;
                    }
                }
            }

            rpcProp.put(RPCServerManager.KEY_PASSWORD, newPassword);
            storeRpcConfig();
        }
    }

    private void changeMaxConnection() throws IOException {
        String errorMsg = "";
        while(true) {
            clearConsole();

            if(errorMsg.isEmpty() == false) {
                ConsoleUtil.printlnRed(errorMsg);
                ConsoleUtil.printlnRed("");
                errorMsg = "";
            }

            System.out.println(String.format(StringUtils.rightPad("RPC Max Connection [%s] ", 30, "="), ConsoleUtil.colorPurple(rpcProp.getProperty(RPCServerManager.KEY_MAX_CONNECTION))));
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Please enter the max connections (0 = No limit)");
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Set default", "10");
            printSettingRow("B", "Done", "");

            String input = ConsoleUtil.readLine(">> ");
            String newMaxConn;
            switch (input) {
                case "A":
                case "a":
                    newMaxConn = "10";
                    break;

                case "B":
                case "b":
                    return;

                default: {
                    try {
                        int maxConn = Integer.parseInt(input);

                        if(maxConn < 0 || maxConn > 65535) {
                            ConsoleUtil.printlnRed("Please enter the correct number.");
                            continue;
                        }

                        newMaxConn = String.valueOf(maxConn);
                    } catch (NumberFormatException e) {
                        errorMsg = "Please enter the correct number.";
                        continue;
                    }
                }
            }

            rpcProp.put(RPCServerManager.KEY_MAX_CONNECTION, newMaxConn);
            storeRpcConfig();
        }
    }

    private void changeAllowIp() throws IOException {
        String errorMsg = "";

        externalLoop:
        while(true) {
            clearConsole();

            if(errorMsg.isEmpty() == false) {
                ConsoleUtil.printlnRed(errorMsg);
                ConsoleUtil.printlnRed("");
                errorMsg = "";
            }

            System.out.println(String.format(StringUtils.rightPad("RPC Allowed IP [%s] ", 30, "="), ConsoleUtil.colorPurple(rpcProp.getProperty(RPCServerManager.KEY_ALLOW_IP).replaceAll(",", ", "))));
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Please enter the allowed IP");
            ConsoleUtil.printlnGreen("Separate with ,(comma)");
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Local", "127.0.0.1");
            printSettingRow("B", "All", "0.0.0.0");
            printSettingRow("C", "Done", "");

            String input = ConsoleUtil.readLine(">> ");
            input = input.replace(" ", "");
            String newList;
            switch (input) {
                case "A":
                case "a":
                    newList = "127.0.0.1";
                    break;

                case "B":
                case "b":
                    newList = "0.0.0.0";
                    break;

                case "C":
                case "c":
                    return;

                default: {
                    String[] ips = input.split(",");
                    for(String ip : ips) {
                        if(!InetAddressUtils.isIPv4Address(ip)) {
                            errorMsg = "Please enter the IP address that matches the format.";
                            continue externalLoop;
                        }
                    }

                    newList = input;
                }
            }

            rpcProp.put(RPCServerManager.KEY_ALLOW_IP, newList);
            storeRpcConfig();
        }
    }

    private void changeMaxPeers() throws IOException {
        String errorMsg = "";
        while(true) {
            clearConsole();

            if(errorMsg.isEmpty() == false) {
                ConsoleUtil.printlnRed(errorMsg);
                ConsoleUtil.printlnRed("");
                errorMsg = "";
            }

            System.out.println(String.format(StringUtils.rightPad("Max Peers [%s] ", 30, "="), ConsoleUtil.colorPurple(rpcProp.getProperty(RPCServerManager.KEY_MAX_PEERS))));
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Please enter the maximum number of peers (5 ~ 100)");
            ConsoleUtil.printlnGreen("If more peers attempt to connect, they will not accept the connection");
            ConsoleUtil.printlnGreen("");
            printSettingRow("A", "Set default", "30");
            printSettingRow("B", "Done", "");

            String input = ConsoleUtil.readLine(">> ");
            String newMaxConn;
            switch (input) {
                case "A":
                case "a":
                    newMaxConn = "30";
                    break;

                case "B":
                case "b":
                    return;

                default: {
                    try {
                        int maxConn = Integer.parseInt(input);

                        if(maxConn < 5 || maxConn > 1000) {
                            ConsoleUtil.printlnRed("Please enter the correct number.");
                            continue;
                        }

                        newMaxConn = String.valueOf(maxConn);
                    } catch (NumberFormatException e) {
                        errorMsg = "Please enter the correct number.";
                        continue;
                    }
                }
            }

            rpcProp.put(RPCServerManager.KEY_MAX_PEERS, newMaxConn);
            storeRpcConfig();
        }
    }

    private void storeRpcConfig() throws IOException {
        OutputStream daemonOutput = new FileOutputStream(dirRpc);
        rpcProp.store(daemonOutput, null);
        daemonOutput.close();
    }


    private boolean isNeedUpdate() throws IOException {
        if(isFailedLoadWalletInfo) {
            return false;
        }

        String jsonUrl = "https://storage.googleapis.com/apis-mn-images/pcwallet/pcwallet.json";

        try (InputStream is = new URL(jsonUrl).openStream()) {

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();

            JSONParser parser = new JSONParser();
            JSONObject jo = (JSONObject) parser.parse(jsonText);
            String newestVer = jo.get("versionNewest").toString();

            updateJar = jo.get("coreJar").toString();

            versionLatest = newestVer;
            versionCurrent = config.projectVersion();

            if (config.projectVersion().equals(newestVer)) {
                // 업데이트 되어있음
                return false;
            }
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void updateCore() {
        if(isFailedLoadWalletInfo) {
            return;
        }

        try {
            File jarFile = new File(CLIStart.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            // 파일을 백업한다.
            ConsoleUtil.printlnCyan("Backup is running ...");
            File jarFileBak = new File(jarFile.getPath() + "." + versionCurrent);

            if(jarFileBak.exists()) {
                jarFileBak.delete();
            }
            jarFile.renameTo(jarFileBak);
            jarFile.delete();

            // 다운로드

            if(!updateJar.isEmpty()) {
                ConsoleUtil.printlnCyan("Start downloading a new version of APIS Core.");
                try (BufferedInputStream in = new BufferedInputStream(new URL(updateJar).openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(jarFile.getAbsolutePath())) {
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    // handle exception
                    e.printStackTrace();
                }

                System.exit(0);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
