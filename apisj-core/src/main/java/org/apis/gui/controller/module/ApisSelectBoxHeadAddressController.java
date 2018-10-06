package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseSelectBoxHeaderController;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAddressController extends BaseSelectBoxHeaderController {

    @FXML
    private Label  addressLabel, maskLabel;
    @FXML
    private ImageView icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (SelectBoxItemModel)model;
        if(model != null) {
            addressLabel.textProperty().unbind();
            addressLabel.textProperty().bind(this.itemModel.addressProperty());
            maskLabel.setText(this.itemModel.getMask());
            icon.setImage(this.itemModel.getIdenticon());
        }
    }
}
