package org.apis.gui.controller.module;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ApisSelectBoxRowsizeController extends BaseViewController {
    @FXML private AnchorPane rootPane;
    @FXML private GridPane pSelectHead;
    @FXML private Label selectHeadText;
    @FXML private VBox selectList;

    private ArrayList<String> rowSizeList = new ArrayList<>();
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

        rowSizeList.add("8");
        rowSizeList.add("15");
        rowSizeList.add("30");
        rowSizeList.add("50");

        for(int i=0; i< rowSizeList.size(); i++){
            addItem(rowSizeList.get(i));
        }

        hideList(rowSizeList.get(0));

    }

    private void addItem(String rowSize){
        Label item = new Label();
        item.setPrefWidth(-1);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setPadding(new Insets(4,8,4,8));
        item.setStyle(new JavaFXStyle().add("-fx-background-color","#ffffff").toString());
        item.setText(rowSize);
        item.setAlignment(Pos.CENTER_RIGHT);
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
            selectHeadText.setText(rowSizeList.get(0));
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
                handler.onChange(getSelectSize());
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

    public int getSelectSize() {
        try{
            return Integer.parseInt(selectedTitle);
        }catch (Exception e){
        }
        return 0;
    }


    private ApisSelectBoxRowsizeImpl handler;
    public void setHandler(ApisSelectBoxRowsizeImpl handler){
        this.handler = handler;
    }
    public interface ApisSelectBoxRowsizeImpl{
        void onChange(int size);
    }
}
