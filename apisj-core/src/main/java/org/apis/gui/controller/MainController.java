package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.AlertItemController;
import org.apis.gui.controller.popup.PopupRestartController;
import org.apis.gui.controller.popup.PopupSyncController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.NotificationManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.MainModel;
import org.apis.gui.model.TokenModel;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController extends BaseViewController {

    @FXML private Label label1, label2, label3, label4, label5;
    @FXML private Pane linePane1, linePane2, linePane3, linePane4, linePane5;
    @FXML private TabPane tabPane;
    @FXML private GridPane popupLayout0, popupLayout1, popupLayout2, popupLayout3;
    @FXML private Label totalNatural, totalUnit, peer, block, timestemp;
    @FXML private ComboBox selectLanguage, footerSelectTotalUnit;
    @FXML private ImageView btnAlert, btnSetting;
    @FXML private AnchorPane alertPane;
    @FXML private VBox alertList;
    @FXML private Label mainFooterTotal, mainFooterPeers, mainFooterTimer;

    private MainTab selectedIndex = MainTab.WALLET;
    private Image imageAlert, imageAlertHover, imageAlertRed, imageAlertRedHover, imageSetting, imageSettingHover;
    private String cursorPane;
    private ArrayList<Label> labels = new ArrayList<>();
    private ArrayList<Pane> lines = new ArrayList<>();

    private MainModel mainModel = new MainModel();
    private PopupSyncController syncController;

    // 이전 마이닝/마스터노드 참여(혹은 시도) 하던 지갑주소
    String miningAddress = AppManager.getGeneralPropertiesData("mining_address");
    String masternodeAddress = AppManager.getGeneralPropertiesData("masternode_address");

    public MainController(){ }

    public void initLayoutHeader(){
        this.labels.add(this.label1);
        this.labels.add(this.label2);
        this.labels.add(this.label3);
        this.labels.add(this.label4);
        this.labels.add(this.label5);

        this.lines.add(this.linePane1);
        this.lines.add(this.linePane2);
        this.lines.add(this.linePane3);
        this.lines.add(this.linePane4);
        this.lines.add(this.linePane5);
    }
    public void initLayoutFooter(){
        this.totalUnit.setText("APIS");
        this.peer.textProperty().bind(mainModel.peerProperty());
        this.block.textProperty().bind(mainModel.blockProperty());
        this.timestemp.textProperty().bind(mainModel.timestempProperty());

        ObservableList<String> langOptions = FXCollections.observableArrayList( "eng", "kor");
        selectLanguage.setItems(langOptions);
        selectLanguage.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue.equals("eng")){
                    AppManager.saveGeneralProperties("language", "eng");
                    StringManager.getInstance().changeBundleEng();
                }else if(newValue.equals("kor")){
                    AppManager.saveGeneralProperties("language", "kor");
                    StringManager.getInstance().changeBundleKor();
                }
            }
        });
        selectLanguage.setValue(AppManager.getGeneralPropertiesData("language"));

        ObservableList<FooterTotalModel> footOptions = FXCollections.observableArrayList();
        footOptions.add(new FooterTotalModel("-1", "APIS"));
        footOptions.add(new FooterTotalModel("-2", "MINERAL"));

        for(TokenModel token : AppManager.getInstance().getTokens()){
            footOptions.add(new FooterTotalModel(token.getTokenAddress(), token.getTokenName()));
        }

        footerSelectTotalUnit.setItems(footOptions);
        footerSelectTotalUnit.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                FooterTotalModel data = (FooterTotalModel)newValue;
                setfooterTotalData(data);

            }
        });
    }

    public void setHeaderActive(MainTab index){

        for(int i=0;i<this.labels.size(); i++){
            this.labels.get(i).setTextFill(Color.web("#999999"));
            this.labels.get(i).setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        }
        for(int i=0;i<this.lines.size(); i++){
            this.lines.get(i).setVisible(false);
        }

        if(index.num >= 0 && index.num < this.labels.size()){
            this.labels.get(index.num).setTextFill(Color.web("#910000"));
            this.labels.get(index.num).setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
        if(index.num >= 0 && index.num < this.lines.size()){
            this.lines.get(index.num).setVisible(true);
        }
    }
    public void selectedHeader(MainTab index){
        this.selectedIndex = index;

        // change header active
        setHeaderActive(this.selectedIndex);

        // change tab pane
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(this.selectedIndex.num);

        switch (this.selectedIndex){
            case WALLET : AppManager.getInstance().guiFx.getWallet().update(); break;
            case TRANSFER : AppManager.getInstance().guiFx.getTransfer().update(); break;
            case SMART_CONTRECT : AppManager.getInstance().guiFx.getSmartContract().update(); break;
            case ADDRESS_MASKING : AppManager.getInstance().guiFx.getAddressMasking().update(); break;
            case TRANSACTION :
                AppManager.getInstance().guiFx.getTransactionNative().hideDetail();
                AppManager.getInstance().guiFx.getTransactionNative().update();
                break;
        }
    }
    public void setfooterTotalData(FooterTotalModel data){
        if(data == null){
            totalNatural.setText("0.00000000");
            totalUnit.setText("APIS");
        }else {
            if (data.tokenAddress.equals("-1")) {
                totalNatural.setText(ApisUtil.readableApis(AppManager.getInstance().getTotalApis(), ',', true));
                totalUnit.setText("APIS");
            } else if (data.tokenAddress.equals("-2")) {
                totalNatural.setText(ApisUtil.readableApis(AppManager.getInstance().getTotalMineral(), ',', true));
                totalUnit.setText("MNR");
            } else {
                totalNatural.setText(ApisUtil.readableApis(AppManager.getInstance().getTotalTokenValue(data.tokenAddress), ',', true));
                totalUnit.setText(AppManager.getInstance().getTokenSymbol(data.tokenAddress));
            }
        }
    }

    public void setBlock(long myBestBlock, long worldBestBlock){
        mainModel.setBlock(myBestBlock, worldBestBlock);
    }

    public void setTimestemp(long timeStemp, long nowStemp){
        mainModel.setTimestemp(timeStemp, nowStemp);
    }

    public void setPeer(long peer){
        mainModel.setPeer(""+peer);
    }

    public void init(){
        int size = AppManager.getInstance().getKeystoreList().size();
        if (size <= 0) {

        }

        if(AppManager.getInstance().isSyncDone()){

        }else{
           syncController = (PopupSyncController)PopupManager.getInstance().showMainPopup("popup_sync.fxml", 0);
        }
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(alertPane.isVisible()) {
            alertPane.setVisible(false);
            alertList.getChildren().clear();
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        cursorPane = id;
        if(id.equals("btnAlert")){
            if(NotificationManager.getInstance().getSize() > 0){
                btnAlert.setImage(imageAlertRedHover);
            }else{
                btnAlert.setImage(imageAlertHover);
            }
        }else if(id.equals("btnSetting")){
            btnSetting.setImage(imageSettingHover);
        }
    }
    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        cursorPane = null;
        if(id.equals("btnAlert")){
            if(NotificationManager.getInstance().getSize() > 0){
                btnAlert.setImage(imageAlertRed);
            }else{
                btnAlert.setImage(imageAlert);
            }
        }else if(id.equals("btnSetting")){
            btnSetting.setImage(imageSetting);
        }
    }
    @FXML
    public void onMouseClickedAlert(InputEvent event){
        alertPane.setVisible(!alertPane.isVisible());
        alertList.getChildren().clear();
        if(alertPane.isVisible()) {
            for (int i = 0; i < NotificationManager.getInstance().getSize(); i++) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("scene/module/alert_item.fxml"));
                    alertList.getChildren().add(loader.load());
                    AlertItemController alertItemController = (AlertItemController) loader.getController();
                    alertItemController.setModel(NotificationManager.getInstance().getList().get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        event.consume();
    }
    public void onMouseClickedSetting(){
        PopupManager.getInstance().showMainPopup("setting.fxml", -1);
    }

    @FXML
    private void onClickTabEvent(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("tab1")) {
            selectedHeader(MainTab.WALLET);
        }else if(id.equals("tab2")) {
            selectedHeader(MainTab.TRANSFER);
        }else if(id.equals("tab3")) {
            selectedHeader(MainTab.SMART_CONTRECT);
        }else if(id.equals("tab4")) {
            selectedHeader(MainTab.ADDRESS_MASKING);
        }else if(id.equals("tab5")) {
            selectedHeader(MainTab.TRANSACTION);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setMain(this);

        // 언어 설정
        languageSetting();

        this.imageAlert = new Image("image/btn_alert@2x.png");
        this.imageAlertHover = new Image("image/btn_alert_hover@2x.png");
        this.imageAlertRed = new Image("image/btn_alert_red@2x.png");
        this.imageAlertRedHover = new Image("image/btn_alert_red_hover@2x.png");
        this.imageSetting = new Image("image/btn_setting@2x.png");
        this.imageSettingHover = new Image("image/btn_setting_hover@2x.png");

        this.tabPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP
                        || event.getCode() == KeyCode.DOWN
                        || event.getCode() == KeyCode.LEFT
                        || event.getCode() == KeyCode.RIGHT) {

                    if(MainController.this.tabPane.isFocused()){
                        event.consume();
                    }else{
                    }
                }
            }
        });

        initLayoutHeader();
        initLayoutFooter();

        selectedHeader(MainTab.WALLET);

        PopupManager.getInstance().setMainPopup0(popupLayout0);
        PopupManager.getInstance().setMainPopup1(popupLayout1);
        PopupManager.getInstance().setMainPopup2(popupLayout2);
        PopupManager.getInstance().setMainPopup3(popupLayout3);

        init();
    }
    public void languageSetting() {
        this.label1.textProperty().bind(StringManager.getInstance().main.tabWallet);
        this.label2.textProperty().bind(StringManager.getInstance().main.tabTransfer);
        this.label3.textProperty().bind(StringManager.getInstance().main.tabSmartContract);
        this.label4.textProperty().bind(StringManager.getInstance().main.tabAddressMasking);
        this.label5.textProperty().bind(StringManager.getInstance().main.tabTransaction);
        this.mainFooterTotal.textProperty().bind(StringManager.getInstance().main.footerTotal);
        this.mainFooterPeers.textProperty().bind(StringManager.getInstance().main.footerPeers);
        this.mainFooterTimer.textProperty().bind(StringManager.getInstance().main.footerTimer);
    }

    @Override
    public void update(){
        if(footerSelectTotalUnit.getValue() == null) {
            footerSelectTotalUnit.getSelectionModel().select(0);
        }else{
            footerSelectTotalUnit.setValue(footerSelectTotalUnit.getValue());
        }
    }

    public void succesSync(){
        if((this.miningAddress != null && this.miningAddress.length() > 0)
                || (this.masternodeAddress != null && this.masternodeAddress.length() > 0)){
            String masterNodeAlias = "";
            String masterNodeAddress = "";
            String miningAlias = "";
            String miningAddress = "";
            for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++){
                if(AppManager.getInstance().getKeystoreExpList().get(i).address.equals(this.miningAddress)){
                    miningAlias = AppManager.getInstance().getKeystoreExpList().get(i).alias;
                    miningAddress = this.miningAddress;

                    this.miningAddress = null;
                    AppManager.saveGeneralProperties("mining_address","");

                }else if(AppManager.getInstance().getKeystoreExpList().get(i).address.equals(this.masternodeAddress)){
                    masterNodeAlias = AppManager.getInstance().getKeystoreExpList().get(i).alias;
                    masterNodeAddress = this.masternodeAddress;

                    this.masternodeAddress = null;
                    AppManager.saveGeneralProperties("masternode_address","");

                }
            }

            PopupRestartController controller = (PopupRestartController)PopupManager.getInstance().showMainPopup("popup_restart.fxml", 0);
            controller.setData(masterNodeAlias, masterNodeAddress, miningAlias, miningAddress);

        }
    }

    public void exitSyncPopup(){
        if(this.syncController != null){
            this.syncController.exit();
            this.syncController = null;
        }
    }

    public void syncSubMessage(long lastBlock, long bestBlock){
        if(this.syncController != null) {
            this.syncController.setSubMessage(lastBlock, bestBlock);
        }
    }

    public MainTab getSelectedIndex(){
        return this.selectedIndex;
    }


    class FooterTotalModel {
        public String tokenAddress;
        public String tokenName;

        public FooterTotalModel(String tokenAddress, String tokenName){
            this.tokenAddress = tokenAddress;
            this.tokenName = tokenName;
        }

        @Override
        public String toString(){
            return tokenName;
        }
    }


    public enum MainTab {
        /* wallet(main) */ WALLET(0),
        /* transfer */ TRANSFER(1),
        /* smart contract */ SMART_CONTRECT(2),
        /* address masking */ ADDRESS_MASKING(3),
        /* transaction */ TRANSACTION(4);
        int num;
        MainTab(int num) {
            this.num = num;
        }
    }
}
