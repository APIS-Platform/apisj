package org.apis.gui.controller.module;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ApisTextFieldController extends BaseViewController {
    public static final int TEXTFIELD_TYPE_TEXT = 0;
    public static final int TEXTFIELD_TYPE_PASS = 1;
    private int textFieldType = TEXTFIELD_TYPE_TEXT;

    public static final int CHECKBTN_TYPE_NONE = 0;
    public static final int CHECKBTN_TYPE_PROGRESS = 1;
    public static final int CHECKBTN_TYPE_FAIL = 2;
    public static final int CHECKBTN_TYPE_SUCCESS = 3;
    private int checkBtnType = CHECKBTN_TYPE_NONE;

    public static final boolean CHECKBTN_EXITED = false;
    public static final boolean CHECKBTN_ENTERED = true;
    private boolean checkBtnEnteredFlag = CHECKBTN_EXITED;

    // color theme {intro / other}
    public static final int THEME_TYPE_MAIN = 0;
    public static final int THEME_TYPE_INTRO = 1;
    private int themeType = THEME_TYPE_MAIN;

    private boolean[] pwValidationFlag = new boolean[3];
    private Pattern pwPatternLetters = Pattern.compile("[a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣]");
    private Pattern pwPatternNumbers = Pattern.compile("[0-9]");
    private Pattern pwPatternSpecials = Pattern.compile("[^a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣0-9]");
    private Pattern pkPatternValidation = Pattern.compile("[^0-9a-fA-F]");

    private ApisTextFieldControllerInterface handler;

    private Node removeNode;

    @FXML
    private TextField textField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ImageView coverBtn, checkBtn, messageImg, keyboardBtn;
    @FXML
    private GridPane message, textFieldGrid;
    @FXML
    private Pane borderLine;
    @FXML
    private Label messageLabel;
    @FXML
    private AnchorPane oskPane;

    private Image circleCrossGreyCheckBtn, circleCrossRedCheckBtn, greenCheckBtn, errorRed, passwordPublic, passwordPrivate,
                  keyboardBlack, keyboardGray;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        circleCrossGreyCheckBtn = new Image("image/ic_circle_cross_grey@2x.png");
        circleCrossRedCheckBtn = new Image("image/ic_circle_cross_red@2x.png");
        errorRed = new Image("image/ic_error_red@2x.png");
        greenCheckBtn = new Image("image/ic_check@2x.png");
        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");
        keyboardBlack = new Image("image/ic_keyboard_black.png");
        keyboardGray = new Image("image/ic_keyboard_gray.png");

        textField.focusedProperty().addListener(textFieldListener);
        passwordField.focusedProperty().addListener(textFieldListener);

        textField.textProperty().bindBidirectional(passwordField.textProperty());
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(handler != null){
                    handler.change(oldValue, newValue);
                }
            }
        });
        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(handler != null){
                    handler.change(oldValue, newValue);
                }
            }
        });

        init(TEXTFIELD_TYPE_PASS, "");
        Arrays.fill(pwValidationFlag, Boolean.FALSE);
    }

    private ChangeListener<Boolean> textFieldListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue) {
                onFocusIn();
            } else {
                onFocusOut();
            }
        }
    };

    @FXML
    private void onMouseClicked(InputEvent event){
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("keyboardBtn")) {
            System.out.println("%%%%%%%%%%%%%%%%On-Screan Keyboard%%%%%%%%%%%%");
            if(!oskPane.isVisible()) {
                keyboardBtn.setImage(keyboardBlack);
                oskPane.setPrefHeight(-1);
                oskPane.setPrefWidth(-1);
                oskPane.setVisible(true);

            } else {
                keyboardBtn.setImage(keyboardGray);
                oskPane.setPrefHeight(-1);
                oskPane.setPrefWidth(-1);
                oskPane.setVisible(false);
            }

        } else if(fxid.equals("coverBtn")){
            togglePasswordField();

            if(this.passwordField.isVisible()) {
                this.coverBtn.setImage(passwordPrivate);
            } else {
                this.coverBtn.setImage(passwordPublic);
            }

        }else if(fxid.equals("checkBtn")){

            if(this.checkBtnEnteredFlag == CHECKBTN_ENTERED){
                if(this.checkBtnType == CHECKBTN_TYPE_PROGRESS
                    || this.checkBtnType == CHECKBTN_TYPE_FAIL) {
                        this.textField.textProperty().setValue("");
                        this.passwordField.textProperty().setValue("");
                }
            }
        }
    }

    @FXML
    private void onMouseEntered(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("checkBtn")){
            this.checkBtnEnteredFlag = CHECKBTN_ENTERED;
        }
    }

    @FXML
    private void onMouseExited(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("checkBtn")){
            this.checkBtnEnteredFlag = CHECKBTN_EXITED;
        }
    }

    private void onFocusIn() {
        this.checkBtnType = CHECKBTN_TYPE_PROGRESS;

        this.textField.setStyle(new JavaFXStyle(this.textField.getStyle()).add("-fx-text-fill", "#2b2b2b").toString());
        this.passwordField.setStyle(new JavaFXStyle(this.passwordField.getStyle()).add("-fx-text-fill", "#2b2b2b").toString());
        this.checkBtn.setImage(circleCrossGreyCheckBtn);
        this.checkBtn.setCursor(Cursor.HAND);
        this.checkBtn.setVisible(true);

        // line color
        switch (themeType){
            case THEME_TYPE_MAIN : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#2b2b2b").toString()); break;
            case THEME_TYPE_INTRO : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#36b25b").toString()); break;
        }
    }

    private void onFocusOut(){
        if(handler != null){
            handler.onFocusOut();
        }
    }

    public void failedForm(String text){
        this.checkBtnType = CHECKBTN_TYPE_FAIL;

        // line color
        switch (themeType){
            case THEME_TYPE_MAIN : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#910000").toString()); break;
            case THEME_TYPE_INTRO : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#910000").toString()); break;
        }

        this.checkBtn.setImage(circleCrossRedCheckBtn);
        this.checkBtn.setCursor(Cursor.HAND);
        this.messageLabel.setText(text);
        this.message.setVisible(true);
    }

    public void succeededForm() {
        this.checkBtnType = CHECKBTN_TYPE_SUCCESS;

        // line color
        switch (themeType){
            case THEME_TYPE_MAIN : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#999999").toString()); break;
            case THEME_TYPE_INTRO : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#2b2b2b").toString()); break;
        }

        this.checkBtn.setImage(greenCheckBtn);
        this.checkBtn.setCursor(Cursor.DEFAULT);
        this.message.setVisible(false);
    }

    public boolean pwValidate(String password) {
        boolean result = false;
        int sum = 0;
        Arrays.fill(pwValidationFlag, Boolean.FALSE);

        if(pwPatternLetters.matcher(password).find()) {
            pwValidationFlag[0] = true;
        }
        if(pwPatternNumbers.matcher(password).find()) {
            pwValidationFlag[1] = true;
        }
        if(pwPatternSpecials.matcher(password).find()) {
            pwValidationFlag[2] = true;
        }

        for(int i=0; i<pwValidationFlag.length; i++) {
            if(pwValidationFlag[i] == true)
                sum++;
        }

        if(sum == 3) {
            result = true;
        }

        return result;
    }

    public boolean pkValidate(String privateKey) {
        boolean result = false;

        if(pkPatternValidation.matcher(privateKey).find()) {
            result = true;
        }

        return result;
    }

    public void togglePasswordField(){
        if(textField.isVisible()){
            passwordField.setVisible(true);
            textField.setVisible(false);
            passwordField.requestFocus();
        } else {
            textField.setVisible(true);
            passwordField.setVisible(false);
            textField.requestFocus();
        }
    }

    public void init(int type, String placeHolder){
        this.textFieldType = type;
        this.checkBtnType = CHECKBTN_TYPE_NONE;

        this.checkBtn.setVisible(false);
        this.message.setVisible(false);
        this.textField.setStyle(new JavaFXStyle(this.textField.getStyle()).add("-fx-text-fill", "#2b2b2b").toString());
        this.passwordField.setStyle(new JavaFXStyle(this.passwordField.getStyle()).add("-fx-text-fill", "#2b2b2b").toString());
        this.textField.setPromptText(placeHolder);
        this.passwordField.setPromptText(placeHolder);

        // line color
        switch (themeType){
            case THEME_TYPE_MAIN : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#999999").toString()); break;
            case THEME_TYPE_INTRO : this.borderLine.setStyle(new JavaFXStyle(this.borderLine.getStyle()).add("-fx-background-color", "#2b2b2b").toString()); break;
        }

        if(textFieldType == TEXTFIELD_TYPE_TEXT){
            this.textField.textProperty().setValue("");
            this.passwordField.setVisible(false);
            this.textField.setVisible(true);
            this.textField.setPadding(new Insets(0, 8, 0, 2));
            if(removeNode == null){
                this.textFieldGrid.getChildren().remove(2);
                removeNode = this.textFieldGrid.getChildren().remove(3);
            }

        }else if(textFieldType == TEXTFIELD_TYPE_PASS){
            this.passwordField.textProperty().setValue("");
            this.textField.setVisible(false);
            this.passwordField.setVisible(true);
            this.keyboardBtn.setImage(keyboardGray);
            this.coverBtn.setImage(passwordPrivate);
        }
    }

    public void init(int type, String placeHolder, int themeType){
        this.themeType = themeType;
        init(type, placeHolder);
    }

    public void setText(String text) {
        this.textField.textProperty().setValue(text);
        this.passwordField.textProperty().setValue(text);
    }
    public void setHandler(ApisTextFieldControllerInterface handler){ this.handler = handler; }

    public boolean getCheckBtnEnteredFlag() { return this.checkBtnEnteredFlag; }
    public String getText(){ return this.textField.getText().trim();}
    public int getCheckBtnType() { return this.checkBtnType; }
    public ApisTextFieldControllerInterface getHandler() { return this.handler; }

    public void requestFocus() {
        this.passwordField.requestFocus();
    }

    public interface ApisTextFieldControllerInterface {
        void onFocusOut();
        void change(String old_text, String new_text);
    }
}
