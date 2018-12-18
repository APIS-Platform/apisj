package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptController extends BaseViewController {

    @FXML private GridPane button;
    @FXML private VBox itemList;
    @FXML private Label title, buttonLabel;


    private ReceiptAddressController addressController;
    private ReceiptAddressController beneficiaryAddressController;
    private ReceiptAddressController payerAddressController;
    private ReceiptItemController amountItemController;
    private ReceiptItemController maskItemController;
    private ReceiptItemController totalFeeItemController;
    private String address, beneficiaryAddress, payerAddress, amount, mask, totalFee, fee;
    private boolean isSuccessed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

        if(amountItemController != null){
            amountItemController.setValue(amount);
        }

        if(maskItemController != null){
            maskItemController.setValue(mask);
        }

        if(totalFeeItemController != null){
            totalFeeItemController.setValue(totalFee);
        }
    }

    public void setTitle(SimpleStringProperty title){
        this.title.textProperty().unbind();
        this.title.textProperty().bind(title);
    }

    public void setButtonTitle(SimpleStringProperty btn){
        this.buttonLabel.textProperty().unbind();
        this.buttonLabel.textProperty().bind(btn);
    }

    public void addAddress(){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_address.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            addressController = (ReceiptAddressController)fxmlController.getController();
            addressController.setTitle(StringManager.getInstance().receipt.address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addBeneficiaryAddress(){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_address.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            beneficiaryAddressController = (ReceiptAddressController)fxmlController.getController();
            beneficiaryAddressController.setTitle(StringManager.getInstance().buymineral.titleLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPayerAddress(){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_address.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            payerAddressController = (ReceiptAddressController)fxmlController.getController();
            payerAddressController.setTitle(StringManager.getInstance().receipt.payer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAmount(){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_item.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            amountItemController = (ReceiptItemController) fxmlController.getController();
            amountItemController.setTitle(StringManager.getInstance().receipt.amount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMaskAddress(){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_item.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            maskItemController = (ReceiptItemController) fxmlController.getController();
            maskItemController.setTitle(StringManager.getInstance().receipt.mask);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTotalFee(){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("module/receipt/receipt_item.fxml");
            itemList.getChildren().add(fxmlController.getNode());
            totalFeeItemController = (ReceiptItemController) fxmlController.getController();
            totalFeeItemController.setTitle(StringManager.getInstance().receipt.totalFee);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setTotalFee(String totalFee){
        this.totalFee = totalFee;
        update();
    }

    public void setFee(String fee){
        this.fee = fee;
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

    private ReceiptImpl handler;
    public void setHandler(ReceiptImpl handler){ this.handler = handler; }

    public interface ReceiptImpl{ void send(); }
}
