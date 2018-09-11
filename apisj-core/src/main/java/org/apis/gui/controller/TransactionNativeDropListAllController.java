package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDropListAllController implements Initializable {
    @FXML
    private AnchorPane bgAnchor;
    @FXML
    private Label selectAllLabel;

    private TransactionNativeDropListAllImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Background Color Setting
        bgAnchor.setOnMouseEntered(event -> bgAnchor.setStyle("-fx-background-color: #f2f2f2;"));
        bgAnchor.setOnMouseExited(event -> bgAnchor.setStyle("-fx-background-color: transparent;"));
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("bgAnchor")) {
            if(handler != null) {
                handler.setDropLabel();
            }
        }
    }

    public String getSelectAllLabel() {
        return selectAllLabel.getText();
    }

    public void setSelectAllLabel(String selectAllLabel) {
        this.selectAllLabel.setText(selectAllLabel);
    }

    public void setHandler(TransactionNativeDropListAllImpl handler) {
        this.handler = handler;
    }

    public interface TransactionNativeDropListAllImpl {
        void setDropLabel();
    }
}
