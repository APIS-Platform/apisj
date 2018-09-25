package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.image.ImageView;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.WalletModel;
import org.apis.keystore.KeyStoreDataExp;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WalletController  implements Initializable {

    @FXML private Label totalAssetLabel1, totalAssetLabel2;
    @FXML private Pane totalAssetLinePane1, totalAssetLinePane2;

    @FXML private Label walletListLabel1, walletListLabel2;
    @FXML private Pane walletListLinePane1, walletListLinePane2;
    @FXML private AnchorPane headerItem, headerGroupItem, toolMiningWallet, toolMasternode, stakingPane;

    @FXML private ImageView btnChangeNameWallet, btnChangePasswordWallet, btnBackupWallet, btnRemoveWallet, iconMiningWallet, iconMasternode;
    @FXML private Label btnMiningWallet, btnToken, btnCreateWallet, btnMasternode;
    @FXML private ImageView tooltip1, tooltip2, tooltip3, tooltip4, tooltipApis;

    @FXML private Label totalTitle, totalSubTitle, totalMainNatureLabel, totalMainDecimalLabel, totalMainUnitLabel, totalSubNatureLabel, totalSubDecimalLabel, totalSubUnitLabel;
    @FXML private ImageView sortNameImg, sortAmountImg;
    @FXML private ImageView sortNameImg1, sortAmountImg1;
    @FXML private TextField searchApisAndTokens;
    @FXML private Label rewaredLabel;

    @FXML private WalletListController walletListBodyController;

    @FXML Label totalAssetLabel, totalTransferLabel, myRewardsLabel, rewardedLabel, nowStakingLabel, howApisLabel,
            tableHeaderName, tableHeaderAddressMasking, tableHeaderAmount, tableHeaderTransfer,
            tableHeaderName2, tableHeaderAmount2, tableHeaderTransfer2
    ;


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
    private int unitTotalType = WalletModel.UNIT_TYPE_APIS;
    private int walletListTabIndex = 0 ;
    private WalletItemModel openWalletItemModel = null; //Wallet Tab에서 클릭되어 있는 지갑 정보
    private int openWalletItemIndex = 0;
    private String reward;


    public WalletController(){
        AppManager.getInstance().guiFx.setWallet(this);
    }


    public void languageSetting() {
        this.totalAssetLabel.textProperty().bind(StringManager.getInstance().wallet.totalAsset);
        this.totalTransferLabel.textProperty().bind(StringManager.getInstance().wallet.totalTransfer);
        this.myRewardsLabel.textProperty().bind(StringManager.getInstance().wallet.myRewards);
        this.nowStakingLabel.textProperty().bind(StringManager.getInstance().wallet.nowStaking);
        this.howApisLabel.textProperty().bind(StringManager.getInstance().wallet.howToGetRewardedWithApis);
        this.btnMiningWallet.textProperty().bind(StringManager.getInstance().wallet.miningButton);
        this.btnMasternode.textProperty().bind(StringManager.getInstance().wallet.masternodeButton);
        this.btnToken.textProperty().bind(StringManager.getInstance().wallet.tokenButton);
        this.btnCreateWallet.textProperty().bind(StringManager.getInstance().wallet.createButton);
        this.rewardedLabel.textProperty().bind(StringManager.getInstance().wallet.rewarded);
        this.walletListLabel1.textProperty().bind(StringManager.getInstance().wallet.tabWallet);
        this.walletListLabel2.textProperty().bind(StringManager.getInstance().wallet.tabAppAndTokens);
        this.totalAssetLabel1.textProperty().bind(StringManager.getInstance().wallet.tabApis);
        this.totalAssetLabel2.textProperty().bind(StringManager.getInstance().wallet.tabMineral);
        this.tableHeaderName.textProperty().bind(StringManager.getInstance().wallet.tableHeaderName);
        this.tableHeaderAddressMasking.textProperty().bind(StringManager.getInstance().wallet.tableHeaderAddressMasking);
        this.tableHeaderAmount.textProperty().bind(StringManager.getInstance().wallet.tableHeaderAmount);
        this.tableHeaderTransfer.textProperty().bind(StringManager.getInstance().wallet.tableHeaderTransfer);
        this.tableHeaderName2.textProperty().bind(StringManager.getInstance().wallet.tableHeaderName);
        this.tableHeaderAmount2.textProperty().bind(StringManager.getInstance().wallet.tableHeaderAmount);
        this.tableHeaderTransfer2.textProperty().bind(StringManager.getInstance().wallet.tableHeaderTransfer);
        this.searchApisAndTokens.promptTextProperty().bind(StringManager.getInstance().common.searchApisAndTokens);
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
        this.totalTitle.textProperty().bind(this.walletModel.totalTitleProperty());
        this.totalSubTitle.textProperty().bind(this.walletModel.totalSubTitleProperty());
        this.totalMainNatureLabel.textProperty().bind(this.walletModel.totalMainNaturalProperty());
        this.totalMainDecimalLabel.textProperty().bind(this.walletModel.totalMainDecimalProperty());
        this.totalMainUnitLabel.textProperty().bind(this.walletModel.totalMainUnitProperty());
        this.totalSubNatureLabel.textProperty().bind(this.walletModel.totalSubNaturalProperty());
        this.totalSubDecimalLabel.textProperty().bind(this.walletModel.totalSubDecimalProperty());
        this.totalSubUnitLabel.textProperty().bind(this.walletModel.totalSubUnitProperty());
        this.rewaredLabel.textProperty().bind(this.walletModel.rewardProperty());
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

        if(index == 0){
            unitTotalType = WalletModel.UNIT_TYPE_APIS;
            walletModel.setTotalType(WalletModel.UNIT_TYPE_APIS);
        }else if(index == 1){
            unitTotalType = WalletModel.UNIT_TYPE_MINERAL;
            walletModel.setTotalType(WalletModel.UNIT_TYPE_MINERAL);
        }
    }


    public void initLayoutWalletListTab(){
        this.walletListLabels.add(this.walletListLabel1);
        this.walletListLabels.add(this.walletListLabel2);

        this.walletListLines.add(this.walletListLinePane1);
        this.walletListLines.add(this.walletListLinePane2);

        this.iconMiningWallet.visibleProperty().bind(this.btnMiningWallet.visibleProperty());
        this.iconMasternode.visibleProperty().bind(this.btnMasternode.visibleProperty());
    }

    public void setWalletListTabActive(int index){
        this.walletListTabIndex = index;

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

        headerItem.setVisible(false);
        headerGroupItem.setVisible(false);
        if(index == 0){
            headerItem.setVisible(true);
        }else if(index == 1){
            headerGroupItem.setVisible(true);
        }
    }

    public void selectedWalletListTab(int index){

        // change header active
        setWalletListTabActive(index);

        // change table layout
        if(this.walletListTabIndex == 0){
            walletListBodyController.init(WalletListController.LIST_TYPE_ITEM);
        }else if(this.walletListTabIndex == 1){
            walletListBodyController.init(WalletListController.LIST_TYPE_GROUP);
        }

        // check remove
        removeWalletCheckList();

        walletListBodyController.update();
        openWalletItemModel = null;
        walletListBodyController.setOpenItem(openWalletItemModel);
        walletListBodyController.setOpenGroupItem(0);

        walletListBodyController.focusIn();

    }

    public void hideToolTipAll(){
        for(int i=0; i<this.tooltips.size(); i++){
            this.tooltips.get(i).setVisible(false);
        }
    }

    public void showToolGroup(boolean showMining, boolean showMaster){
        this.btnChangeNameWallet.setVisible(true);
        this.btnChangePasswordWallet.setVisible(true);
        this.btnBackupWallet.setVisible(true);
        this.btnRemoveWallet.setVisible(true);

        if(showMining){
            showToolMining();
        }else{
            hideToolMining();
        }

        if(showMaster){
            showToolMasternode();
        }else{
            hideToolMasternode();
        }

    }
    public void showToolMining(){
        this.btnMiningWallet.setVisible(true);
        this.toolMiningWallet.setVisible(true);
        this.toolMiningWallet.setPrefWidth(-1);
    }
    public void showToolMasternode(){
        this.btnMasternode.setVisible(true);
        this.toolMasternode.setVisible(true);
        this.toolMasternode.setPrefWidth(-1);
    }

    public void hideToolGroup(){
        this.btnChangeNameWallet.setVisible(false);
        this.btnChangePasswordWallet.setVisible(false);
        this.btnBackupWallet.setVisible(false);
        this.btnRemoveWallet.setVisible(false);
        hideToolMining();
        hideToolMasternode();
    }
    public void hideToolMining(){
        this.btnMiningWallet.setVisible(false);
        this.toolMiningWallet.setVisible(false);
        this.toolMiningWallet.setPrefWidth(0);
    }
    public void hideToolMasternode(){
        this.btnMasternode.setVisible(false);
        this.toolMasternode.setVisible(false);
        this.toolMasternode.setPrefWidth(0);
    }

    public void initWalletList(){
        WalletItemModel walletItemModel = null;
        BigInteger totalApis = BigInteger.ZERO;
        BigInteger totalMineral = BigInteger.ZERO;
        String id, apis, mineral, alias, mask;
        String[] apisSplit, mineralSplit;

        AppManager.getInstance().keystoreFileReadAll();

        boolean isStaking = false;
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

            id = (dataExp.id != null) ? dataExp.id : "";
            apis = (dataExp.balance != null) ? dataExp.balance : "0";
            mineral = (dataExp.mineral != null) ? dataExp.mineral : "0";
            alias = (dataExp.alias != null)? dataExp.alias : "Wallet Alias";
            mask = (dataExp.mask != null)? dataExp.mask : "";

            totalApis = totalApis.add(new BigInteger(apis));
            totalMineral = totalMineral.add(new BigInteger(mineral));

            //새로운리스트와 기존리스트 비교
            int isOverlapIndex = -1;
            for(int m=0; m<walletListModels.size(); m++){
                if (walletListModels.get(m).getId().equals(id)) {
                    isOverlapIndex = m;
                    break;
                }
            }
            if (isOverlapIndex >= 0) {
                // 기존 데이터일 경우
                if(walletListModels.size() > i) {
                    walletItemModel = walletListModels.get(isOverlapIndex);
                }
            } else {
                // 새로운 데이터일 경우
                walletItemModel = new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS);
                walletListModels.add(walletItemModel);
            }

            if(walletItemModel != null) {
                walletItemModel.setId(id);
                walletItemModel.setAlias(alias);
                walletItemModel.setAddress(dataExp.address);
                walletItemModel.setBalance(apis);
                walletItemModel.setMineral(mineral);
                walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
                walletItemModel.setMining(id.equals(AppManager.getInstance().getMiningWalletId()));
                walletItemModel.setMask(mask);
            }
            if(id.equals(AppManager.getInstance().getMiningWalletId())){
                isStaking = true;
            }
        }

        // check staking
        stakingPane.setVisible(isStaking);

        apisSplit = AppManager.addDotWidthIndex(totalApis.toString()).split("\\.");
        mineralSplit = AppManager.addDotWidthIndex(totalMineral.toString()).split("\\.");

        for(int m=0; m<walletListModels.size(); m++){
            WalletItemModel model = walletListModels.get(m);

            //기존리스트와 새로운리스트 비교
            boolean isOverlap = false;
            for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
                KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);
                if(model.getId().equals(dataExp.id)){
                    isOverlap = true;
                    break;
                }
            }

            if(isOverlap){
                model.setTotalApisNatural(apisSplit[0]);
                model.setTotalApisDecimal("."+apisSplit[1]);
                model.setTotalMineralNatural(mineralSplit[0]);
                model.setTotalMineralDecimal("."+mineralSplit[1]);
                walletListBodyController.updateWalletListItem(model);
            }else{
                walletListModels.remove(m);
                m--;
            }
        }

        walletModel.setTotalType(this.unitTotalType);
        walletModel.setTotalApisNatural(apisSplit[0]);
        walletModel.setTotalApisDecimal("."+apisSplit[1]);
        walletModel.setTotalMineralNatural(mineralSplit[0]);
        walletModel.setTotalMineralDecimal("."+mineralSplit[1]);

        walletListSort(walletListSortType);

        // 기존에 열려있던 지갑 리스트를 다시 열어준다.
        walletListBodyController.setOpenItem(openWalletItemModel);
        walletListBodyController.setOpenGroupItem(openWalletItemIndex);
    }

    public void walletListSort(int sortType){
        this.walletListSortType = sortType;
        walletListBodyController.sort(sortType);

        this.sortNameImg.setImage(imageSortNone);
        this.sortAmountImg.setImage(imageSortNone);

        this.sortNameImg1.setImage(imageSortNone);
        this.sortAmountImg1.setImage(imageSortNone);

        if(sortType == WalletListController.SORT_ALIAS_ASC){
            this.sortNameImg.setImage(imageSortAsc);
            this.sortNameImg1.setImage(imageSortAsc);

        }else if(sortType == WalletListController.SORT_ALIAS_DESC){
            this.sortNameImg.setImage(imageSortDesc);
            this.sortNameImg1.setImage(imageSortDesc);

        }else if(sortType == WalletListController.SORT_BALANCE_ASC){
            this.sortAmountImg.setImage(imageSortAsc);
            this.sortAmountImg1.setImage(imageSortAsc);

        }else if(sortType == WalletListController.SORT_BALANCE_DESC){
            this.sortAmountImg.setImage(imageSortDesc);
            this.sortAmountImg1.setImage(imageSortDesc);

        }
    }

    public void onMouseClickedMoveTransfer(){
        AppManager.getInstance().guiFx.getMain().selectedHeader(1);
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
        }else if(id.equals("apisInfoPane")){
            this.tooltipApis.setVisible(true);
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
        }else if(id.equals("apisInfoPane")){
            tooltipApis.setVisible(false);
        }


    }
    @FXML
    private void onClickEventWalletTool(InputEvent event) {
        String id = ((Node) event.getSource()).getId();
        if (id.equals("btnChangeNameWallet")) {
            PopupChangeWalletNameController controller = (PopupChangeWalletNameController) PopupManager.getInstance().showMainPopup("popup_change_wallet_name.fxml", 0);
            controller.setModel(walletCheckList.get(0));

        } else if (id.equals("btnChangePasswordWallet")) {
            PopupChangePasswordController controller = (PopupChangePasswordController) PopupManager.getInstance().showMainPopup("popup_change_wallet_password.fxml", 0);
            controller.setModel(walletCheckList.get(0));

        } else if (id.equals("btnBackupWallet")) {
            PopupBackupWalletPasswordController controller = (PopupBackupWalletPasswordController) PopupManager.getInstance().showMainPopup("popup_backup_wallet_password.fxml", 0);
            controller.setModel(walletCheckList.get(0));
        } else if (id.equals("btnRemoveWallet")) {
            PopupRemoveWalletPasswordController controller = (PopupRemoveWalletPasswordController) PopupManager.getInstance().showMainPopup("popup_remove_wallet_password.fxml", 0);
            controller.setHandler(new PopupRemoveWalletPasswordController.PopupRemoveWalletPassword() {
                @Override
                public void remove(List<String> removeWalletIdList) {
                    for(int i=0; i<removeWalletIdList.size(); i++){
                        for(int j=0; j<AppManager.getInstance().getKeystoreExpList().size(); j++){
                            if(AppManager.getInstance().getKeystoreExpList().get(j).id.equals(removeWalletIdList.get(i))){
                                AppManager.getInstance().getKeystoreExpList().remove(j);
                                j--;
                            }
                        }
                        for(int j=0; j<AppManager.getInstance().getKeystoreList().size(); j++){
                            if(AppManager.getInstance().getKeystoreList().get(j).id.equals(removeWalletIdList.get(i))){
                                AppManager.getInstance().getKeystoreList().remove(j);
                                j--;
                            }
                        }
                        for(int j=0; j<walletListModels.size(); j++){
                            if(walletListModels.get(j).getId().equals(removeWalletIdList.get(i))){
                                walletListBodyController.removeWalletListItem(removeWalletIdList.get(i));
                                walletListModels.remove(j);
                                j--;
                            }
                        }
                        KeyStoreManager.getInstance().deleteKeystore(removeWalletIdList.get(i));
                    }
                    AppManager.getInstance().guiFx.getWallet().update(null);
                }
            });
            controller.setModel(walletCheckList.get(0));

        } else if (id.equals("btnMiningWallet")) {
            PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController) PopupManager.getInstance().showMainPopup("popup_mining_wallet_confirm.fxml", 0);
            controller.setModel(walletCheckList.get(0));

        } else if (id.equals("btnMasternode")) {
            PopupMasternodeController controller = (PopupMasternodeController) PopupManager.getInstance().showMainPopup("popup_masternode.fxml", 0);
            controller.setModel(walletCheckList.get(0));

        }else if(id.equals("btnToken")) {
            PopupManager.getInstance().showMainPopup("popup_token_add_edit.fxml", 0);

        }else if(id.equals("btnCreateWallet")){
            AppManager.getInstance().guiFx.pageMoveIntro(true);

        }
    }

    public void update(String reward){
        this.reward = reward;

        initWalletList();

        // 지갑 리스트 체크한 개수
        // 체크한 지갑 리스트를 다시 설정한다.
        int checkSize = walletCheckList.size();
        if(checkSize == 0){
            removeWalletCheckList();
        }else {
            WalletItemModel model = walletCheckList.get(0);
            removeWalletCheckList();
            addWalletCheckList(model);
        }

        // 리워드 : bigInteger string
        if(reward != null && reward.length() > 0){
            this.walletModel.setReward(AppManager.addDotWidthIndex(reward).split("\\.")[0]);
        }
    }

    // 지갑리스트의 선택 목록을 초기화 한다.
    public void removeWalletCheckList(){
        this.walletCheckList.clear();
        walletListBodyController.unCheckAll();
        hideToolGroup();
    }
    public void addWalletCheckList(WalletItemModel model){
        String balance = model.getBalance();
        if(balance != null && balance.length() > 0){
            balance = balance.replace(".","");
        }else{
            balance = "";
        }
        walletCheckList.add(model);
        if(balance.equals("50000000000000000000000")
                || balance.equals("200000000000000000000000")
                || balance.equals("500000000000000000000000")){
            showToolGroup(true, true);
        }else if(balance.equals("0000000000000000000") || balance.equals("0")) {
            showToolGroup(false, false);
        }else{
            showToolGroup(true, false);
        }
        walletListBodyController.check(model);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // init image loading
        initImageLoad();

        // 언어 설정
        languageSetting();

        // init tabs
        initLayoutTotalAssetTab();
        initLayoutWalletListTab();

        // init top total asset
        initLayoutTotalAsset();

        // select tab
        selectedTotalAssetTab(0);
        selectedWalletListTab(0);

        walletModel.setTotalType(WalletModel.UNIT_TYPE_APIS);
        walletListBodyController.setHandler(new WalletListController.WalletListEvent() {
            @Override
            public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                removeWalletCheckList();
                if(isChecked) {
                    addWalletCheckList(model);
                }
            }

            @Override
            public void onClickOpen(WalletItemModel model, int index) {
                openWalletItemModel = model;
                openWalletItemIndex = index;
            }

            @Override
            public void onClickClose(WalletItemModel model, int index) {
                openWalletItemModel = null;
                openWalletItemIndex = -1;
            }
        });
        openWalletItemModel = null;
        openWalletItemIndex = 0;
        walletListBodyController.setOpenItem(openWalletItemModel);
        walletListBodyController.setOpenGroupItem(openWalletItemIndex);

        removeWalletCheckList();

    }
}
