package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.keystore.KeyStoreData;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;


public class TransferController implements Initializable {
    private BigInteger gasPrice = new BigInteger("50000000000");

    @FXML
    private TextField amountTextField, recevingTextField;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Slider slider;

    @FXML
    private ApisSelectBoxController walletSelectorController;


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        String keystoreId = walletSelectorController.getKeystoreId();
        if(id.equals("sendBtn")){
            //sendTransfer();
            PopupTransferSendController popupController = (PopupTransferSendController)AppManager.getInstance().guiFx.showMainPopup("popup_transfer_send.fxml", 0);
            popupController.setHandler(new PopupTransferSendController.PopupTransferSendInterface() {
                @Override
                public void send(String password) {
                    for(int i=0; i<AppManager.getInstance().getKeystoreList().size(); i++){
                        KeyStoreData data = AppManager.getInstance().getKeystoreList().get(i);
                        if(data.id.equals(keystoreId)){
                            KeyStoreManager.getInstance().setKeystoreJsonData(data.toString());
                            if(KeyStoreManager.getInstance().matchPassword(password)){
                                sendTransfer(password);
                                init();
                                AppManager.getInstance().guiFx.hideMainPopup(0);
                                break;
                            }else{
                                System.out.println("비밀번호 다름");
                            }
                        }
                    }
                }

                @Override
                public void close() {

                }
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final ProgressIndicator pi = new ProgressIndicator(0);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                progressBar.setProgress(new_val.doubleValue() / 100);
                pi.setProgress(new_val.doubleValue() / 100);

                BigInteger minGasPrice = new BigInteger("50000000000");
                BigInteger maxGasPrice = new BigInteger("500000000000");
                gasPrice = minGasPrice.add(maxGasPrice.subtract(minGasPrice).multiply(new BigInteger(""+new_val.intValue())).divide(new BigInteger("100")));
                System.out.println("gasPrice : "+gasPrice.toString());
            }
        });
    }

    public void init(){
        amountTextField.textProperty().setValue("");
        recevingTextField.textProperty().setValue("");
        walletSelectorController.selectedItem(0);
        initSlider();
    }

    public void initSlider(){
        this.slider.valueProperty().setValue(0);
    }

    public void sendTransfer(String sPasswd){
        String sGasPrice = gasPrice.toString();
        String sValue = amountTextField.getText();
        String sAddr = walletSelectorController.getAddress();
        String sToAddress = recevingTextField.getText();

        System.out.println("sGasPrice : " + sGasPrice);
        System.out.println("sValue : " + sValue);
        System.out.println("sAddr : " + sAddr);
        System.out.println("sToAddress : " + sToAddress);

        BigInteger gas = new BigInteger(sGasPrice);
        BigInteger balance = new BigInteger(sValue);

        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && sValue != null && sValue.length() > 0){

            AppManager.getInstance().ethereumCreateTransactions(sAddr, gas.toString(), "200000", sToAddress, balance.toString(), sPasswd);
            AppManager.getInstance().ethereumSendTransactions();
        }
    }
}
