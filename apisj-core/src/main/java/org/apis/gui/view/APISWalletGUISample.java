package org.apis.gui.view;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.Scene;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import org.apis.crypto.ECKey;
import org.apis.keystore.KeyStoreUtil;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class APISWalletGUISample {
    private JFrame mainFrame;
    private JFXPanel fxPanel;
    private Container contentPane;
    private WebView webView;
    private WebEngine webEngine;
    private Scene scene;
    private FileChooser jfc;

    public void mainView() {
        mainFrame = new JFrame();
//        jfc = new FileChooser(mainFrame);
        contentPane = mainFrame.getContentPane();
        contentPane.setLayout(null);
        contentPane.setBackground(Color.decode("#FFFFFF"));
        mainFrame.setTitle("APIS CORE");
        mainFrame.setSize(966,569);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        comInit();
        mainFrame.setVisible(true);
    }

    public void comInit(){
        fxPanel = new JFXPanel();
        fxPanel.setBounds(0,0,960,540);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // WebView setting
                webView = new WebView();
                webEngine = webView.getEngine();
                fxPanel.setScene(new Scene(webView));

                // Path setup
                URL mainURL = this.getClass().getResource("/webView/index.html");
                // Load URL from setting Path
                webEngine.load(mainURL.toExternalForm());

                webEngine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                            if(newState == Worker.State.SUCCEEDED) {
                                // Create JSObject for communication with Javascript
                                JSObject window = (JSObject)webEngine.executeScript("window");
                                window.setMember("app",new JavaApplication());
                            }
                        }
                    }
                );
            }
        });

        contentPane.add(fxPanel);
    }

    public class JavaApplication {
        private byte[] privateKey = new byte[0], decryptedKey = new byte[0];
        private int download_keyStore_flag = 0, download_keyStore_index = 0;
        private String address = "", keystore_file = "", fileName = "", allText = "";
        private String keystore_address = "", download_keyStore_path = "";
        private String downloadFilePath = "C:\\APIS_keystore";
        private File uploadFile, downloadFile = new File(downloadFilePath);
        private FileChooser jfc;

        // Generate Keystore file
        public void generateKeystore(String password) {
            try {
                privateKey = SecureRandom.getInstanceStrong().generateSeed(32);
                address = ECKey.fromPrivate(privateKey).toString();
                keystore_file = KeyStoreUtil.getEncryptKeyStore(privateKey, password);
                // For compare
                System.out.println(Hex.toHexString(privateKey));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            TimeZone time;
            Date date = new Date();
            DateFormat df = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH-mm-ss.SSSZ"
            );

            time = TimeZone.getTimeZone("UTC");
            df.setTimeZone(time);

            keystore_address = keystore_file.substring(keystore_file.indexOf("address") + 10, keystore_file.indexOf("crypto") - 3);

            download_keyStore_path = downloadFilePath+"\\UTC--" + df.format(date) + "--" + keystore_address;
        }

        // Download Keystore file
        public void downloadKeystore() {
            if (download_keyStore_flag == 0) {
                downloadFile.mkdirs();

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(download_keyStore_path))) {
                    bw.write(keystore_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                download_keyStore_flag = 1;

            } else {
                downloadFile.mkdirs();
                download_keyStore_index++;

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(download_keyStore_path + "(" + download_keyStore_index + ")"))) {
                    bw.write(keystore_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Delete Keystore file
        public void deleteKeystore() {
            if(download_keyStore_flag == 1) {
                String fileList[] = downloadFile.list();

                for(int i=0; i<fileList.length; i++) {
                    if(fileList[i].contains(keystore_address)) {
                        File deleteFile = new File(downloadFilePath+"\\"+fileList[i]);
                        deleteFile.delete();
                    }
                }
            }
        }

        public void resetValues() {
            download_keyStore_flag = 0;
            download_keyStore_index = 0;
        }

        public String getPrivateKey() {
            return Hex.toHexString(privateKey);
        }

        // File Read
        public String fileRead() {
            String result = "FileException";
            fileName = "";
            jfc = new FileChooser(mainFrame);
            jfc.setMultiSelectionEnabled(false);

            if(jfc.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                String filePath = jfc.getSelectedFile().toString();
                String splitFilePath[] = filePath.split("\\\\");
                fileName = splitFilePath[splitFilePath.length-1];
                uploadFile = new File(filePath);

                if(uploadFile.exists()) {
                    long l = uploadFile.length();
                    if(l > 10240) {
                        result = "IncorrectFileForm";
                        return result;
                    }
                }

                try (BufferedReader br = new BufferedReader(new FileReader(uploadFile))) {
                    String sCurrentLine;
                    allText = "";

                    while((sCurrentLine = br.readLine()) != null) {
                        sCurrentLine = sCurrentLine.replaceAll(" ","");
                        allText += sCurrentLine.trim();
                    }

                    if((allText.contains("\"version\"") &&
                        allText.contains("\"id\"") &&
                        allText.contains("\"address\"") &&
                        allText.contains("\"crypto\"") &&
                        allText.contains("\"ciphertext\"") &&
                        allText.contains("\"cipherparams\"") &&
                        allText.contains("\"iv\"") &&
                        allText.contains("\"cipher\"") &&
                        allText.contains("\"kdf\"") &&
                        allText.contains("\"kdfparams\"") &&
                        allText.contains("\"dklen\"") &&
                        allText.contains("\"salt\"") &&
                        allText.contains("\"n\"") &&
                        allText.contains("\"r\"") &&
                        allText.contains("\"p\"") &&
                        allText.contains("\"mac\""))
                       &&
                       (allText.substring(0,1).equals("{") &&
                        allText.substring(allText.length()-3,allText.length()).equals("\"}}"))) {
                            result = "CorrectFileForm";
                    } else {
                            result = "IncorrectFileForm";
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            } else {
                return result;
            }
        }

        public boolean matchPassword(String password) {
            boolean result = false;
            String loadedKeystore = allText;

            try {
                decryptedKey = KeyStoreUtil.decryptPrivateKey(loadedKeystore,password);
                // For compare
                System.out.println(Hex.toHexString(decryptedKey));

                if(Hex.toHexString(privateKey).equals(Hex.toHexString(decryptedKey))) {
                    result = true;
                }
            } catch (Exception e) {
                return result;
            }

            return result;
        }

        public String getFileName() {
            return fileName;
        }

        public void errorPopup() {
            JOptionPane.showMessageDialog(mainFrame, "File load error!");
        }

        public void print(String str) {
            System.out.println(str);
        }
    }

    // Set JFileChooser location
    static class FileChooser extends JFileChooser {
        private JFrame mainFrame;

        private FileChooser(JFrame mainFrame) {
            this.mainFrame = mainFrame;
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
