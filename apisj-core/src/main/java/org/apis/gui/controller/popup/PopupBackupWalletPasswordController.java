package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

public class PopupBackupWalletPasswordController extends BasePopupController {
    private WalletItemModel model;

    @FXML private AnchorPane rootPane;
    @FXML private ApisTextFieldController passwordController;
    @FXML private Label yesBtn;
    @FXML private Label title, subTitle, passwordLabel;

    public void change(){

        if (passwordController.getCheckBtnEnteredFlag()) {
            passwordController.setText("");
        }

        String text = passwordController.getText();


        if (text == null || text.equals("")) {
            passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
        } else if(! KeyStoreManager.getInstance().matchPassword(model.getKeystoreJsonData(),  passwordController.getText().trim().getBytes(Charset.forName("UTF-8")))){
            passwordController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
        } else{
            passwordController.succeededForm();

            PopupBackupWalletController controller = (PopupBackupWalletController) PopupManager.getInstance().showMainPopup(rootPane, "popup_backup_wallet.fxml", zIndex);
            controller.setModel(this.model, passwordController.getText());
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

            @Override
            public void onAction() {
                PopupBackupWalletPasswordController.this.change();
            }

            @Override
            public void onKeyTab(){

            }
        });

        succeededForm();
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.backupWalletPasswordTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.backupWalletPasswordSubTitle);
        passwordLabel.textProperty().bind(StringManager.getInstance().popup.backupWalletPasswordPassword);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.backupWalletPasswordYes);
    }

    public void failedForm(){
        StyleManager.backgroundColorStyle(yesBtn, StyleManager.AColor.Cd8d8d8);
    }

    public void succeededForm(){
        StyleManager.backgroundColorStyle(yesBtn, StyleManager.AColor.C910000);
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;
    }

    public ApisTextFieldController getPasswordController() {
        return this.passwordController;
    }
}
