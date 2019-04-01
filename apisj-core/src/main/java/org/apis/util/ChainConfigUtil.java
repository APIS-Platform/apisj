package org.apis.util;

import org.apis.config.SystemProperties;

import java.io.*;

public class ChainConfigUtil {

    public static final int CHAIN_MAINNET = 1;
    public static final int CHAIN_TESTNET = 2;
    public static final int CHAIN_PREBALANCE = 7;

    public static void changeChain(int chain) {

        SystemProperties config = SystemProperties.getDefault();
        if(config == null) { return; }

        File keystore = new File(config.configDir());
        if(!keystore.exists()) {
            if(!keystore.mkdirs()) {
                return;
            }
        }

        String configFileName = config.configDir() + "/network.conf";

        File configFile = new File(configFileName);
        if(configFile.isFile()) {
            if(!configFile.delete()) {
                return;
            }
        }

        switch (chain) {

            // 그 외에는 network.conf 파일을 생성한다
            case CHAIN_PREBALANCE: {
                File newConfigFile = getResourceAsFile("apis_prebalance.conf");
                copyFile(newConfigFile, configFileName);
                break;
            }

            case CHAIN_TESTNET: {
                File newConfigFile = getResourceAsFile("apis_test.conf");
                copyFile(newConfigFile, configFileName);
                break;
            }

            // 메인넷의 경우, network.conf 파일을 삭제한다.
            case CHAIN_MAINNET:
            default:
                // 파일을 따로 생성할 건 없다
                break;
        }
    }

    private static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copyFile(File original, String target) {
        try {
            FileInputStream fis = new FileInputStream(original);
            FileOutputStream fos = new FileOutputStream(target);

            int data;
            while((data = fis.read()) != -1) {
                fos.write(data);
            }
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}