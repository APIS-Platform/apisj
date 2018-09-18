package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsController implements Initializable {
    @FXML
    private Label copy, txHashLabel, nonce, blockNum, blockConfirm, time, confirmedIn, from, to,
                  contractAddr, value, fee, mineral, chargedFee, gasPrice, gasLimit, gasUsed, error;
    // Multilingual Support Label
    @FXML
    private Label transactionDetailsLabel, hashLabel, nonceLabel, blockLabel, blockConfirmLabel, timeLabel, confirmedInLabel,
                  confirmedInUnit, fromLabel, toLabel, contractAddrLabel, valueLabel, feeLabel, mineralLabel, chargedFeeLabel,
                  gasLabel, errorLabel;

    private TransactionNativeDetailsImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Underline Setting
        copy.setOnMouseEntered(event -> txHashLabel.setUnderline(true));
        copy.setOnMouseExited(event -> txHashLabel.setUnderline(false));
    }

    public void languageSetting() {
        transactionDetailsLabel.textProperty().bind(StringManager.getInstance().transaction.detailsLabel);
        hashLabel.textProperty().bind(StringManager.getInstance().transaction.detailsHashLabel);
        nonceLabel.textProperty().bind(StringManager.getInstance().transaction.detailsNonceLabel);
        blockLabel.textProperty().bind(StringManager.getInstance().transaction.blockLabel);
        blockConfirmLabel.textProperty().bind(StringManager.getInstance().transaction.detailsBlockConfirmLabel);
        timeLabel.textProperty().bind(StringManager.getInstance().transaction.timeLabel);
        confirmedInLabel.textProperty().bind(StringManager.getInstance().transaction.detailsConfirmedInLabel);
        confirmedInUnit.textProperty().bind(StringManager.getInstance().transaction.detailsConfirmedInUnit);
        fromLabel.textProperty().bind(StringManager.getInstance().transaction.fromLabel);
        toLabel.textProperty().bind(StringManager.getInstance().transaction.toLabel);
        contractAddrLabel.textProperty().bind(StringManager.getInstance().transaction.detailsContractAddrLabel);
        valueLabel.textProperty().bind(StringManager.getInstance().transaction.valueLabel);
        feeLabel.textProperty().bind(StringManager.getInstance().transaction.feeLabel);
        mineralLabel.textProperty().bind(StringManager.getInstance().transaction.detailsMineralLabel);
        chargedFeeLabel.textProperty().bind(StringManager.getInstance().transaction.detailsChargedFeeLabel);
        gasLabel.textProperty().bind(StringManager.getInstance().transaction.detailsGasLabel);
        errorLabel.textProperty().bind(StringManager.getInstance().transaction.detailsErrorLabel);
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

    public void setTxHashLabel(String txHashLabel) {
        this.txHashLabel.setText(txHashLabel);
    }

    public void setBlockNum(long blockNum) {
        this.blockNum.setText(AppManager.comma(Long.toString(blockNum)));
    }

    public void setBlockConfirm(long blockConfirm) {
        this.blockConfirm.setText(AppManager.comma(Long.toString(blockConfirm)));
    }

    public void setFrom(String from) {
        this.from.setText(from);
    }

    public void setTo(String to) {
        this.to.setText(to);
    }

    public void setValue(String value) {
        this.value.setText(value);
    }

    public void setFee(String fee) {
        this.fee.setText(fee);
    }

    public void setMineral(String mineral) {
        this.mineral.setText(mineral);
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice.setText(gasPrice);
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit.setText(AppManager.comma(Long.toString(gasLimit)));
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed.setText(AppManager.comma(Long.toString(gasUsed)));
    }

    public void setNonce(Long nonce) {
        this.nonce.setText(Long.toString(nonce));
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddr.setText(contractAddr);
    }

    public void setError(String error) {
        this.error.setText(error);
    }
}
