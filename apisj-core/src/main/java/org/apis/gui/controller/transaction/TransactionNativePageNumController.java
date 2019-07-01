package org.apis.gui.controller.transaction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativePageNumController extends BaseViewController {
    @FXML
    private Label pageNum;

    private boolean isPageSelected = false;
    private TransactionNativePageNumImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StyleManager.fontStyle(pageNum, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
    }

    @Override
    public void fontUpdate() {
        StyleManager.fontStyle(pageNum, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("pageNum")) {
            handler.movePage(Integer.parseInt(pageNum.getText()));
        }
    }

    public void isSelected(boolean pageSelected) {
        if(pageSelected) {
            pageNum.setStyle(new JavaFXStyle(pageNum.getStyle()).add("-fx-background-color", "#b01e1e").toString());
            pageNum.setStyle(new JavaFXStyle(pageNum.getStyle()).add("-fx-text-fill", "#ffffff").toString());
        } else {
            pageNum.setStyle(new JavaFXStyle(pageNum.getStyle()).add("-fx-background-color", "#ffffff").toString());
            pageNum.setStyle(new JavaFXStyle(pageNum.getStyle()).add("-fx-text-fill", "#353535").toString());
        }
    }

    public String getPageNum() {
        return pageNum.getText();
    }

    public void setPageNum(String pageNum) {
        this.pageNum.setText(pageNum);
    }

    public boolean isPageSelected() {
        return isPageSelected;
    }

    public void setPageSelected(boolean pageSelected) {
        isPageSelected = pageSelected;
    }

    public void setHandler(TransactionNativePageNumImpl handler) {
        this.handler = handler;
    }

    public interface TransactionNativePageNumImpl {
        void movePage(int pageNum);
    }
}
