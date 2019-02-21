package org.apis.gui.controller.wallet.tokenlist;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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
    @FXML private ImageView walletIcon, icAddressMasking, icTransfer, icKnowledgekey, icLedger;
    @FXML private Label btnAddressMasking, btnTransfer;
    @FXML private Pane bottomLine;

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
        }else if(id.equals("labelAddressMasking")) {
            String text = this.labelAddressMasking.getText();
            if(this.handler != null) {
                this.handler.onClickCopyMask(text);
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
            StyleManager.backgroundColorStyle(btnAddressMasking, StyleManager.AColor.Ce2e2e2);
            StyleManager.borderColorStyle(labelAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(btnAddressMasking, StyleManager.AColor.C2b2b2b);
            icAddressMasking.setImage(ImageManager.icAddAddressMaskingHover);

        }else if(id.equals("btnTransfer")){
            icTransfer.setImage(ImageManager.icTransferHover);
            StyleManager.backgroundColorStyle(btnTransfer, StyleManager.AColor.Ce2e2e2);
            StyleManager.borderColorStyle(btnTransfer, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(btnTransfer, StyleManager.AColor.C2b2b2b);
        }else if(id.equals("labelAddressMasking")) {
            StyleManager.backgroundColorStyle(labelAddressMasking, StyleManager.AColor.Ce2e2e2);
            StyleManager.borderColorStyle(labelAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(labelAddressMasking, StyleManager.AColor.C2b2b2b);
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
            StyleManager.backgroundColorStyle(btnAddressMasking, StyleManager.AColor.Cefefef);
            StyleManager.borderColorStyle(btnAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(btnAddressMasking, StyleManager.AColor.C999999);
            icAddressMasking.setImage(ImageManager.icAddAddressMasking);
        }else if(id.equals("btnTransfer")){
            icTransfer.setImage(ImageManager.icTransfer);
            StyleManager.backgroundColorStyle(btnTransfer, StyleManager.AColor.Cefefef);
            StyleManager.borderColorStyle(btnTransfer, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(btnTransfer, StyleManager.AColor.C999999);
        }else if(id.equals("labelAddressMasking")){
            StyleManager.backgroundColorStyle(labelAddressMasking, StyleManager.AColor.Cefefef);
            StyleManager.borderColorStyle(labelAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(labelAddressMasking, StyleManager.AColor.C2b2b2b);
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
            this.walletIcon.setImage(AppManager.getInstance().getTokenIcon(itemModel.getAddress()));
            this.walletAlias.setText(itemModel.getAlias());
            this.walletAddress.setText(AddressUtil.getShortAddress(itemModel.getAddress(), 6));

            if(this.tokenAddress.equals("-1")){
                this.walletValue.setText(ApisUtil.readableApis(itemModel.getApis(), ',', true));
                this.tokenSymbol.setText("APIS");
            }else if(this.tokenAddress.equals("-2")){
                this.walletValue.setText(ApisUtil.readableApis(itemModel.getMineral(), ',', true));
                this.tokenSymbol.setText("MNR");
            }else{
                this.walletValue.setText(ApisUtil.readableApis(AppManager.getInstance().getTokenValue(this.tokenAddress, itemModel.getAddress()), ',', true));
                this.tokenSymbol.setText(AppManager.getInstance().getTokenSymbol(this.tokenAddress));
            }

            // 보안키 체크
            if(itemModel.isUsedProofKey()){
                StyleManager.fontColorStyle(this.walletAddress, StyleManager.AColor.C2b8a3e);
                icKnowledgekey.setVisible(true);
                icKnowledgekey.setFitWidth(14);
                GridPane.setMargin(icKnowledgekey, new Insets(0, 0, 0, 4));
            }else{
                StyleManager.fontColorStyle(this.walletAddress, StyleManager.AColor.C999999);
                icKnowledgekey.setVisible(false);
                icKnowledgekey.setFitWidth(0.1);
                GridPane.setMargin(icKnowledgekey, new Insets(0, 0, 0, 0));
            }

            // 렛저 체크
            if(AppManager.getInstance().isLedger(itemModel.getAddress())){
                icLedger.setVisible(true);
                icLedger.setFitWidth(25);
                GridPane.setMargin(icLedger, new Insets(2, 0, 2, 4));
            }else{
                icLedger.setVisible(false);
                icLedger.setFitWidth(0.1);
                GridPane.setMargin(icLedger, new Insets(0, 0, 0, 0));
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
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#b01e1e").toString() );
                this.btnCopy.setVisible(true);
                this.walletAddress.setStyle( new JavaFXStyle(this.walletAddress.getStyle()).add("-fx-underline","true").toString() );
                break;
        }
    }

    public void setBottomLineVisible(boolean isVisible){
        bottomLine.setVisible(isVisible);
    }

    public Node getRootPane() { return this.rootPane; }


    private TokenListBodyImpl handler;
    public void setHandler(TokenListBodyImpl handler){ this.handler = handler; }

    public interface TokenListBodyImpl{
        void onClickTransfer(InputEvent event, WalletItemModel model);
        void onClickCopy(String address);
        void onClickAddressMasking(InputEvent event, WalletItemModel model);
        void onClickCopyMask(String mask);
    }
}
