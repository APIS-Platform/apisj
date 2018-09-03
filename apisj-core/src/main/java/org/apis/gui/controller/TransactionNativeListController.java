package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeListController implements Initializable {
    @FXML
    private Label hash, from, to, block;

    private TransactionNativeListImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Underline Setting
        hash.setOnMouseEntered(event -> hash.setUnderline(true));
        from.setOnMouseEntered(event -> from.setUnderline(true));
        to.setOnMouseEntered(event -> to.setUnderline(true));
        hash.setOnMouseExited(event -> hash.setUnderline(false));
        from.setOnMouseExited(event -> from.setUnderline(false));
        to.setOnMouseExited(event -> to.setUnderline(false));

    }

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("hash")) {
            this.handler.showDetails();

        } else if(fxid.equals("from")) {
            this.handler.showDetails();

        } else if(fxid.equals("to")) {
            this.handler.showDetails();

        }
    }

    public interface TransactionNativeListImpl {
        void showDetails();
    }

    public void setHandler(TransactionNativeListImpl handler) {
        this.handler = handler;
    }

}
