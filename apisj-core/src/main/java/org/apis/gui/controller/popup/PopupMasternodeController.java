package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Ellipse;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.controller.module.textfield.ApisAddressFieldController;
import org.apis.gui.controller.module.textfield.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.textfield.ApisTextFieldGroup;
import org.apis.gui.manager.*;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.keystore.KeyStoreManager;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PopupMasternodeController extends BasePopupController {
    private WalletItemModel itemModel;

    @FXML private ApisSelectBoxController recipientController;
    @FXML private ApisTextFieldController passwordController;
    @FXML private ApisAddressFieldController recipientFieldController;
    @FXML private GridPane cancelChangeGrid;
    @FXML private AnchorPane rootPane, recipientInput, recipientSelect;
    @FXML private Label address, recipientInputBtn, startBtn, cancelRqBtn, cancelMnBtn, changeRecipientBtn;
    @FXML private ImageView addrIdentImg;
    @FXML private Label title, walletAddrLabel, passwordLabel, recipientLabel, recipientDesc1, recipientDesc2;

    private Image greyCircleAddrImg;
    private boolean isMyAddressSelected = true;
    private int btnFlag = 0;

    private ApisTextFieldGroup apisTextFieldGroup = new ApisTextFieldGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Image Setting
        greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");

        // Making indent image circular
        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);
        Ellipse ellipse1 = new Ellipse(12, 12);
        ellipse1.setCenterX(12);
        ellipse1.setCenterY(12);

        addrIdentImg.setClip(ellipse);

        btnSetting();

        passwordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            // Focus Out Event
            @Override
            public void onFocusOut() {
                String password = passwordController.getText();

                if(password == null || password.equals("")) {
                    passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else {
                    passwordController.succeededForm();
                }
            }

            // TextProperty Change Event
            @Override
            public void change(String old_text, String new_text) { }

            @Override
            public void onAction() {
                startMasternode(btnFlag);
            }

            @Override
            public void onKeyTab(){
                passwordController.requestFocus();
            }
        });


        recipientController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, true);
        recipientController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {

            }@Override
            public void onMouseClick() {

            }
        });

        apisTextFieldGroup.add(passwordController);
        showStartBtn();
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.masternodeTitle);
        walletAddrLabel.textProperty().bind(StringManager.getInstance().popup.masternodeWalletAddrLabel);
        passwordLabel.textProperty().bind(StringManager.getInstance().popup.masternodePasswordLabel);
        recipientLabel.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientLabel);
        recipientInputBtn.textProperty().bind(StringManager.getInstance().common.directInputButton);
        recipientDesc1.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientDesc1);
        recipientDesc2.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientDesc2);
        startBtn.textProperty().bind(StringManager.getInstance().popup.masternodeStartMasternode);
    }

    private void btnSetting() {
        String propState = AppManager.getGeneralPropertiesData("masternode_status");

        if(propState.equals(Integer.toString(AppManager.MnState.EMPTY_MASTERNODE.num))) {
            showStartBtn();
        } else if(propState.equals(Integer.toString(AppManager.MnState.REQUEST_MASTERNODE.num))) {
            showCancelRqBtn();
        } else if(propState.equals(Integer.toString(AppManager.MnState.MASTERNODE.num))) {
            showCancelChangeGrid();
        }
    }

    private void showStartBtn() {
        startBtn.setVisible(true);
        cancelRqBtn.setVisible(false);
        cancelChangeGrid.setVisible(false);
    }

    private void showCancelRqBtn() {
        startBtn.setVisible(false);
        cancelRqBtn.setVisible(true);
        cancelChangeGrid.setVisible(false);
    }

    private void showCancelChangeGrid() {
        startBtn.setVisible(false);
        cancelRqBtn.setVisible(false);
        cancelChangeGrid.setVisible(true);
    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (WalletItemModel)model;

        address.textProperty().setValue(this.itemModel.getAddress());
        addrIdentImg.setImage(ImageManager.getIdenticons(this.itemModel.getAddress()));

    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("recipientInputBtn")) {
            if(isMyAddressSelected) {
                StyleManager.backgroundColorStyle(recipientInputBtn, StyleManager.AColor.C000000);
                StyleManager.borderColorStyle(recipientInputBtn, StyleManager.AColor.C000000);
                StyleManager.fontColorStyle(recipientInputBtn, StyleManager.AColor.Cffffff);
                recipientFieldController.setText("");
                recipientSelect.setVisible(false);
                recipientInput.setVisible(true);

                StyleManager.backgroundColorStyle(startBtn, StyleManager.AColor.Cd8d8d8);
                startBtn.setDisable(true);
            } else {

                StyleManager.backgroundColorStyle(recipientInputBtn, StyleManager.AColor.Cf8f8fb);
                StyleManager.borderColorStyle(recipientInputBtn, StyleManager.AColor.C999999);
                StyleManager.fontColorStyle(recipientInputBtn, StyleManager.AColor.C999999);
                recipientSelect.setVisible(true);
                recipientInput.setVisible(false);

                StyleManager.backgroundColorStyle(startBtn, StyleManager.AColor.Cb01e1e);
                startBtn.setDisable(false);
            }

            isMyAddressSelected = !isMyAddressSelected;
        } else if(fxid.equals("startBtn")) {
            startMasternode(0);

        } else if(fxid.equals("cancelRqBtn")) {
            startMasternode(1);

        } else if(fxid.equals("cancelMnBtn")) {
            startMasternode(2);

        } else if(fxid.equals("changeRecipientBtn")) {
            startMasternode(3);
        }
    }

    public void startMasternode(int btnFlag){
        this.btnFlag = btnFlag;

        if (passwordController.getCheckBtnEnteredFlag()) {
            passwordController.setText("");
        }

        String keystoreJsonData = itemModel.getKeystoreJsonData();
        String password =  passwordController.getText();
        byte[] recipientAddr = Hex.decode(recipientController.getAddress());
        // 직접 입력한 경우
        if(!isMyAddressSelected){
            recipientAddr = Hex.decode(recipientFieldController.getAddress());
        }

        passwordController.succeededForm();
        if (password == null || password.equals("")) {
            passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
        } else if(!KeyStoreManager.matchPassword(itemModel.getKeystoreJsonData(),  passwordController.getText().trim().toCharArray())){
            passwordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
        } else if(ByteUtil.toHexString(recipientAddr).length() != 40){
            return;
        } else{
            if(btnFlag == 2) {
                AppManager.saveGeneralProperties("masternode_state", Integer.toString(AppManager.MnState.CANCEL_MASTERNODE.num));

            } else if(AppManager.getInstance().ethereumMasternode(keystoreJsonData, password, recipientAddr)){
                passwordController.succeededForm();
                PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml",zIndex + 1);

                switch(btnFlag) {
                    case 0 : AppManager.saveGeneralProperties("masternode_state", Integer.toString(AppManager.MnState.REQUEST_MASTERNODE.num)); break;
                    case 1 : AppManager.saveGeneralProperties("masternode_state", Integer.toString(AppManager.MnState.EMPTY_MASTERNODE.num)); break;
                    case 3 : AppManager.saveGeneralProperties("masternode_state", Integer.toString(AppManager.MnState.REQUEST_MASTERNODE.num)); break;
                    default : break;
                }

                AppManager.getInstance().guiFx.getWallet().updateTableList();
            }
        }
    }

    public ApisTextFieldController getPasswordController() {
        return this.passwordController;
    }
}
