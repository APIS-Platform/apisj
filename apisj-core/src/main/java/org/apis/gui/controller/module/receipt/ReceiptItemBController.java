package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptItemBController extends BaseViewController {

    @FXML private GridPane rootPane;
    @FXML private Label titleLabel, value;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setValue(String value){
        this.value.setText(value);
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
