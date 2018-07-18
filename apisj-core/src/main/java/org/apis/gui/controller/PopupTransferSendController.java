package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class PopupTransferSendController implements Initializable {
    private PopupTransferSendInterface handler;

    @FXML
    private Label sendingAddress, receiveAddress, sendAmount, totalAmount, aferBalance, btnSendTransfer, timeLabel;

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
            if(passwordController.getText().trim().length() > 0){
                if(handler != null){
                    handler.send(password);
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {
                if(new_text.length() > 0){
                    btnSendTransfer.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color:#910000; ");
                }else{
                    btnSendTransfer.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color:#d8d8d8; ");
                }
            }
        });
    }

    public void succeededForm() {
        passwordController.succeededForm();
    }

    public void failedForm(String text){
        passwordController.failedForm(text);
    }

    public void setHandler(PopupTransferSendInterface handler){
        this.handler = handler;
    }

    public void init(String sendAddr, String receivAddr, String sendAmount, String totalAmount, String aferBalance) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm");
        int utc = TimeZone.getDefault().getRawOffset()/1000/3600;

        this.timeLabel.textProperty().setValue(dateFormat.format(new Date()).toUpperCase()+"(UTC+"+utc+")");
        this.sendingAddress.textProperty().setValue(sendAddr);
        this.receiveAddress.textProperty().setValue(receivAddr);
        this.sendAmount.textProperty().setValue(sendAmount+" APIS");
        this.totalAmount.textProperty().setValue(totalAmount+" APIS");
        this.aferBalance.textProperty().setValue(aferBalance+" APIS");
    }

    interface PopupTransferSendInterface {
        public void send(String password);
        public void close();
    }
}
