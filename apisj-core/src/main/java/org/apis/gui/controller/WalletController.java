package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.image.ImageView;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.WalletModel;
import org.apis.keystore.KeyStoreDataExp;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class WalletController  implements Initializable {

    @FXML
    private Label totalAssetLabel1, totalAssetLabel2;
    @FXML
    private Pane totalAssetLinePane1, totalAssetLinePane2;

    @FXML
    private Label walletListLabel1, walletListLabel2;
    @FXML
    private Pane walletListLinePane1, walletListLinePane2;

    @FXML
    private ImageView btnChangeNameWallet, btnChangePasswordWallet, btnBackupWallet, btnRemoveWallet;
    @FXML
    private ImageView tooltip1, tooltip2, tooltip3, tooltip4;

    @FXML
    private Label totalMainNatureLabel, totalMainDecimalLabel, totalSubNatureLabel, totalSubDecimalLabel;
    @FXML
    private ImageView sortNameImg, sortAmountImg;

    @FXML
    private WalletListController walletListBodyController;

    private ArrayList<Label> totalAssetLabels = new ArrayList<>();
    private ArrayList<Pane> totalAssetLines = new ArrayList<>();
    private ArrayList<Label> walletListLabels = new ArrayList<>();
    private ArrayList<Pane> walletListLines = new ArrayList<>();
    private ArrayList<ImageView> tooltips = new ArrayList<>();
    private ArrayList<WalletItemModel> walletListModels = new ArrayList<>();
    private ArrayList<WalletItemModel> walletCheckList = new ArrayList<>();

    private Image imageChangeName, imageChangeNameHover;
    private Image imageChangePassword, imageChangePasswordHover;
    private Image imageBakcup, imageBakcupHover;
    private Image imageRemove, imageRemoveHover;
    private Image imageSortAsc, imageSortDesc, imageSortNone;

    private WalletModel walletModel = new WalletModel();

    private int walletListSortType = WalletListController.SORT_ALIAS_ASC;




    public WalletController(){
        AppManager.getInstance().guiFx.setWallet(this);
    }


    public void initImageLoad(){

        this.imageChangeName = new Image("image/btn_wright@2x.png");
        this.imageChangeNameHover = new Image("image/btn_wright_hover@2x.png");
        this.imageChangePassword = new Image("image/btn_unlock@2x.png");
        this.imageChangePasswordHover = new Image("image/btn_unlock_hover@2x.png");
        this.imageBakcup = new Image("image/btn_share@2x.png");
        this.imageBakcupHover = new Image("image/btn_share_hover@2x.png");
        this.imageRemove = new Image("image/btn_remove@2x.png");
        this.imageRemoveHover = new Image("image/btn_remove_hover@2x.png");

        this.imageSortNone = new Image("image/ic_sort_none@2x.png");
        this.imageSortAsc = new Image("image/ic_sort_up@2x.png");
        this.imageSortDesc = new Image("image/ic_sort_down@2x.png");
    }

    public void initLayoutTotalAsset(){
        this.totalMainNatureLabel.textProperty().bind(this.walletModel.totalMainNaturalProperty());
        this.totalMainDecimalLabel.textProperty().bind(this.walletModel.totalMainDecimalProperty());
        this.totalSubNatureLabel.textProperty().bind(this.walletModel.totalSubNaturalProperty());
        this.totalSubDecimalLabel.textProperty().bind(this.walletModel.totalSubDecimalProperty());
    }

    public void initLayoutTotalAssetTab(){
        this.totalAssetLabels.add(this.totalAssetLabel1);
        this.totalAssetLabels.add(this.totalAssetLabel2);

        this.totalAssetLines.add(this.totalAssetLinePane1);
        this.totalAssetLines.add(this.totalAssetLinePane2);

        this.tooltips.add(tooltip1);
        this.tooltips.add(tooltip2);
        this.tooltips.add(tooltip3);
        this.tooltips.add(tooltip4);

        //PasswordField passwordField;
        //passwordField.set
    }

    public void setTotalAssetTabActive(int index){

        for(int i=0;i<this.totalAssetLabels.size(); i++){
            this.totalAssetLabels.get(i).setTextFill(Color.web("#999999"));
            this.totalAssetLabels.get(i).setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        }
        for(int i=0;i<this.totalAssetLines.size(); i++){
            this.totalAssetLines.get(i).setVisible(false);
        }

        if(index >= 0 && index < this.totalAssetLabels.size()){
            this.totalAssetLabels.get(index).setTextFill(Color.web("#910000"));
            this.totalAssetLabels.get(index).setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
        if(index >= 0 && index < this.totalAssetLines.size()){
            this.totalAssetLines.get(index).setVisible(true);
        }
    }

    public void selectedTotalAssetTab(int index){

        // change header active
        setTotalAssetTabActive(index);
    }


    public void initLayoutWalletListTab(){
        this.walletListLabels.add(this.walletListLabel1);
        this.walletListLabels.add(this.walletListLabel2);

        this.walletListLines.add(this.walletListLinePane1);
        this.walletListLines.add(this.walletListLinePane2);


    }

    public void setWalletListTabActive(int index){

        for(int i=0;i<this.walletListLabels.size(); i++){
            this.walletListLabels.get(i).setTextFill(Color.web("#999999"));
            this.walletListLabels.get(i).setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        }
        for(int i=0;i<this.walletListLines.size(); i++){
            this.walletListLines.get(i).setVisible(false);
        }

        if(index >= 0 && index < this.walletListLabels.size()){
            this.walletListLabels.get(index).setTextFill(Color.web("#910000"));
            this.walletListLabels.get(index).setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        }
        if(index >= 0 && index < this.walletListLines.size()){
            this.walletListLines.get(index).setVisible(true);
        }
    }

    public void reload(){
        walletListModels = new ArrayList<>();
        walletCheckList = new ArrayList<>();
        initWalletList();
    }

    public void selectedWalletListTab(int index){

        // change header active
        setWalletListTabActive(index);
    }

    public void hideToolTipAll(){
        for(int i=0; i<this.tooltips.size(); i++){
            this.tooltips.get(i).setVisible(false);
        }
    }

    public void showToolGroup(){
        this.btnChangeNameWallet.setVisible(true);
        this.btnChangePasswordWallet.setVisible(true);
        this.btnBackupWallet.setVisible(true);
        this.btnRemoveWallet.setVisible(true);
    }
    public void showToolRemove(){
        this.btnChangeNameWallet.setVisible(false);
        this.btnChangePasswordWallet.setVisible(false);
        this.btnBackupWallet.setVisible(false);
        this.btnRemoveWallet.setVisible(true);
    }
    public void hideToolGroup(){
        this.btnChangeNameWallet.setVisible(false);
        this.btnChangePasswordWallet.setVisible(false);
        this.btnBackupWallet.setVisible(false);
        this.btnRemoveWallet.setVisible(false);
    }

    public void initWalletList(){
        WalletItemModel walletItemModel = null;
        BigInteger bigTotalApis = new BigInteger("0");
        BigInteger bigTotalMineral = new BigInteger("0");
        String id, apis, mineral, alias;
        String[] apisSplit, mineralSplit;
        boolean isFirst = (walletListModels.size() == 0);

        if(isFirst){
            AppManager.getInstance().keystoreFileReadAll();
            walletListBodyController.removeWalletListItemAll();
        }
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

            id = (dataExp.id != null) ? dataExp.id : "";
            apis = (dataExp.balance != null) ? dataExp.balance : "0";
            mineral = (dataExp.mineral != null) ? dataExp.mineral : "0";
            alias = (dataExp.alias != null)? dataExp.alias : "Wallet Alias";

            bigTotalApis = bigTotalApis.add(new BigInteger(apis));
            bigTotalMineral = bigTotalMineral.add(new BigInteger(mineral));
            apisSplit = AppManager.addDotWidthIndex(apis).split("\\.");
            mineralSplit = AppManager.addDotWidthIndex(mineral).split("\\.");

            if (isFirst) {
                walletItemModel = new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS);
                walletListModels.add(walletItemModel);
                walletListBodyController.addCreateWalletListItem(walletListModels.get(i));
            } else {
                if(walletListModels.size() > i) {
                    walletItemModel = walletListModels.get(i);
                }
            }

            if(walletItemModel != null) {
                walletItemModel.setId(id);
                walletItemModel.setAlias(alias);
                walletItemModel.setAddress(dataExp.address);
                walletItemModel.setBalance(apis);
                walletItemModel.setMineral(mineral);
                walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
            }
        }

        apisSplit = AppManager.addDotWidthIndex(bigTotalApis.toString()).split("\\.");
        mineralSplit = AppManager.addDotWidthIndex(bigTotalMineral.toString()).split("\\.");
        walletModel.setTotalType(WalletModel.UNIT_TYPE_APIS);
        walletModel.setTotalApisNatural(apisSplit[0]);
        walletModel.setTotalApisDecimal("."+apisSplit[1]);
        walletModel.setTotalMineralNatural(mineralSplit[0]);
        walletModel.setTotalMineralDecimal("."+mineralSplit[1]);

        walletListSort(walletListSortType);
    }

    public void walletListSort(int sortType){
        this.walletListSortType = sortType;
        walletListBodyController.sort(sortType);

        this.sortNameImg.setImage(imageSortNone);
        this.sortAmountImg.setImage(imageSortNone);

        if(sortType == WalletListController.SORT_ALIAS_ASC){
            this.sortNameImg.setImage(imageSortAsc);

        }else if(sortType == WalletListController.SORT_ALIAS_DESC){
            this.sortNameImg.setImage(imageSortDesc);

        }else if(sortType == WalletListController.SORT_BALANCE_ASC){
            this.sortAmountImg.setImage(imageSortAsc);

        }else if(sortType == WalletListController.SORT_BALANCE_DESC){
            this.sortAmountImg.setImage(imageSortDesc);

        }
    }

    public void onMouseClickedNameSort(){
        if(this.walletListSortType != WalletListController.SORT_ALIAS_ASC){
            this.walletListSortType = WalletListController.SORT_ALIAS_ASC;
        }else if(this.walletListSortType != WalletListController.SORT_ALIAS_DESC){
            this.walletListSortType = WalletListController.SORT_ALIAS_DESC;
        }
        walletListSort(this.walletListSortType);
    }

    public void onMouseClickedAmountSort(){
        if(this.walletListSortType != WalletListController.SORT_BALANCE_ASC){
            this.walletListSortType = WalletListController.SORT_BALANCE_ASC;
        }else if(this.walletListSortType != WalletListController.SORT_BALANCE_DESC){
            this.walletListSortType = WalletListController.SORT_BALANCE_DESC;
        }
        walletListSort(this.walletListSortType);
    }

    @FXML
    private void onClickTabEvent(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("totalAssetTab1")) {
            selectedTotalAssetTab(0);
        }else if(id.equals("totalAssetTab2")) {
            selectedTotalAssetTab(1);
        }else if(id.equals("totalAssetTab3")) {
            selectedTotalAssetTab(2);
        }

        if(id.equals("walletListTab1")) {
            selectedWalletListTab(0);
        }else if(id.equals("walletListTab2")) {
            selectedWalletListTab(1);
        }
    }

    @FXML
    private void onMouseEntered(InputEvent event){
        hideToolTipAll();
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnChangeNameWallet")) {
            btnChangeNameWallet.setImage(imageChangeNameHover);
            this.tooltips.get(0).setVisible(true);
        }else if(id.equals("btnChangePasswordWallet")) {
            btnChangePasswordWallet.setImage(imageChangePasswordHover);
            this.tooltips.get(1).setVisible(true);
        }else if(id.equals("btnBackupWallet")) {
            btnBackupWallet.setImage(imageBakcupHover);
            this.tooltips.get(2).setVisible(true);
        }else if(id.equals("btnRemoveWallet")) {
            btnRemoveWallet.setImage(imageRemoveHover);
            this.tooltips.get(3).setVisible(true);
        }

    }
    @FXML
    private void onMouseExited(InputEvent event){
        hideToolTipAll();
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnChangeNameWallet")) {
            btnChangeNameWallet.setImage(imageChangeName);
        }else if(id.equals("btnChangePasswordWallet")) {
            btnChangePasswordWallet.setImage(imageChangePassword);
        }else if(id.equals("btnBackupWallet")) {
            btnBackupWallet.setImage(imageBakcup);
        }else if(id.equals("btnRemoveWallet")) {
            btnRemoveWallet.setImage(imageRemove);
        }


    }
    @FXML
    private void onClickEventWalletTool(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnChangeNameWallet")) {
            PopupChangeWalletNameController controller = (PopupChangeWalletNameController)AppManager.getInstance().guiFx.showMainPopup("popup_change_wallet_name.fxml", 0);
            controller.setModel(walletCheckList.get(0));
        }else if(id.equals("btnChangePasswordWallet")) {
            PopupChangePasswordController controller = (PopupChangePasswordController)AppManager.getInstance().guiFx.showMainPopup("popup_change_wallet_password.fxml", 0);
            controller.setModel(walletCheckList.get(0));
        }else if(id.equals("btnBackupWallet")) {
            PopupBackupWalletPasswordController controller = (PopupBackupWalletPasswordController)AppManager.getInstance().guiFx.showMainPopup("popup_backup_wallet_password.fxml", 0);
            controller.setModel(walletCheckList.get(0));

        }else if(id.equals("btnRemoveWallet")) {
            PopupRemoveWalletController controller = (PopupRemoveWalletController)AppManager.getInstance().guiFx.showMainPopup("popup_remove_wallet.fxml", 0);

            ArrayList<String> removeWalletId = new ArrayList<>();
            for(int i=0; i<walletCheckList.size(); i++){
                removeWalletId.add(walletCheckList.get(i).getId());
            }
            controller.removeList(removeWalletId);
        }else if(id.equals("btnMiningWallet")){
            AppManager.getInstance().guiFx.showMainPopup("popup_mining_wallet.fxml", 0);
        }else if(id.equals("btnCreateWallet")){
            AppManager.getInstance().guiFx.pageMoveIntro(true);
        }
    }

    public void update(){
        initWalletList();

        // 지갑 리스트 체크한 개수
        int checkSize = walletCheckList.size();
        if(checkSize == 0){
            hideToolGroup();
        }else if(checkSize == 1){
            showToolGroup();
        }else {
            showToolRemove();
        }
    }

    // 지갑리스트의 선택 목록을 초기화 한다.
    public void removeWalletCheckList(){
        this.walletCheckList.clear();
        hideToolGroup();
        walletListBodyController.unCheckAll();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // init image loading
        initImageLoad();

        // init tabs
        initLayoutTotalAssetTab();
        initLayoutWalletListTab();

        // init top total asset
        initLayoutTotalAsset();

        // init wallet list model
        initWalletList();

        // select tab
        selectedTotalAssetTab(0);
        selectedWalletListTab(0);

        walletModel.setTotalType(WalletModel.UNIT_TYPE_APIS);
        walletListBodyController.setHandler(new WalletListController.WalletListEvent() {
            @Override
            public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                if(isChecked){
                    boolean has = false;
                    for(int i=0; i<walletCheckList.size(); i++){
                        if(has = (walletCheckList.get(i) == model)){
                            break;
                        }
                    }
                    if(has == false){
                        walletCheckList.add(model);
                    }
                }else{
                    walletCheckList.remove(model);
                }

                // 지갑 리스트 체크한 개수
                int checkSize = walletCheckList.size();
                if(checkSize == 0){
                    hideToolGroup();
                }else if(checkSize == 1){
                    showToolGroup();
                }else {
                    showToolRemove();
                }
            }
        });
        walletListBodyController.setOpenItem(0);

        // 지갑리스트 툴팁 숨기기
        hideToolGroup();
    }
}
