package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMyAddressRegister implements Initializable {

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(1); }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnAddGroup")){
            System.out.println("btnAddGroup");
            AppManager.getInstance().guiFx.showMainPopup("popup_my_address_group.fxml", 1);
        }
    }
}
