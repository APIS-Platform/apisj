package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMiningWalletController extends BasePopupController {

    private SelectBoxWalletItemModel model;

    @FXML
    private ApisSelectBoxController walletSelectorController;
    @FXML
    private Label title, subTitle, addressLabel, addressComment, selectBtn;

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("selectBtn")){
            String walletId = walletSelectorController.getKeystoreId();
            String address = walletSelectorController.getAddress();

            PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController)PopupManager.getInstance().showMainPopup("popup_mining_wallet_confirm.fxml", 1);
            //controller.init(walletId, address);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
        walletSelectorController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
        walletSelectorController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl(){
            @Override
            public void onSelectItem() {
            }

            @Override
            public void onMouseClick() {

            }
        });
        walletSelectorController.setVisibleItemList(false);
    }
    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.miningWalletTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.miningWalletSubTitle);
        addressLabel.textProperty().bind(StringManager.getInstance().popup.miningWalletAddress);
        addressComment.textProperty().bind(StringManager.getInstance().popup.miningWalletAddressComment);
        selectBtn.textProperty().bind(StringManager.getInstance().popup.miningWalletSelect);
    }
}
