package org.apis.gui.controller.module.selectbox;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

    @FXML private Label aliasLabel, addressLabel, balanceLabel, symbolLabel;
    @FXML private ImageView icon, icKnowledgekey, icLedger;

    private String tokenAddress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        AppManager.settingIdenticonStyle(icon);

        addressLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addressLabel.setText(itemModel.addressProperty().get());
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
        this.itemModel = (SelectBoxItemModel)model;

        if(model != null) {
            String stringBalance = "0";
            if(tokenAddress == null) {
                if (isReadableApisKMBT) {
                    stringBalance = ApisUtil.readableApisKMBT(itemModel.getBalance());
                } else {
                    stringBalance = ApisUtil.readableApis(itemModel.getBalance(), ',', true).replaceAll(",", "").split("\\.")[0];
                }
            }else{
                if (isReadableApisKMBT) {
                    stringBalance = ApisUtil.readableApisKMBT(AppManager.getInstance().getTokenValue(tokenAddress, itemModel.getAddress()));
                } else {
                    stringBalance = ApisUtil.readableApis(AppManager.getInstance().getTokenValue(tokenAddress, itemModel.getAddress()), ',', true).replaceAll(",", "").split("\\.")[0];
                }
            }
            balanceLabel.setText(stringBalance);
            aliasLabel.setText(itemModel.aliasProperty().get());
            addressLabel.setText(returnMask());
            icon.setImage(itemModel.getIdenticon());

            // 보안키 체크
            if(itemModel.isUsedProofKey()){
                StyleManager.fontColorStyle(addressLabel, StyleManager.AColor.C2b8a3e);
                icKnowledgekey.setVisible(true);
                icKnowledgekey.setFitWidth(14);
            }else{
                StyleManager.fontColorStyle(addressLabel, StyleManager.AColor.C999999);
                icKnowledgekey.setVisible(false);
                icKnowledgekey.setFitWidth(1);
            }

            // 렛저 체크
            if(AppManager.getInstance().isLedger(itemModel.getAddress())){
                icLedger.setVisible(true);
                icLedger.setFitWidth(25);
            }else{
                icLedger.setVisible(false);
                icLedger.setFitWidth(1);
            }
        }
    }


    public void setTokenAddress(String address){
        this.tokenAddress = address;
        this.symbolLabel.setText("APIS");

        String symbol = AppManager.getInstance().getTokenSymbol(address);
        if(address != null && symbol != null && symbol.length() > 0){
        }else{
            symbol = "APIS";
        }
        this.symbolLabel.setText(symbol);

        String stringBalance = "0";
        if(itemModel != null) {
            if (isReadableApisKMBT) {
                stringBalance = ApisUtil.readableApisKMBT(AppManager.getInstance().getTokenValue(tokenAddress, itemModel.getAddress()));
            } else {
                stringBalance = ApisUtil.readableApis(AppManager.getInstance().getTokenValue(tokenAddress, itemModel.getAddress()), ',', true).replaceAll(",", "").split("\\.")[0];
            }
        }
        balanceLabel.setText(stringBalance);
    }


    public String returnMask(){
        String address = itemModel.addressProperty().get();
        String mask;
        if(address != null){
            mask = AppManager.getInstance().getMaskWithAddress(address);
            if (mask != null && mask.length() > 0) {
                address = mask;
            }
        }
        return address;
    }
}
