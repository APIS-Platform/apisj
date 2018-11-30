package org.apis.gui.controller.wallet.walletlist;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.AddressUtil;
import org.apis.util.blockchain.ApisUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class WalletListHeadController extends BaseViewController {
    public static final int HEADER_STATE_CLOSE = 0;
    public static final int HEADER_STATE_OPEN = 1;
    private int headerState = HEADER_STATE_CLOSE;

    private static final int HEADER_COPY_STATE_NONE = 0;
    private static final int HEADER_COPY_STATE_NORMAL = 1;
    private static final int HEADER_COPY_STATE_ACTIVE = 2;
    private boolean btnCopyClickedFlag = false;

    private boolean isChecked = false;
    private String prevOnMouseClickedEventFxid = "";

    private WalletItemModel model = new WalletItemModel();

    @FXML private AnchorPane rootPane;
    @FXML private ImageView walletIcon;
    @FXML private ImageView btnCheckBox, icAddressMasking, btnTransfer, foldIcon, icKnowledgekey;
    @FXML private Label btnCopy, labelWalletAlias, labelWalletAddress, labelAddressMasking, value, valueUnit;
    @FXML private Pane leftLine;
    @FXML private AnchorPane miningPane;
    @FXML private Label tagLabel, btnAddressMasking;

    private Image imageFold = ImageManager.icFold;
    private Image imageUnFold = ImageManager.icUnFold;
    private Image imageCheck = ImageManager.icCheck;
    private Image imageCheckGrayLine = ImageManager.icCheckGrayLine;
    private Image imageUnCheck = ImageManager.icUnCheck;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle(this.walletIcon.getFitWidth()-0.5,this.walletIcon.getFitHeight()-0.5);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        walletIcon.setClip(clip);

        setCopyState(HEADER_COPY_STATE_NONE);
        setCheck(false);
        setState(HEADER_STATE_CLOSE);

        setMask(null);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("rootPane")){
            if(handler != null
                    && ( prevOnMouseClickedEventFxid.equals("") || prevOnMouseClickedEventFxid.equals("rootPane"))){
                handler.onClickEvent(event, this.model);
            }

            prevOnMouseClickedEventFxid = "rootPane";
        }else if(id.equals("btnCheckBox")){
            setCheck(!this.isChecked);
            if(handler != null){
                handler.onChangeCheck(this.model, isChecked);
            }

            prevOnMouseClickedEventFxid = "btnCheckBox";
        }else if(id.equals("btnCopy")){
            btnCopyClickedFlag = true;

            prevOnMouseClickedEventFxid = "btnCopy";
            String text = this.model.getAddress();
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            if(handler != null){
                handler.onClickCopy(text, this.model);
            }

        }else if(id.equals("btnAddressMasking")){

            prevOnMouseClickedEventFxid = "btnAddressMasking";
            if(handler != null){
                handler.onClickAddressMasking(event, this.model);
            }
        }else if(id.equals("btnTransfer")){

            prevOnMouseClickedEventFxid = "btnTransfer";
            if(handler != null){
                handler.onClickTransfer(event, this.model);
            }
        }
    }
    @FXML
    public void onMouseEntered(InputEvent event){
        btnCopyClickedFlag = false;

        String id = ((Node)event.getSource()).getId();
        if(id.equals("paneAddress")){
            setCopyState(HEADER_COPY_STATE_NORMAL);
        }else if(id.equals("btnCopy")){
            setCopyState(HEADER_COPY_STATE_ACTIVE);
        }else if(id.equals("rootPane")){
            if(isChecked){
                btnCheckBox.setImage(imageCheck);
            }else{
                btnCheckBox.setImage(imageCheckGrayLine);
            }

            if(this.headerState == HEADER_STATE_CLOSE){
                rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#f8f8f8").toString());
            }else if(this.headerState == HEADER_STATE_OPEN){
                rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#eaeaea").toString());
            }

        }else if(id.equals("btnTransfer")){
            btnTransfer.setImage(ImageManager.btnAddTransferHover);
        }else if(id.equals("btnAddressMasking")){
            StyleManager.backgroundColorStyle(btnAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.borderColorStyle(btnAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(btnAddressMasking, StyleManager.AColor.C2b2b2b);
            icAddressMasking.setImage(ImageManager.icAddAddressMaskingHover);
        }
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("paneAddress")){
            setCopyState(HEADER_COPY_STATE_NONE);
        }else if(id.equals("btnCopy")){
            if(btnCopyClickedFlag == true) {
                setCopyState(HEADER_COPY_STATE_NONE);
            } else {
                setCopyState(HEADER_COPY_STATE_NORMAL);
            }
        }else if(id.equals("rootPane")){
            if(isChecked){
                btnCheckBox.setImage(imageCheck);
            }else{
                btnCheckBox.setImage(imageUnCheck);
            }

            if(this.headerState == HEADER_STATE_CLOSE){
                rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#ffffff").toString());
            }else if(this.headerState == HEADER_STATE_OPEN){
                rootPane.setStyle(new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#eaeaea").toString());
            }
        }else if(id.equals("btnTransfer")){
            btnTransfer.setImage(ImageManager.btnAddTransfer);
        }else if(id.equals("btnAddressMasking")){

            StyleManager.backgroundColorStyle(btnAddressMasking, StyleManager.AColor.Cf8f8f8);
            StyleManager.borderColorStyle(btnAddressMasking, StyleManager.AColor.Cd8d8d8);
            StyleManager.fontColorStyle(btnAddressMasking, StyleManager.AColor.C999999);
            icAddressMasking.setImage(ImageManager.icAddAddressMasking);
        }
    }

    public void setMask(String mask){
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

    @Override
    public void setModel(BaseModel model){
        if(model != null) {
            this.model.set((WalletItemModel)model);
            WalletItemModel itemModel = (WalletItemModel) this.model;

            this.walletIcon.setImage(ImageManager.getIdenticons(itemModel.getAddress()));
            this.labelWalletAlias.setText(itemModel.getAlias());
            this.labelWalletAddress.setText(AddressUtil.getShortAddress(itemModel.getAddress(), 8));
            this.value.setText(ApisUtil.readableApis(itemModel.getApis(), ',', false));
            setMask(itemModel.getMask());

            // 마이닝 / 마스터노드 체크
            if (itemModel.isMining()) {
                this.tagLabel.setVisible(true);
                this.tagLabel.setText("MINING");
                this.tagLabel.setPrefWidth(-1);
                GridPane.setMargin(this.tagLabel, new Insets(0, 4, 0, 0));
            } else if (itemModel.isMasterNode()) {
                this.tagLabel.setVisible(true);
                this.tagLabel.setText("MASTERNODE");
                this.tagLabel.setPrefWidth(-1);
                GridPane.setMargin(this.tagLabel, new Insets(0, 4, 0, 0));
            } else {
                this.tagLabel.setVisible(true);
                this.tagLabel.setText("");
                this.tagLabel.setPrefWidth(0);
                GridPane.setMargin(this.tagLabel, new Insets(0, 0, 0, 0));
            }

            // 보안키 체크
            if(itemModel.isUsedProofKey()){
                StyleManager.fontColorStyle(this.labelWalletAddress, StyleManager.AColor.C2b8a3e);
                icKnowledgekey.setVisible(true);
                icKnowledgekey.setFitWidth(14);
            }else{
                StyleManager.fontColorStyle(this.labelWalletAddress, StyleManager.AColor.C999999);
                icKnowledgekey.setVisible(false);
                icKnowledgekey.setFitWidth(1);
            }

        }

    }

    @Override
    public BaseModel getModel(){
        return this.model;
    }


    public void setCheck(boolean isChecked){
        this.isChecked = isChecked;
        if(isChecked){
            btnCheckBox.setImage(imageCheck);
        }else{
            btnCheckBox.setImage(imageUnCheck);
        }
    }

    private void setCopyState(int state){
        switch (state){
            case HEADER_COPY_STATE_NONE :
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#999999").toString() );
                this.btnCopy.setVisible(false);
                this.labelWalletAddress.setStyle( new JavaFXStyle(this.labelWalletAddress.getStyle()).remove("-fx-underline").toString() );
                break;

            case HEADER_COPY_STATE_NORMAL :
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#999999").toString() );
                this.btnCopy.setVisible(true);
                this.labelWalletAddress.setStyle( new JavaFXStyle(this.labelWalletAddress.getStyle()).add("-fx-underline","true").toString() );
                break;

            case HEADER_COPY_STATE_ACTIVE :
                this.btnCopy.setStyle( new JavaFXStyle(this.btnCopy.getStyle()).add("-fx-background-color","#910000").toString() );
                this.btnCopy.setVisible(true);
                this.labelWalletAddress.setStyle( new JavaFXStyle(this.labelWalletAddress.getStyle()).add("-fx-underline","true").toString() );
                break;
        }
    }

    public void setState(int state){
        this.headerState = state;
        switch (state){
            case HEADER_STATE_CLOSE :
                rootPane.setStyle( new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#ffffff").toString() );
                foldIcon.setImage(imageUnFold);
                rootPane.setEffect(null);
                leftLine.setVisible(false);
                break;

            case HEADER_STATE_OPEN :
                rootPane.setStyle( new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#eaeaea").toString() );
                foldIcon.setImage(imageFold);
                rootPane.setEffect(new DropShadow(10, Color.color(0,0,0,0.2)));
                leftLine.setVisible(true);
                break;
        }
    }

    private WalletListHeaderInterface handler;
    public void setHandler(WalletListHeaderInterface handler){this.handler = handler; }
    public interface WalletListHeaderInterface{
        void onClickEvent(InputEvent event, WalletItemModel model);
        void onClickTransfer(InputEvent event, WalletItemModel model);
        void onChangeCheck(WalletItemModel model, boolean isChecked);
        void onClickCopy(String address, WalletItemModel model);
        void onClickAddressMasking(InputEvent event, WalletItemModel model);
    }

}
