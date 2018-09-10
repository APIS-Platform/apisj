package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativePageNumController implements Initializable {
    @FXML
    private Label pageNum;

    private boolean isPageSelected = false;
    private TransactionNativePageNumImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
            pageNum.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-background-color: #910000; " +
                    "-fx-border-color: #d8d8d8; -fx-border-width: 1 0 1 1; -fx-text-fill: #ffffff;");
        } else {
            pageNum.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; -fx-background-color: #ffffff; " +
                    "-fx-border-color: #d8d8d8; -fx-border-width: 1 0 1 1; -fx-text-fill: #353535;");
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
