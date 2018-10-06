package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.model.SelectBoxWalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAddressController extends BaseViewController {
    private SelectBoxWalletItemModel itemModel;
    private ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label addressLabel, maskLabel;
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
        this.itemModel = (SelectBoxWalletItemModel)model;

        if(model != null) {
            addressLabel.textProperty().unbind();
            addressLabel.textProperty().bind(this.itemModel.addressProperty());
            maskLabel.setText(this.itemModel.getMask());
            icon.setImage(this.itemModel.getIdenticon());
        }
    }

    public void onMouseEntered(){ rootPane.setStyle("-fx-background-color: f2f2f2"); }

    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(this.itemModel);
        }

        event.consume();
    }

    public ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface getHandler() {
        return handler;
    }

    public void setHandler(ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface handler) {
        this.handler = handler;
    }


    interface SelectBoxItemAddressInterface{
        void onMouseClicked(SelectBoxWalletItemModel itemModel);
    }
}
