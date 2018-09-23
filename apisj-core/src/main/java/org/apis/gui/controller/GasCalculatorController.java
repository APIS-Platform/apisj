package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class GasCalculatorController implements Initializable {
    // Gas Price Popup Flag
    private static final boolean GAS_PRICE_POPUP_MOUSE_ENTERED = true;
    private static final boolean GAS_PRICE_POPUP_MOUSE_EXITED = false;
    private boolean gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;

    @FXML private AnchorPane rootPane, gasPricePlusMinusPane;
    @FXML private TextField gasLimitTextField;
    @FXML private GridPane gasPriceGrid, gasPricePopupGrid;
    @FXML private ImageView gasPriceMinusBtn, gasPricePlusBtn, gasPricePopupImg;
    @FXML private ProgressBar progressBar;
    @FXML private Slider slider;
    @FXML private Label gasPriceTitle, gasPriceFormula, gasPriceLabel, gasPricePlusMinusLabel, gasLimitLabel, gasPricePopupLabel,gasPricePopupDefaultLabel, detailLabel
            ,detailContentsFeeNum, detailContentsFee, detailContentsTotalNum, detailContentsTotal, lowLabel, highLabel;

    private BigInteger gasPrice = new BigInteger("50"); // Default Gas Price 50
    private BigInteger gasLimit = new BigInteger("0");
    private BigInteger mineral = new BigInteger("0");
    private long minGasLimit = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        gasLimitTextField.focusedProperty().addListener(gasLimitFocuesedListener);
        gasLimitTextField.textProperty().addListener(gasLimitTextListener);
        slider.valueProperty().addListener(sliderListener);

        hideGasPricePopup();
        settingLayoutData();
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxid = ((Node)event.getSource()).getId();

        // Gas Price
        if(fxid.equals("gasPricePlusMinusPane")) {
            if (!gasPricePopupGrid.isVisible()) {
                showGasPricePopup();
                slider.requestFocus();
            }else{
                hideGasPricePopup();
            }
            event.consume();

        } else if(fxid.equals("gasPriceMinusBtn")) {
            slider.setValue(slider.getValue()-10);
            slider.requestFocus();
            event.consume();

        } else if(fxid.equals("gasPricePlusBtn")) {
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

    private ChangeListener<Boolean> gasLimitFocuesedListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
            settingLayoutData();

            if(GasCalculatorController.this.handler != null){
                GasCalculatorController.this.handler.gasLimitTextFieldFocus(gasLimitTextField.isFocused());
            }
        }
    };
    private void textFieldFocus() {
        if(gasLimitTextField.isFocused()) {
            gasLimitTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        } else {
            gasLimitTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
    }

    private ChangeListener<String> gasLimitTextListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            // 숫자만 입력받도록 설정
            if (!newValue.matches("[\\d]*")) {
                gasLimitTextField.setText(newValue.replaceAll("[^\\d]", ""));
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

            gasPrice = new BigInteger(""+newValue.intValue());
            gasPricePlusMinusLabel.textProperty().set(gasPrice.toString()+" nAPIS");

            // (Default) 라는 문구 표기/숨기기
            gasPricePopupDefaultLabel.setVisible(newValue.intValue() == 50);

            settingLayoutData();
            if(handler != null){
                handler.gasPriceSliderChangeValue(newValue.intValue());
            }
        }
    };

    public void languageSetting() {
        gasPriceTitle.textProperty().bind(StringManager.getInstance().smartContract.gasPriceTitle);
        gasPriceFormula.textProperty().bind(StringManager.getInstance().smartContract.gasPriceFormula);
        gasPriceLabel.textProperty().bind(StringManager.getInstance().smartContract.gasPriceLabel);
        gasLimitLabel.textProperty().bind(StringManager.getInstance().smartContract.gasLimitLabel);
        detailLabel.textProperty().bind(StringManager.getInstance().smartContract.detailLabel);
        detailContentsFee.textProperty().bind(StringManager.getInstance().smartContract.detailContentsFee);
        detailContentsTotal.textProperty().bind(StringManager.getInstance().smartContract.detailContentsTotal);
        gasPricePopupDefaultLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1DefaultLabel);
        lowLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1LowLabel);
        highLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1HighLabel);

        gasPricePopupLabel.textProperty().bind(gasPricePlusMinusLabel.textProperty());
        gasPricePlusMinusLabel.textProperty().set(gasPrice +" nAPIS");
    }

    public void showGasPricePopup() {
        gasPricePlusMinusLabel.setTextFill(Color.web("#2b2b2b"));
        gasPriceGrid.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius: 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        gasPricePopupGrid.setVisible(true);
        gasPricePopupGrid.prefHeightProperty().setValue(-1);
        gasPricePopupImg.setVisible(true);
        gasPricePopupImg.prefHeight(90);
    }

    public void hideGasPricePopup() {
        gasPricePlusMinusLabel.setTextFill(Color.web("#999999"));
        gasPriceGrid.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius: 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        gasPricePopupGrid.setVisible(false);
        gasPricePopupGrid.prefHeightProperty().setValue(0);
        gasPricePopupImg.setVisible(false);
        gasPricePopupImg.prefHeight(1);
    }

    public void settingLayoutData(){
        // fee
        BigInteger gasLimit = new BigInteger("0");
        if (gasLimitTextField.getText() != null && gasLimitTextField.getText().trim().length() > 0) {
            gasLimit = new BigInteger(gasLimitTextField.getText().trim().replaceAll("[^0-9]",""));
            this.gasLimit = gasLimit;
        }
        BigInteger fee = getGasPrice().multiply(gasLimit);
        String sfee = fee.toString();
        String[] feeSplit = AppManager.addDotWidthIndex(sfee).split("\\.");

        // mineral
        BigInteger mineral = this.mineral;
        String sMineral = mineral.toString();
        String[] mineralSplit = AppManager.addDotWidthIndex(sMineral).split("\\.");


        detailContentsFeeNum.textProperty().setValue(AppManager.comma(feeSplit[0]) + "." + feeSplit[1]);
        detailContentsTotalNum.textProperty().setValue(AppManager.comma(mineralSplit[0]) + "." + mineralSplit[1]);
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
    }
}
