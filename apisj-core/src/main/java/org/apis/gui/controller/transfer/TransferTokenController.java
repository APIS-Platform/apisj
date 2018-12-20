package org.apis.gui.controller.transfer;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.GUIContractManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.solidity.SolidityType;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TransferTokenController extends BaseViewController {

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_ERC20);
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionTransfer = contract.getByName("transfer");

    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private TextField recevingTextField;
    @FXML private AnchorPane hintMaskAddress;
    @FXML private ImageView hintIcon;
    @FXML private Label hintMaskAddressLabel, recevingAddressLabel, btnMyAddress, btnRecentAddress;

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

        });

        recevingTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String address = AppManager.getInstance().getAddressWithMask(recevingTextField.getText());
                if(address != null && address.length() > 0){
                    hintMaskAddress.setVisible(true);
                    hintMaskAddress.setPrefHeight(-1);
                    hintMaskAddressLabel.setText(recevingTextField.getText() + " = "+address);
                }else{
                    hintMaskAddress.setVisible(false);
                    hintMaskAddress.setPrefHeight(0);
                }

                settingLayoutData();
            }
        });
    }

    public void languageSetting(){
        recevingAddressLabel.textProperty().bind(StringManager.getInstance().transfer.recevingAddress);
        recevingTextField.promptTextProperty().bind(StringManager.getInstance().transfer.recevingAddressPlaceHolder);
        btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);

    }

    @Override
    public void update(){
        walletAndAmountController.update();
        settingLayoutData();
    }

    public void settingLayoutData(){
        gasCalculatorController.setMineral(walletAndAmountController.getMineral());
        estimateGasLimit();
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

    private void estimateGasLimit(){

        String tokenAddress = walletAndAmountController.getTokenAddress();


        Object[] args = new Object[2];
        args[0] = getReceveAddress();// to
        args[1] = getTokenAmount();// token value (aApis)

        String functionName = functionTransfer.name;
        byte[] address = Hex.decode(walletAndAmountController.getAddress());
        long preGasUsed = 0;

        if(tokenAddress != null && AddressUtil.isAddress(tokenAddress) && getReceveAddress() != null && AddressUtil.isAddress(getReceveAddress())){
            System.out.println("abi : "+abi);
            System.out.println("address : "+ByteUtil.toHexString(address));
            System.out.println("tokenAddress : "+tokenAddress);
            System.out.println("getAmount() : "+getAmount().toString());
            System.out.println("functionName : "+functionName);
            System.out.println("args[0] : "+args[0]);
            System.out.println("args[1] : "+args[1]);
            preGasUsed = AppManager.getInstance().getPreGasUsed(abi, address, Hex.decode(tokenAddress), getAmount(), functionName, args);
        }

        if(preGasUsed <= 1){
            gasCalculatorController.setGasLimit("0");
        }else{
            gasCalculatorController.setGasLimit(Long.toString(preGasUsed));
        }

    }



    public void selectedItemWithWalletAddress(String address) {
        this.walletAndAmountController.selectedItemWithWalletAddress(address);
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
    public BigInteger getAfterTokenBalance(){
        // total token amount
        BigInteger chargedTokenAmount = getChargedTokenAmount();

        //after token balance
        BigInteger afterTokenBalance = getTokenBalance().subtract(chargedTokenAmount);

        return afterTokenBalance;
    }

    public BigInteger getAmount() {
        return walletAndAmountController.getAmount();
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

    public BigInteger getChargedTokenAmount(){
        return getTokenAmount();
    }

    public BigInteger getTokenAmount(){
        return walletAndAmountController.getTokenAmount();
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

    public interface TransferTokenImpl{
        void settingLayoutData();
    }
}
