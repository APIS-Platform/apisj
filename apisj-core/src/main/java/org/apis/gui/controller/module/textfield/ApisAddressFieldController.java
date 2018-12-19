package org.apis.gui.controller.module.textfield;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.util.AddressUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisAddressFieldController extends BaseViewController {

    @FXML private AnchorPane rootPane;
    @FXML private TextField addressField;
    @FXML private ImageView icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        AppManager.settingTextFieldStyle(addressField);
        AppManager.settingIdenticonStyle(icon);

        addressField.textProperty().addListener(addressFieldListener());
    }

    public ChangeListener<String> addressFieldListener() {
        return (observable, oldValue, newValue) -> {

            String address = newValue;
            if(newValue.indexOf("@") >= 0){
                address = AppManager.getInstance().getAddressWithMask(newValue);
            }

            icon.setImage(ImageManager.getIdenticons(address));

            if(handler != null){
                handler.change(oldValue, newValue);
            }
        };
    }


    public void setText(String text) {
        addressField.setText(text);
    }

    public String getText() {
        return addressField.getText();
    }

    public String getAddress(){
        String text = getText();

        if(text != null && AddressUtil.isAddress(text)){
            return text;
        }else{
            String address = AppManager.getInstance().getAddressWithMask(text);
            if(address != null && AddressUtil.isAddress(address)){
                return text;
            }
            return "";
        }
    }

    public void setVisible(boolean isVisible) {
        this.rootPane.setVisible(isVisible);
    }


    public ApisAddressFieldImpl handler;
    public void setHandler(ApisAddressFieldImpl handler){ this.handler = handler; }
    public interface ApisAddressFieldImpl{
        void change(String oldValue, String newValue);
    }
}
