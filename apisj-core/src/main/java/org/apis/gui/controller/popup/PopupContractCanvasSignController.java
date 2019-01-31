package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.GasCalculatorMiniController;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.manager.StyleManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractCanvasSignController extends BasePopupController {
    @FXML Label totalBalance, confirmBtn;
    @FXML ApisSelectBoxController selectWalletController;
    @FXML GasCalculatorMiniController gasCalculatorMiniController;

    private PopupContractCanvasSignImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                System.out.println("11111111111111111");
                StyleManager.backgroundColorStyle(confirmBtn, StyleManager.AColor.Cb01e1e);
                System.out.println("2222222222222");
                estimateGasLimit();
                settingLayoutData();
            }
        });

        settingLayoutData();
    }

    public void settingLayoutData() {
        String address = selectWalletController.getAddress();
        BigInteger balance = selectWalletController.getBalance();
        BigInteger mineral = selectWalletController.getMineral();

        this.gasCalculatorMiniController.setMineral(mineral);
        this.totalBalance.setText(ApisUtil.readableApis(balance.toString(), ',', ApisUtil.Unit.aAPIS, true));
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
