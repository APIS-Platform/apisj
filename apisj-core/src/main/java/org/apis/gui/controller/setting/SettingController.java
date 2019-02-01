package org.apis.gui.controller.setting;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.config.SystemProperties;
import org.apis.gui.common.OSInfo;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.popup.PopupSuccessController;
import org.apis.gui.manager.*;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class SettingController extends BasePopupController {
    private final int maxPeerNumber = 30;

    @FXML private Label userNumLabel, cancelBtn, saveBtn, settingsWarning;
    @FXML private ImageView rpcBtnIcon, generalBtnIcon, windowBtnIcon, icCancel;
    @FXML private Label settingsTitle, settingsDesc, userNumTitle, userNumDesc, rpcTitle, generalTitle, windowTitle;
    @FXML private VBox rpcVBox, generalVBox, windowVBox;
    @FXML private SettingItemBtnController rpcStartInputController, startWalletWithLogInBtnController, enableLogEventBtnController, minimizeToTrayBtnController, rewardSaveBtnController;
    @FXML private SettingItemInputController portInputController, whiteListInputController, maxConnectionsInputController, idInputController, passwordInputController;
    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane gridPane, bodyScrollPaneContentPane;

    private boolean isScrolling;

    private Image downGrayIcon, upGrayIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        // Initialize Images
        downGrayIcon = new Image("image/ic_down_black@2x.png");
        upGrayIcon = new Image("image/ic_up_gray@2x.png");

        closeRpc();
        openGeneral();
        openWindow();

        // Initiate items
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "Port");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "White List");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "Max Connections");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "ID");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_PASS, "Password");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "RPC Start");

        addGeneralItem("startWalletWithLogIn");
        //addGeneralItem("enableLogEvent");
        addGeneralItem("rewardSave");
        addWindowItem("minimizeToTray");

        setItemsUnderLine();

        loadSettingData();


        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = bodyScrollPaneContentPane.getHeight() - bodyScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = bodyScrollPane.getVvalue() + add;
                        if(moveV > bodyScrollPane.getVmax()){
                            moveV = bodyScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = bodyScrollPane.getVvalue() - add;
                        if(moveV < bodyScrollPane.getVmin()){
                            moveV = bodyScrollPane.getVmin();
                        }
                    }

                    if(!bodyScrollPane.isPressed()) {
                        bodyScrollPane.setVvalue(moveV);
                    }
                }
            }
        });

    }

    public void languageSetting() {
        this.settingsTitle.textProperty().bind(StringManager.getInstance().setting.settingsTitle);
        this.settingsDesc.textProperty().bind(StringManager.getInstance().setting.settingsDesc);
        this.settingsWarning.textProperty().bind(StringManager.getInstance().setting.settingWarning);
        this.userNumTitle.textProperty().bind(StringManager.getInstance().setting.userNumTitle);
        this.userNumDesc.textProperty().bind(StringManager.getInstance().setting.userNumDesc);
        this.rpcTitle.textProperty().bind(StringManager.getInstance().setting.rpcTitle);
        this.generalTitle.textProperty().bind(StringManager.getInstance().setting.generalTitle);
        this.windowTitle.textProperty().bind(StringManager.getInstance().setting.windowTitle);
        this.saveBtn.textProperty().bind(StringManager.getInstance().common.saveButton);
        this.cancelBtn.textProperty().bind(StringManager.getInstance().common.backButton);
    }

    private void loadSettingData() {
        Properties prop = AppManager.getRPCProperties();
        userNumLabel.setText("30");
        portInputController.setTextField(prop.getProperty("port"));
        whiteListInputController.setTextField(prop.getProperty("allow_ip"));
        maxConnectionsInputController.setTextField(prop.getProperty("max_connections"));
        idInputController.setTextField(prop.getProperty("id"));
        passwordInputController.setTextField(prop.getProperty("password"));
        rpcStartInputController.setSelected(prop.getProperty("use_rpc").equals("true"));

        prop = AppManager.getGeneralProperties();
        if(startWalletWithLogInBtnController != null) {
            startWalletWithLogInBtnController.setSelected(prop.getProperty("in_system_log").equals("true"));
        }
        if(enableLogEventBtnController != null) {
            enableLogEventBtnController.setSelected(prop.getProperty("enable_event_log").equals("true"));
        }
        rewardSaveBtnController.setSelected(prop.getProperty("reward_sound").equals("true"));
        userNumLabel.setText(prop.getProperty("peer_num"));

        prop = AppManager.getWindowProperties();
        minimizeToTrayBtnController.setSelected(prop.getProperty("minimize_to_tray").equals("true"));
    }

    private void addRpcItem(String inputFlag, String contentsId) {
        if(contentsId.equals("Port")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.portInputController = (SettingItemInputController)loader.getController();
                this.portInputController.setInput(inputFlag);
                this.portInputController.setContents(StringManager.getInstance().setting.rpcPortLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("White List")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.whiteListInputController = (SettingItemInputController)loader.getController();
                this.whiteListInputController.setInput(inputFlag);
                this.whiteListInputController.setContents(StringManager.getInstance().setting.rpcWhiteListLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("Max Connections")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.maxConnectionsInputController = (SettingItemInputController)loader.getController();
                this.maxConnectionsInputController.setInput(inputFlag);
                this.maxConnectionsInputController.setContents(StringManager.getInstance().setting.rpcMaxConnectionsLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(contentsId.equals("ID")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.idInputController = (SettingItemInputController)loader.getController();
                this.idInputController.setInput(inputFlag);
                this.idInputController.setContents(StringManager.getInstance().setting.rpcIdLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("Password")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.passwordInputController = (SettingItemInputController)loader.getController();
                this.passwordInputController.setInput(inputFlag);
                this.passwordInputController.setContents(StringManager.getInstance().setting.rpcPwLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(contentsId.equals("RPC Start")){
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.rpcStartInputController = (SettingItemBtnController)loader.getController();
                this.rpcStartInputController.setContents(StringManager.getInstance().setting.rpcStartLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addGeneralItem(String contentsId) {
        if(contentsId.equals("startWalletWithLogIn")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.startWalletWithLogInBtnController = (SettingItemBtnController)loader.getController();
                this.startWalletWithLogInBtnController.setContents(StringManager.getInstance().setting.startWalletWithLogInLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("enableLogEvent")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.enableLogEventBtnController = (SettingItemBtnController)loader.getController();
                this.enableLogEventBtnController.setContents(StringManager.getInstance().setting.enableLogEventLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("rewardSave")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.rewardSaveBtnController = (SettingItemBtnController)loader.getController();
                this.rewardSaveBtnController.setContents(StringManager.getInstance().setting.rewardSoundLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addWindowItem(String contentsId) {
        if(contentsId.equals("minimizeToTray")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                windowVBox.getChildren().add(item);

                this.minimizeToTrayBtnController = (SettingItemBtnController)loader.getController();
                this.minimizeToTrayBtnController.setContents(StringManager.getInstance().setting.minimizeToTrayLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setItemsUnderLine() {
        for(int i=0; rpcVBox.getChildren().size()>i; i++) {
            if(i != rpcVBox.getChildren().size()-1) {
                rpcVBox.getChildren().get(i).setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #d8d8d8;");
            }
        }

        for(int i=0; generalVBox.getChildren().size()>i; i++) {
            if(i != generalVBox.getChildren().size()-1) {
                generalVBox.getChildren().get(i).setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #d8d8d8;");
            }
        }

        for(int i=0; windowVBox.getChildren().size()>i; i++) {
            if(i != windowVBox.getChildren().size()-1) {
                windowVBox.getChildren().get(i).setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #d8d8d8;");
            }
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("rpcHeader")) {
            if(rpcVBox.isVisible()) {
                closeRpc();
            } else {
                openRpc();
            }

        } else if(fxid.equals("generalHeader")) {
            if(generalVBox.isVisible()) {
                closeGeneral();
            } else {
                openGeneral();
            }

        } else if(fxid.equals("windowHeader")) {
            if(windowVBox.isVisible()) {
                closeWindow();
            } else {
                openWindow();
            }

        } else if(fxid.equals("userNumMinus")) {
            int num = Integer.parseInt(userNumLabel.getText());
            if(num > 1) num--;
            userNumLabel.setText(Integer.toString(num));

        } else if(fxid.equals("userNumPlus")) {
            int num = Integer.parseInt(userNumLabel.getText());
            if(num < maxPeerNumber) num++;
            userNumLabel.setText(Integer.toString(num));

        } else if(fxid.equals("cancelBtn")) {
            exit();
            //PopupManager.getInstance().hideMainPopup(-1);

        } else if(fxid.equals("saveBtn")) {

            Properties prop = AppManager.getRPCProperties();
            prop.setProperty("port", portInputController.getTextField().trim());
            prop.setProperty("id", idInputController.getTextField().trim());
            prop.setProperty("password", passwordInputController.getTextField().trim());
            prop.setProperty("max_connections", maxConnectionsInputController.getTextField().trim());
            prop.setProperty("allow_ip", whiteListInputController.getTextField().trim());
            prop.setProperty("use_rpc", "" + rpcStartInputController.isSelected());
            AppManager.saveRPCProperties();

            // RPC server start/stop
            AppManager.getInstance().stopRPC();
            if(rpcStartInputController.isSelected()){
                AppManager.getInstance().startRPC();
            }

            prop = AppManager.getGeneralProperties();
            if(!Boolean.toString(startWalletWithLogInBtnController.isSelected()).equals(prop.getProperty("in_system_log"))) {
                prop.setProperty("in_system_log", "" + startWalletWithLogInBtnController.isSelected());
                // 윈도우 시작프로그램 등록
                if (OSInfo.getOs() == OSInfo.OS.WINDOWS) {
                    File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
                    if ("true".equals(prop.getProperty("in_system_log"))) {
                        try {
                            String exePath = URLDecoder.decode(file.getAbsoluteFile().getParentFile().getParent(), "UTF-8");
                            ArrayList<String> cmd = new ArrayList<String>();
                            // Register start-up menu
//                            cmd.add("powershell.exe");
//                            cmd.add("Start-Process");
//                            cmd.add("-WindowStyle");
//                            cmd.add("hidden");
//                            cmd.add("-FilePath");
//                            cmd.add("powershell.exe");
//                            cmd.add("-verb runAs");
//                            cmd.add("\\\"reg add 'HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run' /v 'apis-core' /t REG_SZ /d '" +
//                                    exePath + "\\apis-core.exe' /f\\\"");
//
//                            ProcessBuilder builder = new ProcessBuilder(cmd);
//                            Process proc = builder.start();
//                            proc.waitFor();

                            // Create schedule
                            cmd.add("powershell.exe");
                            cmd.add("Start-Process");
                            cmd.add("-WindowStyle");
                            cmd.add("hidden");
                            cmd.add("-FilePath");
                            cmd.add("powershell.exe");
                            cmd.add("-verb runAs");
                            cmd.add("\\\"schtasks /create /sc onlogon /tn apis-core /rl highest /tr '" +
                                    exePath + "\\apis-core.exe' /f\\\"");

                            ProcessBuilder builder = new ProcessBuilder(cmd);
                            Process proc = builder.start();
                            proc.waitFor();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        try {
                            ArrayList<String> cmd = new ArrayList<String>();
                            // Register start-up menu
//                            cmd.add("powershell.exe");
//                            cmd.add("Start-Process");
//                            cmd.add("-WindowStyle");
//                            cmd.add("hidden");
//                            cmd.add("-FilePath");
//                            cmd.add("powershell.exe");
//                            cmd.add("-verb runAs");
//                            cmd.add("\\\"reg Delete 'HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run' /v 'apis-core' /f\\\"");
//
//                            ProcessBuilder builder = new ProcessBuilder(cmd);
//                            Process proc = builder.start();
//                            proc.waitFor();

                            // Create schedule
                            cmd.add("powershell.exe");
                            cmd.add("Start-Process");
                            cmd.add("-WindowStyle");
                            cmd.add("hidden");
                            cmd.add("-FilePath");
                            cmd.add("powershell.exe");
                            cmd.add("-verb runAs");
                            cmd.add("\\\"schtasks /delete /tn apis-core /f\\\"");

                            ProcessBuilder builder = new ProcessBuilder(cmd);
                            Process proc = builder.start();
                            proc.waitFor();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            prop.setProperty("in_system_log", "" + startWalletWithLogInBtnController.isSelected());
            if(enableLogEventBtnController != null) {
                prop.setProperty("enable_event_log", "" + enableLogEventBtnController.isSelected());
            }
            prop.setProperty("reward_sound", ""+rewardSaveBtnController.isSelected());
            // Limit number of peer
            prop.setProperty("peer_num", userNumLabel.getText());
            Map<String, Object> cliOptions = new HashMap<>();
            cliOptions.put("peer.maxActivePeers", userNumLabel.getText());
            SystemProperties.getDefault().overrideParams(cliOptions);

            AppManager.saveGeneralProperties();

            prop = AppManager.getWindowProperties();
            prop.setProperty("minimize_to_tray", ""+minimizeToTrayBtnController.isSelected());
            AppManager.saveWindowProperties();


            if("true".equals(prop.getProperty("minimize_to_tray"))){
                Platform.setImplicitExit(false);
                AppManager.getInstance().createTrayIcon(AppManager.getInstance().guiFx.getPrimaryStage());
            }else{
                Platform.setImplicitExit(true);

                if(OSInfo.getOs() != OSInfo.OS.MAC){
                    for(int i = 0; i<SystemTray.getSystemTray().getTrayIcons().length; i++){
                        if(SystemTray.getSystemTray().getTrayIcons()[i].getImage().equals(AppManager.getInstance().getTrayIcon().getImage())){
                            SystemTray.getSystemTray().remove(AppManager.getInstance().getTrayIcon());
                            break;
                        }
                    }
                }
            }
            //exit();
            PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(null, "popup_success.fxml",zIndex+1);
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
        if(fxid.equals("saveBtn")){
            StyleManager.backgroundColorStyle(saveBtn, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(saveBtn, StyleManager.AColor.Cb01e1e);

        }else if(fxid.equals("cancelBtn")){
            StyleManager.backgroundColorStyle(cancelBtn, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(cancelBtn, StyleManager.AColor.Cb01e1e);
            icCancel.setImage(ImageManager.icBackRed);
        }

    }

    @FXML
    public void onMouseExited(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
        if(fxid.equals("saveBtn")){
            StyleManager.backgroundColorStyle(saveBtn, StyleManager.AColor.Cb01e1e);
            StyleManager.fontColorStyle(saveBtn, StyleManager.AColor.Cffffff);
        }else if(fxid.equals("cancelBtn")){
            StyleManager.backgroundColorStyle(cancelBtn, StyleManager.AColor.Cb01e1e);
            StyleManager.fontColorStyle(cancelBtn, StyleManager.AColor.Cffffff);
            icCancel.setImage(ImageManager.icBackWhite);
        }
    }



    public void openRpc() {
        rpcBtnIcon.setImage(upGrayIcon);
        rpcVBox.setVisible(true);
        rpcVBox.prefHeightProperty().setValue(-1);
    }

    public void closeRpc() {
        rpcBtnIcon.setImage(downGrayIcon);
        rpcVBox.setVisible(false);
        rpcVBox.prefHeightProperty().setValue(0);
    }

    public void openGeneral() {
        generalBtnIcon.setImage(upGrayIcon);
        generalVBox.setVisible(true);
        generalVBox.prefHeightProperty().setValue(-1);
    }

    public void closeGeneral() {
        generalBtnIcon.setImage(downGrayIcon);
        generalVBox.setVisible(false);
        generalVBox.prefHeightProperty().setValue(0);
    }

    public void openWindow() {
        windowBtnIcon.setImage(upGrayIcon);
        windowVBox.setVisible(true);
        windowVBox.prefHeightProperty().setValue(-1);
    }

    public void closeWindow() {
        windowBtnIcon.setImage(downGrayIcon);
        windowVBox.setVisible(false);
        windowVBox.prefHeightProperty().setValue(0);
    }

}
