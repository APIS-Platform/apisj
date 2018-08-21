package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.DBManager;
import org.apis.gui.model.MyAddressModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupMyAddressController implements Initializable {
    @FXML
    private VBox list;
    @FXML
    private ScrollPane listPane;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMyAddressList();
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("id :"+id);
        if(id.equals("btnAddMyAddress")){
            AppManager.getInstance().guiFx.showMainPopup("popup_my_address_register.fxml", 1);
        }
    }

    public void initMyAddressList(){
        MyAddressModel model = null;
        for(int i=0; i<DBManager.getInstance().myAddressList.size(); i++){
            model = DBManager.getInstance().myAddressList.get(i);
            try {
                URL labelUrl  = new File("apisj-core/src/main/resources/scene/popup_my_address_item.fxml").toURI().toURL();

                //item
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane pane = loader.load();
                list.getChildren().add(pane);

                System.out.println("model.getAddress() : "+model.getAddress());
                System.out.println("model.getAlias() : "+model.getAlias());

                PopupMyAddressItemController itemController = (PopupMyAddressItemController)loader.getController();
                itemController.setAddress(model.getAddress());
                itemController.setAlias(model.getAlias());

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
}
