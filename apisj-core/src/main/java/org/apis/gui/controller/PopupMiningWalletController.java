package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupMiningWalletController implements Initializable {

    @FXML
    private ApisSelectBoxController walletSelectorController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("selectBtn")){
            AppManager.getInstance().guiFx.showMainPopup("popup_mining_wallet_confirm.fxml", 1);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        walletSelectorController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        walletSelectorController.setHandler(new ApisSelectBoxController.SelectEvent(){
            @Override
            public void onSelectItem() {

            }
        });
        walletSelectorController.setItemListVisible(true);
    }
}
