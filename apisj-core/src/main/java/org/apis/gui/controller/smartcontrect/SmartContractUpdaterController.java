package org.apis.gui.controller.smartcontrect;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.*;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.controller.popup.PopupContractReadWriteSelectController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.*;
import org.apis.gui.model.ContractModel;
import org.apis.solidity.SolidityType;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SmartContractUpdaterController extends BaseViewController {
    private final int CONTRACT_ADDRESS_TYPE_SELECT = 0;
    private final int CONTRACT_ADDRESS_TYPE_INPUT = 1;
    private int contractAddressType = CONTRACT_ADDRESS_TYPE_SELECT;

    @FXML private GridPane solidityTextGrid;
    @FXML private AnchorPane contractInputView, selectContractPane, inputContractPane, solidityCodeTabPane;
    @FXML private ComboBox contractCombo;
    @FXML private VBox contractMethodList;
    @FXML private ImageView selectContractIcon, inputContractIcon, frozenImg;
    @FXML private TextField contractAddressTextField, nonceTextField;
    @FXML private TextFlow solidityTextFlow;
    @FXML private Label selectContractToggleButton, textareaMessage, contractAliasLabel, contractAddressLabel, placeholderLabel, apisTotal, apisTotalLabel, btnStartCompile;
    @FXML private Label selectContract, contractCnstAddr, nonceLabel, cautionLabel;

    @FXML private ApisSelectBoxController selectWalletController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private ApisButtonEsimateGasLimitController btnStartPreGasUsedController;

    private Image greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");

    private CompilationResult res;
    private CompilationResult.ContractMetadata metadata;
    private ArrayList<Object> contractParams = new ArrayList<>();
    private CallTransaction.Function selectFunction;
    private ApisCodeArea solidityTextArea = new ApisCodeArea();

    private boolean isCompiled = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        ImageManager.imageViewRectangle30(selectContractIcon);
        ImageManager.imageViewRectangle30(inputContractIcon);
        frozenImg.setVisible(false);

        AppManager.getInstance().settingTextFieldStyle(nonceTextField);
        AppManager.getInstance().settingTextFieldStyle(contractAddressTextField);
        AppManager.getInstance().settingNodeStyle(selectContractPane);


        // Contract Constructor Address Listener
        contractAddressTextField.focusedProperty().addListener(ctrtFocusListener);
        contractAddressTextField.textProperty().addListener(ctrtKeyListener);

        selectWalletController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, false);
        selectWalletController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {
                SmartContractUpdaterController.this.update();
            }

            @Override
            public void onSelectItem() {
                SmartContractUpdaterController.this.update();

                if(handler != null){
                    handler.onAction();
                }
            }
        });
        gasCalculatorController.setHandler(new GasCalculatorController.GasCalculatorImpl() {
            @Override
            public void gasLimitTextFieldFocus(boolean isFocused) {
                if(handler != null){
                    handler.onAction();
                }
            }

            @Override
            public void gasLimitTextFieldChangeValue(String oldValue, String newValue){
                if(handler != null){
                    handler.onAction();
                }
            }

            @Override
            public void gasPriceSliderChangeValue(int value) {
                if(handler != null){
                    handler.onAction();
                }
            }

        });


        // Text Area Listener
        solidityTextArea.focusedProperty().addListener(solidityTextAreaListener);
        solidityTextArea.setOnKeyReleased(solidityTextAreaOnKeyReleased);
        solidityTextGrid.add(solidityTextArea,0,0);


        contractCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue != null) {
                    if(res != null) {
                        metadata = res.getContract(newValue.toString());
                    }
                    // 생성자 필드 생성
                    //createMethodList(newValue.toString());
                }
            }
        });

        btnStartPreGasUsedController.setHandler(new ApisButtonEsimateGasLimitController.ApisButtonEsimateGasLimitImpl() {
            @Override
            public void onMouseClicked(ApisButtonEsimateGasLimitController controller) {
                estimateGasLimit();
            }
        });

        update();
    }

    public void languageSetting() {
        btnStartCompile.textProperty().bind(StringManager.getInstance().smartContract.startCompileButton);
        selectContractToggleButton.textProperty().bind(StringManager.getInstance().common.directInputButton);
        selectContract.textProperty().bind(StringManager.getInstance().smartContract.selectContract);
        contractCnstAddr.textProperty().bind(StringManager.getInstance().smartContract.selectContractConstructor);
        nonceLabel.textProperty().bind(StringManager.getInstance().smartContract.nonce);
        textareaMessage.textProperty().bind(StringManager.getInstance().smartContract.textareaMessage);
        cautionLabel.textProperty().bind(StringManager.getInstance().smartContract.updateCaution);
    }

    @Override
    public void update(){
        selectWalletController.update();
        apisTotal.setText(ApisUtil.readableApis(selectWalletController.getBalance(), ',', true));
        gasCalculatorController.setMineral(selectWalletController.getMineral());

        byte[] address = Hex.decode(selectWalletController.getAddress());
        byte[] contractAddress = getContractAddress();
        long nonce = AppManager.getInstance().getContractCreateNonce(address,contractAddress);
        nonceTextField.setText(Long.toString(nonce));

        if(nonce >= 0){
            solidityCodeTabPane.setVisible(true);
            solidityCodeTabPane.setPrefHeight(-1);

            cautionLabel.textProperty().unbind();
            cautionLabel.textProperty().bind(StringManager.getInstance().smartContract.updateCaution);
        }else{
            nonceTextField.setText("");

            solidityCodeTabPane.setVisible(false);
            solidityCodeTabPane.setPrefHeight(0);

            if(contractAddress.length > 0){
                cautionLabel.textProperty().unbind();
                cautionLabel.textProperty().bind(StringManager.getInstance().smartContract.creatorNotMatch);
            }else{
                cautionLabel.textProperty().unbind();
                cautionLabel.textProperty().bind(StringManager.getInstance().smartContract.updateCaution);
            }
        }
    }

    public void sendTransfer() {
        if(isReadyTransfer()) {
            String from = selectWalletController.getAddress();
            String value = getAmount().toString();
            String gasPrice = this.gasCalculatorController.getGasPrice().toString();
            String gasLimit = this.gasCalculatorController.getGasLimit().toString();
            byte[] to = AppManager.getInstance().constants.getSMART_CONTRACT_CODE_CHANGER();
            byte[] functionCallBytes = getContractByteCode();

            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null, "popup_contract_warning.fxml", 0);
            controller.setData(from, value, gasPrice, gasLimit, to, new byte[0], functionCallBytes);
            controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                @Override
                public void success(Transaction tx) {
                    byte[] contractAddress = getContractAddress();
                    String contractName = getContractName();
                    String abi = getAbi();

                    DBManager.getInstance().updateContractCode(contractAddress, contractName, abi);
                }

                @Override
                public void fail(Transaction tx) {

                }
            });
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("selectContractToggleButton")) {
            if(contractAddressType == CONTRACT_ADDRESS_TYPE_SELECT) {
                contractAddressType = CONTRACT_ADDRESS_TYPE_INPUT;

                StyleManager.backgroundColorStyle(selectContractToggleButton, StyleManager.AColor.C000000);
                StyleManager.borderColorStyle(selectContractToggleButton, StyleManager.AColor.C000000);
                StyleManager.fontColorStyle(selectContractToggleButton, StyleManager.AColor.Cffffff);
                contractAddressTextField.setText("");
                inputContractIcon.setImage(greyCircleAddrImg);
                selectContractPane.setVisible(false);
                inputContractPane.setVisible(true);
            } else if(contractAddressType == CONTRACT_ADDRESS_TYPE_INPUT) {
                contractAddressType = CONTRACT_ADDRESS_TYPE_SELECT;

                StyleManager.backgroundColorStyle(selectContractToggleButton, StyleManager.AColor.Cf8f8fb);
                StyleManager.borderColorStyle(selectContractToggleButton, StyleManager.AColor.C999999);
                StyleManager.fontColorStyle(selectContractToggleButton, StyleManager.AColor.C999999);
                selectContractPane.setVisible(true);
                inputContractPane.setVisible(false);
            }
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnStartCompile")){
            if(solidityTextArea.getText().length() > 0){
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Ca61c1c);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.Ca61c1c);
            }
        }
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnStartCompile")){
            if(solidityTextArea.getText().length() > 0){
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Cb01e1e);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.Cb01e1e);
            }
        }
    }


    @FXML
    public void onMousePressed(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("btnStartCompile")){
            if(solidityTextArea.getText().length() > 0) {
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Ca61c1c);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.Ca61c1c);
            }
        }
    }

    @FXML
    public void onMouseReleased(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("btnStartCompile")){
            if(solidityTextArea.getText().length() > 0){
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Cb01e1e);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.Cb01e1e);
            }else{
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.C999999);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.C999999);
            }
        }
    }


    @FXML
    public void startToCompile(){
        res = null;
        this.solidityTextFlow.getChildren().clear();

        String contract = this.solidityTextArea.getText();

// 컴파일에 성공하면 json 스트링을 반환한다.
        String message = AppManager.getInstance().ethereumSmartContractStartToCompile(contract);
        if(message != null && message.length() > 0 && AppManager.isJSONValid(message)){
            try {
                isCompiled = true;
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
            isCompiled = false;
            textareaMessage.setVisible(false);
            contractInputView.setVisible(false);

            String[] tempSplit = message.split("<stdin>:");
            for(int i=0; i<tempSplit.length; i++){
                Text text  = new Text(tempSplit[i]);
                text.setFont(Font.font("Roboto Mono", 10));

                if(tempSplit[i].indexOf("Warning") >= 0){
                    text.setFill(Color.rgb(221, 83 , 23));
                }else if(tempSplit[i].indexOf("Error") >= 0){
                    text.setFill(Color.rgb(145, 0 , 0));
                }else {
                    text.setFill(Color.rgb(66, 133 , 244));
                }

                this.solidityTextFlow.getChildren().add(text);
            }
        }
        if(contract == null || contract.length() <= 0){
            textareaMessage.setVisible(true);
            solidityTextFlow.getChildren().clear();
        }

        btnStartPreGasUsedController.setCompiled(isCompiled);
    }

    /**
     * Deploy시 선택한 컨트랙트의 메소드 리스트 생성
     * @param contractName : 컨트렉트 이름
     */
    private void createMethodList(String contractName){
        // 컨트렉트 선택시 생성자 체크
        if(res != null){

            metadata = res.getContract(contractName);
            if(metadata.bin == null || metadata.bin.isEmpty()){
                throw new RuntimeException("Compilation failed, no binary returned");
            }
            CallTransaction.Contract cont = new CallTransaction.Contract(metadata.abi);
            CallTransaction.Function function = cont.getConstructor(); // get constructor
            CallTransaction.Param param = null;

            selectFunction = function;
            contractMethodList.getChildren().clear();  //필드 초기화
            contractParams.clear();

            if(selectFunction == null) { return ; }
            for(int i=0; i<selectFunction.inputs.length; i++){
                param = selectFunction.inputs[i];

                String paramName = param.name;
                String paramType = param.type.toString();

                Node node = null;
                if(param.type instanceof SolidityType.BoolType){
                    // BOOL

                    CheckBox checkBox = new CheckBox();
                    checkBox.setText(paramName);
                    node = checkBox;

                    // param 등록
                    SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();
                    booleanProperty.bind(checkBox.selectedProperty());
                    contractParams.add(booleanProperty);

                }else if(param.type instanceof SolidityType.AddressType){
                    // AddressType
                    final TextField textField = new TextField();
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);
                    node = textField;

                    // Only Hex, maxlength : 40
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("[0-9a-fA-F]*")) {
                            textField.setText(newValue.replaceAll("[^0-9a-fA-F]", ""));
                        }
                        if(textField.getText().length() > 40){
                            textField.setText(textField.getText().substring(0, 40));
                        }
                    });

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.IntType){
                    // INT, uINT

                    final TextField textField = new TextField();
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);

                    // Only Number
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue,
                                            String newValue) {
                            if (!newValue.matches("\\d*")) {
                                textField.setText(newValue.replaceAll("[^\\d]", ""));
                            }

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
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);
                    node = textField;

                    // param 등록
                    SimpleStringProperty stringProperty = new SimpleStringProperty();
                    stringProperty.bind(textField.textProperty());
                    contractParams.add(stringProperty);

                }else if(param.type instanceof SolidityType.BytesType){
                    // BytesType

                    TextField textField = new TextField();
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            if (!newValue.matches("[0-9a-fA-F]*")) {
                                textField.setText(newValue.replaceAll("[^0-9a-fA-F]", ""));
                            }

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
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            if (!newValue.matches("[0-9a-fA-F]*")) {
                                textField.setText(newValue.replaceAll("[^0-9a-fA-F]", ""));
                            }

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
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        }
                    });
                    node = textField;

                }else if(param.type instanceof SolidityType.ArrayType){
                    // ArrayType

                    CallTransaction.Param _param = param;

                    TextField textField = new TextField();
                    textField.setMinHeight(30);
                    textField.setPromptText(paramType+" "+paramName);
                    textField.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            if(_param.type.getCanonicalName().indexOf("int") >=0){
                                if (!textField.getText().matches("[0-9\\[],]*")) {
                                    textField.setText(textField.getText().replaceAll("[^0-9\\[],]", ""));
                                }
                            }else if(_param.type.getCanonicalName().indexOf("address") >=0){
                                if (!textField.getText().matches("[0-9a-fA-F\\[],]*")) {
                                    textField.setText(textField.getText().replaceAll("[^0-9a-fA-F\\[],]", ""));
                                }
                            }

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
        }
    }

    public void openSelectContractPopup(){
        PopupContractReadWriteSelectController controller = (PopupContractReadWriteSelectController)PopupManager.getInstance().showMainPopup(null, "popup_contract_read_write_select.fxml", 0);
        controller.setHandler(new PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl() {
            @Override
            public void onClickSelect(ContractModel model) {
                ContractModel contractModel = model;

                contractAliasLabel.setText(model.getName());
                contractAddressLabel.setText(model.getAddress());
                placeholderLabel.setVisible(false);

                if(AppManager.getInstance().isFrozen(contractAddressLabel.getText())) {
                    frozenImg.setVisible(true);
                    StyleManager.fontColorStyle(contractAddressLabel, StyleManager.AColor.C4871ff);
                } else {
                    frozenImg.setVisible(false);
                    StyleManager.fontColorStyle(contractAddressLabel, StyleManager.AColor.C999999);
                }

                Image image = ImageManager.getIdenticons(contractAddressLabel.textProperty().get());
                if (image != null) {
                    selectContractIcon.setImage(image);
                }

                update();
            }
        });
    }

    public void estimateGasLimit(){
        byte[] address = Hex.decode(selectWalletController.getAddress());
        byte[] contractAddress = AppManager.getInstance().constants.getSMART_CONTRACT_CODE_CHANGER();
        byte[] data = getContractByteCode();
        long preGasUsed = 0;
        if(data.length > 0 && address.length > 0 && contractAddress.length > 0) {
            try {
                preGasUsed = AppManager.getInstance().getPreGasUsed(address, contractAddress, data);
                if(preGasUsed < 0){
                    preGasUsed = 0;
                }
            }catch (Exception e){
                preGasUsed = 0;
            }
        }
        gasCalculatorController.setGasLimit(Long.toString(preGasUsed));
    }

    private ChangeListener<Boolean> ctrtFocusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {


            if(oldValue){
                update();
            }
        }
    };

    private ChangeListener<String> ctrtKeyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!contractAddressTextField.getText().matches("[0-9a-fA-F]*")) {
                contractAddressTextField.setText(contractAddressTextField.getText().replaceAll("[^0-9a-fA-F]", ""));
            }

            int maxlangth = 40;
            if (contractAddressTextField.getText().trim().length() > maxlangth) {
                contractAddressTextField.setText(contractAddressTextField.getText().trim().substring(0, maxlangth));
            }

            if (contractAddressTextField.getText() == null || contractAddressTextField.getText().trim().length() < maxlangth) {

                inputContractIcon.setImage(greyCircleAddrImg);
            } else {
                Image image = ImageManager.getIdenticons(contractAddressTextField.getText().trim());
                if (image != null) {
                    inputContractIcon.setImage(image);
                }
            }
        }
    };

    private byte[]  getContractAddress(){
        if(this.contractAddressType == CONTRACT_ADDRESS_TYPE_SELECT){
            return Hex.decode(this.contractAddressLabel.getText().trim());
        }else if(this.contractAddressType == CONTRACT_ADDRESS_TYPE_INPUT){
            return Hex.decode(this.contractAddressTextField.getText().trim());
        }
        return null;
    }
    private byte[] getContractByteCode(){
        String from = selectWalletController.getAddress();
        String contractSource = solidityTextArea.getText();
        String contractName = getContractName();

        if(from == null || from.length() == 0
                || contractSource == null || contractSource.length() == 0
                || contractName == null || contractName.length() == 0 ){
            return new byte[0];
        }
        byte[] contractAddr = getContractAddress();
        byte[] nonce = ByteUtil.longToBytes(Long.parseLong((nonceTextField.getText().trim() != null) ? nonceTextField.getText().trim() : "0"));
        byte[] byteCode = new byte[0];

        byteCode = AppManager.getInstance().getContractCreationCode(from, ByteUtil.byteArrayToLong(nonce), contractSource, contractName);
        return ByteUtil.merge(contractAddr, nonce, byteCode);
    }

    public String getAbi(){
        return metadata.abi;
    }

    private String getContractName(){
        if(contractCombo.getSelectionModel().getSelectedItem() != null){
            return contractCombo.getSelectionModel().getSelectedItem().toString();
        }else{
            return "";
        }
    }

    public BigInteger getAmount() {
        return BigInteger.ZERO;
    }

    public BigInteger getMineral() {
        return this.selectWalletController.getMineral();
    }

    public BigInteger getBalance() {
        return this.selectWalletController.getBalance();
    }

    public BigInteger getFee() {
        return this.gasCalculatorController.getFee();
    }
    public BigInteger getChargedFee() {
        return this.gasCalculatorController.getTotalFee();
    }

    public BigInteger getChargedAmount(){
        BigInteger totalFee = getChargedFee();
        // total fee
        if(totalFee.toString().indexOf("-") >= 0){
            totalFee = BigInteger.ZERO;
        }

        // total amount
        BigInteger totalAmount = getAmount().add(totalFee);

        return totalAmount;
    }

    public BigInteger getAfterBalance(){
        // total amount
        BigInteger totalAmount = getChargedAmount();

        //after balance
        BigInteger afterBalance = getBalance().subtract(totalAmount);

        return afterBalance;
    }

    public boolean isReadyTransfer(){
        // 소지금체크
        if(getBalance().compareTo(BigInteger.ZERO) <= 0){
            return false;
        }

        // 잔액체크
        if(getAfterBalance().compareTo(BigInteger.ZERO) < 0){
            return false;
        }

        // 데이터 입력 여부 체크
        String data = solidityTextArea.getText();
        String gasLimit = gasCalculatorController.getGasLimit().toString();
        if (data.length() > 0 && contractInputView.isVisible() && gasLimit.length() > 0) {
        }else{
            return false;
        }

        return true;
    }


    private ChangeListener<Boolean> solidityTextAreaListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        }
    };
    private EventHandler<KeyEvent> solidityTextAreaOnKeyReleased = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {

            if(solidityTextArea.getText().length() > 0){
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Cb01e1e);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.Cb01e1e);
            }else{
                StyleManager.backgroundColorStyle(btnStartCompile, StyleManager.AColor.Cffffff);
                StyleManager.fontColorStyle(btnStartCompile, StyleManager.AColor.C999999);
                StyleManager.borderColorStyle(btnStartCompile, StyleManager.AColor.C999999);
            }

            event.consume();
        }
    };

    private SmartContractUpdaterImpl handler;
    public void setHandler(SmartContractUpdaterImpl handler){
        this.handler = handler;
    }
    public interface SmartContractUpdaterImpl {
        void onAction();
    }
}
