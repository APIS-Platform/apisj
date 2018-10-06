package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.NotificationManager;
import org.apis.gui.model.NotificationModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class AlertItemController extends BaseViewController {
    private NotificationModel model;

    @FXML
    private ImageView icon;
    @FXML
    private Label title, subTitle, tag, time, text;
    @FXML
    private GridPane titlePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void setModel(BaseModel model){
        this.model = (NotificationModel)model;
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
