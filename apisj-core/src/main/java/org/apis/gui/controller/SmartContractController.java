package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractController implements Initializable {

    @FXML
    private VBox pSelectList, pSelectChild;
    @FXML
    private GridPane pSelectHead, pSelectItem100, pSelectItem75, pSelectItem50, pSelectItem25, pSelectItem10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
}
