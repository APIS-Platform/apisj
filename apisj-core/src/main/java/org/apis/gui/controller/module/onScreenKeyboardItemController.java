package org.apis.gui.controller.module;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class onScreenKeyboardItemController implements Initializable {
    @FXML
    private Label itemLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemAddEventListener();
    }

    public void itemAddEventListener() {
        itemLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                itemLabel.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
            }
        });

        itemLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                itemLabel.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #ffffff; -fx-text-fill: #202020;");
            }
        });

        itemLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {itemLabel.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #910000; -fx-text-fill: #ffffff;");
            }
        });
    }

    public String getItemLabel() {
        return itemLabel.getText();
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel.setText(itemLabel);
    }

    public void setEmpty() {
        this.itemLabel.setStyle("");
    }

}
