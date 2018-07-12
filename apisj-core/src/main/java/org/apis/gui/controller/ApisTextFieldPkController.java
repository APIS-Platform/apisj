package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisTextFieldPkController implements Initializable {
    @FXML
    private ImageView createWalletPkCover;
    @FXML
    private Image passwordPrivate, passwordPublic;
    @FXML
    private TextField createWalletPkTextField;
    @FXML
    private PasswordField createWalletPkHiddenField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ApisTextFieldPkController");

        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");
    }

    @FXML
    public void togglePasswordFieldClick(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("createWalletPkCover")) {
            togglePasswordField();

            if(this.createWalletPkHiddenField.isVisible()) {
                this.createWalletPkCover.setImage(passwordPrivate);
            } else {
                this.createWalletPkCover.setImage(passwordPublic);
            }
        }else{

            System.out.println("else");
        }
    }

    public void togglePasswordField(){
        TextField textField = this.createWalletPkTextField;
        PasswordField passwordField = this.createWalletPkHiddenField;
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

    public void init() {
        this.createWalletPkTextField.setVisible(false);
        this.createWalletPkHiddenField.setVisible(true);
        this.createWalletPkHiddenField.setText("88888888888888888888888888888888");
        this.createWalletPkCover.setImage(passwordPrivate);
    }
}
