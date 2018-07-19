package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteSelectController implements Initializable {

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("yesBtn")) {
            AppManager.getInstance().guiFx.hideMainPopup(0);
            AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_modify.fxml",0);
        }
    }

}
