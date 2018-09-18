package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.NotificationManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.MainModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final String TOTAL_UNIT_APIS = "APIS";
    private final String TOTAL_UNIT_MINERAL = "MINERAL";

    @FXML
    private Label label1, label2, label3, label4, label5;
    @FXML
    private Pane linePane1, linePane2, linePane3, linePane4, linePane5;
    @FXML
    private TabPane tabPane;
    @FXML
    private GridPane popupLayout0, popupLayout1, popupLayout2;
    @FXML
    private Label totalNatural, totalDecimal, totalUnit, peer, block, timestemp;
    @FXML
    private ComboBox selectLanguage, footerSelectTotalUnit;
    @FXML
    private ImageView btnAlert, btnSetting;
    private Image imageAlert, imageAlertHover, imageAlertRed, imageAlertRedHover, imageSetting, imageSettingHover;
    @FXML
    private AnchorPane alertPane;
    @FXML
    private VBox alertList;
    @FXML
    private Label mainFooterTotal, mainFooterPeers, mainFooterTimer;

    private String cursorPane;


    private ArrayList<Label> labels = new ArrayList<>();
    private ArrayList<Pane> lines = new ArrayList<>();

    private MainModel mainModel = new MainModel();
    private PopupSyncController syncController;

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
        this.totalNatural.textProperty().bind(mainModel.totalBalanceNaturalProperty());
        this.totalDecimal.textProperty().bind(mainModel.totalBalanceDecimalProperty());
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
                    StringManager.getInstance().changeBundleEng();
                }else if(newValue.equals("kor")){
                    StringManager.getInstance().changeBundleKor();
                }
            }
        });

        ObservableList<String> footOptions = FXCollections.observableArrayList( TOTAL_UNIT_APIS, TOTAL_UNIT_MINERAL );
        footerSelectTotalUnit.setItems(footOptions);
        footerSelectTotalUnit.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                totalNatural.textProperty().unbind();
                totalDecimal.textProperty().unbind();
                if(newValue.equals(TOTAL_UNIT_APIS)){
                    totalNatural.textProperty().bind(mainModel.totalBalanceNaturalProperty());
                    totalDecimal.textProperty().bind(mainModel.totalBalanceDecimalProperty());
                    totalUnit.setText("APIS");

                }else if(newValue.equals(TOTAL_UNIT_MINERAL)){
                    totalNatural.textProperty().bind(mainModel.totalMineralNaturalProperty());
                    totalDecimal.textProperty().bind(mainModel.totalMineralDecimalProperty());
                    totalUnit.setText("MNR");
                }
            }
        });
    }

    public void setHeaderActive(int index){

        for(int i=0;i<this.labels.size(); i++){
            this.labels.get(i).setTextFill(Color.web("#999999"));
            this.labels.get(i).setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        }
        for(int i=0;i<this.lines.size(); i++){
            this.lines.get(i).setVisible(false);
        }

        if(index >= 0 && index < this.labels.size()){
            this.labels.get(index).setTextFill(Color.web("#910000"));
            this.labels.get(index).setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
        if(index >= 0 && index < this.lines.size()){
            this.lines.get(index).setVisible(true);
        }

        if(index == 1){
            AppManager.getInstance().guiFx.getTransfer().init();
        }
    }
    public void selectedHeader(int index){

        // change header active
        setHeaderActive(index);

        // change tab pane
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(index);

        if(index == 0){
            // Wallet
            AppManager.getInstance().guiFx.getWallet().update(null);
        }else if(index == 1){
            // Transfer
            AppManager.getInstance().guiFx.getTransfer().update();
        }else if(index == 2){
            // SmartContract
            AppManager.getInstance().guiFx.getSmartContract().initTab(0);
            AppManager.getInstance().guiFx.getSmartContract().update();
        }else if(index == 3){
            // Transaction
            //AppManager.getInstance().guiFx.getTransaction().update();
            AppManager.getInstance().guiFx.getTransactionNative().init();
            AppManager.getInstance().guiFx.getTransactionNative().update();
        }else if(index == 4){
            // Address Masking
            AppManager.getInstance().guiFx.getAddressMasking().update();
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

    public void setTotalBalance(String balance){
        balance = AppManager.addDotWidthIndex(balance);
        String[] balanceSplit = balance.split("\\.");
        this.mainModel.totalBalanceNaturalProperty().setValue(balanceSplit[0]);
        this.mainModel.totalBalanceDecimalProperty().setValue("."+balanceSplit[1]);
    }

    public void setTotalMineral(String mineral){
        mineral = AppManager.addDotWidthIndex(mineral);
        String[] mineralSplit = mineral.split("\\.");
        this.mainModel.totalMineralNaturalProperty().setValue(mineralSplit[0]);
        this.mainModel.totalMineralDecimalProperty().setValue("."+mineralSplit[1]);
    }

    public void init(){
        int size = AppManager.getInstance().getKeystoreList().size();
        if (size <= 0) {

        }

        if(AppManager.getInstance().isSyncDone()){

        }else{
            syncController = (PopupSyncController)AppManager.getInstance().guiFx.showMainPopup("popup_sync.fxml", 0);
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
                    FXMLLoader loader = new FXMLLoader(new File("apisj-core/src/main/resources/scene/alert_item.fxml").toURI().toURL());
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
        AppManager.getInstance().guiFx.showMainPopup("setting.fxml", -1);
    }

    @FXML
    private void onClickTabEvent(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("tab1")) {
            selectedHeader(0);
        }else if(id.equals("tab2")) {
            selectedHeader(1);
        }else if(id.equals("tab3")) {
            selectedHeader(2);
        }else if(id.equals("tab4")) {
            selectedHeader(3);
        }else if(id.equals("tab5")) {
            selectedHeader(4);
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

        selectedHeader(0);

        AppManager.getInstance().guiFx.setMainPopup0(popupLayout0);
        AppManager.getInstance().guiFx.setMainPopup1(popupLayout1);
        AppManager.getInstance().guiFx.setMainPopup2(popupLayout2);

        init();
    }
    public void languageSetting() {
        this.label1.textProperty().bind(StringManager.getInstance().main.tabWallet);
        this.label2.textProperty().bind(StringManager.getInstance().main.tabTransfer);
        this.label3.textProperty().bind(StringManager.getInstance().main.tabSmartContract);
        this.label4.textProperty().bind(StringManager.getInstance().main.tabTransaction);
        this.label5.textProperty().bind(StringManager.getInstance().main.tabAddressMasking);
        this.mainFooterTotal.textProperty().bind(StringManager.getInstance().main.footerTotal);
        this.mainFooterPeers.textProperty().bind(StringManager.getInstance().main.footerPeers);
        this.mainFooterTimer.textProperty().bind(StringManager.getInstance().main.footerTimer);
    }

    public void update(String totalBalance, String totalMineral){
        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setTotalBalance(totalBalance);
        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setTotalMineral(totalMineral);
        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().exitSyncPopup();
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
}
