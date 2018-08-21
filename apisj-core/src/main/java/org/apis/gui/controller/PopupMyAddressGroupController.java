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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class PopupMyAddressGroupController implements Initializable {

    private ArrayList<String> textGroupList = new ArrayList<>();

    @FXML
    private FlowPane list;
    @FXML
    private TextField groupText;

    public void exit(){ AppManager.getInstance().guiFx.showMainPopup("popup_my_address_Register.fxml", 1); }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGroupList();
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnAdd")){
            if(groupText.getText().length() > 0) {

                if(DBManager.getInstance().addressGroupList.contains(groupText.getText())){
                    System.out.println("중복입니다...");
                }else{
                    DBManager.getInstance().addressGroupList.add(groupText.getText());
                    DBManager.getInstance().addressGroupList.sort(new Comparator<String>() {
                        @Override
                        public int compare(String text1, String text2) {
                            return text1.compareTo(text2);
                        }
                    });
                    textGroupList.add(groupText.getText());
                    initGroupList();
                }

                groupText.setText("");
            }
        }
    }

    private void initGroupList(){
        textGroupList = new ArrayList<>();
        list.getChildren().clear();
        for(int i=0; i<DBManager.getInstance().addressGroupList.size(); i++){
            textGroupList.add(DBManager.getInstance().addressGroupList.get(i));
            addList(DBManager.getInstance().addressGroupList.get(i));
        }
    }

    private void addList(String text){

        try {
            URL labelUrl  = new File("apisj-core/src/main/resources/scene/apis_tag_item.fxml").toURI().toURL();

            //item
            FXMLLoader loader = new FXMLLoader(labelUrl);
            Label label = loader.load();
            //label.setMaxWidth( (MAX_WIDTH - 10) / 2 + 20 );
            list.getChildren().add(label);

            ApisTagItemController itemController = (ApisTagItemController)loader.getController();
            itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
            itemController.setText(text);

            itemController.setState(ApisTagItemController.STATE_SETTING_NORAML);
            itemController.setHandle(new ApisTagItemController.ApisTagItemImpl() {
                @Override
                public void onMouseClicked(String text) {

                    // delete group
                    for(int i=0; i<DBManager.getInstance().addressGroupList.size(); i++){
                        if(DBManager.getInstance().addressGroupList.get(i).equals(text)){
                            DBManager.getInstance().addressGroupList.remove(i);
                            break;
                        }
                    }

                    // delete address group
                    for(int i=0; i<DBManager.getInstance().myAddressList.size(); i++){
                        for(int j=0; j<DBManager.getInstance().myAddressList.get(i).getGroupList().size(); j++){
                            if(DBManager.getInstance().myAddressList.get(i).getGroupList().get(j).equals(text)){
                                DBManager.getInstance().myAddressList.get(i).getGroupList().remove(j);
                            }
                        }
                    }

                    initGroupList();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
