package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupRemoveWalletController extends BasePopupController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label title, subTitle, noButton, yesButton;

    private ArrayList<byte[]> removeWalletAddressList = new ArrayList<>();

    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex);
        PopupManager.getInstance().hideMainPopup(zIndex-1);
        parentRequestFocus();
    }

    @FXML
    public void remove(){
        if(handler != null){
            handler.remove(this.removeWalletAddressList);
        }
        if(this.removeWalletAddressList != null && this.removeWalletAddressList.size() != 0) {
            DBManager.getInstance().deleteMyAddress(this.removeWalletAddressList.get(0));
        }
        this.removeWalletAddressList.clear();
        PopupManager.getInstance().hideMainPopup(zIndex);
        PopupManager.getInstance().hideMainPopup(zIndex-1);
    }

    public void removeList(ArrayList<byte[]> walletAddressList){
        this.removeWalletAddressList = walletAddressList;
    }
    public void remove(byte[] walletAddress){
        this.removeWalletAddressList = new ArrayList<>();
        removeWalletAddressList.add(walletAddress);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        bgAnchor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER) {
                remove();
            }
        });
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
        void remove(List<byte[]> removeWalletIdList);
    }
}
