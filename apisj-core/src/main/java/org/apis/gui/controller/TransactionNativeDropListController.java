package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDropListController implements Initializable {
    @FXML
    private AnchorPane bgAnchor;
    @FXML
    private Label walletAddr, addrMasking;

    private TransactionNativeDropListImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Background Color Setting
        bgAnchor.setOnMouseEntered(event -> bgAnchor.setStyle("-fx-background-color: #f2f2f2;"));
        bgAnchor.setOnMouseExited(event -> bgAnchor.setStyle("-fx-background-color: transparent;"));
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("bgAnchor")) {
            if(handler != null) {
                handler.setDropLabel();
            }
        }
    }

    public String getWalletAddr() {
        return walletAddr.getText();
    }

    public void setWalletAddr(String walletAddr) {
        this.walletAddr.setText(walletAddr);
    }

    public String getAddrMasking() {
        return addrMasking.getText();
    }

    public void setAddrMasking(String addrMasking) {
        this.addrMasking.setText(addrMasking);
    }

    public void setHandler(TransactionNativeDropListImpl handler) {
        this.handler = handler;
    }

    public interface TransactionNativeDropListImpl {
        void setDropLabel();
    }
}
