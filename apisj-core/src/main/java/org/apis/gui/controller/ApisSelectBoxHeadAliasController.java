package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.model.SelectBoxWalletItemModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAliasController implements Initializable {
    private SelectBoxWalletItemModel itemModel;

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

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        if(model != null) {
            aliasLabel.textProperty().unbind();
            addressLabel.textProperty().unbind();
            maskLabel.textProperty().unbind();

            aliasLabel.textProperty().bind(this.itemModel.aliasProperty());
            addressLabel.textProperty().bind(this.itemModel.addressProperty());
            maskLabel.textProperty().bind(this.itemModel.maskProperty());

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

    public String getAddress(){
        return this.addressLabel.getText();
    }
    public String getKeystoreId() { return this.itemModel.getKeystoreId(); }

    public String getBalance() { return this.itemModel.getBalance(); }

    public String getMineral() { return this.itemModel.getMineral(); }
}
