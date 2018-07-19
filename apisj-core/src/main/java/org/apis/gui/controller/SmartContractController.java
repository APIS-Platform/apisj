package org.apis.gui.controller;

import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void contractReadWritePopup() {
        AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_create.fxml", 0);
    }
}
