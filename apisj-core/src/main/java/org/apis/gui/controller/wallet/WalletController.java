package org.apis.gui.controller.wallet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import javafx.scene.image.ImageView;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.MainController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.TabMenuController;
import org.apis.gui.controller.popup.*;
import org.apis.gui.controller.wallet.tokenlist.TokenListController;
import org.apis.gui.controller.wallet.walletlist.WalletListController;
import org.apis.gui.manager.*;
import org.apis.gui.model.WalletItemModel;
import org.apis.keystore.KeyStoreDataExp;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WalletController extends BaseViewController {
    private final int TAB_TOP_TYPE_APIS = 0;
    private final int TAB_TOP_TYPE_MINERAL = 1;
    private int tabTopType = TAB_TOP_TYPE_APIS;

    private final int TAB_LIST_TYPE_WALLET = 0;
    private final int TAB_LIST_TYPE_TOKEN = 1;
    private int tabListType = TAB_LIST_TYPE_WALLET ;

    @FXML private Label btnMiningWallet, btnToken, btnCreateWallet, btnMasternode;
    @FXML private Label rewarded;
    @FXML private Label totalTitle, totalSubTitle, totalMainNatureLabel, totalMainUnitLabel, totalSubNatureLabel, totalSubUnitLabel;
    @FXML private AnchorPane toolMiningWallet, toolMasternode, stakingPane;
    @FXML private ImageView btnChangeNameWallet, btnChangePasswordWallet, btnChangeProofKey, btnBackupWallet, btnRemoveWallet, iconMiningWallet, iconMasternode, btnSearch;
    @FXML private ImageView tooltipApis;
    @FXML private TextField searchApisAndTokens;
    @FXML private WalletTooltipController tooltip1Controller, tooltip2Controller, tooltipChangeProofKeyController, tooltip3Controller, tooltip4Controller;
    @FXML private AnchorPane tooltip1Pane, tooltip2Pane, tooltipChangeProofKeyPane, tooltip3Pane, tooltip4Pane, topTransferPane;

    // tab wallet
    @FXML private GridPane walletTable;
    @FXML private AnchorPane headerWalletItem, walletTableScrollContentPane;
    @FXML private ScrollPane walletTableScrollPane;
    @FXML private Label headerWalletNameLabel, headerWalletMaskLabel, headerWalletAmountLabel, headerWalletTransferLabel;
    @FXML private ImageView imgHeaderWalletSortName, imgHeaderWalletSortMask, imgHeaderWalletSortAmount;
    @FXML private WalletListController walletListController;
    // tab token
    @FXML private GridPane tokenTable;
    @FXML private AnchorPane headerTokenItem, tokenTableScrollContentPane;
    @FXML private ScrollPane tokenTableScrollPane;
    @FXML private Label headerTokenNameLabel, headerTokenAmountLabel;
    @FXML private ImageView imgHeaderTokenSortName, imgHeaderTokenSortAmount, buyMineralButton;
    @FXML private TokenListController tokenListController;
    @FXML private TabMenuController tabMenuController, walletListTabMenuController;

    @FXML Label totalAssetLabel, myRewardsLabel, rewardedLabel, nowStakingLabel, howApisLabel,
            headerTokenTransfer
    ;

    @FXML private AnchorPane createWalletPane;
    private ArrayList<WalletItemModel> walletListModels = new ArrayList<>();
    private ArrayList<WalletItemModel> walletCheckList = new ArrayList<>();

    private Image imageChangeName, imageChangeNameHover;
    private Image imageChangePassword, imageChangePasswordHover;
    private Image imageChangeProofKey, imageChangeProofKeyHover;
    private Image imageBakcup, imageBakcupHover;
    private Image imageRemove, imageRemoveHover;
    private Image imageSortAsc, imageSortDesc, imageSortNone;
    private Image imageMiningGrey, imageMiningRed;

    private String reward;
    private boolean isScrollingWalletTable;
    private boolean isScrollingTokenTable;


    public WalletController(){
        AppManager.getInstance().guiFx.setWallet(this);
    }


    public void languageSetting() {
        this.tabMenuController.addItem(StringManager.getInstance().wallet.tabApis, TAB_TOP_TYPE_APIS);
        this.tabMenuController.addItem(StringManager.getInstance().wallet.tabMineral, TAB_TOP_TYPE_MINERAL);
        this.walletListTabMenuController.addItem(StringManager.getInstance().wallet.tabWallet, TAB_LIST_TYPE_WALLET);
        this.walletListTabMenuController.addItem(StringManager.getInstance().wallet.tabAppAndTokens, TAB_LIST_TYPE_TOKEN);

        this.totalAssetLabel.textProperty().bind(StringManager.getInstance().wallet.totalAsset);
        this.myRewardsLabel.textProperty().bind(StringManager.getInstance().wallet.myRewards);
        this.nowStakingLabel.textProperty().bind(StringManager.getInstance().wallet.nowStaking);
        this.howApisLabel.textProperty().bind(StringManager.getInstance().wallet.howToGetRewardedWithApis);
        this.btnMiningWallet.textProperty().bind(StringManager.getInstance().wallet.miningButton);
        this.btnMasternode.textProperty().bind(StringManager.getInstance().wallet.masternodeButton);
        this.btnToken.textProperty().bind(StringManager.getInstance().wallet.tokenButton);
        this.btnCreateWallet.textProperty().bind(StringManager.getInstance().wallet.createButton);
        this.rewardedLabel.textProperty().bind(StringManager.getInstance().wallet.rewarded);
        this.headerWalletNameLabel.textProperty().bind(StringManager.getInstance().wallet.tableHeaderName);
        this.headerWalletMaskLabel.textProperty().bind(StringManager.getInstance().wallet.tableHeaderAddressMasking);
        this.headerWalletAmountLabel.textProperty().bind(StringManager.getInstance().wallet.tableHeaderAmount);
        this.headerWalletTransferLabel.textProperty().bind(StringManager.getInstance().wallet.tableHeaderTransfer);
        this.headerTokenNameLabel.textProperty().bind(StringManager.getInstance().wallet.tableHeaderName);
        this.headerTokenAmountLabel.textProperty().bind(StringManager.getInstance().wallet.tableHeaderAmount);
        this.headerTokenTransfer.textProperty().bind(StringManager.getInstance().wallet.tableHeaderTransfer);
        this.searchApisAndTokens.promptTextProperty().bind(StringManager.getInstance().common.searchApisAndTokens);

        this.tooltip1Controller.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.changeWalletName);
        this.tooltip2Controller.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.changeWalletPassword);
        this.tooltipChangeProofKeyController.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.changeProofKey);
        this.tooltip3Controller.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.backupWallet);
        this.tooltip4Controller.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.removeWallet);


        StyleManager.fontStyle(headerWalletTransferLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerWalletAmountLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerWalletNameLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerWalletMaskLabel, StyleManager.Standard.SemiBold12);

        StyleManager.fontStyle(headerTokenNameLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerTokenAmountLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerTokenTransfer, StyleManager.Standard.SemiBold12);

        StyleManager.fontStyle(searchApisAndTokens, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(totalAssetLabel, StyleManager.Standard.SemiBold14);
        StyleManager.fontStyle(myRewardsLabel, StyleManager.Standard.SemiBold14);


    }

    public void initImageLoad(){

        this.imageChangeName = ImageManager.btnChangeName;
        this.imageChangeNameHover = ImageManager.btnChangeNameHover;
        this.imageChangePassword = ImageManager.btnChangePassword;
        this.imageChangePasswordHover = ImageManager.btnChangePasswordHover;
        this.imageChangeProofKey = ImageManager.btnChangeProofKey;
        this.imageChangeProofKeyHover = ImageManager.btnChangeProofKeyHover;
        this.imageBakcup = ImageManager.btnBackupWallet;
        this.imageBakcupHover = ImageManager.btnBackupWalletHover;
        this.imageRemove = ImageManager.btnRemoveWallet;
        this.imageRemoveHover = ImageManager.btnRemoveWalletHover;

        this.imageSortNone = ImageManager.icSortNONE;
        this.imageSortAsc = ImageManager.icSortASC;
        this.imageSortDesc = ImageManager.icSortDESC;
        this.imageMiningGrey = ImageManager.btnMiningGrey;
        this.imageMiningRed = ImageManager.btnMiningRed;

        this.tooltipApis.setImage(ImageManager.tooltipReward);
        this.tooltipApis.setVisible(false);
    }

    public void settingLayoutData(){
        BigInteger totalApis = AppManager.getInstance().getTotalApis();
        BigInteger totalMineral = AppManager.getInstance().getTotalMineral();
        BigInteger totalRewarded = AppManager.getInstance().getTotalReward();
        this.totalTitle.textProperty().unbind();
        this.totalSubTitle.textProperty().unbind();
        this.rewardedLabel.textProperty().unbind();
        if(tabTopType == TAB_TOP_TYPE_APIS){
            this.totalTitle.textProperty().bind(StringManager.getInstance().wallet.totalAmount);
            this.totalSubTitle.textProperty().bind(StringManager.getInstance().wallet.totalMineralSubAmount);
            this.totalMainNatureLabel.setText(ApisUtil.readableApis(totalApis, ',', true));
            this.totalMainUnitLabel.setText("APIS");
            this.totalSubNatureLabel.setText(ApisUtil.readableApis(totalMineral, ',', true));
            this.totalSubUnitLabel.setText("MNR");
            this.rewardedLabel.textProperty().bind(StringManager.getInstance().wallet.rewarded);
            this.rewarded.setText(ApisUtil.convert(totalRewarded.toString(),ApisUtil.Unit.aAPIS, ApisUtil.Unit.APIS,',',true).split("\\.")[0]);
            this.topTransferPane.setVisible(true);
            this.buyMineralButton.setVisible(false);
        }else if(tabTopType == TAB_TOP_TYPE_MINERAL){
            this.totalTitle.textProperty().bind(StringManager.getInstance().wallet.totalMineralAmount);
            this.totalSubTitle.textProperty().bind(StringManager.getInstance().wallet.totalSubAmount);
            this.totalMainNatureLabel.setText(ApisUtil.readableApis(totalMineral, ',', true));
            this.totalMainUnitLabel.setText("MNR");
            this.totalSubNatureLabel.setText(ApisUtil.readableApis(totalApis, ',', true));
            this.totalSubUnitLabel.setText("APIS");
            this.rewardedLabel.textProperty().bind(StringManager.getInstance().wallet.rewarded);
            this.rewarded.setText(ApisUtil.convert(totalRewarded.toString(),ApisUtil.Unit.aAPIS, ApisUtil.Unit.APIS,',',true).split("\\.")[0]);
            this.topTransferPane.setVisible(false);
            this.buyMineralButton.setVisible(true);
        }
    }

    public void selectedTotalAssetTab(int index){
        this.tabTopType = index;

        settingLayoutData();
    }


    public void initLayoutWalletListTab(){

        this.iconMiningWallet.visibleProperty().bind(this.btnMiningWallet.visibleProperty());
        this.iconMasternode.visibleProperty().bind(this.btnMasternode.visibleProperty());
    }

    private void setWalletListTabActive(int index){

        headerWalletItem.setVisible(false);
        headerTokenItem.setVisible(false);
        if(index == 0){
            headerWalletItem.setVisible(true);
        }else if(index == 1){
            headerTokenItem.setVisible(true);
        }
    }

    public void selectedWalletListTab(int index){
        this.tabListType = index;

        // change header active
        setWalletListTabActive(this.tabListType);

        // change table layout
        if(this.tabListType == TAB_LIST_TYPE_WALLET){
            walletTable.setVisible(true);
            walletTable.setPrefHeight(-1);
            tokenTable.setVisible(false);
            tokenTable.setPrefHeight(0);
        }else if(this.tabListType == TAB_LIST_TYPE_TOKEN){
            walletTable.setVisible(false);
            walletTable.setPrefHeight(0);
            tokenTable.setVisible(true);
            tokenTable.setPrefHeight(-1);
        }

        walletListController.refresh();

        // check remove
        removeWalletCheckList();

        // 화면 새로고침
        update();
    }

    public void hideToolTipAll(){
        tooltip1Controller.hideTooltip();
        tooltip2Controller.hideTooltip();
        tooltipChangeProofKeyController.hideTooltip();
        tooltip3Controller.hideTooltip();
        tooltip4Controller.hideTooltip();
    }

    public void showToolGroup(boolean showMining, boolean showMaster){
        this.btnChangeNameWallet.setVisible(true);
        this.btnChangePasswordWallet.setVisible(true);
        this.btnChangeProofKey.setVisible(true);
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
        this.btnChangeProofKey.setVisible(false);
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

    public void updateTableList(){

        updateWalletModel();

        if(this.tabListType == TAB_LIST_TYPE_WALLET){
            updateWalletList();

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

        }else if(this.tabListType == TAB_LIST_TYPE_TOKEN){
            updateTokenList();
        }
    }

    private void updateWalletModel(){

        BigInteger totalApis = BigInteger.ZERO;
        BigInteger totalMineral = BigInteger.ZERO;
        BigInteger apis = BigInteger.ZERO;
        BigInteger mineral = BigInteger.ZERO;
        String id, alias, mask;
        boolean isUsedProofKey = false;

        WalletItemModel walletItemModel = null;

        boolean isStaking = false;
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

            id = (dataExp.id != null) ? dataExp.id : "";
            alias = (dataExp.alias != null)? dataExp.alias : "Wallet Alias";
            mask = (dataExp.mask != null)? dataExp.mask : "";
            apis = dataExp.balance;
            mineral = dataExp.mineral;
            isUsedProofKey = dataExp.isUsedProofkey;

            totalApis = totalApis.add(apis);
            totalMineral = totalMineral.add(mineral);

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
                walletItemModel = new WalletItemModel();
                walletListModels.add(walletItemModel);
            }

            if(walletItemModel != null) {
                walletItemModel.setId(id);
                walletItemModel.setAlias(alias);
                walletItemModel.setAddress(dataExp.address);
                walletItemModel.setApis(apis);
                walletItemModel.setMineral(mineral);
                walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(i).toString());
                walletItemModel.setMining(id.equals(AppManager.getInstance().getMiningWalletId()));
                walletItemModel.setMasterNode(id.equals(AppManager.getInstance().getMasterNodeWalletId()));
                walletItemModel.setMask(mask);
                walletItemModel.setUsedProofKey(isUsedProofKey);

            }
            if(id.equals(AppManager.getInstance().getMiningWalletId())){
                isStaking = true;
            }
        }

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

            if(!isOverlap){
                walletListModels.remove(m);
                m--;
            }
        }


        // check staking
        stakingPane.setVisible(isStaking);
    }
    private void updateTokenList(){
        tokenListController.updateWallet(walletListModels);
    }
    private void updateWalletList(){

        for(int m=0; m<walletListModels.size(); m++){
            WalletItemModel model = walletListModels.get(m);

            //기존리스트와 새로운리스트 비교
            for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
                KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);
                if(model.getId().equals(dataExp.id)){
                    walletListController.updateWallet(model, m);
                    break;
                }
            }
        }

        // TOKEN HEADER 데이터 넣기
        walletListController.update();

        // 화면새로고침
        walletListController.refresh();

    }

    public void walletSort(Sort sortType){
        walletListController.walletSort(sortType);

        this.imgHeaderWalletSortName.setImage(imageSortNone);
        this.imgHeaderWalletSortMask.setImage(imageSortNone);
        this.imgHeaderWalletSortAmount.setImage(imageSortNone);

        if(sortType == Sort.ALIAS_ASC){
            this.imgHeaderWalletSortName.setImage(imageSortAsc);

        }else if(sortType == Sort.ALIAS_DESC){
            this.imgHeaderWalletSortName.setImage(imageSortDesc);

        }else if(sortType == Sort.VALUE_ASC){
            this.imgHeaderWalletSortAmount.setImage(imageSortAsc);

        }else if(sortType == Sort.VALUE_DESC){
            this.imgHeaderWalletSortAmount.setImage(imageSortDesc);

        }else if(sortType == Sort.MASK_ASC){
            this.imgHeaderWalletSortMask.setImage(imageSortAsc);

        }else if(sortType == Sort.MASK_DESC){
            this.imgHeaderWalletSortMask.setImage(imageSortDesc);

        }

        if(sortType == Sort.ALIAS_ASC || sortType == Sort.ALIAS_DESC) {
            StyleManager.fontColorStyle(headerWalletNameLabel, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(headerWalletMaskLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletAmountLabel, StyleManager.AColor.C999999);
        }else if(sortType == Sort.MASK_ASC || sortType == Sort.MASK_DESC) {
            StyleManager.fontColorStyle(headerWalletNameLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletMaskLabel, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(headerWalletAmountLabel, StyleManager.AColor.C999999);
        }else if(sortType == Sort.VALUE_ASC || sortType == Sort.VALUE_DESC) {
            StyleManager.fontColorStyle(headerWalletNameLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletMaskLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletAmountLabel, StyleManager.AColor.C910000);
        }


    }

    public void tokenSort(Sort sortType){
        tokenListController.tokenSort(sortType);

        this.imgHeaderTokenSortName.setImage(imageSortNone);
        this.imgHeaderTokenSortAmount.setImage(imageSortNone);

        if(sortType == Sort.ALIAS_ASC){
            this.imgHeaderTokenSortName.setImage(imageSortAsc);

        }else if(sortType == Sort.ALIAS_DESC){
            this.imgHeaderTokenSortName.setImage(imageSortDesc);

        }else if(sortType == Sort.VALUE_ASC){
            this.imgHeaderTokenSortAmount.setImage(imageSortAsc);

        }else if(sortType == Sort.VALUE_DESC){
            this.imgHeaderTokenSortAmount.setImage(imageSortDesc);
        }



        if(sortType == Sort.ALIAS_ASC || sortType == Sort.ALIAS_DESC) {
            StyleManager.fontColorStyle(headerTokenNameLabel, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(headerTokenAmountLabel, StyleManager.AColor.C999999);
        }else if(sortType == Sort.VALUE_ASC || sortType == Sort.VALUE_DESC) {
            StyleManager.fontColorStyle(headerTokenNameLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerTokenAmountLabel, StyleManager.AColor.C910000);
        }

    }

    public void onMouseClickedMoveTransfer(){
        if(tabTopType == TAB_TOP_TYPE_APIS) {
            AppManager.getInstance().guiFx.getMain().selectedHeader(MainController.MainTab.TRANSFER);
        }
    }

    public void onClickSortWalletName(){

        Sort sortType = walletListController.getWalletSort();
        if(sortType != Sort.ALIAS_ASC){
            walletSort(Sort.ALIAS_ASC);
        }else if(sortType != Sort.ALIAS_DESC){
            walletSort(Sort.ALIAS_DESC);
        }
    }

    public void onClickSortWalletMask(){

        Sort sortType = walletListController.getWalletSort();
        if(sortType != Sort.MASK_ASC){
            walletSort(Sort.MASK_ASC);
        }else if(sortType != Sort.MASK_DESC){
            walletSort(Sort.MASK_DESC);
        }
    }

    public void onClickSortWalletAmount(){
        Sort sortType = walletListController.getWalletSort();
        if(sortType != Sort.VALUE_ASC){
            walletSort(Sort.VALUE_ASC);
        }else if(sortType != Sort.VALUE_DESC){
            walletSort(Sort.VALUE_DESC);
        }
    }

    public void onClickSortTokenName(){
        Sort sortType = tokenListController.getTokenSort();
        if(sortType != Sort.ALIAS_ASC){
            tokenSort(Sort.ALIAS_ASC);
        }else if(sortType != Sort.ALIAS_DESC){
            tokenSort(Sort.ALIAS_DESC);
        }
    }

    public void onClickSortTokenAmount(){
        Sort sortType = tokenListController.getTokenSort();
        if(sortType != Sort.VALUE_ASC){
            tokenSort(Sort.VALUE_ASC);
        }else if(sortType != Sort.VALUE_DESC){
            tokenSort(Sort.VALUE_DESC);
        }
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
            selectedWalletListTab(TAB_LIST_TYPE_WALLET);
        }else if(id.equals("walletListTab2")) {
            selectedWalletListTab(TAB_LIST_TYPE_TOKEN);
        }

    }

    @FXML
    private void onMouseEntered(InputEvent event){
        hideToolTipAll();
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnChangeNameWallet")) {
            btnChangeNameWallet.setImage(imageChangeNameHover);
            tooltip1Controller.showTooltip();
        }else if(id.equals("btnChangePasswordWallet")) {
            btnChangePasswordWallet.setImage(imageChangePasswordHover);
            tooltip2Controller.showTooltip();
        }else if(id.equals("btnChangeProofKey")) {
            btnChangeProofKey.setImage(imageChangeProofKeyHover);
            tooltipChangeProofKeyController.showTooltip();
        }else if(id.equals("btnBackupWallet")) {
            btnBackupWallet.setImage(imageBakcupHover);
            tooltip3Controller.showTooltip();
        }else if(id.equals("btnRemoveWallet")) {
            btnRemoveWallet.setImage(imageRemoveHover);
            tooltip4Controller.showTooltip();
        }else if(id.equals("apisInfoPane")){
            this.tooltipApis.setVisible(true);
        }else if(id.equals("btnToken")){
            this.btnToken.setStyle(new JavaFXStyle(this.btnToken.getStyle()).add("-fx-background-color", "#810000").toString());
        }else if(id.equals("btnCreateWallet")){
            this.btnCreateWallet.setStyle(new JavaFXStyle(this.btnCreateWallet.getStyle()).add("-fx-background-color", "#810000").toString());
        }else if(id.equals("btnMiningWallet")){
            this.btnMiningWallet.setStyle(new JavaFXStyle(this.btnMiningWallet.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
        }else if(id.equals("btnMasternode")){
            this.btnMasternode.setStyle(new JavaFXStyle(this.btnMasternode.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
        }
        event.consume();

    }
    @FXML
    private void onMouseExited(InputEvent event){
        hideToolTipAll();
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnChangeNameWallet")) {
            btnChangeNameWallet.setImage(imageChangeName);
        }else if(id.equals("btnChangePasswordWallet")) {
            btnChangePasswordWallet.setImage(imageChangePassword);
        }else if(id.equals("btnChangeProofKey")) {
            btnChangeProofKey.setImage(imageChangeProofKey);
        }else if(id.equals("btnBackupWallet")) {
            btnBackupWallet.setImage(imageBakcup);
        }else if(id.equals("btnRemoveWallet")) {
            btnRemoveWallet.setImage(imageRemove);
        }else if(id.equals("apisInfoPane")){
            tooltipApis.setVisible(false);
        }else if(id.equals("btnToken")){
            this.btnToken.setStyle(new JavaFXStyle(this.btnToken.getStyle()).add("-fx-background-color", "#910000").toString());
        }else if(id.equals("btnCreateWallet")){
            this.btnCreateWallet.setStyle(new JavaFXStyle(this.btnCreateWallet.getStyle()).add("-fx-background-color", "#910000").toString());
        }else if(id.equals("btnMiningWallet")){
            this.btnMiningWallet.setStyle(new JavaFXStyle(this.btnMiningWallet.getStyle()).add("-fx-background-color", "#ffffff").toString());
        }else if(id.equals("btnMasternode")){
            this.btnMasternode.setStyle(new JavaFXStyle(this.btnMasternode.getStyle()).add("-fx-background-color", "#ffffff").toString());
        }
        event.consume();

    }
    @FXML
    private void onClickEventWalletTool(InputEvent event) {
        String id = ((Node) event.getSource()).getId();
        if (id.equals("btnChangeNameWallet")) {
            PopupChangeWalletNameController controller = (PopupChangeWalletNameController) PopupManager.getInstance().showMainPopup(null, "popup_change_wallet_name.fxml", 0);
            controller.setModel(walletCheckList.get(0));
            controller.getTextFieldController().requestFocus();

        } else if (id.equals("btnChangePasswordWallet")) {
            PopupChangePasswordController controller = (PopupChangePasswordController) PopupManager.getInstance().showMainPopup(null, "popup_change_wallet_password.fxml", 0);
            controller.setModel(walletCheckList.get(0));
            controller.getCurrentFieldController().requestFocus();

        } else if (id.equals("btnBackupWallet")) {
            PopupBackupWalletPasswordController controller = (PopupBackupWalletPasswordController) PopupManager.getInstance().showMainPopup(null, "popup_backup_wallet_password.fxml", 0);
            controller.setModel(walletCheckList.get(0));
            controller.getPasswordController().requestFocus();

        } else if (id.equals("btnRemoveWallet")) {
            PopupRemoveWalletPasswordController controller = (PopupRemoveWalletPasswordController) PopupManager.getInstance().showMainPopup(null, "popup_remove_wallet_password.fxml", 0);
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
                                walletListController.removeWallet(removeWalletIdList.get(i));
                                walletListModels.remove(j);
                                j--;
                            }
                        }
                        KeyStoreManager.getInstance().deleteKeystore(removeWalletIdList.get(i));
                    }
                    AppManager.getInstance().guiFx.getWallet().update();
                }
            });
            controller.setModel(walletCheckList.get(0));
            controller.getPasswordController().requestFocus();

        } else if (id.equals("btnMasternode") || id.equals("iconMasternode")) {
            PopupMasternodeController controller = (PopupMasternodeController) PopupManager.getInstance().showMainPopup(null, "popup_masternode.fxml", 0);
            controller.setModel(walletCheckList.get(0));
            controller.getPasswordController().requestFocus();

        }else if(id.equals("btnToken") || id.equals("iconToken")) {
            PopupTokenListController controller = (PopupTokenListController)PopupManager.getInstance().showMainPopup(null, "popup_token_list.fxml", 0);
            controller.setHandler(new PopupTokenListController.PopupTokenAddEditImpl() {
                @Override
                public void change() {
                    update();
                }
            });

        }else if(id.equals("btnCreateWallet") || id.equals("iconCreateWallet")){
            AppManager.getInstance().guiFx.pageMoveIntro(true);

        }
    }

    @FXML
    public void onReleasedEventWalletTool(InputEvent event) {
        String id = ((Node) event.getSource()).getId();
        if (id.equals("btnChangeProofKey")) {

            if(AppManager.getInstance().isUsedProofKey(Hex.decode(walletCheckList.get(0).getAddress()))){
                PopupProofOfKnowledgeEditController controller = (PopupProofOfKnowledgeEditController) PopupManager.getInstance().showMainPopup(null, "popup_proof_of_knowledge_edit.fxml", 0);
                controller.setModel(walletCheckList.get(0));
                controller.requestFocus();
            }else{
                PopupProofOfKnowledgeRegisterController controller = (PopupProofOfKnowledgeRegisterController) PopupManager.getInstance().showMainPopup(null, "popup_proof_of_knowledge_register.fxml", 0);
                controller.setModel(walletCheckList.get(0));
                controller.requestFocus();
            }

        }
    }

    @FXML
    public void onMouseClickedBuyMineral(){
        PopupManager.getInstance().showMainPopup(null, "buy_mineral.fxml", -1);
    }

    @FXML
    public void onMouseClickedSearchToken(){
        update();
    }

    @Override
    public void update(){
        this.reward = AppManager.getInstance().getTotalReward().toString();

        // 지갑 리스트 업데이트
        updateTableList();

        // 레이아웃 데이터 업데이트
        settingLayoutData();
    }

    // 지갑리스트의 선택 목록을 초기화 한다.
    public void removeWalletCheckList(){
        this.walletCheckList.clear();
        walletListController.unCheckAll();
        hideToolGroup();
    }
    public void addWalletCheckList(WalletItemModel model){
        String apis = model.getApis().toString();
        walletCheckList.add(model);
        if(apis.equals("50000000000000000000000")
                || apis.equals("200000000000000000000000")
                || apis.equals("500000000000000000000000")){
            showToolGroup(true, true);
        }else if(apis.equals("0000000000000000000") || apis.equals("0")) {
            showToolGroup(false, false);
        }else{
            showToolGroup(true, false);
        }
        // Check mining and change button img & color
        if(model.getId().equals(AppManager.getInstance().getMiningWalletId())) {
            btnMiningWallet.setTextFill(Color.web("#910000"));
            iconMiningWallet.setImage(imageMiningRed);
            toolMiningWallet.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController) PopupManager.getInstance().showMainPopup(null, "popup_mining_wallet_confirm.fxml", 0);
                    controller.setModel(walletCheckList.get(0));
                    controller.setType(PopupMiningWalletConfirmController.MINING_TYPE_STOP);
                    controller.getPasswordFieldController().requestFocus();
                }
            });
        } else {
            btnMiningWallet.setTextFill(Color.web("#999999"));
            iconMiningWallet.setImage(imageMiningGrey);
            toolMiningWallet.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    PopupMiningWalletConfirmController controller = (PopupMiningWalletConfirmController) PopupManager.getInstance().showMainPopup(null, "popup_mining_wallet_confirm.fxml", 0);
                    controller.setModel(walletCheckList.get(0));
                    controller.setType(PopupMiningWalletConfirmController.MINING_TYPE_START);
                    controller.getPasswordFieldController().requestFocus();
                }
            });
        }

        walletListController.check(model);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // init image loading
        initImageLoad();

        // 언어 설정
        languageSetting();

        // init tabs
        initLayoutWalletListTab();
        this.tabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                selectedTotalAssetTab(index);
            }
        });
        this.tabMenuController.selectedMenu(TAB_TOP_TYPE_APIS);
        this.tabMenuController.setFontSize14();
        this.tabMenuController.setHSpace(20);

        this.walletListTabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                selectedWalletListTab(index);
            }
        });
        this.walletListTabMenuController.selectedMenu(TAB_LIST_TYPE_WALLET);
        this.walletListTabMenuController.setFontSize14();
        this.walletListTabMenuController.setHSpace(20);


        // init top total asset
        settingLayoutData();

        // scroll spped init
        walletTableScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrollingWalletTable){
                    isScrollingWalletTable = false;
                }else{
                    isScrollingWalletTable = true;

                    double w1w2 = walletTableScrollContentPane.getHeight() - walletTableScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = walletTableScrollPane.getVvalue() + add;
                        if(moveV > walletTableScrollPane.getVmax()){
                            moveV = walletTableScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = walletTableScrollPane.getVvalue() - add;
                        if(moveV < walletTableScrollPane.getVmin()){
                            moveV = walletTableScrollPane.getVmin();
                        }
                    }

                    walletTableScrollPane.setVvalue(moveV);
                }
            }
        });

        tokenTableScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrollingTokenTable){
                    isScrollingTokenTable = false;
                }else{
                    isScrollingTokenTable = true;

                    double w1w2 = tokenTableScrollContentPane.getHeight() - tokenTableScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = tokenTableScrollPane.getVvalue() + add;
                        if(moveV > tokenTableScrollPane.getVmax()){
                            moveV = tokenTableScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = tokenTableScrollPane.getVvalue() - add;
                        if(moveV < tokenTableScrollPane.getVmin()){
                            moveV = tokenTableScrollPane.getVmin();
                        }
                    }

                    tokenTableScrollPane.setVvalue(moveV);
                }
            }
        });

        searchApisAndTokens.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                update();
            }
        });
        searchApisAndTokens.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(oldValue){
                    // focus out
                    btnSearch.setImage(ImageManager.btnSearchTokenOut);
                    if(searchApisAndTokens.getText().length() > 0){
                        btnSearch.setImage(ImageManager.btnSearchTokenIn);
                    }
                }
                if(newValue){
                    // focus in
                    btnSearch.setImage(ImageManager.btnSearchTokenIn);
                }
            }
        });

        walletListController.setHandler(new WalletListController.WalletListImpl() {
            @Override
            public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                removeWalletCheckList();
                if(isChecked){
                    addWalletCheckList(model);
                }
            }

            @Override
            public void onClickOpen(WalletItemModel model, int index, int listType) {
                System.out.println("walletListController onClickOpen");
            }

            @Override
            public void onClickClose(WalletItemModel model, int index, int listType) {
                System.out.println("walletListController onClickClose");
            }
        });

        AppManager.getInstance().getSearchToken().bind(searchApisAndTokens.textProperty());

        removeWalletCheckList();

        // Default Sort aliasAsc
        walletSort(Sort.ALIAS_ASC);
        tokenSort(Sort.ALIAS_ASC);

    }

    public enum Sort {
        /* 사용자설정 오름차순 */DEFAULT_ASC(0),
        /* 사용자설정 내림차순 */DEFAULT_DESC(1),
        /* 지갑이름 오름차순 */ALIAS_ASC(2),
        /* 지갑이름 내림차순 */ALIAS_DESC(3),
        /* 마스크이름 오름차순 */MASK_ASC(4),
        /* 마스크이름 내림차순 */MASK_DESC(5),
        /* 지갑수량 오름차순 */VALUE_ASC(6),
        /* 지갑수량 내림차순 */VALUE_DESC(7);
        int num;
        Sort(int num) {
            this.num = num;
        }
    }
}
