package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAddressController implements Initializable {
    private SelectBoxWalletItemModel itemModel;

    @FXML
    private Label  addressLabel;
    @FXML
    private ImageView icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( icon.getFitWidth(), icon.getFitHeight() );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
    }

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        if(model != null) {
            addressLabel.textProperty().unbind();

            addressLabel.textProperty().bind(this.itemModel.addressProperty());
        }
    }

    public String getAddress(){
        return this.addressLabel.getText();
    }
    public String getKeystoreId() { return this.itemModel.getKeystoreId(); }

    public String getBalance() { return this.itemModel.getBalance(); }

    public String getMineral() { return this.itemModel.getMineral(); }
}
