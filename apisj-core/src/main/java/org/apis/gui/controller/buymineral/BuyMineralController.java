package org.apis.gui.controller.buymineral;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.receipt.ReceiptController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.*;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralController extends BasePopupController {

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_BUY_MINERAL);
    private byte[] buyMineralAddress =  AppManager.getInstance().constants.getBUY_MINERAL();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionBuyMNR = contract.getByName("buyMNR");
    private CallTransaction.Function functionCalcMNR = contract.getByName("calcMNR");

    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane bodyScrollPaneContentPane;
    @FXML private Label buyMineralLabel,buyMineralSubTitleLabel, backBtn;
    @FXML private ImageView icBack;
    @FXML private BuyMineralBodyController bodyController;
    @FXML private ReceiptController receiptController;

    private boolean isScrolling;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        receiptSetting();

        bodyController.setHandelr(new BuyMineralBodyController.BuyMineralBodyImpl() {
            @Override
            public void settingLayoutData() {
                BuyMineralController.this.settingLayoutData();
            }
        });

        receiptController.setTitle(StringManager.getInstance().receipt.totalFee);
        receiptController.setButtonTitle(StringManager.getInstance().common.payButton);
        receiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                String beneficiaryAddress = bodyController.getBeneficiaryAddress();
                String fromAddress = bodyController.getPayerAddress();
                BigInteger value = bodyController.getValue();
                BigInteger gasPrice = bodyController.getGasPrice();
                BigInteger gasLimit = bodyController.getGasLimit();

                Object[] args = new Object[1];
                args[0] = Hex.decode(beneficiaryAddress);
                byte[] functionCallBytes = functionBuyMNR.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null, "popup_contract_warning.fxml", 1);
                controller.setData(fromAddress, value.toString(), gasPrice.toString(), gasLimit.toString(), buyMineralAddress, new byte[0], functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });


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

        settingLayoutData();

    }

    public void languageSetting(){
        buyMineralLabel.textProperty().bind(StringManager.getInstance().buymineral.buyMineralLabel);
        buyMineralSubTitleLabel.textProperty().bind(StringManager.getInstance().buymineral.buyMineralSubTitleLabel);
        backBtn.textProperty().bind(StringManager.getInstance().common.backButton);
    }

    public void receiptSetting(){
        receiptController.addBeneficiaryAddress();
        receiptController.addVSpace(16);
        receiptController.addLineStyleDotted();
        receiptController.addVSpace(16);
        receiptController.addAmount();
        receiptController.addVSpace(16);
        receiptController.addTotalFee();
        receiptController.addVSpace(16);
        receiptController.addLineStyleDotted();
        receiptController.addVSpace(16);
        receiptController.addPayerAddress();
        receiptController.setSuccessed(false);
    }

    public void settingLayoutData(){

        String beneficiary = bodyController.getBeneficiaryAddress();
        String amount = ApisUtil.readableApis(bodyController.getValue(),',',true);
        String totalFee = bodyController.getTotalFee();
        String payerAddress = bodyController.getPayerAddress();
        receiptController.setBeneficiaryAddress(beneficiary);
        receiptController.setAmount(amount);
        receiptController.setTotalFee(totalFee + " APIS");
        receiptController.setPayerAddress(payerAddress);

        receiptController.setSuccessed(bodyController.isSuccessed());
    }


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("backBtn")){
            exit();
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("backBtn")){
            StyleManager.backgroundColorStyle(backBtn, StyleManager.AColor.Cffffff);
            StyleManager.borderColorStyle(backBtn, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(backBtn, StyleManager.AColor.Cb01e1e);
            icBack.setImage(ImageManager.icBackRed);
        }
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("backBtn")){
            StyleManager.backgroundColorStyle(backBtn, StyleManager.AColor.Cb01e1e);
            StyleManager.borderColorStyle(backBtn, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(backBtn, StyleManager.AColor.Cffffff);
            icBack.setImage(ImageManager.icBackWhite);
        }
    }
}
