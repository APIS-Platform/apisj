package org.apis.gui.controller.wallet.tokenlist;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.AddressUtil;
import org.apis.util.blockchain.ApisUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class TokenListBodyController extends BaseViewController{
    private WalletItemModel model = new WalletItemModel();

    @FXML private AnchorPane rootPane;

    @FXML private GridPane walletPane;
    @FXML private Label walletAlias, walletAddress, walletValue, btnCopy, tokenSymbol, labelAddressMasking;
    @FXML private AnchorPane miningPane;
    @FXML private ImageView walletIcon, btnTransfer, btnAddressMasking, icKnowledgekey;

    private static final int BODY_COPY_STATE_NONE = 0;
    private static final int BODY_COPY_STATE_NORMAL = 1;
    private static final int BODY_COPY_STATE_ACTIVE = 2;
    private boolean btnCopyClickedFlag = false;

    private String tokenAddress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Rectangle walletIconClip = new Rectangle( this.walletIcon.getFitWidth()-0.5, this.walletIcon.getFitHeight()-0.5 );
        walletIconClip.setArcWidth(30);
        walletIconClip.setArcHeight(30);
        walletIcon.setClip(walletIconClip);

        setMask(null);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("rootPane")){
        }else if(id.equals("btnCheckBox")){
        }else if(id.equals("btnCopy")){
            btnCopyClickedFlag = true;

            String text = this.model.getAddress();
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            if(this.handler != null){
                this.handler.onClickCopy(text);
            }

        }else if(id.equals("btnAddressMasking")){
            if(this.handler != null){
                this.handler.onClickAddressMasking(event, this.model);
            }
        }else if(id.equals("btnTransfer")){
            if(this.handler != null){
                this.handler.onClickTransfer(event,  this.model);
            }
        }
    }
    @FXML
    public void onMouseEntered(InputEvent event){
        btnCopyClickedFlag = false;
        String id = ((Node)event.getSource()).getId();
        if(id.equals("paneAddress")){
            setCopyState(BODY_COPY_STATE_NORMAL);
        }else if(id.equals("btnCopy")){
            setCopyState(BODY_COPY_STATE_ACTIVE);
        }else if(id.equals("btnAddressMasking")){
            btnAddressMasking.setImage(ImageManager.btnAddAddressMaskingHover);
        }else if(id.equals("btnTransfer")){
            btnTransfer.setImage(ImageManager.btnAddTransferHover);
        }
    }
    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("paneAddress")){
            setCopyState(BODY_COPY_STATE_NONE);
        }else if(id.equals("btnCopy")){
            if(btnCopyClickedFlag == true) {
                setCopyState(BODY_COPY_STATE_NONE);
            } else {
                setCopyState(BODY_COPY_STATE_NORMAL);
            }
        }else if(id.equals("btnAddressMasking")){
            btnAddressMasking.setImage(ImageManager.btnAddAddressMasking);
        }else if(id.equals("btnTransfer")){
            btnTransfer.setImage(ImageManager.btnAddTransfer);
        }
    }

    public BigInteger getValue(){
        WalletItemModel itemModel = this.model;

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

    @Override
    public void setModel(BaseModel model){
        if(model != null) {
            this.model.set((WalletItemModel) model);
            WalletItemModel itemModel = this.model;
            this.walletIcon.setImage(ImageManager.getIdenticons(itemModel.getAddress()));
            this.walletAlias.setText(itemModel.getAlias());
            this.walletAddress.setText(AddressUtil.getShortAddress(itemModel.getAddress(), 12));

            if(this.tokenAddress.equals("-1")){
                this.walletValue.setText(ApisUtil.readableApis(itemModel.getApis(), ',', false));
                this.tokenSymbol.setText("APIS");
            }else if(this.tokenAddress.equals("-2")){
                this.walletValue.setText(ApisUtil.readableApis(itemModel.getMineral(), ',', false));
                this.tokenSymbol.setText("MNR");
            }else{
                this.walletValue.setText(ApisUtil.readableApis(AppManager.getInstance().getTokenValue(this.tokenAddress, itemModel.getAddress()), ',', false));
                this.tokenSymbol.setText(AppManager.getInstance().getTokenSymbol(this.tokenAddress));
            }

            // 보안키 체크
            if(itemModel.isUsedProofKey()){
                StyleManager.fontColorStyle(this.walletAddress, StyleManager.AColor.C2b8a3e);
                icKnowledgekey.setVisible(true);
            }else{
                StyleManager.fontColorStyle(this.walletAddress, StyleManager.AColor.C999999);
                icKnowledgekey.setVisible(false);
            }

            setMask(itemModel.getMask());
            setCopyState(BODY_COPY_STATE_NONE);
        }

    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getWalletName() {
        if(this.model != null && this.model.getAlias() != null){
            return this.model.getAlias();
        }
        return "";
    }

    private void setMask(String mask){
        if(mask != null && mask.length() > 0){
            labelAddressMasking.setVisible(true);
            labelAddressMasking.setText(mask);
            btnAddressMasking.setVisible(false);
        }else{
            labelAddressMasking.setVisible(false);
            labelAddressMasking.setText("");
            btnAddressMasking.setVisible(true);
        }
    }

    private void setCopyState(int state){
        switch (state){
            case BODY_COPY_STATE_NONE :
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#999999").toString() );
                this.btnCopy.setVisible(false);
                this.walletAddress.setStyle( new JavaFXStyle(this.walletAddress.getStyle()).remove("-fx-underline").toString() );
                break;

            case BODY_COPY_STATE_NORMAL :
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#999999").toString() );
                this.btnCopy.setVisible(true);
                this.walletAddress.setStyle( new JavaFXStyle(this.walletAddress.getStyle()).add("-fx-underline","true").toString() );
                break;

            case BODY_COPY_STATE_ACTIVE :
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#910000").toString() );
                this.btnCopy.setVisible(true);
                this.walletAddress.setStyle( new JavaFXStyle(this.walletAddress.getStyle()).add("-fx-underline","true").toString() );
                break;
        }
    }

    public Node getRootPane() { return this.rootPane; }


    private TokenListBodyImpl handler;
    public void setHandler(TokenListBodyImpl handler){ this.handler = handler; }

    public interface TokenListBodyImpl{
        void onClickTransfer(InputEvent event, WalletItemModel model);
        void onClickCopy(String address);
        void onClickAddressMasking(InputEvent event, WalletItemModel model);
    }
}
