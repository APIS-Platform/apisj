package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAddressController implements Initializable {
    private SelectBoxWalletItemModel itemModel;
    private ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label addressLabel;
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

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        if(model != null) {
            addressLabel.textProperty().unbind();
            addressLabel.textProperty().bind(this.itemModel.addressProperty());

            try {
                Image image = IdenticonGenerator.generateIdenticonsToImage(addressLabel.textProperty().get(), 128, 128);
                if(image != null){
                    this.icon.setImage(image);
                    image = null;
                }
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onMouseEntered(){ rootPane.setStyle("-fx-background-color: f2f2f2"); }

    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    public void onMouseClicked(){
        if(handler != null){
            handler.onMouseClicked(this.itemModel);
        }
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
