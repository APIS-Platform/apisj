package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMiningWalletController implements Initializable {


    public void exit(){
        AppManager.getInstance().guiFx.hidePopup(0);
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("selectBtn")){
            AppManager.getInstance().guiFx.showPopup("popup_mining_wallet_confirm.fxml", 1);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
