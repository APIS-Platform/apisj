package org.apis.gui.controller.popup;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.TextField;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.*;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupTokenEditController extends BasePopupController {


    @FXML private AnchorPane rootPane;
    @FXML private ImageView addrCircleImg;
    @FXML private TextField tokenAddressTextField, nameTextField, symbolTextField, decimalTextField, totalSupplyTextField;

    // Multilingual Support Label
    @FXML private Label editTokenTitle, editTokenDesc, contractAddrLabel, nameLabel, minNumLabel, noBtn, editBtn, symbolLabel, supplyLabel;

    private TokenRecord record;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);
        addrCircleImg.setClip(ellipse);

        AppManager.getInstance().settingTextFieldLineStyle(nameTextField);

        nameTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.TAB){
                    nameTextField.requestFocus();
                    event.consume();
                } else if(event.getCode() == KeyCode.ENTER) {
                    editBtnClicked();
                    event.consume();
                }
            }
        });
    }

    public void languageSetting() {
        editTokenTitle.textProperty().bind(StringManager.getInstance().popup.tokenEditEditTokenTitle);
        editTokenDesc.textProperty().bind(StringManager.getInstance().popup.tokenEditEditTokenDesc);
        contractAddrLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditContractAddrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditNameLabel);
        nameTextField.promptTextProperty().bind(StringManager.getInstance().popup.tokenEditNamePlaceholder);
        minNumLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditMinNumLabel);
        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        editBtn.textProperty().bind(StringManager.getInstance().common.editButton);
        symbolLabel.textProperty().bind(StringManager.getInstance().common.symbolLabel);
        supplyLabel.textProperty().bind(StringManager.getInstance().common.supplyLabel);
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
        }else if(id.equals("editBtn")){
            StyleManager.backgroundColorStyle(editBtn, StyleManager.AColor.Cb01e1e);
        }
    }

    @FXML
    public void onMouseEntered(InputEvent evnet){
        String id = ((Node)evnet.getSource()).getId();
        if(id.equals("noBtn")){
            StyleManager.backgroundColorStyle(noBtn, StyleManager.AColor.Cc8c8c8);
        }else if(id.equals("editBtn")){
            StyleManager.backgroundColorStyle(editBtn, StyleManager.AColor.Ca61c1c);
        }
    }

    public void editBtnClicked() {
        String tokenAddress = tokenAddressTextField.getText();
        String tokenName = nameTextField.getText();
        String tokenSymbol = symbolTextField.getText();
        String tokenDecimal = decimalTextField.getText();
        String totalSupply = totalSupplyTextField.getText();

        byte[] addr = ByteUtil.hexStringToBytes(tokenAddress);
        long decimal = Long.parseLong(tokenDecimal);
        BigInteger supply = new BigInteger(totalSupply);
        DBManager.getInstance().updateTokens(addr, tokenName, tokenSymbol, decimal, supply);
        AppManager.getInstance().loadDBTokens();

        exit();
    }

    public void setData(TokenRecord record){
        this.record = record;

        this.tokenAddressTextField.setText(ByteUtil.toHexString(this.record.getTokenAddress()));
        this.nameTextField.setText(this.record.getTokenName());
        this.symbolTextField.setText(this.record.getTokenSymbol());
        this.decimalTextField.setText(Long.toString(this.record.getDecimal()));
        this.totalSupplyTextField.setText(this.record.getTotalSupply().toString());

        this.addrCircleImg.setImage(AppManager.getInstance().getTokenIcon(ByteUtil.toHexString(this.record.getTokenAddress())));
    }

    @Override
    public void exit(){
        PopupManager.getInstance().showMainPopup(rootPane, "popup_token_list.fxml", zIndex);
        parentRequestFocus();
    }

    public void requestFocus() {
        this.nameTextField.requestFocus();
    }
}
