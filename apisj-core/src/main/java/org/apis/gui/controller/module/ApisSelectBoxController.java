package org.apis.gui.controller.module;

import com.google.zxing.WriterException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseSelectBoxHeaderController;
import org.apis.gui.controller.base.BaseSelectBoxItemController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.keystore.KeyStoreDataExp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ApisSelectBoxController extends BaseViewController {
    public static final int SELECT_BOX_TYPE_ALIAS = 0;
    public static final int SELECT_BOX_TYPE_ADDRESS = 1;
    public static final int SELECT_BOX_TYPE_DOMAIN = 2;
    public static final int SELECT_BOX_TYPE_ONLY_ADDRESS = 3;
    private int selectBoxType = SELECT_BOX_TYPE_ALIAS;

    public static final int STAGE_DEFAULT = 0;
    public static final int STAGE_SELECTED = 1;


    private BaseFxmlController headerFxml;
    private ArrayList<BaseFxmlController> itemFxmlList = new ArrayList<>();
    private SelectBoxItemModel selectedItemModel = null;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private VBox childPane, itemList;
    @FXML
    private GridPane header;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void onMouseClicked(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("header")){
            toggleItemListVisible();

            if(handler != null){
                handler.onMouseClick();
            }
        }
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        onStateDefault();
        setVisibleItemList(false);

        setStage(STAGE_DEFAULT);

        rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setVisibleItemList(false);
            }
        });
    }

    public void init(int boxType){
        this.selectBoxType = boxType;

        setSelectBoxType(this.selectBoxType);

        // 드롭아이템 리스트 초기화
        // 내 지갑 목록이 필요한 타입 = { SELECT_BOX_TYPE_ALIAS, SELECT_BOX_TYPE_ADDRESS, SELECT_BOX_TYPE_ONLY_ADDRESS }
        // 도메인 목록이 필요한 타입 = { SELECT_BOX_TYPE_DOMAIN }
        switch (this.selectBoxType){
            case SELECT_BOX_TYPE_ALIAS :
            case SELECT_BOX_TYPE_ADDRESS :
            case SELECT_BOX_TYPE_ONLY_ADDRESS :
                for (int i = 0; i < AppManager.getInstance().getKeystoreExpList().size(); i++) {
                    KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

                    String address = dataExp.address;
                    String alias = dataExp.alias;
                    String mask = dataExp.mask;
                    SelectBoxItemModel model = new SelectBoxItemModel();
                    model.addressProperty().setValue(address);
                    model.aliasProperty().setValue(alias);
                    model.maskProperty().setValue(mask);
                    model.setKeystoreId(AppManager.getInstance().getKeystoreExpList().get(i).id);
                    model.setBalance(AppManager.getInstance().getKeystoreExpList().get(i).balance);
                    model.setMineral(AppManager.getInstance().getKeystoreExpList().get(i).mineral);
                    try {
                        model.setIdenticon(IdenticonGenerator.generateIdenticonsToImage(address,128,128));
                    } catch (WriterException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // 가지고 있는 모델일 경우 업데이트
                    // 가지고 있지 않는 모델일 경우 추가
                    boolean hasModel = false;
                    for(int j=0; j<itemFxmlList.size(); j++){
                        if(((SelectBoxItemModel)itemFxmlList.get(j).getController().getModel()).getAddress().equals(model.getAddress())){
                            updateItem(j, model);
                            hasModel = true;
                        }
                    }
                    if(!hasModel){
                        addItem(this.selectBoxType, model);
                    }

                }
                if(selectedItemModel == null){
                    selectedItemModel = (SelectBoxItemModel)itemFxmlList.get(0).getController().getModel();
                }
                setHeader(this.selectBoxType, selectedItemModel);
                break;
            case SELECT_BOX_TYPE_DOMAIN :

                // 도메인 리스트 등록
                String[] domainName ={"me", "ico", "shop", "com", "org", "info", "biz", "net", "edu", "team", "pro", "xxx", "xyz", "cat", "dog", "exchange", "dapp", "firm"};
                for(int i=0; i<domainName.length ; i++){

                    SelectBoxItemModel model = new SelectBoxItemModel().setDomainId(""+i).setDomain("@"+domainName[i]).setApis("10");
                    // 가지고 있는 모델일 경우 업데이트
                    // 가지고 있지 않는 모델일 경우 추가
                    boolean hasModel = false;
                    for(int j=0; j<itemFxmlList.size(); j++){
                        if(((SelectBoxItemModel)itemFxmlList.get(j).getController().getModel()).getDomain().equals(model.getDomain())){
                            updateItem(j, model);
                            hasModel = true;
                        }
                    }
                    if(!hasModel){
                        addItem(this.selectBoxType, model);
                    }
                }

                if(selectedItemModel == null){
                    selectedItemModel = (SelectBoxItemModel)itemFxmlList.get(0).getController().getModel();
                }
                setHeader(this.selectBoxType, selectedItemModel);
                break;
        }

    }

    public void update(){
        init(this.selectBoxType);
        if(selectedItemModel != null) {
            for(int i=0; i<itemFxmlList.size(); i++){
                if(((SelectBoxItemModel)itemFxmlList.get(i).getController().getModel()).getKeystoreId().equals(selectedItemModel.getKeystoreId())){
                    selectedItemModel = (SelectBoxItemModel)itemFxmlList.get(i).getController().getModel();
                    setHeader(this.selectBoxType, selectedItemModel);
                    break;
                }
            }
        }else{
            setHeader(this.selectBoxType, (SelectBoxItemModel)itemFxmlList.get(0).getController().getModel());
        }
    }

    public void onStateDefault(){
        String style = "";

        if(this.selectBoxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
            style = style + "-fx-border-radius : 0 0 0 0; -fx-background-radius: 0 0 0 0; ";
            style = style + "-fx-border-color: transparent; ";
            style = style + "-fx-background-color: transparent; ";
        }else{
            style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
            style = style + "-fx-border-color: #d8d8d8; ";
            style = style + "-fx-background-color: #f2f2f2; ";
        }
        header.setStyle(style);
    }

    public void onStateActive(){
        String style = "";

        if(this.selectBoxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
            style = style + "-fx-border-radius : 0 0 0 0; -fx-background-radius: 0 0 0 0; ";
            style = style + "-fx-border-color: transparent; ";
            style = style + "-fx-background-color: transparent; ";
        }else{
            style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
            style = style + "-fx-border-color: #999999; ";
            style = style + "-fx-background-color: #ffffff; ";
        }

        header.setStyle(style);
    }

    public void setSelectBoxType(int boxType){
        if(this.selectBoxType == SELECT_BOX_TYPE_ALIAS){
            this.scrollPane.maxHeightProperty().setValue(170);
        }else if(this.selectBoxType == SELECT_BOX_TYPE_ADDRESS){
            this.scrollPane.maxHeightProperty().setValue(162);
        }else if(this.selectBoxType == SELECT_BOX_TYPE_DOMAIN){
            this.scrollPane.maxHeightProperty().setValue(162);
        }
    }
    public void setHeader(int boxType, SelectBoxItemModel model){
        try {

            String fxmlUrl = null;
            if(boxType == SELECT_BOX_TYPE_ALIAS){
                fxmlUrl = "module/apis_selectbox_head_alias.fxml";
            }else if(boxType == SELECT_BOX_TYPE_ADDRESS){
                fxmlUrl = "module/apis_selectbox_head_address.fxml";
            }else if(boxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
                fxmlUrl = "module/apis_selectbox_head_only_address.fxml";
            }else if(boxType == SELECT_BOX_TYPE_DOMAIN){
                fxmlUrl = "module/apis_selectbox_head_domain.fxml";
            }

            if(headerFxml == null) {
                headerFxml = new BaseFxmlController(fxmlUrl);
            }
            headerFxml.getController().setModel(model);

            header.getChildren().clear();
            header.add(headerFxml.getNode(),0,0);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void updateItem(int index, SelectBoxItemModel model){
        this.itemFxmlList.get(index).getController().setModel(model);
    }
    public void addItem(int boxType, SelectBoxItemModel model){
        try {
            String itemUrl = "module/apis_selectbox_item_alias.fxml";
            if(boxType == SELECT_BOX_TYPE_ALIAS){
                itemUrl = "module/apis_selectbox_item_alias.fxml";
            }else if(boxType == SELECT_BOX_TYPE_ADDRESS){
                itemUrl = "module/apis_selectbox_item_address.fxml";
            }else if(boxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
                itemUrl = "module/apis_selectbox_item_only_address.fxml";
            }else if(boxType == SELECT_BOX_TYPE_DOMAIN){
                itemUrl = "module/apis_selectbox_item_domain.fxml";
            }
            BaseFxmlController itemFxml = new BaseFxmlController(itemUrl);
            BaseSelectBoxItemController itemController = (BaseSelectBoxItemController)itemFxml.getController();
            itemController.setModel(model);
            itemController.setHandler(new BaseSelectBoxItemController.BaseSelectBoxItemImpl() {
                @Override
                public void onMouseClicked(SelectBoxItemModel itemModel) {
                    selectedItemModel = itemModel;

                    // change header data
                    headerFxml.getController().setModel(itemModel);

                    // hide item list
                    ApisSelectBoxController.this.setVisibleItemList(false);
                    setStage(STAGE_SELECTED);

                    if(handler != null){
                        handler.onSelectItem();
                    }
                }
            });
            itemList.getChildren().add(itemFxml.getNode());
            itemFxmlList.add(itemFxml);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setVisibleItemList(boolean isVisible){

        if(isVisible == true){
            this.rootPane.prefHeightProperty().setValue(-1);
            setStage(STAGE_DEFAULT);

        }else{
            if(this.selectBoxType == SELECT_BOX_TYPE_ALIAS){
                this.rootPane.prefHeightProperty().setValue(56);
            }else if(this.selectBoxType == SELECT_BOX_TYPE_ADDRESS){
                this.rootPane.prefHeightProperty().setValue(40);
            }else if(this.selectBoxType == SELECT_BOX_TYPE_DOMAIN){
                this.rootPane.prefHeightProperty().setValue(40);
            }else if(this.selectBoxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
                this.rootPane.prefHeightProperty().setValue(30);
            }
        }

        scrollPane.setVisible(isVisible);
    }

    public void setStage(int stage){
        if(stage == STAGE_SELECTED){
            if(selectBoxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
                String style = "-fx-background-color:transparent; -fx-border-color:transparent; ";
                style = style + "-fx-border-radius : 0 0 0 0; -fx-background-radius: 0 0 0 0; ";
                header.setStyle(style);
            }else{
                String style = "-fx-background-color:#ffffff; -fx-border-color:#999999; ";
                style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
                header.setStyle(style);
            }
        }else{
            if(selectBoxType == SELECT_BOX_TYPE_ONLY_ADDRESS){
                String style = "-fx-background-color:transparent; -fx-border-color:transparent; ";
                style = style + "-fx-border-radius : 0 0 0 0; -fx-background-radius: 0 0 0 0; ";
                header.setStyle(style);
            }else{
                String style = "-fx-background-color:#f2f2f2; -fx-border-color:#d8d8d8; ";
                style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
                header.setStyle(style);
            }
        }
    }

    public void selectedItemWithWalletId(String id) {
        for(int i=0; i<itemFxmlList.size(); i++){
            if(((SelectBoxItemModel)itemFxmlList.get(i).getController().getModel()).getKeystoreId().equals(id)){
                selectedItem(i);
                break;
            }
        }
    }

    public void selectedItemWithAddress(String address){
        for(int i=0; i<itemFxmlList.size(); i++){
            if(((SelectBoxItemModel)itemFxmlList.get(i).getController().getModel()).getAddress().equals(address)){
                selectedItem(i);
                break;
            }
        }
    }

    public void selectedItem(int i) {
        selectedItemModel = (SelectBoxItemModel) itemFxmlList.get(i).getController().getModel();
        headerFxml.getController().setModel(selectedItemModel);
    }

    public void toggleItemListVisible(){
        setVisibleItemList(!scrollPane.isVisible()); }

    public String getAddress(){
        return ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getAddress().trim();
    }

    public String getAlias(){
        return ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getAlias().trim();
    }

    public String getKeystoreId() {
        return ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getKeystoreId().trim();
    }

    public BigInteger getBalance() {
        return ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getBalance();
    }

    public BigInteger getMineral() {
        return  ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getMineral();
    }

    public String getDomain() {
        return  ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getDomain();
    }

    public String getValueApis(){
        return  ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getApis();
    }
    public BigInteger getValueApisToBigInt() {
        switch (this.selectBoxType){
            case SELECT_BOX_TYPE_DOMAIN : return  new BigInteger(getValueApis()).multiply(new BigInteger("1000000000000000000"));
        }
        return null;
    }
    public String getDomainId(){
        return  ((BaseSelectBoxHeaderController)this.headerFxml.getController()).getDomainId();
    }



    private ApisSelectBoxImpl handler;
    public void setHandler(ApisSelectBoxImpl handler) { this.handler = handler; }
    public interface ApisSelectBoxImpl{
        void onMouseClick();
        void onSelectItem();
    }
}
