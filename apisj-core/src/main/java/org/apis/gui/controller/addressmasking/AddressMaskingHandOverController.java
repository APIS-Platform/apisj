package org.apis.gui.controller.addressmasking;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisButtonEstimateGasLimitController;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.module.textfield.ApisAddressFieldController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class AddressMaskingHandOverController extends BaseViewController {
    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
    private byte[] addressMaskingAddress = Hex.decode("1000000000000000000000000000000000037449");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionHandOverMask = contract.getByName("handOverMask");
    private CallTransaction.Function functionDefaultFee = contract.getByName("defaultFee");

    @FXML private ImageView registerAddressIcon, handedToIcon;
    @FXML private Label apisTotal, registerAddressLabel, selectDomainLabel, addressMsg, recipientInputBtn, registerAddressDesc, selectHandedToDesc, handedToMsg;
    @FXML private ApisSelectBoxController selectAddressController, selectHandedToController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private ApisButtonEstimateGasLimitController btnStartPreGasUsedController;
    @FXML private ApisAddressFieldController handedAddressFieldController;

    private boolean isHandToAddressSelected = true;
    private boolean isEnabled = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        selectAddressController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, false);
        selectHandedToController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, false);

        selectAddressController.setHandler(selectBoxImpl);
        selectAddressController.selectedItem(0);
        selectHandedToController.setHandler(selectBoxImpl);
        gasCalculatorController.setHandler(new GasCalculatorController.GasCalculatorImpl() {
            @Override
            public void gasLimitTextFieldFocus(boolean isFocused) {
                settingLayoutData();
            }

            @Override
            public void gasLimitTextFieldChangeValue(String oldValue, String newValue) {
                settingLayoutData();
            }

            @Override
            public void gasPriceSliderChangeValue(int value) {
                settingLayoutData();
            }
        });

        handedAddressFieldController.setHandler(new ApisAddressFieldController.ApisAddressFieldImpl() {
            @Override
            public void change(String address, String mask) {
                settingLayoutData();
            }
        });
        handedAddressFieldController.setVisible(false);

        btnStartPreGasUsedController.setHandler(new ApisButtonEstimateGasLimitController.ApisButtonestimateGasLimitImpl() {
            @Override
            public void onMouseClicked(ApisButtonEstimateGasLimitController controller) {
                isEnabled = true;
                estimateGasLimit();
                settingLayoutData();
            }
        });


        settingLayoutData();
    }

    public void languageSetting(){
        registerAddressLabel.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressLabel);
        selectDomainLabel.textProperty().bind(StringManager.getInstance().addressMasking.selectDomainLabel);
        recipientInputBtn.textProperty().bind(StringManager.getInstance().common.directInputButton);
        selectHandedToDesc.textProperty().bind(StringManager.getInstance().addressMasking.selectHandedToDesc);
        registerAddressDesc.textProperty().bind(StringManager.getInstance().addressMasking.selectAddressDesc);
    }

    public void settingLayoutData(){
        String fromAddress = getHandOverFromAddress();
        String fromMask = AppManager.getInstance().getMaskWithAddress(fromAddress);

        String toAddress = getHandOverToAddress();
        String toMask = AppManager.getInstance().getMaskWithAddress(toAddress);

        btnStartPreGasUsedController.setCompiled(true);
        gasCalculatorController.setDisable(false);

        if(fromMask != null && fromMask.length() > 0){
            registerAddressIcon.setImage(ImageManager.icCheckGreen);
            addressMsg.textProperty().unbind();
            addressMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg);
            StyleManager.getInstance().fontColorStyle(addressMsg, StyleManager.AColor.C36b25b);
        }else{
            registerAddressIcon.setImage(ImageManager.icErrorRed);
            addressMsg.textProperty().unbind();
            addressMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg1);
            StyleManager.getInstance().fontColorStyle(addressMsg, StyleManager.AColor.C910000);
        }

        if(toMask != null && toMask.length() > 0){
            handedToIcon.setImage(ImageManager.icErrorRed);
            handedToMsg.textProperty().unbind();
            handedToMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg2);
            StyleManager.getInstance().fontColorStyle(handedToMsg, StyleManager.AColor.C910000);
        }else if(toAddress == null || toAddress.length() < 40){
            handedToIcon.setImage(ImageManager.icErrorRed);
            handedToMsg.textProperty().unbind();
            handedToMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg3);
            StyleManager.getInstance().fontColorStyle(handedToMsg, StyleManager.AColor.C910000);
        }else{
            handedToIcon.setImage(ImageManager.icCheckGreen);
            handedToMsg.textProperty().unbind();
            handedToMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg);
            StyleManager.getInstance().fontColorStyle(handedToMsg, StyleManager.AColor.C36b25b);
        }


        // 양도할 주소에 마스크가 없는 경우
        if(fromMask == null || fromMask.length() == 0){
            btnStartPreGasUsedController.setCompiled(false);
            gasCalculatorController.setDisable(true);
            gasCalculatorController.setGasLimit("0");
            isEnabled = false;
        }

        // 양도받을 주소에 마스크가 있는 경우
        if (toAddress == null || toAddress.length() < 40 || (toMask != null && toMask.length() > 0) ) {
            btnStartPreGasUsedController.setCompiled(false);
            gasCalculatorController.setDisable(true);
            gasCalculatorController.setGasLimit("0");
            isEnabled = false;
        }



        BigInteger balance = selectAddressController.getBalance();
        String sBalance = ApisUtil.readableApis(balance, ',', true);
        BigInteger mineral = selectAddressController.getMineral();
        gasCalculatorController.setMineral(mineral);

        apisTotal.setText("APIS Total : " + sBalance);

        if(handler != null){
            handler.settingLayoutData();
        }
    }

    private void estimateGasLimit(){
        String fromAddress = getHandOverFromAddress();
        String toAddress = getHandOverToAddress();

        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
        BigInteger value = new BigInteger(""+values[0]);

        long checkGas = AppManager.getInstance().getPreGasUsed(abi, Hex.decode(fromAddress), addressMaskingAddress, value, functionHandOverMask.name, toAddress);
        if(checkGas > 0) {
            String preGasUsed = Long.toString(checkGas);
            gasCalculatorController.setGasLimit(preGasUsed);
        }else{
            gasCalculatorController.setGasLimit("0");
        }
    }


    public String getHandOverFromAddress(){
        return selectAddressController.getAddress();
    }
    public String getHandOverToAddress(){
        if(isHandToAddressSelected){
            return selectHandedToController.getAddress();
        }else{
            return handedAddressFieldController.getAddress();
        }
    }
    public String getHandOverFromMask() {
        return AppManager.getInstance().getMaskWithAddress(getHandOverFromAddress());
    }

    @Override
    public void update(){
        selectAddressController.update();
        selectHandedToController.update();

        settingLayoutData();
    }

    private ApisSelectBoxController.ApisSelectBoxImpl selectBoxImpl = new ApisSelectBoxController.ApisSelectBoxImpl() {
        @Override
        public void onMouseClick() {
        }

        @Override
        public void onSelectItem() {

            String fromAddress = getHandOverFromAddress();
            String fromMask = AppManager.getInstance().getMaskWithAddress(fromAddress);

            String toAddress = getHandOverToAddress();
            String toMask = AppManager.getInstance().getMaskWithAddress(toAddress);

            Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
            BigInteger value = new BigInteger(""+values[0]);



            settingLayoutData();
        }
    };

    public boolean isEnabled(){

        return this.isEnabled;
    }

    public void onMouseClicked(InputEvent event ){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("recipientInputBtn")) {
            if(isHandToAddressSelected) {
                isHandToAddressSelected = false;

                StyleManager.backgroundColorStyle(recipientInputBtn, StyleManager.AColor.C000000);
                StyleManager.borderColorStyle(recipientInputBtn, StyleManager.AColor.C000000);
                StyleManager.fontColorStyle(recipientInputBtn, StyleManager.AColor.Cffffff);
                handedAddressFieldController.setText("");
                handedAddressFieldController.setVisible(true);
                selectHandedToController.setVisible(false);


            } else {
                isHandToAddressSelected = true;

                StyleManager.backgroundColorStyle(recipientInputBtn, StyleManager.AColor.Cf8f8fb);
                StyleManager.borderColorStyle(recipientInputBtn, StyleManager.AColor.C999999);
                StyleManager.fontColorStyle(recipientInputBtn, StyleManager.AColor.C999999);
                selectHandedToController.setVisible(true);
                handedAddressFieldController.setVisible(false);
            }

            settingLayoutData();
        }
    }

    public BigInteger getFee(){
        return gasCalculatorController.getFee();
    }

    public BigInteger getChargedFee() {
        return this.gasCalculatorController.getTotalFee();
    }

    public BigInteger getMineral(){
        return this.selectAddressController.getMineral();
    }

    public BigInteger getAmount() {
        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
        BigInteger value = new BigInteger(""+values[0]);
        return value;
    }

    public BigInteger getChargedAmount(){
        BigInteger totalFee = getChargedFee();
        // total fee
        if(totalFee.toString().indexOf("-") >= 0){
            totalFee = BigInteger.ZERO;
        }

        // total amount
        BigInteger chargedAmount = getAmount().add(totalFee);

        return chargedAmount;
    }

    public BigInteger getAfterBalance(){
        // total amount
        BigInteger chargedAmount = getChargedAmount();

        //after balance
        BigInteger afterBalance = selectAddressController.getBalance().subtract(chargedAmount);

        return afterBalance;
    }

    public void setGasLimit(String gasLimit){
        this.gasCalculatorController.setGasLimit(gasLimit);
    }
    public BigInteger getGasPrice() {
        return gasCalculatorController.getGasPrice();
    }
    public BigInteger getGasLimit() {
        return  gasCalculatorController.getGasLimit();
    }

    private AddressMaskingHandOverImpl handler;
    public void setHandler(AddressMaskingHandOverImpl handler){
        this.handler = handler;
    }
    public interface AddressMaskingHandOverImpl{
        void settingLayoutData();
    }
}
