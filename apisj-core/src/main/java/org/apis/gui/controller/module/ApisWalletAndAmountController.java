package org.apis.gui.controller.module;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.util.blockchain.ApisUtil;

import java.awt.event.InputEvent;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisWalletAndAmountController extends BaseViewController {

    @FXML private AnchorPane rootPane, selectApisUnitPane;
    @FXML private GridPane tokenTotalPane;
    @FXML private Label amountToSendLabel, totalLabel, totalBalance;
    @FXML private TextField amountTextField;
    @FXML private ApisSelectBoxUnitController selectApisUnitController;
    @FXML private ApisSelectBoxPercentController selectPercentController;
    @FXML private ApisSelectBoxController selectWalletController;

    private BigInteger maxAmount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        selectWalletController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
        selectWalletController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {
                settingLayoutData();
            }

            @Override
            public void onSelectItem() {
                setMaxAmount(selectWalletController.getBalance());
                settingLayoutData();
            }
        });
        amountTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String afterValue = amountTextField.getText();
                if(afterValue.length() != 0){
                    afterValue = ApisUtil.clearNumber(amountTextField.getText());
                }

                // 소수점 최대개수 설정
                // ex) APIS의 경우 소수점 최대 18개, fAPIS의 경우 소수점 최대 3개
                String newValueSplit[] = afterValue.split("\\.");
                if(newValueSplit.length >=2 && newValueSplit[1].length() > ApisUtil.getDecimalPoint(selectApisUnitController.getSelectUnit())) {
                    if (selectApisUnitController.getSelectUnit() != ApisUtil.Unit.aAPIS) {
                        afterValue = newValueSplit[0] + "." + newValueSplit[1].substring(0, ApisUtil.getDecimalPoint(selectApisUnitController.getSelectUnit()));
                    } else{
                        afterValue = newValueSplit[0];
                    }
                }

                // 최대금액 이상으로 입력시 Amount를 최대금액으로 표기
                if(maxAmount != null){
                    if(maxAmount.compareTo(selectApisUnitController.convert(afterValue)) < 0){
                        afterValue = ApisUtil.convert(maxAmount.toString(), ApisUtil.Unit.aAPIS, selectApisUnitController.getSelectUnit(), ',',true).replaceAll(",","");
                    }
                }

                if(afterValue.indexOf('.') != afterValue.lastIndexOf('.')){
                    afterValue = oldValue;
                }

                amountTextField.setText(afterValue);
                settingLayoutData();
            }
        });
        amountTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String afterValue = amountTextField.getText();
                if(afterValue.length() != 0){
                    afterValue = ApisUtil.clearNumber(amountTextField.getText());
                }

                String newValueSplit[] = afterValue.split("\\.");
                // 소수점 2개 이상 입력시 두번째 소수점 뒤 숫자 무시
                if(newValueSplit.length >= 3){
                    afterValue = newValueSplit[0]+"."+newValueSplit[1];
                }

                // 소수점 최대개수 설정
                // ex) APIS의 경우 소수점 최대 18개, fAPIS의 경우 소수점 최대 3개
                if(newValueSplit.length >=2 && newValueSplit[1].length() > ApisUtil.getDecimalPoint(selectApisUnitController.getSelectUnit())) {
                    if (selectApisUnitController.getSelectUnit() != ApisUtil.Unit.aAPIS) {
                        afterValue = newValueSplit[0] + "." + newValueSplit[1].substring(0, ApisUtil.getDecimalPoint(selectApisUnitController.getSelectUnit()));
                    } else{
                        afterValue = newValueSplit[0];
                    }
                }

                // 최대금액 이상으로 입력시 Amount를 최대금액으로 표기
                if(maxAmount != null){
                    if(maxAmount.compareTo(selectApisUnitController.convert(afterValue)) < 0){
                        afterValue = ApisUtil.convert(maxAmount.toString(), ApisUtil.Unit.aAPIS, selectApisUnitController.getSelectUnit(), ',',true).replaceAll(",","");
                    }
                }
                amountTextField.setText(afterValue);
                settingLayoutData();
            }
        });

        selectApisUnitController.setHandler(new ApisSelectBoxUnitController.ApisSelectboxUnitImpl() {
            @Override
            public void onChange(String name, BigInteger value) {
                if(selectApisUnitController.getSelectUnit() == ApisUtil.Unit.aAPIS){
                    amountTextField.setText(amountTextField.getText().split("\\.")[0]);
                }else {
                    // 소수점 최대개수 설정
                    // ex) APIS의 경우 소수점 최대 18개, fAPIS의 경우 소수점 최대 3개
                    String newValueSplit[] = amountTextField.getText().split("\\.");
                    if(newValueSplit.length >=2 && newValueSplit[1].length() > ApisUtil.getDecimalPoint(selectApisUnitController.getSelectUnit())){
                        amountTextField.setText(newValueSplit[0] + "." + newValueSplit[1].substring(0, ApisUtil.getDecimalPoint(selectApisUnitController.getSelectUnit())));
                    }
                }

                // 최대금액 이상으로 입력시 Amount를 최대금액으로 표기
                if(maxAmount != null){
                    if(maxAmount.compareTo(selectApisUnitController.convert(amountTextField.getText())) < 0){
                        amountTextField.setText(ApisUtil.convert(maxAmount.toString(), ApisUtil.Unit.aAPIS, selectApisUnitController.getSelectUnit(), ',',true).replaceAll(",",""));
                    }
                }

                settingLayoutData();
            }
        });

        selectPercentController.setHandler(new ApisSelectBoxPercentController.ApisSelectboxPercentImpl() {
            @Override
            public void onChange(String name, BigInteger value) {

                BigInteger balance = getBalance();
                BigInteger percent = getPercent();
                amountTextField.setText(ApisUtil.convert(balance.multiply(percent).divide(BigInteger.valueOf(100)).toString(), ApisUtil.Unit.aAPIS, selectApisUnitController.getSelectUnit(), ',',true).replaceAll(",",""));

                settingLayoutData();
            }
        });

    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
    }

    private void settingLayoutData(){

        BigInteger amount = getAmount();
        BigInteger percent = BigInteger.ZERO;
        BigInteger balance = getBalance();
        BigInteger afterBalace = BigInteger.ZERO;

        selectPercentController.stateDefault();
        for(int i=0; i<selectPercentController.getPercentList().length; i++){
            percent = selectPercentController.getPercentList()[i];
            afterBalace = balance.multiply(percent).divide(BigInteger.valueOf(100));
            if(balance.compareTo(BigInteger.ZERO) == 0) {
                selectPercentController.setPercent("0%");
            } else {
                selectPercentController.setPercent(amount.multiply(BigInteger.valueOf(100)).divide(balance) + "%");
            }
            if(afterBalace.compareTo(amount) == 0){
                selectPercentController.stateActive();
                break;
            }
        }
        BigInteger afterBalance = balance.multiply(percent).divide(BigInteger.valueOf(100));

        if(amount.compareTo(afterBalance) == 0){
            selectPercentController.stateActive();
        }else{
            selectPercentController.stateDefault();
        }

        if(this.handler != null){
            handler.change(getAmount());
        }
    }

    /**
     * 해당 지갑의 자산으로 Amount 최대 값으로 사용한다.
     * @param maxAmount
     */
    public void setMaxAmount(BigInteger maxAmount){
        this.maxAmount = maxAmount;
        this.totalBalance.setText(ApisUtil.readableApis(this.maxAmount, ',', true));
    }
    public BigInteger getAmount(){
        return selectApisUnitController.convert(amountTextField.getText().trim());
    }

    public BigInteger getPercent(){
        return selectPercentController.getSelectPercent();
    }


    private ApisAmountImpl handler;
    public void setHandler(ApisAmountImpl handler){
        this.handler = handler;
    }

    public String getKeystoreId() {
        return this.selectWalletController.getKeystoreId();
    }

    public BigInteger getBalance() {
        return this.selectWalletController.getBalance();
    }

    public void setStage(int stageDefault) {
        this.selectWalletController.setStage(stageDefault);
    }

    public BigInteger getMineral() {
        return this.selectWalletController.getMineral();
    }


    public void selectedItemWithWalletId(String id) {
        this.selectWalletController.selectedItemWithWalletId(id);
    }

    public void walletSelectedItem(int index) {
        this.selectWalletController.selectedItem(index);
    }

    public void walletStateDefault() {
        this.selectWalletController.onStateDefault();
    }

    public void setVisibleWalletItemList(boolean isVisible) {
        this.selectWalletController.setVisibleItemList(isVisible);
    }

    public void setVisibleSelectApisUnit(boolean isVisible) {
        selectApisUnitPane.setVisible(isVisible);
        if(isVisible){
            AnchorPane.setTopAnchor(amountTextField, 0.0);
            AnchorPane.setRightAnchor(amountTextField, 160.0);
            AnchorPane.setBottomAnchor(amountTextField, 0.0);
            AnchorPane.setLeftAnchor(amountTextField, 0.0);

            // 토큰 토탈 보이기
            tokenTotalPane.setVisible(true);
            tokenTotalPane.setPrefWidth(-1);

        }else{
            AnchorPane.setTopAnchor(amountTextField, 0.0);
            AnchorPane.setRightAnchor(amountTextField, 80.0);
            AnchorPane.setBottomAnchor(amountTextField, 0.0);
            AnchorPane.setLeftAnchor(amountTextField, 0.0);

            // 토큰 토탈 숨기기
            tokenTotalPane.setVisible(false);
            tokenTotalPane.setPrefWidth(0);
        }
    }

    public void initLayoutData() {

    }

    public interface ApisAmountImpl{
        void change(BigInteger value);
    }

    public String getAddress(){
        return this.selectWalletController.getAddress();
    }


    public void update() {
        selectWalletController.update();
        setMaxAmount(selectWalletController.getBalance());
    }
}
