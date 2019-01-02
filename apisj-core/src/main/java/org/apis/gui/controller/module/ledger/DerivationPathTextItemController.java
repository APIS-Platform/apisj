package org.apis.gui.controller.module.ledger;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DerivationPathTextItemController extends DerivationPathItemController {
    @FXML private AnchorPane bgAnchor;
    @FXML private ImageView checkImg;
    @FXML private Label categoryLabel;
    @FXML private TextField derivationTextField1, derivationTextField2, derivationTextField3, derivationTextField4;

    private ArrayList<TextField> textFields = new ArrayList<TextField>();
    private DerivationPathItemImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        textFields.add(derivationTextField1);
        textFields.add(derivationTextField2);
        textFields.add(derivationTextField3);
        textFields.add(derivationTextField4);

        AppManager.settingTextFieldStyle(derivationTextField1);
        AppManager.settingTextFieldStyle(derivationTextField2);
        AppManager.settingTextFieldStyle(derivationTextField3);
        AppManager.settingTextFieldStyle(derivationTextField4);

        bgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(handler != null) {
                    handler.clicked();
                }
            }
        });
    }

    public void init(SimpleStringProperty category) {
        this.categoryLabel.textProperty().bind(category);
    }

    @Override
    public void check() {
        checked = true;
        this.checkImg.setImage(ImageManager.checkRed);
    }

    @Override
    public void unCheck() {
        checked = false;
        this.checkImg.setImage(ImageManager.checkGrey);
    }

    public void setHandler(DerivationPathItemImpl handler) {
        this.handler = handler;
    }
}
