package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.TextField;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupTokenEditController extends BasePopupController {


    @FXML private AnchorPane rootPane;
    @FXML private ImageView addrCircleImg, resultAddrCircleImg;
    @FXML private TextField tokenAddressTextField, nameTextField, symbolTextField, decimalTextField, totalSupplyTextField;

    // Multilingual Support Label
    @FXML private Label editTokenTitle, editTokenDesc, contractAddrLabel, nameLabel, minNumLabel, previewLabel, noBtn, editBtn;

    private TokenRecord record;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);


        AppManager.settingTextField(nameTextField);
        addrCircleImg.setClip(ellipse);
        resultAddrCircleImg.imageProperty().bind(addrCircleImg.imageProperty());
    }

    public void languageSetting() {
        editTokenTitle.textProperty().bind(StringManager.getInstance().popup.tokenEditEditTokenTitle);
        editTokenDesc.textProperty().bind(StringManager.getInstance().popup.tokenEditEditTokenDesc);
        contractAddrLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditContractAddrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditNameLabel);
        nameTextField.promptTextProperty().bind(StringManager.getInstance().popup.tokenEditNamePlaceholder);
        minNumLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditMinNumLabel);
        previewLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditPreviewLabel);
        noBtn.textProperty().bind(StringManager.getInstance().popup.tokenEditNoBtn);
        editBtn.textProperty().bind(StringManager.getInstance().popup.tokenEditEditBtn);
    }

    public void editBtnClicked() {
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
    }

    public void setData(TokenRecord record){
        this.record = record;

        this.tokenAddressTextField.setText(ByteUtil.toHexString(this.record.getTokenAddress()));
        this.nameTextField.setText(this.record.getTokenName());
        this.symbolTextField.setText(this.record.getTokenSymbol());
        this.decimalTextField.setText(Long.toString(this.record.getDecimal()));
        this.totalSupplyTextField.setText(this.record.getTotalSupply().toString());

    }

    @Override
    public void exit(){
        PopupManager.getInstance().showMainPopup(rootPane, "popup_token_list.fxml", zIndex);
        parentRequestFocus();
    }

    public void requestFocus() {
        this.tokenAddressTextField.requestFocus();
    }
}
