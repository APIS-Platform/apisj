package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class SlideButtonController implements Initializable {
    public static final boolean BUTTON_OFF = false;
    public static final boolean BUTTON_ON = true;
    private boolean buttonStatus = BUTTON_OFF;

    @FXML
    private Pane backPane;
    @FXML
    private GridPane frontGrid;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonOff();
    }

    public void init(Boolean status) {
        if(status) {
            buttonOn();
        } else {
            buttonOff();
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("slideBtn")){
            setSelected(!buttonStatus);
        }
    }

    private void buttonOff() {
        backPane.setStyle("-fx-border-radius : 10 10 10 10; -fx-background-radius: 10 10 10 10; -fx-background-color: #999999;");
        frontGrid.setHalignment(frontGrid, HPos.LEFT);
        buttonStatus = BUTTON_OFF;
    }

    private void buttonOn() {
        backPane.setStyle("-fx-border-radius : 10 10 10 10; -fx-background-radius: 10 10 10 10; -fx-background-color: #910000;");
        frontGrid.setHalignment(frontGrid, HPos.RIGHT);
        buttonStatus = BUTTON_ON;
    }

    public boolean isSelected() {
        return buttonStatus;
    }

    public void setSelected(boolean status) {
        this.buttonStatus = status;
        if(buttonStatus) {
            buttonOn();
        } else {
            buttonOff();
        }
    }

}
