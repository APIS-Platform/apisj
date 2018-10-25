package org.apis.gui.controller.smartcontrect;

import com.google.zxing.WriterException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apis.core.CallTransaction;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.module.*;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupContractReadWriteSelectController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.GUIContractManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.ContractModel;
import org.apis.solidity.SolidityType;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SmartContractController extends BaseViewController {

    private final int TAB_DEPLOY = 0;
    private final int TAB_CALL_SEND = 1;
    private final int TAB_CONTRACT_FREEZER = 2;
    private final int TAB_CONTRACT_UPDATER = 3;
    private final int TAB_CANVAS = 4;
    private int selectedTabIndex = 0;

    @FXML private AnchorPane tabLeftDeploy, tabLeftCallSend, tabLeftFreezer, tabLeftUpdater, tabLeftCanvas;
    @FXML private Label tabTitle;

    @FXML private TabMenuController tabMenuController;

    @FXML private SmartContractDeployController smartContractDeployController;
    @FXML private SmartContractCallSendController smartContractCallSendController;
    @FXML private SmartContractFreezerController smartContractFreezerController;
    @FXML private SmartContractUpdaterController smartContractUpdaterController;
    @FXML private SmartContractCanvasController smartContractCanvasController;

    @FXML private SmartContractReceiptController receiptController;

    private boolean isMyAddressSelected1 = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setSmartContract(this);
        settingLayoutData();
        // Multilingual Support
        languageSetting();

        receiptController.setHandler(new SmartContractReceiptController.SmartContractReceiptImpl() {
            @Override
            public void onMouseClickTransfer() {
            }
        });

        tabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                initStyleTab(index);
            }
        });

        tabMenuController.selectedMenu(TAB_DEPLOY);
    }

    public void languageSetting() {
        tabTitle.textProperty().bind(StringManager.getInstance().smartContract.tabTitle);

        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel1, TAB_DEPLOY);
        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel2, TAB_CALL_SEND);
        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel3, TAB_CONTRACT_FREEZER);
        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel4, TAB_CONTRACT_UPDATER);
        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel5, TAB_CANVAS);
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
    }

    @FXML
    public void onMouseExited(InputEvent event){

    }

    @FXML
    public void onMouseEntered(InputEvent event){

    }

    public void update(){
        smartContractDeployController.update();
        smartContractCallSendController.update();

        settingLayoutData();
    }

    public void settingLayoutData() {

        // amount to send
        BigInteger amount = BigInteger.ZERO;

        // mineral
        BigInteger balance = BigInteger.ZERO;
        BigInteger mineral = BigInteger.ZERO;
        BigInteger totalFee = BigInteger.ZERO;
        if(selectedTabIndex == TAB_DEPLOY) {

        }else if(selectedTabIndex == TAB_CALL_SEND) {

        } else if(selectedTabIndex == TAB_CONTRACT_FREEZER) {

        } else if(selectedTabIndex == TAB_CONTRACT_UPDATER) {

        } else if(selectedTabIndex == TAB_CANVAS) {

        }

        // total fee
        if(totalFee.toString().indexOf("-") >= 0){
            totalFee = BigInteger.ZERO;
        }

        // total amount
        BigInteger totalAmount = amount.add(totalFee);
        String[] totalAmountSplit = AppManager.addDotWidthIndex(totalAmount.toString()).split("\\.");

        //after balance
        BigInteger afterBalance = balance.subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        receiptController.setTotalAmount(totalAmountSplit[0], "." + totalAmountSplit[1]);
        receiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        receiptController.setFee(ApisUtil.readableApis(totalFee,',',true));
        receiptController.setWithdrawal(ApisUtil.readableApis(totalAmount,',',true));
        receiptController.setAfterBalance(ApisUtil.readableApis(afterBalance,',',true));

        // 트랜스퍼 버튼 활성화/비활성화 체크
    }

    public void initStyleTab(int index) {
        this.selectedTabIndex = index;
        initStyleTabClean();

        if(index == TAB_DEPLOY) {
            this.tabLeftDeploy.setVisible(true);
            this.tabLeftDeploy.setPrefHeight(-1);
            this.receiptController.hideNoFees();

            //button
            this.receiptController.setVisibleTransferButton(true);

        } else if(index == TAB_CALL_SEND) {
            this.tabLeftCallSend.setVisible(true);
            this.tabLeftCallSend.setPrefHeight(-1);
            this.receiptController.hideNoFees();

        } else if(index == TAB_CONTRACT_FREEZER) {
            this.tabLeftFreezer.setVisible(true);
            tabLeftFreezer.setPrefHeight(-1);
            this.receiptController.hideNoFees();

            //button
            this.receiptController.setVisibleTransferButton(true);

        } else if(index == TAB_CONTRACT_UPDATER) {
            this.tabLeftUpdater.setVisible(true);
            this.tabLeftUpdater.setPrefHeight(-1);
            this.receiptController.hideNoFees();

            this.receiptController.setVisibleTransferButton(true);


        } else if(index == TAB_CANVAS) {
            this.tabLeftCanvas.setVisible(true);
            tabLeftCanvas.setPrefHeight(-1);
            this.receiptController.hideNoFees();

            this.receiptController.setVisibleTransferButton(true);

        }

        settingLayoutData();
    }

    public void initStyleTabClean() {
        tabLeftDeploy.setVisible(false);
        tabLeftCallSend.setVisible(false);
        tabLeftFreezer.setVisible(false);
        tabLeftUpdater.setVisible(false);
        tabLeftCanvas.setVisible(false);

        tabLeftDeploy.setPrefHeight(0);
        tabLeftCallSend.setPrefHeight(0);
        tabLeftFreezer.setPrefHeight(0);
        tabLeftUpdater.setPrefHeight(0);
        tabLeftCanvas.setPrefHeight(0);

        receiptController.hideNoFees();

    }



}
