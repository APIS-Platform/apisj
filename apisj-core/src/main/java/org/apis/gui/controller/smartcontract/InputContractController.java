package org.apis.gui.controller.smartcontract;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TextField;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class InputContractController extends BaseViewController {
    @FXML private AnchorPane bgAnchor;
    @FXML private TextField textField;
    @FXML private ImageView icon;

    private InputContractImpl handler;

    private String address;
    private String mask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().settingTextFieldStyle(textField);
        ImageManager.imageViewRectangle30(icon);

        languageSetting();

        // Contract Constructor Address Listener
        textField.textProperty().addListener(ctrtKeyListener);
    }

    private void languageSetting() {
        textField.promptTextProperty().bind(StringManager.getInstance().smartContract.enterContractAddr);
    }

    private ChangeListener<String> ctrtKeyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            address = newValue;
            mask = null;

            if(newValue != null && newValue.indexOf("@") < 0 && newValue.indexOf("0x") >= 0){
                newValue = newValue.replaceAll("0x","");
                textField.setText(newValue);
                return;
            }

            if(newValue != null && newValue.indexOf("@") >= 0){
                mask = newValue;
            }else if(AddressUtil.isAddress(newValue)){
                mask = AppManager.getInstance().getMaskWithAddress(newValue);
            }

            address = newValue;
            if(mask != null && mask.length() > 0){
                //use masking address
                address = AppManager.getInstance().getAddressWithMask(mask);
            }

            icon.setImage(ImageManager.getIdenticons(address));

            if(handler != null){
                handler.change(address, mask);
            }
        }
    };

    public TextField getTextField() {
        return this.textField;
    }

    public void setImage(Image image) {
        this.icon.setImage(image);
    }

    public void setText(String text) {
        this.textField.setText(text);
    }

    public void setHandler(InputContractImpl handler) {
        this.handler = handler;
    }

    public interface InputContractImpl {
        void change(String address, String mask);
    }
}
