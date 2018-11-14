package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressInfoItemController extends BaseViewController {

    @FXML private GridPane rootPane;
    @FXML private Label title, label, hexData;
    @FXML private TextField readOnly, readOnlyHexData;
    @FXML private AnchorPane readOnlyPane, readOnlyHexDataPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setTitle(String title){
        this.title.setText(title);

        this.label.setVisible(false);
        this.hexData.setVisible(false);
        this.readOnlyPane.setVisible(false);
        this.readOnlyHexDataPane.setVisible(false);
    }

    public void setLabel(String label){
        this.label.setText(label);

        this.label.setVisible(true);
        this.hexData.setVisible(false);
        this.readOnlyPane.setVisible(false);
        this.readOnlyHexDataPane.setVisible(false);
    }

    public void setHexData(String hexData){
        this.hexData.setText(hexData);

        this.label.setVisible(false);
        this.hexData.setVisible(true);
        this.readOnlyPane.setVisible(false);
        this.readOnlyHexDataPane.setVisible(false);
    }

    public void setReadOnly(String readOnly){
        this.readOnly.setText(readOnly);

        this.label.setVisible(false);
        this.hexData.setVisible(false);
        this.readOnlyPane.setVisible(true);
        this.readOnlyHexDataPane.setVisible(false);
    }

    public void setReadOnlyHexData(String readOnlyHexData){
        this.readOnlyHexData.setText(readOnlyHexData);

        this.label.setVisible(false);
        this.hexData.setVisible(false);
        this.readOnlyPane.setVisible(false);
        this.readOnlyHexDataPane.setVisible(true);
    }

    public void setBackground(String hexColor){
        this.rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color",hexColor).toString());
    }
}
