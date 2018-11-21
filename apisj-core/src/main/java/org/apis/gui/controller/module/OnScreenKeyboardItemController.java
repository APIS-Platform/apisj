package org.apis.gui.controller.module;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class OnScreenKeyboardItemController implements Initializable {
    @FXML
    private AnchorPane itemPane;
    @FXML
    private Label itemLabel;

    private boolean mouseFocusFlag = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemAddEventListener();
    }

    public void itemAddEventListener() {
        itemLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseFocusFlag = true;

                StyleManager.backgroundColorStyle(itemLabel, StyleManager.AColor.C2b2b2b);
                StyleManager.fontColorStyle(itemLabel, StyleManager.AColor.Cffffff);
            }
        });

        itemLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseFocusFlag = false;
                StyleManager.backgroundColorStyle(itemLabel, StyleManager.AColor.Cffffff);
                StyleManager.fontColorStyle(itemLabel, StyleManager.AColor.C202020);
            }
        });

        itemLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                StyleManager.backgroundColorStyle(itemLabel, StyleManager.AColor.C910000);
                StyleManager.fontColorStyle(itemLabel, StyleManager.AColor.Cffffff);
            }
        });

        itemLabel.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(mouseFocusFlag) {
                    StyleManager.backgroundColorStyle(itemLabel, StyleManager.AColor.C2b2b2b);
                    StyleManager.fontColorStyle(itemLabel, StyleManager.AColor.Cffffff);
                    // Input a selected word to textfield
                    if(handler != null){
                        handler.clicked(itemLabel.getText());
                    }

                } else {

                    StyleManager.backgroundColorStyle(itemLabel, StyleManager.AColor.Cffffff);
                    StyleManager.fontColorStyle(itemLabel, StyleManager.AColor.C202020);
                }
            }
        });
    }

    public String getItemLabel() {
        return this.itemLabel.getText();
    }

    public int getChildrenSize() {
        return this.itemPane.getChildren().size();
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel.setText(itemLabel);
    }

    public void setEmpty() {
        this.itemPane.getChildren().remove(itemLabel);
    }

    public void setItemConvert() {
        this.itemLabel.setText(String.valueOf((char)(getItemLabel().charAt(0)^32)));
    }


    private OnScreenKeyboardItemImpl handler;
    public void setHandler(OnScreenKeyboardItemImpl handler){
        this.handler = handler;
    }
    public interface OnScreenKeyboardItemImpl {
        void clicked(String word);
    }
}
