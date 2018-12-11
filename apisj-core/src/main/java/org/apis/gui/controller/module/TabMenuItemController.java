package org.apis.gui.controller.module;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class TabMenuItemController extends BaseViewController {
    @FXML private AnchorPane rootPane;
    @FXML private Label title;
    @FXML private Pane line;
    private int index;
    private boolean isActive = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isActive) {
                    line.setStyle(new JavaFXStyle(line.getStyle()).add("-fx-background-color", "#999999").toString());
                    line.setVisible(true);
                    StyleManager.fontColorStyle(title, StyleManager.AColor.C2b2b2b);
                }
            }
        });

        rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isActive){
                    line.setVisible(false);
                    StyleManager.fontColorStyle(title, StyleManager.AColor.C999999);
                }
            }
        });

        stateDefault();
        setFontSize12();
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(title.getText(), this.index);
        }
    }

    public void stateActive(){
        rootPane.setOpacity(1.0);

        StyleManager.fontColorStyle(title, StyleManager.AColor.Cb01e1e);
        StyleManager.fontStyle(title, StyleManager.Standard.SemiBold);
        this.line.setStyle(new JavaFXStyle(line.getStyle()).add("-fx-background-color", "#b01e1e").toString());
        this.line.setVisible(true);
        isActive = true;
    }

    public void stateDefault(){
        rootPane.setOpacity(1.0);
        StyleManager.fontColorStyle(title, StyleManager.AColor.C999999);
        StyleManager.fontStyle(title, StyleManager.Standard.Regular);
        this.line.setVisible(false);
        isActive = false;
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

    public void setFontSize12() {
        StyleManager.fontStyle(title, StyleManager.AFontSize.Size12);
    }
    public void setFontSize14() {
        StyleManager.fontStyle(title, StyleManager.AFontSize.Size14);
    }

    public interface TabMenuItemImpl {
        void onMouseClicked(String text, int index);
    }
}
