package org.apis.gui.controller.buymineral;

import javafx.fxml.FXML;
import org.apis.gui.controller.base.BasePopupController;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralController extends BasePopupController {

    @FXML private BuyMineralBodyController bodyController;
    @FXML private BuyMineralReceiptController receiptController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bodyController.setHandelr(new BuyMineralBodyController.BuyMineralBodyImpl() {
            @Override
            public void settingLayoutData() {
                BuyMineralController.this.settingLayoutData();
            }
        });
    }

    public void settingLayoutData(){
        String address = bodyController.getAddress();
        String mask = bodyController.getMask();
        String totalFee = bodyController.getTotalFee();
        String payerAddress = bodyController.getPayerAddress();
        receiptController.setAddress(address);
        receiptController.setMask(mask);
        receiptController.setTotalFee(totalFee);
        receiptController.setPayerAddress(payerAddress);
    }
}
