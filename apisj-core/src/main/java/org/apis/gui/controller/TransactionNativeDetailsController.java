package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.apis.gui.manager.AppManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsController implements Initializable {
    @FXML
    private Label copy, txHashLabel;

    private TransactionNativeDetailsImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Underline Setting
        copy.setOnMouseEntered(event -> txHashLabel.setUnderline(true));
        copy.setOnMouseExited(event -> txHashLabel.setUnderline(false));
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("back")) {
            if(handler != null) {
                handler.hideDetails();
            }

        } else if(fxid.equals("copy")) {
            String text = txHashLabel.getText();
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);

            PopupCopyTxHashController controller = (PopupCopyTxHashController)AppManager.getInstance().guiFx.showMainPopup("popup_copy_tx_hash.fxml", 0);
            controller.setHash(text);
        }
    }

    public void setHandler(TransactionNativeDetailsImpl handler) {
        this.handler = handler;
    }

    public interface TransactionNativeDetailsImpl {
        void hideDetails();
    }
}
