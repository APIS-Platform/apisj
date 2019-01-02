package org.apis.gui.controller.module.ledger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.ImageManager;

import java.net.URL;
import java.util.ResourceBundle;

public class DerivationPathItemController implements Initializable {
    @FXML private AnchorPane bgAnchor;
    @FXML private ImageView checkImg;
    @FXML private Label pathLabel, categoryLabel;

    protected boolean checked = false;

    private DerivationPathItemImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(handler != null) {
                    handler.clicked();
                }
            }
        });
    }

    public void init(String path, String category) {
        this.pathLabel.setText(path);
        this.categoryLabel.setText(category);
        this.checkImg.setImage(ImageManager.checkGrey);
        checked = false;
    }

    public void check() {
        checked = true;
        checkImg.setImage(ImageManager.checkRed);
    }

    public void unCheck() {
        checked = false;
        checkImg.setImage(ImageManager.checkGrey);
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setHandler(DerivationPathItemImpl handler) {
        this.handler = handler;
    }

    public interface DerivationPathItemImpl {
        void clicked();
    }
}
