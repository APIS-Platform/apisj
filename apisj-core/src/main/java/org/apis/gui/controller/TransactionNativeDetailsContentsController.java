package org.apis.gui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsContentsController implements Initializable {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label contentsHeader, contentsBody;
    @FXML private GridPane gridPane;
    @FXML private TextArea textArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        textArea.setVisible(false);
    }

    public void bindContentsHeader(SimpleStringProperty contentsHeader) {
        this.contentsHeader.textProperty().bind(contentsHeader);
    }

    public void setContentsBody(String contentsBody) {
        this.contentsBody.setText(contentsBody);
        this.textArea.setText(contentsBody);
    }

    public void setBgColor(String bgColor) {
        this.bgAnchor.setStyle("-fx-background-color: "+bgColor+";");
    }

    public void setTxtColor(String txtColor) {
        this.contentsBody.setStyle("-fx-text-fill: "+txtColor+"; -fx-font-family: 'Open Sans Regular'; -fx-font-size:12px;");
    }

    public void setHeight(int height){
        gridPane.setPrefHeight(height);
    }

    public void setTextAreaType(int height) {
        setHeight(height);
        textArea.setVisible(true);

    }
}
