package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMyAddress implements Initializable {

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("id :"+id);
    }
}
