package org.apis.cli;

import com.google.common.base.Strings;
import edu.vt.middleware.password.*;
import org.apache.http.conn.util.InetAddressUtils;
import org.apis.config.SystemProperties;
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreManager;
import org.apis.keystore.KeyStoreUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;

public class CLIStart {
    private SystemProperties config;
    private Properties daemonProp;

    public CLIStart() {
        config = SystemProperties.getDefault();

        daemonProp = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        try {
            InputStream input = new FileInputStream("config/daemon.properties");
            daemonProp.load(input);
        } catch (IOException e) {
            daemonProp.setProperty("coinbase", ""); //
            daemonProp.setProperty("masternode", "");
            daemonProp.setProperty("recipient", "");
            daemonProp.setProperty("autoStart", "false");

            OutputStream output = null;
            try {
                output = new FileOutputStream("config/daemon.properties");
                daemonProp.store(output, null);
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    public void startKeystoreCheck() throws IOException {
        if(daemonProp.getProperty("autoStart").equals("true")) {
            String coinbase = daemonProp.getProperty("coinbase");
            if(coinbase != null && !coinbase.isEmpty()) {
                config.setCoinbasePrivateKey(Hex.decode(coinbase));
            }

            String masternode = daemonProp.getProperty("masternode");
            if(masternode != null && !masternode.isEmpty()) {
                config.setMasternodePrivateKey(Hex.decode(masternode));
            }

            String recipient = daemonProp.getProperty("recipient");
            if(recipient != null && !recipient.isEmpty()) {
                config.setMasternodeRecipient(Hex.decode(recipient));
            }
            return;
        }

        List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

        ConsoleUtil.printlnGreen("You too, can get rewards through APIS Block mining.");
        ConsoleUtil.printlnGreen("You should input Private key of a miner to start mining.");
        ConsoleUtil.printlnGreen("The chance of getting reward goes higher with the registered miner's balance.");
        ConsoleUtil.printlnGreen("--");

        if(keyStoreDataList.size() > 0) {
            while(true) {
                keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

                if(keyStoreDataList.size() == 1) {
                    ConsoleUtil.printlnGreen("%d locked Private key is found.", keyStoreDataList.size());
                } else {
                    ConsoleUtil.printlnGreen("%d locked Private key are found.", keyStoreDataList.size());
                }
                ConsoleUtil.printlnGreen("--");
                ConsoleUtil.printlnCyan("1. Generate a new Private key");
                ConsoleUtil.printlnCyan("2. Import from locked Private key file");
                ConsoleUtil.printlnCyan("3. Deactivate mining function");
                ConsoleUtil.printlnCyan("4. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                        continue;
                    }
                    case 2: {
                        ConsoleUtil.printlnGreen("Which address of Private key would you import?");

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
                            daemonProp.setProperty("coinbase", ByteUtil.toHexString(privateKey));
                        } catch (Exception e) {
                            ConsoleUtil.printlnRed("The password you've entered is incorrect.\n");
                            continue;
                        }

                        break;
                    }
                    case 3:
                        // 채굴을 사용하지 않으므로 빠져나간다.
                        daemonProp.setProperty("coinbase", "");
                        break;
                    case 4:
                        ConsoleUtil.printlnGreen("Bye");
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
                ConsoleUtil.printlnGreen("No Private key is found.");
                ConsoleUtil.printlnGreen("--");
                ConsoleUtil.printlnCyan("1. Generate a new Private key");
                ConsoleUtil.printlnCyan("2. Input your Private key");
                ConsoleUtil.printlnCyan("3. Deactivate mining function");
                ConsoleUtil.printlnCyan("4. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        if(!KeyStoreManager.getInstance().createPrivateKeyCLI(null)) {
                            continue;
                        }
                        break;
                    }
                    case 2: {
                        ConsoleUtil.printlnGreen("Please input your Private key.");
                        String privateHex = ConsoleUtil.readLine(">> ");
                        byte[] privateKey = ByteUtil.hexStringToBytes(privateHex);

                        if(!KeyStoreManager.getInstance().createPrivateKeyCLI(privateKey)) {
                            continue;
                        } else {
                            daemonProp.setProperty("coinbase", privateHex);
                        }
                        break;
                    }
                    case 3:
                        daemonProp.setProperty("coinbase", "");
                        break;
                    case 4:
                        System.exit(0);
                        break;
                    default:
                        System.out.println();
                        continue;
                }
                break;
            }
        }



        List<KeyStoreData> keyStoreExceptMiner = KeyStoreManager.getInstance().loadKeyStoreFiles();
        keyStoreExceptMiner.removeIf(data -> FastByteComparisons.equal(Hex.decode(data.address), config.getMinerCoinbase()));   // remove miner address
        int sizeOfKeystoreExceptMiner = keyStoreExceptMiner.size();

        if(sizeOfKeystoreExceptMiner > 0) {
            // 마스터노드 설정을 확인한다.
            ConsoleUtil.printlnGreen("");
            ConsoleUtil.printlnGreen("Also, you can activate the Masternode.");
            ConsoleUtil.printlnGreen("You should input Private key of a masternode to staking.");
            ConsoleUtil.printlnGreen("The balance of the Masternode must be exactly 50,000, 200,000, and 500,000 APIS.");
            ConsoleUtil.printlnGreen("--");

            while (true) {
                if (sizeOfKeystoreExceptMiner == 1) {
                    ConsoleUtil.printlnGreen("%d locked Private key is found.", keyStoreExceptMiner.size());
                } else {
                    ConsoleUtil.printlnGreen("%d locked Private key are found.", keyStoreExceptMiner.size());
                }
                ConsoleUtil.printlnGreen("--");
                ConsoleUtil.printlnCyan("1. Import from locked Private key file");
                ConsoleUtil.printlnCyan("2. Deactivate Masternode function");

                switch (readNumber(">> ")) {
                    case 1: {
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
                            byte[] privateKey = KeyStoreUtil.decryptPrivateKey(data.toString(), String.valueOf(password));
                            config.setMasternodePrivateKey(privateKey);
                            daemonProp.setProperty("masternode", ByteUtil.toHexString(privateKey));

                            while(true) {
                                ConsoleUtil.printlnGreen("Please enter the address to receive the Masternode's reward instead.");
                                String recipient = ConsoleUtil.readLine(">> ");

                                try {
                                    byte[] recipientAddr = Hex.decode(recipient);
                                    if(recipientAddr.length > 20 || recipientAddr.length < 4) {
                                        ConsoleUtil.printlnRed("The address you've entered is incorrect.");
                                        continue;
                                    }

                                    config.setMasternodeRecipient(recipientAddr);
                                    daemonProp.setProperty("recipient", recipient);
                                } catch (Exception e) {
                                    ConsoleUtil.printlnRed("The address you've entered is incorrect.");
                                    continue;
                                }
                                break;
                            }

                            ConsoleUtil.printlnBlue("The Masternode function has been activated for the following address : [%s]", data.address);
                            ConsoleUtil.printlnBlue("The recipient address is : [%s]", ByteUtil.toHexString(config.getMasternodeRecipient()));
                            ConsoleUtil.printlnBlue("The Masternode can only work if the balance is exactly 50,000, 200,000, or 500,000 APIS.");
                            ConsoleUtil.readLine("Press [Enter] to next");
                            ConsoleUtil.printlnBlue("\n\n");
                        } catch (Exception e) {
                            ConsoleUtil.printlnRed("The password you've entered is incorrect.\n");
                            continue;
                        }
                        break;
                    }
                    case 2:
                        // Deactivated Masternode
                        daemonProp.setProperty("masternode", "");
                        daemonProp.setProperty("recipient", "");
                        break;
                    default :
                        System.out.println();
                        continue;
                }
                break;
            }
        }
    }

    private static final int SELECT_SERVERCHECK_START_WITHOUTRPC = 0;
    private static final int SELECT_SERVERCHECK_START_WITHRPC = 1;
    private static final int SELECT_SERVERCHECK_CHANGE_PORT = 2;
    private static final int SELECT_SERVERCHECK_CHANGE_ID = 3;
    private static final int SELECT_SERVERCHECK_CHANGE_PASSWORD = 4;
    private static final int SELECT_SERVERCHECK_CHANGE_MAXCONNECTIONS = 5;
    private static final int SELECT_SERVERCHECK_CHANGE_ALLOWIP = 6;

    public void startRpcServerCheck() throws IOException {
        if(daemonProp.getProperty("autoStart").equals("true")) {
            return;
        }


        File keystore = new File("config");
        if(!keystore.exists()) {
            keystore.mkdirs();
        }

        ConsoleUtil.printlnBlue("You can interact with this program remotely through a Web socket-based RPC server.");
        ConsoleUtil.printlnBlue("The settings of the RPC server can be loaded from a file(rpc.properties) or set via interactive input.\n");
        Properties prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        try {
            InputStream input = new FileInputStream("config/rpc.properties");
            prop.load(input);
        } catch (IOException e) {
            prop.setProperty("port", String.valueOf(new Random().nextInt(10000) + 40000));  // TODO 리스닝 포트는 제외하도록 수정해야함
            prop.setProperty("id", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("password", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("max_connections", String.valueOf(1));
            prop.setProperty("allow_ip", "127.0.0.1");

            File config = new File("config");
            if(!config.exists()) {
                config.mkdirs();
            }
            OutputStream output = new FileOutputStream("config/rpc.properties");
            prop.store(output, null);
            output.close();
        }

        rpc_setting_loop:
        while(true) {
            ConsoleUtil.printlnBlue("\n\nThe current setting is as follows.\n");
            printProp("port", prop);
            printProp("id", prop);
            printProp("password", prop);
            printProp("max_connections", prop);
            printProp("allow_ip", prop);


            ConsoleUtil.printlnBlue("\nDo you want to change the settings?");
            System.out.println( SELECT_SERVERCHECK_START_WITHOUTRPC + ". Start without the RPC server");
            System.out.println( SELECT_SERVERCHECK_START_WITHRPC  + ". Start the RPC server with this setting");
            System.out.println( SELECT_SERVERCHECK_CHANGE_PORT + ". Change port");
            System.out.println( SELECT_SERVERCHECK_CHANGE_ID + ". Change id");
            System.out.println( SELECT_SERVERCHECK_CHANGE_PASSWORD + ". Change password");
            System.out.println( SELECT_SERVERCHECK_CHANGE_MAXCONNECTIONS + ". Change max_connections");
            System.out.println( SELECT_SERVERCHECK_CHANGE_ALLOWIP + ". Change allow_ip");

            switch (readNumber(">> ")) {

                case SELECT_SERVERCHECK_START_WITHOUTRPC:
                    prop.setProperty("use_rpc", String.valueOf(false));
                    break rpc_setting_loop;
                case SELECT_SERVERCHECK_START_WITHRPC:
                    prop.setProperty("use_rpc", String.valueOf(true));
                    break rpc_setting_loop;
                case SELECT_SERVERCHECK_CHANGE_PORT:
                    changePort(prop);
                    break;
                case SELECT_SERVERCHECK_CHANGE_ID:
                    changeId(prop);
                    break;
                case SELECT_SERVERCHECK_CHANGE_PASSWORD:
                    changePassword(prop);
                    break;
                case SELECT_SERVERCHECK_CHANGE_MAXCONNECTIONS:
                    changeMaxConnection(prop);
                    break;
                case SELECT_SERVERCHECK_CHANGE_ALLOWIP:
                    changeAllowIp(prop);
                    break;
            }
        }

        File config = new File("config");
        if(!config.exists()) {
            config.mkdirs();
        }
        OutputStream output = new FileOutputStream("config/rpc.properties");
        prop.store(output, null);
        output.close();

        daemonProp.setProperty("autoStart", "true");
        OutputStream daemonOutput = new FileOutputStream("config/daemon.properties");
        daemonProp.store(daemonOutput, null);
        daemonOutput.close();
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
        ConsoleUtil.printlnPurple(Strings.padEnd(key, 20, ' ') + ": " + prop.getProperty(key));
    }

    private void changePort(Properties prop) throws IOException {
        int port = readNumber("Please enter the port number : ");

        if(port < 0 || port > 65535) {
            ConsoleUtil.printlnRed("Please enter the correct port number.");
            return;
        }

        prop.setProperty("port", String.valueOf(port));
    }

    private void changeId(Properties prop) throws IOException {
        String id = ConsoleUtil.readLine("Please enter the ID : ");

        if(id.length() < 4) {
            ConsoleUtil.printlnRed("ID is too short. Please enter more than 3 characters.");
            return;
        }

        prop.setProperty("id", id);
    }

    private void changePassword(Properties prop) throws IOException {
        String password = ConsoleUtil.readLine("Please enter the Password : ");

        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(new LengthRule(8, 32));
        ruleList.add(new WhitespaceRule());
        ruleList.add(new DigitCharacterRule());
        ruleList.add(new NonAlphanumericCharacterRule());

        PasswordValidator validator = new PasswordValidator(ruleList);
        PasswordData passwordData = new PasswordData(new Password(password));
        RuleResult result = validator.validate(passwordData);

        if(result.isValid()) {
            prop.setProperty("password", password);
        } else {
            for(String msg : validator.getMessages(result)) {
                ConsoleUtil.printlnRed(msg);
            }
        }
    }

    private void changeMaxConnection(Properties prop) throws IOException {
        int max_connections = readNumber("Please enter the max connections (0 - No limit) : ");

        if(max_connections < 0) {
            ConsoleUtil.printlnRed("Please enter the correct number.");
            return;
        }

        prop.setProperty("max_connections", String.valueOf(max_connections));
    }

    private void changeAllowIp(Properties prop) throws IOException {
        String ipList = ConsoleUtil.readLine("Please enter the allow_ip (Separate with ,(comma)) : ");

        if(ipList.equals("0.0.0.0")) {
            prop.setProperty("allow_ip", ipList);
            return;
        }

        String[] ips = ipList.split(",");
        for(String ip : ips) {
            if(!InetAddressUtils.isIPv4Address(ip)) {
                ConsoleUtil.printlnRed("Please enter the IP address that matches the format.");
                return;
            }
        }

        prop.setProperty("allow_ip", ipList);
    }
}
