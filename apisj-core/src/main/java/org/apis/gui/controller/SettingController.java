package org.apis.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SettingController extends BasePopupController {

    @FXML private Label userNumLabel, cancelBtn, saveBtn;
    @FXML private ImageView rpcBtnIcon, generalBtnIcon, windowBtnIcon;
    @FXML private Label settingsTitle, settingsDesc, userNumTitle, userNumDesc, rpcTitle, generalTitle, windowTitle;
    @FXML private VBox rpcVBox, generalVBox, windowVBox;
    @FXML private SettingItemBtnController startWalletWithLogInBtnController, enableLogEventBtnController, minimizeToTrayBtnController, rewordSaveBtnController;
    @FXML private SettingItemInputController portInputController, whiteListInputController, idInputController, passwordInputController;

    private Image downGrayIcon, upGrayIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        // Initialize Images
        downGrayIcon = new Image("image/ic_down_black@2x.png");
        upGrayIcon = new Image("image/ic_up_gray@2x.png");

        closeRpc();
        closeGeneral();
        closeWindow();

        // Initiate items
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "Port");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "White List");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "ID");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_PASS, "Password");
        addGeneralItem("startWalletWithLogIn");
        addGeneralItem("enableLogEvent");
        addGeneralItem("rewordSave");
        addWindowItem("minimizeToTray");

        setItemsUnderLine();

        loadSettingData();
    }

    public void languageSetting() {
        this.settingsTitle.textProperty().bind(StringManager.getInstance().setting.settingsTitle);
        this.settingsDesc.textProperty().bind(StringManager.getInstance().setting.settingsDesc);
        this.userNumTitle.textProperty().bind(StringManager.getInstance().setting.userNumTitle);
        this.userNumDesc.textProperty().bind(StringManager.getInstance().setting.userNumDesc);
        this.rpcTitle.textProperty().bind(StringManager.getInstance().setting.rpcTitle);
        this.generalTitle.textProperty().bind(StringManager.getInstance().setting.generalTitle);
        this.windowTitle.textProperty().bind(StringManager.getInstance().setting.windowTitle);
        this.cancelBtn.textProperty().bind(StringManager.getInstance().setting.cancelBtn);
        this.saveBtn.textProperty().bind(StringManager.getInstance().setting.saveBtn);
    }

    private void loadSettingData() {
        Properties prop = AppManager.getRPCProperties();
        userNumLabel.setText(prop.getProperty("max_connections"));
        portInputController.setTextField(prop.getProperty("port"));
        whiteListInputController.setTextField(prop.getProperty("allow_ip"));
        idInputController.setTextField(prop.getProperty("id"));
        passwordInputController.setTextField(prop.getProperty("password"));

        prop = AppManager.getGeneralProperties();
        startWalletWithLogInBtnController.setSelected(prop.getProperty("in_system_log").equals("true"));
        enableLogEventBtnController.setSelected(prop.getProperty("enable_event_log").equals("true"));
        rewordSaveBtnController.setSelected(prop.getProperty("reward_sound").equals("true"));

        prop = AppManager.getWindowProperties();
        minimizeToTrayBtnController.setSelected(prop.getProperty("minimize_to_tray").equals("true"));
    }

    private void addRpcItem(String inputFlag, String contentsId) {
        if(contentsId.equals("Port")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_input.fxml");
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
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.whiteListInputController = (SettingItemInputController)loader.getController();
                this.whiteListInputController.setInput(inputFlag);
                this.whiteListInputController.setContents(StringManager.getInstance().setting.rpcWhiteListLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("ID")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_input.fxml");
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
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.passwordInputController = (SettingItemInputController)loader.getController();
                this.passwordInputController.setInput(inputFlag);
                this.passwordInputController.setContents(StringManager.getInstance().setting.rpcPwLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addGeneralItem(String contentsId) {
        if(contentsId.equals("startWalletWithLogIn")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_btn.fxml");
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
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.enableLogEventBtnController = (SettingItemBtnController)loader.getController();
                this.enableLogEventBtnController.setContents(StringManager.getInstance().setting.enableLogEventLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("rewordSave")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.rewordSaveBtnController = (SettingItemBtnController)loader.getController();
                this.rewordSaveBtnController.setContents(StringManager.getInstance().setting.rewordSoundLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addWindowItem(String contentsId) {
        if(contentsId.equals("minimizeToTray")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/setting_item_btn.fxml");
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
            if(num < 5) num++;
            userNumLabel.setText(Integer.toString(num));

        } else if(fxid.equals("cancelBtn")) {
            exit();
            //PopupManager.getInstance().hideMainPopup(-1);

        } else if(fxid.equals("saveBtn")) {

            Properties prop = AppManager.getRPCProperties();
            prop.setProperty("port", portInputController.getTextField().trim());
            prop.setProperty("id", idInputController.getTextField().trim());
            prop.setProperty("password", passwordInputController.getTextField().trim());
            prop.setProperty("max_connections", userNumLabel.getText());
            prop.setProperty("allow_ip", whiteListInputController.getTextField().trim());
            AppManager.saveRPCProperties();

            prop = AppManager.getGeneralProperties();
            prop.setProperty("in_system_log", ""+startWalletWithLogInBtnController.isSelected());
            prop.setProperty("enable_event_log", ""+enableLogEventBtnController.isSelected());
            prop.setProperty("reward_sound", ""+rewordSaveBtnController.isSelected());
            System.out.println("rewordSaveBtnController.isSelected() : "+rewordSaveBtnController.isSelected());
            AppManager.saveGeneralProperties();

            prop = AppManager.getWindowProperties();
            prop.setProperty("minimize_to_tray", ""+minimizeToTrayBtnController.isSelected());
            AppManager.saveWindowProperties();

            if("true".equals(prop.getProperty("minimize_to_tray"))){
                Platform.setImplicitExit(false);
                createTrayIcon(AppManager.getInstance().guiFx.getPrimaryStage());
            }else{
                Platform.setImplicitExit(true);
                for(int i=0; i<SystemTray.getSystemTray().getTrayIcons().length; i++){
                    SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[i]);
                }
            }

            exit();
        }
    }

    public void createTrayIcon(final Stage stage) {
        if(SystemTray.isSupported()) {
            java.awt.Image image = null;
            try {
                URL url  = getClass().getClassLoader().getResource("image/ic_favicon@2x.png");

                image = ImageIO.read(url);
                image = image.getScaledInstance(16,16,0);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(SystemTray.isSupported()) {
                                stage.hide();
                                SystemTray.getSystemTray().getTrayIcons()[SystemTray.getSystemTray().getTrayIcons().length-1].displayMessage("Some", "Message", TrayIcon.MessageType.INFO);
                            } else {
                                System.exit(0);
                            }
                        }
                    });
                }
            });

            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                        }
                    });
                }
            };

            // Create a Popup Menu
            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            MenuItem closeItem = new MenuItem("Close");

            showItem.addActionListener(showListener);
            closeItem.addActionListener(closeListener);

            popupMenu.add(showItem);
            popupMenu.add(closeItem);

            // Construct a TrayIcon
            try {
                TrayIcon trayIcon = new TrayIcon(image, "APIS", popupMenu);
                trayIcon.addActionListener(showListener);
                for(int i=0; i<SystemTray.getSystemTray().getTrayIcons().length; i++){
                    SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[i]);
                }
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
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
