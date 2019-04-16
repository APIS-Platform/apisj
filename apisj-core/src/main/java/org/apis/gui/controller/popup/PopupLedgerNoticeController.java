package org.apis.gui.controller.popup;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.core.Transaction;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.hid.HIDDevice;
import org.apis.util.ByteUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupLedgerNoticeController extends BasePopupController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label title, subTitle;

    public PopupLedgerNoticeCallback callback;

    @FXML
    public void exit() {
        PopupManager.getInstance().hideMainPopup(zIndex);
        parentRequestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        bgAnchor.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                }
            }
        });
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.ledgerNoticeTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.ledgerNoticeSubTitle);
    }

    public void getLedgerTx(String address, String value, String gasPrice, String gasLimit, byte[] toAddress, byte[] toMask, byte[] data, String path) {
        Transaction ledgerTx, signedTx = null;

        if (toAddress == null || toAddress.length <= 0) {
            ledgerTx = AppManager.getInstance().generateTransaction(address, value, gasPrice, gasLimit, new byte[0], new byte[0], data);
        } else {
            ledgerTx = AppManager.getInstance().generateTransaction(address, value, gasPrice, gasLimit, toAddress, toMask, data);
        }

        HIDDevice finalLedger = AppManager.getInstance().getLedger();

        // Generate ledger signed tx
        try {
            signedTx = new Transaction(finalLedger.signTransaction(path, ledgerTx, ByteUtil.hexStringToBytes(address)));
        } catch (Exception e) {
            if(callback != null) {
                callback.errorLedgerCallback(e);
            }
        }

        if(callback != null) {
            callback.exitLedgerNoticeCallback(ledgerTx, signedTx);
        }
    }

    public interface PopupLedgerNoticeCallback {
        void exitLedgerNoticeCallback(Transaction ledgerTx, Transaction signedTx);
        void errorLedgerCallback(Exception e);
    }
}
