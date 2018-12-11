package org.apis.gui.controller.buymineral;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralReceiptController extends BaseViewController {

    @FXML private Label totalApis, fromAddress, mask, value, toAddress, payBtn;
    @FXML private Label title, addressLabel, maskLabel, totalFeeLabel, payerLabel;
    @FXML private AnchorPane maskPane;
    @FXML private GridPane btnPay;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        btnPay.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(handler != null){
                    handler.transfer();
                }
            }
        });
    }

    public void languageSetting(){
        title.textProperty().bind(StringManager.getInstance().receipt.totalFee);
        addressLabel.textProperty().bind(StringManager.getInstance().receipt.address);
        maskLabel.textProperty().bind(StringManager.getInstance().receipt.mask);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().receipt.totalFee);
        payerLabel.textProperty().bind(StringManager.getInstance().receipt.payer);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);
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


    private BuyMineralReceiptImpl handler;
    public void setHandler(BuyMineralReceiptImpl handler){
        this.handler = handler;
    }

    public void setSuccessed(boolean isSuccessed) {
        if(isSuccessed){
            StyleManager.backgroundColorStyle(btnPay, StyleManager.AColor.Cb01e1e);
            btnPay.setDisable(false);
        }else{
            StyleManager.backgroundColorStyle(btnPay, StyleManager.AColor.Cd8d8d8);
            btnPay.setDisable(true);
        }
    }

    public interface BuyMineralReceiptImpl {
        void transfer();
    }
}
