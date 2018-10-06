package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyWalletAddressController extends BasePopupController {
    @FXML
    private Label addressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setAddress(String address){
        this.addressLabel.textProperty().setValue(address);
    }
}
