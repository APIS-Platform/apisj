package org.apis.gui.controller;

import javafx.animation.ScaleTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.module.ApisTextFieldGroup;
import org.apis.gui.controller.module.ApisTextFieldPkController;
import org.apis.gui.controller.module.OnScreenKeyboardController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.StringManager;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

public class IntroController extends BaseViewController {
    // Load Wallet Radio Button Values
    private static final int LOAD_WALLET_SELECT_WALLET_FILE = 1;
    private static final int LOAD_WALLET_PRIVATE_KEY = 2;

    private boolean isPrevMain = false;
    // Download Keystore File Flag
    private static boolean DOWNLOAD_KEYSTORE_FILE_FLAG = false;
    // Match Keystore File password Flag
    private static boolean MATCH_KEYSTORE_FILE_PASSWORD = false;
    // Keystore File Delete Flag from Finishing the Phases
    private static boolean DELETE_KEYSTORE_FILE_FLAG = false;

    private int loadWalletPhaseTwoFlag = LOAD_WALLET_SELECT_WALLET_FILE;

    // Link to FXML Controls
    @FXML private ImageView hexagonCreateWalletLabelImg, hexagonLoadWalletLabelImg;
    @FXML private ImageView loadWalletPhaseThreeTypePkNext, loadWalletPhaseFourTypePkLoad;
    @FXML private ImageView introHomeBtn, introNaviOne, introNaviTwo, introNaviThree, introNaviFour;
    @FXML private ImageView loadWalletPhaseTwoRadioOneImg, loadWalletPhaseTwoRadioTwoImg, keystoreFileDragZone;
    @FXML private Label hexagonCreateWalletLabel, hexagonLoadWalletLabel, createWalletNameWarnLabel, createWalletPhaseThreeNext, introNoFour, loadWalletPhaseTwoIntroNoFour, keystoreFileNameLabel, pkLabel, backBtn, backBtn1, backBtn2, backBtn3, backBtn4, nextBtn3, createWalletPhaseTwoNext, introCwPhaseFourRightNext, loadWalletPhaseThreeTypeFileLoad;
    @FXML private GridPane introPhaseOne, introCreateWalletPhaseTwo, introCreateWalletPhaseThree, introCreateWalletPhaseFour, createWalletNameWarn, introModalBackground;
    @FXML private GridPane introLoadWalletPhaseTwo, introLoadWalletPhaseThreeTypeFile, introLoadWalletPhaseThreeTypePk, introLoadWalletPhaseFourTypePk ;
    @FXML private GridPane keystoreFileNameGrid, keystoreFileMessage;
    @FXML private GridPane hexagonCreateWalletBtn, hexagonLoadWalletBtn;
    @FXML private TabPane introPhaseTab;
    @FXML private AnchorPane downloadKeystoreSuccess, downloadKeystoreCaution, copyPk;
    @FXML private Label popupCopyTitle, popupCopySubTitle, popupCopyConfirmBtn;
    @FXML private Label hexagonCreateTitle, hexagonCreateSubTitle, hexagonLoadTitle, hexagonLoadSubTitle;

    // label list;
    @FXML
    private Label introPhaseOneTitle,
            introCwPhaseTwoTitle, introCwPhaseTwoMenu1, introCwPhaseTwoMenu1Comment,
            introCwPhaseThreeTitle, introCwPhaseThreeMenu1, introCwPhaseThreeMenu1Comment,
            introCwPhaseFourTitle, introCwPhaseFourMenu1, introCwPhaseFourMenu1Comment,
            introLwPhaseTwoTitle, introLwPhaseTwoMenu1, introLwPhaseTwoMenu1Comment,
            introLwPhaseThreeTitle, introLwPhaseThreeMenu1, introLwPhaseThreeMenu1Comment,
            introLwPhaseThreeTitle2, introLwPhaseThreeMenu2, introLwPhaseThreeMenu2Comment,
            introLwPhaseFourTitle, introLwPhaseFourMenu1, introLwPhaseFourMenu1Comment,
            introCwPhaseTwoRightTitle, introCwPhaseTwoRightSubTitle,
            introCwPhaseTwoRightNameLabel, introCwPhaseTwoRightPassLabel, introCwPhaseTwoRightCPassLabel,
            introCwPhaseThreeRightTitle, introCwPhaseThreeRightSubTitle, introCwPhaseThreeRightDownload,introCwPhaseThreeRightDownloadBtn,
            introCwPhaseFourRightTitle, introCwPhaseFourRightSubTitle, introCwPhaseFourRightPkLabel,
            introLwPhaseTwoRightTitle, introLwPhaseTwoRightSubTitle, introLwPhaseTwoRightList1, introLwPhaseTwoRightList2,
            introLwPhaseThreeRightTitle, introLwPhaseThreeRightSubTitle,
            introLwPhaseThreeRightTitle2, introLwPhaseThreeRightSubTitle2, introLwPhaseFourRightTitle, introLwPhaseFourRightSubTitle,
            popupSuccessTitle, popupSuccessComment, popupSuccessButton, popupCautionTitle, popupCautionComment, popupCautionNoButton, popupCautionYesButton,
            introLwPhaseThreeRightButtonTitle, introLwPhaseThreeRightPwLabel, introLwPhaseThreeRightPkLabel,
            introLwPhaseFourRightWalletNameLabel, introLwPhaseFourRightWalletPwLabel, introLwPhaseFourRightWalletRePwLabel
    ;

    private Image createLabelOn, createLabelOff, loadLabelOn, loadLabelOff;
    private Image introNavi, introNaviCircle;
    private Image nextGreyBtn, nextRedBtn, loadGreyBtn, loadRedBtn;
    private Image radioCheckBtnRed, radioCheckBtnGrey;
    private Image keystoreFileDragAndDrop, keystoreFileCorrect, keystoreFileWrong;

    private String keystoreFilePath;

    // External GUI and Controller add
    @FXML
    private ApisTextFieldPkController createWalletPrivateKeyController;

