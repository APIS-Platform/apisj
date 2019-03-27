package org.apis.gui.controller.popup;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupSuccessController extends BasePopupController {
    @FXML private Label title, subTitle, yesBtn;
    @FXML private AnchorPane bgAnchor;

    @FXML
    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex-1);
        PopupManager.getInstance().hideMainPopup(zIndex);
        setTitleColor(StyleManager.AColor.C000000);
        parentRequestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bgAnchor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER) {
                exit();
            }
        });

        languageSetting();
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.successTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.successSubTitle);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.successYes);
    }

    public void setTitle(SimpleStringProperty title){
        this.title.textProperty().unbind();
        this.title.textProperty().bind(title);
    }

    public void setSubTitle(SimpleStringProperty subTitle){
        this.subTitle.textProperty().unbind();
        this.subTitle.textProperty().bind(subTitle);
    }

    public void setYesBtn(SimpleStringProperty yesBtn){
        this.yesBtn.textProperty().unbind();
        this.yesBtn.textProperty().bind(yesBtn);
    }

    public void requestFocusYesButton(){
        yesBtn.requestFocus();
    }

    public void setTitleColor(String color) {
        StyleManager.fontColorStyle(title, color);
    }
}
