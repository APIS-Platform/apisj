package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.blockchain.ApisUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptController extends BaseViewController {

    @FXML private AnchorPane rootPane, buttonPane, scrollContent;
    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane button, dimNoFees, receipt;
    @FXML private VBox itemList;
    @FXML private Label titleLabel, titleValue1, titleValue2, symbolLabel, buttonLabel;
    @FXML private Label titleLabel2, noFeesTitle, noFeesSubTitle;

    private ReceiptAddressController addressController;
    private ReceiptAddressController beneficiaryAddressController;
    private ReceiptAddressController payerAddressController;

    private ReceiptItemBController maskItemController;

    private ReceiptValueAController amountValueController;
    private ReceiptValueAController chargedAmountValueController;
    private ReceiptValueAController feeValueController;
    private ReceiptValueAController mineralValueController;
    private ReceiptValueAController chargedFeeValueController;
    private ReceiptValueAController afterBalanceController;

    private ReceiptOnlyValueController afterTokenValueController;
    private ReceiptOnlyValueController tokenAmountValueController;

    private String address, beneficiaryAddress, payerAddress, amount, chargedAmount, mask, fee, mineral, afterBalance, chargedFee, afterTokenBalance, tokenAmount;
    private boolean isSuccessed;
    private boolean isScrolling;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        noFeesTitle.textProperty().bind(StringManager.getInstance().receipt.nofees);
        noFeesSubTitle.textProperty().bind(StringManager.getInstance().receipt.nofeesDesc);

        initializeScrollSpeed();
        setVisibleNoFees(false);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("button")){
            if(isSuccessed) {
                if (handler != null) {
                    handler.send();
                }
            }
        }
    }


    private void initializeScrollSpeed(){
        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = scrollContent.getHeight() - bodyScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = bodyScrollPane.getVvalue() + add;
                        if(moveV > bodyScrollPane.getVmax()){
                            moveV = bodyScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = bodyScrollPane.getVvalue() - add;
                        if(moveV < bodyScrollPane.getVmin()){
                            moveV = bodyScrollPane.getVmin();
                        }
                    }

                    if(!bodyScrollPane.isPressed()) {
                        bodyScrollPane.setVvalue(moveV);
                    }
                }
            }
        });
    }

    @Override
    public void update(){

        if(addressController != null){
            addressController.setAddress(address);
        }

        if(beneficiaryAddressController != null){
            beneficiaryAddressController.setAddress(beneficiaryAddress);
        }

        if(payerAddressController != null){
            payerAddressController.setAddress(payerAddress);
        }

        if(maskItemController != null){
            maskItemController.setValue(mask);
        }

        if(amountValueController != null){
            amountValueController.setValue(amount);
        }

        if(chargedAmountValueController != null){
            chargedAmountValueController.setValue(chargedAmount);
        }

        if(chargedFeeValueController != null){
            chargedFeeValueController.setValue(chargedFee);
        }

        if(feeValueController != null){
            feeValueController.setValue(fee);
        }

        if(afterBalanceController != null){
            afterBalanceController.setValue(afterBalance);
        }

        if(mineralValueController != null){
            mineralValueController.setValue(mineral);
        }

        if(afterTokenValueController != null){
            afterTokenValueController.setValue(afterTokenBalance);
        }

        if(tokenAmountValueController != null){
            tokenAmountValueController.setValue(tokenAmount);
        }
    }

    public void setTitle(SimpleStringProperty title){
        this.titleLabel.textProperty().unbind();
        this.titleLabel.textProperty().bind(title);

        this.titleLabel2.textProperty().unbind();
        this.titleLabel2.textProperty().bind(title);
    }

    public void setTitleValue(BigInteger value){
        String stringValue = ApisUtil.readableApis(value, ',', false);
        String[] values = stringValue.split("\\.");
        titleValue1.setText(values[0]);
        if(values.length > 1) {
            titleValue2.setText("."+values[1]);
        }else{
            titleValue2.setText(".000000000000000000");
        }
    }

    public void setButtonTitle(SimpleStringProperty btn){
        this.buttonLabel.textProperty().unbind();
        this.buttonLabel.textProperty().bind(btn);
    }


    public void addVSpace(float vspace){
        AnchorPane pane = new AnchorPane();
        pane.setMinHeight(-1);
        pane.setPrefHeight(vspace);
        pane.setMaxHeight(-1);

        itemList.getChildren().add(pane);
    }

    public void addLineStyleDotted(){
        AnchorPane pane = new AnchorPane();
        pane.setMinHeight(-1);
        pane.setPrefHeight(0);
        pane.setMaxHeight(-1);
        pane.setStyle("-fx-border-width : 1 0 0 0; -fx-border-color : #c7c8cc; -fx-border-style : segments(2, 2, 2, 2);");

        itemList.getChildren().add(pane);
    }

    public void addAddress(double leftPadding){
        if(addressController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_address.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            addressController = (ReceiptAddressController)fxmlController.getController();
            addressController.setTitle(StringManager.getInstance().receipt.address);
            addressController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addBeneficiaryAddress(double leftPadding){
        if(beneficiaryAddressController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_address.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            beneficiaryAddressController = (ReceiptAddressController)fxmlController.getController();
            beneficiaryAddressController.setTitle(StringManager.getInstance().receipt.beneficiaryAddress);
            beneficiaryAddressController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPayerAddress(double leftPadding){
        if(payerAddressController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_address.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            payerAddressController = (ReceiptAddressController)fxmlController.getController();
            payerAddressController.setTitle(StringManager.getInstance().receipt.payer);
            payerAddressController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMaskAddress(double leftPadding){
        if(maskItemController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_item_b.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            maskItemController = (ReceiptItemBController) fxmlController.getController();
            maskItemController.setTitle(StringManager.getInstance().receipt.mask);
            maskItemController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAmount(double leftPadding){
        if(amountValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_value_a.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            amountValueController = (ReceiptValueAController) fxmlController.getController();
            amountValueController.setTitle(StringManager.getInstance().receipt.amount);
            amountValueController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addChargedAmount(double leftPadding){
        if(chargedAmountValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_value_a.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            chargedAmountValueController = (ReceiptValueAController) fxmlController.getController();
            chargedAmountValueController.setTitle(StringManager.getInstance().receipt.chargedAmount);
            chargedAmountValueController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFee(double leftPadding){
        if(feeValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_value_a.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            feeValueController = (ReceiptValueAController) fxmlController.getController();
            feeValueController.setTitle(StringManager.getInstance().receipt.fee);
            feeValueController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMineral(double leftPadding){
        if(mineralValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_value_a.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            mineralValueController = (ReceiptValueAController) fxmlController.getController();
            mineralValueController.setTitle(StringManager.getInstance().receipt.mineral);
            mineralValueController.setSymbol("MNR");
            mineralValueController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addChargedFee(double leftPadding){
        if(chargedFeeValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_value_a.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            chargedFeeValueController = (ReceiptValueAController) fxmlController.getController();
            chargedFeeValueController.setTitle(StringManager.getInstance().receipt.chargedFee);
            chargedFeeValueController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAfterBalance(double leftPadding){
        if(afterBalanceController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_value_a.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            afterBalanceController = (ReceiptValueAController) fxmlController.getController();
            afterBalanceController.setTitle(StringManager.getInstance().receipt.afterBalanceLabel);
            afterBalanceController.setLeftPadding(leftPadding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAfterTokenBalance(){
        if(afterTokenValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_only_value.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            afterTokenValueController = (ReceiptOnlyValueController) fxmlController.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTokenAmount(){
        if(tokenAmountValueController != null){
            return;
        }

        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_only_value.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            tokenAmountValueController = (ReceiptOnlyValueController) fxmlController.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAddress(String address){
        this.address = address;
        update();
    }

    public void setBeneficiaryAddress(String beneficiaryAddress){
        this.beneficiaryAddress = beneficiaryAddress;
        update();
    }

    public void setPayerAddress(String payerAddress){
        this.payerAddress = payerAddress;
        update();
    }

    public void setMask(String mask) {
        this.mask = mask;
        update();
    }

    public void setAmount(String amount) {
        this.amount = amount;
        update();
    }

    public void setChargedAmount(String chargedAmount) {
        this.chargedAmount = chargedAmount;
        update();
    }

    public void setFee(String fee){
        this.fee = fee;
        update();
    }

    public void setMineral(String mineral){
        this.mineral = mineral;
        update();
    }

    public void setChargedFee(String chargedFee){
        this.chargedFee = chargedFee;
        update();
    }

    public void setAfterBalance(String afterBalance){
        this.afterBalance = afterBalance;
        update();
    }

    public void setAfterTokenBalance(String afterTokenBalance){
        this.afterTokenBalance = afterTokenBalance;
        update();
    }

    public void setTokenSymbol(String symbol){
        if(tokenAmountValueController != null){
            this.tokenAmountValueController.setSymbol(symbol);
        }

        if(afterTokenValueController != null){
            this.afterTokenValueController.setSymbol(symbol);
        }
    }


    public void setTokenAmount(String tokenAmount){
        this.tokenAmount = tokenAmount;
        update();
    }


    public void setSuccessed(boolean isSuccessed){
        this.isSuccessed = isSuccessed;

        if(isSuccessed){
            StyleManager.backgroundColorStyle(button, StyleManager.AColor.Cb01e1e);
        }else{
            StyleManager.backgroundColorStyle(button, StyleManager.AColor.Cd8d8d8);
        }
    }

    public void setVisible(boolean isVisible) {
        rootPane.setVisible(isVisible);
    }

    public void setVisibleNoFees(boolean isVisible){
        dimNoFees.setVisible(isVisible);
        receipt.setVisible(!isVisible);
    }
    public void setVisibleTransferButton(boolean isVisible){
        buttonPane.setVisible(isVisible);
        buttonPane.setPrefHeight((isVisible) ? -1 : 0);
    }

    private ReceiptImpl handler;
    public void setHandler(ReceiptImpl handler){ this.handler = handler; }
    public interface ReceiptImpl{ void send(); }
}
