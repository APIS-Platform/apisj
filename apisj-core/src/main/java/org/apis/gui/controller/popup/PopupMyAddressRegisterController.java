package org.apis.gui.controller.popup;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.apis.db.sql.AddressGroupRecord;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.module.ApisTagItemController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.model.MyAddressModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.ByteUtil;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupMyAddressRegisterController extends BasePopupController {
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

        addressTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!addressTextField.getText().matches("[0-9a-fA-F]*")) {
                    addressTextField.setText(addressTextField.getText().replaceAll("[^0-9a-fA-F]", ""));
                }

                int maxlength = 40;
                if(addressTextField.getText().length() > maxlength){
                    addressTextField.setText(addressTextField.getText().substring(0, maxlength));
                }

                settingLayoutData();
            }
        });

        aliasTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                settingLayoutData();
            }
        });

        settingLayoutData();
    }

    private void languageSetting(){
        titleLabel.textProperty().bind(StringManager.getInstance().myAddress.registerTitle);
        subTitleLabel.textProperty().bind(StringManager.getInstance().myAddress.registerSubTitle);
        walletAddressLabel.textProperty().bind(StringManager.getInstance().myAddress.registerWalletAddress);
        walletNameLabel.textProperty().bind(StringManager.getInstance().myAddress.registerWalletName);
        groupLabel.textProperty().bind(StringManager.getInstance().myAddress.registerGroup);
        noBtn.textProperty().bind(StringManager.getInstance().common.backButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.saveButton);
    }

    public void initGroupList(){
        for(int i=0; i<textGroupList.size(); i++){
            URL labelUrl  = getClass().getClassLoader().getResource("scene/module/apis_tag_item.fxml");
            try {
                String text = this.textGroupList.get(i);

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
                            controller.setModel(model, false);
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

    public void settingLayoutData(){
        String address = addressTextField.getText();
        String name = aliasTextField.getText();

        if(address != null && address.length() > 0 && name != null && name.length() > 0){
            StyleManager.backgroundColorStyle(yesBtn, StyleManager.AColor.Cb01e1e);
        }else{
            StyleManager.backgroundColorStyle(yesBtn, StyleManager.AColor.Cd8d8d8);
        }
    }


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("yesBtn")){
            byte[] address = ByteUtil.hexStringToBytes(addressTextField.getText().trim());
            String alias = aliasTextField.getText().trim();

            if(address != null && address.length > 0 && alias != null && alias.length() > 0) {
                // 지갑 저장
                DBManager.getInstance().updateMyAddress(address, alias, 0);

                // 지갑과 그룹 연결 저장
                for(int i=0; i<selectGroupList.size(); i++){
                    DBManager.getInstance().updateConnectAddressGroup(address, selectGroupList.get(i));
                }

                PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(rootPane,"popup_my_address.fxml", 0);
                controller.setHandler(this.myAddressHandler);
                exit();
            }

        }else if(id.equals("noBtn")){

            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(rootPane,"popup_my_address.fxml", 0);
            controller.setHandler(this.myAddressHandler);
            exit();
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
        }
    }

    private PopupMyAddressController.PopupMyAddressImpl myAddressHandler;
    public void setMyAddressHandler(PopupMyAddressController.PopupMyAddressImpl myAddressHandler) {
        this.myAddressHandler = myAddressHandler;
    }
}
