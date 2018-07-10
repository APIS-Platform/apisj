package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {

    private static final int LOAD_WALLET_SELECT_WALLET_FILE = 1;
    private static final int LOAD_WALLET_PRIVATE_KEY = 2;

    private int loadWalletPhaseTwoFlag = LOAD_WALLET_SELECT_WALLET_FILE;

    @FXML
    private ImageView hexagonCreateWalletBtn, hexagonLoadWalletBtn, hexagonCreateWalletLabelImg, hexagonLoadWalletLabelImg;
    @FXML
    private ImageView introHomeBtn, introNaviOne, introNaviTwo, introNaviThree, introNaviFour;
    @FXML
    private ImageView createWalletNameCheckIcon, createWalletPasswordCheckIcon, createWalletConfirmCheckIcon, createWalletPasswordCover, createWalletConfirmCover, createWalletPkCover;
    @FXML
    private ImageView loadWalletPhaseTwoRadioOneImg, loadWalletPhaseTwoRadioTwoImg;
    @FXML
    private Label hexagonCreateWalletLabel, hexagonLoadWalletLabel, createWalletNameWarnLabel, introNoFour, loadWalletPhaseTwoIntroNoFour;
    @FXML
    private GridPane introPhaseOne, introCreateWalletPhaseTwo, introCreateWalletPhaseThree, introCreateWalletPhaseFour, createWalletNameWarn, introModalBackground;
    @FXML
    private GridPane introLoadWalletPhaseTwo;
    @FXML
    private TabPane introPhaseTab;
    @FXML
    private AnchorPane downloadKeystoreSuccess, downloadKeystoreCaution;
    @FXML
    private TextField createWalletPasswordTextField, createWalletConfirmTextField, createWalletNameTextField, createWalletPkTextField;
    @FXML
    private PasswordField createWalletPasswordHiddenField, createWalletConfirmHiddenField, createWalletPkHiddenField;
    @FXML
    private RadioButton radioBtn;

    private Image createBtnImgOn, createBtnImgOff, loadBtnImgOn, loadBtnImgOff;
    private Image createLabelOn, createLabelOff, loadLabelOn, loadLabelOff;
    private Image introNavi, introNaviCircle;
    private Image passwordPublic, passwordPrivate, checkIconNormal, checkIconSuccess, checkIconFail;
    private Image radioCheckBtnRed, radioCheckBtnGrey;

    @FXML
    private ApisTextFieldPkController apisTextFieldPkController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("introController check.");

        // Hide Home Button when the first access
        this.introHomeBtn.setFitWidth(1);

        // initial Image Setting
        createBtnImgOn = new Image("image/btn_create_wallet@2x.png");
        createBtnImgOff = new Image("image/btn_create_wallet_none@2x.png");
        loadBtnImgOn = new Image("image/btn_load_wallet@2x.png");
        loadBtnImgOff = new Image("image/btn_load_wallet_none@2x.png");
        createLabelOn = new Image("image/ic_plus_white@2x.png");
        createLabelOff = new Image("image/ic_plus_white_none@2x.png");
        loadLabelOn = new Image("image/ic_down_arrow_white@2x.png");
        loadLabelOff = new Image("image/ic_down_arrow_white_none@2x.png");
        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");
        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");
        checkIconNormal = new Image("image/ic_circle_cross_grey@2x.png");
        checkIconSuccess = new Image("image/ic_check@2x.png");
        checkIconFail = new Image("image/ic_circle_cross_red@2x.png");
        radioCheckBtnRed = new Image("image/btn_check_red@2x.png");
        radioCheckBtnGrey = new Image("image/btn_check_grey@2x.png");

        createWalletNameTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue)
                {
                    //INPUT_STATUS_TYPE_FOCUS, null
                    //setInputStatus(INPUT_STATUS_TYPE_FOCUS, createWalletNameTextField, null,
//                            createWalletNameCheckIcon, createWalletNameWarn, createWalletNameWarnLabel, null);
                }
                else
                {
                    System.out.println("Textfield out focus");
                }
            }
        });
    }

    public void hexagonCreateWalletBtnMouseEntered() {
        this.hexagonCreateWalletBtn.setImage(createBtnImgOn);
        this.hexagonCreateWalletLabelImg.setImage(createLabelOn);
        this.hexagonCreateWalletLabel.setOpacity(1);
        this.hexagonLoadWalletBtn.setImage(loadBtnImgOff);
        this.hexagonLoadWalletLabelImg.setImage(loadLabelOff);
        this.hexagonLoadWalletLabel.setOpacity(0.3);
        this.introNoFour.setVisible(true);
        this.introNaviFour.setVisible(true);
    }

    public void hexagonLoadWalletBtnMouseEntered() {
        this.hexagonLoadWalletBtn.setImage(loadBtnImgOn);
        this.hexagonLoadWalletLabelImg.setImage(loadLabelOn);
        this.hexagonLoadWalletLabel.setOpacity(1);
        this.hexagonCreateWalletBtn.setImage(createBtnImgOff);
        this.hexagonCreateWalletLabelImg.setImage(createLabelOff);
        this.hexagonCreateWalletLabel.setOpacity(0.3);
        this.introNoFour.setVisible(false);
        this.introNaviFour.setVisible(false);
    }

    // Create Wallet Phases
    public void createWalletBtnClick() {
        // Initialize phase two values
        createWalletPasswordCover.setImage(passwordPrivate);
        createWalletConfirmCover.setImage(passwordPrivate);
        this.createWalletPasswordHiddenField.setVisible(true);
        this.createWalletPasswordTextField.setVisible(false);
        this.createWalletConfirmHiddenField.setVisible(true);
        this.createWalletConfirmTextField.setVisible(false);
        this.createWalletPasswordHiddenField.setText("");
        this.createWalletConfirmHiddenField.setText("");
        this.createWalletNameCheckIcon.setVisible(false);
        this.createWalletPasswordCheckIcon.setVisible(false);
        this.createWalletConfirmCheckIcon.setVisible(false);

        this.introPhaseOne.setVisible(false);
        this.introCreateWalletPhaseTwo.setVisible(true);
        this.introNaviOne.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviOne.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(1);
    }

    public void createWalletPhaseTwoBackClick() {
        this.introCreateWalletPhaseTwo.setVisible(false);
        this.introPhaseOne.setVisible(true);
        this.introNaviTwo.setImage(introNaviCircle);
        this.introNaviOne.setImage(introNavi);
        this.introNaviTwo.setFitWidth(6);
        this.introNaviOne.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(0);
    }

    public void createWalletPhaseTwoNextClick() {
        this.introCreateWalletPhaseTwo.setVisible(false);
        this.introCreateWalletPhaseThree.setVisible(true);
        this.introNaviTwo.setImage(introNaviCircle);
        this.introNaviThree.setImage(introNavi);
        this.introNaviTwo.setFitWidth(6);
        this.introNaviThree.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(2);
    }

    public void createWalletPhaseThreeBackClick() {
        this.introCreateWalletPhaseThree.setVisible(false);
        this.introCreateWalletPhaseTwo.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(1);
    }

    public void createWalletPhaseThreeNextClick() {
        this.introModalBackground.setVisible(true);
        this.downloadKeystoreCaution.setVisible(true);
    }

    public void downloadKeystoreCautionYes() {
        this.downloadKeystoreCaution.setVisible(false);
        this.introModalBackground.setVisible(false);
        this.introCreateWalletPhaseThree.setVisible(false);
        this.introCreateWalletPhaseFour.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviFour.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviFour.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(3);

        this.apisTextFieldPkController.init();
    }

    public void createWalletPhaseFourBackClick() {
        this.introCreateWalletPhaseFour.setVisible(false);
        this.introCreateWalletPhaseThree.setVisible(true);
        this.introNaviFour.setImage(introNaviCircle);
        this.introNaviThree.setImage(introNavi);
        this.introNaviFour.setFitWidth(6);
        this.introNaviThree.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(2);
    }

    public void createWalletPhaseFourNextClick() {
        //complete create
        this.introCreateWalletPhaseFour.setVisible(false);
        this.introPhaseOne.setVisible(true);
        this.introNaviFour.setImage(introNaviCircle);
        this.introNaviOne.setImage(introNavi);
        this.introNaviFour.setFitWidth(6);
        this.introNaviOne.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(0);
    }


    @FXML
    public void togglePasswordFieldClick(InputEvent event) {
        String fxid = ((ImageView)event.getSource()).getId();

        if(fxid.equals("createWalletPasswordCover")){
            togglePasswordField(this.createWalletPasswordTextField, this.createWalletPasswordHiddenField);

            if(this.createWalletPasswordHiddenField.isVisible()) {
                this.createWalletPasswordCover.setImage(passwordPrivate);
            } else {
                this.createWalletPasswordCover.setImage(passwordPublic);
            }
        }else if(fxid.equals("createWalletConfirmCover")){
            togglePasswordField(this.createWalletConfirmTextField, this.createWalletConfirmHiddenField);

            if(this.createWalletConfirmHiddenField.isVisible()) {
                this.createWalletConfirmCover.setImage(passwordPrivate);
            } else {
                this.createWalletConfirmCover.setImage(passwordPublic);
            }
        } else if(fxid.equals("createWalletPkCOver")) {
            togglePasswordField(this.createWalletPkTextField, this.createWalletPkHiddenField);

            if(this.createWalletPkHiddenField.isVisible()) {
                this.createWalletPkCover.setImage(passwordPrivate);
            } else {
                this.createWalletPkCover.setImage(passwordPublic);
            }
        }
    }

    public void togglePasswordField(TextField textField, PasswordField passwordField){
        if(textField.isVisible()){
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            textField.setVisible(false);
        }else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
        }
    }

    public static final int INPUT_STATUS_TYPE_NORMAL = 0;
    public static final int INPUT_STATUS_TYPE_SUCCESS = 1;
    public static final int INPUT_STATUS_TYPE_FAIL = 2;
    public static final int INPUT_STATUS_TYPE_FOCUS = 3;

    public void setInputStatus(int type, TextField textField, PasswordField passwordField, ImageView checkIcon, GridPane hintPane, Label hintLabel, String hintText) {

        switch(type) {
            case INPUT_STATUS_TYPE_NORMAL :
                break;
            case INPUT_STATUS_TYPE_SUCCESS :
                break;
            case INPUT_STATUS_TYPE_FAIL :
                break;
            case INPUT_STATUS_TYPE_FOCUS :
                String style = textField.getStyle();
                style = style + "-fx-border-color: #36b25b; ";
                style = style + "-fx-text-fill: #2b2b2b; ";
                textField.setStyle(style);
                checkIcon.setImage(checkIconNormal);
                checkIcon.setVisible(true);
                break;
        }
    }

    public void createWalletDownloadKeystoreFile() {
        this.introModalBackground.setVisible(true);
        this.downloadKeystoreSuccess.setVisible(true);
    }

    public void downloadKeystoreConfirm() {
        this.downloadKeystoreSuccess.setVisible(false);
        this.introModalBackground.setVisible(false);
    }

    public void downloadKeystoreCautionNo() {
        this.downloadKeystoreCaution.setVisible(false);
        this.introModalBackground.setVisible(false);
    }

    public void loadWalletBtnClick() {
        this.introPhaseOne.setVisible(false);
        this.introLoadWalletPhaseTwo.setVisible(true);
        this.introNaviOne.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviOne.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(4);
        loadWalletPhaseTwoRadioOne();
    }

    public void loadWalletPhaseTwoBackClick() {
        this.introLoadWalletPhaseTwo.setVisible(false);
        this.introPhaseOne.setVisible(true);
        this.introNaviTwo.setImage(introNaviCircle);
        this.introNaviOne.setImage(introNavi);
        this.introNaviTwo.setFitWidth(6);
        this.introNaviOne.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(0);
        loadWalletPhaseTwoRadioOne();
    }

    public void loadWalletPhaseTwoNextClick() {
        if(loadWalletPhaseTwoFlag == LOAD_WALLET_SELECT_WALLET_FILE) {

        } else if(loadWalletPhaseTwoFlag == LOAD_WALLET_PRIVATE_KEY) {

        }
    }

    public void loadWalletPhaseTwoRadioOne() {
        this.loadWalletPhaseTwoIntroNoFour.setVisible(false);
        this.introNaviFour.setVisible(false);
        this.loadWalletPhaseTwoRadioOneImg.setImage(radioCheckBtnRed);
        this.loadWalletPhaseTwoRadioTwoImg.setImage(radioCheckBtnGrey);
        loadWalletPhaseTwoFlag = LOAD_WALLET_SELECT_WALLET_FILE;
    }

    public void loadWalletPhaseTwoRadioTwo() {
        this.loadWalletPhaseTwoIntroNoFour.setVisible(true);
        this.introNaviFour.setVisible(true);
        this.loadWalletPhaseTwoRadioOneImg.setImage(radioCheckBtnGrey);
        this.loadWalletPhaseTwoRadioTwoImg.setImage(radioCheckBtnRed);
        loadWalletPhaseTwoFlag = LOAD_WALLET_PRIVATE_KEY;
    }



}