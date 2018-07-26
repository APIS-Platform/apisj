package org.apis.gui.controller;

import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupRemoveWalletController implements Initializable {

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

    }



}
