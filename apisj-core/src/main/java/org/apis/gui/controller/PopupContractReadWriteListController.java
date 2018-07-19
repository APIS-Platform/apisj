package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Ellipse;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteListController implements Initializable {
    // Contract Address List isSelected Flag
    private static final boolean NOT_SELECTED = false;
    private static final boolean SELECTED = true;

    private boolean listSelectedFlag = NOT_SELECTED;

    @FXML
    private ImageView selectBtn, addrCircleImg;
    @FXML
    private GridPane listGrid;

    private Image circleGrey, checkCircleRed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        circleGrey = new Image("image/btn_circle_grey@2x.png");
        checkCircleRed = new Image("image/btn_circle_click@2x.png");

        listSelectedFlag = NOT_SELECTED;
        listGrid.setStyle("-fx-border-color: #f2f2f2;");
        selectBtn.setImage(circleGrey);

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);

        addrCircleImg.setClip(ellipse);
    }

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("selectBtn")) {
            if(!listSelectedFlag) {
                listGrid.setStyle("-fx-border-color: #f2f2f2; -fx-background-color: #ffffff;");
                selectBtn.setImage(checkCircleRed);
                listSelectedFlag = SELECTED;
            } else {
                listGrid.setStyle("-fx-border-color: #f2f2f2;");
                selectBtn.setImage(circleGrey);
                listSelectedFlag = NOT_SELECTED;
            }
        }

    }

}
