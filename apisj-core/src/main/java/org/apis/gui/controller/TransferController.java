package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;


public class TransferController implements Initializable {
    @FXML
    private TextField amountTextField, recevingTextField;
    @FXML
    private ApisSelectBoxController walletSelectorController;


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("sendBtn")){
            //sendTransfer();
            AppManager.getInstance().guiFx.showMainPopup("popup_transfer_send.fxml", 0);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void showSendPopup(){

    }

    public void sendTransfer(){
        String sGasPrice = "50000000000";
        String sValue = amountTextField.getText();
        String sAddr = walletSelectorController.getAddress();
        String sToAddress = recevingTextField.getText();
        String sPasswd = "aaaa";

        System.out.println("sGasPrice : " + sGasPrice);
        System.out.println("sValue : " + sValue);
        System.out.println("sAddr : " + sAddr);
        System.out.println("sToAddress : " + sToAddress);

        BigInteger gas = new BigInteger(sGasPrice);
        BigInteger balance = new BigInteger(sValue);

        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && sValue != null && sValue.length() > 0){

            AppManager.getInstance().ethereumCreateTransactions(sAddr, gas.toString(), "200000", sToAddress, balance.toString(), sPasswd);
            AppManager.getInstance().ethereumSendTransactions();
        }
    }
}
