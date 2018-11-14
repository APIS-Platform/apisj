package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressInfoController extends BaseViewController {
    @FXML private TextField searchText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchText.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                searchText.setStyle(new JavaFXStyle(searchText.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
            }
        });

        searchText.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(searchText.isFocused() == true){
                    searchText.setStyle(new JavaFXStyle(searchText.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
                }else{
                    searchText.setStyle(new JavaFXStyle(searchText.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                }
            }
        });

        searchText.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // focus in
                if(newValue){
                    searchText.setStyle(new JavaFXStyle(searchText.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
                }else{
                    searchText.setStyle(new JavaFXStyle(searchText.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                }
            }
        });
    }

    public void update(){

    }

    public void exit(){
        if(handler != null){
            handler.close();
        }
    }

    private AddressInfoImpl handler;
    public void setHandler(AddressInfoImpl handler){
        this.handler = handler;
    }
    public interface AddressInfoImpl{
        void close();
    }
}
