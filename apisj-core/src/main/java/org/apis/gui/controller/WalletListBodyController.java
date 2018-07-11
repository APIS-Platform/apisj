package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class WalletListBodyController implements Initializable {

    public static final int WALLET_LIST_BODY_TYPE_APIS = 0;
    public static final int WALLET_LIST_BODY_TYPE_MINERAL = 1;

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

    public void init(int type){

        setBalance("0");
        switch (type){
            case WALLET_LIST_BODY_TYPE_APIS :
                icon.setImage(apisIcon);
                name.setText("APIS");
                valueUnit.setText("APIS");

                break;
            case WALLET_LIST_BODY_TYPE_MINERAL :
                icon.setImage(mineraIcon);
                name.setText("MINERAL");
                valueUnit.setText("MNR");
                break;
        }
    }

    public void setBalance(String balance){
        if(balance == null) return;

        String newBalance = AppManager.addDotWidthIndex(balance);
        String[] splitBalance = newBalance.split("\\.");

        valueNatural.setText(splitBalance[0]);
        valueDecimal.setText("."+splitBalance[1]);
    }

    public void show(){
        this.rootPane.setMinHeight(52.0);
        this.rootPane.setMaxHeight(52.0);
        this.rootPane.setPrefHeight(52.0);
    }
    public void hide(){
        this.rootPane.setMinHeight(0.0);
        this.rootPane.setMaxHeight(0.0);
        this.rootPane.setPrefHeight(0.0);
    }
}
