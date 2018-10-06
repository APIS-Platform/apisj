package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyTxHashController extends BasePopupController {
    @FXML
    private Label hashLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setHash(String hash){
        StringSelection stringSelection = new StringSelection(hash);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        this.hashLabel.textProperty().setValue(hash);
    }
}
