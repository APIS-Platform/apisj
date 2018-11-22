package org.apis.gui.controller.transfer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class TransferTokenReceiptController extends BaseViewController {

    @FXML
    private GridPane transferBtn, receipt, dimNoFees;
    @FXML private Label transferAmountTitleNature, transferAmountTitleDecimal,transferAmount, fees, totalWithdrawal, afterBalance, afterTokenBalance ;
    @FXML private Label tokenLabel1, tokenLabel2, tokenLabel3, tokenLabel4;
    @FXML private Label transferAmountTitle, detailLabel, transferAmountLabel, gasPriceReceiptLabel, totalWithdrawalLabel, afterBalanceLabel;
    @FXML private Label transferAmountDesc1, transferAmountDesc2, transferBtnLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }
    private void languageSetting(){
        transferAmountTitle.textProperty().bind(StringManager.getInstance().transfer.transferAmount);
        detailLabel.textProperty().bind(StringManager.getInstance().common.detailLabel);
        transferAmountLabel.textProperty().bind(StringManager.getInstance().transfer.transferAmount);
        gasPriceReceiptLabel.textProperty().bind(StringManager.getInstance().transfer.detailFee);
        totalWithdrawalLabel.textProperty().bind(StringManager.getInstance().transfer.detailTotalWithdrawal);
        afterBalanceLabel.textProperty().bind(StringManager.getInstance().transfer.detailAfterBalance);
        transferAmountDesc1.textProperty().bind(StringManager.getInstance().transfer.detailGaspriceComment1);
        transferAmountDesc2.textProperty().bind(StringManager.getInstance().transfer.detailGaspriceComment2);
        transferBtnLabel.textProperty().bind(StringManager.getInstance().transfer.transferButton);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();

        if(handler != null){
            handler.onMouseClickTransfer();
        }
    }

    public void setVisibleTransferButton(boolean isVisible) {
        transferBtn.setVisible(isVisible);
    }

    public void showNoFees() {
        receipt.setVisible(false);
        dimNoFees.setVisible(true);
    }
    public void hideNoFees(){
        receipt.setVisible(true);
        dimNoFees.setVisible(false);
    }

    public void setTotalAmount(String n, String d) {
        transferAmountTitleNature.setText(n);
        transferAmountTitleDecimal.setText(d);
    }

    public void setAmount(String amount) {
        transferAmount.setText(amount);
    }

    public void setFee(String fee) {
        this.fees.setText(fee);
    }

    public void setWithdrawal(String withdrawal) {
        totalWithdrawal.setText(withdrawal);
    }

    public void setAfterBalance(String afterBalance) {
        this.afterBalance.setText(afterBalance);
    }

    public void setAfterTokenBalance(String afterTokenBalance){
        this.afterTokenBalance.setText(afterTokenBalance);
    }

    public void setTokenSymbol(String symbol){
        this.tokenLabel1.setText(symbol);
        this.tokenLabel2.setText(symbol);
        this.tokenLabel3.setText(symbol);
        this.tokenLabel4.setText(symbol);
    }

    public void transferButtonActive() {
        transferBtn.setStyle( new JavaFXStyle(transferBtn.getStyle()).add("-fx-background-color","#910000").toString());
    }

    public void transferButtonDefault() {
        transferBtn.setStyle( new JavaFXStyle(transferBtn.getStyle()).add("-fx-background-color","#d8d8d8").toString());
    }

    private TransferTokenReceiptImpl handler;
    public void setHandler(TransferTokenReceiptImpl handler){
        this.handler = handler;
    }


    public interface TransferTokenReceiptImpl{
        void onMouseClickTransfer();
    }
}
