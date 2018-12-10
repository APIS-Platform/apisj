package org.apis.keystore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apis.config.SystemProperties;
import org.apis.crypto.ECKey;
import org.apis.gui.manager.AppManager;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;

public class KeyStoreManager {
    private static KeyStoreManager manager = null;

    public static KeyStoreManager getInstance() {
        if(manager == null) {
            manager = new KeyStoreManager();
        }
        return manager;
    }

    private SystemProperties config;

    private KeyStoreManager() {
        config = SystemProperties.getDefault();
    }

    public List<KeyStoreData> loadKeyStoreFiles() {
        File keystore = new File(config.keystoreDir());
        if(!keystore.exists()) {
            if(!keystore.mkdirs()) {
                return new ArrayList<>();
            }
        }

        File[] keyList = keystore.listFiles();
        List<KeyStoreData> keyStoreDataList = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        if(keyList != null) {
            for (File file : keyList) {
                if (file.isFile()) {
                    try {
                        String fileText = readFile(file);
                        KeyStoreData data = gson.fromJson(fileText, KeyStoreData.class);

                        if (data != null) {
                            if(data.alias == null || data.alias.isEmpty()) {
                                data.alias = AddressUtil.getShortAddress(data.address);
                            }
                            keyStoreDataList.add(data);
                        }
                    }catch(JsonSyntaxException ignored) {}
                }
            }
        }

        return keyStoreDataList;
    }

