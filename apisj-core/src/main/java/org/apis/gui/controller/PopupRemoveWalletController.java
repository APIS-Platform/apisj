package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.StringManager;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupRemoveWalletController implements Initializable {

    @FXML
    private Label title, subTitle, noButton, yesButton;

    private ArrayList<String> removeWalletIdList = new ArrayList<>();

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    public void remove(){
        for(int i=0; i<removeWalletIdList.size(); i++){
            KeyStoreManager.getInstance().deleteKeystore(removeWalletIdList.get(i));
        }
        this.removeWalletIdList = new ArrayList<>();
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.getWallet().update();
    }

    public void removeList(ArrayList<String> walletIdList){
        this.removeWalletIdList = walletIdList;
    }
    public void remove(String walletId){
        this.removeWalletIdList = new ArrayList<>();
        removeWalletIdList.add(walletId);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }
    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.removeWalletTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.removeWalletSubTitle);
        noButton.textProperty().bind(StringManager.getInstance().popup.removeWalletNo);
        yesButton.textProperty().bind(StringManager.getInstance().popup.removeWalletYes);
    }


}
