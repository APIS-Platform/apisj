package org.apis.gui.controller.transfer;

import javafx.fxml.FXML;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.module.GasCalculatorController;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TransferTokenController extends BaseViewController {
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private GasCalculatorController gasCalculatorController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        walletAndAmountController.setViewTypeApis(ApisWalletAndAmountController.ViewType.token);
        walletAndAmountController.setHandler(new ApisWalletAndAmountController.ApisAmountImpl() {
            @Override
            public void change(BigInteger value) {
                settingLayoutData();
            }
        });
    }

    public void update(){
        settingLayoutData();
    }

    private void settingLayoutData(){
        gasCalculatorController.setMineral(walletAndAmountController.getMineral());
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
}
