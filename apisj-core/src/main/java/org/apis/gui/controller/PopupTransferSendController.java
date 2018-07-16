package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupTransferSendController implements Initializable {
    private PopupTransferSendInterface handler;

    @FXML
    private ApisTextFieldController passwordController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
        if(handler != null){
            handler.close();
        }
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        String password = passwordController.getText();
        if(id.equals("btnSendTransfer")){
            if(handler != null){
                handler.send(password);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setHandler(PopupTransferSendInterface handler){
        this.handler = handler;
    }

    interface PopupTransferSendInterface {
        public void send(String password);
        public void close();
    }
}
