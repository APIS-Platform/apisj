package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
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
        PopupManager.getInstance().hideMainPopup(1);
        PopupManager.getInstance().hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        privateKeyController.setHandler(new ApisTextFieldPkController.ApisTextFieldPkImpl() {
            @Override
            public void copy() {
                PopupCopyPrivateKeyController controller = (PopupCopyPrivateKeyController)PopupManager.getInstance().showMainPopup("popup_copy_private_key.fxml",1);
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

    public void setModel(WalletItemModel model, String password) {
        this.model = model;
        byte[] pk = KeyStoreManager.getInstance().getPrivateKey(this.model.getKstoreJsonData(), password);
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
