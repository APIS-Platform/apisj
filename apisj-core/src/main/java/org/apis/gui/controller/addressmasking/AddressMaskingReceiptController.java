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

public class AddressMaskingReceiptController extends BaseViewController {
    @FXML private Label totalFeeTitle, addressLabel, maskLabel, totalFeeLabel, payerLabel, totalFeeDesc, totalFeePayBtn;
    @FXML private Label address, maskId, maskDomain, totalFeeValue, payerAddress;
    @FXML private GridPane btnPay;

    private boolean isEnabled;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }
    public void languageSetting() {
        totalFeeTitle.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeTitle);
        addressLabel.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeAddress);
        maskLabel.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeMask);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeLabel);
        payerLabel.textProperty().bind(StringManager.getInstance().addressMasking.totalFeePayer);
        totalFeeDesc.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeDesc);
        totalFeePayBtn.textProperty().bind(StringManager.getInstance().common.payButton);
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String id = ((Node) event.getSource()).getId();

        if(handler != null && isEnabled){
            handler.transfer();
        }
    }

    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;

        if(isEnabled){
            StyleManager.backgroundColorStyle(btnPay, StyleManager.AColor.C910000);
        }else{
            StyleManager.backgroundColorStyle(btnPay, StyleManager.AColor.Cd8d8d8);
        }
    }



    public void setAddress(String fromAddress) {
        this.address.setText(fromAddress);
    }
    public void setPayerAddress(String toAddress) {
        this.payerAddress.setText(toAddress);
    }
    public void setMaskId(String maskId) {
        this.maskId.setText(maskId);
    }
    public void setMaskDomain(String maskDomain) {
        this.maskDomain.setText(maskDomain);
    }
    public void setValue(String value) {
        this.totalFeeValue.setText(value);
    }

    private AddressMaskingReceiptImpl handler;
    public void setHandler(AddressMaskingReceiptImpl handler){
        this.handler = handler;
    }
    public interface AddressMaskingReceiptImpl {
        void transfer();
    }
}
