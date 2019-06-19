package org.apis.gui.controller.popup;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.common.QRCodeGenerator;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.StringManager;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyController extends BasePopupController implements ClipboardOwner {
    @FXML private AnchorPane copyPk;
    @FXML private Label titleLabel, subTitleLabel, copyTextLabel, confirmButton;
    @FXML private ImageView QRCode;

    private Clipboard clipboard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        confirmButton.textProperty().bind(StringManager.getInstance().common.okButton);

        this.copyPk.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if(event.getCode() == KeyCode.ENTER) {
                exit();
            }
        });
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // Wait for taking back clipboard's ownership
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Regain ownership
        this.clipboard.setContents(contents, this);
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
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
        clipboard.setContents(stringSelection, stringSelection);
        this.copyTextLabel.textProperty().setValue(text);

        try {
            QRCode.setImage(QRCodeGenerator.generateQRCodeImage(text, 100, 100));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
