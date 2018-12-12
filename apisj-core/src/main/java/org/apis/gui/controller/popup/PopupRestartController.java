package org.apis.gui.controller.popup;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.keystore.KeyStoreDataExp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupRestartController extends BasePopupController {

    @FXML private Label title, subTitle, restartAddressLabel, masterNodeAlias, miningAlias, masterNodeAddress, miningAddress, masterNodeRestartBtn, miningRestartBtn;
    @FXML private ImageView masterNodeIcon, miningIcon;
    @FXML private AnchorPane rootPane, masterNodePane, miningPane;
    @FXML private VBox list;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Rectangle clip = new Rectangle( this.masterNodeIcon.getFitWidth()-0.5, this.masterNodeIcon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        masterNodeIcon.setClip(clip);

        Rectangle clip2 = new Rectangle( this.miningIcon.getFitWidth()-0.5, this.miningIcon.getFitHeight()-0.5 );
        clip2.setArcWidth(30);
        clip2.setArcHeight(30);
        miningIcon.setClip(clip2);

        languageSetting();

        rootPane.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if(event.getCode() == KeyCode.ENTER) {
                miningRestart();
            }
        });
    }
    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().restart.title);
        subTitle.textProperty().bind(StringManager.getInstance().restart.subTitle);
        restartAddressLabel.textProperty().bind(StringManager.getInstance().restart.restartAddressLabel);
        masterNodeRestartBtn.textProperty().bind(StringManager.getInstance().common.restartButton);
        miningRestartBtn.textProperty().bind(StringManager.getInstance().common.restartButton);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if("masterNodeRestartBtn".equals(id)){

            for(int i = 0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
                KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

                if(masterNodeAddress.getText().equals(dataExp.address)){
                    WalletItemModel walletItemModel = new WalletItemModel();
                    walletItemModel.setAlias(dataExp.alias);
                    walletItemModel.setAddress(dataExp.address);
                    walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
                    PopupMasternodeController controller = (PopupMasternodeController) PopupManager.getInstance().showMainPopup(rootPane, "popup_masternode.fxml", zIndex+1);
                    controller.setModel(walletItemModel);
                    controller.getPasswordController().requestFocus();
                    break;
                }
            }

            list.getChildren().remove(masterNodePane);
        }else if("miningRestartBtn".equals(id)){
            miningRestart();
        }

        if(list.getChildren().size() == 0){
            exit();
        }
    }

    private void miningRestart() {
        for(int i = 0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

            if(miningAddress.getText().equals(dataExp.address)){
                WalletItemModel walletItemModel = new WalletItemModel();
                walletItemModel.setAlias(dataExp.alias);
                walletItemModel.setAddress(dataExp.address);
                walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
                PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController) PopupManager.getInstance().showMainPopup(rootPane, "popup_mining_wallet_confirm.fxml", zIndex+1);
                controller.setModel(walletItemModel);
                controller.getPasswordFieldController().requestFocus();
                break;
            }
        }

        list.getChildren().remove(miningPane);

        if(list.getChildren().size() == 0) {
            exit();
        }
    }

    public void setData(String masterNodeAlias, String masterNodeAddress, String miningAlias, String miningAddress){
        if(masterNodeAddress != null && masterNodeAddress.length() > 0){
            BigInteger balance = BigInteger.ZERO;
            for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++){
                if(AppManager.getInstance().getKeystoreExpList().get(i).address.equals(masterNodeAddress)){
                    balance = AppManager.getInstance().getKeystoreExpList().get(i).balance;
                    break;
                }
            }
            masterNodeIcon.setImage(IdenticonGenerator.createIcon(masterNodeAddress));
            this.masterNodeAlias.setText(masterNodeAlias);
            this.masterNodeAddress.setText(masterNodeAddress);
        }else{
            list.getChildren().remove(masterNodePane);
        }

        if(miningAddress != null && miningAddress.length() > 0){
            miningIcon.setImage(IdenticonGenerator.createIcon(miningAddress));
            this.miningAlias.setText(miningAlias);
            this.miningAddress.setText(miningAddress);
        }else{
            list.getChildren().remove(miningPane);
        }
    }
}
