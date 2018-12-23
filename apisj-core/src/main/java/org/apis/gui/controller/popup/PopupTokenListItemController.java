package org.apis.gui.controller.popup;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Ellipse;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.ByteUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PopupTokenListItemController implements Initializable {
    // Contract Address List isSelected Flag
    private static final boolean NOT_SELECTED = false;
    private static final boolean SELECTED = true;

    private boolean listSelectedFlag = NOT_SELECTED;

    private PopupTokenListImpl handler;

    @FXML private ImageView addrCircleImg, frozenImg;
    @FXML private GridPane listGrid;
    @FXML private Label tokenName, tokenAddress;

    private TokenRecord record;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        listSelectedFlag = NOT_SELECTED;

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);

        addrCircleImg.setClip(ellipse);
        frozenImg.setVisible(false);

    }

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("selectBtn")) {
            if(!listSelectedFlag) {
                listGrid.setStyle("-fx-border-color: #f8f8fb; -fx-background-color: #ffffff;");
                listSelectedFlag = SELECTED;
            } else {
                listGrid.setStyle("-fx-border-color: #f8f8fb;");
                listSelectedFlag = NOT_SELECTED;
            }
        }

        else if(fxid.equals("edit")){
            if(handler != null){
                handler.onClickEdit();
            }
        }

        else if(fxid.equals("delete")){
            if(handler != null){
                handler.onClickDelete();
            }
        }

    }


    public PopupTokenListImpl getHandler() {
        return handler;
    }

    public void setHandler(PopupTokenListImpl handler) {
        this.handler = handler;
    }

    public void setData(TokenRecord record) {
        this.record = record;

        this.tokenName.setText(record.getTokenName()+" ("+record.getTokenSymbol()+")");
        this.tokenAddress.setText(ByteUtil.toHexString(record.getTokenAddress()));
        this.addrCircleImg.setImage(AppManager.getInstance().getTokenIcon(ByteUtil.toHexString(record.getTokenAddress())));

        if(tokenAddress != null && !tokenAddress.getText().equals("")) {
            if (AppManager.getInstance().isFrozen(tokenAddress.getText())) {
                frozenImg.setVisible(true);
                StyleManager.fontColorStyle(tokenAddress, StyleManager.AColor.C4871ff);
            } else {
                frozenImg.setVisible(false);
                StyleManager.fontColorStyle(tokenAddress, StyleManager.AColor.C999999);
            }
        }
    }

    public interface PopupTokenListImpl {
        void onClickEdit();
        void onClickDelete();
    }
}
