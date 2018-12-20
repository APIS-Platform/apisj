package org.apis.gui.controller.smartcontrect;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.module.*;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.receipt.ReceiptController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractController extends BaseViewController {

    private final int TAB_DEPLOY = 0;
    private final int TAB_CALL_SEND = 1;
    private final int TAB_CONTRACT_UPDATER = 2;
    private final int TAB_CANVAS = 3;
    private int selectedTabIndex = 0;

    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane scrollGridContent;
    @FXML private AnchorPane bodyScrollPaneParent, tabLeftDeploy, tabLeftCallSend, tabLeftFreezer, tabLeftUpdater, tabLeftCanvas;
    @FXML private Label tabTitle;

    @FXML private TabMenuController tabMenuController;

    @FXML private SmartContractDeployController smartContractDeployController;
    @FXML private SmartContractCallSendController smartContractCallSendController;
    @FXML private SmartContractUpdaterController smartContractUpdaterController;
    @FXML private SmartContractCanvasController smartContractCanvasController;

    @FXML private ReceiptController deployReceiptController;
    @FXML private ReceiptController callSendReceiptController;
    @FXML private ReceiptController updaterReceiptController;
    @FXML private ReceiptController canvasReceiptController;

    private boolean isMyAddressSelected1 = true;
    private boolean isScrolling = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setSmartContract(this);
        languageSetting();

        initializeDeployReceipt();
        initializeCallSendReceipt();
        initializeUpdateReceipt();
        initializeCanvasReceipt();

        initializeScrollSpeed();

        // handler init
        tabMenuController.setHandler(tabMenuImpl);
        tabMenuController.setFontSize14(16);
        smartContractDeployController.setHandler(smartContractDeployImpl);
        smartContractCallSendController.setHandler(smartContractCallSendImpl);
        smartContractUpdaterController.setHandler(smartContractUpdaterImpl);
        smartContractCanvasController.setHandler(smartContractCanvasImpl);

        deployReceiptController.setVisibleNoFees(false);
        callSendReceiptController.setVisibleNoFees(true);
        updaterReceiptController.setVisibleNoFees(false);
        canvasReceiptController.setVisibleNoFees(false);

        // setting init
        tabMenuController.selectedMenu(TAB_DEPLOY);
        settingLayoutData();
    }

    public void languageSetting() {
        tabTitle.textProperty().bind(StringManager.getInstance().smartContract.tabTitle);

        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel1, TAB_DEPLOY);
        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel2, TAB_CALL_SEND);
        tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel3, TAB_CONTRACT_UPDATER);
        //tabMenuController.addItem(StringManager.getInstance().smartContract.tabLabel4, TAB_CANVAS);
    }

    private void initializeScrollSpeed(){
        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = scrollGridContent.getHeight() - bodyScrollPaneParent.getHeight();

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

    public void initializeDeployReceipt(){

        deployReceiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                smartContractDeployController.sendTransfer();
            }
        });

        deployReceiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        deployReceiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);

        deployReceiptController.addAmount(0);
        deployReceiptController.addVSpace(16);
        deployReceiptController.addLineStyleDotted();
        deployReceiptController.addVSpace(16);
        deployReceiptController.addChargedFee(0);
        deployReceiptController.addFee(16);
        deployReceiptController.addMineral(16);
        deployReceiptController.addVSpace(16);
        deployReceiptController.addLineStyleDotted();
        deployReceiptController.addVSpace(16);
        deployReceiptController.addChargedAmount(0);
        deployReceiptController.addVSpace(16);
        deployReceiptController.addLineStyleDotted();
        deployReceiptController.addVSpace(16);
        deployReceiptController.addAfterBalance(0);
        deployReceiptController.setSuccessed(false);
    }

    public void initializeCallSendReceipt(){

        callSendReceiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        callSendReceiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);

        callSendReceiptController.addAmount(0);
        callSendReceiptController.addVSpace(16);
        callSendReceiptController.addLineStyleDotted();
        callSendReceiptController.addVSpace(16);
        callSendReceiptController.addChargedFee(0);
        callSendReceiptController.addFee(16);
        callSendReceiptController.addMineral(16);
        callSendReceiptController.addVSpace(16);
        callSendReceiptController.addLineStyleDotted();
        callSendReceiptController.addVSpace(16);
        callSendReceiptController.addChargedAmount(0);
        callSendReceiptController.addVSpace(16);
        callSendReceiptController.addLineStyleDotted();
        callSendReceiptController.addVSpace(16);
        callSendReceiptController.addAfterBalance(0);
        callSendReceiptController.setSuccessed(false);


    }

    public void initializeUpdateReceipt(){

        updaterReceiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                smartContractUpdaterController.sendTransfer();
            }
        });

        updaterReceiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        updaterReceiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);

        updaterReceiptController.addAmount(0);
        updaterReceiptController.addVSpace(16);
        updaterReceiptController.addLineStyleDotted();
        updaterReceiptController.addVSpace(16);
        updaterReceiptController.addChargedFee(0);
        updaterReceiptController.addFee(16);
        updaterReceiptController.addMineral(16);
        updaterReceiptController.addVSpace(16);
        updaterReceiptController.addLineStyleDotted();
        updaterReceiptController.addVSpace(16);
        updaterReceiptController.addChargedAmount(0);
        updaterReceiptController.addVSpace(16);
        updaterReceiptController.addLineStyleDotted();
        updaterReceiptController.addVSpace(16);
        updaterReceiptController.addAfterBalance(0);
        updaterReceiptController.setSuccessed(false);
    }

    public void initializeCanvasReceipt(){

        canvasReceiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
            }
        });
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
        smartContractUpdaterController.update();
        smartContractCanvasController.update();

        settingLayoutData();
    }

    public void settingLayoutData() {
        switch (selectedTabIndex){
            case TAB_DEPLOY:
                settingLayoutDataDeploy();
                break;
            case TAB_CALL_SEND:
                settingLayoutDataCallSend();
                break;
            case TAB_CONTRACT_UPDATER:
                settingLayoutDataUpdater();
                break;
            case TAB_CANVAS:
                settingLayoutDataCanvas();
                break;
        }
    }

    public void settingLayoutDataDeploy(){

        BigInteger amount = smartContractDeployController.getAmount();
        BigInteger fee = smartContractDeployController.getFee();
        BigInteger mineral = smartContractDeployController.getMineral();
        BigInteger chargedFee = smartContractDeployController.getChargedFee();
        BigInteger chargedAmount = smartContractDeployController.getChargedAmount();
        BigInteger afterBalance = smartContractDeployController.getAfterBalance();

        // charged fee
        chargedFee = (chargedFee.compareTo(BigInteger.ZERO) >=0 ) ? chargedFee : BigInteger.ZERO;

        //after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        deployReceiptController.setTitleValue(chargedAmount);
        deployReceiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        deployReceiptController.setChargedFee(ApisUtil.readableApis(chargedFee,',',true));
        deployReceiptController.setFee(ApisUtil.readableApis(fee,',',true));
        deployReceiptController.setMineral(ApisUtil.readableApis(mineral,',',true));
        deployReceiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount,',',true));
        deployReceiptController.setAfterBalance(ApisUtil.readableApis(afterBalance,',',true));
        deployReceiptController.setSuccessed(smartContractDeployController.isReadyTransfer());

    }
    public void settingLayoutDataCallSend(){

        BigInteger amount = smartContractCallSendController.getAmount();
        BigInteger fee = smartContractCallSendController.getFee();
        BigInteger mineral = smartContractCallSendController.getMineral();
        BigInteger chargedFee = smartContractCallSendController.getChargedFee();
        BigInteger chargedAmount = smartContractCallSendController.getChargedAmount();
        BigInteger afterBalance = smartContractCallSendController.getAfterBalance();

        // charged fee
        if(chargedFee.toString().indexOf("-") >= 0){
            chargedFee = BigInteger.ZERO;
        }
        //after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        callSendReceiptController.setTitleValue(chargedAmount);
        callSendReceiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        callSendReceiptController.setChargedFee(ApisUtil.readableApis(chargedFee,',',true));
        callSendReceiptController.setFee(ApisUtil.readableApis(fee,',',true));
        callSendReceiptController.setMineral(ApisUtil.readableApis(mineral,',',true));
        callSendReceiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount,',',true));
        callSendReceiptController.setAfterBalance(ApisUtil.readableApis(afterBalance,',',true));

    }
    public void settingLayoutDataUpdater(){

        BigInteger amount = smartContractUpdaterController.getAmount();
        BigInteger fee = smartContractUpdaterController.getFee();
        BigInteger mineral = smartContractUpdaterController.getMineral();
        BigInteger chargedFee = smartContractUpdaterController.getChargedFee();
        BigInteger chargedAmount = smartContractUpdaterController.getChargedAmount();
        BigInteger afterBalance = smartContractUpdaterController.getAfterBalance();

        // charged fee
        if(chargedFee.toString().indexOf("-") >= 0){
            chargedFee = BigInteger.ZERO;
        }
        //after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;


        updaterReceiptController.setTitleValue(chargedAmount);
        updaterReceiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        updaterReceiptController.setChargedFee(ApisUtil.readableApis(chargedFee,',',true));
        updaterReceiptController.setFee(ApisUtil.readableApis(fee,',',true));
        updaterReceiptController.setMineral(ApisUtil.readableApis(mineral,',',true));
        updaterReceiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount,',',true));
        updaterReceiptController.setAfterBalance(ApisUtil.readableApis(afterBalance,',',true));

    }
    public void settingLayoutDataCanvas(){

    }

    public void initStyleTab(int index) {
        this.selectedTabIndex = index;
        initStyleTabClean();
        bodyScrollPane.setVvalue(0);


        this.deployReceiptController.setVisible(false);
        this.callSendReceiptController.setVisible(false);
        this.updaterReceiptController.setVisible(false);
        this.canvasReceiptController.setVisible(false);
        if(index == TAB_DEPLOY) {
            this.tabLeftDeploy.setVisible(true);
            this.tabLeftDeploy.setPrefHeight(-1);

            this.deployReceiptController.setVisible(true);
            this.deployReceiptController.setVisibleTransferButton(true);

        } else if(index == TAB_CALL_SEND) {
            this.tabLeftCallSend.setVisible(true);
            this.tabLeftCallSend.setPrefHeight(-1);

            this.callSendReceiptController.setVisible(true);
            this.callSendReceiptController.setVisibleTransferButton(false);
            this.callSendReceiptController.setVisibleNoFees(this.smartContractCallSendController.isReadMethod());

        } else if(index == TAB_CONTRACT_UPDATER) {
            this.tabLeftUpdater.setVisible(true);
            this.tabLeftUpdater.setPrefHeight(-1);

            this.updaterReceiptController.setVisible(true);
            this.updaterReceiptController.setVisibleTransferButton(true);


        } else if(index == TAB_CANVAS) {
            this.tabLeftCanvas.setVisible(true);
            this.tabLeftCanvas.setPrefHeight(-1);
            this.updaterReceiptController.setVisibleTransferButton(true);
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

        //receiptController.hideNoFees();


    }


    private TabMenuController.TabMenuImpl tabMenuImpl = new TabMenuController.TabMenuImpl() {
        @Override
        public void onMouseClicked(String text, int index) {
            initStyleTab(index);
        }
    };
    private SmartContractDeployController.SmartContractDeployImpl smartContractDeployImpl = new SmartContractDeployController.SmartContractDeployImpl() {
        @Override
        public void onAction() {
            settingLayoutData();
        }
    };
    private SmartContractCallSendController.SmartContractCallSendImpl smartContractCallSendImpl = new SmartContractCallSendController.SmartContractCallSendImpl() {
        @Override
        public void onAction() {
            settingLayoutData();
        }
        @Override
        public void isReadMethod(boolean isReadMethod){
            System.out.println("isReadMethod : "+isReadMethod);

            callSendReceiptController.setVisibleNoFees(isReadMethod);
        }
    };
    private SmartContractFreezerController.SmartContractFreezerImpl smartContractFreezerImpl = new SmartContractFreezerController.SmartContractFreezerImpl() {
        @Override
        public void onAction() {
            settingLayoutData();
        }
    };
    private SmartContractUpdaterController.SmartContractUpdaterImpl smartContractUpdaterImpl = new SmartContractUpdaterController.SmartContractUpdaterImpl() {
        @Override
        public void onAction() {
            settingLayoutData();
        }
    };
    private SmartContractCanvasController.SmartContractCanvasImpl smartContractCanvasImpl = new SmartContractCanvasController.SmartContractCanvasImpl() {
        @Override
        public void onAction() {
            settingLayoutData();
        }
    };


}
