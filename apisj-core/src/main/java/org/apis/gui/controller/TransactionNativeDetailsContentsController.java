package org.apis.gui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsContentsController implements Initializable {
    @FXML
    private AnchorPane bgAnchor;
    @FXML
    private Label contentsHeader, contentsBody;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void bindContentsHeader(SimpleStringProperty contentsHeader) {
        this.contentsHeader.textProperty().bind(contentsHeader);
    }

    public void setContentsBody(String contentsBody) {
        this.contentsBody.setText(contentsBody);
    }

    public void setBgColor(String bgColor) {
        this.bgAnchor.setStyle("-fx-background-color: "+bgColor+";");
    }

    public void setTxtColor(String txtColor) {
        this.contentsBody.setStyle("-fx-text-fill: "+txtColor+"; -fx-font-family: 'Open Sans Regular'; -fx-font-size:12px;");
    }
}
