package org.apis.gui.controller.popup;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupResetController extends BasePopupController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label firstLabel, secondLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        bgAnchor.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(KeyCode.ESCAPE == event.getCode()) {
                    event.consume();
                }
            }
        });
    }

    private void languageSetting() {
        firstLabel.textProperty().bind(StringManager.getInstance().popup.resetFirstLabel);
        secondLabel.textProperty().bind(StringManager.getInstance().popup.resetSecondLabel);
    }
}
