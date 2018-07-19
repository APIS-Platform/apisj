package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.model.WalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangeWalletName implements Initializable {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML
    private Label changeBtn;
    @FXML
    private ApisTextFieldController textFieldController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
        textFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {

                String text;

                text = textFieldController.getText();

                if (text == null || text.equals("")) {
                    textFieldController.failedForm("Enter new wallet name.");
                    failedForm();
                } else {
                    textFieldController.succeededForm();
                    succeededForm();
                }
            }
        });

    }

    public void failedForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
        isChangeable = false;
    }

    public void succeededForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
        isChangeable = true;
    }

    public void setModel(WalletItemModel model){
        this.model = model;
        textFieldController.setText(model.getAlias());
    }

    public void change(){
        if(isChangeable){
            KeyStoreManager.getInstance().updateWalletAlias(this.model.getId(), textFieldController.getText());
            AppManager.getInstance().keystoreFileReadAll();
            AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
            AppManager.getInstance().guiFx.getWallet().update();
            exit();
        }
    }
}
