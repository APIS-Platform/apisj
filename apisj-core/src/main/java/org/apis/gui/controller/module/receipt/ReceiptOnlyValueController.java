package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptOnlyValueController extends BaseViewController {

    @FXML private GridPane rootPane;
    @FXML private Label valueLabel, symbolLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setValue(String value){
        if(value == null){
            value = "0.0";
        }
        this.valueLabel.setText(value);
    }

    public void setSymbol(String symbol){
        symbolLabel.setText(symbol);
    }

}
