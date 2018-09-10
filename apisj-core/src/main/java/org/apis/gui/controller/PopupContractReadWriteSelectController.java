package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.db.sql.ContractRecord;
import org.apis.db.sql.DBManager;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.ContractModel;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PopupContractReadWriteSelectController implements Initializable {

    // Multilingual Support Label
    @FXML
    private Label readWriteTitle, readWriteSelect, addrLabel, newLabel, listLabel, editLabel, deleteLabel, selectLabel, noBtn, yesBtn;
    @FXML
    private VBox list;

    private ArrayList<PopupContractReadWriteListController> itemControllers = new ArrayList<>();
    private PopupContractReadWriteListController checkItemController;

    private PopupContractReadWriteSelectImpl handler;

    public void setHandler(PopupContractReadWriteSelectImpl handler) {
        this.handler = handler;
    }

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();



        //db : select contracts
        List<ContractRecord> list = DBManager.getInstance().selectContracts();
        System.out.println("list : "+list.size());
        for(int i=0; i<list.size(); i++){
            ContractModel model = new ContractModel();
            model.setName(list.get(i).getTitle());
            model.setAddress(Hex.toHexString(list.get(i).getAddress()));
            model.setAbi(list.get(i).getAbi());
            addItem(model);
        }

    }

    public void languageSetting() {
        readWriteTitle.textProperty().bind(StringManager.getInstance().contractPopup.readWriteTitle);
        readWriteSelect.textProperty().bind(StringManager.getInstance().contractPopup.readWriteSelect);
        addrLabel.textProperty().bind(StringManager.getInstance().contractPopup.addrLabel);
        newLabel.textProperty().bind(StringManager.getInstance().contractPopup.newLabel);
        listLabel.textProperty().bind(StringManager.getInstance().contractPopup.listLabel);
        editLabel.textProperty().bind(StringManager.getInstance().contractPopup.editLabel);
        deleteLabel.textProperty().bind(StringManager.getInstance().contractPopup.deleteLabel);
        selectLabel.textProperty().bind(StringManager.getInstance().contractPopup.selectLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        yesBtn.textProperty().bind(StringManager.getInstance().contractPopup.yesBtn);
    }

    public void addItem(ContractModel model){
        try {
            URL itemUrl  = new File("apisj-core/src/main/resources/scene/popup_contract_read_write_list.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(itemUrl);
            Node itemNode = loader.load();
            list.getChildren().add(itemNode);

            PopupContractReadWriteListController itemController = (PopupContractReadWriteListController)loader.getController();
            itemController.setModel(model);
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
            AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_create.fxml",1);
        }else if(fxid.equals("yesBtn")){
            if(checkItemController != null){
                if(handler != null){
                    handler.onClickSelect(checkItemController.getModel());
                }
                AppManager.getInstance().guiFx.hideMainPopup(0);
            }
        }
    }


    public interface PopupContractReadWriteSelectImpl{
        void onClickSelect(ContractModel model);
    }
}
