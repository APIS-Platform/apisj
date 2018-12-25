package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BasePopupController;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadingController extends BasePopupController {

    @FXML private AnchorPane rootPane;
    @FXML private Label label;
    @FXML private Label subMessageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

}
