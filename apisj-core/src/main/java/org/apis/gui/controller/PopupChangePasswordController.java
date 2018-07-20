package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.model.WalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangePasswordController implements Initializable {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML
    private Label changeBtn;
    @FXML
    private ApisTextFieldController currentFieldController, newFieldController, reFieldController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");

        currentFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text = currentFieldController.getText();

                if (text == null || text.equals("")) {
                    currentFieldController.failedForm("Please enter your password.");
                } else if (text.length() < 8) {
                    currentFieldController.failedForm("Password must contain at least 8 characters.");
                } else if (!currentFieldController.pwValidate(text)) {
                    currentFieldController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    currentFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {

            }
        });
        newFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                text = newFieldController.getText();

                if (text == null || text.equals("")) {
                    newFieldController.failedForm("Please enter your password.");
                } else if (text.length() < 8) {
                    newFieldController.failedForm("Password must contain at least 8 characters.");
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    newFieldController.succeededForm();
                }

                if (!newFieldController.getText().isEmpty()) {
                    if(reFieldController.getHandler() != null){
                        reFieldController.getHandler().onFocusOut();
                    }
                }else{

                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {

            }
        });

        reFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                text = reFieldController.getText();

                if(text == null || text.equals("")) {
                    reFieldController.failedForm("Please check your password.");
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm("Password does not match the confirm password.");
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {

            }
        });
    }



    public void setModel(WalletItemModel model) {
        this.model = model;

    }

    public void checkChangeNext(){

        if( currentFieldController.getText().length() >= 8
            && currentFieldController.pwValidate(currentFieldController.getText())
            && newFieldController.getText().length() >= 8
            && newFieldController.getText().equals(reFieldController.getText())){
            succeededForm();
        }else{
            failedForm();
        }

    }

    public void failedForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
        isChangeable = false;
    }

    public void succeededForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
        isChangeable = true;
    }

    public void change(){

        if(isChangeable == false){
            return;
        }

        boolean isChanged = KeyStoreManager.getInstance().updateWalletPassword(this.model.getId(), this.model.getAlias(), currentFieldController.getText(), newFieldController.getText());
        if(isChanged){
            AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
            AppManager.getInstance().guiFx.getWallet().update();
            exit();
        }else{
            currentFieldController.failedForm("Please check your password.");
            failedForm();
        }
    }
}
