package org.apis.gui.controller.module.selectbox;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.controller.base.BaseSelectBoxHeaderController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.blockchain.ApisUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAliasController extends BaseSelectBoxHeaderController{

    @FXML private Label aliasLabel, addressLabel, balanceLabel;
    @FXML private ImageView icon, icKnowledgekey;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        AppManager.settingIdenticonStyle(icon);
    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (SelectBoxItemModel)model;

        if(model != null) {
            String stringBalance = "0 APIS";
            if(isReadableApisKMBT) {
                stringBalance = ApisUtil.readableApisKMBT(itemModel.getBalance());
            }else {
                stringBalance = ApisUtil.readableApis(itemModel.getBalance(),',',true).replaceAll(",","").split("\\.")[0];
            }
            balanceLabel.setText(stringBalance);
            aliasLabel.setText(itemModel.aliasProperty().get());
            addressLabel.setText(itemModel.addressProperty().get());
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
}
