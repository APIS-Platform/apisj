package org.apis.gui.controller.module.selectbox;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseSelectBoxItemController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.blockchain.ApisUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAliasController extends BaseSelectBoxItemController {
    private SelectBoxItemModel model = new SelectBoxItemModel();

    @FXML private AnchorPane rootPane;
    @FXML private Label aliasLabel, addressLabel, balanceLabel;
    @FXML private ImageView icon, icKnowledgekey;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);

        addressLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addressLabel.setText(model.addressProperty().get());
            }
        });
        addressLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addressLabel.setText(returnMask());
            }
        });
    }

    @Override
    public void setModel(BaseModel model) {
        this.model.set((SelectBoxItemModel)model);
        SelectBoxItemModel itemModel = this.model;

        if(model != null) {
            String stringBalance = "0 APIS";
            if(isReadableApisKMBT) {
                stringBalance = ApisUtil.readableApisKMBT(itemModel.getBalance());
            }else {
                stringBalance = ApisUtil.readableApis(itemModel.getBalance(),',',true).replaceAll(",","").split("\\.")[0];
            }
            balanceLabel.setText(stringBalance);
            aliasLabel.setText(itemModel.aliasProperty().get());
            addressLabel.setText(returnMask());
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

    public String returnMask(){
        String address = model.addressProperty().get();
        String mask;
        if(address != null){
            mask = AppManager.getInstance().getMaskWithAddress(address);
            if (mask != null && mask.length() > 0) {
                address = mask;
            }
        }
        return address;
    }


    @Override
    public BaseModel getModel(){
        return this.model;
    }

    public void onMouseEntered(){
        rootPane.setStyle("-fx-background-color: f8f8fb");
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
