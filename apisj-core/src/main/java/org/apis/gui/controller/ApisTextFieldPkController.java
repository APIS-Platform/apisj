package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisTextFieldPkController implements Initializable {
    private String style = "-fx-background-insets: 0, 0 0 0 0; -fx-background-color: transparent; -fx-prompt-text-fill: #999999; " +
            "-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;";

    @FXML
    private ImageView createWalletPkCover;
    @FXML
    private Image passwordPrivate, passwordPublic;
    @FXML
    private TextField createWalletPkTextField;
    @FXML
    private PasswordField createWalletPkHiddenField;
    @FXML
    private Pane borderLine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ApisTextFieldPkController");

        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");
    }

    @FXML
    public void togglePasswordFieldClick(InputEvent event) {
        String fxid = ((ImageView)event.getSource()).getId();

        if(fxid.equals("createWalletPkCover")) {
            togglePasswordField();

            if(this.createWalletPkHiddenField.isVisible()) {
                this.createWalletPkCover.setImage(passwordPrivate);
                this.createWalletPkHiddenField.setStyle(style + "-fx-text-fill: #999999;");
                this.borderLine.setStyle("-fx-background-color: #999999;");
            } else {
                this.createWalletPkCover.setImage(passwordPublic);
                this.createWalletPkTextField.setStyle(style + " -fx-text-fill: #2b2b2b;");
                this.borderLine.setStyle("-fx-background-color: #2b2b2b;");
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
        this.createWalletPkHiddenField.setStyle(style + "-fx-text-fill: #999999;");
        this.borderLine.setStyle("-fx-background-color: #999999;");
        this.createWalletPkCover.setImage(passwordPrivate);
    }

    public void copy() {
        String text = createWalletPkHiddenField.getText();
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public void print() {
        Stage printStage = new Stage();
        Parent rootPrint;

        try {
            rootPrint = FXMLLoader.load(getClass().getResource("/scene/apis_textfield.fxml"));
            printStage.initModality(Modality.APPLICATION_MODAL);
            printStage.setTitle("Print Private Key");
            printStage.setScene(new Scene(rootPrint, 400, 300));
            printStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
