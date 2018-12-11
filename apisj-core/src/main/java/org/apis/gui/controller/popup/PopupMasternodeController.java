package org.apis.gui.controller.popup;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.ApisTextFieldGroup;
import org.apis.gui.manager.*;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.keystore.KeyStoreManager;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PopupMasternodeController extends BasePopupController {
    private WalletItemModel itemModel;

    @FXML private ApisSelectBoxController recipientController;
    @FXML private ApisTextFieldController passwordController, knowledgeKeyController;
    @FXML private AnchorPane rootPane, recipientInput, recipientSelect;
    @FXML private Label address, recipientInputBtn, startBtn;
    @FXML private ImageView addrIdentImg, recipientAddrImg;
    @FXML private TextField recipientTextField;
    @FXML private Label title, walletAddrLabel, passwordLabel, knowledgeKeyLabel, recipientLabel, recipientDesc1, recipientDesc2;

    private Image greyCircleAddrImg;
    private boolean isMyAddressSelected = true;

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
        recipientAddrImg.setClip(ellipse1);

        AppManager.settingTextFieldLineStyle(recipientTextField);
        recipientTextField.textProperty().addListener(recipientKeyListener);

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
                knowledgeKeyController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                knowledgeKeyController.requestFocus();
            }
        });

        knowledgeKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.knowledgeKeyPlaceholder.get());
        knowledgeKeyController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String knowledgeKey = knowledgeKeyController.getText();

                if(knowledgeKey == null || knowledgeKey.equals("")) {
                    knowledgeKeyController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else {
                    knowledgeKeyController.succeededForm();
                }
            }

            @Override
            public void change(String old_text, String new_text) { }

            @Override
            public void onAction() {
                startMasternode();
            }

            @Override
            public void onKeyTab() {
                passwordController.requestFocus();
            }
        });


        recipientController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        recipientController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {

            }@Override
            public void onMouseClick() {

            }
        });

        apisTextFieldGroup.add(passwordController);
        apisTextFieldGroup.add(knowledgeKeyController);
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.masternodeTitle);
        walletAddrLabel.textProperty().bind(StringManager.getInstance().popup.masternodeWalletAddrLabel);
        passwordLabel.textProperty().bind(StringManager.getInstance().popup.masternodePasswordLabel);
        knowledgeKeyLabel.textProperty().bind(StringManager.getInstance().popup.masternodeKnowledgeKeyLabel);
        recipientLabel.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientLabel);
        recipientInputBtn.textProperty().bind(StringManager.getInstance().common.directInputButton);
        recipientTextField.promptTextProperty().bind(StringManager.getInstance().popup.masternodeRecipientPlaceholder);
        recipientDesc1.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientDesc1);
        recipientDesc2.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientDesc2);
        startBtn.textProperty().bind(StringManager.getInstance().popup.masternodeStartMasternode);
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
                recipientTextField.setText("");
                recipientAddrImg.setImage(greyCircleAddrImg);
                recipientSelect.setVisible(false);
                recipientInput.setVisible(true);

                StyleManager.backgroundColorStyle(startBtn, StyleManager.AColor.Cd8d8d8);
                startBtn.setDisable(true);
            } else {

                StyleManager.backgroundColorStyle(recipientInputBtn, StyleManager.AColor.Cf2f2f2);
                StyleManager.borderColorStyle(recipientInputBtn, StyleManager.AColor.C999999);
                StyleManager.fontColorStyle(recipientInputBtn, StyleManager.AColor.C999999);
                recipientSelect.setVisible(true);
                recipientInput.setVisible(false);

                StyleManager.backgroundColorStyle(startBtn, StyleManager.AColor.C910000);
                startBtn.setDisable(false);
            }

            isMyAddressSelected = !isMyAddressSelected;
        } else if(fxid.equals("startBtn")) {
            startMasternode();
        }
    }

    private ChangeListener<String> recipientKeyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            String address = recipientTextField.getText();

            if(address.indexOf("@") >= 0){
                address = AppManager.getInstance().getAddressWithMask(address);
            }

            int maxlangth = 40;
            if(address == null || address.length() < maxlangth) {
                recipientAddrImg.setImage(greyCircleAddrImg);

                StyleManager.backgroundColorStyle(startBtn, StyleManager.AColor.Cd8d8d8);
                startBtn.setDisable(true);
            } else {
                Image image = IdenticonGenerator.createIcon(address);
                if(image != null){
                    recipientAddrImg.setImage(image);
                }

                StyleManager.backgroundColorStyle(startBtn, StyleManager.AColor.C910000);
                startBtn.setDisable(false);
            }
        }
    };

    public void startMasternode(){
        if (passwordController.getCheckBtnEnteredFlag()) {
            passwordController.setText("");
        }

        String keystoreJsonData = itemModel.getKeystoreJsonData();
        String password =  passwordController.getText();
        String knowledge = knowledgeKeyController.getText();
        byte[] proofKey = AppManager.getInstance().getProofKey(Hex.decode(itemModel.getAddress()));
        byte[] knowledgeKey = AppManager.getInstance().getKnowledgeKey(knowledgeKeyController.getText().trim());
        byte[] recipientAddr = Hex.decode(recipientController.getAddress());
        // 직접 입력한 경우
        if(!isMyAddressSelected){
            recipientAddr = Hex.decode(recipientTextField.getText().trim());
        }

        passwordController.succeededForm();
        knowledgeKeyController.succeededForm();
        if (password == null || password.equals("")) {
            passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
        } else if(!KeyStoreManager.matchPassword(itemModel.getKeystoreJsonData(),  passwordController.getText().trim().toCharArray())){
            passwordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
        } else if (knowledge == null || knowledge.equals("")) {
            knowledgeKeyController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
        } else if (!Arrays.equals(proofKey, knowledgeKey)) {
            knowledgeKeyController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
        } else if(ByteUtil.toHexString(recipientAddr).length() != 40){
            return;
        } else{

            if(AppManager.getInstance().ethereumMasternode(keystoreJsonData, password, recipientAddr)){

                passwordController.succeededForm();
                PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml",zIndex + 1);

                AppManager.getInstance().guiFx.getWallet().updateTableList();
            }
        }
    }

    public ApisTextFieldController getPasswordController() {
        return this.passwordController;
    }
}
