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
    private ImageView loadWalletPhaseTwoRadioOneImg, loadWalletPhaseTwoRadioTwoImg;
    @FXML
    private Label hexagonCreateWalletLabel, hexagonLoadWalletLabel, createWalletNameWarnLabel, introNoFour, loadWalletPhaseTwoIntroNoFour;
    @FXML
    private GridPane introPhaseOne, introCreateWalletPhaseTwo, introCreateWalletPhaseThree, introCreateWalletPhaseFour, createWalletNameWarn, introModalBackground;
    @FXML
    private GridPane introLoadWalletPhaseTwo, introLoadWalletPhaseThreeTypeFile, introLoadWalletPhaseThreeTypePk, introLoadWalletPhaseFourTypePk;
    @FXML
    private TabPane introPhaseTab;
    @FXML
    private AnchorPane downloadKeystoreSuccess, downloadKeystoreCaution;
    @FXML
    private RadioButton radioBtn;

    private Image createBtnImgOn, createBtnImgOff, loadBtnImgOn, loadBtnImgOff;
    private Image createLabelOn, createLabelOff, loadLabelOn, loadLabelOff;
    private Image introNavi, introNaviCircle;
    private Image passwordPublic, passwordPrivate, checkIconNormal, checkIconSuccess, checkIconFail;
    private Image radioCheckBtnRed, radioCheckBtnGrey;

    @FXML
    private ApisTextFieldPkController apisTextFieldPkController;

    @FXML
    public ApisTextFieldController loadWalletPrivateKeyController, createWalletPhaseTwoWalletNameController, createWalletPhaseTwoWalletPasswordController,
                                    createWalletPhaseTwoConfirmPasswordController;

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

        createWalletPhaseTwoWalletNameController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
        createWalletPhaseTwoWalletNameController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                createWalletPhaseTwoWalletNameController.showMessage("비밀번호가 다릅니다.");
            }
        });

        createWalletPhaseTwoWalletPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        createWalletPhaseTwoConfirmPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "********");
        loadWalletPrivateKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "******************************");
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
            this.introLoadWalletPhaseTwo.setVisible(false);
            this.introLoadWalletPhaseThreeTypeFile.setVisible(true);
            this.introNaviTwo.setImage(introNaviCircle);
            this.introNaviThree.setImage(introNavi);
            this.introNaviTwo.setFitWidth(6);
            this.introNaviThree.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(5);

            // TextField Initialize
            // this.apisTextFieldController.init();
        } else if(loadWalletPhaseTwoFlag == LOAD_WALLET_PRIVATE_KEY) {
            this.introLoadWalletPhaseTwo.setVisible(false);
            this.introLoadWalletPhaseThreeTypePk.setVisible(true);
            this.introNaviTwo.setImage(introNaviCircle);
            this.introNaviThree.setImage(introNavi);
            this.introNaviTwo.setFitWidth(6);
            this.introNaviThree.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(6);

            // TextField Initialize
            // this.apisTextFieldController.init();
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

    public void loadWalletPhaseThreeTypeFileBackClick() {
        this.introLoadWalletPhaseThreeTypeFile.setVisible(false);
        this.introLoadWalletPhaseTwo.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(4);
    }

    public void loadWalletPhaseThreeTypeFileLoadClick() {

    }

    public void loadWalletPhaseThreeTypePkBackClick() {
        this.introLoadWalletPhaseThreeTypePk.setVisible(false);
        this.introLoadWalletPhaseTwo.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(4);
    }

    public void loadWalletPhaseThreeTypePkNextClick() {
        this.introLoadWalletPhaseThreeTypePk.setVisible(false);
        this.introLoadWalletPhaseFourTypePk.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviFour.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviFour.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(7);
    }

    public void loadWalletPhaseFourTypePkBackClick() {
        this.introLoadWalletPhaseFourTypePk.setVisible(false);
        this.introLoadWalletPhaseThreeTypePk.setVisible(true);
        this.introNaviFour.setImage(introNaviCircle);
        this.introNaviThree.setImage(introNavi);
        this.introNaviFour.setFitWidth(6);
        this.introNaviThree.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(6);
    }

    public void loadWalletPhaseFourTypePkLoadClick() {

    }

}