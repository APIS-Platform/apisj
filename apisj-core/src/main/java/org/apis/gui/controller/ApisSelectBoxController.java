package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.SelectBoxWalletItemModel;
import org.apis.keystore.KeyStoreDataExp;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ApisSelectBoxController implements Initializable {
    public static final int SELECT_BOX_TYPE_ALIAS = 0;
    public static final int SELECT_BOX_TYPE_ADDRESS = 1;
    private int selectBoxType = SELECT_BOX_TYPE_ALIAS;

    private ApisSelectBoxHeadAliasController aliasHeaderController;
    private ApisSelectBoxItemAliasController aliasItemController;
    private ApisSelectBoxHeadAddressController addressHeaderController;
    private ApisSelectBoxItemAddressController addressItemController;

    private ArrayList<SelectBoxWalletItemModel> walletItemModels = new ArrayList<SelectBoxWalletItemModel>();

    @FXML
    private AnchorPane rootPane;
    @FXML
    private VBox childPane, itemList;
    @FXML
    private GridPane header;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void onMouseClicked(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("header")){
            toggleItemListVisible();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        onStateDefault();
        setItemListVisible(false);


        setSelectBoxType(SELECT_BOX_TYPE_ALIAS);
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++){
            KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

            String address = dataExp.address;
            String alias = dataExp.alias;
            String mask = "test@me";
            SelectBoxWalletItemModel model = new SelectBoxWalletItemModel();
            model.addressProperty().setValue(address);
            model.aliasProperty().setValue(alias);
            model.maskProperty().setValue(mask);

            addItem(SELECT_BOX_TYPE_ALIAS, model);
        }
        setHeader(SELECT_BOX_TYPE_ALIAS, walletItemModels.get(0));
    }

    public void onStateDefault(){
        String style = "";
        style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
        style = style + "-fx-background-color: #f2f2f2; ";
        style = style + "-fx-border-color: #d8d8d8; ";
        header.setStyle(style);
    }

    public void onStateActive(){
        String style = "";
        style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
        style = style + "-fx-background-color: #ffffff; ";
        style = style + "-fx-border-color: #999999; ";
        header.setStyle(style);
    }

    public void setSelectBoxType(int boxType){
        this.selectBoxType = boxType;
        if(this.selectBoxType == SELECT_BOX_TYPE_ALIAS){
            this.scrollPane.maxHeightProperty().setValue(170);
        }else if(this.selectBoxType == SELECT_BOX_TYPE_ADDRESS){
            this.scrollPane.maxHeightProperty().setValue(162);
        }
    }
    public void setHeader(int boxType, SelectBoxWalletItemModel model){
        try {
            URL aliasHeaderUrl  = new File("apisj-core/src/main/resources/scene/apis_selectbox_head_alias.fxml").toURI().toURL();
            URL addressHeaderUrl  = new File("apisj-core/src/main/resources/scene/apis_selectbox_head_address.fxml").toURI().toURL();
            Node headerNode = null;

            header.getChildren().clear();
            if(boxType == SELECT_BOX_TYPE_ALIAS){
                FXMLLoader loader = new FXMLLoader(aliasHeaderUrl);
                headerNode = loader.load();
                aliasHeaderController = (ApisSelectBoxHeadAliasController)loader.getController();
                aliasHeaderController.setModel(model);

            }else if(boxType == SELECT_BOX_TYPE_ADDRESS){
                FXMLLoader loader = new FXMLLoader(addressHeaderUrl);
                headerNode = loader.load();
                addressHeaderController = (ApisSelectBoxHeadAddressController)loader.getController();
            }
            header.add(headerNode,0,0);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void addItem(int boxType, SelectBoxWalletItemModel model){
        try {
            URL aliasItemUrl  = new File("apisj-core/src/main/resources/scene/apis_selectbox_item_alias.fxml").toURI().toURL();
            URL addressItemUrl  = new File("apisj-core/src/main/resources/scene/apis_selectbox_item_address.fxml").toURI().toURL();
            Node itemNode = null;

            if(boxType == SELECT_BOX_TYPE_ALIAS){
                FXMLLoader loader = new FXMLLoader(aliasItemUrl);
                itemNode = loader.load();
                aliasItemController = (ApisSelectBoxItemAliasController)loader.getController();
                aliasItemController.setModel(model);
                aliasItemController.setHandler(new ApisSelectBoxItemAliasController.SelectBoxItemAliasInterface() {
                    @Override
                    public void onMouseClicked(SelectBoxWalletItemModel itemModel) {
                        ApisSelectBoxController.this.setItemListVisible(false);
                        aliasHeaderController.setModel(itemModel);
                    }
                });

            }else if(boxType == SELECT_BOX_TYPE_ADDRESS){
                FXMLLoader loader = new FXMLLoader(addressItemUrl);
                itemNode = loader.load();
                addressItemController = (ApisSelectBoxItemAddressController)loader.getController();
            }

            itemList.getChildren().add(itemNode);
            walletItemModels.add(model);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setItemListVisible(boolean isVisible){ scrollPane.setVisible(isVisible); }
    public void toggleItemListVisible(){ scrollPane.setVisible(!scrollPane.isVisible()); }

}
