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
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisCodeArea;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.module.TabMenuController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.solidity.SolidityType;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SmartContractDeployController extends BaseViewController {
    private final int TAB_SOLIDITY_CONTRACT = 0;
    private final int TAB_CONTRACT_BYTE_CODE = 1;
    private int selectTabIndex = 0;

    @FXML private GridPane tab1SolidityTextGrid, codeTab1, codeTab2;
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private ComboBox contractCombo;

    // Contract TextArea
    @FXML private AnchorPane contractInputView;
    @FXML private VBox contractMethodList;

    @FXML private TextFlow tab1SolidityTextArea2;
    @FXML private TextArea tab1SolidityTextArea3, tab1SolidityTextArea4;
    @FXML private Label textareaMessage, warningLabel;
    @FXML private TabMenuController tabMenuController;

    private CompilationResult res;
    private CompilationResult.ContractMetadata metadata;
    private ArrayList<Object> contractParams = new ArrayList<>();
    private CallTransaction.Function selectFunction;

    private ArrayList<Object> selectFunctionParams = new ArrayList();
    private ApisCodeArea tab1SolidityTextArea1 = new ApisCodeArea();
    private String selectContractName;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        tabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                setSelectedTab(index);
            }
        });

        // Percentage Select Box List Handling
        walletAndAmountController.setHandler(new ApisWalletAndAmountController.ApisAmountImpl() {
            @Override
            public void change(BigInteger value) {
                if(handler != null){
                    handler.onAction();
                }
            }
        });

        contractCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue != null) {

                    selectContractName = newValue.toString();

                    // 생성자 필드 생성
                    deployContractFieldInMethodList(newValue.toString());
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
        tab1SolidityTextArea1.focusedProperty().addListener(tab1TextAreaListener);
        tab1SolidityTextArea1.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                event.consume();
            }
        });

        tab1SolidityTextArea3.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!tab1SolidityTextArea3.getText().matches("[0-9a-fA-F]*")) {
                    tab1SolidityTextArea3.setText(tab1SolidityTextArea3.getText().replaceAll("[^0-9a-fA-F]", ""));
                }
            }
        });

        tab1SolidityTextGrid.add(tab1SolidityTextArea1,0,0);

        tabMenuController.selectedMenu(TAB_SOLIDITY_CONTRACT);
        setSelectedTab(TAB_SOLIDITY_CONTRACT);
    }

    private ChangeListener<Boolean> tab1TextAreaListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

        }
    };
    public void languageSetting() {

        tabMenuController.addItem(StringManager.getInstance().smartContract.sideTabLabel1, TAB_SOLIDITY_CONTRACT);
        tabMenuController.addItem(StringManager.getInstance().smartContract.sideTabLabel2, TAB_CONTRACT_BYTE_CODE);
        textareaMessage.textProperty().bind(StringManager.getInstance().smartContract.textareaMessage);
    }

    public void update(){
        walletAndAmountController.update();
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if(fxId.equals("btnStartPreGasUsed")){
            startToDeployPreGasUsed();
        }

        if(fxId.equals("btnStartCompile")){
            startToCompile();
        }

        if(fxId.equals("sideTab1")) {
            setSelectedTab(TAB_SOLIDITY_CONTRACT);

        } else if(fxId.equals("sideTab2")) {
            setSelectedTab(TAB_CONTRACT_BYTE_CODE);

        }
    }

    public void sendTransfer() {
        if(isReadyTransfer()){
            String address = this.walletAndAmountController.getAddress().trim();
            String value = this.walletAndAmountController.getAmount().toString();
            String gasPrice = this.gasCalculatorController.getGasPrice().toString();
            String gasLimit = this.gasCalculatorController.getGasLimit().toString();
            String contractName = (String)this.contractCombo.getSelectionModel().getSelectedItem();
            String byteCode = tab1SolidityTextArea3.getText().trim();
            String abi = tab1SolidityTextArea4.getText().trim();
            byte[] initParams = new byte[0];
            byte[] data = null;

            if(this.selectTabIndex == TAB_SOLIDITY_CONTRACT){
                abi = metadata.abi;
                byteCode = metadata.bin;
                data = ByteUtil.merge(Hex.decode(byteCode), initParams);
            } else if(this.selectTabIndex == TAB_CONTRACT_BYTE_CODE) {
                contractName = "(Unnamed) SmartContract";
                abi = tab1SolidityTextArea4.getText();
                byteCode = tab1SolidityTextArea3.getText().replaceAll("[^0-9a-fA-F]]","");
                data = Hex.decode(byteCode);
            }

            CallTransaction.Contract contract = new CallTransaction.Contract(abi);
            CallTransaction.Function function = contract.getByName("");

            if(function != null) {
                // 데이터 불러오기
                Object[] args = new Object[function.inputs.length];
                for (int i = 0; i < contractParams.size(); i++) {
                    if(function.inputs[i].type instanceof SolidityType.BoolType){
                        SimpleBooleanProperty property = (SimpleBooleanProperty) contractParams.get(i);
                        args[i] = property.get();
                    }else if(function.inputs[i].type instanceof SolidityType.StringType){
                        SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                        args[i] = property.get();
                    }else if(function.inputs[i].type instanceof SolidityType.ArrayType){
                        SimpleStringProperty property = (SimpleStringProperty)contractParams.get(i);
                        String strData = property.get();
                        strData = strData.replaceAll("\\[","").replaceAll("]","").replaceAll("\"","");
                        String[] dataSplit = strData.split(",");

                        if(function.inputs[i].type.getCanonicalName().indexOf("int") >=0){
                            List<BigInteger> list = new ArrayList<>();
                            for(int j=0; j<dataSplit.length; j++){
                                if(dataSplit[j].length() != 0){
                                    list.add(new BigInteger(dataSplit[j]));
                                }
                            }
                            args[i] = list;
                        }else{
                            List<String> list = new ArrayList<>();
                            for(int j=0; j<dataSplit.length; j++){
                                if(dataSplit[j].length() != 0){
                                    list.add(dataSplit[j]);
                                }
                            }
                            args[i] = list;
                        }
                    }else if(function.inputs[i].type instanceof SolidityType.FunctionType){

                    }else if(function.inputs[i].type instanceof SolidityType.BytesType){
                        SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                        args[i] = Hex.decode(property.get());
                    }else if(function.inputs[i].type instanceof SolidityType.AddressType){
                        SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                        args[i] = Hex.decode(property.get());
                    }else if(function.inputs[i].type instanceof SolidityType.IntType){
                        SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                        BigInteger integer = new BigInteger((property.get().length() == 0) ? "0" : property.get());
                        args[i] = integer;
                    }else if(function.inputs[i].type instanceof SolidityType.Bytes32Type){
                        SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                        args[i] = Hex.decode(property.get());
                    }

                }

                if(this.selectTabIndex == TAB_SOLIDITY_CONTRACT){
                    if(function.inputs.length > 0){
                        initParams = function.encodeArguments(args);
                        data = ByteUtil.merge(Hex.decode(byteCode), initParams);
                        System.out.println("data : \n"+Hex.toHexString(data));
                        System.out.println("api : \n"+abi);
                    }
                }
            }

            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup("popup_contract_warning.fxml", 0);
            controller.setData(address, value, gasPrice, gasLimit, contractName, abi, data);
        }
    }

    public long getPreGasUsed(CallTransaction.Function function,  String contractName){
        if(function == null){
            return 0;
        }
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
                String strData = simpleStringProperty.get();
                strData = strData.replaceAll("\\[","").replaceAll("]","").replaceAll("\"","");
                String[] dataSplit = strData.split(",");
                List<String> list = new ArrayList<>();
                for(int j=0; j<dataSplit.length; j++){
                    if(dataSplit[j].length() != 0){
                        list.add(dataSplit[j]);
                    }
                }
                args[i] = list;
            }
        } //for function.inputs


        String contract = this.tab1SolidityTextArea1.getText();
        byte[] address = Hex.decode(walletAndAmountController.getAddress());
        long preGasUsed = AppManager.getInstance().getPreGasCreateContract(address, contract, contractName, args);
        System.out.println("preGasUsed : "+preGasUsed);
        return preGasUsed;
    }
    public void checkDeployContractPreGasPrice(byte[] address, byte[]data) {
        long preGasUsed = AppManager.getInstance().getPreGasUsed(address, new byte[0], data);
        gasCalculatorController.setGasLimit(Long.toString(preGasUsed));
    }

    /**
     * Deploy시 선택한 컨트랙트의 메소드 리스트 생성
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
            CallTransaction.Function function = cont.getConstructor(); // get constructor
            selectFunction = function;

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
                            System.out.println("_param.type.getCanonicalName() : "+_param.type.getCanonicalName());
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

    @FXML
    private void startToDeployPreGasUsed(){
        if(selectTabIndex == TAB_SOLIDITY_CONTRACT){
            long preGasUsed = getPreGasUsed(selectFunction, selectContractName);
            gasCalculatorController.setGasLimit(Long.toString(preGasUsed));
        }else{
            byte[] address = Hex.decode(walletAndAmountController.getAddress());
            byte[] data = Hex.decode(tab1SolidityTextArea3.getText());
            checkDeployContractPreGasPrice(address, data);
        }
    }

    @FXML
    public void startToCompile(){
        res = null;
        this.tab1SolidityTextArea2.getChildren().clear();

        String contract = this.tab1SolidityTextArea1.getText();

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
        if(contract == null || contract.length() <= 0){
            textareaMessage.setVisible(true);
            tab1SolidityTextArea2.getChildren().clear();
        }
    }









    public boolean isReadyTransfer(){
        // 잔액체크
        if(getAfterBalance().toString().indexOf("-") >= 0){
            return false;
        }

        // 데이터 입력 여부 체크
        if(selectTabIndex == TAB_SOLIDITY_CONTRACT){
            String data = tab1SolidityTextArea1.getText();
            String gasLimit = gasCalculatorController.getGasLimit().toString();
            if (data.length() > 0 && contractInputView.isVisible() && gasLimit.length() > 0) {
            }else{
                return false;
            }

        }
        else if(selectTabIndex == TAB_CONTRACT_BYTE_CODE) {
            String byteCode = tab1SolidityTextArea3.getText();
            String abi = tab1SolidityTextArea4.getText();
            if(byteCode != null && byteCode.length() > 0 && abi != null && abi.length() > 0){
            }else{
                return false;
            }
        }

        return true;
    }



    public void setSelectedTab(int index) {
        this.selectTabIndex = index;
        if(index == TAB_SOLIDITY_CONTRACT) {
            codeTab1.setVisible(true);
            codeTab2.setVisible(false);

        } else if(index == TAB_CONTRACT_BYTE_CODE) {
            codeTab1.setVisible(false);
            codeTab2.setVisible(true);
        }
    }


    public BigInteger getAmount() {
        return this.walletAndAmountController.getAmount();
    }

    public BigInteger getBalance() {
        return this.walletAndAmountController.getBalance();
    }

    public BigInteger getTotalFee() {
        return this.gasCalculatorController.getTotalFee();
    }

    public BigInteger getTotalAmount(){
        BigInteger totalFee = getTotalFee();
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
        BigInteger totalAmount = getTotalAmount();

        //after balance
        BigInteger afterBalance = getBalance().subtract(totalAmount);

        return afterBalance;
    }



    private SmartContractDeployImpl handler;
    public void setHandler(SmartContractDeployImpl handler){
        this.handler = handler;
    }

    public interface SmartContractDeployImpl {
        void onAction();
    }
}
