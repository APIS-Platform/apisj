package org.apis.gui.manager;

import com.google.gson.Gson;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apis.crypto.ECKey;
import org.apis.gui.common.OSInfo;
import org.apis.keystore.InvalidPasswordException;
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class KeyStoreManager {
    /* ==============================================
     *  KeyStoreManager Field : public
     * ============================================== */
    public static final String defaultKeystorePath = System.getProperty("user.dir") + "/keystore";


    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private byte[] privateKey = new byte[0];
    private String address = "";
    private String walletAddress = "";
    private String keystoreJsonData = "";
    private String keystoreFullPath = "";
    private String keystoreName = "";
    private KeyStoreData keystoreJsonObject = null; //keystoreJsonData to jsonObject
    private File keystoreFile = null;


    /* ==============================================
     *  KeyStoreManager Singleton
     * ============================================== */
    private KeyStoreManager () {}

    private static class Singleton {
        private static final KeyStoreManager instance = new KeyStoreManager();
    }
    public static KeyStoreManager getInstance () {
        return Singleton.instance;
    }


    /* ==============================================
     *  KeyStoreManager Static Method
     * ============================================== */
    public static String openDirectoryReader(){
        String result = null;
        File selectFile = null;

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(KeyStoreManager.getInstance().getDefaultKeystoreDirectory());
        selectFile = directoryChooser.showDialog(AppManager.getInstance().guiFx.getPrimaryStage());

        if(selectFile != null){
            result = selectFile.getPath();
            KeyStoreManager.getInstance().downloadKeystore(result);
        }

        return result;
    }
    // File Read
    public static String openFileReader(){
        String result = "FileException";
        File selectFile = null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(KeyStoreManager.getInstance().getDefaultKeystoreDirectory());
        selectFile = fileChooser.showOpenDialog(AppManager.getInstance().guiFx.getPrimaryStage());

        if(selectFile != null){
            String filePath = selectFile.getPath();
            String fileName = selectFile.getName();

            KeyStoreManager.getInstance().setKeystoreFile(selectFile);

            if(selectFile.exists()) {
                long l = selectFile.length();
                if(l > 10240) {
                    result = "IncorrectFileForm";
                    return result;
                }
            }

            try {
                String allText = AppManager.fileRead(selectFile);
                KeyStoreData keyStoreData = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreData.class);
                KeyStoreManager.getInstance().setKeystoreFullpath(KeyStoreManager.getInstance().getDefaultKeystoreDirectory()+"/"+selectFile.getName());
                KeyStoreManager.getInstance().setKeystoreJsonData(allText.toString().toLowerCase());
                KeyStoreManager.getInstance().setKeystoreJsonObject(keyStoreData);

                result = fileName;
            } catch (com.google.gson.JsonSyntaxException e) {
                e.printStackTrace();
                result = "IncorrectFileForm";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }



    /* ==============================================
     *  KeyStoreManager Method
     * ============================================== */
    public File getDefaultKeystoreDirectory(){
        File keystoreDir = new File(System.getProperty("user.dir"), "keystore");
        if (! keystoreDir.exists()) {
            //create directory
            keystoreDir.mkdirs();
        }
        return keystoreDir;
    }
    public void createKeystoreJsonData(String privateKey, String alias, String password) {
        try {
            if(privateKey == null || "".equals(privateKey)){
                this.privateKey = SecureRandom.getInstanceStrong().generateSeed(32);
            }else {
                this.setPrivateKey(privateKey);
            }


            this.address = ECKey.fromPrivate(this.privateKey).toString();
            this.keystoreJsonData = KeyStoreUtil.getEncryptKeyStore(this.privateKey, alias, password);


            keystoreJsonObject = new Gson().fromJson(this.keystoreJsonData.toLowerCase(), KeyStoreData.class);



            String downloadFilePath = this.getDefaultKeystoreDirectory().getPath();

            TimeZone time;
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ");

            time = TimeZone.getTimeZone("UTC");
            df.setTimeZone(time);

            this.walletAddress =  this.keystoreJsonObject.address;
            this.keystoreName = "/UTC--" + df.format(date) + "--" + this.walletAddress;
            this.keystoreFullPath = downloadFilePath + this.keystoreName;

            downloadKeystore();

            privateKey = null;
            password = null;
        } catch (NoSuchAlgorithmException e) {
            privateKey = null;
            password = null;
            e.printStackTrace();
        } catch (Exception e) {
            privateKey = null;
            password = null;
            e.printStackTrace();
        }
    }
    public void createKeyStoreFileLoad(File openFile) {
        if(openFile != null) {
            String fileName = openFile.getName();
            String absolutePath = openFile.getAbsolutePath();

            try (BufferedReader br = new BufferedReader(new FileReader(openFile))) {
                String sCurrentLine;
                String allText = "";

                while((sCurrentLine = br.readLine()) != null) {
                    sCurrentLine = sCurrentLine.replaceAll(" ","");
                    allText += sCurrentLine.trim();
                }

                KeyStoreData keyStoreData = new Gson().fromJson(allText.toLowerCase(), KeyStoreData.class);
                keystoreJsonObject = keyStoreData;
                this.keystoreJsonData = allText.toLowerCase();
                this.keystoreFullPath = absolutePath;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void downloadKeystore(){
        try{

            String keystoreFullPath = this.keystoreFullPath;
            FileWriter fileWriter = new FileWriter(keystoreFullPath);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(this.keystoreJsonData);
            bw.close();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadKeystore(String path){
        try{

            String keystoreFullPath = path + "/" + this.keystoreName;

            FileWriter fileWriter = new FileWriter(keystoreFullPath);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(this.keystoreJsonData);
            System.out.println(this.keystoreJsonData);
            bw.close();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Delete Keystore file
    public void deleteKeystore() {

        String fileList[] = this.getDefaultKeystoreDirectory().list();

        String fileFullPath = "";
        for(int i=0; i<fileList.length; i++) {
            if(fileList[i].contains(this.walletAddress)) {
                fileFullPath = this.getDefaultKeystoreDirectory().getPath();
                File deleteFile = new File(fileFullPath+"\\"+fileList[i]);
                deleteFile.delete();
            }
        }
    }

    public boolean matchPassword(String password) {
        boolean result = false;
        byte[] decryptedKey = new byte[0];
        try {
            decryptedKey = KeyStoreUtil.decryptPrivateKey(this.keystoreJsonData, password);
            result = true;

        } catch (InvalidPasswordException e){
            password = null;
            return result;
        }catch (Exception e) {
            password = null;
            return result;
        }
        password = null;
        return result;
    }

    /* ==============================================
     *  KeyStoreManager Method : Getter
     * ============================================== */
    public String getPrivateKey() {
        return Hex.toHexString(this.privateKey);
    }
    public String getWalletAddress() {return this.walletAddress; }
    public String getKeystoreJsonData(){ return ("".equals(this.keystoreJsonData)) ? null : this.keystoreJsonData; }
    public String getKeystoreFullPath(){ return ("".equals(this.keystoreFullPath)) ? null : this.keystoreFullPath; }
    public String getKeystoreFileName(){ return this.keystoreFile.getName(); }
    public KeyStoreData getKeystoreJsonObject() { return this.keystoreJsonObject; }

    /* ==============================================
     *  KeyStoreManager Method : Setter
     * ============================================== */
    public String setKeystoreFile(File file){ this.keystoreFile = file; return this.keystoreFile.getName();};
    public void setPrivateKey(String hexPrivateKey){ this.privateKey = Hex.decode(hexPrivateKey); }
    public void setKeystoreJsonData(String keystoreJsonData){ this.keystoreJsonData = keystoreJsonData;}
    public void setKeystoreJsonObject(KeyStoreData keystoreData) { this.keystoreJsonObject = keystoreData; }
    public void setKeystoreFullpath(String fullPath) { this.keystoreFullPath = fullPath; }
}


