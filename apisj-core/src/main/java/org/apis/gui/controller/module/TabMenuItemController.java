package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.gui.controller.base.BaseViewController;

public class TabMenuItemController extends BaseViewController {
    @FXML private AnchorPane rootPane;
    @FXML private Label title;
    @FXML private Pane line;
    private int index;

    @FXML
    private void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(title.getText(), this.index);
        }
    }

    public void stateActive(){
        this.title.setTextFill(Color.web("#910000"));
        this.title.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        this.line.setVisible(true);
    }

    public void stateDefault(){
        this.title.setTextFill(Color.web("#999999"));
        this.title.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        this.line.setVisible(false);
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
