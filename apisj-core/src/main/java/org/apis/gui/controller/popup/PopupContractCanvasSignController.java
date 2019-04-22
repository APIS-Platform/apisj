package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.GasCalculatorMiniController;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.controller.module.textfield.ApisAddressFieldController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractCanvasSignController extends BasePopupController {
    private final int WALLET_ADDRESS_TYPE_SELECT = 0;
    private final int WALLET_ADDRESS_TYPE_INPUT = 1;
    private int walletAddressType = WALLET_ADDRESS_TYPE_SELECT;

    @FXML private AnchorPane selectWalletPane, inputWalletPane;
    @FXML private Label totalBalance, confirmBtn, directInputLabel, errorLabel, titleLabel, subTitleLabel, dataLabel,
                        selectWalletLabel, totalLabel, cancelBtn;
    @FXML private ApisSelectBoxController selectWalletController;
    @FXML private ApisAddressFieldController inputWalletController;
    @FXML private GasCalculatorMiniController gasCalculatorMiniController;

    private PopupContractCanvasSignImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        gasCalculatorMiniController.setDisable(false);

        selectWalletController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, true);
        selectWalletController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {

            }

            @Override
            public void onSelectItem() {
                StyleManager.backgroundColorStyle(confirmBtn, StyleManager.AColor.Cd8d8d8);
                gasCalculatorMiniController.setGasLimit("0");
                settingLayoutData();
            }
        });

        inputWalletController.setHandler(new ApisAddressFieldController.ApisAddressFieldImpl() {
            @Override
            public void change(String address, String mask) {
                settingLayoutData();
            }
        });

        gasCalculatorMiniController.setHandler(new GasCalculatorMiniController.GasCalculatorImpl() {
            @Override
            public void gasLimitTextFieldFocus(boolean isFocused) {

            }

            @Override
            public void gasLimitTextFieldChangeValue(String oldValue, String newValue) {

            }

            @Override
            public void gasPriceSliderChangeValue(int value) {

            }

            @Override
            public void changeGasPricePopup(boolean isVisible) {

            }

            @Override
            public void clickPreGasUsed() {
                confirmBtn.setDisable(false);
                StyleManager.backgroundColorStyle(confirmBtn, StyleManager.AColor.Cb01e1e);
                estimateGasLimit();
                settingLayoutData();
            }
        });

        directInputLabel.setOnMouseClicked(event -> {
            if(walletAddressType == WALLET_ADDRESS_TYPE_SELECT) {
                walletAddressType = WALLET_ADDRESS_TYPE_INPUT;

                StyleManager.backgroundColorStyle(directInputLabel, StyleManager.AColor.C000000);
                StyleManager.borderColorStyle(directInputLabel, StyleManager.AColor.C000000);
                StyleManager.fontColorStyle(directInputLabel, StyleManager.AColor.Cffffff);
                totalBalance.setText("0");
                inputWalletController.setText("");
                inputWalletController.setImage(ImageManager.icCircleNone);
                selectWalletPane.setVisible(false);
                inputWalletPane.setVisible(true);
                settingLayoutData();

            } else if(walletAddressType == WALLET_ADDRESS_TYPE_INPUT){
                walletAddressType = WALLET_ADDRESS_TYPE_SELECT;

                StyleManager.backgroundColorStyle(directInputLabel, StyleManager.AColor.Cf8f8fb);
                StyleManager.borderColorStyle(directInputLabel, StyleManager.AColor.C999999);
                StyleManager.fontColorStyle(directInputLabel, StyleManager.AColor.C999999);
                selectWalletPane.setVisible(true);
                inputWalletPane.setVisible(false);
                settingLayoutData();
            }
        });

        settingLayoutData();
    }

    public void languageSetting() {
        titleLabel.textProperty().bind(StringManager.getInstance().popup.signTx);
        subTitleLabel.textProperty().bind(StringManager.getInstance().popup.signTx);
        dataLabel.textProperty().bind(StringManager.getInstance().transaction.detailsDataLabel);
        selectWalletLabel.textProperty().bind(StringManager.getInstance().popup.selectWallet);
        directInputLabel.textProperty().bind(StringManager.getInstance().common.directInputButton);
        totalLabel.textProperty().bind(StringManager.getInstance().popup.totalAssetLabel);
        cancelBtn.textProperty().bind(StringManager.getInstance().popup.cancel);
        confirmBtn.textProperty().bind(StringManager.getInstance().popup.confirm);
        errorLabel.textProperty().bind(StringManager.getInstance().common.notEnoughBalance);
    }

    public void settingLayoutData() {
        String address = null;
        BigInteger balance = BigInteger.ZERO;
        BigInteger mineral = BigInteger.ZERO;

        if(selectWalletPane.isVisible()) {
            address = selectWalletController.getAddress();
            balance = selectWalletController.getBalance();
            mineral = selectWalletController.getMineral();

        } else {
            address = inputWalletController.getAddress();
            if(address != null) {
                balance = AppManager.getInstance().getBalance(address);
                mineral = AppManager.getInstance().getMineral(address);
            }
        }

        this.gasCalculatorMiniController.setMineral(mineral);
        this.totalBalance.setText(ApisUtil.readableApis(balance.toString(), ',', ApisUtil.Unit.aAPIS, true));

        if(!confirmBtn.isDisable()){
            errorLabel.setVisible(false);
            errorLabel.setPrefHeight(0);

            if(balance.compareTo(BigInteger.valueOf(10)) >= 0){
                if(gasCalculatorMiniController.getTotalFee().compareTo(BigInteger.ZERO) > 0){
                    BigInteger fee = balance.subtract(gasCalculatorMiniController.getTotalFee());
                    if(fee.compareTo(BigInteger.ZERO) < 0){
                        StyleManager.backgroundColorStyle(confirmBtn, StyleManager.AColor.Cd8d8d8);
                        confirmBtn.setDisable(true);

                        errorLabel.setVisible(true);
                        errorLabel.setPrefHeight(-1);
                    }
                }

            }else{
                StyleManager.backgroundColorStyle(confirmBtn, StyleManager.AColor.Cd8d8d8);
                confirmBtn.setDisable(true);

                errorLabel.setVisible(true);
                errorLabel.setPrefHeight(-1);
            }
        }
    }


    private void estimateGasLimit() {
        String address = selectWalletController.getAddress();
        BigInteger balance = selectWalletController.getBalance();
    }

    public void setHandler(PopupContractCanvasSignImpl handler) {
        this.handler = handler;
    }

    public interface PopupContractCanvasSignImpl {
        void onClickConfirm();
    }
}
