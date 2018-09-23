package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.FlowPane;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.DBManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.model.MyAddressModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupMyAddressEditController extends BasePopupController {
    @FXML
    private FlowPane groupList;
    @FXML
    private TextField addressTextField, aliasTextField;

    private ArrayList<String> selectGroupList = new ArrayList<>();      // 선택한 그룹(String)
    private ArrayList<String> textGroupList = new ArrayList<>();                       // 선택할 수 있는 그룹 리스트 (String)
    private ArrayList<ApisTagItemController> groupControllerList = new ArrayList<>();  // 선택할 수 있는 그룹 리스트 (Object)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for(int i=0; i< DBManager.getInstance().addressGroupList.size(); i++){
            textGroupList.add(DBManager.getInstance().addressGroupList.get(i));
        }
        textGroupList.add("+ Add Group");
        initGroupList();
    }

    public void initGroupList(){
        for(int i=0; i<textGroupList.size(); i++){
            try {
                String text = this.textGroupList.get(i);
                URL labelUrl  = getClass().getClassLoader().getResource("scene/apis_tag_item.fxml");

                //item
                FXMLLoader loader = new FXMLLoader(labelUrl);
                Label label = loader.load();
                //label.setMaxWidth( (MAX_WIDTH - 10) / 2 + 20 );
                groupList.getChildren().add(label);

                ApisTagItemController itemController = (ApisTagItemController)loader.getController();
                itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
                itemController.setText(text);

                if(i == textGroupList.size()-1){
                    itemController.setState(ApisTagItemController.STATE_ADD_GROUP);
                    itemController.setHandle(new ApisTagItemController.ApisTagItemImpl() {
                        @Override
                        public void onMouseClicked(String text) {
                            PopupManager.getInstance().showMainPopup("popup_my_address_group.fxml", 1);
                        }
                    });
                }else{
                    itemController.setHandle(new ApisTagItemController.ApisTagItemImpl() {
                        @Override
                        public void onMouseClicked(String text) {
                            if(itemController.getState() == itemController.STATE_VIEW_NORAML){
                                itemController.setState(ApisTagItemController.STATE_VIEW_ACTIVE);
                                selectGroupList.add(text);
                            }else if(itemController.getState() == itemController.STATE_VIEW_ACTIVE){
                                itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
                                selectGroupList.remove(text);
                            }
                        }
                    });
                }

                this.groupControllerList.add(itemController);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnYes")){
            String address = addressTextField.getText();
            String alias = aliasTextField.getText();

            for(int i=0; i<DBManager.getInstance().myAddressList.size();i++){
                if(DBManager.getInstance().myAddressList.get(i).getAddress().equals(address)){
                    DBManager.getInstance().myAddressList.get(i).setAlias(alias);
                    DBManager.getInstance().myAddressList.get(i).setGroupList(selectGroupList);
                    PopupManager.getInstance().showMainPopup("popup_my_address.fxml", 0);
                    break;
                }
            }


            exit();
        }else if(id.equals("btnNo")){
            exit();
        }
    }

    public void init(String address, String alias, ArrayList<String> textList) {
        this.addressTextField.setText(address);
        this.aliasTextField.setText(alias);

        for(int i=0; i<textList.size(); i++){
            for(int j=0; j<groupControllerList.size(); j++){
                if(groupControllerList.get(j).getText().equals(textList.get(i))){
                    selectGroupList.add(textList.get(i));
                    groupControllerList.get(j).setState(ApisTagItemController.STATE_VIEW_ACTIVE);
                }
            }

        }
    }
}
