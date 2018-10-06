package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyPrivateKeyController extends BasePopupController {
    @FXML
    private Label pkLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setPk(String pk){
        StringSelection stringSelection = new StringSelection(pk);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        this.pkLabel.textProperty().setValue(pk);
    }
}
