package org.apis.gui.controller.transaction;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.popup.PopupCopyController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsHBoxController implements Initializable {
    @FXML private HBox contentsBodyHBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.contentsBodyHBox.getChildren().clear();
    }

    public void addList(String group, String from, String to, String value) {
        String fromMask, toMask, shortFrom, shortTo;

        fromMask = AppManager.getInstance().getMaskWithAddress(from);
        toMask = AppManager.getInstance().getMaskWithAddress(to);
        shortFrom = AddressUtil.getShortAddress(from);
        shortTo = AddressUtil.getShortAddress(to);

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
        if(group.equals("token")) {
            addItems(null, StringManager.getInstance().transaction.fromLabel, null);
            addItems(from, null, shortFrom);
            addItems(null, StringManager.getInstance().transaction.toLabel, null);
            addItems(to, null, shortTo);
            addItems(null, StringManager.getInstance().transaction.forLabel, null);
            addItems(null, null, value);
            addItems(null, null, "APIS");
        }
    }

    public void addItems(String copyText, SimpleStringProperty bindText, String setText) {
        AnchorPane anchorPane = new AnchorPane();
        Label label = new Label();

        if(copyText != null) {
            label.setText(setText);
            label.setCursor(Cursor.HAND);
            label.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 12px; -fx-text-fill: #910000;");

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
    }

}
