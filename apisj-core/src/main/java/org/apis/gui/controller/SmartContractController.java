package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apis.core.CallTransaction;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.ContractModel;
import org.apis.solidity.SolidityType;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SmartContractController implements Initializable {
    // Gas Price Popup Flag
    private static final boolean GAS_PRICE_POPUP_MOUSE_ENTERED = true;
    private static final boolean GAS_PRICE_POPUP_MOUSE_EXITED = false;
    private boolean gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;
    private int selectedTabIndex = 0;
    // Gas Price
    private BigInteger gasPrice = new BigInteger("50");

    @FXML
    private Label aliasLabel, addressLabel, placeholderLabel;

    @FXML
    private Label tabLabel1, tabLabel2, tabLabel3, sideTabLabel1, sideTabLabel2;
    @FXML
    private Pane tabLinePane1, tabLinePane2, tabLinePane3, sideTabLinePane1, sideTabLinePane2;
    @FXML
    private AnchorPane tab1LeftPane, tab1RightPane, tab2LeftPane, tab2RightPane;
    @FXML
    private AnchorPane tab1AmountPane, tab2AmountPane, tab2ReadWritePane;
    @FXML
    private GridPane tab1GasPriceGrid, tab1GasPricePopupGrid, tab2GasPriceGrid, tab2GasPricePopupGrid;
    @FXML
    private GridPane transferBtn;
    @FXML
    private Label writeBtn, readBtn;
    @FXML
    private Label tab1GasPricePlusMinusLabel, tab2GasPricePlusMinusLabel, tab1GasPricePopupLabel, tab2GasPricePopupLabel, tab1GasPricePopupDefaultLabel, tab2GasPricePopupDefaultLabel;
    @FXML
    private Label cSelectHeadText, pSelectHeadText, pSelectHeadText_1;
    @FXML
    private ImageView icon, cSelectHeadImg, tab1GasPricePopupImg, tab2GasPricePopupImg, tab1GasPriceMinusBtn, tab2GasPriceMinusBtn, tab1GasPricePlusBtn, tab2GasPricePlusBtn;
    @FXML
    private VBox cSelectList, cSelectChild;
    @FXML
    private ScrollPane cSelectListView;
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
    private GridPane tab1SolidityTextGrid, codeTab1, codeTab2;
    private ApisCodeArea tab1SolidityTextArea1 = new ApisCodeArea();
    @FXML
    private TextFlow tab1SolidityTextArea2;
    @FXML
    private TextArea tab1SolidityTextArea3;
    @FXML
    private ProgressBar tab1ProgressBar, tab2ProgressBar;
    @FXML
    private Slider tab1Slider, tab2Slider;
    @FXML
    private GridPane walletInputView;
    @FXML private AnchorPane walletSelectViewDim;

    // Multilingual Support Label
    @FXML
    private Label tabTitle, selectWallet, amountToSend, amountTotal, textareaMessage, gasPriceTitle, gasPriceFormula, gasPriceLabel, gasLimitLabel, detailLabel,
                  detailContentsFee, detailContentsTotal, tab1LowLabel, tab1HighLabel, transferAmountTitle, detailLabel1, transferAmountLabel, gasPriceReceipt,
                  totalWithdrawal, afterBalance, transferAmountDesc1, transferAmountDesc2, transferBtnLabel, selectContract, selectWallet1, amountToSend1, amountTotal1,
                  readWriteContract, gasPriceTitle1, gasPriceFormula1, gasPriceLabel1, gasLimitLabel1, detailLabel2, detailContentsFee1, detailContentsTotal1,
                  tab2LowLabel, tab2HighLabel;
    // Number Label
    @FXML
    private Label amountToSendNature, amountToSendDecimal, amountToSendNature1, amountToSendDecimal1, detailContentsFeeNum, detailContentsTotalNum, detailContentsFeeNum1,
                  detailContentsTotalNum1, transferAmountTitleNature, transferAmountTitleDecimal, transferAmountLabelNature, transferAmountLabelDecimal,
                  gasPriceReceiptNature, gasPriceReceiptDecimal, totalWithdrawalNature, totalWithdrawalDecimal, afterBalanceNature, afterBalanceDecimal;

    @FXML
    private ApisSelectBoxController walletSelectorController, walletSelector_1Controller;

    // Contract TextArea
    @FXML private ScrollPane contractInputView;
    @FXML private ComboBox contractCombo;
    @FXML private VBox contractMethodList;
    @FXML private VBox methodParameterList;


    private Image downGrey, downWhite;
    // Percentage Select Box Lists
    private ArrayList<VBox> pSelectLists = new ArrayList<>();
    private ArrayList<VBox> pSelectChildList = new ArrayList<>();
    private ArrayList<GridPane> pSelectHeadList = new ArrayList<>();
    private ArrayList<Label> pSelectHeadTextList = new ArrayList<>();
    private ArrayList<TextField> pAmountTextFieldList = new ArrayList<>();
    private ArrayList<ApisSelectBoxController> pWalletSelectorList = new ArrayList<>();
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


    // 컨트렉트 객체
    private CompilationResult res;
    private CompilationResult.ContractMetadata metadata;
    private ArrayList<Object> contractParams = new ArrayList<>();
    private ArrayList<Object> selectFunctionParams = new ArrayList();
    private ArrayList<ContractMethodListItemController> returnItemController = new ArrayList<>();
    private Thread autoCompileThread;
    private long minGasLimit;
    private ContractModel selectContractModel;
    private CallTransaction.Function selectFunction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setSmartContract(this);
        settingLayoutData();
        // Multilingual Support
        languageSetting();

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
        pSelectHeadTextList.add(pSelectHeadText);
        pAmountTextFieldList.add(tab1AmountTextField);
        pWalletSelectorList.add(walletSelectorController);
        pSelectItem100List.add(pSelectItem100);
        pSelectItem75List.add(pSelectItem75);
        pSelectItem50List.add(pSelectItem50);
        pSelectItem25List.add(pSelectItem25);
        pSelectItem10List.add(pSelectItem10);

        walletSelectorController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {

            }
        });

        pSelectLists.add(pSelectList_1);
        pSelectChildList.add(pSelectChild_1);
        pSelectHeadList.add(pSelectHead_1);
        pSelectHeadTextList.add(pSelectHeadText_1);
        pAmountTextFieldList.add(tab2AmountTextField);
        pWalletSelectorList.add(walletSelector_1Controller);
        pSelectItem100List.add(pSelectItem100_1);
        pSelectItem75List.add(pSelectItem75_1);
        pSelectItem50List.add(pSelectItem50_1);
        pSelectItem25List.add(pSelectItem25_1);
        pSelectItem10List.add(pSelectItem10_1);

        walletSelector_1Controller.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {

            }
        });

        hidePercentSelectBox(0);
        hidePercentSelectBox(1);

        // Contract Read and Write Select Box List Handling
        initContract();
        hideContractSelectBox();

        // Focused
        tab1AmountTextField.focusedProperty().addListener(tab1AmountListener);
        tab1GasLimitTextField.focusedProperty().addListener(tab1GasLimitListener);
        tab2AmountTextField.focusedProperty().addListener(tab2AmountListener);
        tab2GasLimitTextField.focusedProperty().addListener(tab2GasLimitListener);

        // Input
        tab1AmountTextField.textProperty().addListener(tab1AmountTextListener);
        tab1GasLimitTextField.textProperty().addListener(tab1GasLimitTextListener);

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
        tab1SolidityTextArea1.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                autoCompileThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Thread thread = Thread.currentThread();
                        int count = 0;
                        while(autoCompileThread == thread){
                            try {
                                thread.sleep(1000);
                                count++;
                                if(count == 5){ // 5초 카운트 이후, 컴파일
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            startToCompile();
                                        }
                                    });
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                autoCompileThread.start();

                event.consume();
            }
        });

        tab1SolidityTextGrid.add(tab1SolidityTextArea1,0,0);

        contractCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue != null) {

                    // 생성자 필드 생성
                    deployContractFieldInMethodList(newValue.toString());
                }
            }
        });
    }

    public void addMethodSelectItem(CallTransaction.Function function, String contractAddress, String medataAbi ){
        if(function == null || function.type == CallTransaction.FunctionType.constructor){
            return;
        }

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle(new JavaFXStyle(anchorPane.getStyle()).add("-fx-background-color","#ffffff").toString());
        Label label = new Label(function.name);
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
                // 선택한 함수 변경
                selectFunction = function;

                // 선택한 함수로 셀렉트박스 헤드 변경
                cSelectHeadText.setText(label.getText());
                hideContractSelectBox();

                // show param list view
                tab2ReadWritePane.setVisible(true);
                tab2ReadWritePane.prefHeightProperty().setValue(-1);

                // Read 인지 Write인지 체크
                boolean isRead = false;

                String funcStateMutability = (function.stateMutability != null) ? function.stateMutability.name() : "null";
                if("view".equals(funcStateMutability)
                        || "pure".equals(funcStateMutability)){
                    // Read
                    isRead = true;
                }else {
                    // Write
                    isRead = false;
                }

                // 버튼 변경
                if(isRead){
                    transferBtn.setVisible(false);
                    writeBtn.setVisible(false);
                    readBtn.setVisible(true);

                    // 인자가 없는 경우 (Call)
                    if(function.inputs.length == 0){
                        transferBtn.setVisible(false);
                        writeBtn.setVisible(false);
                        readBtn.setVisible(false);
                    }

                    // 지갑선택란 숨김
                    setWaleltInputViewVisible(false, false);

                    // right pane visible
                    tab1RightPane.setVisible(false);
                    tab2RightPane.setVisible(true);
                }else{
                    transferBtn.setVisible(false);
                    writeBtn.setVisible(true);
                    readBtn.setVisible(false);

                    // 지갑선택란 표기
                    setWaleltInputViewVisible(true, false);

                    // right pane visible
                    tab1RightPane.setVisible(true);
                    tab2RightPane.setVisible(false);
                }

                // create method var
                int itemType = 0; int dataType = 0;
                methodParameterList.getChildren().clear();
                selectFunctionParams.clear();
                returnItemController.clear();

                for(int i=0; i<function.inputs.length; i++){
                    itemType = ContractMethodListItemController.ITEM_TYPE_PARAM;

                    // dataType
                    methodParameterList.getChildren().add(createMethodParam(itemType, dataType, function.inputs[i], function, contractAddress, medataAbi));
                }

                // read 인 경우에만 리턴값 표기
                if(isRead) {
                    for(int i=0; i<function.outputs.length; i++){
                        itemType = ContractMethodListItemController.ITEM_TYPE_RETURN;

                        // dataType
                        methodParameterList.getChildren().add(createMethodParam(itemType, dataType, function.outputs[i], function, null, null));
                    }

                    // 인자가 없는 경우 데이터 불러오기
                    if(function.inputs.length == 0){
                        CallTransaction.Contract contract = new CallTransaction.Contract(medataAbi);
                        Object[] result = AppManager.getInstance().callConstantFunction(contractAddress, contract.getByName(function.name));
                        for(int i=0; i<function.outputs.length; i++){
                            returnItemController.get(i).setItemText(result[i].toString());
                        }
                    }
                }

                // TODO:  Write인 경우 - 인자 있을 경우 처리를 해야함.
                if(!isRead){

                    checkSendFunctionPreGasPrice(selectFunction, contractAddress, medataAbi);

//                    Object[] args = new Object[0];
//                    long preGasUsed = AppManager.getInstance().getPreGasUsed(medataAbi, Hex.decode(walletSelectorController.getAddress()), Hex.decode(contractAddress), function.name, args);
//                    tab2GasLimitTextField.textProperty().set(""+preGasUsed);
//                    minGasLimit = preGasUsed;
                }

            }
        });
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        anchorPane.getChildren().add(label);
        cSelectList.getChildren().add(anchorPane);
    }

    public Node createMethodParam(int itemType, int dataType, CallTransaction.Param param, CallTransaction.Function function, String contractAddress, String medataAbi){
        try {
            String paramName = param.name;
            String dataTypeName = param.type.getName();

            URL methodListItem = new File("apisj-core/src/main/resources/scene/contract_method_list_item.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(methodListItem);
            Node node = loader.load();
            ContractMethodListItemController itemController = (ContractMethodListItemController)loader.getController();
            itemController.setData(itemType, paramName, dataType, dataTypeName);
            itemController.setItemText("");

            if(itemType == ContractMethodListItemController.ITEM_TYPE_RETURN) {
                returnItemController.add(itemController);
            }else{
                itemController.setHandler(new ContractMethodListItemController.ContractMethodListItemImpl() {
                    @Override
                    public void change(Object oldValue, Object newValue) {
                        checkSendFunctionPreGasPrice(function, contractAddress, medataAbi);
                    }
                });
            }

            if(param.type instanceof SolidityType.BoolType){
                // BOOL

                // param 등록
                SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();
                booleanProperty.bind(itemController.selectedProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(booleanProperty);
                }

            }else if(param.type instanceof SolidityType.AddressType){
                // AddressType

                // param 등록
                SimpleStringProperty stringProperty = new SimpleStringProperty();
                stringProperty.bind(itemController.textProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(stringProperty);
                }

            }else if(param.type instanceof SolidityType.IntType){
                // INT, uINT

                // param 등록
                SimpleStringProperty stringProperty = new SimpleStringProperty();
                stringProperty.bind(itemController.textProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(stringProperty);
                }

            }else if(param.type instanceof SolidityType.StringType){
                // StringType

                // param 등록
                SimpleStringProperty stringProperty = new SimpleStringProperty();
                stringProperty.bind(itemController.textProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(stringProperty);
                }

            }else if(param.type instanceof SolidityType.BytesType){
                // BytesType

                // param 등록
                SimpleStringProperty stringProperty = new SimpleStringProperty();
                stringProperty.bind(itemController.textProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(stringProperty);
                }

            }else if(param.type instanceof SolidityType.Bytes32Type){
                // Bytes32Type

                // param 등록
                SimpleStringProperty stringProperty = new SimpleStringProperty();
                stringProperty.bind(itemController.textProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(stringProperty);
                }

            }else if(param.type instanceof SolidityType.FunctionType){
                // FunctionType


            }else if(param.type instanceof SolidityType.ArrayType){
                // ArrayType

                // param 등록
                SimpleStringProperty stringProperty = new SimpleStringProperty();
                stringProperty.bind(itemController.textProperty());
                if(itemType == ContractMethodListItemController.ITEM_TYPE_PARAM) {
                    selectFunctionParams.add(stringProperty);
                }
            }



            return node;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setWaleltInputViewVisible(boolean isVisible, boolean isPlaceHolder){
        if(isPlaceHolder){
            walletInputView.setVisible(true);
            walletInputView.setPrefHeight(-1);
            walletSelectViewDim.setVisible(true);
            //walletSelectViewDim.setPrefHeight(200);
        }else {
            walletInputView.setVisible(isVisible);
            walletInputView.setPrefHeight((isVisible)?-1:0);
            walletSelectViewDim.setVisible(false);
            walletSelectViewDim.setPrefHeight(0);
        }
    }

    public void languageSetting() {
        tabTitle.textProperty().bind(StringManager.getInstance().smartContract.tabTitle);
        tabLabel1.textProperty().bind(StringManager.getInstance().smartContract.tabLabel1);
        tabLabel2.textProperty().bind(StringManager.getInstance().smartContract.tabLabel2);
        tabLabel3.textProperty().bind(StringManager.getInstance().smartContract.tabLabel3);
        selectWallet.textProperty().bind(StringManager.getInstance().smartContract.selectWallet);
        amountToSend.textProperty().bind(StringManager.getInstance().smartContract.amountToSend);
        amountTotal.textProperty().bind(StringManager.getInstance().smartContract.amountTotal);
        sideTabLabel1.textProperty().bind(StringManager.getInstance().smartContract.sideTabLabel1);
        sideTabLabel2.textProperty().bind(StringManager.getInstance().smartContract.sideTabLabel2);
        textareaMessage.textProperty().bind(StringManager.getInstance().smartContract.textareaMessage);

        gasPriceTitle.textProperty().bind(StringManager.getInstance().smartContract.gasPriceTitle);
        gasPriceFormula.textProperty().bind(StringManager.getInstance().smartContract.gasPriceFormula);
        gasPriceLabel.textProperty().bind(StringManager.getInstance().smartContract.gasPriceLabel);
        gasLimitLabel.textProperty().bind(StringManager.getInstance().smartContract.gasLimitLabel);
        detailLabel.textProperty().bind(StringManager.getInstance().smartContract.detailLabel);
        detailContentsFee.textProperty().bind(StringManager.getInstance().smartContract.detailContentsFee);
        detailContentsTotal.textProperty().bind(StringManager.getInstance().smartContract.detailContentsTotal);
        tab1GasPricePopupDefaultLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1DefaultLabel);
        tab1LowLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1LowLabel);
        tab1HighLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1HighLabel);
        transferAmountTitle.textProperty().bind(StringManager.getInstance().smartContract.transferAmountLabel);
        detailLabel1.textProperty().bind(StringManager.getInstance().smartContract.detailLabel);
        transferAmountLabel.textProperty().bind(StringManager.getInstance().smartContract.transferAmountLabel);
        gasPriceReceipt.textProperty().bind(StringManager.getInstance().smartContract.gasPriceReceipt);
        totalWithdrawal.textProperty().bind(StringManager.getInstance().smartContract.totalWithdrawal);
        afterBalance.textProperty().bind(StringManager.getInstance().smartContract.afterBalance);
        transferAmountDesc1.textProperty().bind(StringManager.getInstance().smartContract.transferAmountDesc1);
        transferAmountDesc2.textProperty().bind(StringManager.getInstance().smartContract.transferAmountDesc2);
        transferBtnLabel.textProperty().bind(StringManager.getInstance().smartContract.transferBtnLabel);
        selectContract.textProperty().bind(StringManager.getInstance().smartContract.selectContract);
        selectWallet1.textProperty().bind(StringManager.getInstance().smartContract.selectWallet1);
        amountToSend1.textProperty().bind(StringManager.getInstance().smartContract.amountToSend);
        amountTotal1.textProperty().bind(StringManager.getInstance().smartContract.amountTotal);
        readWriteContract.textProperty().bind(StringManager.getInstance().smartContract.readWriteContract);
        gasPriceTitle1.textProperty().bind(StringManager.getInstance().smartContract.gasPriceTitle);
        gasPriceFormula1.textProperty().bind(StringManager.getInstance().smartContract.gasPriceFormula);
        gasPriceLabel1.textProperty().bind(StringManager.getInstance().smartContract.gasPriceLabel);
        gasLimitLabel1.textProperty().bind(StringManager.getInstance().smartContract.gasLimitLabel);
        detailLabel2.textProperty().bind(StringManager.getInstance().smartContract.detailLabel);
        detailContentsFee1.textProperty().bind(StringManager.getInstance().smartContract.detailContentsFee);
        detailContentsTotal1.textProperty().bind(StringManager.getInstance().smartContract.detailContentsTotal);
        tab2GasPricePopupDefaultLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1DefaultLabel);
        tab2LowLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1LowLabel);
        tab2HighLabel.textProperty().bind(StringManager.getInstance().smartContract.tab1HighLabel);
    }

    private ChangeListener<Boolean> tab1AmountListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

            String sAmount = tab1AmountTextField.getText();
            String[] amountSplit = sAmount.split("\\.");
            if(sAmount != null && !sAmount.equals("")){
                if(amountSplit.length == 0){
                    sAmount = "0.000000000000000000";
                }else if(amountSplit.length == 1){
                    sAmount = sAmount.replace(".","") + ".000000000000000000";
                }else{
                    String decimal = amountSplit[1];
                    for(int i=0; i<18 - amountSplit[1].length(); i++){
                        decimal = decimal + "0";
                    }
                    amountSplit[1] = decimal;
                    sAmount = amountSplit[0] + "." + amountSplit[1];
                }
                tab1AmountTextField.textProperty().setValue(sAmount);
            }

            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> tab1GasLimitListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
            if(newValue != null){
                String gasLimit = tab1GasLimitTextField.getText();
                if(gasLimit.length() > 0 ){// TODO : && minGasLimit > Long.parseLong(gasLimit)){
                    tab1GasLimitTextField.setText(""+minGasLimit);
                }
            }
        }
    };

    private ChangeListener<Boolean> tab2AmountListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<Boolean> tab2GasLimitListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            textFieldFocus();
        }
    };

    private ChangeListener<String> tab1AmountTextListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("[\\d.]*")) {
                tab1AmountTextField.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        }
    };

    private ChangeListener<String> tab1GasLimitTextListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("[\\d]*")) {
                tab1GasLimitTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
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
            settingLayoutData();
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
            settingLayoutData();
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
            settingLayoutData();
        }

        if(tab1GasLimitTextField.isFocused()) {
            tab1GasLimitTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        } else {
            tab1GasLimitTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            settingLayoutData();
        }

        if(tab2AmountTextField.isFocused()) {
            tab2AmountPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        } else {
            tab2AmountPane.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
            settingLayoutData();
        }

        if(tab2GasLimitTextField.isFocused()) {
            tab2GasLimitTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        } else {
            tab2GasLimitTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            settingLayoutData();
        }
    }

    @FXML
    public void contractDeployPopup() {
        if(checkTransferButton()){
            String address = this.walletSelectorController.getAddress();
            String balance = this.tab1AmountTextField.getText().replace(".","");
            String gasPrice = new BigInteger(""+(int)tab1Slider.getValue()).multiply(new BigInteger("1000000000")).toString();
            String gasLimit = this.tab1GasLimitTextField.getText();
            String contractName = (String)this.contractCombo.getSelectionModel().getSelectedItem();

            byte[] initParams = new byte[0];
            byte[] data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);

            CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
            CallTransaction.Function function = contract.getByName("");

            if(function != null) {
                // 데이터 불러오기
                Object[] args = new Object[function.inputs.length];
                for (int i = 0; i < contractParams.size(); i++) {
                    SimpleStringProperty stringProperty = (SimpleStringProperty) contractParams.get(i);

                    if(function.inputs[i].type instanceof SolidityType.BoolType){
                        args[i] = stringProperty.get();
                    }else if(function.inputs[i].type instanceof SolidityType.StringType){
                        args[i] = stringProperty.get();
                    }else if(function.inputs[i].type instanceof SolidityType.ArrayType){
                        args[i] = stringProperty.get();
                    }else if(function.inputs[i].type instanceof SolidityType.FunctionType){

                    }else if(function.inputs[i].type instanceof SolidityType.BytesType){
                        args[i] = Hex.decode(stringProperty.get());
                    }else if(function.inputs[i].type instanceof SolidityType.AddressType){
                        args[i] = Hex.decode(stringProperty.get());
                    }else if(function.inputs[i].type instanceof SolidityType.IntType){
                        BigInteger integer = new BigInteger(stringProperty.get());
                        args[i] = integer;
                    }else if(function.inputs[i].type instanceof SolidityType.Bytes32Type){
                        args[i] = Hex.decode(stringProperty.get());
                    }else if(function.inputs[i].type instanceof SolidityType.DynamicArrayType){
                        args[i] = stringProperty.get();
                    }else if(function.inputs[i].type instanceof SolidityType.StaticArrayType){
                        args[i] = stringProperty.get();
                    }

                    System.out.println("args["+i+"] : "+args[i]);
                }

                if(function.inputs.length > 0){
                    initParams = function.encodeArguments(args);
                }

                data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);
            }

            PopupContractWarningController controller = (PopupContractWarningController) AppManager.getInstance().guiFx.showMainPopup("popup_contract_warning.fxml", 0);
            controller.setData(address, balance, gasPrice, gasLimit, contractName, metadata.abi, data);
        }
    }
    @FXML
    public void contractCallSendPopup(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if("readBtn".equals(id)){
            String functionName = this.selectFunction.name;
            String contractAddress = this.selectContractModel.getAddress();
            String medataAbi = this.selectContractModel.getAbi();

            // 데이터 불러오기
            Object[] args = new Object[this.selectFunction.inputs.length];
            for (int i = 0; i < selectFunctionParams.size(); i++) {
                SimpleStringProperty stringProperty = (SimpleStringProperty) selectFunctionParams.get(i);

                if(this.selectFunction.inputs[i].type instanceof SolidityType.BoolType){
                    args[i] = selectFunctionParams.get(i);
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.StringType){
                    args[i] = stringProperty.get();
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.ArrayType){
                    args[i] = stringProperty.get();
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.FunctionType){

                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.BytesType){
                    args[i] = Hex.decode(stringProperty.get());
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.AddressType){
                    args[i] = Hex.decode(stringProperty.get());
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.IntType){
                    BigInteger integer = new BigInteger(stringProperty.get());
                    args[i] = integer;
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.Bytes32Type){
                    args[i] = Hex.decode(stringProperty.get());
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.DynamicArrayType){
                    args[i] = stringProperty.get();
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.StaticArrayType){
                    args[i] = stringProperty.get();
                }

            }
            CallTransaction.Contract contract = new CallTransaction.Contract(medataAbi);
            Object[] objects = AppManager.getInstance().callConstantFunction(contractAddress, contract.getByName(functionName), args);
            for(int i=0; i<objects.length; i++){
                returnItemController.get(i).setItemText(objects[i].toString());
            }
        }else if("writeBtn".equals(id)){
            String address = this.walletSelector_1Controller.getAddress();
            String balance = this.tab2AmountTextField.getText().replace(".","");
            String gasPrice = new BigInteger(""+(int)tab2Slider.getValue()).multiply(new BigInteger("1000000000")).toString();
            String gasLimit = this.tab2GasLimitTextField.getText();
            byte[] contractAddress = selectContractModel.getAddressByte();

            Object[] args = new Object[this.selectFunction.inputs.length];
            for (int i = 0; i < selectFunctionParams.size(); i++) {
                SimpleStringProperty stringProperty = (SimpleStringProperty) selectFunctionParams.get(i);

                if(this.selectFunction.inputs[i].type instanceof SolidityType.BoolType){
                    args[i] = selectFunctionParams.get(i);
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.StringType){
                    args[i] = stringProperty.get();
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.ArrayType){
                    args[i] = stringProperty.get();
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.FunctionType){

                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.BytesType){
                    args[i] = Hex.decode(stringProperty.get());
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.AddressType){
                    args[i] = Hex.decode(stringProperty.get());
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.IntType){
                    BigInteger integer = new BigInteger((stringProperty.get() == null || stringProperty.get().equals(""))?"0":stringProperty.get());
                    args[i] = integer;
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.Bytes32Type){
                    args[i] = Hex.decode(stringProperty.get());
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.DynamicArrayType){
                    args[i] = stringProperty.get();
                }else if(this.selectFunction.inputs[i].type instanceof SolidityType.StaticArrayType){
                    args[i] = stringProperty.get();
                }

                System.out.println("args[i] : "+args[i]);
            }

            CallTransaction.Contract contract = new CallTransaction.Contract(this.selectContractModel.getAbi());
            CallTransaction.Function setter = contract.getByName(selectFunction.name);
            byte[] functionCallBytes = setter.encode(args);

            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) AppManager.getInstance().guiFx.showMainPopup("popup_contract_warning.fxml", 0);
            controller.setData(address, balance, gasPrice, gasLimit, contractAddress, functionCallBytes);
        }

    }

    @FXML
    public void contractSelectPopup(){
        PopupContractReadWriteSelectController controller = (PopupContractReadWriteSelectController)AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_select.fxml", 0);
        controller.setHandler(new PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl() {
            @Override
            public void onClickSelect(ContractModel model) {
                selectContractModel = model;

                aliasLabel.setText(model.getName());
                addressLabel.setText(model.getAddress());
                placeholderLabel.setVisible(false);

                try {
                    Image image = IdenticonGenerator.generateIdenticonsToImage(addressLabel.textProperty().get(), 128, 128);
                    if(image != null){
                        SmartContractController.this.icon.setImage(image);
                        image = null;
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // get contract method list
                CallTransaction.Contract contract = new CallTransaction.Contract(model.getAbi());
                CallTransaction.Function[] functions =  contract.functions;
                cSelectList.getChildren().clear();
                for(int i=0; i<functions.length; i++){
                    if("function".equals(functions[i].type.name())){
                        addMethodSelectItem(functions[i], model.getAddress(), model.getAbi());
                    }
                }
            }
        });
    }

    @FXML
    public void startToCompile(){
        res = null;
        this.tab1SolidityTextArea2.getChildren().clear();

        String contract = this.tab1SolidityTextArea1.getText();
        if(contract == null || contract.length() <= 0){
            return;
        }
// 컴파일에 성공하면 json 스트링을 반환한다.
        String message = AppManager.getInstance().ethereumSmartContractStartToCompile(contract);
        if(message != null && message.length() > 0 && AppManager.isJSONValid(message)){
            try {
                textareaMessage.setVisible(false);
                contractInputView.setVisible(true);

                res = CompilationResult.parse(message);

                // 컨트렉트 이름 파싱
                // <stdin>:testContract
                ArrayList<String> contractList = new ArrayList<>();
                String[] splitKey = null;
                for(int i=0; i<res.getContractKeys().size(); i++){
                    splitKey = res.getContractKeys().get(i).split(":");
                    if(splitKey.length > 1){
                        contractList.add(splitKey[1]);
                    }
                }


                // 컨트렉트 등록
                ObservableList list = FXCollections.observableList(contractList);
                if(list.size() > 0){
                    this.contractCombo.getItems().clear();
                    this.contractCombo.setItems(list);

                    // 첫번째 아이템 선택
                    this.contractCombo.getSelectionModel().select(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
// 컴파일에 실패할 경우
        }else{
            textareaMessage.setVisible(false);
            contractInputView.setVisible(false);

            String[] tempSplit = message.split("<stdin>:");
            for(int i=0; i<tempSplit.length; i++){
                System.out.println("["+(i+1)+"] : "+tempSplit[i]);
                Text text  = new Text(tempSplit[i]);
                text.setFont(Font.font("Roboto Mono", 10));

                if(tempSplit[i].indexOf("Warning") >= 0){
                    text.setFill(Color.rgb(221, 83 , 23));
                }else if(tempSplit[i].indexOf("Error") >= 0){
                    text.setFill(Color.rgb(145, 0 , 0));
                }else {
                    text.setFill(Color.rgb(66, 133 , 244));
                }

                this.tab1SolidityTextArea2.getChildren().add(text);
            }
        }

        checkTransferButton();
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

        } else if(fxid.equals("sideTab1")) {
            initSideTab(0);

        } else if(fxid.equals("sideTab2")) {
            initSideTab(1);

        }

        // Amount Percentage Select Box
        String tempId = null;
        for(int i=0; i<pSelectHeadList.size(); i++){

            // header
            tempId = (i == 0) ? "pSelectHead" : "pSelectHead_"+i;
            if(fxid.equals(tempId)){
                if(this.pSelectLists.get(i).isVisible() == true) {
                    hidePercentSelectBox(i);
                } else {
                    showPercentSelectBox(i);
                }
            }

            // 100
            tempId = (i == 0) ? "pSelectItem100" : "pSelectItem100_"+i;
            if(fxid.equals(tempId)){
                this.pSelectHeadTextList.get(i).textProperty().setValue("100%");
                String sBalance = pWalletSelectorList.get(i).getBalance();
                BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("100")).divide(new BigInteger("100"));
                pAmountTextFieldList.get(i).textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
                this.pSelectHeadList.get(i).setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000;");
                hidePercentSelectBox(i);
                settingLayoutData();
            }

            // 75
            tempId = (i == 0) ? "pSelectItem75" : "pSelectItem75_"+i;
            if(fxid.equals(tempId)){
                this.pSelectHeadTextList.get(i).textProperty().setValue("75%");
                String sBalance = pWalletSelectorList.get(i).getBalance();
                BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("75")).divide(new BigInteger("100"));
                pAmountTextFieldList.get(i).textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
                this.pSelectHeadList.get(i).setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000;");
                hidePercentSelectBox(i);
                settingLayoutData();
            }

            // 50
            tempId = (i == 0) ? "pSelectItem50" : "pSelectItem50_"+i;
            if(fxid.equals(tempId)){
                this.pSelectHeadTextList.get(i).textProperty().setValue("50%");
                String sBalance = pWalletSelectorList.get(i).getBalance();
                BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("50")).divide(new BigInteger("100"));
                pAmountTextFieldList.get(i).textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
                this.pSelectHeadList.get(i).setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000;");
                hidePercentSelectBox(i);
                settingLayoutData();
            }

            // 25
            tempId = (i == 0) ? "pSelectItem25" : "pSelectItem25_"+i;
            if(fxid.equals(tempId)){
                this.pSelectHeadTextList.get(i).textProperty().setValue("25%");
                String sBalance = pWalletSelectorList.get(i).getBalance();
                BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("25")).divide(new BigInteger("100"));
                pAmountTextFieldList.get(i).textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
                this.pSelectHeadList.get(i).setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000;");
                hidePercentSelectBox(i);
                settingLayoutData();
            }

            // 10
            tempId = (i == 0) ? "pSelectItem10" : "pSelectItem10_"+i;
            if(fxid.equals(tempId)){
                this.pSelectHeadTextList.get(i).textProperty().setValue("10%");
                String sBalance = pWalletSelectorList.get(i).getBalance();
                BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("10")).divide(new BigInteger("100"));
                pAmountTextFieldList.get(i).textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
                this.pSelectHeadList.get(i).setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000;");
                hidePercentSelectBox(i);
                settingLayoutData();
            }
        }

        // Contract Read and Write Select Box
        if(fxid.equals("cSelectHead")) {
            if(this.cSelectListView.isVisible() == true) {
                hideContractSelectBox();
            } else {
                showContractSelectBox();
            }
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

        // Gas Price Popup
        for(int i=0; i<gasPriceGridList.size(); i++) {
            if (id.equals("tab"+(i+1)+"GasPricePopupGrid")) {
                gasPricePopupFlag = GAS_PRICE_POPUP_MOUSE_EXITED;
            }
        }
    }

    public void update(){
        for(int i=0; i<pWalletSelectorList.size(); i++) {
            pWalletSelectorList.get(i).update();
            pWalletSelectorList.get(i).setStage(ApisSelectBoxController.STAGE_DEFAULT);
        }
        settingLayoutData();
    }

    // 화면 초기
    private void initLayoutData(){
        // 지갑선택
        for(int i=0; i<pWalletSelectorList.size(); i++){
            pWalletSelectorList.get(i).selectedItem(0);
        }

        // Amount 텍스트 필드
        for(int i=0; i<pAmountTextFieldList.size(); i++){
            pAmountTextFieldList.get(i).textProperty().set("");
        }

        // Contract Editor
        textareaMessage.setVisible(true);
        contractInputView.setVisible(false);
        contractMethodList.getChildren().clear();

        //
        tab1Slider.setValue(tab1Slider.getMin());

        tab1GasLimitTextField.textProperty().set("");

        settingLayoutData();
    }

    public void settingLayoutData(){
        for(int i=0; i<pWalletSelectorList.size(); i++) {
            String sBalance = pWalletSelectorList.get(i).getBalance();
            String[] balanceSplit = AppManager.addDotWidthIndex(sBalance).split("\\.");

            // amount to send
            String sAmount = pAmountTextFieldList.get(i).getText();
            sAmount = (sAmount != null && !sAmount.equals("")) ? sAmount : AppManager.addDotWidthIndex("0");
            String[] amountSplit = sAmount.split("\\.");

            // fee
            BigInteger gasLimit = null;
            if(i == 0) {
                if (tab1GasLimitTextField.getText() != null && tab1GasLimitTextField.getText().length() > 0) {
                    gasLimit = new BigInteger(tab1GasLimitTextField.getText());
                } else {
                    gasLimit = new BigInteger("0");
                }
            } else {
                if (tab2GasLimitTextField.getText() != null && tab2GasLimitTextField.getText().length() > 0) {
                    gasLimit = new BigInteger(tab2GasLimitTextField.getText());
                } else {
                    gasLimit = new BigInteger("0");
                }
            }
            BigInteger fee = gasPrice.multiply(new BigInteger("1000000000")).multiply(gasLimit);
            String[] feeSplit = AppManager.addDotWidthIndex(fee.toString()).split("\\.");

            // mineral
            String sMineral = pWalletSelectorList.get(i).getMineral();
            String[] mineralSplit = AppManager.addDotWidthIndex(sMineral).split("\\.");
            BigInteger mineral = new BigInteger(sMineral);

            // gas
            BigInteger gas = fee.subtract(mineral);
            gas = (gas.compareTo(new BigInteger("0")) > 0) ? gas : new BigInteger("0");
            String[] gasSplit = AppManager.addDotWidthIndex(gas.toString()).split("\\.");

            // total amount
            BigInteger totalAmount = new BigInteger(sAmount.replace(".","")).add(gas);
            String[] totalAmountSplit = AppManager.addDotWidthIndex(totalAmount.toString()).split("\\.");

            //after balance
            BigInteger afterBalance = new BigInteger(pWalletSelectorList.get(i).getBalance()).subtract(totalAmount);
            afterBalance = (afterBalance.compareTo(new BigInteger("0")) >=0 ) ? afterBalance : new BigInteger("0");
            String[] afterBalanceSplit = AppManager.addDotWidthIndex(afterBalance.toString()).split("\\.");

            if(i == 0) {
                amountToSendNature.textProperty().setValue(AppManager.comma(balanceSplit[0]));
                amountToSendDecimal.textProperty().setValue("." + balanceSplit[1]);

                detailContentsFeeNum.textProperty().setValue(AppManager.comma(feeSplit[0]) + "." + feeSplit[1]);
                detailContentsTotalNum.textProperty().setValue(AppManager.comma(mineralSplit[0]) + "." + mineralSplit[1]);

            } else {
                amountToSendNature1.textProperty().setValue(AppManager.comma(balanceSplit[0]));
                amountToSendDecimal1.textProperty().setValue("." + balanceSplit[1]);

                detailContentsFeeNum1.textProperty().setValue(AppManager.comma(feeSplit[0]) + "." + feeSplit[1]);
                detailContentsTotalNum1.textProperty().setValue(AppManager.comma(mineralSplit[0]) + "." + mineralSplit[1]);
            }

            if(selectedTabIndex == i) {
                transferAmountTitleNature.textProperty().setValue(AppManager.comma(totalAmountSplit[0]));
                transferAmountTitleDecimal.textProperty().setValue("." + totalAmountSplit[1]);

                transferAmountLabelNature.textProperty().setValue(AppManager.comma(amountSplit[0]));
                transferAmountLabelDecimal.textProperty().setValue("." + amountSplit[1]);

                gasPriceReceiptNature.textProperty().setValue(AppManager.comma(gasSplit[0]));
                gasPriceReceiptDecimal.textProperty().setValue("." + gasSplit[1]);

                totalWithdrawalNature.textProperty().setValue(AppManager.comma(totalAmountSplit[0]));
                totalWithdrawalDecimal.textProperty().setValue("." + totalAmountSplit[1]);

                afterBalanceNature.textProperty().setValue(AppManager.comma(afterBalanceSplit[0]));
                afterBalanceDecimal.textProperty().setValue("." + afterBalanceSplit[1]);
            }
        }

        // 트랜스퍼 버튼 활성화/비활성화 체크
        checkTransferButton();
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
        this.cSelectListView.setVisible(true);
        this.cSelectListView.prefHeightProperty().setValue(-1);
        this.cSelectChild.prefHeightProperty().setValue(-1);
    }

    public void hideContractSelectBox(){
        this.cSelectListView.setVisible(false);
        this.cSelectListView.prefHeightProperty().setValue(0);
        this.cSelectChild.prefHeightProperty().setValue(40);
    }

    public void initTab(int index) {
        this.selectedTabIndex = index;
        initTabClean();
        initSideTabClean();

        if(index == 0) {    //Deploy
            this.tab1LeftPane.setVisible(true);
            this.tab1RightPane.setVisible(true);
            this.transferBtn.setVisible(true);
            this.tabLabel1.setTextFill(Color.web("#910000"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane1.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane1.setVisible(true);

            //button
            transferBtn.setVisible(true);
            writeBtn.setVisible(false);
            readBtn.setVisible(false);

            checkTransferButton();

            // layout data
            initLayoutData();

            // right pane visible
            tab1RightPane.setVisible(true);
            tab2RightPane.setVisible(false);
        } else if(index == 1) {     // Call/Send
            this.tab2LeftPane.setVisible(true);
            this.tab1RightPane.setVisible(true);
            this.tabLabel2.setTextFill(Color.web("#910000"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane2.setVisible(true);

            //button
            transferBtn.setVisible(false);
            writeBtn.setVisible(false);
            readBtn.setVisible(false);

            // walletInputView Hidden
            setWaleltInputViewVisible(true, true);

            // right pane visible
            tab1RightPane.setVisible(false);
            tab2RightPane.setVisible(true);
        } else if(index == 2) {     // Canvas
            this.tabLabel3.setTextFill(Color.web("#910000"));
            this.tabLabel3.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane3.setVisible(true);

        }
        settingLayoutData();
    }

    public void initSideTab(int index) {
        initSideTabClean();

        if(index == 0) {
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane1.setVisible(true);

            codeTab1.setVisible(true);
            codeTab2.setVisible(false);

        } else if(index == 1) {
            this.sideTabLabel2.setTextFill(Color.web("#910000"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane2.setVisible(true);

            codeTab1.setVisible(false);
            codeTab2.setVisible(true);
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
        tabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel3.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLinePane1.setVisible(false);
        tabLinePane2.setVisible(false);
        tabLinePane3.setVisible(false);
    }

    public void initSideTabClean() {
        sideTabLabel1.setTextFill(Color.web("#999999"));
        sideTabLabel2.setTextFill(Color.web("#999999"));
        sideTabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        sideTabLinePane1.setVisible(false);
        sideTabLinePane2.setVisible(false);

        tab1SolidityTextArea1.clear();
        tab1SolidityTextArea2.getChildren().clear();
        tab1SolidityTextArea3.setText("");
    }

    public void initContract() {
        cSelectHead.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;");
        cSelectHeadText.setText("Select a function");
        cSelectHeadText.setTextFill(Color.web("#999999"));
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

    public boolean checkTransferButton(){
        boolean result = false;

        String data = tab1SolidityTextArea1.getText();
        String gasLimit = tab1GasLimitTextField.getText();
        if(data.length() > 0 && contractInputView.isVisible()
                && gasLimit.length() > 0){
                // TODO : && minGasLimit <= Long.parseLong(gasLimit)){
            result = true;
        }

        if(result){
            transferBtn.setStyle( new JavaFXStyle(transferBtn.getStyle()).add("-fx-background-color","#910000").toString());
        }else{
            transferBtn.setStyle( new JavaFXStyle(transferBtn.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }
        return result;
    }


    /**
     *
     * @param contractName : 컨트렉트 이름
     */
    private void deployContractFieldInMethodList(String contractName){
        // 컨트렉트 선택시 생성자 체크
        if(res != null){

            metadata = res.getContract(contractName);
            if(metadata.bin == null || metadata.bin.isEmpty()){
                throw new RuntimeException("Compilation failed, no binary returned");
            }

            CallTransaction.Contract cont = new CallTransaction.Contract(metadata.abi);
            CallTransaction.Function function = cont.getByName(""); // get constructor

            contractMethodList.getChildren().clear();  //필드 초기화
            contractParams.clear();

            if(function == null) { return ; }
            CallTransaction.Param param = null;
            for(int i=0; i<function.inputs.length; i++){
                param = function.inputs[i];

                String paramName = param.name;
                String paramType = param.type.toString();

                Node node = null;
                if(param.type instanceof SolidityType.BoolType){
                    // BOOL

                    CheckBox checkBox = new CheckBox();
                    checkBox.setText(paramName);
                    checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = checkBox;

                    // param 등록
                    SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();
                    booleanProperty.bind(checkBox.selectedProperty());
                    contractParams.add(booleanProperty);

                }else if(param.type instanceof SolidityType.AddressType){
                    // AddressType
                    final TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);
                    node = textField;

                    // Only Hex, maxlength : 40
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("[0-9a-f]*")) {
                            textField.setText(newValue.replaceAll("[^0-9a-f]", ""));
                        }
                        if(textField.getText().length() > 40){
                            textField.setText(textField.getText().substring(0, 40));
                        }

                        // get preGasPrice
                        checkDeployContractPreGasPrice(function, contractName);
                    });

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.IntType){
                    // INT, uINT

                    System.out.println("node int");
                    final TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);

                    // Only Number
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue,
                                            String newValue) {
                            if (!newValue.matches("\\d*")) {
                                textField.setText(newValue.replaceAll("[^\\d]", ""));
                            }

                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = textField;

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.StringType){
                    // StringType

                    TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = textField;

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.BytesType){
                    // BytesType

                    TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = textField;

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.Bytes32Type){
                    // Bytes32Type

                    TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = textField;

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.FunctionType){
                    // FunctionType

                    TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = textField;

                }else if(param.type instanceof SolidityType.ArrayType){
                    // ArrayType

                    TextField textField = new TextField();
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            // get preGasPrice
                            checkDeployContractPreGasPrice(function, contractName);
                        }
                    });
                    node = textField;

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);
                }

                if(node != null){
                    //필드에 추가
                    contractMethodList.getChildren().add(node);
                }
            } //for function.inputs

            checkDeployContractPreGasPrice(function, contractName);
        }
    }

    public void checkDeployContractPreGasPrice(CallTransaction.Function function,  String contractName){
        Object[] args = new Object[function.inputs.length];

        // 초기화
        CallTransaction.Param param = null;
        for(int i=0; i<function.inputs.length; i++){
            param = function.inputs[i];

            if(param.type instanceof SolidityType.BoolType){
                // BOOL
                SimpleBooleanProperty booleanProperty = (SimpleBooleanProperty)contractParams.get(i);
                args[i] = booleanProperty.get();

            }else if(param.type instanceof SolidityType.AddressType){
                // AddressType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)contractParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.IntType){
                // INT, uINT
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)contractParams.get(i);
                try{
                    args[i] = Integer.parseInt(simpleStringProperty.get());
                }catch (NumberFormatException e){
                    args[i] = 0;
                }

            }else if(param.type instanceof SolidityType.StringType){
                // StringType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)contractParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.BytesType){
                // BytesType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)contractParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.Bytes32Type){
                // Bytes32Type
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)contractParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.FunctionType){
                // FunctionType
                args[i] = new byte[0];

            }else if(param.type instanceof SolidityType.ArrayType){
                // ArrayType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)contractParams.get(i);
                args[i] = simpleStringProperty.get();
            }
        } //for function.inputs


        String contract = this.tab1SolidityTextArea1.getText();
        byte[] address = Hex.decode(walletSelectorController.getAddress());
        long preGasUsed = AppManager.getInstance().getPreGasCreateContract(address, contract, contractName, args);
        tab1GasLimitTextField.textProperty().set(""+preGasUsed);
        minGasLimit = preGasUsed;
    }

    public void checkSendFunctionPreGasPrice(CallTransaction.Function function,  String contractAddress, String medataAbi){
        Object[] args = new Object[function.inputs.length];

        // 초기화
        CallTransaction.Param param = null;
        for(int i=0; i<function.inputs.length; i++){
            param = function.inputs[i];

            if(param.type instanceof SolidityType.BoolType){
                // BOOL
                SimpleBooleanProperty booleanProperty = (SimpleBooleanProperty)selectFunctionParams.get(i);
                args[i] = booleanProperty.get();

            }else if(param.type instanceof SolidityType.AddressType){
                // AddressType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)selectFunctionParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.IntType){
                // INT, uINT
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)selectFunctionParams.get(i);
                try{
                    args[i] = Integer.parseInt(simpleStringProperty.get());
                }catch (NumberFormatException e){
                    args[i] = 0;
                }

            }else if(param.type instanceof SolidityType.StringType){
                // StringType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)selectFunctionParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.BytesType){
                // BytesType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)selectFunctionParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.Bytes32Type){
                // Bytes32Type
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)selectFunctionParams.get(i);
                args[i] = simpleStringProperty.get();

            }else if(param.type instanceof SolidityType.FunctionType){
                // FunctionType
                args[i] = new byte[0];

            }else if(param.type instanceof SolidityType.ArrayType){
                // ArrayType
                SimpleStringProperty simpleStringProperty = (SimpleStringProperty)selectFunctionParams.get(i);
                args[i] = simpleStringProperty.get();
            }
        } //for function.inputs

        String functionName = function.name;
        byte[] address = Hex.decode(walletSelectorController.getAddress());
        long preGasUsed = AppManager.getInstance().getPreGasUsed(medataAbi, address, Hex.decode(contractAddress), functionName, args);
        tab2GasLimitTextField.textProperty().set(""+preGasUsed);
        minGasLimit = preGasUsed;
    }
}