    @FXML
    public ApisTextFieldController createWalletPhaseTwoWalletNameController, createWalletPhaseTwoWalletPasswordController, createWalletPhaseTwoConfirmPasswordController,
                                   loadWalletPhaseThreeTypeFilePwController, loadWalletPrivateKeyController, loadWalletPhaseFourTypePkNmController,
                                   loadWalletPhaseFourTypePkPwController, loadWalletPhaseFourTypePkCfController;

    private ApisTextFieldGroup apisTextFieldGroup = new ApisTextFieldGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setIntro(this);

        // 언어 설정
        languageSetting();

        // Hide Home Button when the first access
        setVisibleHomeBtn(false);

        // Tab Pane Direction Key Block
        introPhaseTab.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.LEFT
                || event.getCode() == KeyCode.RIGHT
                || event.getCode() == KeyCode.UP
                || event.getCode() == KeyCode.DOWN) {
                    if(introPhaseTab.isFocused()){
                        event.consume();
                    }else{
                    }
            }
        });

        // Set Escape Key event
        introModalBackground.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if(event.getCode() == KeyCode.ESCAPE) {
                if(downloadKeystoreSuccess.isVisible()) {
                    downloadKeystoreConfirm();
                } else if(downloadKeystoreCaution.isVisible()) {
                    downloadKeystoreCautionNo();
                } else if(copyPk.isVisible()) {
                    copyPkConfirm();
                }
            } else if(event.getCode() == KeyCode.ENTER) {
                if(downloadKeystoreSuccess.isVisible()) {
                    downloadKeystoreConfirm();
                } else if(downloadKeystoreCaution.isVisible()) {
                    downloadKeystoreCautionYes();
                } else if(copyPk.isVisible()) {
                    copyPkConfirm();
                }
            }
        });

        // initial Image Setting
        createLabelOn = new Image("image/ic_plus_white@2x.png");
        createLabelOff = new Image("image/ic_plus_white_none@2x.png");
        loadLabelOn = new Image("image/ic_down_arrow_white@2x.png");
        loadLabelOff = new Image("image/ic_down_arrow_white_none@2x.png");
        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");
        nextGreyBtn = new Image("image/btn_next_grey@2x.png");
        nextRedBtn = new Image("image/btn_next_red@2x.png");
        loadGreyBtn = new Image("image/btn_load_grey@2x.png");
        loadRedBtn = new Image("image/btn_load_red@2x.png");
        radioCheckBtnRed = new Image("image/btn_check_red@2x.png");
        radioCheckBtnGrey = new Image("image/btn_check_grey@2x.png");
        keystoreFileDragAndDrop = new Image("image/bg_dragdrop@2x.png");
        keystoreFileCorrect = new Image("image/bg_dragdrop_black@2x.png");
        keystoreFileWrong = new Image("image/bg_dragdrop_red@2x.png");

        // inital File Path Setting
        keystoreFilePath = null;

        // Create Wallet Phase 2 Textfield Validation Work
        createWalletPhaseTwoWalletNameController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, StringManager.getInstance().common.walletNamePlaceholder.get(), ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        createWalletPhaseTwoWalletNameController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (createWalletPhaseTwoWalletNameController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoWalletNameController.setText("");
                }

                text = createWalletPhaseTwoWalletNameController.getText();

                if (text == null || text.equals("")) {
                    createWalletPhaseTwoWalletNameController.failedForm(StringManager.getInstance().common.walletNameNull.get());
                } else {
                    createWalletPhaseTwoWalletNameController.succeededForm();
                }

                createWalletPhaseTwoActivateNext();
            }

            @Override
            public void change(String old_text, String new_text) {



            }

            @Override
            public void onAction() {
                createWalletPhaseTwoWalletPasswordController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                createWalletPhaseTwoWalletPasswordController.requestFocus();
            }
        });

        createWalletPhaseTwoWalletPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get(), ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        createWalletPhaseTwoWalletPasswordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(createWalletPhaseTwoWalletPasswordController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoWalletPasswordController.setText("");
                }

                text = createWalletPhaseTwoWalletPasswordController.getText();

                if(text == null || text.equals("")) {
                    createWalletPhaseTwoWalletPasswordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(text.length() < 8) {
                    createWalletPhaseTwoWalletPasswordController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if(!createWalletPhaseTwoWalletPasswordController.pwValidate(text)) {
                    createWalletPhaseTwoWalletPasswordController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
                } else {
                    createWalletPhaseTwoWalletPasswordController.succeededForm();
                }

                if(!createWalletPhaseTwoConfirmPasswordController.getText().isEmpty()) {
                    createWalletPhaseTwoConfirmPasswordController.getHandler().onFocusOut();
                }

                createWalletPhaseTwoActivateNext();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text = null;

                if(createWalletPhaseTwoWalletPasswordController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoWalletPasswordController.setText("");
                }

                text = createWalletPhaseTwoWalletPasswordController.getText();

                if(text == null || text.equals("")) {
                    createWalletPhaseTwoWalletPasswordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(text.length() < 8) {
                    createWalletPhaseTwoWalletPasswordController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if(!createWalletPhaseTwoWalletPasswordController.pwValidate(text)) {
                    createWalletPhaseTwoWalletPasswordController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
                } else {
                    createWalletPhaseTwoWalletPasswordController.succeededForm();
                }

                createWalletPhaseTwoActivateNext();
            }

            @Override
            public void onAction() {
                createWalletPhaseTwoConfirmPasswordController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                createWalletPhaseTwoConfirmPasswordController.requestFocus();
            }
        });

        createWalletPhaseTwoConfirmPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "", ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        createWalletPhaseTwoConfirmPasswordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoConfirmPasswordController.setText("");
                }

                text = createWalletPhaseTwoConfirmPasswordController.getText();

                if(text == null || text.equals("")) {
                    createWalletPhaseTwoConfirmPasswordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                } else if(!text.equals(createWalletPhaseTwoWalletPasswordController.getText())) {
                    createWalletPhaseTwoConfirmPasswordController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    createWalletPhaseTwoConfirmPasswordController.succeededForm();
                }

                createWalletPhaseTwoActivateNext();

            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoConfirmPasswordController.setText("");
                }

                text = createWalletPhaseTwoConfirmPasswordController.getText();

                if(text == null || text.equals("")) {
                    createWalletPhaseTwoConfirmPasswordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                } else if(!text.equals(createWalletPhaseTwoWalletPasswordController.getText())) {
                    createWalletPhaseTwoConfirmPasswordController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    createWalletPhaseTwoConfirmPasswordController.succeededForm();
                }

                createWalletPhaseTwoActivateNext();
            }

            @Override
            public void onAction() {
                createWalletPhaseTwoWalletNameController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                createWalletPhaseTwoWalletNameController.requestFocus();
            }
        });


        // Load Wallet Phase 3 Type File Password Validation
        loadWalletPhaseThreeTypeFilePwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get(), ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        loadWalletPhaseThreeTypeFilePwController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                StyleManager.backgroundColorStyle(loadWalletPhaseThreeTypeFileLoad, StyleManager.AColor.Cd8d8d8);
                loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.HAND);

                if (loadWalletPhaseThreeTypeFilePwController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseThreeTypeFilePwController.setText("");
                }

                byte[] password = loadWalletPhaseThreeTypeFilePwController.getText().trim().getBytes(Charset.forName("UTF-8"));

                MATCH_KEYSTORE_FILE_PASSWORD = false;

                if (password == null) {
                    loadWalletPhaseThreeTypeFilePwController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if (!KeyStoreManager.getInstance().matchPassword(password)) {
                    loadWalletPhaseThreeTypeFilePwController.failedForm(StringManager.getInstance().common.walletPasswordNotKeystoreMatch.get());
                } else {
                    MATCH_KEYSTORE_FILE_PASSWORD = true;
                    loadWalletPhaseThreeTypeFilePwController.succeededForm();

                    StyleManager.backgroundColorStyle(loadWalletPhaseThreeTypeFileLoad, StyleManager.AColor.C910000);
                    loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.HAND);
                }
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;
                StyleManager.backgroundColorStyle(loadWalletPhaseThreeTypeFileLoad, StyleManager.AColor.Cd8d8d8);
                loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.HAND);

                if (loadWalletPhaseThreeTypeFilePwController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseThreeTypeFilePwController.setText("");
                }

                text = loadWalletPhaseThreeTypeFilePwController.getText();

                MATCH_KEYSTORE_FILE_PASSWORD = false;

                if (text == null || text.equals("")) {
                    loadWalletPhaseThreeTypeFilePwController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else {
                    MATCH_KEYSTORE_FILE_PASSWORD = true;
                    loadWalletPhaseThreeTypeFilePwController.succeededForm();

                    StyleManager.backgroundColorStyle(loadWalletPhaseThreeTypeFileLoad, StyleManager.AColor.C910000);
                    loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.HAND);
                }
            }

            @Override
            public void onAction() {

            }

            @Override
            public void onKeyTab(){

            }
        });

        // Load Wallet Phase 3 Type Private Key Validation
        loadWalletPrivateKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "", ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        loadWalletPrivateKeyController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                if (!loadWalletPrivateKeyController.getText().matches("[0-9a-fA-F]*")) {
                    loadWalletPrivateKeyController.setText(loadWalletPrivateKeyController.getText().replaceAll("[^0-9a-fA-F]", ""));
                }

                String text;
                loadWalletPhaseThreeTypePkNext.setImage(nextGreyBtn);
                loadWalletPhaseThreeTypePkNext.setCursor(Cursor.HAND);

                if(loadWalletPrivateKeyController.getCheckBtnEnteredFlag()) {
                    loadWalletPrivateKeyController.setText("");
                }

                text = loadWalletPrivateKeyController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPrivateKeyController.failedForm(StringManager.getInstance().common.privateKeyNull.get());
                } else if(loadWalletPrivateKeyController.pkValidate(text) || text.length() != 64) {
                    loadWalletPrivateKeyController.failedForm(StringManager.getInstance().common.privateKeyIncorrect.get());
                } else {
                    loadWalletPrivateKeyController.succeededForm();
                    loadWalletPhaseThreeTypePkNext.setImage(nextRedBtn);
                    loadWalletPhaseThreeTypePkNext.setCursor(Cursor.HAND);
                }
            }

            @Override
            public void change(String old_text, String new_text) {
                if (!loadWalletPrivateKeyController.getText().matches("[0-9a-fA-F]*")) {
                    loadWalletPrivateKeyController.setText(loadWalletPrivateKeyController.getText().replaceAll("[^0-9a-fA-F]", ""));
                }

                int maxlength = 64;
                if(loadWalletPrivateKeyController.getText().length() > maxlength){
                    loadWalletPrivateKeyController.setText(loadWalletPrivateKeyController.getText().substring(0, maxlength));
                }

                String text;
                loadWalletPhaseThreeTypePkNext.setImage(nextGreyBtn);
                loadWalletPhaseThreeTypePkNext.setCursor(Cursor.HAND);

                if(loadWalletPrivateKeyController.getCheckBtnEnteredFlag()) {
                    loadWalletPrivateKeyController.setText("");
                }

                text = loadWalletPrivateKeyController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPrivateKeyController.failedForm(StringManager.getInstance().common.privateKeyNull.get());
                } else if(loadWalletPrivateKeyController.pkValidate(text) || text.length() != 64) {
                    loadWalletPrivateKeyController.failedForm(StringManager.getInstance().common.privateKeyIncorrect.get());
                } else {
                    loadWalletPrivateKeyController.succeededForm();
                    loadWalletPhaseThreeTypePkNext.setImage(nextRedBtn);
                    loadWalletPhaseThreeTypePkNext.setCursor(Cursor.HAND);
                }
            }

            @Override
            public void onAction() {

            }

            @Override
            public void onKeyTab(){

            }
        });

        // Load Wallet Phase 4 TextField Validation Work
        loadWalletPhaseFourTypePkNmController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "", ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        loadWalletPhaseFourTypePkNmController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (loadWalletPhaseFourTypePkNmController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkNmController.setText("");
                }

                text = loadWalletPhaseFourTypePkNmController.getText();

                if (text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkNmController.failedForm(StringManager.getInstance().common.walletNameNull.get());
                } else {
                    loadWalletPhaseFourTypePkNmController.succeededForm();
                }

                loadWalletPhaseFourActivateLoad();
            }

            @Override
            public void change(String old_text, String new_text) {

            }

            @Override
            public void onAction() {
                loadWalletPhaseFourTypePkPwController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                loadWalletPhaseFourTypePkPwController.requestFocus();
            }
        });

        loadWalletPhaseFourTypePkPwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get(), ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        loadWalletPhaseFourTypePkPwController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(loadWalletPhaseFourTypePkPwController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkPwController.setText("");
                }

                text = loadWalletPhaseFourTypePkPwController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkPwController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(text.length() < 8) {
                    loadWalletPhaseFourTypePkPwController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if(!loadWalletPhaseFourTypePkPwController.pwValidate(text)) {
                    loadWalletPhaseFourTypePkPwController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
                } else {
                    loadWalletPhaseFourTypePkPwController.succeededForm();
                }

                if(!loadWalletPhaseFourTypePkCfController.getText().isEmpty()) {
                    loadWalletPhaseFourTypePkCfController.getHandler().onFocusOut();
                }

                loadWalletPhaseFourActivateLoad();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if(loadWalletPhaseFourTypePkPwController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkPwController.setText("");
                }

                text = loadWalletPhaseFourTypePkPwController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkPwController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(text.length() < 8) {
                    loadWalletPhaseFourTypePkPwController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if(!loadWalletPhaseFourTypePkPwController.pwValidate(text)) {
                    loadWalletPhaseFourTypePkPwController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
                } else {
                    loadWalletPhaseFourTypePkPwController.succeededForm();
                }

                if(!loadWalletPhaseFourTypePkCfController.getText().isEmpty()) {
                    loadWalletPhaseFourTypePkCfController.getHandler().onFocusOut();
                }

                loadWalletPhaseFourActivateLoad();
            }

            @Override
            public void onAction() {
                loadWalletPhaseFourTypePkCfController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                loadWalletPhaseFourTypePkCfController.requestFocus();
            }
        });

        loadWalletPhaseFourTypePkCfController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "", ApisTextFieldController.THEME_TYPE_INTRO, OnScreenKeyboardController.CARET_INTRO);
        loadWalletPhaseFourTypePkCfController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(loadWalletPhaseFourTypePkCfController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkCfController.setText("");
                }

                text = loadWalletPhaseFourTypePkCfController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkCfController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                } else if(!text.equals(loadWalletPhaseFourTypePkPwController.getText())) {
                    loadWalletPhaseFourTypePkCfController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    loadWalletPhaseFourTypePkCfController.succeededForm();
                }

                loadWalletPhaseFourActivateLoad();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if(loadWalletPhaseFourTypePkCfController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkCfController.setText("");
                }

                text = loadWalletPhaseFourTypePkCfController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkCfController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                } else if(!text.equals(loadWalletPhaseFourTypePkPwController.getText())) {
                    loadWalletPhaseFourTypePkCfController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    loadWalletPhaseFourTypePkCfController.succeededForm();
                }

                loadWalletPhaseFourActivateLoad();
            }

            @Override
            public void onAction() {
                loadWalletPhaseFourTypePkNmController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                loadWalletPhaseFourTypePkNmController.requestFocus();
            }
        });

        createWalletPrivateKeyController.setHandler(new ApisTextFieldPkController.ApisTextFieldPkImpl() {
            @Override
            public void copy() {
                pkLabel.setText(createWalletPrivateKeyController.getText());
                introModalBackground.setVisible(true);
                copyPk.setVisible(true);
                introModalBackground.requestFocus();
            }
            @Override
            public void onAction(){

            }
            @Override
            public void onKeyTab(){

            }
        });

        apisTextFieldGroup.add(createWalletPhaseTwoWalletNameController);
        apisTextFieldGroup.add(createWalletPhaseTwoWalletPasswordController);
        apisTextFieldGroup.add(createWalletPhaseTwoConfirmPasswordController);
        apisTextFieldGroup.add(loadWalletPrivateKeyController);
        apisTextFieldGroup.add(loadWalletPhaseFourTypePkNmController);
        apisTextFieldGroup.add(loadWalletPhaseFourTypePkPwController);
        apisTextFieldGroup.add(loadWalletPhaseFourTypePkCfController);
    }

    public void languageSetting(){
        this.introPhaseOneTitle.textProperty().bind(StringManager.getInstance().intro.phaseOneTitle);
        this.hexagonCreateWalletLabel.textProperty().bind(StringManager.getInstance().intro.phaseOneMenu1);
        this.hexagonLoadWalletLabel.textProperty().bind(StringManager.getInstance().intro.phaseOneMenu2);

        this.hexagonCreateTitle.textProperty().bind(StringManager.getInstance().intro.phaseOneMenu1);
        this.hexagonCreateSubTitle.textProperty().bind(StringManager.getInstance().intro.phaseOneMenuMsg1);
        this.hexagonLoadTitle.textProperty().bind(StringManager.getInstance().intro.phaseOneMenu2);
        this.hexagonLoadSubTitle.textProperty().bind(StringManager.getInstance().intro.phaseOneMenuMsg2);

        this.introCwPhaseTwoTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseTwoTitle);
        this.introCwPhaseTwoMenu1.textProperty().bind(StringManager.getInstance().intro.cwPhaseTwoMenu1);
        this.introCwPhaseTwoMenu1Comment.textProperty().bind(StringManager.getInstance().intro.cwPhaseTwoMenu1Comment);
        this.introCwPhaseThreeTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseThreeTitle);
        this.introCwPhaseThreeMenu1.textProperty().bind(StringManager.getInstance().intro.cwPhaseThreeMenu1);
        this.introCwPhaseThreeMenu1Comment.textProperty().bind(StringManager.getInstance().intro.cwPhaseThreeMenu1Comment);
        this.introCwPhaseFourTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseFourTitle);
        this.introCwPhaseFourMenu1.textProperty().bind(StringManager.getInstance().intro.cwPhaseFourMenu1);
        this.introCwPhaseFourMenu1Comment.textProperty().bind(StringManager.getInstance().intro.cwPhaseFourMenu1Comment);

        this.introLwPhaseTwoTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoTitle);
        this.introLwPhaseTwoMenu1.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoMenu1);
        this.introLwPhaseTwoMenu1Comment.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoMenu1Comment);
        this.introLwPhaseThreeTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeTitle);
        this.introLwPhaseThreeMenu1.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu1);
        this.introLwPhaseThreeMenu1Comment.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu1Comment);
        this.introLwPhaseThreeTitle2.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeTitle2);
        this.introLwPhaseThreeMenu2.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu2);
        this.introLwPhaseThreeMenu2Comment.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu2Comment);
        this.introLwPhaseFourTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseFourTitle);
        this.introLwPhaseFourMenu1.textProperty().bind(StringManager.getInstance().intro.lwPhaseFourMenu1);
        this.introLwPhaseFourMenu1Comment.textProperty().bind(StringManager.getInstance().intro.lwPhaseFourMenu1Comment);

        this.introCwPhaseTwoRightTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseTwoMenu1);
        this.introCwPhaseTwoRightSubTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseTwoMenu1Comment);
        this.introCwPhaseTwoRightNameLabel.textProperty().bind(StringManager.getInstance().intro.walletNameLabel);
        this.introCwPhaseTwoRightPassLabel.textProperty().bind(StringManager.getInstance().intro.walletPasswordLabel);
        this.introCwPhaseTwoRightCPassLabel.textProperty().bind(StringManager.getInstance().intro.confirmPasswordLabel);
        this.introCwPhaseThreeRightTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseThreeMenu1);
        this.introCwPhaseThreeRightSubTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseThreeMenu1Comment);
        this.introCwPhaseThreeRightDownload.textProperty().bind(StringManager.getInstance().common.downloadLabel);
        this.introCwPhaseThreeRightDownloadBtn.textProperty().bind(StringManager.getInstance().intro.cwPhaseThreeMenu1DownloadBtn);
        this.introCwPhaseFourRightTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseFourMenu1);
        this.introCwPhaseFourRightSubTitle.textProperty().bind(StringManager.getInstance().intro.cwPhaseFourMenu1Comment);
        this.introCwPhaseFourRightPkLabel.textProperty().bind(StringManager.getInstance().common.privateKeyLabel);

        this.introLwPhaseTwoRightTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoMenu1);
        this.introLwPhaseTwoRightSubTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoMenu1Comment);
        this.introLwPhaseTwoRightList1.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoListItem1);
        this.introLwPhaseTwoRightList2.textProperty().bind(StringManager.getInstance().intro.lwPhaseTwoListItem2);
        this.introLwPhaseThreeRightTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu1);
        this.introLwPhaseThreeRightSubTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu1Comment);
        this.introLwPhaseThreeRightTitle2.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu2);
        this.introLwPhaseThreeRightSubTitle2.textProperty().bind(StringManager.getInstance().intro.lwPhaseThreeMenu2Comment);
        this.introLwPhaseThreeRightPwLabel.textProperty().bind(StringManager.getInstance().common.passwordLabel);
        this.introLwPhaseThreeRightButtonTitle.textProperty().bind(StringManager.getInstance().intro.introLwPhaseThreeRightButtonTitle);
        this.introLwPhaseThreeRightPkLabel.textProperty().bind(StringManager.getInstance().common.privateKeyLabel);

        this.introLwPhaseFourRightTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseFourMenu1);
        this.introLwPhaseFourRightSubTitle.textProperty().bind(StringManager.getInstance().intro.lwPhaseFourMenu1Comment);
        this.introLwPhaseFourRightWalletNameLabel.textProperty().bind(StringManager.getInstance().common.walletNameLabel);
        this.introLwPhaseFourRightWalletPwLabel.textProperty().bind(StringManager.getInstance().common.walletPasswordLabel);
        this.introLwPhaseFourRightWalletRePwLabel.textProperty().bind(StringManager.getInstance().common.walletRePasswordLabel);

        this.popupSuccessTitle.textProperty().bind(StringManager.getInstance().intro.popupSuccessTitle);
        this.popupSuccessComment.textProperty().bind(StringManager.getInstance().intro.popupSuccessComment);
        this.popupSuccessButton.textProperty().bind(StringManager.getInstance().common.confirmButton);
        this.popupCautionTitle.textProperty().bind(StringManager.getInstance().intro.popupCautionTitle);
        this.popupCautionComment.textProperty().bind(StringManager.getInstance().intro.popupCautionComment);
        this.popupCautionNoButton.textProperty().bind(StringManager.getInstance().common.noButton);
        this.popupCautionYesButton.textProperty().bind(StringManager.getInstance().common.yesButton);

        this.backBtn.textProperty().bind(StringManager.getInstance().common.backButton);
        this.backBtn1.textProperty().bind(StringManager.getInstance().common.backButton);
        this.backBtn2.textProperty().bind(StringManager.getInstance().common.backButton);
        this.backBtn3.textProperty().bind(StringManager.getInstance().common.backButton);
        this.backBtn4.textProperty().bind(StringManager.getInstance().common.backButton);

        this.createWalletPhaseTwoNext.textProperty().bind(StringManager.getInstance().common.nextButton);
        this.createWalletPhaseThreeNext.textProperty().bind(StringManager.getInstance().common.nextButton);
        this.introCwPhaseFourRightNext.textProperty().bind(StringManager.getInstance().common.nextButton);
        this.nextBtn3.textProperty().bind(StringManager.getInstance().common.nextButton);
        this.loadWalletPhaseThreeTypeFileLoad.textProperty().bind(StringManager.getInstance().common.nextButton);

        this.popupCopyTitle.textProperty().bind(StringManager.getInstance().popup.copyPkTitle);
        this.popupCopySubTitle.textProperty().bind(StringManager.getInstance().popup.copyPkSubTitle);
        this.popupCopyConfirmBtn.textProperty().bind(StringManager.getInstance().common.confirmButton);
    }

    public void setVisibleHomeBtn(boolean isVisible) {
        if(isVisible) {
            this.introHomeBtn.setVisible(true);
            this.introHomeBtn.setFitWidth(106);
        } else {
            this.introHomeBtn.setVisible(false);
            this.introHomeBtn.setFitWidth(1);
        }
    }

    // Next button Control
    public void createWalletPhaseTwoActivateNext() {
        StyleManager.backgroundColorStyle(createWalletPhaseTwoNext, StyleManager.AColor.Cd8d8d8);
        createWalletPhaseTwoNext.setCursor(Cursor.HAND);

        if(createWalletPhaseTwoWalletNameController.getCheckBtnType() == 3) {
            if(createWalletPhaseTwoWalletPasswordController.getCheckBtnType() == 3) {
                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnType() == 3) {
                    StyleManager.backgroundColorStyle(createWalletPhaseTwoNext, StyleManager.AColor.C910000);
                    createWalletPhaseTwoNext.setCursor(Cursor.HAND);

                }
            }
        }
    }

    public void loadWalletPhaseFourActivateLoad() {
        loadWalletPhaseFourTypePkLoad.setImage(loadGreyBtn);
        loadWalletPhaseFourTypePkLoad.setCursor(Cursor.HAND);

        if(loadWalletPhaseFourTypePkNmController.getCheckBtnType() == 3) {
            if(loadWalletPhaseFourTypePkPwController.getCheckBtnType() == 3) {
                if(loadWalletPhaseFourTypePkCfController.getCheckBtnType() == 3) {
                    loadWalletPhaseFourTypePkLoad.setImage(loadRedBtn);
                    loadWalletPhaseFourTypePkLoad.setCursor(Cursor.HAND);
                }
            }
        }
    }

    // Phase 1 Landing Page
    public void hexagonCreateWalletBtnMouseEntered() {
        this.hexagonCreateWalletLabelImg.setImage(createLabelOn);
        this.hexagonCreateWalletBtn.setOpacity(1);
        this.hexagonCreateWalletLabel.setOpacity(1);
        this.hexagonLoadWalletLabelImg.setImage(loadLabelOff);
        this.hexagonLoadWalletBtn.setOpacity(0.3);
        this.hexagonLoadWalletLabel.setOpacity(0.3);
        this.introNoFour.setVisible(true);
        this.introNaviFour.setVisible(true);

    }

    public void hexagonLoadWalletBtnMouseEntered() {
        this.hexagonLoadWalletLabelImg.setImage(loadLabelOn);
        this.hexagonLoadWalletBtn.setOpacity(1);
        this.hexagonLoadWalletLabel.setOpacity(1);
        this.hexagonCreateWalletLabelImg.setImage(createLabelOff);
        this.hexagonCreateWalletBtn.setOpacity(0.3);
        this.hexagonCreateWalletLabel.setOpacity(0.3);
        this.introNoFour.setVisible(false);
        this.introNaviFour.setVisible(false);

    }

    // Create Wallet Phases
    public void createWalletBtnClick() {
        createWalletPhaseTwoWalletNameController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, StringManager.getInstance().common.walletNamePlaceholder.get());
        createWalletPhaseTwoWalletPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        createWalletPhaseTwoConfirmPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
        StyleManager.backgroundColorStyle(createWalletPhaseTwoNext, StyleManager.AColor.Cd8d8d8);
        createWalletPhaseTwoNext.setCursor(Cursor.HAND);
        this.introPhaseOne.setVisible(false);
        this.introCreateWalletPhaseTwo.setVisible(true);
        this.introNaviOne.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviOne.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(1);

        createWalletPhaseTwoWalletNameController.requestFocus();
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
        createWalletPhaseTwoWalletNameController.getHandler().onFocusOut();
        createWalletPhaseTwoWalletPasswordController.getHandler().onFocusOut();
        createWalletPhaseTwoConfirmPasswordController.getHandler().onFocusOut();

        if(createWalletPhaseTwoWalletNameController.getCheckBtnType() == 3) {
            if(createWalletPhaseTwoWalletPasswordController.getCheckBtnType() == 3) {
                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnType() == 3) {
                    StyleManager.backgroundColorStyle(createWalletPhaseThreeNext, StyleManager.AColor.Cd8d8d8);
                    this.createWalletPhaseThreeNext.setCursor(Cursor.HAND);
                    this.introCreateWalletPhaseTwo.setVisible(false);
                    this.introCreateWalletPhaseThree.setVisible(true);
                    this.introNaviTwo.setImage(introNaviCircle);
                    this.introNaviThree.setImage(introNavi);
                    this.introNaviTwo.setFitWidth(6);
                    this.introNaviThree.setFitWidth(24);
                    this.introPhaseTab.getSelectionModel().select(2);

                    // Create Keystore
                    String wName = createWalletPhaseTwoWalletNameController.getText();
                    String wPasswd = createWalletPhaseTwoWalletPasswordController.getText();
                    KeyStoreManager.getInstance().createKeystore(null, wName, wPasswd);
                    createWalletPrivateKeyController.setText(KeyStoreManager.getInstance().getPrivateKey());
                    createWalletPrivateKeyController.setAddress(KeyStoreManager.getInstance().getWalletAddress());
                }
            }
        }
    }

    public void createWalletPhaseThreeBackClick() {
        KeyStoreManager.getInstance().deleteKeystore();
        this.DOWNLOAD_KEYSTORE_FILE_FLAG = false;
        this.introCreateWalletPhaseThree.setVisible(false);
        this.introCreateWalletPhaseTwo.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(1);

        this.createWalletPhaseTwoWalletNameController.requestFocus();
    }

    public void createWalletPhaseThreeNextClick() {
        if(this.DOWNLOAD_KEYSTORE_FILE_FLAG) {
            this.downloadKeystoreCaution.setVisible(false);
            this.introModalBackground.setVisible(false);
            this.introCreateWalletPhaseThree.setVisible(false);
            this.introCreateWalletPhaseFour.setVisible(true);
            this.introNaviThree.setImage(introNaviCircle);
            this.introNaviFour.setImage(introNavi);
            this.introNaviThree.setFitWidth(6);
            this.introNaviFour.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(3);

            this.createWalletPrivateKeyController.setText(KeyStoreManager.getInstance().getPrivateKey());
            this.createWalletPrivateKeyController.init();
        } else {
            this.introModalBackground.setVisible(true);
            this.downloadKeystoreCaution.setVisible(true);
            this.introModalBackground.requestFocus();
        }
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

        this.createWalletPrivateKeyController.setText(KeyStoreManager.getInstance().getPrivateKey());
        this.createWalletPrivateKeyController.init();
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
        // Create Wallet Complete
        DELETE_KEYSTORE_FILE_FLAG = true;
        this.introCreateWalletPhaseFour.setVisible(false);
        this.introPhaseOne.setVisible(true);
        this.introNaviFour.setImage(introNaviCircle);
        this.introNaviOne.setImage(introNavi);
        this.introNaviFour.setFitWidth(6);
        this.introNaviOne.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(0);

        KeyStoreManager.getInstance().setPrivateKey(null);
        AppManager.getInstance().guiFx.pageMoveMain();
    }

    public void createWalletDownloadKeystoreFile() {
        if(KeyStoreManager.openDirectoryReader() != null){
            this.DOWNLOAD_KEYSTORE_FILE_FLAG = true;
            StyleManager.backgroundColorStyle(createWalletPhaseThreeNext, StyleManager.AColor.C910000);
            this.createWalletPhaseThreeNext.setCursor(Cursor.HAND);
            this.introModalBackground.setVisible(true);
            this.downloadKeystoreSuccess.setVisible(true);
            this.introModalBackground.requestFocus();
        }
    }

    public void downloadKeystoreConfirm() {
        this.downloadKeystoreSuccess.setVisible(false);
        this.introModalBackground.setVisible(false);
    }

    public void downloadKeystoreCautionNo() {
        this.downloadKeystoreCaution.setVisible(false);
        this.introModalBackground.setVisible(false);
    }

    public void copyPkConfirm() {
        this.copyPk.setVisible(false);
        this.introModalBackground.setVisible(false);
    }

    // Load Wallet Phases
    public void loadWalletBtnClick() {
        DELETE_KEYSTORE_FILE_FLAG = true;

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
        DELETE_KEYSTORE_FILE_FLAG = false;

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
            this.keystoreFileDragZone.setImage(keystoreFileDragAndDrop);
            this.keystoreFileNameGrid.setVisible(false);
            this.keystoreFileMessage.setVisible(false);
            StyleManager.backgroundColorStyle(loadWalletPhaseThreeTypeFileLoad, StyleManager.AColor.Cd8d8d8);
            this.loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.HAND);
            this.introLoadWalletPhaseTwo.setVisible(false);
            this.introLoadWalletPhaseThreeTypeFile.setVisible(true);
            this.introNaviTwo.setImage(introNaviCircle);
            this.introNaviThree.setImage(introNavi);
            this.introNaviTwo.setFitWidth(6);
            this.introNaviThree.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(5);

            // TextField Initialize
            loadWalletPhaseThreeTypeFilePwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        } else if(loadWalletPhaseTwoFlag == LOAD_WALLET_PRIVATE_KEY) {
            this.loadWalletPhaseThreeTypePkNext.setImage(nextGreyBtn);
            this.loadWalletPhaseThreeTypePkNext.setCursor(Cursor.HAND);
            this.introLoadWalletPhaseTwo.setVisible(false);
            this.introLoadWalletPhaseThreeTypePk.setVisible(true);
            this.introNaviTwo.setImage(introNaviCircle);
            this.introNaviThree.setImage(introNavi);
            this.introNaviTwo.setFitWidth(6);
            this.introNaviThree.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(6);

            // TextField Initialize
            loadWalletPrivateKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
            loadWalletPrivateKeyController.requestFocus();
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
        loadWalletPhaseThreeTypeFilePwController.getHandler().onFocusOut();

        if(MATCH_KEYSTORE_FILE_PASSWORD) {
            MATCH_KEYSTORE_FILE_PASSWORD = false;
            this.introLoadWalletPhaseThreeTypeFile.setVisible(false);
            this.introPhaseOne.setVisible(true);
            this.introNaviThree.setImage(introNaviCircle);
            this.introNaviOne.setImage(introNavi);
            this.introNaviThree.setFitWidth(6);
            this.introNaviOne.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(0);

            KeyStoreManager.getInstance().downloadKeystore();
            AppManager.getInstance().guiFx.pageMoveMain();
        } else {
        }
    }

    public void loadWalletKeystoreFileChooser() {
        // Reset File data before Validation
        KeyStoreManager.getInstance().setKeystoreJsonData("");

        String result = KeyStoreManager.getInstance().openFileReader();

        if (result.equals("FileException")) {
            keystoreFileDragZone.setImage(keystoreFileDragAndDrop);
            keystoreFileNameGrid.setVisible(false);
            keystoreFileMessage.setVisible(false);
        } else if (result.equals("IncorrectFileForm")) {
            keystoreFileDragZone.setImage(keystoreFileWrong);
            keystoreFileNameLabel.setText(KeyStoreManager.getInstance().getKeystoreFileName());
            keystoreFileNameGrid.setVisible(true);
            keystoreFileMessage.setVisible(true);
        } else if (result.equals("CancelFileChooser")) {
            // Nothing to do
        } else {
            keystoreFileDragZone.setImage(keystoreFileCorrect);
            keystoreFileNameLabel.setText(KeyStoreManager.getInstance().getKeystoreFileName());
            keystoreFileNameGrid.setVisible(true);
            keystoreFileMessage.setVisible(false);
        }

        if(loadWalletPhaseThreeTypeFilePwController.getText().length() > 0) {
            loadWalletPhaseThreeTypeFilePwController.getHandler().onFocusOut();
        }
    }

    public void keystoreDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if(db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        } else {
            event.consume();
        }
    }

    public void keystoreDragReleased(DragEvent event) {
        // Reset File data before Validation
        KeyStoreManager.getInstance().setKeystoreJsonData("");

        Dragboard db = event.getDragboard();
        boolean success = false;

        if(db.hasFiles()) {
            success = true;
            keystoreFilePath = null;
            if(db.getFiles() != null && db.getFiles().size() > 0) {
                keystoreFilePath = db.getFiles().get(0).getAbsolutePath();

                String result = KeyStoreManager.getInstance().keystoreCheckFile(new File(keystoreFilePath));

                if(result.equals("FileException")) {
                    keystoreFileDragZone.setImage(keystoreFileDragAndDrop);
                    keystoreFileNameGrid.setVisible(false);
                    keystoreFileMessage.setVisible(false);
                } else if(result.equals("IncorrectFileForm")) {
                    keystoreFileDragZone.setImage(keystoreFileWrong);
                    keystoreFileNameLabel.setText(KeyStoreManager.getInstance().getKeystoreFileName());
                    keystoreFileNameGrid.setVisible(true);
                    keystoreFileMessage.setVisible(true);
                } else {
                    keystoreFileDragZone.setImage(keystoreFileCorrect);
                    keystoreFileNameLabel.setText(KeyStoreManager.getInstance().getKeystoreFileName());
                    keystoreFileNameGrid.setVisible(true);
                    keystoreFileMessage.setVisible(false);
                }
            }

            if(loadWalletPhaseThreeTypeFilePwController.getText().length() > 0) {
                loadWalletPhaseThreeTypeFilePwController.getHandler().onFocusOut();
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    public void keystoreFileCancelChoose() {
        // File Name and Message Visible False
        keystoreFileNameGrid.setVisible(false);
        keystoreFileMessage.setVisible(false);

        // reset ImageView
        keystoreFileDragZone.setImage(keystoreFileDragAndDrop);

        // reset file path
        keystoreFilePath = null;
        KeyStoreManager.getInstance().setKeystoreJsonData("");
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
        loadWalletPrivateKeyController.getHandler().onFocusOut();
        // TextField Initialize
        loadWalletPhaseFourTypePkNmController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "");
        loadWalletPhaseFourTypePkPwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        loadWalletPhaseFourTypePkCfController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");

        if(this.loadWalletPrivateKeyController.getCheckBtnType() == 3) {
            this.loadWalletPhaseFourTypePkLoad.setImage(loadGreyBtn);
            this.loadWalletPhaseFourTypePkLoad.setCursor(Cursor.HAND);
            this.introLoadWalletPhaseThreeTypePk.setVisible(false);
            this.introLoadWalletPhaseFourTypePk.setVisible(true);
            this.introNaviThree.setImage(introNaviCircle);
            this.introNaviFour.setImage(introNavi);
            this.introNaviThree.setFitWidth(6);
            this.introNaviFour.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(7);

            this.loadWalletPhaseFourTypePkNmController.requestFocus();
        }
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
        loadWalletPhaseFourTypePkNmController.getHandler().onFocusOut();
        loadWalletPhaseFourTypePkPwController.getHandler().onFocusOut();
        loadWalletPhaseFourTypePkCfController.getHandler().onFocusOut();

        if(loadWalletPhaseFourTypePkNmController.getCheckBtnType() == 3) {
            if(loadWalletPhaseFourTypePkPwController.getCheckBtnType() == 3) {
                if(loadWalletPhaseFourTypePkCfController.getCheckBtnType() == 3) {
                    String wName = loadWalletPhaseFourTypePkNmController.getText();
                    String wPasswd = loadWalletPhaseFourTypePkPwController.getText();
                    KeyStoreManager.getInstance().createKeystore(loadWalletPrivateKeyController.getText(), wName, wPasswd);

                    this.introLoadWalletPhaseFourTypePk.setVisible(false);
                    this.introPhaseOne.setVisible(true);
                    this.introNaviFour.setImage(introNaviCircle);
                    this.introNaviOne.setImage(introNavi);
                    this.introNaviFour.setFitWidth(6);
                    this.introNaviOne.setFitWidth(24);
                    this.introNoFour.setVisible(false);
                    this.introNaviFour.setVisible(false);
                    this.introPhaseTab.getSelectionModel().select(0);

                    AppManager.getInstance().guiFx.pageMoveMain();
                }
            }
        }
    }

    public static boolean getDeleteKeystoreFileFlag() {
        return DELETE_KEYSTORE_FILE_FLAG;
    }

    public static void setDeleteKeystoreFileFlag(boolean deleteKeystoreFileFlag) {
        DELETE_KEYSTORE_FILE_FLAG = deleteKeystoreFileFlag;
    }

    public void setPrevMain(boolean isPrevMain) {
        this.isPrevMain = isPrevMain;
        setVisibleHomeBtn(this.isPrevMain);
        KeyStoreManager.getInstance().clear();
    }

    public void homeBtnClicked() {
        KeyStoreManager.getInstance().deleteKeystore();
        AppManager.getInstance().guiFx.pageMoveMain();

    }
}