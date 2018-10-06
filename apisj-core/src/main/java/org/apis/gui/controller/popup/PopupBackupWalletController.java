package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.module.ApisTextFieldPkController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.spongycastle.util.encoders.Hex;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupBackupWalletController extends BasePopupController {
    private WalletItemModel model;

    @FXML
    private ApisTextFieldPkController privateKeyController;
    @FXML
    private Label title, downloadLabel, downloadButton, privateKeyLabel, footerComment;

    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex);
        PopupManager.getInstance().hideMainPopup(zIndex-1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        privateKeyController.setHandler(new ApisTextFieldPkController.ApisTextFieldPkImpl() {
            @Override
            public void copy() {
                PopupCopyPrivateKeyController controller = (PopupCopyPrivateKeyController)PopupManager.getInstance().showMainPopup("popup_copy_private_key.fxml",zIndex);
                controller.setPk(privateKeyController.getText());
            }
        });
    }
    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.backupWalletTitle);
        downloadLabel.textProperty().bind(StringManager.getInstance().popup.backupWalletDownload);
        downloadButton.textProperty().bind(StringManager.getInstance().popup.backupWalletDownload);
        privateKeyLabel.textProperty().bind(StringManager.getInstance().popup.backupWalletPrivateKey);
        footerComment.textProperty().bind(StringManager.getInstance().popup.backupWalletFooterComment);
    }

    public void setModel(BaseModel model, String password) {
        this.model = (WalletItemModel)model;
        byte[] pk = KeyStoreManager.getInstance().getPrivateKey(this.model.getKeystoreJsonData(), password);
        if(pk != null){
            this.privateKeyController.setText(Hex.toHexString(pk));
            this.privateKeyController.setAddress(this.model.getAddress());
        }else{
            System.out.println("pk is null");
        }

    }

    public void download(){
        if(this.model != null) {
            KeyStoreManager.getInstance().backupKeystoreWith(this.model.getId());
        }
    }
}
