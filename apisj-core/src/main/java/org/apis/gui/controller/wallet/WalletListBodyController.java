package org.apis.gui.controller.wallet;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.ImageManager;
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

    @FXML private AnchorPane rootPane;

    // 토큰 타입
    @FXML private GridPane tokenPane;
    @FXML private ImageView tokenIcon;
    @FXML private Label tokenName, tokenValue, tokenSymbol, noTransaction;

    // 지갑 타입
    @FXML private GridPane walletPane;
    @FXML private Label walletAlias, walletAddress, walletValue, btnCopy, walletApis, labelAddressMasking;
    @FXML private AnchorPane miningPane;
    @FXML private ImageView walletIcon, btnTransfer, btnAddressMasking;

    private WalletListGroupController.GroupType groupType = WalletListGroupController.GroupType.WALLET;

    private static final int BODY_COPY_STATE_NONE = 0;
    private static final int BODY_COPY_STATE_NORMAL = 1;
    private static final int BODY_COPY_STATE_ACTIVE = 2;
    private boolean btnCopyClickedFlag = false;

    private WalletItemModel model;
    private Image apisIcon = ImageManager.apisIcon;
    private Image mineraIcon = ImageManager.mineraIcon;
    private BigInteger tokenValueBigInt;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // set a clip to apply rounded border to the original image.
        Rectangle tokenIconClip = new Rectangle( this.tokenIcon.getFitWidth()-0.5, this.tokenIcon.getFitHeight()-0.5 );
        tokenIconClip.setArcWidth(30);
        tokenIconClip.setArcHeight(30);
        tokenIcon.setClip(tokenIconClip);

        Rectangle walletIconClip = new Rectangle( this.walletIcon.getFitWidth()-0.5, this.walletIcon.getFitHeight()-0.5 );
        walletIconClip.setArcWidth(30);
        walletIconClip.setArcHeight(30);
        walletIcon.setClip(walletIconClip);

        setGroupType(WalletListGroupController.GroupType.WALLET);

        setMask(null);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("rootPane")){
        }else if(id.equals("btnCheckBox")){
        }else if(id.equals("btnCopy")){
            btnCopyClickedFlag = true;

            String text = walletAddress.getText();
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            if(this.handler != null){
                this.handler.onClickCopy(text);
            }

        }else if(id.equals("btnAddressMasking")){
            if(this.handler != null){
                this.handler.onClickAddressMasking(event);
            }
        }else if(id.equals("btnTransfer")){
            if(this.handler != null){
                this.handler.onClickTransfer(event);
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
        }
    }

    public void setGroupType(WalletListGroupController.GroupType groupType){
        this.groupType = groupType;

        if(this.groupType == WalletListGroupController.GroupType.WALLET){
            tokenPane.setVisible(true);
            walletPane.setVisible(false);
        }else if(this.groupType == WalletListGroupController.GroupType.TOKEN){
            tokenPane.setVisible(false);
            walletPane.setVisible(true);
        }
    }

    public BigInteger getValue(){
        if(this.groupType == WalletListGroupController.GroupType.WALLET){
            return this.model.getApis();
        }else if(this.groupType == WalletListGroupController.GroupType.TOKEN){
            return tokenValueBigInt;
        }
        return BigInteger.ZERO;
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
        this.model = (WalletItemModel)model;

        this.tokenSymbol.setText(this.model.getTokenSymbol());
        this.tokenName.setText(this.model.getTokenName());
        this.tokenValue.setText(ApisUtil.readableApis(this.model.getTokenValue(),',',false));
        this.walletAlias.setText(this.model.getAlias());
        this.walletIcon.setImage(ImageManager.getIdenticons(this.model.getAddress()));

        if(this.model.getTokenName().toLowerCase().equals("apis")){
            this.tokenIcon.setImage(ImageManager.apisIcon);
        }else if(this.model.getTokenName().toLowerCase().equals("mineral")){
            this.tokenIcon.setImage(ImageManager.mineraIcon);
        }else {
            this.tokenIcon.setImage(ImageManager.getIdenticons(this.model.getTokenAddress()));
        }


        setMask(this.model.getMask());

        setCopyState(BODY_COPY_STATE_NONE);

    }
    public WalletItemModel getModel() { return this.model; }

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


    private WalletListBodyInterface handler;
    public void setHandler(WalletListBodyInterface handler){ this.handler = handler; }
    public interface WalletListBodyInterface{
        void onClickEvent(InputEvent event);
        void onClickTransfer(InputEvent event);
        void onChangeCheck(WalletItemModel model, boolean isChecked);
        void onClickCopy(String address);
        void onClickAddressMasking(InputEvent event);
    }
}
