package org.apis.gui.controller.wallet;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.keystore.KeyStoreDataExp;
import org.apis.util.AddressUtil;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class RewardDetailItemController extends BaseViewController {


    @FXML private AnchorPane rootPane;
    @FXML private ImageView icon;
    @FXML private Label balance, aliasLabel, addressLabel;

    private String address, mask, alias;
    private BigInteger reward;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        AppManager.settingIdenticonStyle(icon);

        addressLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addressLabel.setText(address);
            }
        });

        addressLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(mask != null && mask.length() > 0){
                    addressLabel.setText(mask);
                }else{
                    addressLabel.setText(address);
                }
            }
        });
    }

    public void setData(KeyStoreDataExp data){
        this.mask = AppManager.getInstance().getMaskWithAddress(data.address);
        this.address = AddressUtil.getShortAddress(data.address, 6);
        this.reward = data.rewards;
        this.alias = data.alias;

        this.icon.setImage(ImageManager.getIdenticons(data.address));
        this.aliasLabel.setText(data.alias);
        this.balance.setText(ApisUtil.readableApis(data.rewards, ',', true));
        if(mask != null && mask.length() > 0){
            addressLabel.setText(mask);
        }else{
            addressLabel.setText(address);
        }

    }

    public void setBackground(String backgroundColor) {
        this.rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color",backgroundColor).toString());
    }

    public BigInteger getReward() {
        return this.reward;
    }

    public String getAlias(){
        return this.alias;
    }
}
