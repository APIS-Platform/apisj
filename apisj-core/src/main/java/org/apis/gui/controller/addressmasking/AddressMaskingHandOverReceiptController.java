package org.apis.gui.controller.addressmasking;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressMaskingHandOverReceiptController extends BaseViewController {

    @FXML private GridPane btnPay;
    @FXML private Label totalFeeTitle, addressLabel, maskLabel, handValueLabel, handedToLabel, payButton, fromAddress, toAddress, mask, value;

    private boolean enabled;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }
    public void languageSetting() {
        totalFeeTitle.textProperty().bind(StringManager.getInstance().receipt.totalFee);
        addressLabel.textProperty().bind(StringManager.getInstance().receipt.address);
        maskLabel.textProperty().bind(StringManager.getInstance().receipt.mask);
        handValueLabel.textProperty().bind(StringManager.getInstance().receipt.handedToValue);
        handedToLabel.textProperty().bind(StringManager.getInstance().receipt.handedTo);
        payButton.textProperty().bind(StringManager.getInstance().common.payButton);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();

        if(enabled){
            if(handler != null){
                handler.transfer();
            }
        }
    }



    public void setFromAddress(String fromAddress) {
        this.fromAddress.setText(fromAddress);
    }
    public void setToAddress(String toAddress) {
        this.toAddress.setText(toAddress);
    }
    public void setMask(String mask) {
        this.mask.setText(mask);
    }
    public void setValue(String value) {
        this.value.setText(value);
    }


    private AddressMaskingHandOverReceiptImpl handler;
    public void setHandler(AddressMaskingHandOverReceiptImpl handler){
        this.handler = handler;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if(enabled){
            StyleManager.backgroundColorStyle(btnPay, StyleManager.AColor.Cb01e1e);
        }else{
            StyleManager.backgroundColorStyle(btnPay, StyleManager.AColor.Cd8d8d8);
        }
    }

    public interface AddressMaskingHandOverReceiptImpl{
        void transfer();
    }
}
