package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apis.gui.model.WalletItemModel;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class WalletListController implements Initializable {
    public static final int SORT_ALIAS_ASC = 1;
    public static final int SORT_ALIAS_DESC = 2;
    public static final int SORT_BALANCE_ASC = 3;
    public static final int SORT_BALANCE_DESC = 4;

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


    public void sort(int sortType){
        switch (sortType){
            case SORT_ALIAS_ASC :
                itemsList.sort(new Comparator<WalletListItem>(){
                    public int compare(WalletListItem item1, WalletListItem item2){
                        return item1.getModel().getAlias().compareTo(item2.getModel().getAlias());
                    }
                });

                break;
            case SORT_ALIAS_DESC :
                itemsList.sort(new Comparator<WalletListItem>(){
                    public int compare(WalletListItem item1, WalletListItem item2){
                        return item2.getModel().getAlias().compareTo(item1.getModel().getAlias());
                    }
                });

                break;
            case SORT_BALANCE_ASC :
                itemsList.sort(new Comparator<WalletListItem>(){
                    public int compare(WalletListItem item1, WalletListItem item2){
                        BigInteger big1 = new BigInteger(item1.getApis().getBalance());
                        BigInteger big2 = new BigInteger(item2.getApis().getBalance());
                        return big1.compareTo(big2);
                    }
                });

                break;
            case SORT_BALANCE_DESC :
                itemsList.sort(new Comparator<WalletListItem>(){
                    public int compare(WalletListItem item1, WalletListItem item2){
                        BigInteger big1 = new BigInteger(item1.getApis().getBalance());
                        BigInteger big2 = new BigInteger(item2.getApis().getBalance());
                        return big2.compareTo(big1);
                    }
                });

                break;
        }

        //ArrayList<WalletListItem> tempItems = itemsList.
        listBox.getChildren().clear();
        for(int i=0; i<itemsList.size(); i++){
            listBox.getChildren().add(itemsList.get(i).getHeaderNode());
            listBox.getChildren().add(itemsList.get(i).getApisNode());
            listBox.getChildren().add(itemsList.get(i).getMineralNode());
        }
    }



    class WalletListItem{
        private WalletItemModel model;
        private Node headerNode;
        private Node apisNode;
        private Node mineralNode;

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
                headerNode = loader.load();
                parent.getChildren().add(headerNode);
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
                apisNode = loader.load();
                parent.getChildren().add(apisNode);
                apis = (WalletListBodyController)loader.getController();
                apis.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_APIS);
                apis.setModel(this.model);

                //mineral
                loader = new FXMLLoader(bodyUrl);
                mineralNode = loader.load();
                parent.getChildren().add(mineralNode);
                mineral = (WalletListBodyController)loader.getController();
                mineral.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_MINERAL);
                mineral.setModel(this.model);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        public void setModel(WalletItemModel model){
            this.model = model;
            header.setModel(this.model);
            apis.setModel(this.model);
            mineral.setModel(this.model);
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

        public WalletItemModel getModel(){ return this.model;}
        public Node getHeaderNode(){return this.headerNode;}
        public Node getApisNode(){return this.apisNode;}
        public Node getMineralNode(){return this.mineralNode;}
    }
}
