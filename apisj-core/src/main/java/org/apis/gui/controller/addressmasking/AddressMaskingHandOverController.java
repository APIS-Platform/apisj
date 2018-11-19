package org.apis.gui.controller.addressmasking;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
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

    @FXML private Label apisTotal, registerAddressLabel, selectDomainLabel;
    @FXML private ApisSelectBoxController selectAddressController, selectHandedToController;
    @FXML private GasCalculatorController gasCalculatorController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        selectAddressController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        selectHandedToController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);

        selectAddressController.setHandler(selectBoxImpl);
        selectHandedToController.setHandler(selectBoxImpl);
        gasCalculatorController.setHandler(new GasCalculatorController.GasCalculatorImpl() {
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
            public void changeGasPricePopup(boolean isVisible){

            }
        });
    }

    public void languageSetting(){
        registerAddressLabel.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressLabel);
        selectDomainLabel.textProperty().bind(StringManager.getInstance().addressMasking.selectDomainLabel);
    }

    public void settingLayoutData(){
        BigInteger balance = selectAddressController.getBalance();
        String sBalance = ApisUtil.readableApis(balance, ',', true);
        BigInteger mineral = selectAddressController.getMineral();
        gasCalculatorController.setMineral(mineral);

        apisTotal.setText("APIS Total : " + sBalance);

        if(handler != null){
            handler.settingLayoutData();
        }
    }

    public String getHandOverFromAddress(){
        return selectAddressController.getAddress();
    }
    public String getHandOverToAddress(){
        return selectHandedToController.getAddress();
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
            String toAddress = getHandOverToAddress();
            Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
            BigInteger value = new BigInteger(""+values[0]);
            long preGasUsed = AppManager.getInstance().getPreGasUsed(abi, Hex.decode(fromAddress), addressMaskingAddress, value, functionHandOverMask.name, toAddress);
            setGasUsed(preGasUsed);

            settingLayoutData();
        }
    };

    public void setGasLimit(String gasLimit){
        this.gasCalculatorController.setGasLimit(gasLimit);
    }
    public BigInteger getGasPrice() {
        return gasCalculatorController.getGasPrice();
    }
    public BigInteger getGasLimit() {
        return  gasCalculatorController.getGasLimit();
    }

    public void setGasUsed(long preGasUsed) {
        this.gasCalculatorController.setGasLimit(Long.toString(preGasUsed));
    }

    private AddressMaskingHandOverImpl handler;
    public void setHandler(AddressMaskingHandOverImpl handler){
        this.handler = handler;
    }
    public interface AddressMaskingHandOverImpl{
        void settingLayoutData();
    }
}
