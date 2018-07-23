package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractController implements Initializable {

    @FXML
    private Label tabLabel1, tabLabel2, tabLabel3, tabLabel4, sideTabLabel1, sideTabLabel2;
    @FXML
    private Pane tabLinePane1, tabLinePane2, tabLinePane3, tabLinePane4, sideTabLinePane1, sideTabLinePane2;
    @FXML
    private AnchorPane tab1, tab2, tab3, tab4, sideTab1, sideTab2;
    @FXML
    private VBox pSelectList, pSelectChild;
    @FXML
    private GridPane pSelectHead, pSelectItem100, pSelectItem75, pSelectItem50, pSelectItem25, pSelectItem10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTabClean();
        initSideTabClean();

        this.tabLabel1.setTextFill(Color.web("#910000"));
        this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
        this.tabLinePane1.setVisible(true);
        this.sideTabLabel1.setTextFill(Color.web("#910000"));
        this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        this.sideTabLinePane1.setVisible(true);

        hidePercentSelectBox();

    }

    public void contractReadWritePopup() {
        AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_create.fxml", 0);
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("pSelectHead")){
            if(this.pSelectList.isVisible() == true){
                hidePercentSelectBox();
            }else{
                showPercentSelectBox();
            }

        } else if(fxid.equals("tab1")) {
            initTab(0);

        } else if(fxid.equals("tab2")) {
            initTab(1);

        } else if(fxid.equals("tab3")) {
            initTab(2);

        } else if(fxid.equals("tab4")) {
            initTab(3);

        } else if(fxid.equals("sideTab1")) {
            initSideTab(0);

        } else if(fxid.equals("sideTab2")) {
            initSideTab(1);

        }

    }

    @FXML
    private void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("pSelectItem100")){
            pSelectItem100.setStyle("-fx-background-color : #f2f2f2");
        }else if(id.equals("pSelectItem75")){
            pSelectItem75.setStyle("-fx-background-color : #f2f2f2");
        }else if(id.equals("pSelectItem50")){
            pSelectItem50.setStyle("-fx-background-color : #f2f2f2");
        }else if(id.equals("pSelectItem25")){
            pSelectItem25.setStyle("-fx-background-color : #f2f2f2");
        }else if(id.equals("pSelectItem10")){
            pSelectItem10.setStyle("-fx-background-color : #f2f2f2");
        }
    }

    @FXML
    private void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("pSelectItem100")){
            pSelectItem100.setStyle("-fx-background-color : #ffffff");
        }else if(id.equals("pSelectItem75")){
            pSelectItem75.setStyle("-fx-background-color : #ffffff");
        }else if(id.equals("pSelectItem50")){
            pSelectItem50.setStyle("-fx-background-color : #ffffff");
        }else if(id.equals("pSelectItem25")){
            pSelectItem25.setStyle("-fx-background-color : #ffffff");
        }else if(id.equals("pSelectItem10")){
            pSelectItem10.setStyle("-fx-background-color : #ffffff");
        }
    }

    public void showPercentSelectBox(){
        this.pSelectList.setVisible(true);
        this.pSelectList.prefHeightProperty().setValue(-1);
        this.pSelectChild.prefHeightProperty().setValue(-1);
    }

    public void hidePercentSelectBox(){
        this.pSelectList.setVisible(false);
        this.pSelectList.prefHeightProperty().setValue(0);
        this.pSelectChild.prefHeightProperty().setValue(48);
    }

    public void initTab(int index) {
        initTabClean();
        initSideTabClean();

        if(index == 0) {
            this.tabLabel1.setTextFill(Color.web("#910000"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane1.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane1.setVisible(true);

        } else if(index == 1) {
            this.tabLabel2.setTextFill(Color.web("#910000"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane2.setVisible(true);

        } else if(index == 2) {
            this.tabLabel3.setTextFill(Color.web("#910000"));
            this.tabLabel3.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane3.setVisible(true);

        } else if(index == 3) {
            this.tabLabel4.setTextFill(Color.web("#910000"));
            this.tabLabel4.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:11px;");
            this.tabLinePane4.setVisible(true);

        }
    }

    public void initSideTab(int index) {
        initSideTabClean();

        if(index == 0) {
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane1.setVisible(true);
            System.out.println("111111111");

        } else if(index == 1) {
            this.sideTabLabel2.setTextFill(Color.web("#910000"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.sideTabLinePane2.setVisible(true);
            System.out.println("222222222");

        }
    }

    public void initTabClean() {
        tabLabel1.setTextFill(Color.web("#999999"));
        tabLabel2.setTextFill(Color.web("#999999"));
        tabLabel3.setTextFill(Color.web("#999999"));
        tabLabel4.setTextFill(Color.web("#999999"));
        tabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel3.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLabel4.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:11px;");
        tabLinePane1.setVisible(false);
        tabLinePane2.setVisible(false);
        tabLinePane3.setVisible(false);
        tabLinePane4.setVisible(false);
    }

    public void initSideTabClean() {
        sideTabLabel1.setTextFill(Color.web("#999999"));
        sideTabLabel2.setTextFill(Color.web("#999999"));
        sideTabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        sideTabLinePane1.setVisible(false);
        sideTabLinePane2.setVisible(false);
    }

}
