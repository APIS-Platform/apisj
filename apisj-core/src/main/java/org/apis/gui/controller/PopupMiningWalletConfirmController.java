package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.config.SystemProperties;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.keystore.KeyStoreUtil;
import org.apis.util.ConsoleUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMiningWalletConfirmController implements Initializable {
    private String walletId, address;
    private boolean isChangeable = false;

    @FXML
    private Label addressLabel, startBtn;
    @FXML
    private ApisTextFieldController passwordFieldController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(1);
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if( AppManager.getInstance().startMining(walletId, passwordFieldController.getText()) ){
            AppManager.getInstance().guiFx.hideMainPopup(0);
            AppManager.getInstance().guiFx.hideMainPopup(1);
        }else{
            passwordFieldController.failedForm("Please check your password.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {
                if(new_text == null || new_text.length() == 0){
                    failedForm();
                }else{
                    succeededForm();
                }
            }
        });
    }


    public void failedForm(){
        startBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
        isChangeable = false;
    }

    public void succeededForm(){
        startBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
        isChangeable = true;
    }

    public void init(String walletId, String address) {
        this.walletId = walletId;
        this.address = address;

        addressLabel.textProperty().setValue(this.address);
    }
}
