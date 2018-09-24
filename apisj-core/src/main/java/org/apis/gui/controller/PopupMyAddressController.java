package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.MyAddressRecord;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.MyAddressModel;
import org.apis.util.ByteUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupMyAddressController extends BasePopupController {
    @FXML private VBox list;
    @FXML private ScrollPane listPane;
    @FXML private TextField searchTextField;
    @FXML private Label topAddBtnLabel, titleLabel, subTitleLabel, myAddressListLabel, editLabel, deleteLabel, selectLabel, noBtn, yesBtn;

    private String selectAddress;
    private ArrayList<PopupMyAddressItemController> itemControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        searchTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(oldValue){
                    searchMyAddressList(searchTextField.getText());
                }
            }
        });
    }

    private void languageSetting(){
        titleLabel.textProperty().bind(StringManager.getInstance().myAddress.title);
        subTitleLabel.textProperty().bind(StringManager.getInstance().myAddress.subTitle);
        topAddBtnLabel.textProperty().bind(StringManager.getInstance().myAddress.topAddBtn);
        myAddressListLabel.textProperty().bind(StringManager.getInstance().myAddress.myAddressList);
        editLabel.textProperty().bind(StringManager.getInstance().myAddress.edit);
        deleteLabel.textProperty().bind(StringManager.getInstance().myAddress.delete);
        selectLabel.textProperty().bind(StringManager.getInstance().myAddress.select);
        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.yesButton);
        searchTextField.promptTextProperty().bind(StringManager.getInstance().myAddress.searchPlaceHolder);
    }


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("id :"+id);
        if(id.equals("btnAddMyAddress")){
            PopupMyAddressRegisterController controller = (PopupMyAddressRegisterController)PopupManager.getInstance().showMainPopup("popup_my_address_register.fxml", 1);
            controller.setMyAddressHandler(this.handler);
            controller.setModel(new MyAddressModel("","",null));
        }else if(id.equals("yesBtn")){
            if(handler != null){
                handler.onClickYes(selectAddress);
            }
            exit();
        }else if(id.equals("noBtn")){
            exit();
        }
    }

    @FXML
    public void onAction(ActionEvent ae){
        searchMyAddressList(searchTextField.getText());
    }


    public void searchMyAddressList(String search){
        list.getChildren().clear();
        itemControllers.clear();

        MyAddressModel model = null;
        List<MyAddressRecord> myAddressList = DBManager.getInstance().selectMyAddressSearch(search);
        for(int i=0; i<myAddressList.size(); i++){
            model = new MyAddressModel(ByteUtil.toHexString(myAddressList.get(i).getAddress()), myAddressList.get(i).getAlias(), null);
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup_my_address_item.fxml");

                //item
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane pane = loader.load();
                list.getChildren().add(pane);

                PopupMyAddressItemController itemController = (PopupMyAddressItemController)loader.getController();
                itemController.setAddress(model.getAddress());
                itemController.setAlias(model.getAlias());
                itemController.setModel(model);
                itemController.setMyAddressHandler(handler);
                itemController.setHandler(new PopupMyAddressItemController.PopupMyAddressItemImpl() {
                    @Override
                    public void onMouseClickedGroupTag(String text) {
                        searchTextField.setText(text);
                        searchMyAddressList(text);
                    }

                    @Override
                    public void onMouseClickedSelected(String text) {
                        if(text.equals(selectAddress)){
                            selectAddress = null;
                        }else{
                            selectAddress = text;
                        }

                        for(int i=0; i<itemControllers.size(); i++){
                            itemControllers.get(i).setSelected(false);
                            if(itemControllers.get(i).getAddress().equals(selectAddress)){
                                itemControllers.get(i).setSelected(true);
                            }
                        }
                    }
                });

                itemControllers.add(itemController);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(list.getChildren().size() == 0){
            listPane.setVisible(false);
        }else{
            listPane.setVisible(true);
        }
    }


    private PopupMyAddressImpl handler;
    public void setHandler(PopupMyAddressImpl handler){
        this.handler = handler;
        searchMyAddressList("");
    }
    public interface PopupMyAddressImpl{
        void onClickYes(String address);
    }
}
