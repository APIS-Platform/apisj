package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SmartContractController implements Initializable {
    // Gas Price Popup Flag
    private static final boolean GAS_PRICE_POPUP_MOUSE_ENTERED = true;
    private static final boolean GAS_PRICE_POPUP_MOUSE_EXITED = false;
    private boolean gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;
    // Gas Price
    private BigInteger gasPrice = new BigInteger("50");

    @FXML
    private Label tabLabel1, tabLabel2, tabLabel3, tabLabel4, sideTabLabel1, sideTabLabel2;
    @FXML
    private Pane tabLinePane1, tabLinePane2, tabLinePane3, tabLinePane4, sideTabLinePane1, sideTabLinePane2;
    @FXML
    private AnchorPane tab1LeftPane, tab1RightPane, tab2LeftPane;
    @FXML
    private AnchorPane tab1AmountPane, tab2AmountPane, tab2ReadWritePane;
    @FXML
    private GridPane tab1GasPriceGrid, tab1GasPricePopupGrid, tab2GasPriceGrid, tab2GasPricePopupGrid;
    @FXML
    private GridPane transferBtn;
    @FXML
    private Label tab1GasPricePlusMinusLabel, tab2GasPricePlusMinusLabel, tab1GasPricePopupLabel, tab2GasPricePopupLabel, tab1GasPricePopupDefaultLabel, tab2GasPricePopupDefaultLabel;
    @FXML
    private Label cSelectHeadText, cSelectItemDefaultText, cSelectItemBalanceText;
    @FXML
    private ImageView cSelectHeadImg, tab1GasPricePopupImg, tab2GasPricePopupImg, tab1GasPriceMinusBtn, tab2GasPriceMinusBtn, tab1GasPricePlusBtn, tab2GasPricePlusBtn;
    @FXML
    private VBox cSelectList, cSelectChild;
    @FXML
    private GridPane cSelectHead, cSelectItemDefault, cSelectItemBalance;
    @FXML
    private VBox pSelectList, pSelectChild;
    @FXML
    private GridPane pSelectHead, pSelectItem100, pSelectItem75, pSelectItem50, pSelectItem25, pSelectItem10;
    @FXML
    private VBox pSelectList_1, pSelectChild_1;
    @FXML
    private GridPane pSelectHead_1, pSelectItem100_1, pSelectItem75_1, pSelectItem50_1, pSelectItem25_1, pSelectItem10_1;
    @FXML
    private TextField tab1AmountTextField, tab2AmountTextField, tab1GasLimitTextField, tab2GasLimitTextField, tab2ReadWriteTextField1, tab2ReadWriteTextField2;
    @FXML
    private TextArea tab1SolidityTextArea1, tab1SolidityTextArea2;
    @FXML
    private ProgressBar tab1ProgressBar, tab2ProgressBar;
    @FXML
    private Slider tab1Slider, tab2Slider;

    private Image downGrey, downWhite;
    // Percentage Select Box Lists
    private ArrayList<VBox> pSelectLists = new ArrayList<>();
    private ArrayList<VBox> pSelectChildList = new ArrayList<>();
    private ArrayList<GridPane> pSelectHeadList = new ArrayList<>();
    private ArrayList<GridPane> pSelectItem100List = new ArrayList<>();
    private ArrayList<GridPane> pSelectItem75List = new ArrayList<>();
    private ArrayList<GridPane> pSelectItem50List = new ArrayList<>();
    private ArrayList<GridPane> pSelectItem25List = new ArrayList<>();
    private ArrayList<GridPane> pSelectItem10List = new ArrayList<>();
    // Gas Price Slider Lists
    private ArrayList<GridPane> gasPriceGridList = new ArrayList<>();
    private ArrayList<GridPane> gasPricePopupGridList = new ArrayList<>();
    private ArrayList<Label> gasPricePlusMinusLabelList = new ArrayList<>();
    private ArrayList<Label> gasPricePopupLabelList = new ArrayList<>();
    private ArrayList<Label> gasPricePopupDefaultLabelList = new ArrayList<>();
    private ArrayList<ImageView> gasPriceMinusBtnList = new ArrayList<>();
    private ArrayList<ImageView> gasPricePlusBtnList = new ArrayList<>();
    private ArrayList<ImageView> gasPricePopupImgList = new ArrayList<>();
    private ArrayList<Slider> gasPriceSliderList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTabClean();
        initSideTabClean();

        this.tab1LeftPane.setVisible(true);
        this.tab1RightPane.setVisible(true);
        this.transferBtn.setVisible(true);
        this.tabLabel1.setTextFill(Color.web("#910000"));
        this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
        this.tabLinePane1.setVisible(true);
        this.sideTabLabel1.setTextFill(Color.web("#910000"));
        this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        this.sideTabLinePane1.setVisible(true);

        // Image Setting
        downGrey = new Image("image/ic_down_gray@2x.png");
        downWhite = new Image("image/ic_down_white@2x.png");

        // Percentage Select Box List Handling
        pSelectLists.add(pSelectList);
        pSelectChildList.add(pSelectChild);
        pSelectHeadList.add(pSelectHead);
        pSelectItem100List.add(pSelectItem100);
        pSelectItem75List.add(pSelectItem75);
        pSelectItem50List.add(pSelectItem50);
        pSelectItem25List.add(pSelectItem25);
        pSelectItem10List.add(pSelectItem10);

        pSelectLists.add(pSelectList_1);
        pSelectChildList.add(pSelectChild_1);
        pSelectHeadList.add(pSelectHead_1);
        pSelectItem100List.add(pSelectItem100_1);
        pSelectItem75List.add(pSelectItem75_1);
        pSelectItem50List.add(pSelectItem50_1);
        pSelectItem25List.add(pSelectItem25_1);
        pSelectItem10List.add(pSelectItem10_1);

        hidePercentSelectBox(0);
        hidePercentSelectBox(1);

        // Contract Read and Write Select Box List Handling
        initContract();
        hideContractSelectBox();

        tab1AmountTextField.focusedProperty().addListener(tab1AmountListener);
        tab1GasLimitTextField.focusedProperty().addListener(tab1GasLimitListener);
        tab2AmountTextField.focusedProperty().addListener(tab2AmountListener);
        tab2GasLimitTextField.focusedProperty().addListener(tab2GasLimitListener);

        // Progress Bar and Slider Handling
        tab1Slider.valueProperty().addListener(tab1SliderListener);
        tab2Slider.valueProperty().addListener(tab2SliderListener);

        gasPriceGridList.add(tab1GasPriceGrid);
        gasPricePopupGridList.add(tab1GasPricePopupGrid);
        gasPricePopupImgList.add(tab1GasPricePopupImg);
        gasPricePlusMinusLabelList.add(tab1GasPricePlusMinusLabel);
        gasPricePopupLabelList.add(tab1GasPricePopupLabel);
        gasPricePopupDefaultLabelList.add(tab1GasPricePopupLabel);
        gasPriceMinusBtnList.add(tab1GasPriceMinusBtn);
        gasPricePlusBtnList.add(tab1GasPricePlusBtn);
        gasPriceSliderList.add(tab1Slider);

        gasPriceGridList.add(tab2GasPriceGrid);
        gasPricePopupGridList.add(tab2GasPricePopupGrid);
        gasPricePopupImgList.add(tab2GasPricePopupImg);
        gasPricePlusMinusLabelList.add(tab2GasPricePlusMinusLabel);
        gasPricePopupLabelList.add(tab2GasPricePopupLabel);
        gasPricePopupDefaultLabelList.add(tab2GasPricePopupLabel);
        gasPriceMinusBtnList.add(tab2GasPriceMinusBtn);
        gasPricePlusBtnList.add(tab2GasPricePlusBtn);
        gasPriceSliderList.add(tab2Slider);

        for(int i=0; i<gasPriceGridList.size(); i++){
            this.gasPricePopupLabelList.get(i).textProperty().bind(gasPricePlusMinusLabelList.get(i).textProperty());
            this.gasPricePlusMinusLabelList.get(i).textProperty().set(gasPrice+" nAPIS");
            hideGasPricePopup(i);
        }

        // Text Area Listener
        tab1SolidityTextArea1.focusedProperty().addListener(tab1TextAreaListener);
        tab1SolidityTextArea2.focusedProperty().addListener(tab1TextAreaListener);

    }

    private ChangeListener<Boolean> tab1AmountListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> tab1GasLimitListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> tab2AmountListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> tab2GasLimitListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Number> tab1SliderListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            tab1ProgressBar.setProgress((newValue.doubleValue()-tab1Slider.getMin()) / (tab1Slider.getMax()-tab1Slider.getMin()));

            gasPrice = new BigInteger(""+newValue.intValue());
            tab1GasPricePlusMinusLabel.textProperty().set(gasPrice.toString()+" nAPIS");
            if(newValue.intValue() != 50) {
                tab1GasPricePopupDefaultLabel.setVisible(false);
            } else {
                tab1GasPricePopupDefaultLabel.setVisible(true);
            }
        }
    };

    private ChangeListener<Number> tab2SliderListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            tab2ProgressBar.setProgress((newValue.doubleValue()-tab2Slider.getMin()) / (tab2Slider.getMax()-tab2Slider.getMin()));

            gasPrice = new BigInteger(""+newValue.intValue());
            tab2GasPricePlusMinusLabel.textProperty().set(gasPrice.toString()+" nAPIS");
            if(newValue.intValue() != 50) {
                tab2GasPricePopupDefaultLabel.setVisible(false);
            } else {
                tab2GasPricePopupDefaultLabel.setVisible(true);
            }
        }
    };

    private ChangeListener<Boolean> tab1TextAreaListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue) {
                hideGasPricePopupAll();
            }
        }
    };

    public void textFieldFocus() {
        hideGasPricePopupAll();

        if(tab1AmountTextField.isFocused()) {
            tab1AmountPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        } else {
            tab1AmountPane.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        }

        if(tab1GasLimitTextField.isFocused()) {
            tab1GasLimitTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        } else {
            tab1GasLimitTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }

        if(tab2AmountTextField.isFocused()) {
            tab2AmountPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        } else {
            tab2AmountPane.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        }

        if(tab2GasLimitTextField.isFocused()) {
            tab2GasLimitTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        } else {
            tab2GasLimitTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
    }

    public void contractReadWritePopup() {
        AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_create.fxml", 0);
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("tab1")) {
            initTab(0);

        } else if(fxid.equals("tab2")) {
            initTab(1);

        } else if(fxid.equals("tab3")) {
            initTab(2);

        } else if(fxid.equals("tab4")) {
            initTab(3);

        } else if(fxid.equals("sideTab1")) {
            initSideTab(0);

        } else if(fxid.equals("sideTab2")) {
            initSideTab(1);

        }

        // Amount Percentage Select Box
        String tempId = null;
        for(int i=0; i<pSelectHeadList.size(); i++){
            if(i == 0) {
                tempId = "pSelectHead";
            } else {
                tempId = "pSelectHead_"+i;
            }

            if(fxid.equals(tempId)){
                if(this.pSelectLists.get(i).isVisible() == true) {
                    hidePercentSelectBox(i);
                } else {
                    showPercentSelectBox(i);
                }
            }
        }

        // Contract Read and Write Select Box
        if(fxid.equals("cSelectHead")) {
            if(this.cSelectList.isVisible() == true) {
                hideContractSelectBox();
            } else {
                showContractSelectBox();
            }
        } else if(fxid.equals("cSelectItemDefault")) {
            initContract();
            hideContractSelectBox();
        } else if(fxid.equals("cSelectItemBalance")) {
            cSelectHead.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
            cSelectHeadText.setText(cSelectItemBalanceText.getText());
            cSelectHeadText.setTextFill(Color.web("#999999"));
            cSelectHeadImg.setImage(downGrey);
            tab2ReadWritePane.setVisible(true);
            tab2ReadWritePane.prefHeightProperty().setValue(-1);

            hideContractSelectBox();
        }

        tempId = null;
        // Gas Price
        for(int i=0; i<gasPriceGridList.size(); i++) {
            tempId = "tab"+(i+1)+"GasPricePlusMinusPane";
            if(fxid.equals(tempId)) {
                if (!gasPricePopupGridList.get(i).isVisible()) {
                    showGasPricePopup(i);
                    gasPriceSliderList.get(i).requestFocus();
                    event.consume();
                }

            } else if(fxid.equals("tab"+(i+1)+"GasPriceMinusBtn")) {
                gasPriceSliderList.get(i).setValue(gasPriceSliderList.get(i).getValue()-10);
                gasPriceSliderList.get(i).requestFocus();
                event.consume();

            } else if(fxid.equals("tab"+(i+1)+"GasPricePlusBtn")) {
                gasPriceSliderList.get(i).setValue(gasPriceSliderList.get(i).getValue()+10);
                gasPriceSliderList.get(i).requestFocus();
                event.consume();

            }

            if(fxid.equals("contractHomePane")) {
                if(gasPricePopupGridList.get(i).isVisible()) {
                    if (!gasPricePopupFlag) {
                        hideGasPricePopupAll();
                    } else {
                        gasPriceSliderList.get(i).requestFocus();
                    }
                }
            }

        }

    }

    @FXML
    private void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        // Amount Percentage Select Box
        for(int i=0; i<pSelectHeadList.size(); i++){
            if(id.equals((i == 0) ? "pSelectItem100" : "pSelectItem100_"+i)){
                pSelectItem100List.get(i).setStyle("-fx-background-color : #f2f2f2");

            }else if(id.equals((i == 0) ? "pSelectItem75" : "pSelectItem75_"+i)){
                pSelectItem75List.get(i).setStyle("-fx-background-color : #f2f2f2");

            }else if(id.equals((i == 0) ? "pSelectItem50" : "pSelectItem50_"+i)){
                pSelectItem50List.get(i).setStyle("-fx-background-color : #f2f2f2");

            }else if(id.equals((i == 0) ? "pSelectItem25" : "pSelectItem25_"+i)){
                pSelectItem25List.get(i).setStyle("-fx-background-color : #f2f2f2");

            }else if(id.equals((i == 0) ? "pSelectItem10" : "pSelectItem10_"+i)){
                pSelectItem10List.get(i).setStyle("-fx-background-color : #f2f2f2");

            }
        }

        // Contract Read and Write Select Box
        if(id.equals("cSelectItemDefault")) {
            cSelectItemDefault.setStyle("-fx-background-color: #f2f2f2;");
        } else if(id.equals("cSelectItemBalance")) {
            cSelectItemBalance.setStyle("-fx-background-color: #f2f2f2;");
        }

        // Gas Price Popup
        for(int i=0; i<gasPriceGridList.size(); i++) {
            if (id.equals("tab"+(i+1)+"GasPricePopupGrid")) {
                gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_ENTERED;
            }
        }
    }

    @FXML
    private void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        // Amount Percentage Select Box
        for(int i=0; i<pSelectHeadList.size(); i++){
            if(id.equals((i == 0) ? "pSelectItem100" : "pSelectItem100_"+i)){
                pSelectItem100List.get(i).setStyle("-fx-background-color : #ffffff");

            }else if(id.equals((i == 0) ? "pSelectItem75" : "pSelectItem75_"+i)){
                pSelectItem75List.get(i).setStyle("-fx-background-color : #ffffff");

            }else if(id.equals((i == 0) ? "pSelectItem50" : "pSelectItem50_"+i)){
                pSelectItem50List.get(i).setStyle("-fx-background-color : #ffffff");

            }else if(id.equals((i == 0) ? "pSelectItem25" : "pSelectItem25_"+i)){
                pSelectItem25List.get(i).setStyle("-fx-background-color : #ffffff");

            }else if(id.equals((i == 0) ? "pSelectItem10" : "pSelectItem10_"+i)){
                pSelectItem10List.get(i).setStyle("-fx-background-color : #ffffff");

            }
        }

        // Contract Read and Write Select Box
        if(id.equals("cSelectItemDefault")) {
            cSelectItemDefault.setStyle("-fx-background-color: #ffffff;");
        } else if(id.equals("cSelectItemBalance")) {
            cSelectItemBalance.setStyle("-fx-background-color: #ffffff;");
        }

        // Gas Price Popup
        for(int i=0; i<gasPriceGridList.size(); i++) {
            if (id.equals("tab"+(i+1)+"GasPricePopupGrid")) {
                gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;
            }
        }
    }

    public void showPercentSelectBox(int index){
        this.pSelectLists.get(index).setVisible(true);
        this.pSelectLists.get(index).prefHeightProperty().setValue(-1);
        this.pSelectChildList.get(index).prefHeightProperty().setValue(-1);
    }

    public void hidePercentSelectBox(int index){
        this.pSelectLists.get(index).setVisible(false);
        this.pSelectLists.get(index).prefHeightProperty().setValue(0);
        this.pSelectChildList.get(index).prefHeightProperty().setValue(48);
    }

    public void showContractSelectBox(){
        this.cSelectList.setVisible(true);
        this.cSelectList.prefHeightProperty().setValue(-1);
        this.cSelectChild.prefHeightProperty().setValue(-1);
    }

    public void hideContractSelectBox(){
        this.cSelectList.setVisible(false);
        this.cSelectList.prefHeightProperty().setValue(0);
        this.cSelectChild.prefHeightProperty().setValue(40);
    }

    public void initTab(int index) {
        initTabClean();
        initSideTabClean();

        if(index == 0) {
            this.tab1LeftPane.setVisible(true);
            this.tab1RightPane.setVisible(true);
            this.transferBtn.setVisible(true);
            this.tabLabel1.setTextFill(Color.web("#910000"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane1.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane1.setVisible(true);

        } else if(index == 1) {
            this.tab2LeftPane.setVisible(true);
            this.tab1RightPane.setVisible(true);
            this.tabLabel2.setTextFill(Color.web("#910000"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane2.setVisible(true);

        } else if(index == 2) {
            this.tabLabel3.setTextFill(Color.web("#910000"));
            this.tabLabel3.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane3.setVisible(true);

        } else if(index == 3) {
            this.tabLabel4.setTextFill(Color.web("#910000"));
            this.tabLabel4.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane4.setVisible(true);

        }
    }

    public void initSideTab(int index) {
        initSideTabClean();

        if(index == 0) {
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane1.setVisible(true);

        } else if(index == 1) {
            this.sideTabLabel2.setTextFill(Color.web("#910000"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane2.setVisible(true);

        }
    }

    public void initTabClean() {
        tab1LeftPane.setVisible(false);
        tab1RightPane.setVisible(false);
        tab2LeftPane.setVisible(false);
        transferBtn.setVisible(false);
        tabLabel1.setTextFill(Color.web("#999999"));
        tabLabel2.setTextFill(Color.web("#999999"));
        tabLabel3.setTextFill(Color.web("#999999"));
        tabLabel4.setTextFill(Color.web("#999999"));
        tabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel3.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel4.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLinePane1.setVisible(false);
        tabLinePane2.setVisible(false);
        tabLinePane3.setVisible(false);
        tabLinePane4.setVisible(false);
    }

    public void initSideTabClean() {
        sideTabLabel1.setTextFill(Color.web("#999999"));
        sideTabLabel2.setTextFill(Color.web("#999999"));
        sideTabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        sideTabLinePane1.setVisible(false);
        sideTabLinePane2.setVisible(false);
    }

    public void initContract() {
        cSelectHead.setStyle("-fx-background-color: #999999; -fx-border-color: d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        cSelectHeadText.setText(cSelectItemDefaultText.getText());
        cSelectHeadText.setTextFill(Color.web("#ffffff"));
        cSelectHeadImg.setImage(downWhite);
        tab2ReadWritePane.setVisible(false);
        tab2ReadWritePane.prefHeightProperty().setValue(0);
    }

    public void showGasPricePopup(int index) {
        gasPricePlusMinusLabelList.get(index).setTextFill(Color.web("#2b2b2b"));
        gasPriceGridList.get(index).setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8d8d8; -fx-border-radius: 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        gasPricePopupGridList.get(index).setVisible(true);
        gasPricePopupImgList.get(index).setVisible(true);
        gasPricePopupGridList.get(index).prefHeightProperty().setValue(-1);
        gasPricePopupImgList.get(index).prefHeight(90);
    }

    public void hideGasPricePopup(int index) {
        gasPricePlusMinusLabelList.get(index).setTextFill(Color.web("#999999"));
        gasPriceGridList.get(index).setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius: 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        gasPricePopupGridList.get(index).setVisible(false);
        gasPricePopupImgList.get(index).setVisible(false);
        gasPricePopupGridList.get(index).prefHeightProperty().setValue(0);
        gasPricePopupImgList.get(index).prefHeight(1);
    }

    public void hideGasPricePopupAll() {
        for(int i=0; i<gasPriceGridList.size(); i++) {
            hideGasPricePopup(i);
        }
    }

}
