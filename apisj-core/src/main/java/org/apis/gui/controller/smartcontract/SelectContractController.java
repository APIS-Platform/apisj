package org.apis.gui.controller.smartcontract;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SelectContractController extends BaseViewController {
    @FXML private AnchorPane bgAnchor;
    @FXML private ImageView icon, frozenImg;
    @FXML private Label aliasLabel, addressLabel, placeholderLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        frozenImg.setVisible(false);
        AppManager.getInstance().settingNodeStyle(bgAnchor);
        ImageManager.imageViewRectangle30(icon);

        languageSetting();

        addressLabel.textProperty().addListener(addressTextListener);
    }

    private void languageSetting() {
        placeholderLabel.textProperty().bind(StringManager.getInstance().smartContract.selectContract);
    }

    private ChangeListener<String> addressTextListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if(AppManager.getInstance().isFrozen(addressLabel.getText())) {
                frozenImg.setVisible(true);
                StyleManager.fontColorStyle(addressLabel, StyleManager.AColor.C4871ff);
            } else {
                frozenImg.setVisible(false);
                StyleManager.fontColorStyle(addressLabel, StyleManager.AColor.C999999);
            }

            Image image = ImageManager.getIdenticons(addressLabel.textProperty().get());
            if (image != null) {
                icon.setImage(image);
            }
        }
    };

    public void setIconImage(Image image) {
        this.icon.setImage(image);
    }

    public void setAlias(String alias) {
        this.aliasLabel.setText(alias);
    }

    public String getAddress() {
        return this.addressLabel.getText();
    }

    public void setAddress(String address) {
        this.addressLabel.setText(address);
    }

    public void setPlaceHolderVisible(boolean visibility) {
        this.placeholderLabel.setVisible(visibility);
    }

    public void setFrozenImgVisible(boolean visibility) {
        this.frozenImg.setVisible(visibility);
    }
}
