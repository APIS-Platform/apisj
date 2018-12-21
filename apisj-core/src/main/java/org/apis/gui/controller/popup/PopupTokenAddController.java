package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.TextField;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.*;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupTokenAddController extends BasePopupController {

    @FXML private AnchorPane rootPane;
    @FXML private ImageView addrCircleImg;
    @FXML private TextField tokenAddressTextField, nameTextField, symbolTextField, decimalTextField, totalSupplyTextField;
    @FXML private Label addTokenTitle, addTokenDesc, contractAddrLabel, nameLabel, minNumLabel, noBtn, addBtn, supplyLabel, symbolLabel;
    @FXML private ScrollPane scrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);

        addrCircleImg.setClip(ellipse);

        AppManager.settingTextFieldStyle(tokenAddressTextField);
        AppManager.settingTextFieldLineStyle(nameTextField);

        tokenAddressTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if(oldValue){

                    String contractAddress = tokenAddressTextField.getText();
                    String tokenName = AppManager.getInstance().getTokenName(contractAddress);
                    String tokenSymbol = AppManager.getInstance().getTokenSymbol(contractAddress);
                    BigInteger totalSupply = AppManager.getInstance().getTokenTotalSupply(contractAddress);
                    long decimal = AppManager.getInstance().getTokenDecimals(contractAddress);

                    nameTextField.setText(tokenName);
                    symbolTextField.setText(tokenSymbol);
                    totalSupplyTextField.setText(totalSupply.toString());
                    decimalTextField.setText(Long.toString(decimal));
                    addrCircleImg.setImage(AppManager.getInstance().getTokenIcon(contractAddress));
                }
            }
        });

        tokenAddressTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int maxlangth = 40;
                if (tokenAddressTextField.getText().trim().length() > maxlangth) {
                    tokenAddressTextField.setText(tokenAddressTextField.getText().trim().substring(0, maxlangth));
                }
            }
        });

        tokenAddressTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.TAB){
                    nameTextField.requestFocus();
                    event.consume();
                }else if(event.getCode() == KeyCode.ENTER){
                    nameTextField.requestFocus();
                    event.consume();
                }
            }
        });

        nameTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.TAB){
                    tokenAddressTextField.requestFocus();
                    event.consume();
                }else if(event.getCode() == KeyCode.ENTER){
                    addBtnClicked();
                    event.consume();
                }
            }
        });
    }

    public void languageSetting() {
        addTokenTitle.textProperty().bind(StringManager.getInstance().popup.tokenAddAddTokenTitle);
        addTokenDesc.textProperty().bind(StringManager.getInstance().popup.tokenAddAddTokenDesc);
        contractAddrLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditContractAddrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditNameLabel);
        nameTextField.promptTextProperty().bind(StringManager.getInstance().popup.tokenEditNamePlaceholder);
        minNumLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditMinNumLabel);
        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        addBtn.textProperty().bind(StringManager.getInstance().common.addButton);
        supplyLabel.textProperty().bind(StringManager.getInstance().common.supplyLabel);
        symbolLabel.textProperty().bind(StringManager.getInstance().common.symbolLabel);
    }

    public void addBtnClicked() {
        String tokenAddress = tokenAddressTextField.getText();
        String tokenName = nameTextField.getText();
        String tokenSymbol = symbolTextField.getText();
        String tokenDecimal = decimalTextField.getText();
        String totalSupply = totalSupplyTextField.getText();

        byte[] addr = Hex.decode(tokenAddress);
        long decimal = Long.parseLong(tokenDecimal);
        BigInteger supply = new BigInteger(totalSupply);
        DBManager.getInstance().updateTokens(addr, tokenName, tokenSymbol, decimal, supply);
        AppManager.getInstance().initTokens();

        exit();
        if(handler != null){
            handler.add();
        }
    }

    @Override
    public void exit(){
        PopupManager.getInstance().showMainPopup(rootPane, "popup_token_list.fxml", zIndex);
        parentRequestFocus();
    }

    @FXML
    public void onMouseClicked(InputEvent evnet){
        String id = ((Node)evnet.getSource()).getId();
        if(id.equals("noBtn")) {
            exit();
        }

    }

    @FXML
    public void onMouseExited(InputEvent evnet){
        String id = ((Node)evnet.getSource()).getId();
        if(id.equals("noBtn")){
            StyleManager.backgroundColorStyle(noBtn, StyleManager.AColor.Cd8d8d8);
        }else if(id.equals("addBtn")){
            StyleManager.backgroundColorStyle(addBtn, StyleManager.AColor.Cb01e1e);
        }
    }

    @FXML
    public void onMouseEntered(InputEvent evnet){
        String id = ((Node)evnet.getSource()).getId();
        if(id.equals("noBtn")){
            StyleManager.backgroundColorStyle(noBtn, StyleManager.AColor.Cc8c8c8);
        }else if(id.equals("addBtn")){
            StyleManager.backgroundColorStyle(addBtn, StyleManager.AColor.Ca61c1c);
        }
    }


    private PopupAddTokenImpl handler;
    public void setHandler(PopupAddTokenImpl handler){
        this.handler = handler;
    }
    public interface PopupAddTokenImpl{
        void add();
    }

    public void requestFocus() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tokenAddressTextField.requestFocus();
                System.out.println("requestFocus");
            }
        });
    }
}
