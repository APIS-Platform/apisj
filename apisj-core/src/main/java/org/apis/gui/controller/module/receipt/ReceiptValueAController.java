package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptValueAController extends BaseViewController {

    @FXML private GridPane rootPane;
    @FXML private AnchorPane vSpace;
    @FXML private Label titleLabel, valueLabel, symbolLabel;

    private String address, mask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setValue(String value){
        if(value == null){
            value = "0.0";
        }
        this.valueLabel.setText(value);
    }


    public void setTitle(SimpleStringProperty title) {
        this.titleLabel.textProperty().unbind();
        this.titleLabel.textProperty().bind(title);
    }

    public void setLeftPadding(double leftPadding){
        this.rootPane.setPadding(new Insets(0,0,0,leftPadding));
        if(leftPadding > 0){
            vSpace.setPrefHeight(4);
        }
    }
}
