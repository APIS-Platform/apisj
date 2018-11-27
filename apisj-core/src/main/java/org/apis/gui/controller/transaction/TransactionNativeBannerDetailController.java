package org.apis.gui.controller.transaction;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.util.blockchain.ApisUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeBannerDetailController extends BaseViewController {


    @FXML private AnchorPane rootPane;
    @FXML private ImageView icon;
    @FXML private Label tokenName, balance, symbol;

    private String tokenAddress;
    private String address;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
    }


    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;

        icon.setImage(AppManager.getInstance().getTokenIcon(this.tokenAddress));
        symbol.setText(AppManager.getInstance().getTokenSymbol(this.tokenAddress));
        tokenName.setText(AppManager.getInstance().getTokenNameDB(this.tokenAddress));

        update();
    }

    public void setAddress(String address){
        this.address = address;

        update();
    }

    public void update(){
        if(this.address != null){
            if(this.tokenAddress.equals("-1")){
                if(this.address.length() == 40){
                    this.balance.setText(ApisUtil.readableApis(AppManager.getInstance().getBalance(this.address), ',', true));
                }else{
                    this.balance.setText("0");
                }
            }else if(this.tokenAddress.equals("-2")){
                if(this.address.length() == 40){
                    this.balance.setText(ApisUtil.readableApis(AppManager.getInstance().getMineral(this.address), ',', true));
                }else{
                    this.balance.setText("0");
                }
            }else {
                this.balance.setText(ApisUtil.readableApis(AppManager.getInstance().getTokenValue(this.tokenAddress, this.address), ',', true));
            }
        }
    }

    public void setBackground(String backgroundColor) {
        this.rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color",backgroundColor).toString());
    }
}
