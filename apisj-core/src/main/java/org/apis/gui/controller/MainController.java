package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Label label1, label2, label3, label4, label5;
    @FXML
    private Pane linePane1, linePane2, linePane3, linePane4, linePane5;
    @FXML
    private TabPane tabPane;
    @FXML
    private GridPane popupLayout1, popupLayout2;

    private ArrayList<Label> labels = new ArrayList<>();
    private ArrayList<Pane> lines = new ArrayList<>();


    public void initLayoutHeader(){
        this.labels.add(this.label1);
        this.labels.add(this.label2);
        this.labels.add(this.label3);
        this.labels.add(this.label4);
        this.labels.add(this.label5);

        this.lines.add(this.linePane1);
        this.lines.add(this.linePane2);
        this.lines.add(this.linePane3);
        this.lines.add(this.linePane4);
        this.lines.add(this.linePane5);
    }
    public void setHeaderActive(int index){

        for(int i=0;i<this.labels.size(); i++){
            this.labels.get(i).setTextFill(Color.web("#999999"));
            this.labels.get(i).setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        }
        for(int i=0;i<this.lines.size(); i++){
            this.lines.get(i).setVisible(false);
        }

        if(index >= 0 && index < this.labels.size()){
            this.labels.get(index).setTextFill(Color.web("#910000"));
            this.labels.get(index).setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
        if(index >= 0 && index < this.lines.size()){
            this.lines.get(index).setVisible(true);
        }
    }
    public void selectedHeader(int index){

        // change header active
        setHeaderActive(index);

        // change tab pane
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(index);
    }

    @FXML
    private void onClickTabEvent(InputEvent event){
        String id = ((AnchorPane)event.getSource()).getId();
        if(id.equals("tab1")) {
            selectedHeader(0);
        }else if(id.equals("tab2")) {
            selectedHeader(1);
        }else if(id.equals("tab3")) {
            selectedHeader(2);
        }else if(id.equals("tab4")) {
            selectedHeader(3);
        }else if(id.equals("tab5")) {
            selectedHeader(4);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLayoutHeader();

        selectedHeader(0);

        AppManager.getInstance().guiFx.setPopupLayer1(popupLayout1);
        AppManager.getInstance().guiFx.setPopupLayer2(popupLayout2);

    }
}
