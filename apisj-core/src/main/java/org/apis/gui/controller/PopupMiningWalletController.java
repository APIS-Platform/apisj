package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMiningWalletController implements Initializable {

    private SelectBoxWalletItemModel model;

    @FXML
    private ApisSelectBoxController walletSelectorController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("selectBtn")){
            String walletId = walletSelectorController.getKeystoreId();
            String address = walletSelectorController.getAddress();

            PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController)AppManager.getInstance().guiFx.showMainPopup("popup_mining_wallet_confirm.fxml", 1);
            controller.init(walletId, address);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        walletSelectorController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
        walletSelectorController.setHandler(new ApisSelectBoxController.SelectEvent(){
            @Override
            public void onSelectItem() {
            }
        });
        walletSelectorController.setVisibleItemList(false);
    }
}
