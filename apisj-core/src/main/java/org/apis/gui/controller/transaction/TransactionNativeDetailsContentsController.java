package org.apis.gui.controller.transaction;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StyleManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TransactionNativeDetailsContentsController extends BaseViewController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label contentsHeader, contentsBody;
    @FXML private VBox contentsBodyVBox;
    @FXML private HBox contentsBodyList;
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
                    PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(null, "popup_copy.fxml", 0);
                    controller.setCopyWalletAddress(copyText);
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
        textArea.setPrefHeight(0);
    }

    public void bindContentsHeader(SimpleStringProperty contentsHeader) {
        this.contentsHeader.textProperty().bind(contentsHeader);
    }

    public void setContentsBody(String contentsBody, String fontFamilyName){
        this.contentsBody.setStyle(new JavaFXStyle(this.contentsBody.getStyle()).add("-fx-font-family", fontFamilyName).toString());
        setContentsBody(contentsBody);
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
            contentsBody.setCursor(Cursor.HAND);
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
        this.contentsBody.setStyle("-fx-text-fill: "+txtColor+"; -fx-font-family: 'Noto Sans CJK JP Regular'; -fx-font-size:12px;");
    }

    public void setHeight(int height){
        gridPane.setPrefHeight(height);
    }

    public void setTextAreaType(int height) {
        setHeight(height);
        textArea.setPrefHeight(-1);
        textArea.setVisible(true);
        contentsBody.setVisible(false);
    }

    public void contentsBodyVBoxClear() {
        this.contentsBodyVBox.getChildren().clear();
    }

    public void contentsBodyVBoxAdd(String group, String from, String to, String value) {
        try {
            URL hboxURL = getClass().getClassLoader().getResource("scene/transaction/transaction_native_details_hbox.fxml");
            FXMLLoader loader = new FXMLLoader(hboxURL);
            HBox hBox = loader.load();
            this.contentsBodyVBox.getChildren().add(hBox);

            TransactionNativeDetailsHBoxController controller = (TransactionNativeDetailsHBoxController) loader.getController();
            controller.addList(group, from, to, value);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFrozen() {
        Image frozen = new Image("image/ic_freeze@2x.png");
        ImageView frozenImg = new ImageView(frozen);

        frozenImg.setFitWidth(13);
        frozenImg.setFitHeight(14);
        contentsBodyList.getChildren().add(frozenImg);
        contentsBodyList.setMargin(frozenImg, new Insets(2, 4, 0, 4));
        setTxtColor(StyleManager.AColor.C4871ff);
    }

}
