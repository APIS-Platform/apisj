package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.StringManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyController extends BasePopupController {
    @FXML
    private Label titleLabel, subTitleLabel, copyTextLabel, confirmButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        confirmButton.textProperty().bind(StringManager.getInstance().common.okButton);
    }

    public void setTitle(String title){
        this.titleLabel.textProperty().unbind();
        this.titleLabel.setText(title);
    }

    public void setSubTitle(String subTitleLabel){
        this.subTitleLabel.textProperty().unbind();
        this.subTitleLabel.setText(subTitleLabel);
    }

    public void setText(String text){
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        this.copyTextLabel.textProperty().setValue(text);
    }

    public void setCopyPk(String text){
        this.titleLabel.textProperty().bind(StringManager.getInstance().popup.copyPkTitle);
        this.subTitleLabel.textProperty().bind(StringManager.getInstance().popup.copyPkSubTitle);
        setText(text);
    }

    public void setCopyWalletAddress(String text){
        this.titleLabel.textProperty().bind(StringManager.getInstance().popup.copyWalletAddressTitle);
        this.subTitleLabel.textProperty().bind(StringManager.getInstance().popup.copyWalletAddressSubTitle);
        setText(text);
    }

    public void setCopyTxHash(String text){
        this.titleLabel.textProperty().bind(StringManager.getInstance().popup.copyTxHashTitle);
        this.subTitleLabel.textProperty().bind(StringManager.getInstance().popup.copyTxHashSubTitle);
        setText(text);
    }

    public void setCopyMask(String text) {
        this.titleLabel.textProperty().bind(StringManager.getInstance().popup.copyMaskTitle);
        this.subTitleLabel.textProperty().bind(StringManager.getInstance().popup.copyMaskSubTitle);
        setText(text);
    }
}
