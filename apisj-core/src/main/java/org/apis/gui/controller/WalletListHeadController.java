package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.WalletItemModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class WalletListHeadController implements Initializable {
    public static final int HEADER_STATE_CLOSE = 0;
    public static final int HEADER_STATE_OPEN = 1;

    private static final int HEADER_COPY_STATE_NONE = 0;
    private static final int HEADER_COPY_STATE_NORMAL = 1;
    private static final int HEADER_COPY_STATE_ACTIVE = 2;

    private Image imageFold, imageUnFold;
    private Image imageCheck, imageUnCheck;


    private WalletItemModel model;
    private boolean isChecked = false;
    private String prevOnMouseClickedEventFxid = "";

    private WalletListHeaderInterface handler;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private ImageView walletIcon;

    @FXML
    private ImageView btnCheckBox, btnAddressMasking, btnTransfer, foldIcon;

    @FXML
    private Label btnCopy, labelWalletAlias, labelWalletAddress, valueNatural, valueDecimal, valueUnit;

    @FXML
    private Pane leftLine;

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
            prevOnMouseClickedEventFxid = "btnCheckBox";
        }else if(id.equals("btnCopy")){

            prevOnMouseClickedEventFxid = "btnCopy";
            String text = labelWalletAddress.getText();
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            if(handler != null){
                handler.onClickCopy(text);
            }

            setCopyState(HEADER_COPY_STATE_NONE);

        }else if(id.equals("btnAddressMasking")){

            prevOnMouseClickedEventFxid = "btnAddressMasking";
        }else if(id.equals("btnTransfer")){

            prevOnMouseClickedEventFxid = "btnTransfer";
            if(handler != null){
                handler.onClickTransfer(event);
            }
        }
    }
    @FXML
    public void onMouseEntered(InputEvent event){
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
            setCopyState(HEADER_COPY_STATE_NORMAL);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( walletIcon.getFitWidth(), walletIcon.getFitHeight() );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        walletIcon.setClip(clip);

        imageFold = new Image("image/btn_fold.png");
        imageUnFold = new Image("image/btn_unfold.png");
        imageCheck = new Image("image/btn_circle_click.png@2x.png");
        imageUnCheck = new Image("image/btn_circle_noneclick@2x.png");

        setCopyState(HEADER_COPY_STATE_NONE);
        setCheck(false);
        setState(HEADER_STATE_CLOSE);

        setBalance("0");
    }

    public void setModel(WalletItemModel model){
        this.model = model;

        labelWalletAlias.textProperty().bind(this.model.aliasProperty());
        labelWalletAddress.textProperty().bind(this.model.addressProperty());
        valueNatural.textProperty().bind(this.model.naturalProperty());
        valueDecimal.textProperty().bind(this.model.decimalProperty());
        valueUnit.textProperty().bind(this.model.unitProperty());

    }


    public void setCheck(boolean isChecked){
        this.isChecked = isChecked;
        if(isChecked){
            btnCheckBox.setImage(imageCheck);
        }else{
            btnCheckBox.setImage(imageUnCheck);
        }

        if(handler != null){
            handler.onChangeCheck(model, isChecked);
        }
    }

    private void setCopyState(int state){
        switch (state){
            case HEADER_COPY_STATE_NONE :
                this.btnCopy.setStyle("-fx-background-color:#999999;");
                this.btnCopy.setVisible(false);
                break;

            case HEADER_COPY_STATE_NORMAL :
                this.btnCopy.setStyle("-fx-background-color:#999999;");
                this.btnCopy.setVisible(true);
                break;

            case HEADER_COPY_STATE_ACTIVE :
                this.btnCopy.setStyle("-fx-background-color:#910000;");
                this.btnCopy.setVisible(true);
                break;
        }
    }

    public void setState(int state){
        switch (state){
            case HEADER_STATE_CLOSE :
                rootPane.setStyle("-fx-background-color : #ffffff; ");
                foldIcon.setImage(imageUnFold);
                rootPane.setEffect(null);
                leftLine.setVisible(false);
                break;

            case HEADER_STATE_OPEN :
                rootPane.setStyle("-fx-background-color : #eaeaea; ");
                foldIcon.setImage(imageFold);
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
    }
    public void setHandler(WalletListHeaderInterface handler){this.handler = handler;}

}
