package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupSyncController implements Initializable {
    @FXML
    private Label subMessageLabel;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setSubMessage(0, 0);
    }

    public void setSubMessage(long lastBlock, long bestBlock){
        String sLastBlock = AppManager.comma(""+lastBlock);
        String sBestBlock = AppManager.comma(""+bestBlock);
        this.subMessageLabel.textProperty().setValue("Processing block "+sLastBlock+" of "+sBestBlock+".");
    }

}
