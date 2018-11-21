package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMiningWalletConfirmController extends BasePopupController {
    public static final int MINING_TYPE_START = 0;
    public static final int MINING_TYPE_STOP = 1;
    private int miningType = MINING_TYPE_START;

    private WalletItemModel itemModel;

    @FXML private AnchorPane rootPane;
    @FXML private Label title, subTitle, passwordLabel, addressLabel, address, startBtn;
    @FXML private ImageView addressIcon;
    @FXML private ApisTextFieldController passwordFieldController;

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        toggleMining();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
        passwordFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {
                if(new_text == null || new_text.length() == 0){
                    failedForm();
                }else{
                    succeededForm();
                }
            }

            @Override
            public void onAction() {
                toggleMining();
            }

            @Override
            public void onKeyTab(){

            }
        });

        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.addressIcon.getFitWidth()-0.5, this.addressIcon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        addressIcon.setClip(clip);
    }
    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.miningWalletConfirmTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.miningWalletConfirmSubTitle);
        addressLabel.textProperty().bind(StringManager.getInstance().popup.miningWaleltConfirmAddress);
        passwordLabel.textProperty().bind(StringManager.getInstance().popup.miningWalletConfirmPassword);
        startBtn.textProperty().bind(StringManager.getInstance().popup.miningWalletConfirmStart);
    }

    private void toggleMining(){
        if(miningType == MINING_TYPE_START) {
            if (AppManager.getInstance().startMining(this.itemModel.getId(), passwordFieldController.getText())) {
                AppManager.getInstance().setMiningWalletId(this.itemModel.getId());
                PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml", zIndex);
                AppManager.getInstance().guiFx.getWallet().updateTableList();
            } else {
                passwordFieldController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
            }
        }else {
            AppManager.getInstance().stopMining();
            PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml", zIndex);
            AppManager.getInstance().guiFx.getWallet().updateTableList();
        }
    }

    public void setType(int miningType){
        this.miningType = miningType;
        if(miningType == MINING_TYPE_START){
            startBtn.textProperty().unbind();
            startBtn.textProperty().bind(StringManager.getInstance().popup.miningWalletConfirmStart);
        }else if(miningType == MINING_TYPE_STOP){
            startBtn.textProperty().unbind();
            startBtn.textProperty().bind(StringManager.getInstance().popup.miningWalletConfirmStop);
        }
    }

    public void failedForm(){
        startBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
    }

    public void succeededForm(){
        startBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (WalletItemModel)model;
        address.textProperty().setValue(this.itemModel.getAddress());
        addressIcon.setImage(ImageManager.getIdenticons(this.itemModel.getAddress()));
    }

    public ApisTextFieldController getPasswordFieldController() {
        return this.passwordFieldController;
    }
}
