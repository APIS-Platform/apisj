package org.apis.gui.controller.module;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.*;
import org.apis.gui.manager.StyleManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class OnScreenKeyboardController implements Initializable {
    // Caret reposition [need / not]
    public static final boolean CARET_MAIN = false;
    public static final boolean CARET_INTRO = true;
    private boolean caretFlag = CARET_MAIN;

    @FXML
    private HBox row1, row2, row3, row4;
    @FXML
    private ImageView shiftImg, backspaceImg, refreshImg;
    @FXML
    private AnchorPane shift, backspace, refresh;
    @FXML
    private Label changeType, space;

    private Image shiftEmpty, shiftFillBlack, shiftFillWhite, backspaceBlack, backspaceWhite, refreshBlack, refreshWhite;
    private ArrayList<OnScreenKeyboardItemController> rowOneItems = new ArrayList<>();
    private ArrayList<OnScreenKeyboardItemController> rowTwoItems = new ArrayList<>();
    private ArrayList<OnScreenKeyboardItemController> rowThreeItems = new ArrayList<>();
    private ArrayList<OnScreenKeyboardItemController> rowFourItems = new ArrayList<>();

    private URL fxmlUrl = getClass().getClassLoader().getResource("scene/module/on_screen_keyboard_item.fxml");;
    private FXMLLoader loader;

    private boolean shiftMouseFocusFlag, backspaceMouseFocusFlag, changeTypeMouseFocusFlag, spaceMouseFocusFlag,
            refreshMouseFocusFlag, shiftClickedFlag, changeTypeClickedFlag;

    private TextField textField;
    private PasswordField passwordField;
    private int currentCaretPosition = -1;
    private IndexRange selection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Image
        imageSetting();

        // Add row item
        addRow();
    }

    public void init() {
        shiftClickedFlag = false;
        changeTypeClickedFlag = false;
        shiftImg.setImage(shiftEmpty);
        changeType.setText("!@#");

        refresh();
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
        rowOneItems.clear();

        // Key relocation for secure
        Random r = new Random();
        int space = r.nextInt(12);
        int space2;
        do{
            space2 = r.nextInt(12);
        } while(space == space2);

        try {
            int j = 1;
            for(int i=0; i<12; i++) {
                this.loader = new FXMLLoader(fxmlUrl);
                Node node = this.loader.load();
                OnScreenKeyboardItemController controller = (OnScreenKeyboardItemController)this.loader.getController();
                controller.setHandler(inputWordImpl);
                rowOneItems.add(controller);

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
        row2.getChildren().clear();
        rowTwoItems.clear();

        // Key relocation for secure
        Random r = new Random();
        int space = r.nextInt(12);
        int space2;
        do{
            space2 = r.nextInt(12);
        } while(space == space2);

        try {
            int j = 1;
            for(int i=0; i<12; i++) {
                this.loader = new FXMLLoader(fxmlUrl);
                Node node = this.loader.load();
                OnScreenKeyboardItemController controller = (OnScreenKeyboardItemController) this.loader.getController();
                controller.setHandler(inputWordImpl);
                rowTwoItems.add(this.loader.getController());

                if(i == space || i == space2) {
                    rowTwoItems.get(i).setItemLabel("");
                    rowTwoItems.get(i).setEmpty();
                } else {
                    switch(j) {
                        case 1 :
                            rowTwoItems.get(i).setItemLabel("q");
                            break;
                        case 2 :
                            rowTwoItems.get(i).setItemLabel("w");
                            break;
                        case 3 :
                            rowTwoItems.get(i).setItemLabel("e");
                            break;
                        case 4 :
                            rowTwoItems.get(i).setItemLabel("r");
                            break;
                        case 5 :
                            rowTwoItems.get(i).setItemLabel("t");
                            break;
                        case 6 :
                            rowTwoItems.get(i).setItemLabel("y");
                            break;
                        case 7 :
                            rowTwoItems.get(i).setItemLabel("u");
                            break;
                        case 8 :
                            rowTwoItems.get(i).setItemLabel("i");
                            break;
                        case 9 :
                            rowTwoItems.get(i).setItemLabel("o");
                            break;
                        case 10 :
                            rowTwoItems.get(i).setItemLabel("p");
                            break;
                        default : break;
                    }
                    j++;
                }

                row2.getChildren().add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRowThree() {
        row3.getChildren().clear();
        rowThreeItems.clear();

        // Key relocation for secure
        Random r = new Random();
        int space = r.nextInt(12);
        int space2, space3;
        do{
            space2 = r.nextInt(12);
            space3 = r.nextInt(12);
        } while(space == space2 || space == space3 || space2 == space3);

        try {
            int j = 1;
            for(int i=0; i<12; i++) {
                this.loader = new FXMLLoader(fxmlUrl);
                Node node = this.loader.load();
                OnScreenKeyboardItemController controller = (OnScreenKeyboardItemController) this.loader.getController();
                controller.setHandler(inputWordImpl);
                rowThreeItems.add(this.loader.getController());

                if(i == space || i == space2 || i == space3) {
                    rowThreeItems.get(i).setItemLabel("");
                    rowThreeItems.get(i).setEmpty();
                } else {
                    switch(j) {
                        case 1 :
                            rowThreeItems.get(i).setItemLabel("a");
                            break;
                        case 2 :
                            rowThreeItems.get(i).setItemLabel("s");
                            break;
                        case 3 :
                            rowThreeItems.get(i).setItemLabel("d");
                            break;
                        case 4 :
                            rowThreeItems.get(i).setItemLabel("f");
                            break;
                        case 5 :
                            rowThreeItems.get(i).setItemLabel("g");
                            break;
                        case 6 :
                            rowThreeItems.get(i).setItemLabel("h");
                            break;
                        case 7 :
                            rowThreeItems.get(i).setItemLabel("j");
                            break;
                        case 8 :
                            rowThreeItems.get(i).setItemLabel("k");
                            break;
                        case 9 :
                            rowThreeItems.get(i).setItemLabel("l");
                            break;
                        default : break;
                    }
                    j++;
                }

                row3.getChildren().add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRowFour() {
        row4.getChildren().clear();
        rowFourItems.clear();

        // Key relocation for secure
        Random r = new Random();
        int space = r.nextInt(9);
        int space2;
        do{
            space2 = r.nextInt(9);
        } while(space == space2);

        try {
            int j = 1;
            for(int i=0; i<9; i++) {
                this.loader = new FXMLLoader(fxmlUrl);
                Node node = this.loader.load();
                OnScreenKeyboardItemController controller = (OnScreenKeyboardItemController) this.loader.getController();
                controller.setHandler(inputWordImpl);
                rowFourItems.add(this.loader.getController());

                if(i == space || i == space2) {
                    rowFourItems.get(i).setItemLabel("");
                    rowFourItems.get(i).setEmpty();
                } else {
                    switch(j) {
                        case 1 :
                            rowFourItems.get(i).setItemLabel("z");
                            break;
                        case 2 :
                            rowFourItems.get(i).setItemLabel("x");
                            break;
                        case 3 :
                            rowFourItems.get(i).setItemLabel("c");
                            break;
                        case 4 :
                            rowFourItems.get(i).setItemLabel("v");
                            break;
                        case 5 :
                            rowFourItems.get(i).setItemLabel("b");
                            break;
                        case 6 :
                            rowFourItems.get(i).setItemLabel("n");
                            break;
                        case 7 :
                            rowFourItems.get(i).setItemLabel("m");
                            break;
                        default : break;
                    }
                    j++;
                }

                row4.getChildren().add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shift() {
        for(int i=0; i<rowTwoItems.size(); i++) {
            if(rowTwoItems.get(i).getChildrenSize() != 0) {
                rowTwoItems.get(i).setItemConvert();
            }
        }
        for(int i=0; i<rowThreeItems.size(); i++) {
            if(rowThreeItems.get(i).getChildrenSize() != 0) {
                rowThreeItems.get(i).setItemConvert();
            }
        }
        for(int i=0; i<rowFourItems.size(); i++) {
            if(rowFourItems.get(i).getChildrenSize() != 0) {
                rowFourItems.get(i).setItemConvert();
            }
        }
    }

    private void backspace() {
        TextField tempTextField = textField;
        int position;
        int tempSelectionLength = 0;

        // Delete selected text before positioning caret
        if(textField.isVisible()) {
            tempTextField = textField;
        } else {
            tempTextField = passwordField;
        }

        if (tempTextField.getSelectedText().length() != 0) {
            position = tempTextField.getSelection().getStart();
            tempTextField.setText(tempTextField.getText().substring(0, tempTextField.getSelection().getStart() > 0 ? tempTextField.getSelection().getStart() : 0) +
                    tempTextField.getText().substring(tempTextField.getSelection().getEnd()));
            tempSelectionLength = 1;

        } else {
            position = tempTextField.getCaretPosition();
            if(caretFlag) {
                position = currentCaretPosition;
                if (currentCaretPosition == -1) {
                    position = tempTextField.getText().length();
                }
                if (selection.getLength() != 0) {
                    position = selection.getStart();
                    tempTextField.setText(tempTextField.getText().substring(0, selection.getStart() > 0 ? selection.getStart() : 0) +
                            tempTextField.getText().substring(selection.getEnd()));
                    tempSelectionLength = 1;
                }
            }
        }

        // Caret positioning
        if(position > 0) {
            if(tempSelectionLength == 0) {
                tempTextField.setText(tempTextField.getText().substring(0, position - 1) + tempTextField.getText().substring(position));
                if(caretFlag) {
                    currentCaretPosition = position - 1;
                    caretRepositioning(1);
                }
                tempTextField.positionCaret(position - 1);

            } else {
                if(caretFlag) {
                    currentCaretPosition = position;
                    caretRepositioning(1);
                }
                tempTextField.positionCaret(position);
            }
        } else {
            currentCaretPosition = position;
            caretRepositioning(1);
        }
    }

    private void inputWord(String word) {
        TextField tempTextField = textField;
        int position;

        // Delete selected text if exists
        if(textField.isVisible()) {
            tempTextField = textField;
        } else {
            tempTextField = passwordField;
        }

        if (tempTextField.getSelectedText().length() != 0) {
            position = tempTextField.getSelection().getStart();
            tempTextField.setText(tempTextField.getText().substring(0, tempTextField.getSelection().getStart() > 0 ? tempTextField.getSelection().getStart() : 0) +
                    tempTextField.getText().substring(tempTextField.getSelection().getEnd()));

        } else {
            position = tempTextField.getCaretPosition();
            if(caretFlag) {
                position = currentCaretPosition;
                if(currentCaretPosition == -1) {
                    position = tempTextField.getText().length();
                }
                if(selection.getLength() != 0) {
                    position = selection.getStart();
                    tempTextField.setText(textField.getText().substring(0, selection.getStart() > 0 ? selection.getStart() : 0) +
                            tempTextField.getText().substring(selection.getEnd()));
                }
            }
        }

        // Caret positioning
        tempTextField.setText(tempTextField.getText().substring(0, position) + word + tempTextField.getText().substring(position));
        if(caretFlag) {
            currentCaretPosition = position + 1;
            tempTextField.positionCaret(currentCaretPosition);
            caretRepositioning(1);
        } else {
            tempTextField.positionCaret(position + 1);
        }
    }

    private void refresh() {
        addRow();
        if(shiftClickedFlag) {
            shift();
        }
        if(changeTypeClickedFlag) {
            changeType();
        }
    }

    private void changeType() {
        int j;
        // Change type special characters to alphabet
        if(!changeTypeClickedFlag) {
            j = 1;
            for (int i = 0; i < rowOneItems.size(); i++) {
                if (rowOneItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowOneItems.get(i).setItemLabel("1");
                            break;
                        case 2:
                            rowOneItems.get(i).setItemLabel("2");
                            break;
                        case 3:
                            rowOneItems.get(i).setItemLabel("3");
                            break;
                        case 4:
                            rowOneItems.get(i).setItemLabel("4");
                            break;
                        case 5:
                            rowOneItems.get(i).setItemLabel("5");
                            break;
                        case 6:
                            rowOneItems.get(i).setItemLabel("6");
                            break;
                        case 7:
                            rowOneItems.get(i).setItemLabel("7");
                            break;
                        case 8:
                            rowOneItems.get(i).setItemLabel("8");
                            break;
                        case 9:
                            rowOneItems.get(i).setItemLabel("9");
                            break;
                        case 10:
                            rowOneItems.get(i).setItemLabel("0");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            j = 1;
            for (int i = 0; i < rowTwoItems.size(); i++) {
                if (rowTwoItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowTwoItems.get(i).setItemLabel("q");
                            break;
                        case 2:
                            rowTwoItems.get(i).setItemLabel("w");
                            break;
                        case 3:
                            rowTwoItems.get(i).setItemLabel("e");
                            break;
                        case 4:
                            rowTwoItems.get(i).setItemLabel("r");
                            break;
                        case 5:
                            rowTwoItems.get(i).setItemLabel("t");
                            break;
                        case 6:
                            rowTwoItems.get(i).setItemLabel("y");
                            break;
                        case 7:
                            rowTwoItems.get(i).setItemLabel("u");
                            break;
                        case 8:
                            rowTwoItems.get(i).setItemLabel("i");
                            break;
                        case 9:
                            rowTwoItems.get(i).setItemLabel("o");
                            break;
                        case 10:
                            rowTwoItems.get(i).setItemLabel("p");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            j = 1;
            for (int i = 0; i < rowThreeItems.size(); i++) {
                if (rowThreeItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowThreeItems.get(i).setItemLabel("a");
                            break;
                        case 2:
                            rowThreeItems.get(i).setItemLabel("s");
                            break;
                        case 3:
                            rowThreeItems.get(i).setItemLabel("d");
                            break;
                        case 4:
                            rowThreeItems.get(i).setItemLabel("f");
                            break;
                        case 5:
                            rowThreeItems.get(i).setItemLabel("g");
                            break;
                        case 6:
                            rowThreeItems.get(i).setItemLabel("h");
                            break;
                        case 7:
                            rowThreeItems.get(i).setItemLabel("j");
                            break;
                        case 8:
                            rowThreeItems.get(i).setItemLabel("k");
                            break;
                        case 9:
                            rowThreeItems.get(i).setItemLabel("l");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            j = 1;
            for (int i = 0; i < rowFourItems.size(); i++) {
                if (rowFourItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowFourItems.get(i).setItemLabel("z");
                            break;
                        case 2:
                            rowFourItems.get(i).setItemLabel("x");
                            break;
                        case 3:
                            rowFourItems.get(i).setItemLabel("c");
                            break;
                        case 4:
                            rowFourItems.get(i).setItemLabel("v");
                            break;
                        case 5:
                            rowFourItems.get(i).setItemLabel("b");
                            break;
                        case 6:
                            rowFourItems.get(i).setItemLabel("n");
                            break;
                        case 7:
                            rowFourItems.get(i).setItemLabel("m");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            if(shiftClickedFlag) {
                shift();
            }
        // Change type alphabet to special characters;
        } else {
            j = 1;
            for (int i = 0; i < rowOneItems.size(); i++) {
                if (rowOneItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowOneItems.get(i).setItemLabel("!");
                            break;
                        case 2:
                            rowOneItems.get(i).setItemLabel("@");
                            break;
                        case 3:
                            rowOneItems.get(i).setItemLabel("#");
                            break;
                        case 4:
                            rowOneItems.get(i).setItemLabel("$");
                            break;
                        case 5:
                            rowOneItems.get(i).setItemLabel("%");
                            break;
                        case 6:
                            rowOneItems.get(i).setItemLabel("^");
                            break;
                        case 7:
                            rowOneItems.get(i).setItemLabel("&");
                            break;
                        case 8:
                            rowOneItems.get(i).setItemLabel("*");
                            break;
                        case 9:
                            rowOneItems.get(i).setItemLabel("(");
                            break;
                        case 10:
                            rowOneItems.get(i).setItemLabel(")");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            j = 1;
            for (int i = 0; i < rowTwoItems.size(); i++) {
                if (rowTwoItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowTwoItems.get(i).setItemLabel("-");
                            break;
                        case 2:
                            rowTwoItems.get(i).setItemLabel("=");
                            break;
                        case 3:
                            rowTwoItems.get(i).setItemLabel("+");
                            break;
                        case 4:
                            rowTwoItems.get(i).setItemLabel("{");
                            break;
                        case 5:
                            rowTwoItems.get(i).setItemLabel("}");
                            break;
                        case 6:
                            rowTwoItems.get(i).setItemLabel("[");
                            break;
                        case 7:
                            rowTwoItems.get(i).setItemLabel("]");
                            break;
                        case 8:
                            rowTwoItems.get(i).setItemLabel("\\");
                            break;
                        case 9:
                            rowTwoItems.get(i).setItemLabel(":");
                            break;
                        case 10:
                            rowTwoItems.get(i).setItemLabel(";");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            j = 1;
            for (int i = 0; i < rowThreeItems.size(); i++) {
                if (rowThreeItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowThreeItems.get(i).setItemLabel("\"");
                            break;
                        case 2:
                            rowThreeItems.get(i).setItemLabel("'");
                            break;
                        case 3:
                            rowThreeItems.get(i).setItemLabel("<");
                            break;
                        case 4:
                            rowThreeItems.get(i).setItemLabel(">");
                            break;
                        case 5:
                            rowThreeItems.get(i).setItemLabel(",");
                            break;
                        case 6:
                            rowThreeItems.get(i).setItemLabel(".");
                            break;
                        case 7:
                            rowThreeItems.get(i).setItemLabel("/");
                            break;
                        case 8:
                            rowThreeItems.get(i).setItemLabel("?");
                            break;
                        case 9:
                            rowThreeItems.get(i).setItemLabel("|");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
            j = 1;
            for (int i = 0; i < rowFourItems.size(); i++) {
                if (rowFourItems.get(i).getChildrenSize() != 0) {
                    switch (j) {
                        case 1:
                            rowFourItems.get(i).setItemLabel("~");
                            break;
                        case 2:
                            rowFourItems.get(i).setItemLabel("`");
                            break;
                        case 3:
                            rowFourItems.get(i).setItemLabel("_");
                            break;
                        case 4:
                            rowFourItems.get(i).setItemLabel("$");
                            break;
                        case 5:
                            rowFourItems.get(i).setItemLabel("#");
                            break;
                        case 6:
                            rowFourItems.get(i).setItemLabel("@");
                            break;
                        case 7:
                            rowFourItems.get(i).setItemLabel("!");
                            break;
                        default:
                            break;
                    }
                    j++;
                }
            }
        }
    }

    public void caretRepositioning(int type) {
        int tempCaret = currentCaretPosition;
        if(textField.isVisible()) {
            textField.requestFocus();

            if(tempCaret == -1) {
                textField.positionCaret(textField.getText().length());
            } else {
                textField.positionCaret(tempCaret);
            }

            if(selection != null && selection.getLength() != 0 && type == 0) {
                textField.selectRange(tempCaret == selection.getStart() ? selection.getEnd() : selection.getStart(), tempCaret);
            }

        } else {
            passwordField.requestFocus();

            if(tempCaret == -1) {
                passwordField.positionCaret(passwordField.getText().length());
            } else {
                passwordField.positionCaret(tempCaret);
            }

            if(selection != null && selection.getLength() != 0 && type == 0) {
                passwordField.selectRange(tempCaret == selection.getStart() ? selection.getEnd() : selection.getStart(), tempCaret);
            }

        }
    }

    @FXML
    public void onMouseEntered(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            StyleManager.backgroundColorStyle(shift, StyleManager.AColor.C2b2b2b);
            shiftImg.setImage(shiftFillWhite);
            shiftMouseFocusFlag = true;

        } else if(fxid.equals("backspace")) {
            StyleManager.backgroundColorStyle(backspace, StyleManager.AColor.C2b2b2b);
            backspaceImg.setImage(backspaceWhite);
            backspaceMouseFocusFlag = true;

        } else if(fxid.equals("changeType")) {
            StyleManager.backgroundColorStyle(changeType, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(changeType, StyleManager.AColor.Cffffff);
            changeTypeMouseFocusFlag = true;

        } else if(fxid.equals("space")) {
            StyleManager.backgroundColorStyle(space, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(space, StyleManager.AColor.Cffffff);
            spaceMouseFocusFlag = true;

        } else if(fxid.equals("refresh")) {
            StyleManager.backgroundColorStyle(refresh, StyleManager.AColor.C2b2b2b);
            refreshImg.setImage(refreshWhite);
            refreshMouseFocusFlag = true;
        }
    }

    @FXML
    public void onMouseExited(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            if(shiftClickedFlag) {
                StyleManager.backgroundColorStyle(shift, StyleManager.AColor.Cf2f2f2);
                shiftImg.setImage(shiftFillBlack);
            } else {
                StyleManager.backgroundColorStyle(shift, StyleManager.AColor.Cf2f2f2);
                shiftImg.setImage(shiftEmpty);
            }
            shiftMouseFocusFlag = false;

        } else if(fxid.equals("backspace")) {
            StyleManager.backgroundColorStyle(backspace, StyleManager.AColor.Cf2f2f2);
            backspaceImg.setImage(backspaceBlack);
            backspaceMouseFocusFlag = false;

        } else if(fxid.equals("changeType")) {
            StyleManager.backgroundColorStyle(changeType, StyleManager.AColor.Cf2f2f2);
            StyleManager.fontColorStyle(changeType, StyleManager.AColor.C202020);
            changeTypeMouseFocusFlag = false;

        } else if(fxid.equals("space")) {
            StyleManager.backgroundColorStyle(space, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(space, StyleManager.AColor.C202020);
            spaceMouseFocusFlag = false;

        } else if(fxid.equals("refresh")) {
            StyleManager.backgroundColorStyle(refresh, StyleManager.AColor.Cf2f2f2);
            refreshImg.setImage(refreshBlack);
            refreshMouseFocusFlag = false;
        }
    }

    @FXML
    public void onMousePressed(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            StyleManager.backgroundColorStyle(shift, StyleManager.AColor.C910000);

        } else if(fxid.equals("backspace")) {
            StyleManager.backgroundColorStyle(backspace, StyleManager.AColor.C910000);

        } else if(fxid.equals("changeType")) {
            StyleManager.backgroundColorStyle(changeType, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(changeType, StyleManager.AColor.Cffffff);

        } else if(fxid.equals("space")) {
            StyleManager.backgroundColorStyle(space, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(space, StyleManager.AColor.Cffffff);

        } else if(fxid.equals("refresh")) {
            StyleManager.backgroundColorStyle(refresh, StyleManager.AColor.C910000);
        }
    }

    @FXML
    public void onMouseReleased(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("shift")) {
            if(shiftMouseFocusFlag) {
                StyleManager.backgroundColorStyle(shift, StyleManager.AColor.C2b2b2b);
                shiftImg.setImage(shiftFillWhite);
                // Convert items
                if(!changeTypeClickedFlag) {
                    shiftClickedFlag = !shiftClickedFlag;
                    shift();
                }
            } else {
                if(shiftClickedFlag) {
                    StyleManager.backgroundColorStyle(shift, StyleManager.AColor.Cf2f2f2);
                    shiftImg.setImage(shiftFillBlack);
                } else {
                    StyleManager.backgroundColorStyle(shift, StyleManager.AColor.Cf2f2f2);
                    shiftImg.setImage(shiftEmpty);
                }
            }

        } else if(fxid.equals("backspace")) {
            if(backspaceMouseFocusFlag) {
                StyleManager.backgroundColorStyle(backspace, StyleManager.AColor.C2b2b2b);
                backspaceImg.setImage(backspaceWhite);
                // Remove last word from textField
                backspace();
            } else {
                StyleManager.backgroundColorStyle(backspace, StyleManager.AColor.Cf2f2f2);
                backspaceImg.setImage(backspaceBlack);
            }

        } else if(fxid.equals("changeType")) {
            if(changeTypeMouseFocusFlag) {
                StyleManager.backgroundColorStyle(changeType, StyleManager.AColor.C2b2b2b);
                StyleManager.fontColorStyle(changeType, StyleManager.AColor.Cffffff);
                // Change type alphabet to special characters, or reverse
                changeTypeClickedFlag = !changeTypeClickedFlag;
                if(changeTypeClickedFlag) {
                    changeType.setText("abc");
                } else {
                    changeType.setText("!@#");
                }
                changeType();
            } else {
                StyleManager.backgroundColorStyle(changeType, StyleManager.AColor.Cf2f2f2);
                StyleManager.fontColorStyle(changeType, StyleManager.AColor.C202020);
            }

        } else if(fxid.equals("space")) {
            if(spaceMouseFocusFlag) {
                StyleManager.backgroundColorStyle(space, StyleManager.AColor.C2b2b2b);
                StyleManager.fontColorStyle(space, StyleManager.AColor.Cffffff);
                // Input space to textField
                inputWord(" ");
            } else {
                StyleManager.backgroundColorStyle(space, StyleManager.AColor.Cffffff);
                StyleManager.fontColorStyle(space, StyleManager.AColor.C202020);
            }

        } else if(fxid.equals("refresh")) {
            if(refreshMouseFocusFlag) {
                StyleManager.backgroundColorStyle(refresh, StyleManager.AColor.C2b2b2b);
                refreshImg.setImage(refreshWhite);
                // Refresh all item's location
                refresh();

            } else {
                StyleManager.backgroundColorStyle(refresh, StyleManager.AColor.Cf2f2f2);
                refreshImg.setImage(refreshBlack);
            }
        }
    }

    private OnScreenKeyboardItemController.OnScreenKeyboardItemImpl inputWordImpl = new OnScreenKeyboardItemController.OnScreenKeyboardItemImpl() {
        @Override
        public void clicked(String word) {
            inputWord(word);
        }
    };

    public void setTextField(TextField textField){
        this.textField = textField;

        textField.caretPositionProperty().addListener(textFieldListener);
    }

    private ChangeListener<Number> textFieldListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if(newValue != null && newValue.intValue() >= 0) {
                //System.out.println("position "+oldValue.intValue() + " -- > "+newValue.intValue() + "focus("+textField.isFocused()+")");
                currentCaretPosition = 0;

                if(!textField.isFocused() && newValue.intValue() == 0) {
                    currentCaretPosition = oldValue.intValue();
                }

                if(!textField.isFocused() && oldValue.intValue() == 0) {
                    currentCaretPosition = newValue.intValue();
                }
            }
        }
    };

    public void setPasswordField(PasswordField passwordField) {
        this.passwordField = passwordField;

        passwordField.caretPositionProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue != null && newValue.intValue() >= 0) {
                    //System.out.println("position "+oldValue.intValue() + " -- > "+newValue.intValue() + "focus("+passwordField.isFocused()+")");
                    currentCaretPosition = 0;

                    if(!passwordField.isFocused() && newValue.intValue() == 0) {
                        currentCaretPosition = oldValue.intValue();
                    }

                    if(!passwordField.isFocused() && oldValue.intValue() == 0) {
                        currentCaretPosition = newValue.intValue();
                    }
                }
            }
        });
    }

    public void setCurrentCaretPosition(int currentCaretPosition) {
        this.currentCaretPosition = currentCaretPosition;
    }

    public void setCaretFlag(boolean caretFlag) {
        this.caretFlag = caretFlag;
    }

    public void setSelection(IndexRange selection) {
        this.selection = selection;
    }

    public int getCurrentCaretPosition() {
        return this.currentCaretPosition;
    }

    public boolean getCaretFlag() {
        return this.caretFlag;
    }

    public IndexRange getSelection() {
        return this.selection;
    }
}
