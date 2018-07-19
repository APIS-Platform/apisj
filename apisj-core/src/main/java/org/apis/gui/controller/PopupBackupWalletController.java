package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.model.WalletItemModel;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupBackupWalletController implements Initializable {
    private WalletItemModel model;

    @FXML
    private ApisTextFieldPkController privateKeyController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(WalletItemModel model) {
        this.model = model;
    }

    public void download(){
        KeyStoreManager.getInstance().backupKeystoreWith(this.model.getId());
    }
}
