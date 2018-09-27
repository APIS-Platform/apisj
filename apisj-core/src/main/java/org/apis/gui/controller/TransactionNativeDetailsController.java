package org.apis.gui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class TransactionNativeDetailsController implements Initializable {
    @FXML
    private VBox detailsList;
    @FXML
    private Label copy, txHashLabel;
    // Multilingual Support Label
    @FXML
    private Label transactionDetailsLabel, hashLabel;

    private String nonceValue, blockValue, blockConfirmValue, timeValue, confirmedInValue, fromValue, toValue = "", contractAddrValue = "",
                   valueValue, feeValue, mineralValue, chargedFeeValue, gasPriceValue, gasLimitValue, gasUsedValue, errorValue;
    private SimpleStringProperty blockConfirmUnit = new SimpleStringProperty("");
    private SimpleStringProperty confirmedInUnit = new SimpleStringProperty("");
    private TransactionNativeDetailsImpl handler;
    private ArrayList<TransactionNativeDetailsContentsController> contentsControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Underline Setting
        copy.setOnMouseEntered(event -> txHashLabel.setUnderline(true));
        copy.setOnMouseExited(event -> txHashLabel.setUnderline(false));

        this.init();
    }

    public void init() {
        // Details List Setting
        setDetailsList();
    }

    public void languageSetting() {
        transactionDetailsLabel.textProperty().bind(StringManager.getInstance().transaction.detailsLabel);
        hashLabel.textProperty().bind(StringManager.getInstance().transaction.detailsHashLabel);
        blockConfirmUnit.bind(StringManager.getInstance().transaction.detailsBlockConfirmLabel);
        confirmedInUnit.bind(StringManager.getInstance().transaction.detailsConfirmedInUnit);
    }

    public void setDetailsList() {
        detailsList.getChildren().clear();
        contentsControllers.clear();
        // Add Contents
        addDetailsContents("Nonce");
        addDetailsContents("Block");
        addDetailsContents("Time");
        addDetailsContents("ConfirmedIn");
        addDetailsContents("From");
        addDetailsContents("To");
        addDetailsContents("ContractAddr");
        addDetailsContents("Value");
        addDetailsContents("ChargedFee");
        addDetailsContents("Fee");
        addDetailsContents("Mineral");
        addDetailsContents("GasPriceLimitUsed");
        addDetailsContents("Error");

        // Set Background color
        for(int i=0; i<contentsControllers.size(); i++) {
            if(i % 2 == 0) {
                contentsControllers.get(i).setBgColor("transparent");
            } else {
                contentsControllers.get(i).setBgColor("#f2f2f2");
            }
        }
    }

    public void addDetailsContents(String contentsHeader) {
        try {
            URL labelURL = getClass().getClassLoader().getResource("scene/transaction_native_details_contents.fxml");
            FXMLLoader loader = new FXMLLoader(labelURL);
            AnchorPane item = loader.load();
            detailsList.getChildren().add(item);

            TransactionNativeDetailsContentsController itemController = (TransactionNativeDetailsContentsController)loader.getController();
            contentsControllers.add(itemController);
            String contentsBody = "";
            String mask = null;
            switch(contentsHeader) {
                case "Nonce" :
                    itemController.setTxtColor("#910000");
                    contentsBody = nonceValue;
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsNonceLabel);
                    break;
                case "Block" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = blockValue + " | " + blockConfirmValue + " " + blockConfirmUnit.get();
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.blockLabel);
                    break;
                case "Time" :
                    itemController.setTxtColor("#2b2b2b");
                    //contentsBody = timeValue;
                    contentsBody = "##2018.07.06 14:07:03 (22 minutes ago)##";
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.timeLabel);
                    break;
                case "ConfirmedIn" :
                    itemController.setTxtColor("#2b2b2b");
                    //contentsBody = confirmedInValue;
                    contentsBody = "##116.289## " + confirmedInUnit.get();
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsConfirmedInLabel);
                    break;
                case "From" :
                    itemController.setTxtColor("#910000");
                    contentsBody = fromValue;
                    mask = AppManager.getInstance().getMaskWithAddress(fromValue);
                    if(mask != null && mask.length() > 0){
                        contentsBody = contentsBody + " ("+mask+")";
                    }
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.fromLabel);
                    break;
                case "To" :
                    itemController.setTxtColor("#910000");
                    contentsBody = toValue;
                    mask = AppManager.getInstance().getMaskWithAddress(toValue);
                    if(mask != null && mask.length() > 0){
                        contentsBody = contentsBody + " ("+mask+")";
                    }
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.toLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;
                case "ContractAddr" :
                    itemController.setTxtColor("#910000");
                    contentsBody = contractAddrValue;
                    mask = AppManager.getInstance().getMaskWithAddress(contractAddrValue);
                    if(mask != null && mask.length() > 0){
                        contentsBody = contentsBody + " ("+mask+")";
                    }
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsContractAddrLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;
                case "Value" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = valueValue + " APIS";
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.valueLabel);
                    break;
                case "ChargedFee" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = chargedFeeValue + " APIS ";
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsChargedFeeLabel);
                    break;
                case "Fee" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = feeValue + " APIS";
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.feeLabel);
                    break;
                case "Mineral" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = mineralValue + " MNR";
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsMineralLabel);
                    break;
                case "GasPriceLimitUsed" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = gasPriceValue + " nAPIS / " + gasLimitValue + " / " + gasUsedValue;
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsGasLabel);
                    break;
                case "Error" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = errorValue;
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsErrorLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;
                default :
                    break;
            }
            itemController.setContentsBody(contentsBody);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            PopupCopyTxHashController controller = (PopupCopyTxHashController)PopupManager.getInstance().showMainPopup("popup_copy_tx_hash.fxml", 0);
            controller.setHash(text);
        }
    }

    public void setHandler(TransactionNativeDetailsImpl handler) {
        this.handler = handler;
    }

    public void setChargedFee(String chargedFeeValue) {
        this.chargedFeeValue = chargedFeeValue;
    }

    public interface TransactionNativeDetailsImpl {
        void hideDetails();
    }

    public void setTxHashLabel(String txHashLabel) {
        this.txHashLabel.setText(txHashLabel);
    }

    public void setBlockValue(long blockValue) {
        this.blockValue = AppManager.comma(Long.toString(blockValue));
    }

    public void setBlockConfirm(long blockConfirm) {
        this.blockConfirmValue = AppManager.comma(Long.toString(blockConfirm));
    }

    public void setFrom(String from) {
        this.fromValue = from;
    }

    public void setTo(String to) {
        this.toValue = to;

    }

    public void setValue(String value) {
        this.valueValue = value;
    }

    public void setFee(String fee) {
        this.feeValue = fee;
    }

    public void setMineral(String mineral) {
        this.mineralValue = mineral;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPriceValue = gasPrice;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimitValue = AppManager.comma(Long.toString(gasLimit));
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsedValue = AppManager.comma(Long.toString(gasUsed));
    }

    public void setNonce(Long nonce) {
        this.nonceValue = Long.toString(nonce);
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddrValue = contractAddr;
    }

    public void setError(String error) {
        this.errorValue = error;
    }
}
