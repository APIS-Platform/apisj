package org.apis.gui.controller.module;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class TabMenuItemController extends BaseViewController {
    @FXML private AnchorPane rootPane;
    @FXML private Label title;
    @FXML private Pane line;
    private int index;
    private boolean isVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isVisible) {
                    line.setStyle(new JavaFXStyle(line.getStyle()).add("-fx-background-color", "#999999").toString());
                    line.setVisible(true);
                    rootPane.setOpacity(0.6);
                }
            }
        });

        rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isVisible){
                    line.setVisible(false);
                }
                rootPane.setOpacity(1.0);
            }
        });

        stateDefault();
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(title.getText(), this.index);
        }
    }

    public void stateActive(){
        rootPane.setOpacity(1.0);
        this.title.setTextFill(Color.web("#910000"));
        this.title.setStyle(new JavaFXStyle(this.title.getStyle()).add("-fx-font-family", "Open Sans SemiBold").toString());
        this.line.setStyle(new JavaFXStyle(line.getStyle()).add("-fx-background-color", "#910000").toString());
        this.line.setVisible(true);
        isVisible = true;
    }

    public void stateDefault(){
        rootPane.setOpacity(1.0);
        this.title.setTextFill(Color.web("#999999"));
        this.title.setStyle(new JavaFXStyle(this.title.getStyle()).add("-fx-font-family", "Open Sans").toString());
        this.line.setVisible(false);
        isVisible = false;
    }

    public Label getTitle(){
        return this.title;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private TabMenuItemImpl handler;
    public void setHandler(TabMenuItemImpl handler){
        this.handler = handler;
    }

    public int getIndex() {
        return this.index;
    }

    public interface TabMenuItemImpl {
        void onMouseClicked(String text, int index);
    }
}
