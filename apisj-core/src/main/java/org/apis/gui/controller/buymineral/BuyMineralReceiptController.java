package org.apis.gui.controller.buymineral;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralReceiptController extends BaseViewController {

    @FXML private Label totalApis, fromAddress, mask, value, toAddress;
    @FXML private AnchorPane maskPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setAddress(String address) {
        fromAddress.setText(address);
    }

    public void setMask(String mask) {
        this.mask.setText(mask);
        if(mask == null || mask.length() <= 0){
            maskPane.setVisible(false);
            maskPane.setPrefHeight(0);
        }else{
            maskPane.setVisible(true);
            maskPane.setPrefHeight(-1);
        }
    }

    public void setTotalFee(String totalFee) {
        totalApis.setText(totalFee);
        value.setText(totalFee+" APIS");
    }

    public void setPayerAddress(String payerAddress) {
        toAddress.setText(payerAddress);
    }
}
