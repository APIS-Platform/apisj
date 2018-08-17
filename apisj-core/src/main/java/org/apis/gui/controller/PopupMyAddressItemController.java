package org.apis.gui.controller;

import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.DBManager;
import org.apis.gui.model.MyAddressModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupMyAddressItemController implements Initializable {
    private final float MAX_WIDTH = 375;

    @FXML
    private GridPane gridPane;
    @FXML
    private ImageView icon, btnEdit, btnDelete, btnSelete, btnLeft, btnRight;
    @FXML
    private Label aliasLabel, addressLabel;
    @FXML
    private HBox list;

    private ArrayList<String> textList = new ArrayList<>();     // group text all
    private ArrayList<ArrayList<String>> groupList = new ArrayList<>(); // group text paging
    private int cursorIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        String address = addressLabel.getText();
        String alias = aliasLabel.getText();

        if(id.equals("btnEdit")){
            PopupMyAddressEditController editController = (PopupMyAddressEditController)AppManager.getInstance().guiFx.showMainPopup("popup_my_address_edit.fxml", 1);
            editController.init(address, alias, textList);

        }else if(id.equals("btnDelete")){
            for(int i=0; i<DBManager.getInstance().myAddressList.size(); i++){
                if(DBManager.getInstance().myAddressList.get(i).getAddress().equals(address)){
                    DBManager.getInstance().myAddressList.remove(i);
                    break;
                }
            }
            PopupMyAddressController myAddressController = (PopupMyAddressController)AppManager.getInstance().guiFx.showMainPopup("popup_my_address.fxml", 0);

        }else if(id.equals("btnSelete")){


        }else if(id.equals("btnLeft")){
            prevTextList();

        }else if(id.equals("btnRight")){
            nextTextList();

        }
    }

    // split textList to groupList
    private void setTextList(ArrayList<String> _textList){
        textList = _textList;
        ArrayList<String> tempTextList = new ArrayList<>();

        // sum(Label 가로길이 + 사이간격) <= 스크롤 가로길이
        float widthSum = 0;
        for(int i=0; i<textList.size(); i++){

            try {
                String text = textList.get(i);
                URL labelUrl  = new File("apisj-core/src/main/resources/scene/apis_tag_item.fxml").toURI().toURL();

                //item
                FXMLLoader loader = new FXMLLoader(labelUrl);
                Label label = loader.load();
                label.setMaxWidth( (MAX_WIDTH - 10) / 2 + 20 );
                list.getChildren().add(label);

                ApisTagItemController itemController = (ApisTagItemController)loader.getController();
                itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
                itemController.setText(text);

                if(widthSum > 0){
                    widthSum += 10; // spacing
                }

                float fontWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(label.getText(), label.getFont());
                widthSum += (fontWidth <= (MAX_WIDTH - 10) / 2 ) ? fontWidth + 20 : (MAX_WIDTH - 10) / 2 ;
                System.out.println("widthSum : "+widthSum);

                // split group
                if(MAX_WIDTH >= widthSum){
                    tempTextList.add(textList.get(i));
                }else{
                    groupList.add(tempTextList);
                    //init
                    tempTextList = new ArrayList<>();
                    widthSum = 0;
                    i--;
                }

                // add last group
                if(i == textList.size() -1 && tempTextList.size() > 0){
                    groupList.add(tempTextList);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showTextList(int cursorIndex){
        this.cursorIndex = cursorIndex;
        list.getChildren().clear();
        if(this.groupList.size() > 0) {
            for (int i = 0; i < this.groupList.get(cursorIndex).size(); i++) {
                try {
                    String text = this.groupList.get(cursorIndex).get(i);
                    URL labelUrl = new File("apisj-core/src/main/resources/scene/apis_tag_item.fxml").toURI().toURL();

                    //item
                    FXMLLoader loader = new FXMLLoader(labelUrl);
                    Label label = loader.load();
                    label.setMaxWidth((MAX_WIDTH - 10) / 2 + 20);
                    list.getChildren().add(label);

                    ApisTagItemController itemController = (ApisTagItemController) loader.getController();
                    itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
                    itemController.setText(text);

                    if (cursorIndex == 0 && i == 0) {
                        itemController.setState(ApisTagItemController.STATE_VIEW_ACTIVE);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void prevTextList(){
        if(cursorIndex > 0){
            cursorIndex--;
        }
        showTextList(cursorIndex);
    }

    public void nextTextList(){
        if(cursorIndex < this.groupList.size()-1){
            cursorIndex++;
        }
        showTextList(cursorIndex);
    }

    public void setAddress(String address) {
        this.addressLabel.setText(address);

        settingGroup(address);
    }

    public void setAlias(String alias) {
        this.aliasLabel.setText(alias);
    }

    public void settingGroup(String address){
        ArrayList<String> tempTextList = new ArrayList<>();

        MyAddressModel model = null;
        for(int i=0; i<DBManager.getInstance().myAddressList.size(); i++){
            model = DBManager.getInstance().myAddressList.get(i);
            System.out.println("model :"+model.getGroupList().size());
            if(model.getAddress().equals(address)){
                for(int j=0; j<model.getGroupList().size(); j++){
                    tempTextList.add(model.getGroupList().get(j));
                }
                break;
            }
        }
        setTextList(tempTextList);
        showTextList(0);

        // delete row
        System.out.println("this.groupList.size() : "+this.groupList.size());
        if(this.groupList.size() == 0) {
            for (int i = 0; i < gridPane.getChildren().size(); i++) {
                Node child = gridPane.getChildren().get(i);
                if (child.isManaged()) {
                    Integer rowIndex = GridPane.getRowIndex(child);
                    if (rowIndex != null) {
                        if (rowIndex == 1) {
                            gridPane.getChildren().remove(i);
                        }
                    }
                }
            }
            gridPane.getRowConstraints().remove(1);
        }
    }
}