    private String readFile(File file) {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while(line != null) {
                sb.append(line).append(System.lineSeparator());
                line = br.readLine();
            }

            br.close();
            return sb.toString().replaceAll("Crypto","crypto");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean createPrivateKeyCLI(byte[] privateKey) throws IOException {
        if(privateKey == null) {
            privateKey = SecureRandom.getSeed(32);;
        }

        char[] password1 = ConsoleUtil.readPassword(ConsoleUtil.ANSI_GREEN + "Please input your password : " + ConsoleUtil.ANSI_RESET);
        char[] password2 = ConsoleUtil.readPassword(ConsoleUtil.ANSI_GREEN + "Please confirm your password : " + ConsoleUtil.ANSI_RESET);

        if (Arrays.equals(password1, password2)) {
            String alias = ConsoleUtil.readLine(ConsoleUtil.ANSI_GREEN  + "Please input alias : " + ConsoleUtil.ANSI_RESET);

            savePrivateKeyStore(privateKey, alias, password1);
            config.setCoinbasePrivateKey(privateKey);
            return true;
        } else {
            System.out.println("Passwords do not match.");
            return false;
        }
    }

    public KeyStoreData savePrivateKeyStore(byte[] privateKey, String alias, char[] password) {
        String keystoreStr = KeyStoreUtil.getEncryptKeyStore(privateKey, alias, String.valueOf(password));

        KeyStoreData data = new GsonBuilder().create().fromJson(keystoreStr, KeyStoreData.class);
        if(data == null) {
            return null;
        }

        // 기존 파일을 삭제한다.
        deleteKeystore(Hex.decode(data.address));

        // 파일을 저장한다.
        PrintWriter writer;
        try {
            writer = new PrintWriter(config.keystoreDir() + "/" + KeyStoreUtil.getFileName(data), "UTF-8");
            writer.print(keystoreStr);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return data;
        }

        return data;
    }

    public KeyStoreData savePrivateKeyStore(KeyStoreData keyStoreData){
        if(keyStoreData == null) {
            return null;
        }
        String keystoreStr =  keyStoreData.toString();
        // 파일을 저장한다.
        PrintWriter writer;
        try {
            writer = new PrintWriter(config.keystoreDir() + "/" + KeyStoreUtil.getFileName(keyStoreData), "UTF-8");
            writer.print(keystoreStr);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        return keyStoreData;
    }

    public KeyStoreData savePrivateKeyStore(String alias, char[] password){
        byte[] privateKey = SecureRandom.getSeed(32);
        return savePrivateKeyStore(privateKey, alias, password);
    }

    public byte[] createPrivateKey(String password) {
        byte[] privateKey = SecureRandom.getSeed(32);
        savePrivateKeyStore(privateKey, "", password.toCharArray());

        return privateKey;
    }

    public ECKey findKeyStoreFile(byte[] address, String password) throws NotSupportCipherException, InvalidPasswordException, KeystoreVersionException, NotSupportKdfException {
        List<KeyStoreData> keyDataList = loadKeyStoreFiles();
        KeyStoreData foundKey = null;
        for(KeyStoreData key : keyDataList) {
            if(key.address.contains(ByteUtil.toHexString(address))) {
                foundKey = key;
            }
        }

        if(foundKey == null) {
            return null;
        }

        byte[] privateKey = KeyStoreUtil.decryptPrivateKey(foundKey.toString(), password);
        return ECKey.fromPrivate(privateKey);
    }

    public static KeyStoreData checkKeystoreFile(File file){
        if(file == null) return null;
        if(!file.exists()) return null;

        long l = file.length();
        if(l > 10240) {
            return null;
        }

        try {
            String allText = AppManager.fileRead(file);

            if(allText.length() > 0) {
                KeyStoreData keyStoreData = new Gson().fromJson(allText, KeyStoreData.class);
                return keyStoreData;
            } else {
                return null;
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            return null;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean matchPassword(String keystoreJsonData, char[] password){
        boolean result = false;
        try {
            KeyStoreUtil.decryptPrivateKey(keystoreJsonData, String.valueOf(password));
            result = true;
        }catch (InvalidPasswordException e){
        }catch (Exception e) {
        }
        return result;
    }

    public static byte[] getPrivateKey(String keystoreJsonData, String password){
        byte[] decryptedKey = null;
        try {
            decryptedKey = KeyStoreUtil.decryptPrivateKey(keystoreJsonData, password);
        }catch (InvalidPasswordException e){
        }catch (Exception e) {
        }
        return decryptedKey;
    }

    public void deleteKeystore(byte[] address){
        if(address == null || ByteUtil.toHexString(address).length() < 40){
            return ;
        }

        File keystore = new File(config.keystoreDir());
        if(!keystore.exists()) {
            if(!keystore.mkdirs()) {
                return ;
            }
        }

        File[] keyList = keystore.listFiles();
        Gson gson = new GsonBuilder().create();

        // check delete file list
        List<File> deleteFiles = new ArrayList<>();
        if(keyList != null) {
            for (File file : keyList) {
                if (file.isFile()) {
                    try {
                        String fileText = readFile(file);
                        KeyStoreData data = gson.fromJson(fileText, KeyStoreData.class);

                        if (data != null) {
                            if(ByteUtil.toHexString(address).equals(data.address)) {
                                deleteFiles.add(file);
                            }
                        }
                    }catch(JsonSyntaxException ignored) {}
                }
            }
        }

        // delete file list
        for(int i=0; i<deleteFiles.size(); i++){
            File deleteFile = deleteFiles.get(i);
            deleteFile.delete();
        }
    }

    public void updateWalletAlias(String address, String alias) {
        List<KeyStoreData> fileList = loadKeyStoreFiles();
        KeyStoreData changeData = null;
        for(KeyStoreData data : fileList){
            if (data.address.equals(address)) {
                changeData = data;
                //파일삭제
                deleteKeystore(Hex.decode(address));
                break;
            }
        }

        if(changeData != null){
            changeData.alias = alias;

            String keystoreJsonData = changeData.toString();

            // 파일을 저장한다.
            PrintWriter writer;
            try {
                writer = new PrintWriter(config.keystoreDir() + "/" + KeyStoreUtil.getFileName(changeData), "UTF-8");
                writer.print(keystoreJsonData);
                writer.close();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean updateWalletPassword(String address, char[] currentPassword, char[] newPassword) {
        List<KeyStoreData> fileList = loadKeyStoreFiles();
        KeyStoreData changeData = null;
        for(KeyStoreData data : fileList){
            if (data.address.equals(address)) {
                changeData = data;
                //파일삭제
                deleteKeystore(Hex.decode(address));
                break;
            }
        }

        if(changeData != null){
            try {
                byte[] privateKey = KeyStoreUtil.decryptPrivateKey(changeData.toString(), String.valueOf(currentPassword));
                System.out.println("privateKey : "+ByteUtil.toHexString(privateKey));
                savePrivateKeyStore(privateKey, changeData.alias, newPassword);

                return true;
            } catch (KeystoreVersionException e) {
            } catch (NotSupportKdfException e) {
            } catch (NotSupportCipherException e) {
            } catch (InvalidPasswordException e) {
            }
        }

        return false;
    }
}
