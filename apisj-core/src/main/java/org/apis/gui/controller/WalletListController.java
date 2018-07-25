package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;
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

    public static final int LIST_TYPE_ITEM = 0;
    public static final int LIST_TYPE_GROUP = 1;

    private int listType = LIST_TYPE_ITEM;
    private WalletListEvent handler;

    // Wallet Tab 리스트
    private ArrayList<WalletListItem> itemList = new ArrayList<WalletListItem>();

    // Apis & Mineral 리스트, 토큰 추가시 tokenList 추가필요.
    private ArrayList<WalletListGroupItem> groupList = new ArrayList<>();
    private WalletListGroupItem apisList;
    private WalletListGroupItem mineralList;


    @FXML
    private VBox listBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void init(int listType){
        this.listType = listType;
    }

    public void addCreateWalletListItem(WalletItemModel model){
        WalletListItem item = new WalletListItem(listBox, itemList, model);
        item.closeList();
        itemList.add(item);

        if(apisList == null) {
            apisList = new WalletListGroupItem(listBox, groupList, model, WalletListGroupItem.WALLET_LIST_GROUP_TYPE_APIS);
            groupList.add(apisList);
        }
        apisList.add(model);


        if(mineralList == null){
            mineralList = new WalletListGroupItem(listBox, groupList, model, WalletListGroupItem.WALLET_LIST_GROUP_TYPE_MINERAL);
            groupList.add(mineralList);
        }
        mineralList.add(model);


    }
    public void removeWalletListItemAll(){
        itemList.clear();
    }

    public void setOpenItem(int index) {
        for(int i = 0; i< itemList.size(); i++){
            if(i == index){
                itemList.get(i).openList();
            }else{
                itemList.get(i).closeList();
            }
        }
    }

    public void setOpenGroupItem(int index) {
        for(int i = 0; i< groupList.size(); i++){
            if(i == index){
                groupList.get(i).openList();
            }else{
                groupList.get(i).closeList();
            }
        }
    }

    public void check(WalletItemModel model) {
        for(int i = 0; i< itemList.size(); i++){
            if(model.getId().equals(itemList.get(i).getModel().getId())){
                itemList.get(i).setCheck(true);
                break;
            }
        }
    }

    public void checkAll() {
        for(int i = 0; i< itemList.size(); i++){
            itemList.get(i).setCheck(true);
        }
    }

    public void unCheck(WalletItemModel model) {
        for(int i = 0; i< itemList.size(); i++){
            if(model.getId().equals(itemList.get(i).getModel().getId())){
                itemList.get(i).setCheck(false);
                break;
            }
        }
    }

    public void unCheckAll(){
        for(int i = 0; i< itemList.size(); i++){
            itemList.get(i).setCheck(false);
        }
    }


    // VBOX에 담긴 Node를 모두 삭제 하고, 정렬된 Node를 추가한다.
    public void sort(int sortType){
        listBox.getChildren().clear();
        if(this.listType  == LIST_TYPE_ITEM) {
            switch (sortType){
                case SORT_ALIAS_ASC :
                    itemList.sort(new Comparator<WalletListItem>(){
                        public int compare(WalletListItem item1, WalletListItem item2){
                            return item1.getModel().getAlias().toLowerCase().compareTo(item2.getModel().getAlias().toLowerCase());
                        }
                    });

                    break;
                case SORT_ALIAS_DESC :
                    itemList.sort(new Comparator<WalletListItem>(){
                        public int compare(WalletListItem item1, WalletListItem item2){
                            return item2.getModel().getAlias().toLowerCase().compareTo(item1.getModel().getAlias().toLowerCase());
                        }
                    });

                    break;
                case SORT_BALANCE_ASC :
                    itemList.sort(new Comparator<WalletListItem>(){
                        public int compare(WalletListItem item1, WalletListItem item2){
                            BigInteger big1 = new BigInteger(item1.getApis().getBalance());
                            BigInteger big2 = new BigInteger(item2.getApis().getBalance());
                            return big1.compareTo(big2);
                        }
                    });

                    break;
                case SORT_BALANCE_DESC :
                    itemList.sort(new Comparator<WalletListItem>(){
                        public int compare(WalletListItem item1, WalletListItem item2){
                            BigInteger big1 = new BigInteger(item1.getApis().getBalance());
                            BigInteger big2 = new BigInteger(item2.getApis().getBalance());
                            return big2.compareTo(big1);
                        }
                    });

                    break;
            }

            for (int i = 0; i < itemList.size(); i++) {
                listBox.getChildren().add(itemList.get(i).getHeaderNode());
                listBox.getChildren().add(itemList.get(i).getApisNode());
                listBox.getChildren().add(itemList.get(i).getMineralNode());
            }
        }else if(this.listType  == LIST_TYPE_GROUP) {
            for(int i=0; i<groupList.size(); i++){
                switch (sortType){
                    case SORT_ALIAS_ASC     : groupList.get(i).sort(WalletListGroupItem.SORT_ALIAS_ASC); break;
                    case SORT_ALIAS_DESC    : groupList.get(i).sort(WalletListGroupItem.SORT_ALIAS_DESC); break;
                    case SORT_BALANCE_ASC   : groupList.get(i).sort(WalletListGroupItem.SORT_BALANCE_ASC); break;
                    case SORT_BALANCE_DESC  : groupList.get(i).sort(WalletListGroupItem.SORT_BALANCE_DESC); break;
                }
            }

            listBox.getChildren().add(apisList.getHeaderNode());
            for(int i=0; i<apisList.getItemNodeList().size(); i++){
                listBox.getChildren().add(apisList.getItemNodeList().get(i));
            }
            listBox.getChildren().add(mineralList.getHeaderNode());
            for(int i=0; i<mineralList.getItemNodeList().size(); i++){
                listBox.getChildren().add(mineralList.getItemNodeList().get(i));
            }
        }
    }

    public WalletListEvent getHandler() { return handler; }
    public void setHandler(WalletListEvent handler) { this.handler = handler; }


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
            try {
                URL headerUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_header.fxml").toURI().toURL();
                URL bodyUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_body.fxml").toURI().toURL();

                //header
                FXMLLoader loader = new FXMLLoader(headerUrl);
                headerNode = loader.load();
                parent.getChildren().add(headerNode);
                header = (WalletListHeadController)loader.getController();
                header.init(WalletListHeadController.WALLET_LIST_HEADER_TYPE_GROUP);
                header.setModel(this.model);
                header.setHandler(new WalletListHeadController.WalletListHeaderInterface() {
                    @Override
                    public void onClickEvent(InputEvent event) {

                        boolean isOpen = WalletListItem.this.isOpen;

                        for(int i=0; i<itemsList.size(); i++){
                            itemsList.get(i).closeList();
                            if(itemsList.get(i) == WalletListItem.this
                                    && isOpen == false){
                                itemsList.get(i).openList();
                            }
                        }
                    }

                    @Override
                    public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                        if(handler != null){
                            handler.onChangeCheck(model, isChecked);
                        }
                    }

                    @Override
                    public void onClickTransfer(InputEvent event) {
                        AppManager.getInstance().guiFx.getTransfer().init(model.getId());
                        AppManager.getInstance().guiFx.getMain().selectedHeader(1);
                    }

                    @Override
                    public void onClickCopy(String address) {
                       PopupCopyWalletAddressController controller = (PopupCopyWalletAddressController)AppManager.getInstance().guiFx.showMainPopup("popup_copy_wallet_address.fxml",0);
                       controller.setAddress(address);
                    }

                    @Override
                    public void onClickAddressMasking(InputEvent event) {
                        PopupMaskingController controller = (PopupMaskingController)AppManager.getInstance().guiFx.showMainPopup("popup_masking.fxml",0);

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

        public void setCheck(boolean isCheck){
            this.header.setCheck(isCheck);
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

    class WalletListGroupItem{
        public static final int SORT_ALIAS_ASC = 1;
        public static final int SORT_ALIAS_DESC = 2;
        public static final int SORT_BALANCE_ASC = 3;
        public static final int SORT_BALANCE_DESC = 4;

        public static final int WALLET_LIST_GROUP_TYPE_APIS = 0;
        public static final int WALLET_LIST_GROUP_TYPE_MINERAL = 1;

        private WalletItemModel model;
        private ArrayList<WalletItemModel> itemList = new ArrayList<>();
        private Node headerNode;

        private WalletListHeadController header;
        private ArrayList<WalletListBodyController> itemControllerList = new ArrayList<>();
        private VBox parent;

        private boolean isOpen = false;
        private int groupType = WALLET_LIST_GROUP_TYPE_APIS;

        public WalletListGroupItem(VBox parent, ArrayList<WalletListGroupItem> groupList, WalletItemModel model, int groupType){
            try {
                this.parent = parent;
                this.groupType = groupType;

                URL headerUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_header.fxml").toURI().toURL();

                setModel(model);

                //header
                FXMLLoader loader = new FXMLLoader(headerUrl);
                headerNode = loader.load();
                parent.getChildren().add(headerNode);
                header = (WalletListHeadController)loader.getController();
                if(this.groupType == WalletListGroupItem.WALLET_LIST_GROUP_TYPE_APIS) {
                    header.init(WalletListHeadController.WALLET_LIST_HEADER_TYPE_APIS);
                }else{
                    header.init(WalletListHeadController.WALLET_LIST_HEADER_TYPE_MINERAL);
                }

                header.setModel(this.model);
                header.setHandler(new WalletListHeadController.WalletListHeaderInterface() {
                    @Override
                    public void onClickEvent(InputEvent event) {
                        boolean isOpen = WalletListGroupItem.this.isOpen;

                        for(int i=0; i<groupList.size(); i++){
                            groupList.get(i).closeList();
                            if(groupList.get(i) == WalletListGroupItem.this
                                    && isOpen == false){
                                groupList.get(i).openList();
                            }
                        }
                    }

                    @Override
                    public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                        if(handler != null){
                            handler.onChangeCheck(model, isChecked);
                        }
                    }

                    @Override
                    public void onClickTransfer(InputEvent event) { }

                    @Override
                    public void onClickCopy(String address) { }

                    @Override
                    public void onClickAddressMasking(InputEvent event) { }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public void openList(){
            this.header.setState(WalletListHeadController.HEADER_STATE_OPEN);
            for(int i=0; i<itemControllerList.size(); i++){
                itemControllerList.get(i).show();
            }
            this.isOpen = true;
        }
        public void closeList(){
            this.header.setState(WalletListHeadController.HEADER_STATE_CLOSE);
            for(int i=0; i<itemControllerList.size(); i++){
                itemControllerList.get(i).hide();
            }
            this.isOpen = false;
        }

        public void add(WalletItemModel model) {
            try {
                URL bodyUrl  = new File("apisj-core/src/main/resources/scene/wallet_list_body.fxml").toURI().toURL();
                itemList.add(model);

                FXMLLoader loader = new FXMLLoader(bodyUrl);
                parent.getChildren().add(loader.load());
                WalletListBodyController itemController = (WalletListBodyController)loader.getController();
                if(this.groupType == WALLET_LIST_GROUP_TYPE_APIS) {
                    itemController.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_APIS_ADDRESS);
                }else if(this.groupType == WALLET_LIST_GROUP_TYPE_MINERAL) {
                    itemController.init(WalletListBodyController.WALLET_LIST_BODY_TYPE_MINERAL_ADDRESS);
                }
                itemController.setModel(model);
                itemController.setHandler(new WalletListBodyController.WalletListBodyInterface() {


                    @Override
                    public void onClickEvent(InputEvent event) {

                    }

                    @Override
                    public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                        if(handler != null){
                            handler.onChangeCheck(model, isChecked);
                        }
                    }

                    @Override
                    public void onClickTransfer(InputEvent event) {
                        AppManager.getInstance().guiFx.getTransfer().init(model.getId());
                        AppManager.getInstance().guiFx.getMain().selectedHeader(1);
                    }

                    @Override
                    public void onClickCopy(String address) {
                        PopupCopyWalletAddressController controller = (PopupCopyWalletAddressController)AppManager.getInstance().guiFx.showMainPopup("popup_copy_wallet_address.fxml",0);
                        controller.setAddress(address);
                    }

                    @Override
                    public void onClickAddressMasking(InputEvent event) {
                        PopupMaskingController controller = (PopupMaskingController)AppManager.getInstance().guiFx.showMainPopup("popup_masking.fxml",0);

                    }
                });
                itemControllerList.add(itemController);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public Node getHeaderNode(){ return this.headerNode; }

        public void setModel(WalletItemModel model){
            this.model = model;
        }

        public ArrayList<WalletItemModel> getItemList(){ return this.itemList; }
        public ArrayList<Node> getItemNodeList(){
            ArrayList<Node> list = new ArrayList<>();

            for(int i=0; i<itemControllerList.size(); i++){
                list.add(itemControllerList.get(i).getRootPane());
            }

            return list;
        }

        public void sort(int sortType) {
            switch (sortType){
                case SORT_ALIAS_ASC :
                    itemList.sort(new Comparator<WalletItemModel>(){
                        public int compare(WalletItemModel item1, WalletItemModel item2){
                            return item1.getAlias().toLowerCase().compareTo(item2.getAlias().toLowerCase());
                        }
                    });
                    itemControllerList.sort(new Comparator<WalletListBodyController>(){
                        public int compare(WalletListBodyController item1, WalletListBodyController item2){
                            return item1.getModel().getAlias().toLowerCase().compareTo(item2.getModel().getAlias().toLowerCase());
                        }
                    });

                    break;
                case SORT_ALIAS_DESC :
                    itemList.sort(new Comparator<WalletItemModel>(){
                        public int compare(WalletItemModel item1, WalletItemModel item2){
                            return item2.getAlias().toLowerCase().compareTo(item1.getAlias().toLowerCase());
                        }
                    });
                    itemControllerList.sort(new Comparator<WalletListBodyController>(){
                        public int compare(WalletListBodyController item1, WalletListBodyController item2){
                            return item2.getModel().getAlias().toLowerCase().compareTo(item1.getModel().getAlias().toLowerCase());
                        }
                    });

                    break;
                case SORT_BALANCE_ASC :
                    itemList.sort(new Comparator<WalletItemModel>(){
                        public int compare(WalletItemModel item1, WalletItemModel item2){
                            BigInteger big1 = new BigInteger(item1.getBalance().replace(".",""));
                            BigInteger big2 = new BigInteger(item2.getBalance().replace(".",""));
                            return big1.compareTo(big2);
                        }
                    });
                    itemControllerList.sort(new Comparator<WalletListBodyController>(){
                        public int compare(WalletListBodyController item1, WalletListBodyController item2){
                            BigInteger big1 = new BigInteger(item1.getModel().getBalance().replace(".",""));
                            BigInteger big2 = new BigInteger(item2.getModel().getBalance().replace(".",""));
                            return big1.compareTo(big2);
                        }
                    });

                    break;
                case SORT_BALANCE_DESC :
                    itemList.sort(new Comparator<WalletItemModel>(){
                        public int compare(WalletItemModel item1, WalletItemModel item2){
                            BigInteger big1 = new BigInteger(item1.getBalance().replace(".",""));
                            BigInteger big2 = new BigInteger(item2.getBalance().replace(".",""));
                            return big2.compareTo(big1);
                        }
                    });
                    itemControllerList.sort(new Comparator<WalletListBodyController>(){
                        public int compare(WalletListBodyController item1, WalletListBodyController item2){
                            BigInteger big1 = new BigInteger(item1.getModel().getBalance().replace(".",""));
                            BigInteger big2 = new BigInteger(item2.getModel().getBalance().replace(".",""));
                            return big2.compareTo(big1);
                        }
                    });
                    break;
            }

        }
    }

    public interface WalletListEvent{
        void onChangeCheck(WalletItemModel model, boolean isChecked);
    }
}
