package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisTextFieldController implements Initializable {
    public static final int TEXTFIELD_TYPE_TEXT = 0;
    public static final int TEXTFIELD_TYPE_PASS = 1;

    private int textFieldType = TEXTFIELD_TYPE_TEXT;

    private String style = "-fx-background-insets: 0, 0 0 0 0; -fx-background-color: transparent; -fx-prompt-text-fill: #999999; " +
            "-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;";

    private ApisTextFieldControllerInterface handler;

    @FXML
    private TextField textField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ImageView coverBtn, checkBtn, messageImg;
    @FXML
    private GridPane message, textFieldGrid;
    @FXML
    private Pane borderLine;
    @FXML
    private Label messageLabel;

    private Image circleCrossGreyCheckBtn, circleCrossRedCheckBtn, greenCheckBtn, errorRed, passwordPublic, passwordPrivate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.out.println("initialize");

        circleCrossGreyCheckBtn = new Image("image/ic_circle_cross_grey@2x.png");
        circleCrossRedCheckBtn = new Image("image/ic_circle_cross_red@2x.png");
        errorRed = new Image("image/ic_error_red@2x.png");
        greenCheckBtn = new Image("image/ic_check@2x.png");
        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");

        textField.focusedProperty().addListener(textFieldListener);
        passwordField.focusedProperty().addListener(textFieldListener);

        init(TEXTFIELD_TYPE_PASS, "******************************");

    }

    private ChangeListener<Boolean> textFieldListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue) {
                if(textFieldType == TEXTFIELD_TYPE_TEXT);
            } else {
               onFocusOut();
                System.out.println("Textfield focus out!");
            }
        }
    };

    public interface ApisTextFieldControllerInterface {
        void onFocusOut();
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("coverBtn")){
            togglePasswordField();

            if(this.passwordField.isVisible()) {
                this.coverBtn.setImage(passwordPrivate);
            } else {
                this.coverBtn.setImage(passwordPublic);
            }

        }else if(fxid.equals("checkBtn")){

        }
    }

    private void onFocusOut(){
        if(handler != null){
            handler.onFocusOut();
        }
    }
    public void showMessage(String text){
        messageLabel.setText(text);
    }

    public void togglePasswordField(){
        if(textField.isVisible()){
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            textField.setVisible(false);
        } else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
        }
    }

    public void init(int type, String placeHolder){
        this.textFieldType = type;

        this.checkBtn.setVisible(false);
        this.borderLine.setStyle("-fx-background-color: #2b2b2b");
        this.message.setVisible(false);
        this.textField.setStyle(style+" -fx-text-fill: #2b2b2b;");
        this.passwordField.setStyle(style+" -fx-text-fill: #2b2b2b;");
        this.textField.setPromptText(placeHolder);
        this.passwordField.setPromptText(placeHolder);

        if(textFieldType == TEXTFIELD_TYPE_TEXT){
            this.textField.setText("");
            this.passwordField.setVisible(false);
            this.textField.setVisible(true);
            this.textField.setPadding(new Insets(0, 8, 0, 2));
            this.textFieldGrid.getChildren().remove(2);

        }else if(textFieldType == TEXTFIELD_TYPE_PASS){
            this.passwordField.setText("");
            this.textField.setVisible(false);
            this.passwordField.setVisible(true);
            this.coverBtn.setImage(passwordPrivate);
        }
    }

    public String getText(){ return this.textField.getText();}
    public void setHandler(ApisTextFieldControllerInterface handler){this.handler = handler;}
}
