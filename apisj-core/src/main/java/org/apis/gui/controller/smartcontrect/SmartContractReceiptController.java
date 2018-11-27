package org.apis.gui.controller.smartcontrect;

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

public class SmartContractReceiptController extends BaseViewController {

    @FXML private GridPane transferBtn, receipt, dimNoFees;
    @FXML private Label transferAmountTitleNature, transferAmountTitleDecimal,transferAmount, fees, totalWithdrawal, afterBalance;
    @FXML private Label transferAmountTitle, detailLabel, transferAmountLabel, feeLabel, totalWithdrawalLabel, afterBalanceLabel;
    @FXML private Label transferAmountDesc1, transferAmountDesc2, transferBtnLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

    }
    public void languageSetting() {
        transferAmountTitle.textProperty().bind(StringManager.getInstance().receipt.transferAmount);
        transferAmountLabel.textProperty().bind(StringManager.getInstance().receipt.transferAmount);
        detailLabel.textProperty().bind(StringManager.getInstance().common.detailLabel);
        feeLabel.textProperty().bind(StringManager.getInstance().receipt.fee);
        totalWithdrawalLabel.textProperty().bind(StringManager.getInstance().receipt.withdrawalLabel);
        afterBalanceLabel.textProperty().bind(StringManager.getInstance().receipt.afterBalanceLabel);
        transferAmountDesc1.textProperty().bind(StringManager.getInstance().receipt.amountDesc1);
        transferAmountDesc2.textProperty().bind(StringManager.getInstance().receipt.amountDesc2);
        transferBtnLabel.textProperty().bind(StringManager.getInstance().receipt.transferButton);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();

        if(handler != null){
            handler.onMouseClickTransfer();
        }
    }


    private SmartContractReceiptImpl handler;
    public void setHandler(SmartContractReceiptImpl handler){
        this.handler = handler;
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

    public void setActive(boolean isAction) {
        if(isAction){
            transferBtn.setStyle( new JavaFXStyle(transferBtn.getStyle()).add("-fx-background-color","#910000").toString());
        }else{
            transferBtn.setStyle( new JavaFXStyle(transferBtn.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }
    }

    public interface SmartContractReceiptImpl{
        void onMouseClickTransfer();
    }
}
