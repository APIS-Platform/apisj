package org.apis.gui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsContentsController implements Initializable {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label contentsHeader, contentsBody;
    @FXML private GridPane gridPane;
    @FXML private TextArea textArea;

    private boolean isCopyable = false;
    private String copyText = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        contentsBody.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isCopyable){
                    AppManager.copyClipboard(copyText);
                    PopupCopyTxHashController controller = (PopupCopyTxHashController)PopupManager.getInstance().showMainPopup("popup_copy_tx_hash.fxml",0);
                    controller.setHash(copyText);
                }
            }
        });

        contentsBody.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isCopyable){
                    contentsBody.setStyle(new JavaFXStyle(contentsBody.getStyle()).add("-fx-underline","true").toString());
                }
            }
        });

        contentsBody.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                contentsBody.setStyle(new JavaFXStyle(contentsBody.getStyle()).remove("-fx-underline").toString());
            }
        });
        textArea.setVisible(false);
    }

    public void bindContentsHeader(SimpleStringProperty contentsHeader) {
        this.contentsHeader.textProperty().bind(contentsHeader);
    }

    public void setContentsBody(String contentsBody) {
        this.contentsBody.setText(contentsBody);
        this.textArea.setText(contentsBody);
    }

    public void setOnClickCopyText(boolean isCopyable, String copyText){
        this.isCopyable = isCopyable;

        if(this.isCopyable){
            contentsBody.setCursor(Cursor.HAND);
        }else{
            contentsBody.setCursor(Cursor.DEFAULT);
        }

        if(copyText == null){
            this.copyText = contentsBody.getText();
        }else{
            this.copyText = copyText;
        }
    }
    public void setBgColor(String bgColor) {
        this.bgAnchor.setStyle("-fx-background-color: "+bgColor+";");
    }

    public void setTxtColor(String txtColor) {
        this.contentsBody.setStyle("-fx-text-fill: "+txtColor+"; -fx-font-family: 'Open Sans Regular'; -fx-font-size:12px;");
    }

    public void setHeight(int height){
        gridPane.setPrefHeight(height);
    }

    public void setTextAreaType(int height) {
        setHeight(height);
        textArea.setVisible(true);

    }
}
