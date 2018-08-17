package org.apis.gui.controller;

import com.sun.javafx.css.StyleManager;
import com.sun.org.apache.xml.internal.security.Init;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apis.gui.common.JavaFXStyle;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisTagItemController implements Initializable {
    public static final int STATE_VIEW_NORAML= 0;
    public static final int STATE_VIEW_ACTIVE = 1;
    public static final int STATE_SETTING_NORAML = 10;
    public static final int STATE_SETTING_ACTIVE = 11;
    public static final int STATE_ADD_GROUP = 100;
    private int state = STATE_VIEW_NORAML;

    private ApisTagItemImpl handle;
    @FXML
    private Label text;
    @FXML
    private ImageView btnClose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setState(STATE_VIEW_NORAML);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handle != null){
            handle.onMouseClicked(text.getText());
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setState(int state) {
        this.state = state;
        switch (state){
            case STATE_VIEW_NORAML :
                text.setContentDisplay(ContentDisplay.TEXT_ONLY);
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-background-color","#fafafa").toString());
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-text-fill","#999999").toString());
                break;

            case STATE_VIEW_ACTIVE :
                text.setContentDisplay(ContentDisplay.TEXT_ONLY);
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-background-color","#f2f2f2").toString());
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-text-fill","#434343").toString());
                break;

            case STATE_SETTING_NORAML :
                text.setContentDisplay(ContentDisplay.RIGHT);
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-background-color","#fafafa").toString());
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-text-fill","#999999").toString());
                break;

            case STATE_SETTING_ACTIVE :
                text.setContentDisplay(ContentDisplay.RIGHT);
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-background-color","#f2f2f2").toString());
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-text-fill","#434343").toString());
                break;

            case STATE_ADD_GROUP :
                text.setContentDisplay(ContentDisplay.TEXT_ONLY);
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-background-color","#910000").toString());
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-border-color","#910000").toString());
                text.setStyle(new JavaFXStyle(text.getStyle()).add("-fx-text-fill","#ffffff").toString());
                break;
        }
    }

    public int getState(){ return this.state;}

    public ApisTagItemImpl getHandle() {
        return handle;
    }

    public void setHandle(ApisTagItemImpl handle) {
        this.handle = handle;
    }

    public interface ApisTagItemImpl {
        void onMouseClicked(String text);
    }

    public String getText(){ return this.text.getText(); }
}
