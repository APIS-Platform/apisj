package org.apis.gui.controller.module.textfield;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisAddressFieldController extends BaseViewController {

    @FXML private AnchorPane rootPane;
    @FXML private TextField address;
    @FXML private ImageView icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.settingTextFieldStyle(address);

        address.textProperty().addListener((observable, oldValue, newValue) -> {
            if(handler != null){
                handler.change(oldValue, newValue);
            }
        });
    }


    public void setText(String text) {
        address.setText(text);
    }

    public String getText() {
        return address.getText();
    }

    public void setVisible(boolean isVisible) {
        this.rootPane.setVisible(isVisible);
    }

    public ApisAddressFieldImpl handler;
    public void setHandler(ApisAddressFieldImpl handler){
        this.handler = handler;
    }

    public interface ApisAddressFieldImpl{
        void change(String oldValue, String newValue);
    }
}
