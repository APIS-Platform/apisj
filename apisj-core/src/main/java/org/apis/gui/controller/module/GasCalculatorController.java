package org.apis.gui.controller.module;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class GasCalculatorController extends BaseViewController {
    // Gas Price Popup Flag
    private static final boolean GAS_PRICE_POPUP_MOUSE_ENTERED = true;
    private static final boolean GAS_PRICE_POPUP_MOUSE_EXITED = false;
    private boolean gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;

    @FXML private AnchorPane rootPane, gasPricePlusMinusPane;
    @FXML private TextField gasPriceTextField, gasLimitTextField;
    @FXML private GridPane  gasPricePopupGrid;
    @FXML private ImageView gasPriceMinusBtn, gasPricePlusBtn, gasPricePopupImg;
    @FXML private ProgressBar progressBar;
    @FXML private Slider slider;
    @FXML private Label gasPriceTitle, gasPriceFormula, gasPriceLabel, gasLimitLabel, gasPricePopupLabel, gasPricePopupDefaultLabel, detailLabel
            ,detailContentsFeeNum, detailContentsFee, detailContentsTotalNum, detailContentsTotal, lowLabel, highLabel;

    private BigInteger gasPrice = BigInteger.valueOf(50); // Default Gas Price 50
    private BigInteger gasLimit = BigInteger.ZERO;
    private BigInteger mineral = BigInteger.ZERO;
    private long minGasLimit = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        gasPriceTextField.textProperty().addListener(gasPriceTextListener);
        gasPriceTextField.focusedProperty().addListener(gasPriceFocuesedListener);

        gasLimitTextField.focusedProperty().addListener(gasLimitFocuesedListener);
        gasLimitTextField.textProperty().addListener(gasLimitTextListener);
        slider.valueProperty().addListener(sliderListener);

        //hideGasPricePopup();
        settingLayoutData();

        rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideGasPricePopup();
            }
        });


    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxid = ((Node)event.getSource()).getId();

        // Gas Price
        if(fxid.equals("gasPricePlusMinusPane")) {
            showGasPricePopup();
            slider.requestFocus();
            event.consume();

        } else if(fxid.equals("gasPriceMinusBtn")) {
            showGasPricePopup();
            slider.setValue(slider.getValue()-10);
            slider.requestFocus();
            event.consume();

        } else if(fxid.equals("gasPricePlusBtn")) {
            showGasPricePopup();
            slider.setValue(slider.getValue()+10);
            slider.requestFocus();
            event.consume();

        } else if(fxid.equals("rootPane")){
            hideGasPricePopup();
        }
    }

    @FXML
    public void onMouseEntered(InputEvent e){
        String id = ((Node)e.getSource()).getId();

        // Gas Price Popup
        if (id.equals("gasPricePopupGrid")) {
            gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_ENTERED;
        }
    }

    @FXML
    public void onMouseExited(InputEvent e){
        String id = ((Node)e.getSource()).getId();

        // Gas Price Popup
        if (id.equals("gasPricePopupGrid")) {
            gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;
        }
    }

    private ChangeListener<Boolean> gasPriceFocuesedListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            BigInteger bigInteger = new BigInteger(gasPriceTextField.getText());
            if(bigInteger.compareTo(BigInteger.valueOf(500)) > 0){
                gasPriceTextField.setText("500");
            }else if(bigInteger.compareTo(BigInteger.valueOf(50)) < 0){
                gasPriceTextField.setText("50");
            }
        }
    };

    private ChangeListener<Boolean> gasLimitFocuesedListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            settingLayoutData();

            if(GasCalculatorController.this.handler != null){
                GasCalculatorController.this.handler.gasLimitTextFieldFocus(gasLimitTextField.isFocused());
            }
        }
    };

    private ChangeListener<String> gasPriceTextListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            gasPricePopupLabel.setText(newValue+" nAPIS");
            slider.setValue(Double.parseDouble(newValue));
        }
    };
    private ChangeListener<String> gasLimitTextListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            // 숫자만 입력받도록 설정
            if (!newValue.matches("[\\d]*")) {
                gasLimitTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if(newValue.length() > 1 && newValue.indexOf(".") < 0 && newValue.indexOf("0") == 0){
                gasLimitTextField.setText(newValue.substring(1, newValue.length()));
            }

            try {
                BigInteger gasLimit = new BigInteger(gasLimitTextField.getText());
                BigInteger maxLimit = new BigInteger("50000000");

                if(gasLimit.compareTo(maxLimit) > 0){
                    gasLimitTextField.setText(maxLimit.toString());
                }
            }catch (Exception e){

            }

            settingLayoutData();

            if(GasCalculatorController.this.handler != null){
                GasCalculatorController.this.handler.gasLimitTextFieldFocus(gasLimitTextField.isFocused());
            }
        }
    };

    private ChangeListener<Number> sliderListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            progressBar.setProgress((newValue.doubleValue()-slider.getMin()) / (slider.getMax()-slider.getMin()));

            gasPrice = BigInteger.valueOf(newValue.intValue());
            gasPriceTextField.setText(gasPrice.toString());
            gasPricePopupLabel.setText(gasPrice.toString()+" nAPIS");

            // (Default) 라는 문구 표기/숨기기
            gasPricePopupDefaultLabel.setVisible(newValue.intValue() == 50);

            settingLayoutData();
            if(handler != null){
                handler.gasPriceSliderChangeValue(newValue.intValue());
            }
        }
    };

    public void languageSetting() {
        gasPriceTitle.textProperty().bind(StringManager.getInstance().module.gasPriceTitle);
        gasPriceFormula.textProperty().bind(StringManager.getInstance().module.gasPriceFormula);
        gasPriceLabel.textProperty().bind(StringManager.getInstance().module.gasPriceLabel);
        gasLimitLabel.textProperty().bind(StringManager.getInstance().module.gasLimitLabel);
        detailLabel.textProperty().bind(StringManager.getInstance().module.detailLabel);
        detailContentsFee.textProperty().bind(StringManager.getInstance().module.detailContentsFee);
        detailContentsTotal.textProperty().bind(StringManager.getInstance().module.detailContentsTotal);
        gasPricePopupDefaultLabel.textProperty().bind(StringManager.getInstance().module.tab1DefaultLabel);
        lowLabel.textProperty().bind(StringManager.getInstance().module.tab1LowLabel);
        highLabel.textProperty().bind(StringManager.getInstance().module.tab1HighLabel);
        gasPriceTextField.setText(gasPrice.toString());
    }

    public void showGasPricePopup() {
        gasPricePlusMinusPane.setVisible(true);

        if(handler != null){
            handler.changeGasPricePopup(true);
        }
    }

    public void hideGasPricePopup() {
        gasPricePlusMinusPane.setVisible(false);

        if(handler != null){
            handler.changeGasPricePopup(false);
        }
    }

    public void settingLayoutData(){
        // fee
        BigInteger gasLimit = BigInteger.ZERO;
        if (gasLimitTextField.getText() != null && gasLimitTextField.getText().trim().length() > 0) {
            gasLimit = new BigInteger(gasLimitTextField.getText().trim().replaceAll("[^0-9]",""));
            this.gasLimit = gasLimit;
        }
        BigInteger fee = getGasPrice().multiply(gasLimit);

        // mineral
        BigInteger mineral = this.mineral;


        detailContentsFeeNum.textProperty().setValue(ApisUtil.readableApis(fee, ',',true));
        detailContentsTotalNum.textProperty().setValue(ApisUtil.readableApis(mineral, ',',true));
    }


    public void setMineral(BigInteger mineral){
        this.mineral = mineral;
        settingLayoutData();
    }
    public BigInteger getFee(){
        return getGasPrice().multiply(gasLimit);
    }
    public BigInteger getTotalFee(){
        BigInteger totalFee = getFee().subtract(this.mineral);
        return totalFee;
    }
    public BigInteger getGasPrice(){
        return this.gasPrice.multiply(new BigInteger("1000000000"));
    }
    public BigInteger getGasLimit(){
        return this.gasLimit;
    }
    public void setGasLimit(String gaslimit) {
        gasLimitTextField.textProperty().set(gaslimit);
        settingLayoutData();
    }

    private GasCalculatorImpl handler;
    public void setHandler(GasCalculatorImpl handler) { this.handler = handler;}
    public interface GasCalculatorImpl{
        void gasLimitTextFieldFocus(boolean isFocused);
        void gasLimitTextFieldChangeValue(String oldValue, String newValue);
        void gasPriceSliderChangeValue(int value);
        void changeGasPricePopup(boolean isVisible);
    }
}
