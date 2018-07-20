package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.model.WalletItemModel;
import org.spongycastle.util.encoders.Hex;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupBackupWalletController implements Initializable {
    private WalletItemModel model;

    @FXML
    private ApisTextFieldPkController privateKeyController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(1);
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(WalletItemModel model, String password) {
        this.model = model;
        byte[] pk = KeyStoreManager.getInstance().getPrivateKey(this.model.getKstoreJsonData(), password);
        if(pk != null){
            this.privateKeyController.setText(Hex.toHexString(pk));
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
