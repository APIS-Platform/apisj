package org.apis.gui.controller.module.ledger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.ImageManager;

import java.net.URL;
import java.util.ResourceBundle;

public class LedgerAddressItemController implements Initializable {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label address, balance;
    @FXML private ImageView checkImg;

    private String path;
    private LedgerAddressItemImpl handler;
    private boolean checked = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkImg.setImage(ImageManager.checkGrey);

        this.bgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(handler != null) {
                    handler.clicked();
                }
            }
        });
    }

    public void check() {
        checked = true;
        checkImg.setImage(ImageManager.checkRed);
    }

    public void unCheck() {
        checked = false;
        checkImg.setImage(ImageManager.checkGrey);
    }

    public boolean isChecked() {
        return this.checked;
    }

    public String getAddress() {
        return address.getText();
    }

    public void setAddress(String address) {
        this.address.setText(address);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBalance() {
        return balance.getText();
    }

    public void setBalance(String balance) {
        this.balance.setText(balance);
    }

    public void setHandler(LedgerAddressItemImpl handler) {
        this.handler = handler;
    }

    public interface LedgerAddressItemImpl {
        void clicked();
    }
}
