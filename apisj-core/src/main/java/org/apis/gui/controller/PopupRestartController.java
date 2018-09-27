package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.keystore.KeyStoreDataExp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupRestartController extends BasePopupController {

    @FXML private Label title, subTitle, restartAddressLabel, masterNodeAlias, miningAlias, masterNodeAddress, miningAddress, masterNodeRestartBtn, miningRestartBtn;
    @FXML private ImageView masterNodeIcon, miningIcon;
    @FXML private AnchorPane masterNodePane, miningPane;
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
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("id : "+id);
        if("masterNodeRestartBtn".equals(id)){

            for(int i = 0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
                KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

                if(masterNodeAddress.getText().equals(dataExp.address)){
                    WalletItemModel walletItemModel = new WalletItemModel();
                    walletItemModel.setId(dataExp.id);
                    walletItemModel.setAlias(dataExp.alias);
                    walletItemModel.setAddress(dataExp.address);
                    walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
                    PopupMasternodeController controller = (PopupMasternodeController) PopupManager.getInstance().showMainPopup("popup_masternode.fxml", zIndex+1);
                    controller.setModel(walletItemModel);
                    break;
                }
            }

            list.getChildren().remove(masterNodePane);
        }else if("miningRestartBtn".equals(id)){
            for(int i = 0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
                KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

                if(miningAddress.getText().equals(dataExp.address)){
                    WalletItemModel walletItemModel = new WalletItemModel();
                    walletItemModel.setId(dataExp.id);
                    walletItemModel.setAlias(dataExp.alias);
                    walletItemModel.setAddress(dataExp.address);
                    walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
                    PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController) PopupManager.getInstance().showMainPopup("popup_mining_wallet_confirm.fxml", zIndex+1);
                    controller.setModel(walletItemModel);
                    break;
                }
            }

            list.getChildren().remove(miningPane);
        }

        if(list.getChildren().size() == 0){
            exit();
        }
    }

    public void setData(String masterNodeAlias, String masterNodeAddress, String miningAlias, String miningAddress){
        if(masterNodeAddress != null && masterNodeAddress.length() > 0){
            try {
                masterNodeIcon.setImage(IdenticonGenerator.generateIdenticonsToImage(masterNodeAddress, 128, 128));
                this.masterNodeAlias.setText(masterNodeAlias);
                this.masterNodeAddress.setText(masterNodeAddress);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            list.getChildren().remove(masterNodePane);
        }

        if(miningAddress != null && miningAddress.length() > 0){
            try {
                miningIcon.setImage(IdenticonGenerator.generateIdenticonsToImage(miningAddress, 128, 128));
                this.miningAlias.setText(miningAlias);
                this.miningAddress.setText(miningAddress);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            list.getChildren().remove(miningPane);
        }
    }
}
