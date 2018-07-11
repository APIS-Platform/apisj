package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;

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

        itemsList.add(new WalletListItem(listBox, itemsList));
        itemsList.add(new WalletListItem(listBox, itemsList));
        itemsList.add(new WalletListItem(listBox, itemsList));
        itemsList.add(new WalletListItem(listBox, itemsList));


        for(int i=0; i<itemsList.size(); i++){
            itemsList.get(i).closeList();
        }
        itemsList.get(0).openList();
    }


    class WalletListItem{
        private ArrayList<WalletListItem> itemsList = new ArrayList<WalletListItem>();

        private WalletListHeadController header;
        private WalletListBodyController apis;
        private WalletListBodyController mineral;

        public WalletListItem(VBox parent, ArrayList<WalletListItem> itemsList){
            this.itemsList = itemsList;

            try {
                URL headerUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_header.fxml").toURI().toURL();
                URL bodyUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_body.fxml").toURI().toURL();

                //header
                FXMLLoader loader = new FXMLLoader(headerUrl);
                parent.getChildren().add(loader.load());
                header = (WalletListHeadController)loader.getController();
                header.setHandler(new WalletListHeadController.WalletListHeaderInterface() {
                    @Override
                    public void onClickEvent(InputEvent event) {

                        for(int i=0; i<WalletListItem.this.itemsList.size(); i++){
                            WalletListItem.this.itemsList.get(i).closeList();
                            if(WalletListItem.this.itemsList.get(i) == WalletListItem.this){
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

                //mineral
                loader = new FXMLLoader(bodyUrl);
                parent.getChildren().add(loader.load());
                mineral = (WalletListBodyController)loader.getController();
                mineral.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_MINERAL);

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
        }
        public void closeList(){
            this.header.setState(WalletListHeadController.HEADER_STATE_CLOSE);
            this.apis.hide();
            this.mineral.hide();
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
