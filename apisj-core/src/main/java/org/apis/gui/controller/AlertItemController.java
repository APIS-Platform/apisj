package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apis.gui.manager.NotificationManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AlertItemController implements Initializable {
    private NotificationManager.NotificationModel model;

    @FXML
    private ImageView icon;
    @FXML
    private Label title, subTitle, tag, time, text;
    @FXML
    private GridPane titlePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(NotificationManager.NotificationModel model){
        this.model = model;
        title.textProperty().setValue(this.model.getTitle());
        subTitle.textProperty().setValue(this.model.getSubTitle());
        tag.textProperty().setValue(this.model.getTag());
        text.textProperty().setValue(this.model.getText());

        if(this.model.getTitle() == null || this.model.getTitle().length() == 0){
            titlePane.getRowConstraints().remove(0);
            titlePane.getRowConstraints().remove(1);
            titlePane.getRowConstraints().remove(2);

        }

    }


}
