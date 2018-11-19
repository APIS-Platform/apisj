package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.db.sql.ContractRecord;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.ContractModel;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupContractReadWriteSelectController extends BasePopupController {

    // Multilingual Support Label
    @FXML private AnchorPane rootPane;
    @FXML private Label readWriteTitle, readWriteSelect, addrLabel, newLabel, listLabel, editLabel, deleteLabel, selectLabel, noBtn, yesBtn;
    @FXML private VBox list;
    @FXML private ScrollPane listPane;

    private ArrayList<PopupContractReadWriteListController> itemControllers = new ArrayList<>();
    private PopupContractReadWriteListController checkItemController;

    private PopupContractReadWriteSelectImpl handler;

    public void setHandler(PopupContractReadWriteSelectImpl handler) {
        this.handler = handler;

        //db : select contracts
        List<ContractRecord> list = DBManager.getInstance().selectContracts();
        if(list.size() == 0){
            listPane.setVisible(false);
        }else{
            listPane.setVisible(true);
        }
        for(int i=0; i<list.size(); i++){
            ContractModel model = new ContractModel();
            model.setName(list.get(i).getTitle());
            model.setAddress(Hex.toHexString(list.get(i).getAddress()));
            model.setAbi(list.get(i).getAbi());
            addItem(model);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();
    }

    public void languageSetting() {
        readWriteTitle.textProperty().bind(StringManager.getInstance().contractPopup.readWriteTitle);
        readWriteSelect.textProperty().bind(StringManager.getInstance().contractPopup.readWriteSelect);
        addrLabel.textProperty().bind(StringManager.getInstance().contractPopup.addrLabel);
        newLabel.textProperty().bind(StringManager.getInstance().contractPopup.newLabel);
        listLabel.textProperty().bind(StringManager.getInstance().contractPopup.listLabel);
        editLabel.textProperty().bind(StringManager.getInstance().common.editLabel);
        deleteLabel.textProperty().bind(StringManager.getInstance().common.deleteLabel);
        selectLabel.textProperty().bind(StringManager.getInstance().common.selectLabel);
        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.yesButton);
    }

    public void addItem(ContractModel model){
        try {
            URL itemUrl = getClass().getClassLoader().getResource("scene/popup/popup_contract_read_write_list.fxml");
            FXMLLoader loader = new FXMLLoader(itemUrl);
            Node itemNode = loader.load();
            list.getChildren().add(itemNode);

            PopupContractReadWriteListController itemController = (PopupContractReadWriteListController)loader.getController();
            itemController.setModel(model);
            itemController.setContractSelectHandler(this.handler);
            itemController.setHandler(new PopupContractReadWriteListController.PopupContractReadWriteListImpl() {
                @Override
                public void changed(PopupContractReadWriteListController obj, boolean isSelected) {
                    if(isSelected){
                        checkItemController = obj;
                    }else{
                        checkItemController = null;
                    }

                    for(int i=0; i<itemControllers.size(); i++){
                        if(checkItemController == itemControllers.get(i)){
                            itemControllers.get(i).setSelected(true);
                        }else{
                            itemControllers.get(i).setSelected(false);
                        }
                    }
                }
            });

            itemControllers.add(itemController);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("newContractBtn")) {
            PopupContractReadWriteCreateController controller =  (PopupContractReadWriteCreateController)PopupManager.getInstance().showMainPopup(rootPane, "popup_contract_read_write_create.fxml",1);
            controller.setContractSelectHandler(this.handler);
        }else if(fxid.equals("yesBtn")){
            if(checkItemController != null){
                if(handler != null){
                    handler.onClickSelect(checkItemController.getModel());
                }
                exit();
            }
        }
    }


    public interface PopupContractReadWriteSelectImpl{
        void onClickSelect(ContractModel model);
    }
}
