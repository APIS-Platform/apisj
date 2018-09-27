package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.WalletItemModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class WalletListHeadController implements Initializable {
    public static final int WALLET_LIST_HEADER = 0;  // 첫번째 TAB HEADER
    public static final int TOKEN_LIST_HEADER_TYPE_APIS = 1;    // 두번째 TAB HEADER (APIS)
    public static final int TOKEN_LIST_HEADER_TYPE_MINERAL = 2;    // 두번째 TAB HEADER (MINERAL)
    private int headerType = WALLET_LIST_HEADER;

    public static final int HEADER_STATE_CLOSE = 0;
    public static final int HEADER_STATE_OPEN = 1;

    private static final int HEADER_COPY_STATE_NONE = 0;
    private static final int HEADER_COPY_STATE_NORMAL = 1;
    private static final int HEADER_COPY_STATE_ACTIVE = 2;
    private boolean btnCopyClickedFlag = false;

    private Image imageFold, imageUnFold;
    private Image imageCheck, imageUnCheck;


    private WalletItemModel model;
    private Image apisIcon, mineraIcon;
    private boolean isChecked = false;
    private String prevOnMouseClickedEventFxid = "";

    private WalletListHeaderInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private GridPane groupTypePane, unitTypePane;

    @FXML
    private ImageView walletIcon;
    @FXML
    private ImageView btnCheckBox, btnAddressMasking, btnTransfer, foldIcon;
    @FXML
    private Label btnCopy, labelWalletAlias, labelWalletAddress, labelAddressMasking, valueNatural, valueDecimal, valueUnit;
    @FXML
    private Pane leftLine;
    @FXML
    private AnchorPane miningPane;
    @FXML private Label tagLabel;

    @FXML
    private ImageView walletIcon1, foldIcon1;
    @FXML
    private Label name, valueNatural1, valueDecimal1, valueUnit1;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apisIcon = new Image("image/ic_apis@2x.png");
        mineraIcon = new Image("image/ic_mineral@2x.png");

        imageFold = new Image("image/btn_fold@2x.png");
        imageUnFold = new Image("image/btn_unfold@2x.png");
        imageCheck = new Image("image/btn_circle_red@2x.png");
        imageUnCheck = new Image("image/btn_circle_none@2x.png");

        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle(this.walletIcon.getFitWidth()-0.5,this.walletIcon.getFitHeight()-0.5);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        walletIcon.setClip(clip);

        setCopyState(HEADER_COPY_STATE_NONE);
        setCheck(false);
        setState(HEADER_STATE_CLOSE);

        setBalance("0");
        setMask(null);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("rootPane")){
            if(handler != null
                    && ( prevOnMouseClickedEventFxid.equals("") || prevOnMouseClickedEventFxid.equals("rootPane"))){
                handler.onClickEvent(event);
            }

            prevOnMouseClickedEventFxid = "rootPane";
        }else if(id.equals("btnCheckBox")){
            setCheck(!this.isChecked);
            if(handler != null){
                handler.onChangeCheck(model, isChecked);
            }

            prevOnMouseClickedEventFxid = "btnCheckBox";
        }else if(id.equals("btnCopy")){
            btnCopyClickedFlag = true;

            prevOnMouseClickedEventFxid = "btnCopy";
            String text = labelWalletAddress.getText();
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            if(handler != null){
                handler.onClickCopy(text);
            }

        }else if(id.equals("btnAddressMasking")){

            prevOnMouseClickedEventFxid = "btnAddressMasking";
            if(handler != null){
                handler.onClickAddressMasking(event);
            }
        }else if(id.equals("btnTransfer")){

            prevOnMouseClickedEventFxid = "btnTransfer";
            if(handler != null){
                handler.onClickTransfer(event);
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

    public WalletListHeadController init(int type){
        this.headerType = type;

        switch (this.headerType){
            case TOKEN_LIST_HEADER_TYPE_APIS: case TOKEN_LIST_HEADER_TYPE_MINERAL:
                unitTypePane.setVisible(true);
                groupTypePane.setVisible(false);
                break;

            case WALLET_LIST_HEADER:
                unitTypePane.setVisible(false);
                groupTypePane.setVisible(true);

                break;
        }
        return this;
    }

    public void setModel(WalletItemModel model){
        this.model = model;
        this.walletIcon.setImage(this.model.getIdenticon());

        setMask(model.getMask());

        switch (this.headerType){
            case TOKEN_LIST_HEADER_TYPE_APIS:
                walletIcon1.setImage(apisIcon);
                name.setText(WalletItemModel.WALLET_NAME_APIS);
                valueNatural1.textProperty().bind(this.model.totalApisNaturalProperty());
                valueDecimal1.textProperty().bind(this.model.totalApisDecimalProperty());
                valueUnit1.setText(WalletItemModel.UNIT_TYPE_STRING_APIS);

                break;
            case TOKEN_LIST_HEADER_TYPE_MINERAL:
                walletIcon1.setImage(mineraIcon);
                name.setText(WalletItemModel.WALLET_NAME_MINERAL);
                valueNatural1.textProperty().bind(this.model.totalMineralNaturalProperty());
                valueDecimal1.textProperty().bind(this.model.totalMineralDecimalProperty());
                valueUnit1.setText(WalletItemModel.UNIT_TYPE_STRING_MINERAL);
                break;
            case WALLET_LIST_HEADER:
                labelWalletAlias.textProperty().bind(this.model.aliasProperty());
                labelWalletAddress.textProperty().bind(this.model.addressProperty());
                valueNatural.setText(AppManager.commaSpace(this.model.naturalProperty().get()));
                valueDecimal.textProperty().bind(this.model.decimalProperty());
                valueUnit.textProperty().bind(this.model.unitProperty());

                if(this.model.isMining()){
                    tagLabel.setText("MINING");
                    tagLabel.setVisible(true);
                    tagLabel.setPrefWidth(-1);
                    GridPane.setMargin(tagLabel, new Insets(0,4,0,0));
                }else if(this.model.isMasterNode()){
                    tagLabel.setText("MASTERNODE");
                    tagLabel.setVisible(true);
                    tagLabel.setPrefWidth(-1);
                    GridPane.setMargin(tagLabel, new Insets(0,4,0,0));
                }else{
                    tagLabel.setText("");
                    tagLabel.setVisible(false);
                    tagLabel.setPrefWidth(0);
                    GridPane.setMargin(tagLabel, new Insets(0,0,0,0));
                }

                break;
        }
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
        switch (state){
            case HEADER_STATE_CLOSE :
                rootPane.setStyle( new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#ffffff").toString() );
                foldIcon.setImage(imageUnFold);
                foldIcon1.setImage(imageUnFold);
                rootPane.setEffect(null);
                leftLine.setVisible(false);
                break;

            case HEADER_STATE_OPEN :
                rootPane.setStyle( new JavaFXStyle(rootPane.getStyle()).add("-fx-background-color", "#eaeaea").toString() );
                foldIcon.setImage(imageFold);
                foldIcon1.setImage(imageFold);
                rootPane.setEffect(new DropShadow(10, Color.color(0,0,0,0.2)));
                leftLine.setVisible(true);
                break;
        }
    }

    public void setBalance(String balance){
        if(balance == null) return;

        String newBalance = AppManager.addDotWidthIndex(balance);
        String[] splitBalance = newBalance.split("\\.");

        valueNatural.setText(splitBalance[0]);
        valueDecimal.setText("."+splitBalance[1]);
    }


    public interface WalletListHeaderInterface{
        void onClickEvent(InputEvent event);
        void onClickTransfer(InputEvent event);
        void onChangeCheck(WalletItemModel model, boolean isChecked);
        void onClickCopy(String address);
        void onClickAddressMasking(InputEvent event);
    }
    public void setHandler(WalletListHeaderInterface handler){this.handler = handler; }

}
