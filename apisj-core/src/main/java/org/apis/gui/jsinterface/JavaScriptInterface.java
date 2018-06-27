package org.apis.gui.jsinterface;

import com.google.gson.Gson;
import com.google.zxing.WriterException;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apis.gui.common.APISPrintDialog;
import org.apis.gui.common.OSInfo;
import org.apis.gui.common.QRCodeGenerator;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.view.APISWalletGUI;
import org.apis.keystore.KeyStoreData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.*;
import java.math.BigInteger;

public class JavaScriptInterface {
    private APISWalletGUI apisWallet;
    private APISPrintDialog dialog;
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
        if(apisWallet.getCreateWalletCompleteFlag() == 0) {
            deleteKeystore();
        }
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
        AppManager.getInstance().keystoreFileReadAll();

        return  new Gson().toJson(AppManager.getInstance().getKeystoreExpList());
    }
    public String getKeyStoreTotalBalance(){
        return AppManager.getInstance().getTotalBalance();
    }
    public String getKeyStoreTotalMineral(){
        return AppManager.getInstance().getTotalMineral();
    }


    /* ==============================================
     *  Intro Class
     * ============================================== */
    public void createKeystore(String wPk, String wName, String wPasswd){
        //wPk = "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec";
        //address = "31e2e1ed11951c7091dfba62cd4b7145e947219c"
        KeyStoreManager.getInstance().createKeystoreJsonData(wPk, wName, wPasswd);
    }
    public void downloadKeystore() { KeyStoreManager.getInstance().downloadKeystore(); }
    public void deleteKeystore(){ KeyStoreManager.getInstance().deleteKeystore(); }
    public void resetKeystore(){ KeyStoreManager.getInstance().resetKeystore(); }
    public void createWalletComplete() { this.apisWallet.createWalletComplete(); }
    public void setVisibleDragAndDropPanel(boolean visible){ this.apisWallet.setVisibleDragAndDropPanel(visible); }

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

                result = "CorrectFileForm";
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

    public String openDirectoryReader(){
        System.out.println("openDirectoryReader");
        String result = "";
        File selectFile = null;

        if(OSInfo.getOs() == OSInfo.OS.WINDOWS){
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(KeyStoreManager.getInstance().getDefaultKeystoreDirectory());
            selectFile = directoryChooser.showDialog(null);
        }else if(OSInfo.getOs() == OSInfo.OS.MAC){
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(KeyStoreManager.getInstance().getDefaultKeystoreDirectory());
            selectFile = directoryChooser.showDialog(null);
        }

        if(selectFile != null){
            result = selectFile.getPath();
            KeyStoreManager.getInstance().downloadKeystore(result);
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


    public void showPrintFrameForWallet() {
        byte[] address = this.getWalletAddr().getBytes();
        byte[] privateKey = this.getPrivateKey().getBytes();

        String addrBase64 = generateQRCodeImage(address);
        String pkBase64 = generateQRCodeImage(privateKey);

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                if(dialog == null) {
                    dialog = new APISPrintDialog();
                }
                dialog.init(address, privateKey, addrBase64, pkBase64);
                //dialog.initHtml();
                dialog.setModal(true);
                dialog.setVisible(true);
            }
        });
        th.start();
    }

    private String generateQRCodeImage(byte[] message){
        String base64 = null;
        try {
            base64 = QRCodeGenerator.generateQRCodeImage(message, 200, 200);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return base64;
    }


    /* ==============================================
     *  Ethereum Method
     * ============================================== */

    public void ethereumCreateTransactions(String addr, String sGasPrice, String sGasUnit, String sToAddress, String sValue, String sValueUnit, String sPasswd){

        BigInteger gas = new BigInteger(sGasPrice);
        gas = gas.multiply(new BigInteger(sGasUnit));

        BigInteger balance = new BigInteger(sValue);
        balance = balance.multiply(new BigInteger(sValueUnit));

        if(addr!= null && addr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && sValue != null && sValue.length() > 0){
            AppManager.getInstance().ethereumCreateTransactions(addr, gas.toString(), "200000", sToAddress, balance.toString(), sPasswd);
            sPasswd = null;
        }else{
        }

    }
    public void ethereumSendTransactions(){
        AppManager.getInstance().ethereumSendTransactions();
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
