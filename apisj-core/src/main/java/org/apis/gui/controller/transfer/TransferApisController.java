package org.apis.gui.controller.transfer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TransferApisController extends BaseViewController {
    private final BigInteger GAS_LIMIT = new BigInteger("200000");
    private BigInteger gasPrice = new BigInteger("50000000000");
    private Image hintImageCheck = ImageManager.hintImageCheck;
    private Image hintImageError = ImageManager.hintImageError;

    @FXML private TextField recevingTextField;
    @FXML private ProgressBar progressBar;
    @FXML private Slider slider;
    @FXML private AnchorPane hintMaskAddress;
    @FXML private ImageView hintIcon;
    @FXML
    private Label totalMineralNature, detailMineralNature, detailGasNature, totalFeeNature,
            btnMyAddress, btnRecentAddress, hintMaskAddressLabel, feeLabel, feeCommentLabel,
            totalMineralLabel, detailLabel1, apisFeeLabel1, apisFeeLabel2,
            lowLabel, highLabel, gaspriceComment1Label, gaspriceComment2Label, recevingAddressLabel
            ;
    @FXML private ApisWalletAndAmountController walletAndAmountController;

    public void languageSetting() {
        this.feeLabel.textProperty().bind(StringManager.getInstance().transfer.fee);
        this.feeCommentLabel.textProperty().bind(StringManager.getInstance().transfer.feeComment);
        this.totalMineralLabel.textProperty().bind(StringManager.getInstance().transfer.totalMineral);
        this.detailLabel1.textProperty().bind(StringManager.getInstance().transfer.detail);
        this.apisFeeLabel1.textProperty().bind(StringManager.getInstance().transfer.apisFee);
        this.apisFeeLabel2.textProperty().bind(StringManager.getInstance().transfer.apisFee);
        this.lowLabel.textProperty().bind(StringManager.getInstance().transfer.low);
        this.highLabel.textProperty().bind(StringManager.getInstance().transfer.high);
        this.gaspriceComment1Label.textProperty().bind(StringManager.getInstance().transfer.gaspriceComment1);
        this.gaspriceComment2Label.textProperty().bind(StringManager.getInstance().transfer.gaspriceComment2);
        this.recevingAddressLabel.textProperty().bind(StringManager.getInstance().transfer.recevingAddress);
        this.btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        this.btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);
        this.recevingTextField.promptTextProperty().bind(StringManager.getInstance().transfer.recevingAddressPlaceHolder);
        this.detailMineralNature.textProperty().bind(totalMineralNature.textProperty());
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        slider.valueProperty().addListener(sliderImpl);
        walletAndAmountController.setHandler(apisAmountImpl);
        recevingTextField.focusedProperty().addListener(recevingFocused);
        recevingTextField.textProperty().addListener(recevingText);
        this.slider.valueProperty().setValue(0);

        walletAndAmountController.setGasLimit(GAS_LIMIT);
        walletAndAmountController.setGasPrice(gasPrice);
    }

    public void settingLayoutData(){
        // gas
        BigInteger sGasPrice = gasPrice.multiply(GAS_LIMIT);
        //mineral
        BigInteger mineral = walletAndAmountController.getMineral();
        String sMineral = mineral.toString();

        //fee
        BigInteger fee = gasPrice.multiply(GAS_LIMIT).subtract(mineral);
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;

        detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
        totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
        totalFeeNature.textProperty().setValue(ApisUtil.readableApis(fee,',',true));

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
            PopupRecentAddressController controller = (PopupRecentAddressController)PopupManager.getInstance().showMainPopup("popup_recent_address.fxml", 0);
            controller.setHandler(new PopupRecentAddressController.PopupRecentAddressImpl() {
                @Override
                public void onMouseClickYes(String address) {
                    recevingTextField.setText(address);
                }
            });
        }else if(id.equals("btnMyAddress")){
            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup("popup_my_address.fxml", 0);
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
        return GAS_LIMIT;
    }
    public BigInteger getGasPrice(){
        return this.gasPrice;
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
        BigInteger fee = gasPrice.multiply(GAS_LIMIT).subtract(getMineral());
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;
        return fee;
    }





    private ChangeListener<Number> sliderImpl = new ChangeListener<Number>() {
        public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
            // min:50 * 10^9
            // max:500 * 10^9
            progressBar.setProgress((new_val.doubleValue()-slider.getMin()) / (slider.getMax()-slider.getMin()));
            gasPrice = new BigInteger(""+new_val.intValue()).multiply(new BigInteger("1000000000"));
            walletAndAmountController.setGasPrice(gasPrice);
            if(walletAndAmountController.getAmount().compareTo(walletAndAmountController.getAmountToMax()) >= 0){
                walletAndAmountController.setAmountToMax();
            }
            settingLayoutData();
        }
    };
    private ApisWalletAndAmountController.ApisAmountImpl apisAmountImpl = new ApisWalletAndAmountController.ApisAmountImpl() {
        @Override
        public void change(BigInteger value) {
            settingLayoutData();
        }
    };
    private ChangeListener<Boolean> recevingFocused = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            walletAndAmountController.setStage(ApisSelectBoxController.STAGE_DEFAULT);

            if(newValue) {
                //onFocusIn();
                String style = "";
                style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;";
                style = style + "-fx-background-color : #ffffff; ";
                style = style + "-fx-border-color : #999999; ";
                recevingTextField.setStyle(style);
            } else {
                //onFocusOut();
                String style = "";
                style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
                style = style + "-fx-background-color : #f2f2f2; ";
                style = style + "-fx-border-color : #d8d8d8; ";
                recevingTextField.setStyle(style);

                String mask = recevingTextField.getText();
                if(mask.indexOf("@") >= 0){
                    //use masking address
                    String address = AppManager.getInstance().getAddressWithMask(mask);
                    if(address != null) {
                        hintMaskAddressLabel.textProperty().setValue(mask + " = " + address);
                        hintMaskAddressLabel.setTextFill(Color.web("#36b25b"));
                        hintIcon.setImage(hintImageCheck);

                    }else{
                        hintMaskAddressLabel.textProperty().setValue("No matching addresses found.");
                        hintMaskAddressLabel.setTextFill(Color.web("#910000"));
                        hintIcon.setImage(hintImageError);
                    }
                    showHintMaskAddress();
                }else{
                    //use hex address
                    hideHintMaskAddress();
                }
            }
            settingLayoutData();
        }
    };
    private ChangeListener<String> recevingText = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
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
