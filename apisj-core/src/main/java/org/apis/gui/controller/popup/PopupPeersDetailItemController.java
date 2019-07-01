package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupPeersDetailItemController extends BaseViewController {
    @FXML private Label head, contents;

    public PopupPeersDetailItemController() {}
    public PopupPeersDetailItemController(String head, String contents) {
        this.head.setText(head);
        this.contents.setText(contents);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public String getHeadText() {
        return this.head.getText();
    }

    public void setHeadText(String head) {
        this.head.setText(head);
    }

    public String getContentsText() {
        return this.contents.getText();
    }

    public void setContentsText(String contents) {
        this.contents.setText(contents);
    }
}
