package org.apis.gui.controller.module;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class TooltipTopController extends BaseViewController {

    @FXML private AnchorPane rootPane;
    @FXML private Label tooltipText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideTooltip();
    }

    public void showTooltip(){
        if(tooltipText.getText().length() > 0) {
            rootPane.setPrefHeight(-1);
            rootPane.setPrefWidth(-1);
            rootPane.setVisible(true);
        }
    }
    public void hideTooltip(){
        rootPane.setPrefHeight(0);
        rootPane.setPrefWidth(0);
        rootPane.setVisible(false);
    }

    public Label getTooltipText(){
        return this.tooltipText;
    }

    public double getWidth() {
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(this.tooltipText.getText(), Font.font("Noto Sans CJK JP Medium", 10));
        return width;
    }
}
