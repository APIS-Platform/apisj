package org.apis.gui.jsinterface;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import org.apis.gui.common.OSInfo;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.view.APISWalletGUI;
import org.apis.keystore.KeyStoreData;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class JavaScriptInterface {
    private APISWalletGUI apisWallet;
    public JavaScriptInterface(APISWalletGUI apisWallet){
        this.apisWallet = apisWallet;
    }


    /* ==============================================
     *  Common Class
     * ============================================== */
    public void windowMinimize() {
        apisWallet.getMainFrame().setState(Frame.ICONIFIED);
    }

    public void windowClose() {
        System.exit(0);
    }

    public int getKeystoreListSize(){
        return AppManager.getInstance().getKeystoreList().size();
    }

    public String getDefaultKeyStorePath(){
        String path = KeyStoreManager.getInstance().getDefaultKeystoreDirectory().getPath();
        if(path.length() > 0){
            return path;
        }else{
            return "";
        }
    }

    public String getKeyStoreDataListAllWithJson(){
        return  new Gson().toJson(AppManager.getInstance().getKeystoreList());
    }


    /* ==============================================
     *  Intro Class
     * ============================================== */
    public void createKeystore(String wPk, String wName, String wPasswd){
        //wPk = "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec";
        //address = "31e2e1ed11951c7091dfba62cd4b7145e947219c"
        KeyStoreManager.getInstance().createKeystoreJsonData(wPk, wName, wPasswd);
    }

    public void downloadKeystore() {
        KeyStoreManager.getInstance().downloadKeystore();
    }

    public void deleteKeystore(){
        KeyStoreManager.getInstance().deleteKeystore();
    }

    public void resetKeystore(){
        KeyStoreManager.getInstance().resetKeystore();
    }

    public void createWalletComplete() {
        this.apisWallet.createWalletComplete();
    }

    // File Read
    public String openFileReader(){
        String result = "FileException";
        File selectFile = null;

        if(OSInfo.getOs() == OSInfo.OS.WINDOWS){
            APISFileChooser jfc = new APISFileChooser(this.apisWallet.getMainFrame());
            if(jfc.showOpenDialog(this.apisWallet.getMainFrame().getContentPane()) == JFileChooser.APPROVE_OPTION) {
                selectFile = jfc.getSelectedFile();
            }
        }else if(OSInfo.getOs() == OSInfo.OS.MAC){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(KeyStoreManager.getInstance().getDefaultKeystoreDirectory());
            selectFile = fileChooser.showOpenDialog(null);
        }

        if(selectFile != null){
            String filePath = selectFile.getPath();
            String fileName = selectFile.getName();

            System.out.println("filePath : "+filePath);
            System.out.println("fileName : "+fileName);

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
                KeyStoreManager.getInstance().setKeystoreJsonData(allText.toString().toLowerCase());
                KeyStoreManager.getInstance().setKeystoreJsonObject(keyStoreData);
                KeyStoreManager.getInstance().setKeystoreFile(selectFile);

                result = "CorrectFileForm";
            } catch (com.google.gson.JsonSyntaxException e) {
                System.out.println("Json Syntax Error!");
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

    public boolean matchPassword(String password) {
        return KeyStoreManager.getInstance().matchPassword(password);
    }

    public String getPrivateKey(){
        return KeyStoreManager.getInstance().getPrivateKey();
    }
    public String getKeystore(){
        return KeyStoreManager.getInstance().getKeystoreJsonData();
    }
    public String getFileName(){
        return KeyStoreManager.getInstance().getKeystoreFileName();
    }

    public String getWalletName() {
        String walletName = "unknown wallet name";
        if(KeyStoreManager.getInstance().getKeystoreJsonObject() != null){
            walletName = KeyStoreManager.getInstance().getKeystoreJsonObject().alias;
        }
        return walletName;
    }

    public String getWalletAddr() {
        String walletAddr = "unknown wallet address";
        if(KeyStoreManager.getInstance().getKeystoreJsonObject() != null){
            walletAddr = KeyStoreManager.getInstance().getKeystoreJsonObject().address;
        }
        return walletAddr;
    }


    /* ==============================================
     *  Ethereum Method
     * ============================================== */

    public void ethereumCreateTransactions(String addr, String sGasPrice, String sGasLimit, String sToAddress, String sValue){

        if(addr!= null && addr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sGasLimit != null && sGasLimit.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && sValue != null && sValue.length() > 0){
            System.out.println("ethereumCreateTransactions Call!!");
            AppManager.getInstance().ethereumCreateTransactions(addr, sGasPrice, sGasLimit, sToAddress, sValue);
            System.out.println("ethereumCreateTransactions Success!!");
        }else{
            System.out.println("ethereumCreateTransactions Failed!!");
        }

    }
    public void ethereumSendTransactions(){
        System.out.println("ethereumSendTransactions Call!!");
        AppManager.getInstance().ethereumSendTransactions();
        System.out.println("ethereumSendTransactions Success!!");
    }


    /* ==============================================
     *  Swing Class
     * ============================================== */

    // Set JFileChooser location
    static class APISFileChooser extends JFileChooser {
        private JFrame mainFrame;

        private APISFileChooser(JFrame mainFrame) {
            this.mainFrame = mainFrame;
            this.setCurrentDirectory(KeyStoreManager.getInstance().getDefaultKeystoreDirectory());
        }

        protected JDialog createDialog(Component parent) throws HeadlessException {
            JDialog dlg = super.createDialog(parent);
            Point location = mainFrame.getLocationOnScreen();
            int x = location.x ;
            int y = location.y + 10;

            dlg.setLocation(x,y);

            return dlg;
        }
    }
}
