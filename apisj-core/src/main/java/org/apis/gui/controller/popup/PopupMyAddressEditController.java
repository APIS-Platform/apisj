package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.apis.db.sql.AddressGroupRecord;
import org.apis.db.sql.ConnectAddressGroupRecord;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.module.ApisTagItemController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.MyAddressModel;
import org.apis.gui.model.base.BaseModel;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupMyAddressEditController extends BasePopupController {
    @FXML private AnchorPane rootPane;
    @FXML private FlowPane groupList;
    @FXML private TextField addressTextField, aliasTextField;
    @FXML private Label titleLabel, subTitleLabel, walletAddressLabel, walletNameLabel, groupLabel, noBtn, yesBtn;

    private ArrayList<String> selectGroupList = new ArrayList<>();      // 선택한 그룹(String)
    private ArrayList<String> textGroupList = new ArrayList<>();                       // 선택할 수 있는 그룹 리스트 (String)
    private ArrayList<ApisTagItemController> groupControllerList = new ArrayList<>();  // 선택할 수 있는 그룹 리스트 (Object)
    private MyAddressModel model;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        List<AddressGroupRecord> groups = DBManager.getInstance().selectAddressGroups();
        for(int i=0; i<groups.size(); i++){
            textGroupList.add(groups.get(i).getGroupName());
        }
        textGroupList.add("+ "+StringManager.getInstance().myAddress.addGroup.get());
        initGroupList();
    }

    private void languageSetting(){
        titleLabel.textProperty().bind(StringManager.getInstance().myAddress.editTitle);
        subTitleLabel.textProperty().bind(StringManager.getInstance().myAddress.editSubTitle);
        walletAddressLabel.textProperty().bind(StringManager.getInstance().myAddress.editWalletAddress);
        walletNameLabel.textProperty().bind(StringManager.getInstance().myAddress.editWalletName);
        groupLabel.textProperty().bind(StringManager.getInstance().myAddress.editGroup);
        noBtn.textProperty().bind(StringManager.getInstance().common.backButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.saveButton);
    }

    public void initGroupList(){
        for(int i=0; i<textGroupList.size(); i++){
            try {
                String text = this.textGroupList.get(i);
                URL labelUrl  = getClass().getClassLoader().getResource("scene/module/apis_tag_item.fxml");

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
                            model.setAddress(addressTextField.getText().trim());
                            model.setAlias(aliasTextField.getText().trim());
                            model.setGroupList(selectGroupList);

                            PopupMyAddressGroupController controller = (PopupMyAddressGroupController)PopupManager.getInstance().showMainPopup(rootPane, "popup_my_address_group.fxml", 1);
                            controller.setMyAddressHandler(myAddressHandler);
                            controller.setModel(model, true);
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
        if(id.equals("yesBtn")){
            byte[] address = Hex.decode(addressTextField.getText().trim());
            String alias = aliasTextField.getText().trim();

            // 지갑 저장
            DBManager.getInstance().updateMyAddress(address, alias, model.getExist());

            // 지갑과 그룹 연결 저장
            DBManager.getInstance().deleteConnectAddressGroup(address);
            for(int i=0; i<selectGroupList.size(); i++){
                DBManager.getInstance().updateConnectAddressGroup(address, selectGroupList.get(i));
            }

            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(rootPane, "popup_my_address.fxml", 0);
            controller.setHandler(this.myAddressHandler);
            exit();
        }else if(id.equals("noBtn")){

            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(rootPane, "popup_my_address.fxml", 0);
            controller.setHandler(this.myAddressHandler);
            exit();
        }
    }

    private void init(String address) {
        String alias = DBManager.getInstance().selectMyAddressSearch(address).get(0).getAlias();
        ArrayList<String> textList = new ArrayList<>();
        List<ConnectAddressGroupRecord> list = DBManager.getInstance().selectConnectAddressGroup(Hex.decode(address));
        for(int i=0; i<list.size(); i++){
            textList.add(list.get(i).getGroupName());
        }

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

    @Override
    public void setModel(BaseModel model){
        this.model = (MyAddressModel)model;

        this.addressTextField.setText(this.model.getAddress());
        this.aliasTextField.setText(this.model.getAlias());

        if(this.model.getGroupList() != null){
            this.selectGroupList = this.model.getGroupList();
            for(int i=0; i<selectGroupList.size(); i++){
                for(int j=0; j<groupControllerList.size(); j++){
                    if(groupControllerList.get(j).getText().equals(selectGroupList.get(i))){
                        groupControllerList.get(j).setState(ApisTagItemController.STATE_VIEW_ACTIVE);
                    }
                }

            }
        }else{
            ArrayList<String> textList = new ArrayList<>();
            List<ConnectAddressGroupRecord> list = DBManager.getInstance().selectConnectAddressGroup(Hex.decode(this.model.getAddress()));
            for(int i=0; i<list.size(); i++){
                textList.add(list.get(i).getGroupName());
            }
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

    private PopupMyAddressController.PopupMyAddressImpl myAddressHandler;
    public void setMyAddressHandler(PopupMyAddressController.PopupMyAddressImpl myAddressHandler) {
        this.myAddressHandler = myAddressHandler;
    }
}
