package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;
import sun.java2d.loops.FillPath;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupMyAddressGroup implements Initializable {

    private ArrayList<String> groupList = new ArrayList<>();

    @FXML
    private FlowPane list;
    @FXML
    private TextField groupText;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(1); }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnAdd")){
            groupList.add(groupText.getText());

            String text = groupText.getText();
            Label label = new Label(text);
            label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-border-color:#d8d8d8; ");
            label.setPadding(new Insets(5, 10, 5, 10));
            label.setTextFill(Color.rgb(153,153,153));
            label.setMaxWidth( 200 );

            list.getChildren().add(label);

            groupText.setText("");
        }
    }
}
