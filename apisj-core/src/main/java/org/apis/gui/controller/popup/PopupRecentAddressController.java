package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.RecentAddressRecord;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.StringManager;
import org.apis.util.ByteUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupRecentAddressController extends BasePopupController {

    @FXML private VBox list;
    @FXML private Label titleLabel, subTitleLabel, addressLabel, addressLabel2, listLabel, timeLabel, selectLabel, noBtn, yesBtn;

    private String selectedAddress;
    private ArrayList<PopupRecentAddressItemController> controllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        List<RecentAddressRecord> list = DBManager.getInstance().selectRecentAddress();
        this.controllers.clear();
        this.list.getChildren().clear();
        for(int i=0; i<list.size(); i++){
            addItem(ByteUtil.toHexString(list.get(i).getAddress()), list.get(i).getAlias(), list.get(i).getCreatedAt());
        }
    }
    private void languageSetting(){
        titleLabel.textProperty().bind(StringManager.getInstance().recentAddress.title);
        subTitleLabel.textProperty().bind(StringManager.getInstance().recentAddress.subTitle);
        addressLabel.textProperty().bind(StringManager.getInstance().recentAddress.address);
        addressLabel2.textProperty().bind(StringManager.getInstance().recentAddress.address2);
        listLabel.textProperty().bind(StringManager.getInstance().recentAddress.list);
        timeLabel.textProperty().bind(StringManager.getInstance().recentAddress.time);
        selectLabel.textProperty().bind(StringManager.getInstance().recentAddress.select);
        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.yesButton);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if("yesBtn".equals(fxId)){
            if(handler != null){
                handler.onMouseClickYes(selectedAddress);
            }

            exit();
        }
    }

    public void addItem(String address, String alias, long createdAt){
        try {
            URL fxmlUrl = getClass().getClassLoader().getResource("scene/popup_recent_address_item.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            list.getChildren().add(loader.load());
            PopupRecentAddressItemController controller = (PopupRecentAddressItemController) loader.getController();
            controller.setData(address, alias, createdAt);
            controller.setHandler(new PopupRecentAddressItemController.PopupRecentAddressItemImpl() {
                @Override
                public void onMouseClicked(String address) {
                    if(address.equals(selectedAddress)){
                        selectedAddress = null;
                    }else{
                        selectedAddress = address;
                    }
                    for(int i=0; i<controllers.size(); i++){
                        controllers.get(i).setSelected(false);
                        if(controllers.get(i).getAddress().equals(selectedAddress)){
                            controllers.get(i).setSelected(true);
                        }
                    }
                }
            });
            this.controllers.add(controller);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private PopupRecentAddressImpl handler;
    public void setHandler(PopupRecentAddressImpl handler){
        this.handler = handler;
    }
    public interface PopupRecentAddressImpl {
        void onMouseClickYes(String address);
    }
}
