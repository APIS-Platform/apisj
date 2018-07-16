package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.WalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class WalletListBodyController implements Initializable {

    public static final int WALLET_LIST_BODY_TYPE_APIS = 0;
    public static final int WALLET_LIST_BODY_TYPE_MINERAL = 1;
    private int bodyType = WALLET_LIST_BODY_TYPE_APIS;

    private WalletItemModel model;
    private Image apisIcon, mineraIcon;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private ImageView icon;

    @FXML
    private Label name, valueNatural, valueDecimal, valueUnit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apisIcon = new Image("image/ic_apis@2x.png");
        mineraIcon = new Image("image/ic_mineral@2x.png");

        init(WALLET_LIST_BODY_TYPE_APIS);
    }

    public WalletListBodyController init(int type){
        this.bodyType = type;
        return this;
    }

    public void setBalance(String balance){
        if(balance == null) return;

        String newBalance = AppManager.addDotWidthIndex(balance);
        String[] splitBalance = newBalance.split("\\.");

        switch (this.bodyType){
            case WALLET_LIST_BODY_TYPE_APIS :
                this.model.apisNaturalProperty().setValue(splitBalance[0]);
                this.model.apisDecimalProperty().setValue("."+splitBalance[1]);
                break;

            case WALLET_LIST_BODY_TYPE_MINERAL :
                this.model.mineralNaturalProperty().setValue(splitBalance[0]);
                this.model.mineralDecimalProperty().setValue("."+splitBalance[1]);
                break;
        }
    }
    public String getBalance(){
        String result = "";
        switch (this.bodyType){
            case WALLET_LIST_BODY_TYPE_APIS :
                result = result + this.model.getApisNatural();
                result = result + this.model.getApisDecimal();
                break;

            case WALLET_LIST_BODY_TYPE_MINERAL :
                result = result + this.model.getMineralNatural();
                result = result + this.model.getMineralDecimal();
                break;
        }

        return result.replace(".","");
    }


    public void show(){
        this.rootPane.setMinHeight(52.0);
        this.rootPane.setMaxHeight(52.0);
        this.rootPane.setPrefHeight(52.0);
        this.rootPane.setVisible(true);
    }
    public void hide(){
        this.rootPane.setMinHeight(0.0);
        this.rootPane.setMaxHeight(0.0);
        this.rootPane.setPrefHeight(0.0);
        this.rootPane.setVisible(false);
    }

    public void setModel(WalletItemModel model){
        this.model = model;

        valueNatural.textProperty().unbind();
        valueDecimal.textProperty().unbind();
        switch (this.bodyType){
            case WALLET_LIST_BODY_TYPE_APIS :
                name.setText(WalletItemModel.WALLET_NAME_APIS);
                valueUnit.setText(WalletItemModel.UNIT_TYPE_STRING_APIS);
                icon.setImage(apisIcon);
                valueNatural.textProperty().bind(this.model.apisNaturalProperty());
                valueDecimal.textProperty().bind(this.model.apisDecimalProperty());
                break;
            case WALLET_LIST_BODY_TYPE_MINERAL :
                name.setText(WalletItemModel.WALLET_NAME_MINERAL);
                valueUnit.setText(WalletItemModel.UNIT_TYPE_STRING_MINERAL);
                icon.setImage(mineraIcon);
                valueNatural.textProperty().bind(this.model.mineralNaturalProperty());
                valueDecimal.textProperty().bind(this.model.mineralDecimalProperty());
                break;
        }

    }
}
