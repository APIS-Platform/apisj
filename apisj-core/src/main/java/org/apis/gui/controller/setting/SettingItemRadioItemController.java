package org.apis.gui.controller.setting;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingItemRadioItemController implements Initializable {
    @FXML private Label name, networkIdLabel, networkId;
    @FXML private ImageView checkImg;
    @FXML private TextField textField;
    @FXML private GridPane itemGrid, contentsGrid, networkIdGrid, bgGrid;

    public final static String SETTING_ITEM_RADIO_LABEL = "label";
    public final static String SETTING_ITEM_RADIO_TEXTFIELD = "textfield";
    private boolean checked = false;
    private SettingItemRadioItemImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    textField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                            " -fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
                } else {
                    textField.setStyle("-fx-background-color: #f8f8fb; -fx-border-color: #d8d8d8; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;" +
                            " -fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size:12px; -fx-text-fill: #2b2b2b;");
                    if(textField.getText().length() != 0 && Integer.parseInt(textField.getText()) == 0) {
                        textField.setText("1");
                    }
                }
            }
        });

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() != 0) {
                StringProperty string = (StringProperty) observable;
                String newValueString = newValue;

                if (!newValue.matches("[0-9*]")) {
                    newValueString = newValue.replaceAll("[^0-9]", "");
                }

                if(newValueString.length() != 0) {
                    int newValueInt = Integer.parseInt(newValueString);
                    if (newValueInt > 65535) {
                        newValueInt = 65535;
                    }
                    newValueString = Integer.toString(newValueInt);
                }

                string.set(newValueString);
            }
        });

        itemGrid.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(handler != null) {
                    handler.clicked();
                }
            }
        });

        textField.setOnMouseClicked(event -> {
            if(handler != null) {
                handler.clicked();
            }
        });
    }

    public void languageSetting() {
        networkIdLabel.textProperty().bind(StringManager.getInstance().setting.networkIdLabel1);
    }

    public void setType(String type) {
        if(type.equals(SETTING_ITEM_RADIO_LABEL)) {
            textField.setVisible(false);
            textField.setPrefWidth(0);
            textField.setPrefHeight(0);
            GridPane.setMargin(textField, new Insets(0, 0, 0, 0));

        } else if(type.equals(SETTING_ITEM_RADIO_TEXTFIELD)) {
            textField.setVisible(true);
            textField.setPrefWidth(184);
            textField.setPrefHeight(32);
            contentsGrid.getChildren().remove(networkIdGrid);
            GridPane.setMargin(textField, new Insets(0, 0, 0, 8));
        }
    }

    public void check() {
        checked = true;
        checkImg.setImage(ImageManager.checkRed);
    }

    public void uncheck() {
        checked = false;
        checkImg.setImage(ImageManager.checkGrey);
    }

    public boolean isChecked() {
        return this.checked;
    }

    public String getName() {
        return this.name.getText();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getNetworkId() {
        return this.networkId.getText();
    }

    public void setNetworkId(String networkId) {
        this.networkId.setText(networkId);
    }

    public String getText() {
        if(this.textField.getText().equals("")) {
            return "1";
        }
        return this.textField.getText();
    }

    public void setText(String text) {
        this.textField.setText(text);
    }

    public void requestFocus() {
        this.textField.requestFocus();
    }

    public void setBgGridMargin(double top, double right, double bottom, double left) {
        AnchorPane.setTopAnchor(bgGrid, top);
        AnchorPane.setRightAnchor(bgGrid, right);
        AnchorPane.setBottomAnchor(bgGrid, bottom);
        AnchorPane.setLeftAnchor(bgGrid, left);
    }

    public void setHandler(SettingItemRadioItemImpl handler) {
        this.handler = handler;
    }

    public SettingItemRadioItemImpl getHandler() {
        return this.handler;
    }

    public interface SettingItemRadioItemImpl {
        void clicked();
    }
}
