package org.apis.gui.controller.transaction;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.AddressUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionNativeDetailsController extends BaseViewController {
    @FXML private VBox detailsList;
    @FXML private Label copy, txHashLabel;
    @FXML private ScrollPane bodyScrollPane;
    @FXML private AnchorPane bodyScrollPaneContentPane;
    @FXML private GridPane hashPane;

    // Multilingual Support Label
    @FXML
    private Label hashLabel, back;

    private String nonceValue, blockValue, blockConfirmValue, timeValue, confirmedInValue, originalData, fromValue, toValue = "", contractAddrValue = "",
                   valueValue, feeValue, mineralValue, chargedFeeValue, gasPriceValue, gasLimitValue, gasUsedValue, inputData, eventLogs, errorValue;
    private String[] tokenFromValue, tokenToValue, tokenValueValue, internalTxFromValue, internalTxToValue, internalTxValue;
    private SimpleStringProperty blockConfirmUnit = new SimpleStringProperty("");
    private SimpleStringProperty confirmedInUnit = new SimpleStringProperty("");
    private TransactionNativeDetailsImpl handler;
    private ArrayList<TransactionNativeDetailsContentsController> contentsControllers = new ArrayList<>();

    private boolean isScrolling;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Underline Setting
        hashPane.setOnMouseClicked(event -> {
            PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(null, "popup_copy.fxml", 0);
            controller.setCopyTxHash(txHashLabel.getText());
        });
        hashPane.setOnMouseEntered(event -> { txHashLabel.setUnderline(true); copy.setVisible(true); });
        hashPane.setOnMouseExited(event -> { txHashLabel.setUnderline(false); copy.setVisible(false); });

        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = bodyScrollPaneContentPane.getHeight() - bodyScrollPane.getHeight();

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

        this.init();
    }

    public void init() {
        // Details List Setting
        setDetailsList();
    }

    public void languageSetting() {
        hashLabel.textProperty().bind(StringManager.getInstance().transaction.detailsHashLabel);
        blockConfirmUnit.bind(StringManager.getInstance().transaction.detailsBlockConfirmLabel);
        confirmedInUnit.bind(StringManager.getInstance().transaction.detailsConfirmedInUnit);
        back.textProperty().bind(StringManager.getInstance().common.backButton);
    }

    public void setDetailsList() {
        detailsList.getChildren().clear();
        contentsControllers.clear();
        // Add Contents
        addDetailsContents("Nonce");
        addDetailsContents("Block");
        addDetailsContents("Time");
        addDetailsContents("From");
        addDetailsContents("To");
        addDetailsContents("ContractAddr");
        addDetailsContents("InternalTx");
        addDetailsContents("TokensTransfered");
        addDetailsContents("Value");
        addDetailsContents("ChargedFee");
        addDetailsContents("Fee");
        addDetailsContents("Mineral");
        addDetailsContents("GasPriceLimitUsed");
        addDetailsContents("EventLogs");
        addDetailsContents("Data");
        addDetailsContents("Error");

        // Set Background color
        for(int i=0; i<contentsControllers.size(); i++) {
            if(i % 2 == 0) {
                contentsControllers.get(i).setBgColor("transparent");
            } else {
                contentsControllers.get(i).setBgColor("#f8f8fb");
            }
        }
    }

    public void addDetailsContents(String contentsHeader) {
        try {
            URL labelURL = getClass().getClassLoader().getResource("scene/transaction/transaction_native_details_contents.fxml");
            FXMLLoader loader = new FXMLLoader(labelURL);
            AnchorPane item = loader.load();
            detailsList.getChildren().add(item);

            TransactionNativeDetailsContentsController itemController = (TransactionNativeDetailsContentsController)loader.getController();
            contentsControllers.add(itemController);
            String contentsBody = "";
            String mask = null;
            switch(contentsHeader) {
                case "Nonce" :
                    itemController.setTxtColor("#b01e1e");
                    contentsBody = nonceValue;
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsNonceLabel);
                    break;

                case "Block" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = blockValue + " | " + blockConfirmValue + " " + blockConfirmUnit.get();
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.blockLabel);
                    break;

                case "Time" :
                    itemController.setTxtColor("#2b2b2b");
                    //contentsBody = timeValue;
                    contentsBody = timeValue;
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.timeLabel);
                    break;

                case "ConfirmedIn" :
                    itemController.setTxtColor("#2b2b2b");
                    //contentsBody = confirmedInValue;
                    contentsBody = "##116.289## " + confirmedInUnit.get();
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsConfirmedInLabel);
                    break;

                case "From" :
                    itemController.setTxtColor("#b01e1e");
                    itemController.setOnClickCopyText(true,fromValue);
                    contentsBody = fromValue;
                    if(contentsBody != null && !contentsBody.equals("")) {
                        if (AppManager.getInstance().isFrozen(contentsBody)) {
                            itemController.addFrozen();
                        }
                    }
                    mask = AppManager.getInstance().getMaskWithAddress(fromValue);
                    if(mask != null && mask.length() > 0){
                        contentsBody = contentsBody + " ("+mask+")";
                    }
                    itemController.setContentsBody(contentsBody, "'Roboto Mono'");
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.fromLabel);
                    break;

                case "To" :
                    itemController.setTxtColor("#b01e1e");
                    itemController.setOnClickCopyText(true,toValue);
                    contentsBody = toValue;
                    if(contentsBody != null && !contentsBody.equals("")) {
                        if (AppManager.getInstance().isFrozen(contentsBody)) {
                            itemController.addFrozen();
                        }
                    }
                    mask = AppManager.getInstance().getMaskWithAddress(toValue);
                    if(mask != null && mask.length() > 0){
                        contentsBody = contentsBody + " ("+mask+")";
                    }
                    itemController.setContentsBody(contentsBody, "'Roboto Mono'");
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.toLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;

                case "ContractAddr" :
                    itemController.setTxtColor("#b01e1e");
                    itemController.setOnClickCopyText(true,contractAddrValue);
                    contentsBody = contractAddrValue;
                    mask = AppManager.getInstance().getMaskWithAddress(contractAddrValue);
                    if(mask != null && mask.length() > 0){
                        contentsBody = contentsBody + " ("+mask+")";
                    }
                    itemController.setContentsBody(contentsBody, "'Roboto Mono'");
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsContractAddrLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;

                case "InternalTx" :
                    if(internalTxValue != null) {
                        itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsInternalTxLabel);
                        itemController.contentsBodyVBoxClear();
                        for(int i = 0; i < internalTxValue.length; i++) {
                            itemController.contentsBodyVBoxAdd("internalTx", internalTxFromValue[i], internalTxToValue[i], internalTxValue[i]);
                        }

                    } else {
                        detailsList.getChildren().remove(detailsList.getChildren().size() - 1);
                        contentsControllers.remove(itemController);
                    }
                    internalTxValue = null;
                    break;

                case "TokensTransfered" :
                    if(tokenValueValue != null) {
                        itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsTokenTransferedLabel);
                        itemController.contentsBodyVBoxClear();
                        for(int i = 0; i < tokenValueValue.length; i++) {
                            itemController.contentsBodyVBoxAdd("token", tokenFromValue[i], tokenToValue[i], tokenValueValue[i]);
                        }

                    } else {
                        detailsList.getChildren().remove(detailsList.getChildren().size() - 1);
                        contentsControllers.remove(itemController);
                    }
                    tokenValueValue = null;
                    break;

                case "Value" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = valueValue + " APIS";
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.valueLabel);
                    break;

                case "ChargedFee" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = chargedFeeValue + " APIS ";
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsChargedFeeLabel);
                    break;

                case "Fee" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = feeValue + " APIS";
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.feeLabel);
                    break;

                case "Mineral" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = mineralValue + " MNR";
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsMineralLabel);
                    break;

                case "GasPriceLimitUsed" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = gasPriceValue + " nAPIS / " + gasLimitValue + " / " + gasUsedValue;
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsGasLabel);
                    break;

                case "EventLogs" :
                    itemController.setTxtColor("#2b2b2b");
                    itemController.setTextAreaType(160);
                    contentsBody = eventLogs;
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsEventLogsLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;

                case "Error" :
                    itemController.setTxtColor("#2b2b2b");
                    contentsBody = errorValue;
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsErrorLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;

                case "Data" :
                    itemController.setTxtColor("#2b2b2b");
                    itemController.setTextAreaType(80);
                    contentsBody = originalData;
                    itemController.setContentsBody(contentsBody);
                    itemController.bindContentsHeader(StringManager.getInstance().transaction.detailsDataLabel);
                    if(contentsBody == null || contentsBody.length() == 0) {
                        detailsList.getChildren().remove(detailsList.getChildren().size()-1);
                        contentsControllers.remove(itemController);
                    }
                    break;

                default :
                    break;
            }

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
            String txHash = txHashLabel.getText();
            PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(null, "popup_copy.fxml", 0);
            controller.setCopyTxHash(txHash);
        }
    }

    public void setHandler(TransactionNativeDetailsImpl handler) {
        this.handler = handler;
    }

    public void setChargedFee(String chargedFeeValue) {
        this.chargedFeeValue = chargedFeeValue;
    }

    public void setTime(String time) {
        this.timeValue = time;
    }

    public void setOriginalData(String originalData) {
        this.originalData = originalData;
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

    public void setInternalTxFromValue(String[] internalTxFrom) {
        this.internalTxFromValue = internalTxFrom;
    }

    public void setInternalTxToValue(String[] internalTxTo) {
        this.internalTxToValue = internalTxTo;
    }

    public void setInternalTxValue(String[] internalTxValue) {
        this.internalTxValue = internalTxValue;
    }
    public void setTokenFrom(String[] tokenFrom) {
        this.tokenFromValue = tokenFrom;
    }

    public void setTokenToValue(String[] tokenTo) {
        this.tokenToValue = tokenTo;
    }

    public void setTokenValueValue(String[] tokenValueString) {
        this.tokenValueValue = tokenValueString;
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

    public void setInputData(String inputData) { this.inputData = inputData; }

    public void setEventLogs(String eventLogs) { this.eventLogs = eventLogs; }

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
