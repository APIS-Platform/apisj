package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangeWalletNameController extends BasePopupController {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML
    private Label changeBtn;
    @FXML
    private ApisTextFieldController textFieldController;
    @FXML
    private Label title, subTitle, nameLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
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
    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.changeWalletNameTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.changeWalletNameSubTitle);
        nameLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletNameName);
        changeBtn.textProperty().bind(StringManager.getInstance().popup.changeWalletNameChange);
    }


    public void failedForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
        isChangeable = false;
    }

    public void succeededForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
        isChangeable = true;
    }

    @Override
    public void setModel(BaseModel model){
        this.model = (WalletItemModel)model;
        textFieldController.setText(this.model.getAlias());
    }

    public void change(){
        if(isChangeable){
            KeyStoreManager.getInstance().updateWalletAlias(this.model.getId(), textFieldController.getText());
            AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
            AppManager.getInstance().guiFx.getWallet().update();
            exit();
        }
    }
}
