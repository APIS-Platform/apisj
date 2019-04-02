package org.apis.gui.controller.setting;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SettingItemUpdateController implements Initializable {
    @FXML private Label contents, latestVerLabel, latestVer, versionChk, updateBtn;

    public static final boolean VERSION_UPDATED = true;
    public static final boolean VERSION_NOT_UPDATED = false;
    private boolean versionStatus = VERSION_NOT_UPDATED;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        updateBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(!versionStatus) {
////                    String jsonUrl = "https://gist.githubusercontent.com/Oxchild/c73c783b8054d9b85f7fdcdfbbc821b1/raw/android.json";
////                    String jsonUrl = "https://gist.githubusercontent.com/Oxchild/c73c783b8054d9b85f7fdcdfbbc821b1/raw";
//                    String url = "http://wagi.xyz:8000/lib.zip";
//                    BufferedInputStream in = null;
//                    FileOutputStream fout = null;
//                    try {
////                        in = new BufferedInputStream(new URL(jsonUrl).openStream());
//                        in = new BufferedInputStream(new URL(url).openStream());
//                        fout = new FileOutputStream("test_json.zip");
//
//                        final byte data[] = new byte[1024];
//                        int count;
//                        while((count = in.read(data, 0, 1024)) != -1) {
//                            fout.write(data, 0, count);
//                        }
//
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    } finally {
//                        try {
//                            if (in != null) {
//                                in.close();
//                            }
//                            if (fout != null) {
//                                fout.close();
//                            }
//                        } catch(IOException e2) {
//                            e2.printStackTrace();
//                        }
//                    }
                    // Unzip zip file
                    FileInputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    BufferedOutputStream bufferedOutputStream = null;
                    ZipInputStream zipInputStream = null;

                    try {
                        inputStream = new FileInputStream("test_json.zip");
                        zipInputStream = new ZipInputStream(inputStream);
                        ZipEntry zipEntry = null;

                        while((zipEntry = zipInputStream.getNextEntry()) != null) {
                            String fileName = zipEntry.getName();
                            File file = new File("test_json", fileName);
                            File folder = file.getParentFile();

                            if(!folder.exists()) {
                                folder.mkdirs();
                            }

                            outputStream = new FileOutputStream(file);
                            bufferedOutputStream = new BufferedOutputStream(outputStream);

                            int length = 0;
                            while((length = zipInputStream.read()) != -1) {
                                bufferedOutputStream.write(length);
                            }

                            zipInputStream.closeEntry();
                            outputStream.flush();
                            outputStream.close();
                        }
                        zipInputStream.close();
                    } catch(IOException e2) {

                    } finally {
                        try {
                            zipInputStream.closeEntry();
                            outputStream.flush();
                            outputStream.close();
                            zipInputStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }//if(!versionStatus)
            }
        });

        updateBtn.setOnMouseEntered(event -> {
            if(!isVersionStatus()) {
                StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.C910000);
            }
        });

        updateBtn.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isVersionStatus()) {
                    StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.Cb01e1e);
                }
            }
        });
    }

    private void languageSetting() {
        latestVerLabel.textProperty().bind(StringManager.getInstance().setting.latestVerLabel);
        updateBtn.textProperty().bind(StringManager.getInstance().setting.updateLabel);
    }

    public String getContents() {
        return this.contents.getText();
    }

    public void setContents(String contents) {
        this.contents.setText(contents);
    }

    public String getLatestVer() {
        return this.latestVer.getText();
    }

    public void setLatestVer(String latestVer) {
        this.latestVer.setText(latestVer);
    }

    public void setVersionChk(String versionChk) {
        this.versionChk.setText(versionChk);
    }

    public boolean isVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(boolean versionStatus) {
        this.versionStatus = versionStatus;
        if(versionStatus) {
            StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.Cd8d8d8);
        } else {
            StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.Cb01e1e);
        }
    }

}
