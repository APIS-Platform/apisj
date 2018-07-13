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

import java.io.*;
import java.security.SecureRandom;
import java.util.*;

public class CLIStart {
    private SystemProperties config;

    public CLIStart() {
        config = SystemProperties.getDefault();
    }


    public void startKeystoreCheck() throws IOException {
        List<KeyStoreData> keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

        if(keyStoreDataList.size() > 0) {
            while(true) {
                keyStoreDataList = KeyStoreManager.getInstance().loadKeyStoreFiles();

                ConsoleUtil.printlnBlue("APIS mining requires a miner's private key.\nThere are %d keystore files.", keyStoreDataList.size());
                System.out.println("1. Create a new private key");
                System.out.println("2. Load from keystore file");
                System.out.println("3. Turn off mining.");
                System.out.println("4. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        KeyStoreManager.getInstance().createPrivateKeyCLI(null);
                        continue;
                    }
                    case 2: {
                        ConsoleUtil.printlnBlue("Which address will you use as coinbase(miner)?");

                        for(int i = 0; i < keyStoreDataList.size(); i++) {
                            ConsoleUtil.printlnPurple(i + ". " + keyStoreDataList.get(i).address);
                        }

                        int addressIndex = readNumber(">> ");
                        if(addressIndex >= keyStoreDataList.size()) {
                            ConsoleUtil.printlnRed("Please enter the correct number.\n");
                            continue;
                        }

                        KeyStoreData data;
                        try {
                            data = keyStoreDataList.get(addressIndex);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            ConsoleUtil.printlnRed("Please enter the correct number..\n");
                            continue;
                        }
                        if(data == null) {
                            ConsoleUtil.printlnRed("The keystore data is empty.\n");
                            continue;
                        }

                        char[] password = ConsoleUtil.readPassword("Please enter the password for the keystore file\n>> ");
                        try {
                            byte[] privateKey = KeyStoreUtil.decryptPrivateKey(data.toString(), String.valueOf(password));
                            config.setCoinbasePrivateKey(privateKey);
                        } catch (Exception e) {
                            ConsoleUtil.printlnRed("You can not extract the private key with the password you entered.\n");
                            continue;
                        }

                        break;
                    }
                    case 3:
                        // 채굴을 사용하지 않으므로 빠져나간다.
                        break;
                    case 4:
                        ConsoleUtil.printlnBlue("Bye~");
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
                ConsoleUtil.printlnBlue("APIS mining requires a miner's private key.\nThere are no keystore file");
                System.out.println("1. Create a new private key");
                System.out.println("2. Enter the known private key");
                System.out.println("3. Turn off mining");
                System.out.println("4. Exit");

                switch (readNumber(">> ")) {
                    case 1: {
                        if(!KeyStoreManager.getInstance().createPrivateKeyCLI(null)) {
                            continue;
                        }
                        break;
                    }
                    case 2: {
                        ConsoleUtil.printlnBlue("Please input private key");
                        String privateHex = ConsoleUtil.readLine(">> ");
                        byte[] privateKey = ByteUtil.hexStringToBytes(privateHex);

                        if(!KeyStoreManager.getInstance().createPrivateKeyCLI(privateKey)) {
                            continue;
                        }
                        break;
                    }
                    case 3:
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
    }

    public void startRpcServerCheck() throws IOException {
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
            prop.setProperty("port", String.valueOf(new Random().nextInt(10000) + 40000));
            prop.setProperty("id", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("password", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("max_connections", String.valueOf(1));
            prop.setProperty("allow_ip", "127.0.0.1");

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
            System.out.println("0. Start the RPC server with this setting");
            System.out.println("1. Change port");
            System.out.println("2. Change id");
            System.out.println("3. Change password");
            System.out.println("4. Change max_connections");
            System.out.println("5. Change allow_ip");

            switch (readNumber(">> ")) {
                case 0:
                    break rpc_setting_loop;
                case 1:
                    changePort(prop);
                    break;
                case 2:
                    changeId(prop);
                    break;
                case 3:
                    changePassword(prop);
                    break;
                case 4:
                    changeMaxConnection(prop);
                    break;
                case 5:
                    changeAllowIp(prop);
                    break;
            }
        }

        OutputStream output = new FileOutputStream("config/rpc.properties");
        prop.store(output, null);
        output.close();


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
