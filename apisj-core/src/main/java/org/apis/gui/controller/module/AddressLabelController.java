package org.apis.gui.controller.module;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyTxHashController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressLabelController extends BaseViewController {

    @FXML private AnchorPane rootPane, tooltip;
    @FXML private Label address, tooltipText;
    @FXML private ImageView infoIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideTooltip();
        hideAddressUnderLine();

        rootPane.setOnMouseClicked(onMouseClicked);
        rootPane.setOnMouseEntered(onMouseEnteredEvent);
        rootPane.setOnMouseExited(onMouseExitedEvent);
    }
    private EventHandler<MouseEvent> onMouseClicked = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(handler != null){
                handler.onMouseClicked(address.getText());
            }
        }
    };
    private EventHandler<MouseEvent> onMouseEnteredEvent = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            showTooltip();
            showAddressUnderLine();
            event.consume();
        }
    };
    private EventHandler<MouseEvent> onMouseExitedEvent = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            hideTooltip();
            hideAddressUnderLine();
            event.consume();
        }
    };

    public void setAddress(String address) {
        this.address.setText(address);
    }
    public void setTooltip(String tooltip){
        this.tooltipText.setText((tooltip !=null)?tooltip:"");
        if(tooltip != null && tooltip.length() > 0) {
            this.address.setPadding(new Insets(0, 13, 0,0));
            this.infoIcon.setVisible(true);
        }else{
            this.address.setPadding(new Insets(0, 0, 0,0));
            this.infoIcon.setVisible(false);
        }
    }


    public void showAddressUnderLine(){
        address.setStyle(new JavaFXStyle(address.getStyle()).add("-fx-underline","true").toString());
    }

    public void hideAddressUnderLine(){
        address.setStyle(new JavaFXStyle(address.getStyle()).remove("-fx-underline").toString());
    }

    public void showTooltip(){
        if(tooltipText.getText().length() > 0) {
            tooltip.setPrefHeight(-1);
            tooltip.setVisible(true);
        }
    }
    public void hideTooltip(){
        tooltip.setPrefHeight(0);
        tooltip.setVisible(false);
    }

    private AddressLabelImpl handler;
    public void setHandler(AddressLabelImpl handler){
        this.handler = handler;
    }
    public interface AddressLabelImpl{
        void onMouseClicked(String address);
    }
}
