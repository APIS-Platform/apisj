package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.util.blockchain.ApisUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectboxUnitController implements Initializable {
    @FXML private AnchorPane rooPane;
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



        for(int i=unitList.length-1; i >= 0; i--){
            addItem(unitList[i]);
        }

        hideList(null);
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
}
