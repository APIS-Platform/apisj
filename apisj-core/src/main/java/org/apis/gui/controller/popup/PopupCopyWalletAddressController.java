package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyWalletAddressController extends BasePopupController {
    @FXML
    private Label addressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setAddress(String address){
        StringSelection stringSelection = new StringSelection(address);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        this.addressLabel.textProperty().setValue(address);
    }
}
