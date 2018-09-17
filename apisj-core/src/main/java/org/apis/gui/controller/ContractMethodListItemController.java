package org.apis.gui.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ContractMethodListItemController implements Initializable {
    public static final int ITEM_TYPE_PARAM = 0;
    public static final int ITEM_TYPE_RETURN = 1;
    private int itemType = ITEM_TYPE_PARAM;

    public static final int DATA_TYPE_STRING = 0;
    public static final int DATA_TYPE_INT = 1;
    public static final int DATA_TYPE_ARRAY_INT = 3;
    public static final int DATA_TYPE_ADDRESS = 4;
    private int dataType = DATA_TYPE_STRING;

    @FXML private GridPane rootPane;
    @FXML private ImageView icon;
    @FXML private Label paramName, paramType;
    @FXML private TextField textField;

    private SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();
    private SimpleStringProperty stringProperty = new SimpleStringProperty();

    private ContractMethodListItemImpl handler;

    public void setHandler(ContractMethodListItemImpl handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        stringProperty.bind(textField.textProperty());

        // default
        setData(itemType, "", dataType, "");

        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(handler != null){
                    handler.change(oldValue, newValue);
                }
            }
        });
    }
    public void setItemText(String text){
        this.textField.textProperty().set(text);
    }
    public void setData(int itemType, String paramName, int dataType, String dataTypeName){
        setItemType(itemType);
        setParamName(paramName);
        setDataType(dataType, dataTypeName);
    }
    private void setParamName(String paramName){
        this.paramName.setText(paramName);
    }
    private void setItemType(int itemType){
        this.itemType = itemType;

        switch (this.itemType){
            case ITEM_TYPE_PARAM :
                icon.setVisible(false);
                icon.fitWidthProperty().set(1);
                rootPane.paddingProperty().set(Insets.EMPTY);
                textField.setEditable(true);

                return;
            case ITEM_TYPE_RETURN :
                icon.setVisible(true);
                icon.fitWidthProperty().set(10);
                rootPane.paddingProperty().set(new Insets(0,0,0,16));
                textField.setEditable(false);
                return;
        }
    }
    private void setDataType(int dataType, String dataTypeName){
        this.dataType = dataType;

        switch (this.dataType){
            case DATA_TYPE_STRING :

                break;
            case DATA_TYPE_INT :

                break;
            case DATA_TYPE_ARRAY_INT :

                break;
            case DATA_TYPE_ADDRESS :

                break;
        }

        paramType.setText(dataTypeName);
    }

    public String getText(){ return this.textField.getText(); }

    public SimpleBooleanProperty selectedProperty() {
        return booleanProperty;
    }

    public SimpleStringProperty textProperty() {
        return stringProperty;
    }

    public interface ContractMethodListItemImpl{
        void change(Object oldValue, Object newValue);
    }
}
