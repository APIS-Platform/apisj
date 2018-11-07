package org.apis.gui.controller.buymineral;

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
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.manager.AppManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralBodyController extends BaseViewController {

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_BUY_MINERAL);
    private byte[] buyMineralAddress =  AppManager.getInstance().constants.getBUY_MINERAL();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionBuyMNR = contract.getByName("buyMNR");

    private final String[] chargeAmountSelectTextList = { "1", "10", "100", "1,000", "10,000", "100,000", "1,000,000"};
    private final String[] mineralDetailSelectTextList = {
            "1 APIS = 1 MNR (0% 보너스)",
            "10 APIS = 11 MNR (10%)",
            "100 APIS = 120 MNR (20%)",
            "1,000 APIS = 1,300 MNR (30%)",
            "10,000 APIS = 14,000 MNR (40%)",
            "100,000 APIS = 150,000 MNR (50%)",
            "1,000,000 APIS = 1,600,000 MNR (60%)"};

    @FXML private VBox chargeAmountSelectChild, mineralDetailSelectChild;
    @FXML private VBox chargeAmountSelectList, mineralDetailSelectList;
    @FXML private ScrollPane chargeAmountSelectListView, mineralDetailSelectListView;
    @FXML private Label chargeAmountSelectHead, mineralDetailSelectHead, beneficiaryInputButton, bonusMineral;
    @FXML private TextField beneficiaryTextField, chargeAmount;
    @FXML private ApisSelectBoxController beneficiaryController, payerController;
    @FXML private GasCalculatorController gasCalculatorController;

    private boolean isBeneficiarySelected = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        beneficiaryController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        payerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);


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

        initChargeAmountSelectBox();
        initMineralDetailSelectBox();
        hideSelectList(chargeAmountSelectListView, chargeAmountSelectList);
        hideSelectList(mineralDetailSelectListView, mineralDetailSelectList);

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
            beneficiaryInputButton.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                    "-fx-border-color: #000000; -fx-text-fill: #ffffff; -fx-background-color: #000000;");
            beneficiaryTextField.setText("");
            beneficiaryController.setVisible(false);
            beneficiaryTextField.setVisible(true);
        } else {
            isBeneficiarySelected = true;
            beneficiaryInputButton.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                    "-fx-border-color: #999999; -fx-text-fill: #999999; -fx-background-color: #f2f2f2;");
            beneficiaryController.setVisible(true);
            beneficiaryTextField.setVisible(false);
        }
    }



    public void initChargeAmountSelectBox(){
        chargeAmountSelectList.getChildren().clear();
        for(int i=0; i<chargeAmountSelectTextList.length; i++){
            addSelectBoxItem(chargeAmountSelectList, chargeAmountSelectTextList[i]+" APIS");
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
        for(int i=0; i<mineralDetailSelectTextList.length; i++){
            addSelectBoxItem(mineralDetailSelectList, mineralDetailSelectTextList[i]);
        }

        mineralDetailSelectChild.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideSelectList(mineralDetailSelectListView, mineralDetailSelectList);
            }
        });
    }

    private void addSelectBoxItem(VBox list, String text){
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle(new JavaFXStyle(anchorPane.getStyle()).add("-fx-background-color","#ffffff").toString());
        Label label = new Label();
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
                if(list == chargeAmountSelectList){
                    chargeAmountSelectHead.setText(label.getText());
                    chargeAmount.setText(label.getText().split(" ")[0].replaceAll(",",""));
                    bonusMineral.setText(Double.toString(getCalMineral(Double.parseDouble(chargeAmount.getText()))));

                    settingLayoutData();

                }
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

    private double getCalMineral(double apis){

        if(apis >= 1000000){
            return apis * 1.6;
        }else if(apis >= 100000){
            return apis * 1.5;
        }else if(apis >= 10000){
            return apis * 1.4;
        }else if(apis >= 1000){
            return apis * 1.3;
        }else if(apis >= 100){
            return apis * 1.2;
        }else if(apis >= 10){
            return apis * 1.1;
        }else {
            return apis;
        }
    }

    public String getAddress() {
        return this.beneficiaryController.getAddress();
    }

    public String getMask() {
        return AppManager.getInstance().getMaskWithAddress(getAddress());
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

    private BuyMineralBodyImpl handelr;
    public void setHandelr(BuyMineralBodyImpl handelr){
        this.handelr = handelr;
    }
    public interface BuyMineralBodyImpl{
        void settingLayoutData();
    }

}
