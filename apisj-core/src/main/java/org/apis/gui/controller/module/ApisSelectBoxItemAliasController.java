package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseSelectBoxItemController;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAliasController extends BaseSelectBoxItemController {
    private SelectBoxItemModel model = new SelectBoxItemModel();

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label aliasLabel, addressLabel, maskLabel;
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
        this.model.set((SelectBoxItemModel)model);
        SelectBoxItemModel itemModel = this.model;

        if(model != null) {
            aliasLabel.textProperty().unbind();
            addressLabel.textProperty().unbind();
            maskLabel.textProperty().unbind();

            aliasLabel.textProperty().bind(itemModel.aliasProperty());
            addressLabel.textProperty().bind(itemModel.addressProperty());
            maskLabel.textProperty().bind(itemModel.maskProperty());

            icon.setImage(itemModel.getIdenticon());
        }
    }
    @Override
    public BaseModel getModel(){
        return this.model;
    }

    public void onMouseEntered(){
        rootPane.setStyle("-fx-background-color: f2f2f2");
    }

    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(this.model);
        }
        event.consume();
    }
}
