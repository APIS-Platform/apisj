package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.config.SystemProperties;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupSuccessController extends BasePopupController {
    @FXML private Label title, subTitle, yesBtn;
    @FXML private AnchorPane bgAnchor;

    private EventHandler enterFilter;

    @FXML
    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex-1);
        PopupManager.getInstance().hideMainPopup(zIndex);
        setTitleColor(StyleManager.AColor.C000000);
        parentRequestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enterFilter = new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    exit();
                }
            }
        };
        bgAnchor.addEventFilter(KeyEvent.KEY_PRESSED, enterFilter);

        languageSetting();
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.successTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.successSubTitle);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.successYes);
    }

    public void setTitle(SimpleStringProperty title){
        this.title.textProperty().unbind();
        this.title.textProperty().bind(title);
    }

    public void setSubTitle(SimpleStringProperty subTitle){
        this.subTitle.textProperty().unbind();
        this.subTitle.textProperty().bind(subTitle);
    }

    public void setYesBtn(SimpleStringProperty yesBtn){
        this.yesBtn.textProperty().unbind();
        this.yesBtn.textProperty().bind(yesBtn);
    }

    public void requestFocusYesButton(){
        yesBtn.requestFocus();
    }

    public void setTitleColor(String color) {
        StyleManager.fontColorStyle(title, color);
    }

    public void setRestartApp(String zipFileName) {
        bgAnchor.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                }
            }
        });
        bgAnchor.removeEventFilter(KeyEvent.KEY_PRESSED, enterFilter);
        bgAnchor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER) {
                restartApp(zipFileName);
            }
        });

        yesBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                restartApp(zipFileName);
            }
        });
    }

    private boolean isRestartApp = false;
    public void restartApp(String zipFileName) {
        if(isRestartApp) {
            return;
        }
        isRestartApp = true;

        final File currentExe;
        BufferedWriter bw = null;
        try {
            currentExe = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            String exePath = URLDecoder.decode(currentExe.getAbsoluteFile().getParentFile().getParent(), "UTF-8");
            String destPath = URLDecoder.decode(currentExe.getAbsoluteFile().getParent(), StandardCharsets.UTF_8.name());
            String tempPath = SystemProperties.getDefault().tempDir();
            String batFileName = "update.bat";

            /**
             * batch file command
             **/
            String batchFileStr = ":: Remove zip\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Remove-Item -Path '" + tempPath + "\\" + zipFileName + "' -Recurse\\\"\n" +
                "\n" +
                ":: Check the process and kill\n" +
                ":Kill\n" +
                "taskkill /f /im apis-core.exe\n" +
                "timeout 1 > NUL\n" +
                "for /f %%a in ('tasklist ^| find /i /c \"apis-core\"') do (set apis=%%a)\n" +
                "if not \\\"%apis%\\\"==\\\"0\\\" goto Kill\n" +
                "\n" +
                ":: Remove temp\n" +
                ":del1\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Remove-Item -Path '" + destPath + "\\lib' -Recurse\\\"\n" +
                "timeout 1 > NUL\n" +
                "if exist \"" + destPath + "\\lib\\\" goto del1\n" +
                ":del2\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Remove-Item -Path '" + destPath + "\\apis-core.cfg' -Recurse\\\"\n" +
                "timeout 1 > NUL\n" +
                "if exist \"" + destPath + "\\apis-core.cfg\" goto del2\n" +
                ":del3\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Remove-Item -Path '" + destPath + "\\project-jfx.jar' -Recurse\\\"\n" +
                "timeout 1 > NUL\n" +
                "if exist \"" + destPath + "\\project-jfx.jar\" goto del3\n" +
                "\n" +
                ":: Copy temp to destination\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Copy-Item '" + tempPath + "\\*' -Destination '" + destPath + "' -Recurse -Force\\\"\n" +
                "\n" +
                ":: Remove temp\n" +
                ":Loop\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Remove-Item -Path '" + tempPath + "' -Recurse\\\"\n" +
                "timeout 1 > NUL\n" +
                "if exist \"" + tempPath + "\\\" goto Loop\n" +
                "\n" +
                ":: Start program\n" +
                "powershell.exe Start-Process -FilePath powershell.exe -WindowStyle hidden -verb runAs \\\"Start-Sleep -s 1 | & '" + exePath + "\\apis-core.exe'\\\"\n" +
                "\n" +
                ":: Remove batch file\n" +
                ":Loop2\n" +
                "powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs \\\"Remove-Item -Path '" + destPath + "\\" + batFileName + "' -Recurse\\\"\n" +
                "timeout 1 > NUL\n" +
                "if exist \"" + destPath + "\\" + batFileName + "\" goto Loop2";
            String line = System.getProperty("line.separator");
            batchFileStr.replace("\n", line);

            bw = new BufferedWriter(new FileWriter(destPath + "\\" + batFileName));
            bw.write(batchFileStr);
            bw.flush();

            // Start batch file
            final ArrayList<String> command = new ArrayList<String>();
            command.add("powershell.exe");
            command.add("Start-Process");
            command.add("-FilePath");
            command.add("powershell.exe");
            command.add("-WindowStyle");
            command.add("hidden");
            command.add("-verb runAs");
            command.add("\\\"& '" + destPath + "\\" + batFileName + "'\\\"");

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            Platform.exit();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
