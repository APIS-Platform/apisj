package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apis.gui.controller.base.BaseViewController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class onScreenKeyboardController implements Initializable {
    @FXML
    private HBox row1, row2, row3, row4;
    @FXML
    private ImageView shiftImg, backspaceImg, refreshImg;
    @FXML
    private AnchorPane shift, backspace, refresh;
    @FXML
    private Label changeType, space;

    private Image shiftEmpty, shiftFillBlack, shiftFillWhite, backspaceBlack, backspaceWhite, refreshBlack, refreshWhite;
    private ArrayList<onScreenKeyboardItemController> rowOneItems = new ArrayList<>();

    private URL fxmlUrl;
    private FXMLLoader loader;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Image
        imageSetting();

        // Add row item
        this.fxmlUrl = getClass().getClassLoader().getResource("scene/on_screen_keyboard_item.fxml");
        addRow();

        // Add event listener

    }

    private void imageSetting() {
        shiftEmpty = new Image("image/ic_up_empty.png");
        shiftFillBlack = new Image("image/ic_up_fill_black.png");
        shiftFillWhite = new Image("image/ic_up_fill_white.png");
        backspaceBlack = new Image("image/ic_backspace_black.png");
        backspaceWhite = new Image("image/ic_backspace_white.png");
        refreshBlack = new Image("image/ic_refresh_black.png");
        refreshWhite = new Image("image/ic_refresh_white.png");
    }

    private void addRow() {
        addRowOne();
        addRowTwo();
        addRowThree();
        addRowFour();
    }

    private void addRowOne() {
        row1.getChildren().clear();

        // Key relocation for secure
        Random r = new Random();
        int space = r.nextInt(12);
        int space2 = r.nextInt(12);
        while(space != space2) {
            space2 = r.nextInt(12);
        }

        try {
            int j = 1;
            for(int i=0; i<12; i++) {
                this.loader = new FXMLLoader(fxmlUrl);
                Node node = this.loader.load();
                rowOneItems.add(this.loader.getController());

                if(i == space || i == space2) {
                    rowOneItems.get(i).setItemLabel("");
                    rowOneItems.get(i).setEmpty();
                } else {
                    rowOneItems.get(i).setItemLabel(Integer.toString(j));
                    j++;
                }

                row1.getChildren().add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRowTwo() {

    }

    private void addRowThree() {

    }

    private void addRowFour() {

    }
}
