package org.apis.gui.controller;

import com.sun.org.apache.xml.internal.security.Init;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisTagItem implements Initializable {
    @FXML
    private Label text;
    @FXML
    private ImageView btnClose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("onMouseClicked : "+id);
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("onMouseEntered : "+id);
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("onMouseEntered : "+id);
    }
}
