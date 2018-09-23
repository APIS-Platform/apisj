package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupRemoveWalletPasswordController extends BasePopupController {
    private WalletItemModel model;

    @FXML
    private ApisTextFieldController passwordController;
    @FXML
    private Label yesBtn;
    @FXML
    private Label title, subTitle, passwordLabel;

    public void change(){

        if (passwordController.getCheckBtnEnteredFlag()) {
            passwordController.setText("");
        }

        String text = passwordController.getText();


        if (text == null || text.equals("")) {
            passwordController.failedForm("Please enter your password.");
        } else if(! KeyStoreManager.getInstance().matchPassword(model.getKstoreJsonData(),  passwordController.getText())){
            passwordController.failedForm("Please enter your password.");
        } else{
            passwordController.succeededForm();

            PopupRemoveWalletController controller = (PopupRemoveWalletController) PopupManager.getInstance().showMainPopup("popup_remove_wallet.fxml", 1);
            controller.remove(model.getId());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
        passwordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {

            }
        });

        succeededForm();
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.removeWalletPasswordTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.removeWalletPasswordSubTitle);
        passwordLabel.textProperty().bind(StringManager.getInstance().popup.removeWalletPasswordPassword);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.removeWalletPasswordYes);
    }

    public void failedForm(){
        yesBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
    }

    public void succeededForm(){
        yesBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
    }

    public void setModel(WalletItemModel model) {
        this.model = model;
    }
}
