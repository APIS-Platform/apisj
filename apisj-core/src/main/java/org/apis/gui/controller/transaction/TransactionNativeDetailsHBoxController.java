package org.apis.gui.controller.transaction;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.AddressUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsHBoxController extends BaseViewController {
    @FXML private HBox contentsBodyHBox;

    private Image frozen = new Image("image/ic_freeze@2x.png");
    private ImageView frozenImg = new ImageView(frozen);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.contentsBodyHBox.getChildren().clear();
        StyleManager.fontStyle(contentsBodyHBox, StyleManager.Standard.Regular, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
    }

    @Override
    public void fontUpdate() {
        StyleManager.fontStyle(contentsBodyHBox, StyleManager.Hex.Regular, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
    }

    public void addList(String group, String from, String to, String value) {
        String fromMask, toMask, shortFrom, shortTo;

        fromMask = AppManager.getInstance().getMaskWithAddress(from);
        toMask = AppManager.getInstance().getMaskWithAddress(to);
        shortFrom = AddressUtil.getShortAddress(from, 6);
        shortTo = AddressUtil.getShortAddress(to, 6);

        if (fromMask != null && fromMask.length() > 0) {
            shortFrom = shortFrom + " (" + fromMask + ")";
        }
        if (toMask != null && toMask.length() > 0) {
            shortTo = shortTo + " (" + toMask + ")";
        }

        // Add labels to HBox
        if(group.equals("internalTx")) {
            addItems(null, StringManager.getInstance().transaction.transferLabel, null);
            addItems(null, null, value);
            addItems(null, null, "APIS");
            addItems(null, StringManager.getInstance().transaction.fromLabel, null);
            addItems(from, null, shortFrom);
            addItems(null, StringManager.getInstance().transaction.toLabel, null);
            addItems(to, null, shortTo);
        }
        if(group.indexOf("token") == 0) {
            String[] tokenInfo = group.split(",");
            addItems(null, StringManager.getInstance().transaction.fromLabel, null);
            addItems(from, null, shortFrom);
            addItems(null, StringManager.getInstance().transaction.toLabel, null);
            addItems(to, null, shortTo);
            addItems(null, StringManager.getInstance().transaction.forLabel, null);
            addItems(null, null, value);
            if(tokenInfo.length > 1) {
                addItems(null, null, AppManager.getInstance().getTokenSymbol(tokenInfo[1]));
            }else{
                addItems(null, null, "APIS");
            }
        }
    }

    public void addItems(String copyText, SimpleStringProperty bindText, String setText) {
        AnchorPane anchorPane = new AnchorPane();
        Label label = new Label();
        boolean isFrozen = false;

        if(copyText != null) {
            label.setText(setText);
            label.setCursor(Cursor.HAND);
            if(!copyText.equals("")) {
                isFrozen = AppManager.getInstance().isFrozen(copyText);
                StyleManager.fontStyle(label, StyleManager.Hex.Regular, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
                if(isFrozen) {
                    label.setStyle(new JavaFXStyle(label.getStyle()).add("-fx-text-fill", "#4871ff").toString());
                } else {
                    label.setStyle(new JavaFXStyle(label.getStyle()).add("-fx-text-fill", "#b01e1e").toString());
                }
            }

            label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(null,"popup_copy.fxml", 0);
                    controller.setCopyWalletAddress(copyText);
                }
            });
            label.setOnMouseEntered(event -> label.setStyle(new JavaFXStyle(label.getStyle()).add("-fx-underline", "true").toString()));
            label.setOnMouseExited(event -> label.setStyle(new JavaFXStyle(label.getStyle()).remove("-fx-underline").toString()));

        } else {
            label.setMinWidth(Double.NEGATIVE_INFINITY);
            if(setText == null) {
                label.textProperty().bind(bindText);
            } else {
                label.setText(setText);
            }
        }

        AnchorPane.setBottomAnchor(label, 0.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        anchorPane.getChildren().add(label);

        this.contentsBodyHBox.getChildren().add(anchorPane);

        if(isFrozen) {
            frozenImg.setFitWidth(13);
            frozenImg.setFitHeight(14);
            this.contentsBodyHBox.setMargin(frozenImg, new Insets(2, 4, 0, 4));
            this.contentsBodyHBox.getChildren().add(frozenImg);
        }
    }

}
