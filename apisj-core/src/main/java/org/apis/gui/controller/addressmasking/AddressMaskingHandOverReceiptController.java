package org.apis.gui.controller.addressmasking;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressMaskingHandOverReceiptController extends BaseViewController {

    @FXML private Label totalFeeTitle, addressLabel, maskLabel, totalFeeLabel, handedToLabel, totalDesc, payButton, fromAddress, toAddress, mask, value;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }
    public void languageSetting() {
        totalFeeTitle.textProperty().bind(StringManager.getInstance().receipt.fee);
        addressLabel.textProperty().bind(StringManager.getInstance().receipt.address);
        maskLabel.textProperty().bind(StringManager.getInstance().receipt.mask);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().receipt.totalFee);
        handedToLabel.textProperty().bind(StringManager.getInstance().receipt.handedTo);
        totalDesc.textProperty().bind(StringManager.getInstance().receipt.maskDesc);
        payButton.textProperty().bind(StringManager.getInstance().common.payButton);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if(handler != null){
            handler.transfer();
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
    public interface AddressMaskingHandOverReceiptImpl{
        void transfer();
    }
}
