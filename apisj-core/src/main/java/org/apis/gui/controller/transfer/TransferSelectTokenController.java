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
import org.apis.gui.manager.AppManager;
import org.apis.util.ByteUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class TransferSelectTokenController extends BaseViewController {
    @FXML private Label header;
    @FXML private VBox itemList;
    @FXML private ScrollPane scrollPane;

    private String selectTokenName = "APIS";
    private String selectTokenAddress;

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
        if(scrollPane.isVisible()){
            hideList(selectTokenName, selectTokenAddress);
        }else{
            showList();
        }
    }

    private void initTokens(){
        itemList.getChildren().clear();
        List<TokenRecord> tokens = DBManager.getInstance().selectTokens();
        addItem("APIS", "-1");
        for(int i=0; i<tokens.size(); i++){
            addItem(tokens.get(i).getTokenName(), ByteUtil.toHexString(tokens.get(i).getTokenAddress()));
        }
    }


    private void showList(){
        scrollPane.setPrefHeight(-1);
        scrollPane.setVisible(true);
    }
    private void hideList(String tokenName, String tokenAddress){
        selectTokenName = tokenName;
        selectTokenAddress = tokenAddress;
        this.header.setText(selectTokenName);
        scrollPane.setPrefHeight(0);
        scrollPane.setVisible(false);
    }

    private void addItem(String tokenName, String tokenAddress){
        Label item = new Label();
        item.setMinWidth(-1);
        item.setPrefWidth(-1);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setPadding(new Insets(16,16,16,16));
        item.setStyle(new JavaFXStyle().add("-fx-background-color","#ffffff").toString());
        item.setText(tokenName);
        item.setOnMouseExited(mouseExitedHandler);
        item.setOnMouseEntered(mouseEnteredHandler);
        item.setOnMouseClicked(mouseClickedHandler);
        item.setId(tokenAddress);
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
            item.setStyle(new JavaFXStyle(item.getStyle()).add("-fx-background-color","#f8f8fb").toString());
        }
    };

    private EventHandler<MouseEvent> mouseClickedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Label item = (Label)event.getSource();
            hideList(item.getText(), item.getId());

            if(handler != null){
                handler.onChange(item.getText(), item.getId());
            }
        }
    };

    public String getSelectTokenName(){
        return this.selectTokenName;
    }

    public String getSelectTokenAddress(){
        return (this.selectTokenAddress != null) ? this.selectTokenAddress : "" ;
    }


    private TransferSelectTokenImpl handler;
    public void setHeader(TransferSelectTokenImpl handler){
        this.handler = handler;
    }
    public void setSelectedToken(String tokenAddress){
        if(tokenAddress != null
                && (tokenAddress.equals("-1") || tokenAddress.equals("-2"))){
            hideList("APIS", "-1");
        } else {
            for(int i=0; i<itemList.getChildren().size(); i++){
                if(itemList.getChildren().get(i).getId().equals(tokenAddress)){
                    hideList(((Label)itemList.getChildren().get(i)).getText(), tokenAddress);
                    break;
                }
            }
        }
    }

    public String getTokenSymbol() {
        return AppManager.getInstance().getTokenSymbol(this.selectTokenAddress);
    }

    public interface TransferSelectTokenImpl{
        void onChange(String tokenName, String tokenAddress);
    }
}
