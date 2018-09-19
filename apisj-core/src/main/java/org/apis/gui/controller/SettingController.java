package org.apis.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.DBManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.run.MainFX;
import org.iq80.leveldb.DB;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingController implements Initializable {
    @FXML
    private Label userNumLabel, cancelBtn, saveBtn;
    @FXML
    private ImageView rpcBtnIcon, generalBtnIcon, windowBtnIcon, rpcPwCover;
    @FXML
    private GridPane rpcGrid, generalGrid, windowGrid;
    @FXML
    private PasswordField rpcPwPasswordField;
    @FXML
    private TextField rpcPortTextField, rpcWhiteListTextField, rpcIdTextField, rpcPwTextField;

    @FXML
    public SlideButtonController startWalletWithLogInBtnController, enableLogEventBtnController, minimizeToTrayBtnController;

    private Image downGrayIcon, upGrayIcon, privateIcon, publicIcon;

    // Multilingual Label
    @FXML
    private Label settingsTitle, settingsDesc, userNumTitle, userNumDesc, rpcTitle, rpcPortLabel, rpcWhiteListLabel, rpcIdLabel, rpcPwLabel,
                  generalTitle, startWalletWithLogInLabel, enableLogEventLabel, windowTitle, minimizeToTrayLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        // Initialize Images
        downGrayIcon = new Image("image/ic_down_black@2x.png");
        upGrayIcon = new Image("image/ic_up_gray@2x.png");
        privateIcon = new Image("image/ic_private@2x.png");
        publicIcon = new Image("image/ic_public@2x.png");

        // Initialize Slide Button
        startWalletWithLogInBtnController.init(DBManager.getInstance().isStartWalletWithLogIn());
        enableLogEventBtnController.init(DBManager.getInstance().isEnableLogEvent());
        minimizeToTrayBtnController.init(DBManager.getInstance().isMinimizeToTray());

        // Initialize Variables
        rpcPwTextField.textProperty().bindBidirectional(rpcPwPasswordField.textProperty());
        userNumLabel.setText(DBManager.getInstance().getUserNum());
        rpcPortTextField.setText(DBManager.getInstance().getPort());
        rpcWhiteListTextField.setText(DBManager.getInstance().getWhiteList());
        rpcIdTextField.setText(DBManager.getInstance().getId());
        rpcPwPasswordField.setText(DBManager.getInstance().getPw());
        rpcPwCover.setImage(privateIcon);
        rpcPwTextField.setVisible(false);
        rpcPwPasswordField.setVisible(true);
        closeRpc();
        closeGeneral();
        closeWindow();

        // Initialize TextField Focus Listener
        rpcPortTextField.focusedProperty().addListener(rpcPortListener);
        rpcWhiteListTextField.focusedProperty().addListener(rpcWhiteListListener);
        rpcIdTextField.focusedProperty().addListener(rpcIdListener);
        rpcPwTextField.focusedProperty().addListener(rpcPwListener);
        rpcPwPasswordField.focusedProperty().addListener(rpcPwPfListener);
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

    private ChangeListener<Boolean> rpcPortListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcWhiteListListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcIdListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcPwListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> rpcPwPfListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
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
            AppManager.getInstance().guiFx.hideMainPopup(-1);

        } else if(fxid.equals("saveBtn")) {
            DBManager.getInstance().setUserNum(userNumLabel.getText());
            DBManager.getInstance().setPort(rpcPortTextField.getText());
            DBManager.getInstance().setWhiteList(rpcWhiteListTextField.getText());
            DBManager.getInstance().setId(rpcIdTextField.getText());
            DBManager.getInstance().setPw(rpcPwPasswordField.getText());

            DBManager.getInstance().setStartWalletWithLogIn(startWalletWithLogInBtnController.isSelected());
            DBManager.getInstance().setEnableLogEvent(enableLogEventBtnController.isSelected());
            DBManager.getInstance().setMinimizeToTray(minimizeToTrayBtnController.isSelected());

            if (DBManager.getInstance().isMinimizeToTray()) {
                try {
                    Platform.setImplicitExit(false);
                    if(DBManager.getInstance().getTray().getTrayIcons().length == 0) {
                        DBManager.getInstance().getTray().add(DBManager.getInstance().getTrayIcon());
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            } else {
                Platform.setImplicitExit(true);
                DBManager.getInstance().getTray().remove(DBManager.getInstance().getTrayIcon());
            }

            AppManager.getInstance().guiFx.hideMainPopup(-1);
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
