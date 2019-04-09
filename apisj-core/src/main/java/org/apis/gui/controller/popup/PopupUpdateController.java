package org.apis.gui.controller.popup;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.config.SystemProperties;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.StringManager;
import org.apis.util.CompressionUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class PopupUpdateController extends BasePopupController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label firstLabel, secondLabel;

    private String latestVer;
    public PopupProcessCallback callback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        bgAnchor.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                }
            }
        });
    }

    private void languageSetting() {
        firstLabel.textProperty().bind(StringManager.getInstance().popup.updateFirstLabel);
        secondLabel.textProperty().bind(StringManager.getInstance().popup.resetSecondLabel);
    }

    public void versionUpdate() {
        // Download patch file from server
        String url = "https://storage.googleapis.com/apis-mn-images/pcwallet/"+ latestVer + ".zip";
        String zipFileName = "apis-core-" + latestVer + "-release.zip";
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        byte[] buf = new byte[8 * 1024];

        File tempFolder = new File(SystemProperties.getDefault().tempDir());
        if(!tempFolder.exists()) {
            tempFolder.mkdirs();
        }

        try {
            bis = new BufferedInputStream(new URL(url).openStream());
            bos = new BufferedOutputStream(new FileOutputStream(SystemProperties.getDefault().tempDir() + "/" + zipFileName));

            int data;
            while((data = bis.read(buf)) != -1) {
                bos.write(buf, 0, data);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch(IOException e2) {
                e2.printStackTrace();
            }
        }

        // Unzip zip file
        CompressionUtil cu = new CompressionUtil();
        File mainPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        File tempDir = new File(SystemProperties.getDefault().tempDir());
        try {
            cu.unzip(new File(tempDir.getPath() + "/" + zipFileName), tempDir, StandardCharsets.UTF_8.name());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (callback != null) {
            callback.exitPopupProcessCallback(zipFileName);
        }
    }

    public void setLatestVer(String latestVer) {
        this.latestVer = latestVer;
    }

    public interface PopupProcessCallback {
        void exitPopupProcessCallback(String zipFileName);
    }
}
