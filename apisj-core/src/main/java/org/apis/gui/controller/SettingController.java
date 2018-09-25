package org.apis.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.ByteUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

public class SettingController extends BasePopupController {

    @FXML private Label userNumLabel, cancelBtn, saveBtn;
    @FXML private ImageView rpcBtnIcon, generalBtnIcon, windowBtnIcon, rpcPwCover;
    @FXML private GridPane rpcGrid, generalGrid, windowGrid;
    @FXML private PasswordField rpcPwPasswordField;
    @FXML private TextField rpcPortTextField, rpcWhiteListTextField, rpcIdTextField, rpcPwTextField;
    @FXML public SlideButtonController startWalletWithLogInBtnController, enableLogEventBtnController, minimizeToTrayBtnController;
    @FXML private Label settingsTitle, settingsDesc, userNumTitle, userNumDesc, rpcTitle, rpcPortLabel, rpcWhiteListLabel, rpcIdLabel, rpcPwLabel,
                  generalTitle, startWalletWithLogInLabel, enableLogEventLabel, windowTitle, minimizeToTrayLabel;

    private Image downGrayIcon, upGrayIcon, privateIcon, publicIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        // Initialize Images
        downGrayIcon = new Image("image/ic_down_black@2x.png");
        upGrayIcon = new Image("image/ic_up_gray@2x.png");
        privateIcon = new Image("image/ic_private@2x.png");
        publicIcon = new Image("image/ic_public@2x.png");

        rpcPwTextField.textProperty().bindBidirectional(rpcPwPasswordField.textProperty());

        closeRpc();
        closeGeneral();
        closeWindow();

        loadSettingData();
    }

    public void languageSetting() {
        this.settingsTitle.textProperty().bind(StringManager.getInstance().setting.settingsTitle);
        this.settingsDesc.textProperty().bind(StringManager.getInstance().setting.settingsDesc);
        this.userNumTitle.textProperty().bind(StringManager.getInstance().setting.userNumTitle);
        this.userNumDesc.textProperty().bind(StringManager.getInstance().setting.userNumDesc);
        this.rpcTitle.textProperty().bind(StringManager.getInstance().setting.rpcTitle);
        this.rpcPortLabel.textProperty().bind(StringManager.getInstance().setting.rpcPortLabel);
        this.rpcWhiteListLabel.textProperty().bind(StringManager.getInstance().setting.rpcWhiteListLabel);
        this.rpcIdLabel.textProperty().bind(StringManager.getInstance().setting.rpcIdLabel);
        this.rpcPwLabel.textProperty().bind(StringManager.getInstance().setting.rpcPwLabel);
        this.generalTitle.textProperty().bind(StringManager.getInstance().setting.generalTitle);
        this.startWalletWithLogInLabel.textProperty().bind(StringManager.getInstance().setting.startWalletWithLogInLabel);
        this.enableLogEventLabel.textProperty().bind(StringManager.getInstance().setting.enableLogEventLabel);
        this.windowTitle.textProperty().bind(StringManager.getInstance().setting.windowTitle);
        this.minimizeToTrayLabel.textProperty().bind(StringManager.getInstance().setting.minimizeToTrayLabel);
        this.cancelBtn.textProperty().bind(StringManager.getInstance().setting.cancelBtn);
        this.saveBtn.textProperty().bind(StringManager.getInstance().setting.saveBtn);
    }

    private void loadSettingData(){
        Properties prop = AppManager.getRPCProperties();
        userNumLabel.setText(prop.getProperty("max_connections"));
        rpcPortTextField.setText(prop.getProperty("port"));
        rpcWhiteListTextField.setText(prop.getProperty("allow_ip"));
        rpcIdTextField.setText(prop.getProperty("id"));
        rpcPwTextField.setText(prop.getProperty("password"));

        prop = AppManager.getGeneralProperties();
        startWalletWithLogInBtnController.setSelected(prop.getProperty("in_system_log").equals("true"));
        enableLogEventBtnController.setSelected(prop.getProperty("enable_event_log").equals("true"));

        prop = AppManager.getWindowProperties();
        minimizeToTrayBtnController.setSelected(prop.getProperty("minimize_to_tray").equals("true"));
    }

    private ChangeListener<Boolean> rpcPortListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcWhiteListListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcIdListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcPwListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcPwPfListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    public void textFieldFocus() {
        if(rpcPortTextField.isFocused()) {
            rpcPortTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        } else {
            rpcPortTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        }

        if(rpcWhiteListTextField.isFocused()) {
            rpcWhiteListTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        } else {
            rpcWhiteListTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        }

        if(rpcIdTextField.isFocused()) {
            rpcIdTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        } else {
            rpcIdTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        }

        if(rpcPwTextField.isFocused()) {
            rpcPwTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        } else {
            rpcPwTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        }

        if(rpcPwPasswordField.isFocused()) {
            rpcPwPasswordField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        } else {
            rpcPwPasswordField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("rpcHeader")) {
            if(rpcGrid.isVisible()) {
                closeRpc();
            } else {
                openRpc();
            }

        } else if(fxid.equals("generalHeader")) {
            if(generalGrid.isVisible()) {
                closeGeneral();
            } else {
                openGeneral();
            }

        } else if(fxid.equals("windowHeader")) {
            if(windowGrid.isVisible()) {
                closeWindow();
            } else {
                openWindow();
            }

        } else if(fxid.equals("rpcPwCover")) {
            if(rpcPwTextField.isVisible()) {
                rpcPwCover.setImage(privateIcon);
                rpcPwTextField.setVisible(false);
                rpcPwPasswordField.setVisible(true);
            } else {
                rpcPwCover.setImage(publicIcon);
                rpcPwTextField.setVisible(true);
                rpcPwPasswordField.setVisible(false);
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
            prop.setProperty("port", rpcPortTextField.getText().trim());
            prop.setProperty("id", rpcIdTextField.getText().trim());
            prop.setProperty("password", rpcPwTextField.getText().trim());
            prop.setProperty("max_connections", userNumLabel.getText());
            prop.setProperty("allow_ip", rpcWhiteListTextField.getText().trim());
            AppManager.saveRPCProperties();

            prop = AppManager.getGeneralProperties();
            prop.setProperty("in_system_log", ""+startWalletWithLogInBtnController.isSelected());
            prop.setProperty("enable_event_log", ""+enableLogEventBtnController.isSelected());
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
        rpcGrid.setVisible(true);
        rpcGrid.prefHeightProperty().setValue(-1);
    }

    public void closeRpc() {
        rpcBtnIcon.setImage(downGrayIcon);
        rpcGrid.setVisible(false);
        rpcGrid.prefHeightProperty().setValue(0);
    }

    public void openGeneral() {
        generalBtnIcon.setImage(upGrayIcon);
        generalGrid.setVisible(true);
        generalGrid.prefHeightProperty().setValue(-1);
    }

    public void closeGeneral() {
        generalBtnIcon.setImage(downGrayIcon);
        generalGrid.setVisible(false);
        generalGrid.prefHeightProperty().setValue(0);
    }

    public void openWindow() {
        windowBtnIcon.setImage(upGrayIcon);
        windowGrid.setVisible(true);
        windowGrid.prefHeightProperty().setValue(-1);
    }

    public void closeWindow() {
        windowBtnIcon.setImage(downGrayIcon);
        windowGrid.setVisible(false);
        windowGrid.prefHeightProperty().setValue(0);
    }

}
