package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.gui.model.WalletItemModel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class WalletListController implements Initializable {

    private ArrayList<WalletListItem> itemsList = new ArrayList<WalletListItem>();

    @FXML
    private VBox listBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void addCreateWalletListItem(WalletItemModel model){
        WalletListItem item = new WalletListItem(listBox, itemsList, model);
        item.closeList();
        itemsList.add(item);
    }
    public void removeWalletListItemAll(){
        itemsList.clear();
    }

    public void setOpenItem(int index) {
        for(int i=0; i<itemsList.size(); i++){
            if(i == index){
                itemsList.get(i).openList();
            }else{
                itemsList.get(i).closeList();
            }
        }
    }

    class WalletListItem{
        private WalletItemModel model;

        private ArrayList<WalletListItem> itemsList = new ArrayList<WalletListItem>();

        private WalletListHeadController header;
        private WalletListBodyController apis;
        private WalletListBodyController mineral;

        private boolean isOpen = false;

        public WalletListItem(VBox parent, ArrayList<WalletListItem> itemsList, WalletItemModel model){
            this.model = model;
            this.itemsList = itemsList;

            try {
                URL headerUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_header.fxml").toURI().toURL();
                URL bodyUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_body.fxml").toURI().toURL();

                //header
                FXMLLoader loader = new FXMLLoader(headerUrl);
                parent.getChildren().add(loader.load());
                header = (WalletListHeadController)loader.getController();
                header.setModel(this.model);
                header.setHandler(new WalletListHeadController.WalletListHeaderInterface() {
                    @Override
                    public void onClickEvent(InputEvent event) {
                        boolean isOpen = WalletListItem.this.isOpen;

                        for(int i=0; i<WalletListItem.this.itemsList.size(); i++){
                            WalletListItem.this.itemsList.get(i).closeList();
                            if(WalletListItem.this.itemsList.get(i) == WalletListItem.this
                                    && isOpen == false){
                                WalletListItem.this.itemsList.get(i).openList();
                            }
                        }
                    }
                });
                //apis
                loader = new FXMLLoader(bodyUrl);
                parent.getChildren().add(loader.load());
                apis = (WalletListBodyController)loader.getController();
                apis.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_APIS);
                apis.setModel(this.model);

                //mineral
                loader = new FXMLLoader(bodyUrl);
                parent.getChildren().add(loader.load());
                mineral = (WalletListBodyController)loader.getController();
                mineral.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_MINERAL);
                mineral.setModel(this.model);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public void openList(){
            this.header.setState(WalletListHeadController.HEADER_STATE_OPEN);
            this.apis.show();
            this.mineral.show();
            this.isOpen = true;
        }
        public void closeList(){
            this.header.setState(WalletListHeadController.HEADER_STATE_CLOSE);
            this.apis.hide();
            this.mineral.hide();
            this.isOpen = false;
        }

        public WalletListHeadController getHeader() {
            return header;
        }

        public void setHeader(WalletListHeadController header) {
            this.header = header;
        }

        public WalletListBodyController getApis() {
            return apis;
        }

        public void setApis(WalletListBodyController apis) {
            this.apis = apis;
        }

        public WalletListBodyController getMineral() {
            return mineral;
        }

        public void setMineral(WalletListBodyController mineral) {
            this.mineral = mineral;
        }
    }
}
