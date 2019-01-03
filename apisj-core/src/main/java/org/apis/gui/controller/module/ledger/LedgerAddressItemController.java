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

    private LedgerAddressItemImpl handler;

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

    public String getAddress() {
        return address.getText();
    }

    public void setAddress(String address) {
        this.address.setText(address);
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
