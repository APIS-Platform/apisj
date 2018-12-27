package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.module.textfield.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.keystore.KeyStoreManager;

import java.net.URL;
import java.net.URLDecoder;
import java.util.ResourceBundle;

public class PopupChangeWalletNameController extends BasePopupController {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML private AnchorPane rootPane;
    @FXML private Label changeBtn;
    @FXML private ApisTextFieldController textFieldController;
    @FXML private Label title, subTitle, nameLabel;

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

                String text = textFieldController.getText();

                if (text == null || text.equals("")) {
                    textFieldController.failedForm("Enter new wallet name.");
                    failedForm();
                } else {
                    textFieldController.succeededForm();
                    succeededForm();
                }
            }

            @Override
            public void onAction() {
                PopupChangeWalletNameController.this.change();
            }

            @Override
            public void onKeyTab(){

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
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #b01e1e ;");
        isChangeable = true;
    }

    @Override
    public void setModel(BaseModel model){
        this.model = (WalletItemModel)model;
        textFieldController.setText(this.model.getAlias());
    }

    public void change(){
        if(isChangeable){
            try {
                String name = URLDecoder.decode(textFieldController.getText(), "UTF-8");

                KeyStoreManager.getInstance().updateWalletAlias(this.model.getAddress(), name);
                AppManager.getInstance().keystoreFileReadAll();
                AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
                AppManager.getInstance().guiFx.getWallet().update();
                PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml", zIndex);
                controller.requestFocusYesButton();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ApisTextFieldController getTextFieldController() {
        return this.textFieldController;
    }
}
