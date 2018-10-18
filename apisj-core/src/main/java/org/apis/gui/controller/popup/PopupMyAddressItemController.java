package org.apis.gui.controller.popup;

import com.google.zxing.WriterException;
import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import org.apis.db.sql.ConnectAddressGroupRecord;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.controller.module.ApisTagItemController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.model.MyAddressModel;
import org.apis.gui.model.base.BaseModel;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupMyAddressItemController extends BaseViewController {
    private final float MAX_WIDTH = 375;

    @FXML private GridPane gridPane;
    @FXML private ImageView icon, btnEdit, btnDelete, btnSelete, btnLeft, btnRight;
    @FXML private Label aliasLabel, addressLabel;
    @FXML private HBox list;

    private MyAddressModel model;

    private ArrayList<String> textList = new ArrayList<>();     // group text all
    private ArrayList<ArrayList<String>> groupList = new ArrayList<>(); // group text paging
    private int cursorIndex = 0;
    private boolean exist = false;

    private Image imageCheck = new Image("image/btn_circle_red@2x.png");
    private Image imageUnCheck = new Image("image/btn_circle_none@2x.png");

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
            model.setAddress(address);
            model.setAlias(alias);
            model.setGroupList(null);

            PopupMyAddressEditController editController = (PopupMyAddressEditController)PopupManager.getInstance().showMainPopup("popup_my_address_edit.fxml", 1);
            editController.setMyAddressHandler(myAddressHandler);
            editController.setModel(model);

            event.consume();
        }else if(id.equals("btnDelete")){
            DBManager.getInstance().deleteMyAddress(Hex.decode(address));
            PopupMyAddressController myAddressController = (PopupMyAddressController)PopupManager.getInstance().showMainPopup("popup_my_address.fxml", 0);
            myAddressController.setHandler(myAddressHandler);
            event.consume();
        }else if(id.equals("btnSelete")){

        }else if(id.equals("btnLeft")){
            prevTextList();

            event.consume();
        }else if(id.equals("btnRight")){
            nextTextList();

            event.consume();
        }else if(id.equals("rootPane")){
            if(this.handler != null){
                this.handler.onMouseClickedSelected(address);
            }
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
                URL labelUrl  = getClass().getClassLoader().getResource("scene/module/apis_tag_item.fxml");

                //item
                FXMLLoader loader = new FXMLLoader(labelUrl);
                Label label = loader.load();
                label.setMaxWidth( (MAX_WIDTH - 10) / 2 + 20 );
                list.getChildren().add(label);

                ApisTagItemController itemController = (ApisTagItemController)loader.getController();
                itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
                itemController.setText(text);
                itemController.setHandle(new ApisTagItemController.ApisTagItemImpl() {
                    @Override
                    public void onMouseClicked(String text) {
                        if(handler != null){
                            handler.onMouseClickedGroupTag(text);
                        }
                    }
                });

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
                    URL labelUrl = getClass().getClassLoader().getResource("scene/module/apis_tag_item.fxml");

                    //item
                    FXMLLoader loader = new FXMLLoader(labelUrl);
                    Label label = loader.load();
                    label.setMaxWidth((MAX_WIDTH - 10) / 2 + 20);
                    list.getChildren().add(label);

                    ApisTagItemController itemController = (ApisTagItemController) loader.getController();
                    itemController.setState(ApisTagItemController.STATE_VIEW_NORAML);
                    itemController.setText(text);
                    itemController.setHandle(new ApisTagItemController.ApisTagItemImpl() {
                        @Override
                        public void onMouseClicked(String text) {
                            if(handler != null){
                                handler.onMouseClickedGroupTag(text);
                            }
                        }
                    });

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

        try {
            this.icon.setImage(IdenticonGenerator.generateIdenticonsToImage(address, 128, 128));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        settingGroup(address);
    }

    public void setAlias(String alias) {
        this.aliasLabel.setText(alias);
    }

    public void settingGroup(String address){
        ArrayList<String> tempTextList = new ArrayList<>();

        List<ConnectAddressGroupRecord> list = DBManager.getInstance().selectConnectAddressGroup(Hex.decode(address));
        for(int i=0; i<list.size(); i++){
            tempTextList.add(list.get(i).getGroupName());
        }
        setTextList(tempTextList);
        showTextList(0);

        // delete row
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

    public void setSelected(boolean isSelected){
        if(isSelected){
            btnSelete.setImage(imageCheck);
        }else{
            btnSelete.setImage(imageUnCheck);
        }
    }

    @Override
    public void setModel(BaseModel model){
        this.model = (MyAddressModel)model;
    }

    private PopupMyAddressItemImpl handler;
    public void setHandler(PopupMyAddressItemImpl handler){
        this.handler = handler;
    }

    public String getAddress() {
        return this.addressLabel.getText().trim();
    }

    private PopupMyAddressController.PopupMyAddressImpl myAddressHandler;
    public void setMyAddressHandler(PopupMyAddressController.PopupMyAddressImpl myAddressHandler) {
        this.myAddressHandler = myAddressHandler;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
        btnDelete.setVisible(!this.exist);
    }

    public boolean getExist() {
        return this.exist;
    }

    public interface PopupMyAddressItemImpl{
        void onMouseClickedGroupTag(String text);
        void onMouseClickedSelected(String address);
    }
}
