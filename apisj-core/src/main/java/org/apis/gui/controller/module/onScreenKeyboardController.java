package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apis.gui.controller.base.BaseViewController;
import sun.plugin.javascript.navig.Anchor;

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

    private URL fxmlUrl = getClass().getClassLoader().getResource("scene/module/on_screen_keyboard_item.fxml");;
    private FXMLLoader loader;

    private boolean shiftMouseFocusFlag, backspaceMouseFoucsFlag, changeTypeMouseFocusFlag, spaceMouseFocusFlag, refreshMouseFocusFlag;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Image
        imageSetting();

        // Add row item
        addRow();
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
        while(space == space2) {
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
                    if(j == 10) {
                        rowOneItems.get(i).setItemLabel("0");
                    } else {
                        rowOneItems.get(i).setItemLabel(Integer.toString(j));
                    }
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

    @FXML
    public void onMouseEntered(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            shift.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #2b2b2b;");
            shiftImg.setImage(shiftFillWhite);
            shiftMouseFocusFlag = true;

        } else if(fxid.equals("backspace")) {
            backspace.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #2b2b2b;");
            backspaceImg.setImage(backspaceWhite);
            backspaceMouseFoucsFlag = true;

        } else if(fxid.equals("changeType")) {
            changeType.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
            changeTypeMouseFocusFlag = true;

        } else if(fxid.equals("space")) {
            space.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
            spaceMouseFocusFlag = true;

        } else if(fxid.equals("refresh")) {
            refresh.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #2b2b2b;");
            refreshImg.setImage(refreshWhite);
            refreshMouseFocusFlag = true;
        }
    }

    @FXML
    public void onMouseExited(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            shift.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #f2f2f2;");
            shiftImg.setImage(shiftFillBlack);
            shiftMouseFocusFlag = false;

        } else if(fxid.equals("backspace")) {
            backspace.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #f2f2f2;");
            backspaceImg.setImage(backspaceBlack);
            backspaceMouseFoucsFlag = false;

        } else if(fxid.equals("changeType")) {
            changeType.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #f2f2f2; -fx-text-fill: #202020;");
            changeTypeMouseFocusFlag = false;

        } else if(fxid.equals("space")) {
            space.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #ffffff; -fx-text-fill: #202020;");
            spaceMouseFocusFlag = false;

        } else if(fxid.equals("refresh")) {
            refresh.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #f2f2f2;");
            refreshImg.setImage(refreshBlack);
            refreshMouseFocusFlag = false;
        }
    }

    @FXML
    public void onMousePressed(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            shift.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #910000;");

        } else if(fxid.equals("backspace")) {
            backspace.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #910000;");

        } else if(fxid.equals("changeType")) {
            changeType.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #910000; -fx-text-fill: #ffffff;");

        } else if(fxid.equals("space")) {
            space.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                    " -fx-background-color: #910000; -fx-text-fill: #ffffff;");

        } else if(fxid.equals("refresh")) {
            refresh.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #910000;");
        }
    }

    @FXML
    public void onMouseReleased(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            if(shiftMouseFocusFlag) {
                shift.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #2b2b2b;");
                shiftImg.setImage(shiftFillWhite);
            } else {
                shift.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #f2f2f2;");
                shiftImg.setImage(shiftFillBlack);
            }

        } else if(fxid.equals("backspace")) {
            if(backspaceMouseFoucsFlag) {
                backspace.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #2b2b2b;");
                backspaceImg.setImage(backspaceWhite);
            } else {
                backspace.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #f2f2f2;");
                backspaceImg.setImage(backspaceBlack);
            }

        } else if(fxid.equals("changeType")) {
            if(changeTypeMouseFocusFlag) {
                changeType.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
            } else {
                changeType.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #f2f2f2; -fx-text-fill: #202020;");
            }

        } else if(fxid.equals("space")) {
            if(spaceMouseFocusFlag) {
                space.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
            } else {
                space.setStyle("-fx-font-family: 'Open Sans Bold'; -fx-font-size:16px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                        " -fx-background-color: #ffffff; -fx-text-fill: #202020;");
            }

        } else if(fxid.equals("refresh")) {
            if(refreshMouseFocusFlag) {
                refresh.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #2b2b2b;");
                refreshImg.setImage(refreshWhite);
            } else {
                refresh.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-background-color: #f2f2f2;");
                refreshImg.setImage(refreshBlack);
            }
        }
    }

}
