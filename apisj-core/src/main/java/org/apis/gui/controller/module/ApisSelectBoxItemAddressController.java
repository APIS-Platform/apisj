package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseSelectBoxItemController;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAddressController extends BaseSelectBoxItemController {
    private SelectBoxItemModel model = new SelectBoxItemModel();

    @FXML private AnchorPane rootPane;
    @FXML private Label addressLabel, maskLabel;
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
        this.model.set((SelectBoxItemModel)model);
        SelectBoxItemModel itemModel = (SelectBoxItemModel)this.model;

        if(model != null) {
            addressLabel.textProperty().unbind();
            addressLabel.textProperty().bind(itemModel.addressProperty());
            maskLabel.setText(itemModel.getMask());
            icon.setImage(itemModel.getIdenticon());

            // 보안키 체크
            if(itemModel.isUsedProofKey()){
                StyleManager.fontColorStyle(addressLabel, StyleManager.AColor.C2b8a3e);
                icKnowledgekey.setVisible(true);
            }else{
                StyleManager.fontColorStyle(addressLabel, StyleManager.AColor.C999999);
                icKnowledgekey.setVisible(false);
            }
        }
    }
    @Override
    public BaseModel getModel(){
        return this.model;
    }

    public void onMouseEntered(){ rootPane.setStyle("-fx-background-color: f8f8fb"); }

    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked((SelectBoxItemModel)this.model);
        }

        event.consume();
    }
}
