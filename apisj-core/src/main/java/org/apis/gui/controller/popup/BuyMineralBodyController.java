package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralBodyController extends BaseViewController {
    @FXML private ApisSelectBoxController beneficiaryController, payerController, apisSelectController, mineralDetailController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        beneficiaryController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
        payerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
    }
}
