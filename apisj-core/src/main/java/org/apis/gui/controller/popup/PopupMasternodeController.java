package org.apis.gui.controller.popup;

import com.google.zxing.WriterException;
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
import org.apis.gui.manager.*;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupMasternodeController extends BasePopupController {
    private WalletItemModel itemModel;

    @FXML private ApisSelectBoxController recipientController;
    @FXML private ApisTextFieldController passwordController;
    @FXML private AnchorPane recipientInput, recipientSelect;
    @FXML private Label address, recipientInputBtn, startBtn;
    @FXML private ImageView addrIdentImg, recipientAddrImg;
    @FXML private TextField recipientTextField;
    @FXML private Label title, walletAddrLabel, passwordLabel, recipientLabel, recipientDesc1, recipientDesc2;

    private Image greyCircleAddrImg;
    private String text;
    private boolean isMyAddressSelected = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Image Setting
        greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");
        text = passwordController.getText();

        // Making indent image circular
        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);
        Ellipse ellipse1 = new Ellipse(12, 12);
        ellipse1.setCenterX(12);
        ellipse1.setCenterY(12);

        addrIdentImg.setClip(ellipse);
        recipientAddrImg.setClip(ellipse1);

        recipientTextField.focusedProperty().addListener(recipientFocusListener);
        recipientTextField.textProperty().addListener(recipientKeyListener);

        passwordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            // Focus Out Event
            @Override
            public void onFocusOut() {
                if (passwordController.getCheckBtnEnteredFlag()) {
                    passwordController.setText("");
                }

                text = passwordController.getText();

                if (text == null || text.equals("")) {
                    passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                    failedForm();
                } else if(!KeyStoreManager.getInstance().matchPassword(itemModel.getKeystoreJsonData(),  passwordController.getText())){
                    passwordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                    failedForm();
                } else{
                    passwordController.succeededForm();
                    succeededForm();
                }
            }

            // TextProperty Change Event
            @Override
            public void change(String old_text, String new_text) {
                text = passwordController.getText();

                if(text == null || text.length() == 0) {
                    failedForm();
                } else {
                    succeededForm();
                }
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
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.masternodeTitle);
        walletAddrLabel.textProperty().bind(StringManager.getInstance().popup.masternodeWalletAddrLabel);
        passwordLabel.textProperty().bind(StringManager.getInstance().popup.masternodePasswordLabel);
        recipientLabel.textProperty().bind(StringManager.getInstance().popup.masternodeRecipientLabel);
        recipientInputBtn.textProperty().bind(StringManager.getInstance().popup.masternodeDirectInput);
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
                recipientInputBtn.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #000000; -fx-text-fill: #ffffff; -fx-background-color: #000000;");
                recipientTextField.setText("");
                recipientAddrImg.setImage(greyCircleAddrImg);
                recipientSelect.setVisible(false);
                recipientInput.setVisible(true);
            } else {
                recipientInputBtn.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #999999; -fx-text-fill: #999999; -fx-background-color: #f2f2f2;");
                recipientSelect.setVisible(true);
                recipientInput.setVisible(false);
            }

            isMyAddressSelected = !isMyAddressSelected;
        } else if(fxid.equals("startBtn")) {
            if (passwordController.getCheckBtnEnteredFlag()) {
                passwordController.setText("");
            }

            text = passwordController.getText();

            if (text == null || text.equals("")) {
                passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                failedForm();
            } else if(!KeyStoreManager.getInstance().matchPassword(itemModel.getKeystoreJsonData(),  passwordController.getText())){
                passwordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                failedForm();
            } else{
                String keystoreJsonData = itemModel.getKeystoreJsonData();
                String password = this.passwordController.getText();
                byte[] recipientAddr = Hex.decode(recipientController.getAddress());

                // 직접 입력한 경우
                if(!isMyAddressSelected){
                    recipientAddr = Hex.decode(recipientTextField.getText().trim());
                }

                if(AppManager.getInstance().ethereumMasternode(keystoreJsonData, password, recipientAddr)){

                    AppManager.getInstance().setMasterNodeWalletId(itemModel.getId());
                    // 파일로 저장
                    AppManager.saveGeneralProperties("masternode_address", itemModel.getAddress());

                    passwordController.succeededForm();
                    succeededForm();
                    PopupManager.getInstance().showMainPopup("popup_success.fxml",zIndex+1);

                    AppManager.getInstance().guiFx.getWallet().updateWalletList();
                }
            }
        }
    }

    private ChangeListener<Boolean> recipientFocusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue) {
                recipientTextField.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px;  -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #999999; -fx-background-color: #ffffff;");
            } else {
                recipientTextField.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px;  -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #d8d8d8; -fx-background-color: #f2f2f2;");
            }
        }
    };

    private ChangeListener<String> recipientKeyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!recipientTextField.getText().matches("[0-9a-fA-F]*")) {
                recipientTextField.setText(recipientTextField.getText().replaceAll("[^0-9a-fA-F]", ""));
            }

            int maxlangth = 40;
            if(recipientTextField.getText().trim().length() > maxlangth){
                recipientTextField.setText(recipientTextField.getText().trim().substring(0, maxlangth));
            }

            if(recipientTextField.getText() == null || recipientTextField.getText().trim().length() < maxlangth) {
                recipientAddrImg.setImage(greyCircleAddrImg);
            } else {
                try {
                    Image image = IdenticonGenerator.generateIdenticonsToImage(recipientTextField.getText().trim(), 128, 128);
                    if(image != null){
                        recipientAddrImg.setImage(image);
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void failedForm(){
        startBtn.setCursor(Cursor.HAND);
        startBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8; " +
                "-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:14px;");
    }

    public void succeededForm(){
        startBtn.setCursor(Cursor.HAND);
        startBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000; " +
                "-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:14px;");
    }

}
