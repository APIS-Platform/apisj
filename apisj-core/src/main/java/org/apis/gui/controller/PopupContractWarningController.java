package org.apis.gui.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.db.sql.DBManager;
import org.apis.facade.EthereumImpl;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import javafx.scene.control.*;
import org.apis.gui.manager.StringManager;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupContractWarningController implements Initializable {

    // Multilingual Support Label
    @FXML
    private Label warningTitle, warningDesc, walletPasswordLabel, generateTxBtn, rawTxLabel, signedTxLabel, noBtn, yesBtn;

    @FXML
    private ApisTextFieldController passwordController;

    @FXML private TextArea rawTxArea, signedTxArea;

    private String address, balance, gasPrice, gasLimit, contractName;
    private CompilationResult.ContractMetadata metadata;
    private ArrayList<Object> contractParams;
    private Transaction tx;

    public void exit() { AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

    }


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if("generateTxBtn".equals(id)){

            String password = passwordController.getText();
            if(metadata != null){
                byte[] initParams = new byte[0];
                byte[] data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);

                CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
                CallTransaction.Function function = contract.getByName("");

                if(function != null) {
                    Object[] param = new Object[contractParams.size()];
                    for (int i = 0; i < contractParams.size(); i++) {
                        if (contractParams.get(i) instanceof SimpleStringProperty) {
                            SimpleStringProperty stringProperty = (SimpleStringProperty) contractParams.get(i);
                            try {
                                BigInteger integer = new BigInteger(stringProperty.get());
                                param[i] = integer;
                            } catch (Exception err) {
                                try{
                                    param[i] = Hex.decode(stringProperty.get());
                                }catch (Exception err2){
                                    param[i] = stringProperty.get();
                                }
                            }
                        } else if (contractParams.get(i) instanceof SimpleBooleanProperty) {
                            SimpleBooleanProperty booleanProperty = (SimpleBooleanProperty) contractParams.get(i);
                            param[i] = booleanProperty.get();
                        }
                    }

                    if(function.inputs.length > 0){
                        initParams = function.encodeArguments(param);
                    }

                    data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);
                }

                tx = AppManager.getInstance().ethereumGenerateTransaction(this.address, this.balance, this.gasPrice, this.gasLimit, new byte[0],  password, data);
                rawTxArea.setText(tx.toString());
                signedTxArea.setText(Hex.toHexString(tx.getEncoded()));

                System.out.println("tx.getHash() : "+Hex.toHexString(tx.getHash()));
                System.out.println("tx.getContractAddress() : " + Hex.toHexString(tx.getContractAddress()));

                yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color","#910000").toString());
            }
        }else if("noBtn".equals(id)){
            exit();
        }else if("yesBtn".equals(id)){
            if(tx != null){
                AppManager.getInstance().ethereumSendTransactions(tx);
                AppManager.getInstance().guiFx.showMainPopup("popup_success.fxml",1);

                byte[] address = tx.getSender();
                byte[] contractAddress = tx.getContractAddress();
                String abi = this.metadata.abi;

                DBManager.getInstance().updateAbi(address, contractAddress, abi);
                // 컨트렉트를 직접 저장하지 않고, 우선 abi만 저장 후,
                // 컨트렉트가 블록에 씌워졌을 때,비로소 컨트렉트를 저장한다.
                // DBManager.getInstance().updateContract(address, title, mask, abi, canvas_url);
            }
        }

    }

    public void languageSetting() {
        warningTitle.textProperty().bind(StringManager.getInstance().contractPopup.warningTitle);
        warningDesc.textProperty().bind(StringManager.getInstance().contractPopup.warningDesc);
        generateTxBtn.textProperty().bind(StringManager.getInstance().contractPopup.generateTxBtn);
        rawTxLabel.textProperty().bind(StringManager.getInstance().contractPopup.rawTxLabel);
        signedTxLabel.textProperty().bind(StringManager.getInstance().contractPopup.signedTxLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        yesBtn.textProperty().bind(StringManager.getInstance().contractPopup.yesBtn);
        walletPasswordLabel.textProperty().bind(StringManager.getInstance().contractPopup.walletPasswordLabel);
    }

    public void setData(String address, String balance, String gasPrice, String gasLimit, CompilationResult.ContractMetadata metadata, String contractName, ArrayList<Object> contractParams) {
        this.address = address;
        this.balance = balance;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.metadata = metadata;
        this.contractName = contractName;
        this.contractParams = contractParams;
    }
}
