package org.apis.gui.controller.addressmasking;

import javafx.fxml.FXML;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressMaskingHandOverController extends BaseViewController {

    @FXML private ApisSelectBoxController selectAddressController, selectHandedToController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectAddressController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        selectHandedToController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
    }

    @Override
    public void update(){
        selectAddressController.update();
        selectHandedToController.update();
    }
}
