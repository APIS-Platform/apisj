package org.apis.gui.controller.smartcontract;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class ContractMethodListItemController extends BaseViewController {
    public static final int ITEM_TYPE_PARAM = 0;
    public static final int ITEM_TYPE_RETURN = 1;
    private int itemType = ITEM_TYPE_PARAM;

    public static final int DATA_TYPE_STRING = 0;
    public static final int DATA_TYPE_INT = 1;
    public static final int DATA_TYPE_ARRAY_INT = 3;
    public static final int DATA_TYPE_ADDRESS = 4;
    public static final int DATA_TYPE_BOOL = 5;
    private int dataType = DATA_TYPE_STRING;

    @FXML private GridPane rootPane;
    @FXML private ImageView icon;
    @FXML private Label paramName, paramType;
    @FXML private TextField textField;
    @FXML private CheckBox checkBox;

    private SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();
    private SimpleStringProperty stringProperty = new SimpleStringProperty();

    private ContractMethodListItemImpl handler;

    public void setHandler(ContractMethodListItemImpl handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        stringProperty.bind(textField.textProperty());
        booleanProperty.bind(checkBox.selectedProperty());

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
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //checkBox.setSelected(booleanProperty.getValue());
            }
        });
        checkBox.setDisable(true);
        checkBox.setOpacity(1);
    }
    public void setItemText(String text){
        this.textField.textProperty().set(text);
    }
    public void setSelected(boolean isSelected){
        this.checkBox.setSelected(isSelected);
    }
    public void setData(int itemType, String paramName, int dataType, String dataTypeName){
        setItemType(itemType);
        setParamName(paramName);
        setDataType(dataType, dataTypeName);
    }
    private void setParamName(String paramName){
        this.paramName.setText(paramName);
        this.checkBox.textProperty().set(paramName);
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
                textField.setVisible(true);
                checkBox.setVisible(false);
                break;
            case DATA_TYPE_INT :
                textField.setVisible(true);
                checkBox.setVisible(false);
                break;
            case DATA_TYPE_ARRAY_INT :
                textField.setVisible(true);
                checkBox.setVisible(false);
                break;
            case DATA_TYPE_ADDRESS :
                textField.setVisible(true);
                checkBox.setVisible(false);
                break;
            case DATA_TYPE_BOOL :
                textField.setVisible(false);
                checkBox.setVisible(true);
                break;
        }

        paramType.setText(dataTypeName);
    }

    public int getDataType(){
        return this.dataType;
    }

    public String getText(){ return this.textField.getText().trim(); }
    public boolean isSelected() { return this.checkBox.isSelected(); }

    public SimpleBooleanProperty selectedProperty() {
        return booleanProperty;
    }

    public SimpleStringProperty textProperty() {
        return stringProperty;
    }
    public ReadOnlyBooleanProperty focusedProperty(){
        return this.textField.focusedProperty();
    }

    public interface ContractMethodListItemImpl{
        void change(Object oldValue, Object newValue);
    }
}
