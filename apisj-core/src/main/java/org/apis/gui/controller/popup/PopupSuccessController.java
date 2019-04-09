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
import org.apis.cli.CLIStart;
import org.apis.config.SystemProperties;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.run.MainFX;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
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

    public void restartApp(String zipFileName) {
        final File currentExe;
        try {
            currentExe = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            String exePath = URLDecoder.decode(currentExe.getAbsoluteFile().getParentFile().getParent(), "UTF-8");
            String destPath = URLDecoder.decode(currentExe.getAbsoluteFile().getParent(), StandardCharsets.UTF_8.name());
            String tempPath = SystemProperties.getDefault().tempDir();

            /* Build command:
             * powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs "Remove-Item -Path 'tempPath + \ + zipFileName' -Recurse"
             * powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs "Copy-Item 'tempPath + \*' -Destination 'destPath' -Recurse -Force"
             * powershell.exe Start-Process -WindowStyle hidden -FilePath powershell.exe -verb runAs "Remove-Item -Path 'tempPath' -Recurse"
             * powershell.exe -WindowStyle hidden & "exePath + \apis-core.exe"
             * */
            final ArrayList<String> command = new ArrayList<String>();

            // Remove zip
            command.add("powershell.exe");
            command.add("Start-Process");
            command.add("-WindowStyle");
            command.add("hidden");
            command.add("-FilePath");
            command.add("powershell.exe");
            command.add("-verb runAs");
            command.add("\\\"Remove-Item -Path '" + tempPath + "\\" + zipFileName + "' -Recurse\\\"");

            ProcessBuilder builder = new ProcessBuilder(command);
            Process proc = builder.start();
            proc.waitFor();

            // Copy temp to destination
            command.clear();
            command.add("powershell.exe");
            command.add("Start-Process");
            command.add("-WindowStyle");
            command.add("hidden");
            command.add("-FilePath");
            command.add("powershell.exe");
            command.add("-verb runAs");
            command.add("\\\"Copy-Item '" + tempPath + "\\*' -Destination '" + destPath + "' -Recurse -Force\\\"");

            builder = new ProcessBuilder(command);
            proc = builder.start();
            proc.waitFor();

            // Remove temp
            command.clear();
            command.add("powershell.exe");
            command.add("Start-Process");
            command.add("-WindowStyle");
            command.add("hidden");
            command.add("-FilePath");
            command.add("powershell.exe");
            command.add("-verb runAs");
            command.add("\\\"Remove-Item -Path '" + tempPath + "' -Recurse\\\"");

            builder = new ProcessBuilder(command);
            proc = builder.start();
            proc.waitFor();

            command.clear();
            command.add("powershell.exe");
            command.add("-WindowStyle");
            command.add("hidden");
            command.add("& \\\"" + exePath + "\\apis-core.exe\\\"");

            builder = new ProcessBuilder(command);
            builder.start();
            Platform.exit();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
