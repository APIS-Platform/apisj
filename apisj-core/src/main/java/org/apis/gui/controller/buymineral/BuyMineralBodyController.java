package org.apis.gui.controller.buymineral;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisButtonEsimateGasLimitController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralBodyController extends BaseViewController {

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_BUY_MINERAL);
    private byte[] buyMineralAddress =  AppManager.getInstance().constants.getBUY_MINERAL();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionBuyMNR = contract.getByName("buyMNR");
    private CallTransaction.Function functionCalcMNR = contract.getByName("calcMNR");

    private final String[] chargeAmountSelectTextList = { "1", "10", "100", "1,000", "10,000", "100,000", "1,000,000", "10,000,000"};

    @FXML private VBox chargeAmountSelectChild, mineralDetailSelectChild;
    @FXML private VBox chargeAmountSelectList, mineralDetailSelectList;
    @FXML private ScrollPane chargeAmountSelectListView, mineralDetailSelectListView;
    @FXML private Label chargeAmountSelectHead, mineralDetailSelectHead, beneficiaryInputButton, bonusMineral, percent, percentInput, titleLabel, chargeLabel, payerLabel, bonusLabel;
    @FXML private TextField beneficiaryTextField, chargeAmount;
    @FXML private ApisSelectBoxController beneficiaryController, payerController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private ApisButtonEsimateGasLimitController btnByteCodePreGasUsedController;

    private boolean isBeneficiarySelected = true;
    private boolean isSuccessed = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        AppManager.settingTextFieldStyle(beneficiaryTextField);
        AppManager.settingTextFieldStyle(chargeAmount);

        beneficiaryController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        payerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        beneficiaryTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(handelr != null){
                    handelr.settingLayoutData();
                }
            }
        });

        beneficiaryController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {

            }

            @Override
            public void onSelectItem() {
                settingLayoutData();
            }
        });

        payerController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {
            }

            @Override
            public void onSelectItem() {

                isSuccessed = false;
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

        chargeAmount.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                BigInteger apis = new BigInteger(ApisUtil.convert(newValue.split(" ")[0].replaceAll(",",""), ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS, ',', false).replaceAll(",","").replaceAll("\\.",""));
                BigInteger mineral = getCalMineral(apis);
                BigInteger percent = BigInteger.ZERO;

                if(apis.compareTo(BigInteger.ZERO) > 0){
                    percent = mineral.multiply(BigInteger.valueOf(100)).divide(apis).subtract(BigInteger.valueOf(100));
                }

                chargeAmount.setText(ApisUtil.readableApis(apis, ',',true));
                bonusMineral.setText(ApisUtil.readableApis(mineral, ',', true));

                BuyMineralBodyController.this.percent.setText("+"+percent.toString()+"%");
                BuyMineralBodyController.this.percentInput.setText("+"+percent.toString()+"%");


                isSuccessed = false;
                settingLayoutData();
            }
        });

        btnByteCodePreGasUsedController.setHandler(new ApisButtonEsimateGasLimitController.ApisButtonEsimateGasLimitImpl() {
            @Override
            public void onMouseClicked(ApisButtonEsimateGasLimitController controller) {
                isSuccessed = true;
                estimateGasLimit();
                settingLayoutData();
            }
        });


        initChargeAmountSelectBox();
        initMineralDetailSelectBox();
        hideSelectList(chargeAmountSelectListView, chargeAmountSelectList);
        hideSelectList(mineralDetailSelectListView, mineralDetailSelectList);
        settingLayoutData();
    }

    private void languageSetting(){
        titleLabel.textProperty().bind(StringManager.getInstance().buymineral.titleLabel);
        chargeLabel.textProperty().bind(StringManager.getInstance().buymineral.chargeLabel);
        bonusLabel.textProperty().bind(StringManager.getInstance().buymineral.bonusLabel);
        payerLabel.textProperty().bind(StringManager.getInstance().common.payerLabel);
        beneficiaryInputButton.textProperty().bind(StringManager.getInstance().common.directInputButton);
    }

    @FXML
    public void onMouseClickedChargeAmount(){
        if(chargeAmountSelectListView.isVisible()){
            hideSelectList(chargeAmountSelectListView, chargeAmountSelectList);
        }else{
            showSelectList(chargeAmountSelectListView, chargeAmountSelectList);
        }
    }

    @FXML
    public void onMouseClickedDetail(){
        if(mineralDetailSelectListView.isVisible()){
            hideSelectList(mineralDetailSelectListView, mineralDetailSelectList);
        }else{
            showSelectList(mineralDetailSelectListView, mineralDetailSelectList);
        }
    }

    @FXML
    public void onMouseClickedDirectInput(){
        if(isBeneficiarySelected) {
            isBeneficiarySelected = false;

            StyleManager.backgroundColorStyle(beneficiaryInputButton, StyleManager.AColor.C000000);
            StyleManager.borderColorStyle(beneficiaryInputButton, StyleManager.AColor.C000000);
            StyleManager.fontColorStyle(beneficiaryInputButton, StyleManager.AColor.Cffffff);
            beneficiaryTextField.setText("");
            beneficiaryController.setVisible(false);
            beneficiaryTextField.setVisible(true);
        } else {
            isBeneficiarySelected = true;
            StyleManager.backgroundColorStyle(beneficiaryInputButton, StyleManager.AColor.Cf2f2f2);
            StyleManager.borderColorStyle(beneficiaryInputButton, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(beneficiaryInputButton, StyleManager.AColor.C999999);
            beneficiaryController.setVisible(true);
            beneficiaryTextField.setVisible(false);
        }

        if(handelr != null){
            handelr.settingLayoutData();
        }
    }

    @FXML
    public void estimateGasLimit(){
        BigInteger value = getValue();
        String functionName = "buyMNR";
        byte[] from = Hex.decode(payerController.getAddress());
        byte[] to = buyMineralAddress;
        Object[] args = new Object[1];
        args[0] = Hex.decode(beneficiaryController.getAddress());
        long preGasUsed = AppManager.getInstance().getPreGasUsed(abi, from, to, value, functionName, args);
        gasCalculatorController.setGasLimit(Long.toString(preGasUsed));
    }

    public void initChargeAmountSelectBox(){
        chargeAmountSelectList.getChildren().clear();
        for(int i=0; i<chargeAmountSelectTextList.length; i++){
            addSelectBoxItem(chargeAmountSelectList, chargeAmountSelectTextList[i]+" APIS", chargeAmountSelectTextList[i]);
        }

        chargeAmountSelectChild.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideSelectList(chargeAmountSelectListView, chargeAmountSelectList);
            }
        });
    }

    public void initMineralDetailSelectBox(){
        mineralDetailSelectList.getChildren().clear();
        for(int i=0; i<chargeAmountSelectTextList.length; i++){
            BigInteger apis = new BigInteger(ApisUtil.convert(chargeAmountSelectTextList[i].replaceAll(",",""), ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS, ',', false).replaceAll(",","").replaceAll("\\.",""));
            BigInteger mineral = getCalMineral(apis);
            BigInteger percent = mineral.multiply(BigInteger.valueOf(100)).divide(apis).subtract(BigInteger.valueOf(100));

            String text = chargeAmountSelectTextList[i]+" APIS = "+ApisUtil.readableApis(mineral, ',', true)+" MNR (+"+percent.toString()+"%)";
            addSelectBoxItem(mineralDetailSelectList, text, chargeAmountSelectTextList[i]);
        }

        mineralDetailSelectChild.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideSelectList(mineralDetailSelectListView, mineralDetailSelectList);
            }
        });
    }

    private void addSelectBoxItem(VBox list, String text, String value){
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle(new JavaFXStyle(anchorPane.getStyle()).add("-fx-background-color","#ffffff").toString());
        Label label = new Label();
        label.setId(value);
        label.setText(text);
        label.setPadding(new Insets(8,16,8,16));
        label.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                label.setStyle(new JavaFXStyle(label.getStyle()).add("-fx-background-color","#f2f2f2").toString());
            }
        });
        label.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                label.setStyle(new JavaFXStyle(label.getStyle()).add("-fx-background-color","#ffffff").toString());
            }
        });

        // method list click
        label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                chargeAmountSelectHead.setText(label.getId()+" APIS");

                BigInteger apis = new BigInteger(ApisUtil.convert(label.getId().replaceAll(",",""), ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS, ',', false).replaceAll(",","").replaceAll("\\.",""));
                BigInteger mineral = getCalMineral(apis);
                BigInteger percent = mineral.multiply(BigInteger.valueOf(100)).divide(apis).subtract(BigInteger.valueOf(100));

                bonusMineral.setText(ApisUtil.readableApis(mineral, ',', true));
                chargeAmount.setText(ApisUtil.readableApis(apis, ',',true).split(" ")[0].replaceAll(",",""));

                BuyMineralBodyController.this.percent.setText("+"+percent.toString()+"%");
                BuyMineralBodyController.this.percentInput.setText("+"+percent.toString()+"%");

                settingLayoutData();
            }
        });
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        anchorPane.getChildren().add(label);
        list.getChildren().add(anchorPane);
    }

    public void settingLayoutData(){
        gasCalculatorController.setMineral(payerController.getMineral());

        String apis = chargeAmount.getText();
        if(apis != null && !apis.equals("0") && apis.length() > 0){
            btnByteCodePreGasUsedController.setCompiled(true);
        }else{
            btnByteCodePreGasUsedController.setCompiled(false);
        }


        if(handelr != null){
            handelr.settingLayoutData();
        }
    }

    public void showSelectList(ScrollPane scrollPane, VBox list){
        scrollPane.setVisible(true);
        scrollPane.prefHeightProperty().setValue(-1);
        list.prefHeightProperty().setValue(-1);
    }

    public void hideSelectList(ScrollPane scrollPane, VBox list){
        scrollPane.setVisible(false);
        scrollPane.prefHeightProperty().setValue(0);
        list.prefHeightProperty().setValue(40);
    }

    private BigInteger getCalMineral(BigInteger apis){
        if(apis == null ){
            apis = BigInteger.ZERO;
        }
        String functionName = functionCalcMNR.name;
        String contractAddress = ByteUtil.toHexString(buyMineralAddress);
        String medataAbi = this.abi;


        // 데이터 불러오기
        Object[] args = new Object[1];
        args[0] = apis;
        CallTransaction.Contract contract = new CallTransaction.Contract(medataAbi);
        Object[] result = AppManager.getInstance().callConstantFunction(contractAddress, contract.getByName(functionName), args);

        if(result[0].toString() == null || result[0].toString().length() <= 0){
            return BigInteger.ZERO;
        }
        return new BigInteger(""+result[0]);
    }

    public String getBeneficiaryAddress() {
        if(isBeneficiarySelected){
            return this.beneficiaryController.getAddress();
        }else{
            return this.beneficiaryTextField.getText().trim();
        }
    }

    public String getMask() {
        if(AddressUtil.isAddress(getBeneficiaryAddress())){
            return AppManager.getInstance().getMaskWithAddress(getBeneficiaryAddress());
        }else{
            return null;
        }
    }

    public String getTotalFee() {
        if(gasCalculatorController.getTotalFee().compareTo(BigInteger.ZERO) >= 0) {
            return ApisUtil.readableApis(gasCalculatorController.getTotalFee(), ',', true);
        }else{
            return "0";
        }
    }

    public String getPayerAddress() {
        return this.payerController.getAddress();
    }

    public String getContractAddress() {
        return ByteUtil.toHexString(buyMineralAddress);
    }

    public BigInteger getGasPrice() {
        return this.gasCalculatorController.getGasPrice();
    }

    public BigInteger getGasLimit() {
        return this.gasCalculatorController.getGasLimit();
    }

    public BigInteger getValue() {
        String changeAmount = ApisUtil.convert(this.chargeAmount.getText().trim().replaceAll(",",""), ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS, ',', false).replaceAll(",","");
        BigInteger value = new BigInteger(changeAmount);
        return value;
    }

    public boolean isSuccessed(){
        return isSuccessed;
    }


    private BuyMineralBodyImpl handelr;
    public void setHandelr(BuyMineralBodyImpl handelr){
        this.handelr = handelr;
    }
    public interface BuyMineralBodyImpl{
        void settingLayoutData();
    }

}
