package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectboxUnitController implements Initializable {
    @FXML private AnchorPane rootPane;
    @FXML private GridPane pSelectHead;
    @FXML private Label selectHeadText;
    @FXML private VBox selectList;

    private ApisUtil.Unit[] unitList = ApisUtil.Unit.values();
    private String selectedTitle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        pSelectHead.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectList.isVisible()){
                    hideList(selectedTitle);
                }else{
                    showList();
                }
            }
        });

        rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideList(selectedTitle);
            }
        });

        for(int i=unitList.length-1; i >= 0; i--){
            addItem(unitList[i]);
        }

        hideList(unitList[unitList.length-1].name());
    }

    private void addItem(ApisUtil.Unit unit){
        Label item = new Label();
        item.setPrefWidth(80);
        item.setPadding(new Insets(10,16,10,16));
        item.setStyle(new JavaFXStyle().add("-fx-background-color","#ffffff").toString());
        item.setText(unit.name());
        item.setOnMouseExited(mouseExitedHandler);
        item.setOnMouseEntered(mouseEnteredHandler);
        item.setOnMouseClicked(mouseClickedHandler);
        selectList.getChildren().add(item);
    }

    public void showList(){
        this.selectList.setVisible(true);
        this.selectList.prefHeightProperty().setValue(-1);
    }

    public void hideList(String title){
        this.selectedTitle = title;

        this.selectList.setVisible(false);
        this.selectList.prefHeightProperty().setValue(0);

        if(title == null || title.length() == 0){
            selectHeadText.setText(unitList[unitList.length-1].name());
        }else{
            selectHeadText.setText(title);
        }
    }



    private EventHandler<MouseEvent> mouseClickedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Label item = (Label)event.getSource();
            hideList(item.getText());

            if(handler != null){
                handler.onChange(selectedTitle, getSelectUnitValue());
            }
        }
    };

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

    public BigInteger getSelectUnitValue() {
        if(selectedTitle != null) {
            switch (selectedTitle) {
                case "aAPIS":
                    return BigInteger.valueOf(1);
                case "fAPIS":
                    return BigInteger.valueOf(1_000);
                case "pAPIS":
                    return BigInteger.valueOf(1_000_000);
                case "nAPIS":
                    return BigInteger.valueOf(1_000_000_000);
                case "uAPIS":
                    return BigInteger.valueOf(1_000_000_000_000L);
                case "mAPIS":
                    return BigInteger.valueOf(1_000_000_000_000_000L);
                case "APIS":
                    return BigInteger.valueOf(1_000_000_000_000_000_000L);
            }
        }
        return BigInteger.ZERO;
    }

    public ApisUtil.Unit getSelectUnit(){
        if(selectedTitle != null) {
            switch (selectedTitle) {
                case "aAPIS": return ApisUtil.Unit.aAPIS;
                case "fAPIS": return ApisUtil.Unit.fAPIS;
                case "pAPIS": return ApisUtil.Unit.pAPIS;
                case "nAPIS": return ApisUtil.Unit.nAPIS;
                case "uAPIS": return ApisUtil.Unit.uAPIS;
                case "mAPIS": return ApisUtil.Unit.mAPIS;
                case "APIS": return ApisUtil.Unit.APIS;
            }
        }
        return null;
    }

    private ApisSelectboxUnitImpl handler;
    public void setHandler(ApisSelectboxUnitImpl handler){
        this.handler = handler;
    }

    public BigInteger getValue(String value) {
        BigInteger bigInteger = BigInteger.ZERO;
        String bigString = "";
        value = (value != null && value.length() != 0) ? value : "0";
        if(selectedTitle != null) {
            switch (selectedTitle) {
                case "aAPIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.aAPIS, false); break;
                case "fAPIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.fAPIS, false); break;
                case "pAPIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.pAPIS, false); break;
                case "nAPIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.nAPIS, false); break;
                case "uAPIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.uAPIS, false); break;
                case "mAPIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.mAPIS, false); break;
                case "APIS":
                    bigString = ApisUtil.readableApis(value, ',', ApisUtil.Unit.APIS, false); break;
            }
            bigInteger = new BigInteger(bigString.replaceAll(",","").replaceAll("\\.",""));
        }
        return bigInteger;
    }

    public interface ApisSelectboxUnitImpl{
        void onChange(String name, BigInteger value);
    }
}
