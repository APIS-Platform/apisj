package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.List;
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
        } else if(! KeyStoreManager.getInstance().matchPassword(model.getKeystoreJsonData(),  passwordController.getText())){
            passwordController.failedForm("Please enter your password.");
        } else{
            passwordController.succeededForm();

            PopupRemoveWalletController controller = (PopupRemoveWalletController) PopupManager.getInstance().showMainPopup("popup_remove_wallet.fxml", zIndex+1);
            controller.setHandler(new PopupRemoveWalletController.PopupRemoveWalletImpl() {
                @Override
                public void remove(List<String> removeWalletIdList) {
                    if(handler != null){
                        handler.remove(removeWalletIdList);
                    }
                }
            });
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

            @Override
            public void onAction() {

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

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;
    }


    private PopupRemoveWalletPassword handler;
    public void setHandler(PopupRemoveWalletPassword handler){
        this.handler = handler;
    }
    public interface PopupRemoveWalletPassword{
        void remove(List<String> removeWalletIdList);
    }

    public ApisTextFieldController getPasswordController() {
        return this.passwordController;
    }
}
