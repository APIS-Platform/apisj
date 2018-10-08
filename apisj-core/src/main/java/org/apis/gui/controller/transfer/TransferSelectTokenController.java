package org.apis.gui.controller.transfer;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class TransferSelectTokenController extends BaseViewController {
    @FXML private Label header;
    @FXML private VBox itemList;
    @FXML private ScrollPane scrollPane;

    private String selectText = "APIS";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTokens();
    }

    @Override
    public void update(){
        initTokens();
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        hideList(selectText);
    }

    private void initTokens(){
        itemList.getChildren().clear();
        List<TokenRecord> tokens = DBManager.getInstance().selectTokens();
        addItem("APIS");
        for(int i=0; i<tokens.size(); i++){
            addItem(tokens.get(i).getTokenName());
        }
    }


    private void hideList(String text){
        selectText = text;
        this.header.setText(selectText);
        if(scrollPane.isVisible()){
            scrollPane.setPrefHeight(0);
            scrollPane.setVisible(false);
        }else{
            scrollPane.setPrefHeight(-1);
            scrollPane.setVisible(true);
        }
    }

    private void addItem(String percent){
        Label item = new Label();
        item.setPrefWidth(80);
        item.setPadding(new Insets(16,16,16,16));
        item.setStyle(new JavaFXStyle().add("-fx-background-color","#ffffff").toString());
        item.setText(percent);
        item.setOnMouseExited(mouseExitedHandler);
        item.setOnMouseEntered(mouseEnteredHandler);
        item.setOnMouseClicked(mouseClickedHandler);
        itemList.getChildren().add(item);
    }
    private EventHandler<MouseEvent> mouseExitedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Label item = (Label)event.getSource();
            item.setStyle(new JavaFXStyle(item.getStyle()).add("-fx-background-color","#ffffff").toString());
        }
    };

    private EventHandler<MouseEvent> mouseEnteredHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Label item = (Label)event.getSource();
            item.setStyle(new JavaFXStyle(item.getStyle()).add("-fx-background-color","#f2f2f2").toString());
        }
    };

    private EventHandler<MouseEvent> mouseClickedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Label item = (Label)event.getSource();
            hideList(item.getText());

            if(handler != null){
                handler.onChange(item.getText());
            }
        }
    };


    private TransferSelectTokenImpl handler;
    public void setHeader(TransferSelectTokenImpl handler){
        this.handler = handler;
    }
    public interface TransferSelectTokenImpl{
        void onChange(String tokenName);
    }
}
