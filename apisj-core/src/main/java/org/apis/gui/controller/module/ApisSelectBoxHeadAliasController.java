package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseSelectBoxHeaderController;
import org.apis.gui.manager.FontManager;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAliasController extends BaseSelectBoxHeaderController{

    @FXML private Label aliasLabel, addressLabel, maskLabel;
    @FXML private ImageView icon, icKnowledgekey;

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
            aliasLabel.textProperty().unbind();
            addressLabel.textProperty().unbind();
            maskLabel.textProperty().unbind();

            aliasLabel.textProperty().bind(this.itemModel.aliasProperty());
            addressLabel.textProperty().bind(this.itemModel.addressProperty());
            maskLabel.textProperty().bind(this.itemModel.maskProperty());
            icon.setImage(this.itemModel.getIdenticon());

            // 보안키 체크
            if(itemModel.isUsedProofKey()){
                FontManager.fontStyle(addressLabel, FontManager.AFontColor.C2b8a3e);
                icKnowledgekey.setVisible(true);
            }else{
                FontManager.fontStyle(addressLabel, FontManager.AFontColor.C999999);
                icKnowledgekey.setVisible(false);
            }

        }
    }
}
