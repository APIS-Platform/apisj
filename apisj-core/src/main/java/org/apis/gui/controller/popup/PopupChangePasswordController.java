package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.ApisTextFieldGroup;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.keystore.KeyStoreManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangePasswordController extends BasePopupController {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML private AnchorPane rootPane;
    @FXML private Label changeBtn;
    @FXML private ApisTextFieldController currentFieldController, newFieldController, reFieldController;
    @FXML private Label title, subTitle, currentPasswordLabel, newPasswordLabel;

    private ApisTextFieldGroup apisTextFieldGroup = new ApisTextFieldGroup();

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordSubTitle);
        currentPasswordLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordCurrentPw);
        newPasswordLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordNewPw);
        changeBtn.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordChange);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        currentFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());

        currentFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text = currentFieldController.getText();

                if (text == null || text.equals("")) {
                    currentFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else {
                    currentFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {

            }

            @Override
            public void onAction() {
                newFieldController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                newFieldController.requestFocus();
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
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if (text.length() < 8) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
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
                String text;

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                text = newFieldController.getText();

                if (text == null || text.equals("")) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if (text.length() < 8) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
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
            public void onAction() {
                reFieldController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                reFieldController.requestFocus();
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
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if(reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                text = reFieldController.getText();

                if(text == null || text.equals("")) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void onAction() {
                PopupChangePasswordController.this.change();
            }

            @Override
            public void onKeyTab(){
                currentFieldController.requestFocus();
            }
        });

        apisTextFieldGroup.add(currentFieldController);
        apisTextFieldGroup.add(newFieldController);
        apisTextFieldGroup.add(reFieldController);
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;

    }

    public void checkChangeNext(){
        if(newFieldController.getText().length() >= 8
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
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #b01e1e ;");
        isChangeable = true;
    }

    public void change(){

        if(isChangeable == false){
            return;
        }

        boolean isChanged = KeyStoreManager.getInstance().updateWalletPassword(this.model.getAddress(), currentFieldController.getText().toCharArray(), newFieldController.getText().toCharArray());
        if(isChanged){
            AppManager.getInstance().keystoreFileReadAll();
            AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
            AppManager.getInstance().guiFx.getWallet().update();
            PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml", zIndex);
            controller.requestFocusYesButton();
        }else{
            failedForm();
            currentFieldController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
        }
    }

    public ApisTextFieldController getCurrentFieldController() {
        return this.currentFieldController;
    }
}
