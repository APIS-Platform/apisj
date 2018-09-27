package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupRemoveWalletController extends BasePopupController {

    @FXML
    private Label title, subTitle, noButton, yesButton;

    private ArrayList<String> removeWalletIdList = new ArrayList<>();

    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex);
        PopupManager.getInstance().hideMainPopup(zIndex-1);
    }

    @FXML
    public void remove(){
        if(handler != null){
            handler.remove(this.removeWalletIdList);
        }
        this.removeWalletIdList = new ArrayList<>();
        PopupManager.getInstance().hideMainPopup(zIndex);
        PopupManager.getInstance().hideMainPopup(zIndex-1);
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


    private PopupRemoveWalletImpl handler;
    public void setHandler(PopupRemoveWalletImpl handler){
        this.handler = handler;
    }
    public interface PopupRemoveWalletImpl{
        void remove(List<String> removeWalletIdList);
    }
}
