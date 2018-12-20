package org.apis.gui.controller.buymineral;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import org.apis.gui.controller.module.MessageLineController;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.module.textfield.ApisAddressFieldController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.InputConditionManager;
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
    @FXML private Label apisTotalBalance, apisTotalLabel;
    @FXML private TextField amount;
    @FXML private ApisAddressFieldController beneficiaryTextFieldController;
    @FXML private ApisSelectBoxController beneficiaryController, payerController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private ApisButtonEsimateGasLimitController btnByteCodePreGasUsedController;
    @FXML private MessageLineController payerMessageController;

    private boolean isBeneficiarySelected = true;
    private boolean isSuccessed = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();


        AppManager.settingTextFieldStyle(amount);

        beneficiaryController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, false);
        payerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, false);

        beneficiaryTextFieldController.setHandler(new ApisAddressFieldController.ApisAddressFieldImpl() {
            @Override
            public void change(String oldValue, String newValue) {
                settingLayoutData();
            }
        });
        beneficiaryTextFieldController.setVisible(false);

        beneficiaryController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {

            }

            @Override
            public void onSelectItem() {
                settingLayoutData();
            }
        });
        beneficiaryController.setVisible(true);

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
        });

        amount.textProperty().addListener(InputConditionManager.onlyNumberListener());
        amount.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                BigInteger apis = new BigInteger(ApisUtil.convert(newValue.replaceAll(",",""), ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS, ',', false).replaceAll(",","").replaceAll("\\.",""));
                BigInteger mineral = getCalMineral(apis);
                BigInteger percent = BigInteger.ZERO;

                if(apis.compareTo(BigInteger.ZERO) > 0){
                    percent = mineral.multiply(BigInteger.valueOf(100)).divide(apis).subtract(BigInteger.valueOf(100));
                }

                BuyMineralBodyController.this.percent.setText("+"+percent.toString()+"%");
                BuyMineralBodyController.this.percentInput.setText("+"+percent.toString()+"%");


                isSuccessed = false;

                bonusMineral.setText(ApisUtil.readableApis(mineral, ',', true));
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
        mineralDetailSelectHead.textProperty().bind(StringManager.getInstance().buymineral.mineralDetailSelectHead);
        apisTotalLabel.textProperty().bind(StringManager.getInstance().common.total);
        payerMessageController.setFailed(StringManager.getInstance().common.notEnoughBalance);
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
            beneficiaryTextFieldController.setText("");
            beneficiaryController.setVisible(false);
            beneficiaryTextFieldController.setVisible(true);
        } else {
            isBeneficiarySelected = true;
            StyleManager.backgroundColorStyle(beneficiaryInputButton, StyleManager.AColor.Cf8f8fb);
            StyleManager.borderColorStyle(beneficiaryInputButton, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(beneficiaryInputButton, StyleManager.AColor.C999999);
            beneficiaryController.setVisible(true);
            beneficiaryTextFieldController.setVisible(false);
        }

        if(handelr != null){
            handelr.settingLayoutData();
        }
    }

    @FXML
    public void estimateGasLimit(){
        BigInteger value = getAmount();
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
                label.setStyle(new JavaFXStyle(label.getStyle()).add("-fx-background-color","#f8f8fb").toString());
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
                amount.setText(ApisUtil.readableApis(apis, ',',true).split(" ")[0].replaceAll(",",""));

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
        boolean isCheckAddress = true;
        boolean isCheckBalance = true;
        boolean isCheckGasLimit = true;

        String address = getBeneficiaryAddress();
        if(address == null || address.length() <= 0){
            isCheckAddress = false;
        }

        BigInteger payerTotalApis = payerController.getBalance();
        apisTotalBalance.setText(ApisUtil.readableApis(payerTotalApis, ',', true));

        BigInteger mineral = payerController.getMineral();
        gasCalculatorController.setMineral(mineral);

        BigInteger chargedFee = gasCalculatorController.getTotalFee();
        if(chargedFee.compareTo(BigInteger.ZERO) <= 0){
            chargedFee = BigInteger.ZERO;
        }

        String amount = this.amount.getText();
        amount = ApisUtil.convert(amount, ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS,',',false).replaceAll(",","").replaceAll("\\.","");
        BigInteger chargedAmount = new BigInteger(amount);
        chargedAmount = chargedAmount.add(chargedFee);

        BigInteger gasLimit = gasCalculatorController.getGasLimit();
        if(gasLimit.compareTo(BigInteger.ZERO) <= 0){
            isCheckGasLimit = false;
        }

        if(payerTotalApis.subtract(chargedAmount).compareTo(BigInteger.ZERO) < 0){
            // show not enough balance message
            payerMessageController.setVisible(true);
            btnByteCodePreGasUsedController.setCompiled(false);
            isCheckBalance = false;
        }else{
            // hiden not enough balance message
            payerMessageController.setVisible(false);
            btnByteCodePreGasUsedController.setCompiled(true);
        }


        if(isCheckAddress && isCheckBalance && isCheckGasLimit){
            isSuccessed = true;
        }else{
            isSuccessed = false;
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
            return this.beneficiaryTextFieldController.getAddress();
        }
    }

    public String getMask() {
        if(AddressUtil.isAddress(getBeneficiaryAddress())){
            return AppManager.getInstance().getMaskWithAddress(getBeneficiaryAddress());
        }else{
            return null;
        }
    }

    public String getChargedFee() {
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

    public BigInteger getAmount() {
        String amount = ApisUtil.convert(this.amount.getText().trim().replaceAll(",",""), ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS, ',', false).replaceAll(",","");
        BigInteger value = new BigInteger(amount);
        return value;
    }

    public BigInteger getChargedAmount(){
        BigInteger mineral = payerController.getMineral();
        gasCalculatorController.setMineral(mineral);

        BigInteger chargedFee = gasCalculatorController.getTotalFee();
        if(chargedFee.compareTo(BigInteger.ZERO) <= 0){
            chargedFee = BigInteger.ZERO;
        }

        String amount = this.amount.getText();
        amount = ApisUtil.convert(amount, ApisUtil.Unit.APIS, ApisUtil.Unit.aAPIS,',',false).replaceAll(",","").replaceAll("\\.","");
        BigInteger chargedAmount = new BigInteger(amount);
        chargedAmount = chargedAmount.add(chargedFee);
        return chargedAmount;
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
