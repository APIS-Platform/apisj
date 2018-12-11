package org.apis.gui.controller.wallet.walletlist;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.model.TokenModel;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.blockchain.ApisUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class WalletListBodyController extends BaseViewController{
    private WalletItemModel model = new WalletItemModel();
    @FXML private AnchorPane rootPane;

    // 토큰 타입
    @FXML private Pane bottomLine;
    @FXML private ImageView tokenIcon;
    @FXML private Label tokenName, tokenValue, tokenSymbol, noTransaction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // set a clip to apply rounded border to the original image.
        Rectangle tokenIconClip = new Rectangle( this.tokenIcon.getFitWidth()-0.5, this.tokenIcon.getFitHeight()-0.5 );
        tokenIconClip.setArcWidth(30);
        tokenIconClip.setArcHeight(30);
        tokenIcon.setClip(tokenIconClip);
    }

    public BigInteger getValue(){
        WalletItemModel itemModel = (WalletItemModel) this.model;

        if(itemModel.getTokenAddress().equals("-1")){
            return itemModel.getApis();
        }else if(itemModel.getTokenAddress().equals("-2")){
            return itemModel.getMineral();
        }else{
            return AppManager.getInstance().getTokenValue(itemModel.getTokenAddress(), itemModel.getAddress());
        }
    }

    public void show(){
        this.rootPane.setMinHeight(64.0);
        this.rootPane.setMaxHeight(64.0);
        this.rootPane.setPrefHeight(64.0);
        this.rootPane.setVisible(true);
    }
    public void hide(){
        this.rootPane.setMinHeight(0.0);
        this.rootPane.setMaxHeight(0.0);
        this.rootPane.setPrefHeight(0.0);
        this.rootPane.setVisible(false);
    }
    public void setBottomLineVisible(boolean isVisible){
        bottomLine.setVisible(isVisible);
    }

    @Override
    public void setModel(BaseModel model){
        if(model != null) {
            this.model.set((WalletItemModel) model);
            WalletItemModel itemModel =  this.model;
            if(itemModel.getTokenAddress().equals("-1")){
                this.tokenValue.setText(ApisUtil.readableApis(itemModel.getApis(), ',',false));
                this.tokenIcon.setImage(ImageManager.apisIcon);
                this.tokenName.setText("APIS");
                this.tokenSymbol.setText("APIS");
            }else if(itemModel.getTokenAddress().equals("-2")){
                this.tokenValue.setText(ApisUtil.readableApis(itemModel.getMineral(), ',',false));
                this.tokenIcon.setImage(ImageManager.mineraIcon);
                this.tokenName.setText("MINERAL");
                this.tokenSymbol.setText("MNR");
            }else {
                this.tokenValue.setText(ApisUtil.readableApis(AppManager.getInstance().getTokenValue(itemModel.getTokenAddress(), itemModel.getAddress()), ',', false));
                this.tokenIcon.setImage(AppManager.getInstance().getTokenIcon(itemModel.getTokenAddress()));
                this.tokenSymbol.setText(AppManager.getInstance().getTokenSymbol(itemModel.getTokenAddress()));

                for(TokenModel token : AppManager.getInstance().getTokens()){
                    if(token.getTokenAddress().equals(itemModel.getTokenAddress())){
                        this.tokenName.setText(token.getTokenName());
                        break;
                    }
                }
            }

        }

    }

    @Override
    public BaseModel getModel(){
        return this.model;
    }

    public Node getRootPane() { return this.rootPane; }
}
