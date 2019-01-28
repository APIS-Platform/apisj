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

import java.net.URL;
import java.util.ResourceBundle;

public class InputContractController extends BaseViewController {
    @FXML private AnchorPane bgAnchor;
    @FXML private TextField textField;
    @FXML private ImageView icon;

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
            if (!textField.getText().matches("[0-9a-fA-F]*")) {
                textField.setText(textField.getText().replaceAll("[^0-9a-fA-F]", ""));
            }

            int maxlangth = 40;
            if (textField.getText().trim().length() > maxlangth) {
                textField.setText(textField.getText().trim().substring(0, maxlangth));
            }

            if (textField.getText() == null || textField.getText().trim().length() < maxlangth) {

                icon.setImage(ImageManager.icCircleNone);
            } else {
                Image image = ImageManager.getIdenticons(textField.getText().trim());
                if (image != null) {
                    icon.setImage(image);
                }
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
}
