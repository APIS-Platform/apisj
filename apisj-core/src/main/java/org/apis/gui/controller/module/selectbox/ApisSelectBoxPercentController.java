package org.apis.gui.controller.module.selectbox;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxPercentController extends BaseViewController {
    @FXML private AnchorPane rootPane;
    @FXML private GridPane pSelectHead;
    @FXML private Label selectHeadText;
    @FXML private VBox selectList;

    private String[] list = {"100%", "75%", "50%", "25%", "10%"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        pSelectHead.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectList.isVisible()){
                    hideList(selectHeadText.getText());
                }else{
                    showList();
                }
            }
        });

        rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideList(selectHeadText.getText());
            }
        });

        for(int i=0; i < list.length; i++){
            addItem(list[i]);
        }

        hideList(list[0]);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
    }


    @FXML
    public void onMouseExited(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
    }


    @FXML
    public void onMouseEntered(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
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
        selectList.getChildren().add(item);
    }

    public void showList(){
        this.selectList.setVisible(true);
        this.selectList.prefHeightProperty().setValue(-1);
    }

    public void hideList(String title){

        this.selectList.setVisible(false);
        this.selectList.prefHeightProperty().setValue(0);

        if(title == null || title.length() == 0){
            selectHeadText.setText(list[list.length-1]);
        }else{
            selectHeadText.setText(title);
        }
    }

    public BigInteger[] getPercentList(){
        BigInteger resultList[] = new BigInteger[this.list.length];
        for(int i=0; i<this.list.length; i++){
            resultList[i] = new BigInteger(this.list[i].replaceAll("%",""));
        }
        return resultList;
    }


    private EventHandler<MouseEvent> mouseClickedHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Label item = (Label)event.getSource();
            hideList(item.getText());

            if(handler != null){
                handler.onChange(selectHeadText.getText(), getSelectPercent());
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
            item.setStyle(new JavaFXStyle(item.getStyle()).add("-fx-background-color","#f8f8fb").toString());
        }
    };

    public BigInteger getSelectPercent(){
        if(selectHeadText.getText() == null || selectHeadText.getText().length() == 0){
            return null;
        }
        return new BigInteger(selectHeadText.getText().replaceAll("%",""));
    }

    /**
     * 기본값과 비교할 값이 현재 선택한
     * @param stdValue
     * @param targetValue
     */
    public void setCheckStyle(BigInteger stdValue, BigInteger targetValue){
        for(int i=0; i<list.length; i++){
            if(targetValue.compareTo(convert(stdValue, Integer.parseInt(list[i].replaceAll("%","")))) == 0){
                rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#b01e1e").toString());
                selectHeadText.setText(list[i]);
                break;
            }else{
                rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#999999").toString());
            }
        }
    }
    /**
     * 입력한 금액을 해당 퍼센트에 맞는 aAPIS 단위로 출력한다.
     * @param value
     * @return
     */
    public BigInteger convert(BigInteger value) {
        if(selectHeadText.getText() != null) {
            BigInteger percent = new BigInteger(selectHeadText.getText().replaceAll("%",""));
            value.multiply(percent);
            value.divide(BigInteger.valueOf(100));
        }
        return value;
    }
    public BigInteger convert(BigInteger value, int percent) {
        value.multiply(BigInteger.valueOf(percent));
        value.divide(BigInteger.valueOf(100));
        return value;
    }

    public void stateActive(){
        rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color","#b01e1e").toString());
    }

    public void stateDefault(){
        rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color","#999999").toString());
    }


    private ApisSelectboxPercentImpl handler;
    public void setHandler(ApisSelectboxPercentImpl handler){
        this.handler = handler;
    }

    public void setPercent(String percent) {
        this.selectHeadText.setText(percent);
    }

    public interface ApisSelectboxPercentImpl{
        void onChange(String name, BigInteger value);
    }
}
