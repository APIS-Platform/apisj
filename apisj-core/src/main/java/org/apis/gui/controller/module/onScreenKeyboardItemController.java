package org.apis.gui.controller.module;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class onScreenKeyboardItemController implements Initializable {
    @FXML
    private AnchorPane itemPane;
    @FXML
    private Label itemLabel;

    private boolean mouseFocusFlag = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemAddEventListener();
    }

    public void itemAddEventListener() {
        itemLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseFocusFlag = true;
                itemLabel.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
            }
        });

        itemLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseFocusFlag = false;
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

        itemLabel.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(mouseFocusFlag) {
                    itemLabel.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                            " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
                } else {
                    itemLabel.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                            " -fx-background-color: #ffffff; -fx-text-fill: #202020;");
                }
            }
        });
    }

    public String getItemLabel() {
        return this.itemLabel.getText();
    }

    public int getChildrenSize() {
        return this.itemPane.getChildren().size();
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel.setText(itemLabel);
    }

    public void setEmpty() {
        this.itemPane.getChildren().remove(itemLabel);
    }

    public void setItemConvert() {
        this.itemLabel.setText(String.valueOf((char)(getItemLabel().charAt(0)^32)));
    }

}
