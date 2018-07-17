package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.MainModel;

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
    @FXML
    private Label totalNatural, totalDecimal, peer, block, timestemp;

    private ArrayList<Label> labels = new ArrayList<>();
    private ArrayList<Pane> lines = new ArrayList<>();

    private MainModel mainModel = new MainModel();

    public MainController(){
        AppManager.getInstance().guiFx.setMain(this);
    }


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
    public void initLayoutFooter(){
        this.totalNatural.textProperty().bind(mainModel.totalBalanceNaturalProperty());
        this.totalDecimal.textProperty().bind(mainModel.totalBalanceDecimalProperty());
        //this.totalNatural.textProperty().bind(mainModel.totalMineralNaturalProperty());
        //this.totalDecimal.textProperty().bind(mainModel.totalMineralDecimalProperty());
        this.peer.textProperty().bind(mainModel.peerProperty());
        this.block.textProperty().bind(mainModel.blockProperty());
        this.timestemp.textProperty().bind(mainModel.timestempProperty());
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

        if(index == 1){
            AppManager.getInstance().guiFx.getTransfer().init();
        }
    }
    public void selectedHeader(int index){

        // change header active
        setHeaderActive(index);

        // change tab pane
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(index);
    }

    public void setBlock(long myBestBlock, long worldBestBlock){
        mainModel.setBlock(myBestBlock, worldBestBlock);
    }

    public void setTimestemp(long timeStemp, long nowStemp){
        mainModel.setTimestemp(timeStemp, nowStemp);
    }

    public void setPeer(long peer){
        mainModel.setPeer(""+peer);
    }

    public void setTotalBalance(String balance){
        balance = AppManager.addDotWidthIndex(balance);
        String[] balanceSplit = balance.split("\\.");
        this.mainModel.totalBalanceNaturalProperty().setValue(balanceSplit[0]);
        this.mainModel.totalBalanceDecimalProperty().setValue("."+balanceSplit[1]);
    }

    public void setTotalMineral(String mineral){
        mineral = AppManager.addDotWidthIndex(mineral);
        String[] mineralSplit = mineral.split("\\.");
        this.mainModel.totalMineralNaturalProperty().setValue(mineralSplit[0]);
        this.mainModel.totalMineralDecimalProperty().setValue("."+mineralSplit[1]);
    }




    @FXML
    private void onClickTabEvent(InputEvent event){
        String id = ((Node)event.getSource()).getId();
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

        this.tabPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP
                    || event.getCode() == KeyCode.DOWN
                    || event.getCode() == KeyCode.LEFT
                    || event.getCode() == KeyCode.RIGHT) {

                    event.consume();
                }
            }
        });

        initLayoutHeader();
        initLayoutFooter();

        selectedHeader(0);

        AppManager.getInstance().guiFx.setMainPopup1(popupLayout1);
        AppManager.getInstance().guiFx.setMainPopup2(popupLayout2);

        int size = AppManager.getInstance().getKeystoreList().size();
        if (size <= 0) {

        }
    }
}
