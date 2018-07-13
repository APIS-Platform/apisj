package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {
    // Load Wallet Radio Button Values
    private static final int LOAD_WALLET_SELECT_WALLET_FILE = 1;
    private static final int LOAD_WALLET_PRIVATE_KEY = 2;

    // Download Keystore File Flag
    private static boolean DOWNLOAD_KEYSTORE_FILE_FLAG = false;

    private int loadWalletPhaseTwoFlag = LOAD_WALLET_SELECT_WALLET_FILE;

    // Link to FXML Controls
    @FXML
    private ImageView hexagonCreateWalletBtn, hexagonLoadWalletBtn, hexagonCreateWalletLabelImg, hexagonLoadWalletLabelImg;
    @FXML
    private ImageView createWalletPhaseTwoNext, createWalletPhaseThreeNext, loadWalletPhaseThreeTypeFileLoad, loadWalletPhaseThreeTypePkNext, loadWalletPhaseFourTypePkLoad;
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

    private Image createBtnImgOn, createBtnImgOff, loadBtnImgOn, loadBtnImgOff;
    private Image createLabelOn, createLabelOff, loadLabelOn, loadLabelOff;
    private Image introNavi, introNaviCircle;
    private Image nextGreyBtn, nextRedBtn, loadGreyBtn, loadRedBtn;
    private Image radioCheckBtnRed, radioCheckBtnGrey;

    // External GUI and Controller add
    @FXML
    private ApisTextFieldPkController apisTextFieldPkController;

    @FXML
    public ApisTextFieldController createWalletPhaseTwoWalletNameController, createWalletPhaseTwoWalletPasswordController, createWalletPhaseTwoConfirmPasswordController,
                                   loadWalletPhaseThreeTypeFilePwController, loadWalletPrivateKeyController, loadWalletPhaseFourTypePkNmController,
                                   loadWalletPhaseFourTypePkPwController, loadWalletPhaseFourTypePkCfController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("introController check.");

        // Hide Home Button when the first access
        this.introHomeBtn.setVisible(false);
        this.introHomeBtn.setFitWidth(1);

        // Tab Pane Direction Key Block
        introPhaseTab.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.TAB
                || event.getCode() == KeyCode.LEFT
                || event.getCode() == KeyCode.RIGHT
                || event.getCode() == KeyCode.UP
                || event.getCode() == KeyCode.DOWN) {
                    event.consume();
            }
        });

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
        nextGreyBtn = new Image("image/btn_next_grey@2x.png");
        nextRedBtn = new Image("image/btn_next_red@2x.png");
        loadGreyBtn = new Image("image/btn_load_grey@2x.png");
        loadRedBtn = new Image("image/btn_load_red@2x.png");
        radioCheckBtnRed = new Image("image/btn_check_red@2x.png");
        radioCheckBtnGrey = new Image("image/btn_check_grey@2x.png");

        // Create Wallet Phase 2 Textfield Validation Work
        createWalletPhaseTwoWalletNameController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
        createWalletPhaseTwoWalletNameController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (createWalletPhaseTwoWalletNameController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoWalletNameController.setText("");
                }

                text = createWalletPhaseTwoWalletNameController.getText();

                if (text == null || text.equals("")) {
                    createWalletPhaseTwoWalletNameController.failedForm("Enter new wallet name.");
                } else {
                    createWalletPhaseTwoWalletNameController.succeededForm();
                }

                createWalletPhaseTwoActivateNext();
            }
        });

        createWalletPhaseTwoWalletPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        createWalletPhaseTwoWalletPasswordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(createWalletPhaseTwoWalletPasswordController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoWalletPasswordController.setText("");
                }

                text = createWalletPhaseTwoWalletPasswordController.getText();

                if(text == null || text.equals("")) {
                    createWalletPhaseTwoWalletPasswordController.failedForm("Please enter your password.");
                } else if(text.length() < 8) {
                    createWalletPhaseTwoWalletPasswordController.failedForm("Password must contain at least 8 characters.");
                } else if(!createWalletPhaseTwoWalletPasswordController.pwValidate(text)) {
                    createWalletPhaseTwoWalletPasswordController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    createWalletPhaseTwoWalletPasswordController.succeededForm();
                }

                if(!createWalletPhaseTwoConfirmPasswordController.getText().isEmpty()) {
                    createWalletPhaseTwoConfirmPasswordController.getHandler().onFocusOut();
                }

                createWalletPhaseTwoActivateNext();
            }
        });

        createWalletPhaseTwoConfirmPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
        createWalletPhaseTwoConfirmPasswordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnEnteredFlag()) {
                    createWalletPhaseTwoConfirmPasswordController.setText("");
                }

                text = createWalletPhaseTwoConfirmPasswordController.getText();

                if(text == null || text.equals("")) {
                    createWalletPhaseTwoConfirmPasswordController.failedForm("Please check your password.");
                } else if(!text.equals(createWalletPhaseTwoWalletPasswordController.getText())) {
                    createWalletPhaseTwoConfirmPasswordController.failedForm("Password does not match the confirm password.");
                } else {
                    createWalletPhaseTwoConfirmPasswordController.succeededForm();
                }

                createWalletPhaseTwoActivateNext();
            }
        });

        // Load Wallet Phase 3 Type File Password Validation
        loadWalletPhaseThreeTypeFilePwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        loadWalletPhaseThreeTypeFilePwController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;
                loadWalletPhaseThreeTypeFileLoad.setImage(loadGreyBtn);
                loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.DEFAULT);

                if(loadWalletPhaseThreeTypeFilePwController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseThreeTypeFilePwController.setText("");
                }

                text = loadWalletPhaseThreeTypeFilePwController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseThreeTypeFilePwController.failedForm("Please enter your password.");
                } else if(text.length() < 8) {
                    loadWalletPhaseThreeTypeFilePwController.failedForm("Password must contain at least 8 characters.");
                } else if(!loadWalletPhaseThreeTypeFilePwController.pwValidate(text)) {
                    loadWalletPhaseThreeTypeFilePwController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    loadWalletPhaseThreeTypeFilePwController.succeededForm();
                    loadWalletPhaseThreeTypeFileLoad.setImage(loadRedBtn);
                    loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.HAND);
                }
            }
        });

        // Load Wallet Phase 3 Type Private Key Validation
        loadWalletPrivateKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
        loadWalletPrivateKeyController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;
                loadWalletPhaseThreeTypePkNext.setImage(nextGreyBtn);
                loadWalletPhaseThreeTypePkNext.setCursor(Cursor.DEFAULT);

                if(loadWalletPrivateKeyController.getCheckBtnEnteredFlag()) {
                    loadWalletPrivateKeyController.setText("");
                }

                text = loadWalletPrivateKeyController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPrivateKeyController.failedForm("Please enter your private key.");
                } else if(text.length() != 64) {
                    loadWalletPrivateKeyController.failedForm("Incorrect private key.");
                } else {
                    loadWalletPrivateKeyController.succeededForm();
                    loadWalletPhaseThreeTypePkNext.setImage(nextRedBtn);
                    loadWalletPhaseThreeTypePkNext.setCursor(Cursor.HAND);
                }
            }
        });

        // Load Wallet Phase 4 TextField Validation Work
        loadWalletPhaseFourTypePkNmController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
        loadWalletPhaseFourTypePkNmController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (loadWalletPhaseFourTypePkNmController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkNmController.setText("");
                }

                text = loadWalletPhaseFourTypePkNmController.getText();

                if (text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkNmController.failedForm("Enter new wallet name.");
                } else {
                    loadWalletPhaseFourTypePkNmController.succeededForm();
                }

                loadWalletPhaseFourActivateLoad();
            }
        });

        loadWalletPhaseFourTypePkPwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        loadWalletPhaseFourTypePkPwController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(loadWalletPhaseFourTypePkPwController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkPwController.setText("");
                }

                text = loadWalletPhaseFourTypePkPwController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkPwController.failedForm("Please enter your password.");
                } else if(text.length() < 8) {
                    loadWalletPhaseFourTypePkPwController.failedForm("Password must contain at least 8 characters.");
                } else if(!loadWalletPhaseFourTypePkPwController.pwValidate(text)) {
                    loadWalletPhaseFourTypePkPwController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    loadWalletPhaseFourTypePkPwController.succeededForm();
                }

                if(!loadWalletPhaseFourTypePkCfController.getText().isEmpty()) {
                    loadWalletPhaseFourTypePkCfController.getHandler().onFocusOut();
                }

                loadWalletPhaseFourActivateLoad();
            }
        });

        loadWalletPhaseFourTypePkCfController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
        loadWalletPhaseFourTypePkCfController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(loadWalletPhaseFourTypePkCfController.getCheckBtnEnteredFlag()) {
                    loadWalletPhaseFourTypePkCfController.setText("");
                }

                text = loadWalletPhaseFourTypePkCfController.getText();

                if(text == null || text.equals("")) {
                    loadWalletPhaseFourTypePkCfController.failedForm("Please check your password.");
                } else if(!text.equals(loadWalletPhaseFourTypePkPwController.getText())) {
                    loadWalletPhaseFourTypePkCfController.failedForm("Password does not match the confirm password.");
                } else {
                    loadWalletPhaseFourTypePkCfController.succeededForm();
                }

                loadWalletPhaseFourActivateLoad();
            }
        });
    }

    // Next button Control
    public void createWalletPhaseTwoActivateNext() {
        createWalletPhaseTwoNext.setImage(nextGreyBtn);
        createWalletPhaseTwoNext.setCursor(Cursor.DEFAULT);

        if(createWalletPhaseTwoWalletNameController.getCheckBtnType() == 3) {
            if(createWalletPhaseTwoWalletPasswordController.getCheckBtnType() == 3) {
                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnType() == 3) {
                    createWalletPhaseTwoNext.setImage(nextRedBtn);
                    createWalletPhaseTwoNext.setCursor(Cursor.HAND);
                }
            }
        }
    }

    public void loadWalletPhaseFourActivateLoad() {
        loadWalletPhaseFourTypePkLoad.setImage(loadGreyBtn);
        loadWalletPhaseFourTypePkLoad.setCursor(Cursor.DEFAULT);

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
        createWalletPhaseTwoWalletNameController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
        createWalletPhaseTwoWalletPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        createWalletPhaseTwoConfirmPasswordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
        createWalletPhaseTwoNext.setImage(nextGreyBtn);
        createWalletPhaseTwoNext.setCursor(Cursor.DEFAULT);
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
        createWalletPhaseTwoWalletNameController.getHandler().onFocusOut();
        createWalletPhaseTwoWalletPasswordController.getHandler().onFocusOut();
        createWalletPhaseTwoConfirmPasswordController.getHandler().onFocusOut();

        if(createWalletPhaseTwoWalletNameController.getCheckBtnType() == 3) {
            if(createWalletPhaseTwoWalletPasswordController.getCheckBtnType() == 3) {
                if(createWalletPhaseTwoConfirmPasswordController.getCheckBtnType() == 3) {
                    this.createWalletPhaseThreeNext.setImage(nextGreyBtn);
                    this.createWalletPhaseThreeNext.setCursor(Cursor.DEFAULT);
                    this.introCreateWalletPhaseTwo.setVisible(false);
                    this.introCreateWalletPhaseThree.setVisible(true);
                    this.introNaviTwo.setImage(introNaviCircle);
                    this.introNaviThree.setImage(introNavi);
                    this.introNaviTwo.setFitWidth(6);
                    this.introNaviThree.setFitWidth(24);
                    this.introPhaseTab.getSelectionModel().select(2);
                }
            }
        }
    }

    public void createWalletPhaseThreeBackClick() {
        this.DOWNLOAD_KEYSTORE_FILE_FLAG = false;
        this.introCreateWalletPhaseThree.setVisible(false);
        this.introCreateWalletPhaseTwo.setVisible(true);
        this.introNaviThree.setImage(introNaviCircle);
        this.introNaviTwo.setImage(introNavi);
        this.introNaviThree.setFitWidth(6);
        this.introNaviTwo.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(1);
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

            this.apisTextFieldPkController.init();
        } else {
            this.introModalBackground.setVisible(true);
            this.downloadKeystoreCaution.setVisible(true);
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
        // Create Wallet Complete
        this.introCreateWalletPhaseFour.setVisible(false);
        this.introPhaseOne.setVisible(true);
        this.introNaviFour.setImage(introNaviCircle);
        this.introNaviOne.setImage(introNavi);
        this.introNaviFour.setFitWidth(6);
        this.introNaviOne.setFitWidth(24);
        this.introPhaseTab.getSelectionModel().select(0);
    }

    public void createWalletDownloadKeystoreFile() {
        this.DOWNLOAD_KEYSTORE_FILE_FLAG = true;
        this.createWalletPhaseThreeNext.setImage(nextRedBtn);
        this.createWalletPhaseThreeNext.setCursor(Cursor.HAND);
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


    // Load Wallet Phases
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
            this.loadWalletPhaseThreeTypeFileLoad.setImage(loadGreyBtn);
            this.loadWalletPhaseThreeTypeFileLoad.setCursor(Cursor.DEFAULT);
            this.introLoadWalletPhaseTwo.setVisible(false);
            this.introLoadWalletPhaseThreeTypeFile.setVisible(true);
            this.introNaviTwo.setImage(introNaviCircle);
            this.introNaviThree.setImage(introNavi);
            this.introNaviTwo.setFitWidth(6);
            this.introNaviThree.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(5);

            // TextField Initialize
            loadWalletPhaseThreeTypeFilePwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        } else if(loadWalletPhaseTwoFlag == LOAD_WALLET_PRIVATE_KEY) {
            this.loadWalletPhaseThreeTypePkNext.setImage(nextGreyBtn);
            this.loadWalletPhaseThreeTypePkNext.setCursor(Cursor.DEFAULT);
            this.introLoadWalletPhaseTwo.setVisible(false);
            this.introLoadWalletPhaseThreeTypePk.setVisible(true);
            this.introNaviTwo.setImage(introNaviCircle);
            this.introNaviThree.setImage(introNavi);
            this.introNaviTwo.setFitWidth(6);
            this.introNaviThree.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(6);

            // TextField Initialize
            loadWalletPrivateKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");
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
        loadWalletPrivateKeyController.getHandler().onFocusOut();
        // TextField Initialize
        loadWalletPhaseFourTypePkNmController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
        loadWalletPhaseFourTypePkPwController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        loadWalletPhaseFourTypePkCfController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "");

        if(this.loadWalletPrivateKeyController.getCheckBtnType() == 3) {
            this.loadWalletPhaseFourTypePkLoad.setImage(loadGreyBtn);
            this.loadWalletPhaseFourTypePkLoad.setCursor(Cursor.DEFAULT);
            this.introLoadWalletPhaseThreeTypePk.setVisible(false);
            this.introLoadWalletPhaseFourTypePk.setVisible(true);
            this.introNaviThree.setImage(introNaviCircle);
            this.introNaviFour.setImage(introNavi);
            this.introNaviThree.setFitWidth(6);
            this.introNaviFour.setFitWidth(24);
            this.introPhaseTab.getSelectionModel().select(7);
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
                    this.loadWalletPhaseFourTypePkLoad.setImage(loadGreyBtn);
                    this.loadWalletPhaseFourTypePkLoad.setCursor(Cursor.DEFAULT);
                    this.introLoadWalletPhaseFourTypePk.setVisible(false);
                    this.introPhaseOne.setVisible(true);
                    this.introNaviFour.setImage(introNaviCircle);
                    this.introNaviOne.setImage(introNavi);
                    this.introNaviFour.setFitWidth(6);
                    this.introNaviOne.setFitWidth(24);
                    this.introNoFour.setVisible(false);
                    this.introNaviFour.setVisible(false);
                    this.introPhaseTab.getSelectionModel().select(0);
                }
            }
        }
    }

}