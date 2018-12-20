package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptValueBController extends BaseViewController {

    @FXML private GridPane rootPane;
    @FXML private Label titleLabel, value1, value2, unit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setValue(String value){
        if(value == null){
            value = "0.0";
        }
        String valueSplit[] = value.split("\\.");
        this.value1.setText(valueSplit[0]);
        if(valueSplit.length > 1){
            this.value2.setText("."+valueSplit[1]);
        }
    }

    public void setTitle(SimpleStringProperty title) {
        this.titleLabel.textProperty().unbind();
        this.titleLabel.textProperty().bind(title);
    }

    public void setLeftPadding(double leftPadding){
        this.rootPane.setPadding(new Insets(0,0,0,leftPadding));
        if(leftPadding > 0){
        }
    }
}
