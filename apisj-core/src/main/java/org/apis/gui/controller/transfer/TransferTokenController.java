package org.apis.gui.controller.transfer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TransferTokenController extends BaseViewController {
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private TextField recevingTextField;
    @FXML private AnchorPane hintMaskAddress;
    @FXML private ImageView hintIcon;
    @FXML private Label hintMaskAddressLabel, recevingAddressLabel, btnMyAddress, btnRecentAddress, gaspriceComment1Label, gaspriceComment2Label;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
        AppManager.settingTextFieldStyle(recevingTextField);

        walletAndAmountController.setViewTypeApis(ApisWalletAndAmountController.ViewType.token);
        walletAndAmountController.setHandler(new ApisWalletAndAmountController.ApisAmountImpl() {
            @Override
            public void change(BigInteger value) {

                String receveAddress = getReceveAddress();

                if(receveAddress != null && receveAddress.length() > 0) {
                    Object args[] = new Object[2];
                    args[0] = getReceveAddress(); // to address : 프리가스 확인용으로 임의의
                    args[1] = getAmount(); // token amount

                    byte[] sender = Hex.decode(walletAndAmountController.getAddress());
                    byte[] contractAddress = Hex.decode(walletAndAmountController.getTokenAddress());
                    byte[] data = AppManager.getInstance().getTokenSendTransferData(args);
                    gasCalculatorController.setGasLimit(Long.toString(AppManager.getInstance().getPreGasUsed(sender, contractAddress, data)));
                }

                settingLayoutData();
            }
        });

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

            @Override
            public void changeGasPricePopup(boolean isVisible){

            }
        });

        recevingTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String address = AppManager.getInstance().getAddressWithMask(recevingTextField.getText());
                if(address != null && address.length() > 0){
                    hintMaskAddress.setVisible(true);
                    hintMaskAddress.setPrefHeight(-1);
                    hintMaskAddressLabel.setText(recevingTextField.getText() + " = "+address);
                }else{
                    hintMaskAddress.setVisible(false);
                    hintMaskAddress.setPrefHeight(0);
                }
            }
        });
    }

    public void languageSetting(){
        recevingAddressLabel.textProperty().bind(StringManager.getInstance().transfer.recevingAddress);
        recevingTextField.promptTextProperty().bind(StringManager.getInstance().transfer.recevingAddressPlaceHolder);
        btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);

        gaspriceComment1Label.textProperty().bind(StringManager.getInstance().transfer.detailGaspriceComment1);
        gaspriceComment2Label.textProperty().bind(StringManager.getInstance().transfer.detailGaspriceComment2);
    }

    @Override
    public void update(){
        walletAndAmountController.update();
        settingLayoutData();
    }

    public void settingLayoutData(){
        gasCalculatorController.setMineral(walletAndAmountController.getMineral());
        if(handler != null){
            this.handler.settingLayoutData();
        }
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if(fxId.equals("btnMyAddress")){
            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(null,"popup_my_address.fxml", 0);
            controller.setHandler(new PopupMyAddressController.PopupMyAddressImpl() {
                @Override
                public void onClickYes(String address) {
                    recevingTextField.setText(address);

                    String receveAddress = getReceveAddress();

                    if(receveAddress != null && receveAddress.length() > 0) {
                        Object args[] = new Object[2];
                        args[0] = getReceveAddress(); // to address : 프리가스 확인용으로 임의의
                        args[1] = getAmount(); // token amount

                        byte[] sender = Hex.decode(walletAndAmountController.getAddress());
                        byte[] contractAddress = Hex.decode(walletAndAmountController.getTokenAddress());
                        byte[] data = AppManager.getInstance().getTokenSendTransferData(args);
                        gasCalculatorController.setGasLimit(Long.toString(AppManager.getInstance().getPreGasUsed(sender, contractAddress, data)));
                    }
                }
            });
        }else if(fxId.equals("btnRecentAddress")){
            PopupRecentAddressController controller = (PopupRecentAddressController)PopupManager.getInstance().showMainPopup(null,"popup_recent_address.fxml", 0);
            controller.setHandler(new PopupRecentAddressController.PopupRecentAddressImpl() {
                @Override
                public void onMouseClickYes(String address) {
                    recevingTextField.setText(address);

                    String receveAddress = getReceveAddress();

                    if(receveAddress != null && receveAddress.length() > 0) {
                        Object args[] = new Object[2];
                        args[0] = getReceveAddress(); // to address : 프리가스 확인용으로 임의의
                        args[1] = getAmount(); // token amount

                        byte[] sender = Hex.decode(walletAndAmountController.getAddress());
                        byte[] contractAddress = Hex.decode(walletAndAmountController.getTokenAddress());
                        byte[] data = AppManager.getInstance().getTokenSendTransferData(args);
                        gasCalculatorController.setGasLimit(Long.toString(AppManager.getInstance().getPreGasUsed(sender, contractAddress, data)));
                    }
                }
            });
        }
    }



    public void selectedItemWithWalletId(String id) {
        this.walletAndAmountController.selectedItemWithWalletId(id);
        this.walletAndAmountController.update();
    }

    public void setTokenAddress(String tokenAddress){
        walletAndAmountController.setTokenAddress(tokenAddress);
    }

    public void setTokenSymbol(String symbol){
        walletAndAmountController.setTokenSymbol(symbol);
    }

    public void setTokenName(String tokenName){
        walletAndAmountController.setTokenName(tokenName);
    }

    public BigInteger getTokenBalance() {
        return walletAndAmountController.getTokenBalance();
    }

    public BigInteger getBalance(){
        return walletAndAmountController.getBalance();
    }

    public BigInteger getAmount() {
        return walletAndAmountController.getAmount();
    }

    public BigInteger getGasPrice() {
        return gasCalculatorController.getGasPrice();
    }

    public BigInteger getGasLimit(){
        return gasCalculatorController.getGasLimit();
    }

    public BigInteger getMineral() {
        return walletAndAmountController.getMineral();
    }

    private TransferTokenImpl handler;
    public void setHandler(TransferTokenImpl handler){
        this.handler = handler;
    }

    public BigInteger getTotalFee() {
        return getGasPrice().multiply(getGasLimit()).subtract(getMineral());
    }

    public String getReceveAddress() {
        if(this.recevingTextField.getText() == null || this.recevingTextField.getText().length() == 0){
            return null;
        }

        if(AddressUtil.isAddress(this.recevingTextField.getText())){
            return this.recevingTextField.getText();
        }else {
            String address = AppManager.getInstance().getAddressWithMask(this.recevingTextField.getText());
            if(address != null && address.length() > 0){
                return address;
            }else{
                return null;
            }
        }
    }

    public String getSendAddress() {
        return this.walletAndAmountController.getAddress();
    }

    public String getKeystoreId() {
        return this.walletAndAmountController.getKeystoreId();
    }

    public interface TransferTokenImpl{
        void settingLayoutData();
    }
}
