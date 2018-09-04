package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.awt.event.InputEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativePageNumController implements Initializable {
    @FXML
    private Label pageNum;

    private TransactionNativePageNumImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("pageNum")) {
            handler.movePage();
        }
    }

    public String getPageNum() {
        return pageNum.getText();
    }

    public void setPageNum(String pageNum) {
        this.pageNum.setText(pageNum);
    }

    public interface TransactionNativePageNumImpl {
        void movePage();
    }
}
