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
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TransferApisController extends BaseViewController {
    private Image hintImageCheck = ImageManager.hintImageCheck;
    private Image hintImageError = ImageManager.hintImageError;

    @FXML private TextField recevingTextField;
    @FXML private AnchorPane hintMaskAddress;
    @FXML private ImageView hintIcon;
    @FXML
    private Label btnMyAddress, btnRecentAddress, hintMaskAddressLabel,
            lowLabel, recevingAddressLabel
            ;
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private GasCalculatorController gasCalculatorController;

    public void languageSetting() {
        this.recevingAddressLabel.textProperty().bind(StringManager.getInstance().transfer.recevingAddress);
        this.btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        this.btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);
        this.recevingTextField.promptTextProperty().bind(StringManager.getInstance().transfer.recevingAddressPlaceHolder);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        AppManager.settingTextFieldStyle(recevingTextField);

        walletAndAmountController.setHandler(apisAmountImpl);
        recevingTextField.focusedProperty().addListener(recevingFocused);
        recevingTextField.textProperty().addListener(recevingText);

        gasCalculatorController.setGasLimit("200000");

    }

    public void settingLayoutData(){
        //mineral
        BigInteger mineral = walletAndAmountController.getMineral();
        String sMineral = mineral.toString();
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
                    recevingTextField.setText(address);
                }
            });
        }else if(id.equals("btnMyAddress")){
            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(null, "popup_my_address.fxml", 0);
            controller.setHandler(new PopupMyAddressController.PopupMyAddressImpl() {
                @Override
                public void onClickYes(String address) {
                    recevingTextField.setText(address);
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

    public void selectedItemWithWalletId(String id) {
        this.walletAndAmountController.selectedItemWithWalletId(id);
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

    public BigInteger getMineral() {
        return this.walletAndAmountController.getMineral();
    }

    public String getAddress() {
        return this.walletAndAmountController.getAddress();
    }

    public String getKeystoreId() {
        return this.walletAndAmountController.getKeystoreId();
    }

    public String getReceiveAddress() {
        return (recevingTextField.getText() != null) ? recevingTextField.getText().trim() : "";
    }

    public BigInteger getFee() {
        BigInteger fee = getGasPrice().multiply(getGasLimit()).subtract(getMineral());
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;
        return fee;
    }


    private ApisWalletAndAmountController.ApisAmountImpl apisAmountImpl = new ApisWalletAndAmountController.ApisAmountImpl() {
        @Override
        public void change(BigInteger value) {
            settingLayoutData();
        }
    };
    private ChangeListener<Boolean> recevingFocused = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

            settingLayoutData();
        }
    };
    private ChangeListener<String> recevingText = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            settingLayoutData();

            String mask = recevingTextField.getText();
            if(mask.indexOf("@") >= 0){
                //use masking address
                String address = AppManager.getInstance().getAddressWithMask(mask);
                if(address != null) {
                    hintMaskAddressLabel.textProperty().setValue(mask + " = " + address);
                    hintMaskAddressLabel.setTextFill(Color.web("#36b25b"));
                    hintIcon.setImage(hintImageCheck);

                }else{
                    hintMaskAddressLabel.textProperty().setValue(StringManager.getInstance().common.addressNotMath.get());
                    hintMaskAddressLabel.setTextFill(Color.web("#910000"));
                    hintIcon.setImage(hintImageError);
                }
                showHintMaskAddress();
            }else{
                //use hex address
                hideHintMaskAddress();
            }
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
