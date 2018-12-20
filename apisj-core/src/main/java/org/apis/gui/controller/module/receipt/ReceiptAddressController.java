package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptAddressController extends BaseViewController {

    @FXML private GridPane rootPane;
    @FXML private Label titleLabel, addressLabel;

    private String address, mask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setAddress(String address){
        this.address = address;
        if(address != null) {
            String mask = AppManager.getInstance().getMaskWithAddress(address);
            if(mask != null && mask.length() > 0){
                this.mask = mask;
            }else{
                this.mask = null;
            }
        }
        if(mask != null && mask.length() > 0){
            this.addressLabel.setText(mask);
        }else{
            this.addressLabel.setText(address);
        }


        this.addressLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Label label = (Label) event.getSource();
                label.setText(address);
            }
        });

        this.addressLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Label label = (Label) event.getSource();
                if(mask != null && mask.length() > 0){
                    label.setText(mask);
                }else{
                    label.setText(address);
                }
            }
        });


    }

    public void setTitle(SimpleStringProperty title) {
        this.titleLabel.textProperty().unbind();
        this.titleLabel.textProperty().bind(title);
    }

    public void setLeftPadding(double leftPadding){
        this.rootPane.setPadding(new Insets(0,0,0,leftPadding));
        if(leftPadding > 0){
        }
    }
}
