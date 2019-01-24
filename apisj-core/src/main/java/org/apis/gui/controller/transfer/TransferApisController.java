package org.apis.gui.controller.transfer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.module.HintMaskAddressController;
import org.apis.gui.controller.module.textfield.ApisAddressFieldController;
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TransferApisController extends BaseViewController {
    @FXML private AnchorPane hintMaskAddress;
    @FXML
    private Label btnMyAddress, btnRecentAddress, lowLabel, ReceivingAddressLabel
            ;
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private ApisAddressFieldController ReceivingFieldController;
    @FXML private HintMaskAddressController hintController;

    public void languageSetting() {
        this.ReceivingAddressLabel.textProperty().bind(StringManager.getInstance().transfer.ReceivingAddress);
        this.btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        this.btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideHintMaskAddress();
        languageSetting();

        walletAndAmountController.setHandler(apisAmountImpl);
        ReceivingFieldController.setHandler(new ApisAddressFieldController.ApisAddressFieldImpl() {
            @Override
            public void change(String address, String mask) {
                if(mask != null && mask.length() > 0){
                    //use masking address
                    if(address != null) {
                        hintController.setHintMaskAddressLabel(mask + " = " + address);
                        hintController.setSuccessed();

                    }else{
                        hintController.setFailed(StringManager.getInstance().common.addressNotMath);
                    }
                    showHintMaskAddress();
                }else{

                    //use hex address
                    hideHintMaskAddress();
                }

                settingLayoutData();
            }
        });

        gasCalculatorController.setGasLimit("200000");

    }

    public void settingLayoutData(){
        //mineral
        BigInteger mineral = walletAndAmountController.getMineral();
        gasCalculatorController.setMineral(mineral);

        // gas
        BigInteger sGasPrice = gasCalculatorController.getGasPrice();

        //fee
        BigInteger fee = getGasPrice().multiply(getGasLimit()).subtract(mineral);
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;

        if(handler != null){
            handler.settingLayoutData();
        }
    }

    @Override
    public void update(){
        walletAndAmountController.update();
        settingLayoutData();
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        id = (id != null) ? id : "";
        if(id.equals("rootPane")){
        }

        else if(id.equals("btnRecentAddress")){
            PopupRecentAddressController controller = (PopupRecentAddressController)PopupManager.getInstance().showMainPopup(null, "popup_recent_address.fxml", 0);
            controller.setHandler(new PopupRecentAddressController.PopupRecentAddressImpl() {
                @Override
                public void onMouseClickYes(String address) {
                    ReceivingFieldController.setText(address);
                }
            });
        }else if(id.equals("btnMyAddress")){
            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(null, "popup_my_address.fxml", 0);
            controller.setHandler(new PopupMyAddressController.PopupMyAddressImpl() {
                @Override
                public void onClickYes(String address) {
                    ReceivingFieldController.setText(address);
                }
            });
        }
    }
    @FXML
    private void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
    }
    @FXML
    private void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
    }

    public void showError() {
        walletAndAmountController.showError();
    }
    public void hideError() {
        walletAndAmountController.hideError();
    }

    public void selectedItemWithWalletAddress(String address) {
        this.walletAndAmountController.selectedItemWithWalletAddress(address);
        this.walletAndAmountController.update();
    }

    private void showHintMaskAddress(){
        this.hintMaskAddress.setVisible(true);
        this.hintMaskAddress.prefHeightProperty().setValue(-1);
    }
    private void hideHintMaskAddress(){
        this.hintMaskAddress.setVisible(false);
        this.hintMaskAddress.prefHeightProperty().setValue(0);
    }

    public BigInteger getGasLimit(){
        return gasCalculatorController.getGasLimit();
    }
    public BigInteger getGasPrice(){
        return gasCalculatorController.getGasPrice();
    }

    public BigInteger getBalance() {
        return this.walletAndAmountController.getBalance();
    }

    public BigInteger getAmount() {
        return this.walletAndAmountController.getAmount();
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

    public BigInteger getMineral() {
        return this.walletAndAmountController.getMineral();
    }

    public String getAddress() {
        return this.walletAndAmountController.getAddress();
    }

    public String getReceiveAddress() {
        return ReceivingFieldController.getAddress();
    }
    public String getReceiveText(){
        return ReceivingFieldController.getText();
    }

    public BigInteger getFee() {
        BigInteger fee = getGasPrice().multiply(getGasLimit()).subtract(getMineral());
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;
        return fee;
    }
    public BigInteger getChargedFee() {
        return this.gasCalculatorController.getTotalFee();
    }
    public BigInteger getAfterBalance(){
        // total amount
        BigInteger chargedAmount = getChargedAmount();

        //after balance
        BigInteger afterBalance = getBalance().subtract(chargedAmount);

        return afterBalance;
    }

    public boolean isReadyTransfer(){
        // 소지금체크
        if(getBalance().compareTo(BigInteger.ZERO) <= 0){
            return false;
        }

        // 잔액체크
        if(getAfterBalance().compareTo(BigInteger.ZERO) < 0){
            return false;
        }

        // 받는 사람 주소 체크
        if(!AddressUtil.isAddress(ReceivingFieldController.getAddress())){
            return false;
        }

        BigInteger gasLimit = gasCalculatorController.getGasLimit();
        if(gasLimit.compareTo(BigInteger.ONE) <= 0){
            return false;
        }

        return true;
    }



    private ApisWalletAndAmountController.ApisAmountImpl apisAmountImpl = new ApisWalletAndAmountController.ApisAmountImpl() {
        @Override
        public void change(BigInteger value) {
            settingLayoutData();
        }
    };
    private ChangeListener<Boolean> ReceivingFocused = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

            settingLayoutData();
        }
    };





    private TransferApisImpl handler;
    public void setHandler(TransferApisImpl handler){
        this.handler = handler;
    }

    public interface TransferApisImpl{
        void settingLayoutData();
    }
}
