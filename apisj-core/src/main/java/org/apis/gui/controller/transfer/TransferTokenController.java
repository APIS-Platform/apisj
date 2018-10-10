package org.apis.gui.controller.transfer;

import javafx.fxml.FXML;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;

import java.net.URL;
import java.util.ResourceBundle;

public class TransferTokenController extends BaseViewController {
    @FXML private ApisWalletAndAmountController walletAndAmountController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        walletAndAmountController.setViewTypeApis(ApisWalletAndAmountController.ViewType.token);
    }

    public void setTokenSymbol(String symbol){
        walletAndAmountController.setTokenSymbol(symbol);
    }
}
