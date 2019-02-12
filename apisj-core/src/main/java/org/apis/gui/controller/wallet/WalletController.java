package org.apis.gui.controller.wallet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javafx.scene.image.ImageView;
import org.apis.config.SystemProperties;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.MainController;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.TabMenuController;
import org.apis.gui.controller.popup.*;
import org.apis.gui.controller.transaction.TransactionNativeBannerDetailController;
import org.apis.gui.controller.wallet.tokenlist.TokenListController;
import org.apis.gui.controller.wallet.walletlist.WalletListController;
import org.apis.gui.controller.wallet.walletlist.WalletListGroupController;
import org.apis.gui.manager.*;
import org.apis.gui.model.WalletItemModel;
import org.apis.keystore.KeyStoreDataExp;
import org.apis.keystore.KeyStoreManager;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

public class WalletController extends BaseViewController {
    private final int TAB_TOP_TYPE_APIS = 0;
    private final int TAB_TOP_TYPE_MINERAL = 1;
    private int tabTopType = TAB_TOP_TYPE_APIS;

    private final int TAB_LIST_TYPE_WALLET = 0;
    private final int TAB_LIST_TYPE_TOKEN = 1;
    private int tabListType = TAB_LIST_TYPE_WALLET ;

    @FXML private Label btnMiningWallet, btnToken, btnCreateWallet, btnMasternode;
    @FXML private Label rewarded, rewarded1;
    @FXML private Label totalTitle, totalSubTitle, totalMainNatureLabel, totalMainUnitLabel, totalSubNatureLabel, totalSubUnitLabel;
    @FXML private Label btnKnowledgeKey;
    @FXML private AnchorPane toolKnowledgekey, toolMiningWallet, toolMasternode, stakingPane;
    @FXML private AnchorPane stakingPane1;
    @FXML private ImageView iconDownDown, iconDownDown1, btnChangeNameWallet, btnChangePasswordWallet, btnBackupWallet, btnRemoveWallet, iconKnowledgeKey, iconMiningWallet, iconMasternode, btnSearch;
    @FXML private TextField searchApisAndTokens;
    @FXML private WalletTooltipController tooltip1Controller, tooltip2Controller, tooltip3Controller, tooltip4Controller;
    @FXML private AnchorPane tooltip1Pane, tooltip2Pane, tooltip3Pane, tooltip4Pane, topTransferPane;

    // reward detail
    @FXML private ScrollPane rewardDetailScroll;
    @FXML private AnchorPane rewardDetailRoot, rewardDetailRoot1, rewardDetailScrollContent;
    @FXML private VBox rewardDetailList;
    @FXML private GridPane rewordDetailPane;

    // tab wallet
    @FXML private GridPane walletTable, toolGroupGrid;
    @FXML private AnchorPane headerWalletItem, walletTableScrollContentPane;
    @FXML private ScrollPane walletTableScrollPane;
    @FXML private Label headerWalletNameLabel, headerWalletMaskLabel, headerWalletAmountLabel, headerWalletTransferLabel;
    @FXML private ImageView imgHeaderWalletSortName, imgHeaderWalletSortMask, imgHeaderWalletSortAmount;
    @FXML private WalletListController walletListController;
    // tab token
    @FXML private GridPane tokenTable, buyMineralButton;
    @FXML private AnchorPane headerTokenItem, tokenTableScrollContentPane;
    @FXML private ScrollPane tokenTableScrollPane;
    @FXML private Label headerTokenNameLabel, headerTokenAmountLabel, buyMineralLabel;
    @FXML private ImageView imgHeaderTokenSortName, imgHeaderTokenSortAmount;
    @FXML private TokenListController tokenListController;
    @FXML private TabMenuController tabMenuController, walletListTabMenuController;

    @FXML private Label totalAssetLabel, myRewardsLabel, rewardedLabel, rewardedLabel1, nowStakingLabel, howApisLabel1, headerTokenTransfer;
    @FXML private Label myRewardsLabel1, nowStakingLabel1;
    @FXML private Label rewardEng11, rewardEng21, rewardEng31, rewardEng41, rewardKor11, rewardKor21, rewardKor31, rewardKor41, rewardKor51;
    @FXML private AnchorPane rewardTooltip1;

    @FXML private AnchorPane createWalletPane;
    private ArrayList<WalletItemModel> walletListModels = new ArrayList<>();
    private ArrayList<WalletItemModel> walletCheckList = new ArrayList<>();
    private ArrayList<BaseFxmlController> rewardDetails = new ArrayList<>();

    private Image imageChangeName, imageChangeNameHover;
    private Image imageChangePassword, imageChangePasswordHover;
    private Image imageChangeProofKey, imageChangeProofKeyHover, imageChangeProofKeyUsed;
    private Image imageBakcup, imageBakcupHover;
    private Image imageRemove, imageRemoveHover;
    private Image imageSortAsc, imageSortDesc, imageSortNone;
    private Image imageMiningGrey, imageMiningRed, imageMiningBlack;
    private Image imageMasternodeGrey, imageMasternodeRed, imageMasternodeBlack;

    private String reward;
    private boolean isScrollingWalletTable;
    private boolean isScrollingTokenTable;


    public WalletController(){
        AppManager.getInstance().guiFx.setWallet(this);
    }


    public void languageSetting() {
        this.buyMineralLabel.textProperty().bind(StringManager.getInstance().common.buyMineralButton);

        this.tabMenuController.addItem(StringManager.getInstance().wallet.tabApis, TAB_TOP_TYPE_APIS);
        this.tabMenuController.addItem(StringManager.getInstance().wallet.tabMineral, TAB_TOP_TYPE_MINERAL);
        this.walletListTabMenuController.addItem(StringManager.getInstance().wallet.tabWallet, TAB_LIST_TYPE_WALLET);
        this.walletListTabMenuController.addItem(StringManager.getInstance().wallet.tabAppAndTokens, TAB_LIST_TYPE_TOKEN);

        this.totalAssetLabel.textProperty().bind(StringManager.getInstance().wallet.totalAsset);
        this.myRewardsLabel.textProperty().bind(StringManager.getInstance().wallet.myRewards);
        this.nowStakingLabel.textProperty().bind(StringManager.getInstance().wallet.nowStaking);
        this.nowStakingLabel1.textProperty().bind(StringManager.getInstance().wallet.nowStaking);
        this.howApisLabel1.textProperty().bind(StringManager.getInstance().wallet.howToGetRewardedWithApis);
        this.btnKnowledgeKey.textProperty().bind(StringManager.getInstance().wallet.knowledgeKeyButton);
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
        this.tooltip3Controller.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.backupWallet);
        this.tooltip4Controller.getTooltipText().textProperty().bind(StringManager.getInstance().wallet.removeWallet);

        this.rewardEng11.textProperty().bind(StringManager.getInstance().wallet.rewardEng1);
        this.rewardEng21.textProperty().bind(StringManager.getInstance().wallet.rewardEng2);
        this.rewardEng31.textProperty().bind(StringManager.getInstance().wallet.rewardEng3);
        this.rewardEng41.textProperty().bind(StringManager.getInstance().wallet.rewardEng4);
        this.rewardKor11.textProperty().bind(StringManager.getInstance().wallet.rewardKor1);
        this.rewardKor21.textProperty().bind(StringManager.getInstance().wallet.rewardKor2);
        this.rewardKor31.textProperty().bind(StringManager.getInstance().wallet.rewardKor3);
        this.rewardKor41.textProperty().bind(StringManager.getInstance().wallet.rewardKor4);
        this.rewardKor51.textProperty().bind(StringManager.getInstance().wallet.rewardKor5);

        StyleManager.fontStyle(buyMineralLabel, StyleManager.Standard.SemiBold12);

        StyleManager.fontStyle(headerWalletTransferLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerWalletAmountLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerWalletNameLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerWalletMaskLabel, StyleManager.Standard.SemiBold12);

        StyleManager.fontStyle(totalMainNatureLabel, StyleManager.Barlow.Regular32);
        StyleManager.fontStyle(totalMainUnitLabel, StyleManager.Barlow.SemiBold12);
        StyleManager.fontStyle(totalSubNatureLabel, StyleManager.Barlow.Regular12);
        StyleManager.fontStyle(totalSubUnitLabel, StyleManager.Barlow.Regular12);
        StyleManager.fontStyle(totalSubTitle, StyleManager.Barlow.Regular12);
        StyleManager.fontStyle(rewardedLabel, StyleManager.Barlow.SemiBold12);
        StyleManager.fontStyle(rewardedLabel1, StyleManager.Barlow.SemiBold12);

        StyleManager.fontStyle(headerTokenNameLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerTokenAmountLabel, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(headerTokenTransfer, StyleManager.Standard.SemiBold12);

        StyleManager.fontStyle(searchApisAndTokens, StyleManager.Standard.SemiBold12);
        StyleManager.fontStyle(totalAssetLabel, StyleManager.Barlow.SemiBold14);
        StyleManager.fontStyle(myRewardsLabel, StyleManager.Barlow.Regular14);
        StyleManager.fontStyle(totalTitle, StyleManager.Barlow.Regular14);
        StyleManager.fontStyle(rewarded, StyleManager.Barlow.Regular32);


        StyleManager.fontStyle(rewardEng11, StyleManager.Standard.Regular12);
        StyleManager.fontStyle(rewardEng21, StyleManager.Standard.SemiBold14);
        StyleManager.fontStyle(rewardEng31, StyleManager.Standard.Regular12);
        StyleManager.fontStyle(rewardEng41, StyleManager.Standard.SemiBold14);
        StyleManager.fontStyle(rewardKor11, StyleManager.Standard.SemiBold14);
        StyleManager.fontStyle(rewardKor21, StyleManager.Standard.Regular12);
        StyleManager.fontStyle(rewardKor31, StyleManager.Standard.SemiBold14);
        StyleManager.fontStyle(rewardKor41, StyleManager.Standard.Regular12);
        StyleManager.fontStyle(rewardKor51, StyleManager.Standard.Regular12);

        StyleManager.fontColorStyle(rewardEng11, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardEng21, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardEng31, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardEng41, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardKor11, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardKor21, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardKor31, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardKor41, StyleManager.AColor.Cf8f8fb);
        StyleManager.fontColorStyle(rewardKor51, StyleManager.AColor.Cf8f8fb);

    }

    public void initImageLoad(){

        this.imageChangeName = ImageManager.btnChangeName;
        this.imageChangeNameHover = ImageManager.btnChangeNameHover;
        this.imageChangePassword = ImageManager.btnChangePassword;
        this.imageChangePasswordHover = ImageManager.btnChangePasswordHover;
        this.imageChangeProofKey = ImageManager.btnChangeProofKey;
        this.imageChangeProofKeyHover = ImageManager.btnChangeProofKeyHover;
        this.imageChangeProofKeyUsed = ImageManager.btnChangeProofKeyUsed;
        this.imageBakcup = ImageManager.btnBackupWallet;
        this.imageBakcupHover = ImageManager.btnBackupWalletHover;
        this.imageRemove = ImageManager.btnRemoveWallet;
        this.imageRemoveHover = ImageManager.btnRemoveWalletHover;

        this.imageSortNone = ImageManager.icSortNONE;
        this.imageSortAsc = ImageManager.icSortASC;
        this.imageSortDesc = ImageManager.icSortDESC;
        this.imageMiningGrey = ImageManager.btnMiningGrey;
        this.imageMiningRed = ImageManager.btnMiningRed;
        this.imageMiningBlack = ImageManager.btnMiningBlack;
        this.imageMasternodeGrey = ImageManager.btnMasternodeGrey;
        this.imageMasternodeRed = ImageManager.btnMasternodeRed;
        this.imageMasternodeBlack = ImageManager.btnMasternodeBlack;

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
        tooltip3Controller.hideTooltip();
        tooltip4Controller.hideTooltip();

        tooltip1Pane.setPrefHeight(0);
        tooltip2Pane.setPrefHeight(0);
        tooltip3Pane.setPrefHeight(0);
        tooltip4Pane.setPrefHeight(0);
    }

    public void showLedgerToolGroup(){
        this.btnChangeNameWallet.setVisible(true);
        this.btnChangePasswordWallet.setVisible(false);
        this.btnBackupWallet.setVisible(false);
        this.btnRemoveWallet.setVisible(true);
        this.toolKnowledgekey.setVisible(true);

        this.toolGroupGrid.getColumnConstraints().get(3).setPrefWidth(0);
        this.toolGroupGrid.getColumnConstraints().get(4).setPrefWidth(0);

        hideToolMining();
        hideToolMasternode();
    }

    public void showToolGroup(boolean showMining, boolean showMaster){
        this.btnChangeNameWallet.setVisible(true);
        this.btnChangePasswordWallet.setVisible(true);
        this.btnBackupWallet.setVisible(true);
        this.btnRemoveWallet.setVisible(true);
        this.toolKnowledgekey.setVisible(true);

        this.toolGroupGrid.getColumnConstraints().get(3).setPrefWidth(40);
        this.toolGroupGrid.getColumnConstraints().get(4).setPrefWidth(40);

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
        this.toolKnowledgekey.setVisible(false);
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
        String address, alias, mask;
        boolean isUsedProofKey = false;

        WalletItemModel walletItemModel = null;

        boolean isStaking = false;
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);

            address = (dataExp.address != null) ? dataExp.address : "";
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
                if (walletListModels.get(m).getAddress().equals(address)) {
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
                walletItemModel.setAlias(alias);
                walletItemModel.setAddress(dataExp.address);
                walletItemModel.setApis(apis);
                walletItemModel.setMineral(mineral);

                /* 비밀번호 변경을 위해 JSON 추가
                 * 렛저 지갑은 JSON을 사용하지 않기 때문에 예외처리
                 */
                if(!AppManager.getInstance().isLedger(dataExp.address)){
                    for(int k=0; k<AppManager.getInstance().getKeystoreList().size(); k++){
                        if(AppManager.getInstance().getKeystoreList().get(k).address.equals(dataExp.address)){
                            walletItemModel.setKeystoreJsonData(AppManager.getInstance().getKeystoreList().get(k).toString());
                            break;
                        }
                    }
                }else{
                    walletItemModel.setKeystoreJsonData("");
                }
                walletItemModel.setMining(address.equals(AppManager.getInstance().getMiningWalletAddress()));
                walletItemModel.setMasterNode(AppManager.getInstance().isMasterNode(dataExp.address));
                walletItemModel.setMask(mask);
                walletItemModel.setUsedProofKey(isUsedProofKey);
            }
            if(address.equals(AppManager.getInstance().getMiningWalletAddress())){
                isStaking = true;
            }
        }

        for(int m=0; m<walletListModels.size(); m++){
            WalletItemModel model = walletListModels.get(m);

            //기존리스트와 새로운리스트 비교
            boolean isOverlap = false;
            for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
                KeyStoreDataExp dataExp = AppManager.getInstance().getKeystoreExpList().get(i);
                if(model.getAddress().equals(dataExp.address)){
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
        stakingPane1.setVisible(isStaking);
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
                if(model.getAddress().equals(dataExp.address)){
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

    public void updateRewardDetailList(){
        // 리스트 수 체크
        ArrayList<KeyStoreDataExp> keyStoreDataExpArray = AppManager.getInstance().getKeystoreExpList();

        // 지갑수가 데이터수보다 많을 경우
        if(keyStoreDataExpArray.size() > rewardDetails.size()) {
            for (int i = rewardDetails.size(); i < keyStoreDataExpArray.size(); i++) {
                addRewardDetail(keyStoreDataExpArray.get(i));
            }
        }

        // 데이터수가 지갑수보다 많을 경우
        if(rewardDetails.size() > keyStoreDataExpArray.size()) {
            for(int i=0; i<rewardDetails.size(); i++ ){
                if(i >= keyStoreDataExpArray.size()){
                    rewardDetails.remove(i);
                    i--;
                }
            }
        }

        // 데이터 대입
        for(int i=0; i<rewardDetails.size(); i++ ){
            RewardDetailItemController controller = (RewardDetailItemController) rewardDetails.get(i).getController();
            controller.setData(keyStoreDataExpArray.get(i));
        }

        // 새로고침
        drawRewardDetailNode();
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
            StyleManager.fontColorStyle(headerWalletNameLabel, StyleManager.AColor.Cb01e1e);
            StyleManager.fontColorStyle(headerWalletMaskLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletAmountLabel, StyleManager.AColor.C999999);
        }else if(sortType == Sort.MASK_ASC || sortType == Sort.MASK_DESC) {
            StyleManager.fontColorStyle(headerWalletNameLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletMaskLabel, StyleManager.AColor.Cb01e1e);
            StyleManager.fontColorStyle(headerWalletAmountLabel, StyleManager.AColor.C999999);
        }else if(sortType == Sort.VALUE_ASC || sortType == Sort.VALUE_DESC) {
            StyleManager.fontColorStyle(headerWalletNameLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletMaskLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerWalletAmountLabel, StyleManager.AColor.Cb01e1e);
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
            StyleManager.fontColorStyle(headerTokenNameLabel, StyleManager.AColor.Cb01e1e);
            StyleManager.fontColorStyle(headerTokenAmountLabel, StyleManager.AColor.C999999);
        }else if(sortType == Sort.VALUE_ASC || sortType == Sort.VALUE_DESC) {
            StyleManager.fontColorStyle(headerTokenNameLabel, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(headerTokenAmountLabel, StyleManager.AColor.Cb01e1e);
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
            tooltip1Pane.setPrefHeight(-1);
        }else if(id.equals("btnChangePasswordWallet")) {
            btnChangePasswordWallet.setImage(imageChangePasswordHover);
            tooltip2Controller.showTooltip();
            tooltip2Pane.setPrefHeight(-1);
        }else if(id.equals("toolKnowledgekey")) {
            // Check knowledge key used
            if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(walletCheckList.get(0).getAddress()))){
                StyleManager.fontColorStyle(btnKnowledgeKey, StyleManager.AColor.C2b8a3e);
                iconKnowledgeKey.setImage(imageChangeProofKeyUsed);
            }else{
                StyleManager.fontColorStyle(btnKnowledgeKey, StyleManager.AColor.C2b2b2b);
                iconKnowledgeKey.setImage(imageChangeProofKeyHover);
            }
            StyleManager.backgroundColorStyle(btnKnowledgeKey, StyleManager.AColor.Cd8d8d8);
        }else if(id.equals("btnBackupWallet")) {
            btnBackupWallet.setImage(imageBakcupHover);
            tooltip3Controller.showTooltip();
            tooltip3Pane.setPrefHeight(-1);
        }else if(id.equals("btnRemoveWallet")) {
            btnRemoveWallet.setImage(imageRemoveHover);
            tooltip4Controller.showTooltip();
            tooltip4Pane.setPrefHeight(-1);
        }else if(id.equals("apisInfoPane1")){
            this.rewardTooltip1.setVisible(true);
        }else if(id.equals("btnToken")){
            this.btnToken.setStyle(new JavaFXStyle(this.btnToken.getStyle()).add("-fx-background-color", "#a61c1c").toString());
        }else if(id.equals("btnCreateWallet")){
            this.btnCreateWallet.setStyle(new JavaFXStyle(this.btnCreateWallet.getStyle()).add("-fx-background-color", "#a61c1c").toString());
        }else if(id.equals("btnMiningWallet")){
            if(AppManager.getInstance().isMining(walletCheckList.get(0).getAddress())) {
                StyleManager.fontColorStyle(btnMiningWallet, StyleManager.AColor.Cb01e1e);
                iconMiningWallet.setImage(imageMiningRed);
            } else {
                StyleManager.fontColorStyle(btnMiningWallet, StyleManager.AColor.C2b2b2b);
                iconMiningWallet.setImage(imageMiningBlack);
            }
            StyleManager.backgroundColorStyle(btnMiningWallet, StyleManager.AColor.Cd8d8d8);
        }else if(id.equals("btnMasternode")){
            if(AppManager.getInstance().isMasterNode(walletCheckList.get(0).getAddress())
                && SystemProperties.getDefault().getMasternodeKey() != null
                && AppManager.getGeneralPropertiesData("masternode_address").equals(walletCheckList.get(0).getAddress())){
                StyleManager.fontColorStyle(btnMasternode, StyleManager.AColor.Cb01e1e);
                iconMasternode.setImage(imageMasternodeRed);
            } else {
                StyleManager.fontColorStyle(btnMasternode, StyleManager.AColor.C2b2b2b);
                iconMasternode.setImage(imageMasternodeBlack);
            }
            StyleManager.backgroundColorStyle(btnMasternode, StyleManager.AColor.Cd8d8d8);
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
        }else if(id.equals("toolKnowledgekey")) {
            if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(walletCheckList.get(0).getAddress()))){
                StyleManager.fontColorStyle(btnKnowledgeKey, StyleManager.AColor.C2b8a3e);
                iconKnowledgeKey.setImage(imageChangeProofKeyUsed);
            }else{
                StyleManager.fontColorStyle(btnKnowledgeKey, StyleManager.AColor.C999999);
                iconKnowledgeKey.setImage(imageChangeProofKey);
            }
            StyleManager.backgroundColorStyle(btnKnowledgeKey, StyleManager.AColor.Cffffff);
        }else if(id.equals("btnBackupWallet")) {
            btnBackupWallet.setImage(imageBakcup);
        }else if(id.equals("btnRemoveWallet")) {
            btnRemoveWallet.setImage(imageRemove);
        }else if(id.equals("apisInfoPane1")){
            this.rewardTooltip1.setVisible(false);
        }else if(id.equals("btnToken")){
            this.btnToken.setStyle(new JavaFXStyle(this.btnToken.getStyle()).add("-fx-background-color", "#b01e1e").toString());
        }else if(id.equals("btnCreateWallet")){
            this.btnCreateWallet.setStyle(new JavaFXStyle(this.btnCreateWallet.getStyle()).add("-fx-background-color", "#b01e1e").toString());
        }else if(id.equals("btnMiningWallet")){
            if(AppManager.getInstance().isMining(walletCheckList.get(0).getAddress())) {
                StyleManager.fontColorStyle(btnMiningWallet, StyleManager.AColor.Cb01e1e);
                iconMiningWallet.setImage(imageMiningRed);
            }else{
                StyleManager.fontColorStyle(btnMiningWallet, StyleManager.AColor.C999999);
                iconMiningWallet.setImage(imageMiningGrey);
            }
            StyleManager.backgroundColorStyle(btnMiningWallet, StyleManager.AColor.Cffffff);
        }else if(id.equals("btnMasternode")){
            if(AppManager.getInstance().isMasterNode(walletCheckList.get(0).getAddress())
                && SystemProperties.getDefault().getMasternodeKey() != null
                && AppManager.getGeneralPropertiesData("masternode_address").equals(walletCheckList.get(0).getAddress())){
                StyleManager.fontColorStyle(btnMasternode, StyleManager.AColor.Cb01e1e);
                iconMasternode.setImage(imageMasternodeRed);
            }else{
                StyleManager.fontColorStyle(btnMasternode, StyleManager.AColor.C999999);
                iconMasternode.setImage(imageMasternodeGrey);
            }
            StyleManager.backgroundColorStyle(btnMasternode, StyleManager.AColor.Cffffff);
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
                public void remove(List<byte[]> removeWalletAddressList) {
                    for(int i=0; i<removeWalletAddressList.size(); i++){
                        if(AppManager.getInstance().isLedger(ByteUtil.toHexString(removeWalletAddressList.get(i)))) {
                            DBManager.getInstance().deleteLedger(removeWalletAddressList.get(i));
                        }
                        for(int j=0; j<AppManager.getInstance().getKeystoreExpList().size(); j++){
                            if(AppManager.getInstance().getKeystoreExpList().get(j).address.equals(ByteUtil.toHexString(removeWalletAddressList.get(i)))){
                                AppManager.getInstance().getKeystoreExpList().remove(j);
                                j--;
                            }
                        }
                        for(int j=0; j<AppManager.getInstance().getKeystoreList().size(); j++){
                            if(AppManager.getInstance().getKeystoreList().get(j).address.equals(ByteUtil.toHexString(removeWalletAddressList.get(i)))){
                                AppManager.getInstance().getKeystoreList().remove(j);
                                j--;
                            }
                        }
                        for(int j=0; j<walletListModels.size(); j++){
                            if(walletListModels.get(j).getAddress().equals(ByteUtil.toHexString(removeWalletAddressList.get(i)))){
                                walletListController.removeWallet(removeWalletAddressList.get(i));
                                walletListModels.remove(j);
                                j--;
                            }
                        }
                        KeyStoreManager.getInstance().deleteKeystore(removeWalletAddressList.get(i));
                    }

                    int size = AppManager.getInstance().keystoreFileReadAll().size();
                    if(size == 0){
                        AppManager.getInstance().guiFx.pageMoveIntro(false);
                    }

                    AppManager.getInstance().guiFx.getWallet().update();
                }
            });
            controller.setModel(walletCheckList.get(0));
            if(AppManager.getInstance().isLedger(walletCheckList.get(0).getAddress())) {
                controller.removeDialog(true);
            }
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
        if (id.equals("toolKnowledgekey")) {

            if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(walletCheckList.get(0).getAddress()))){
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

        // 리워드 상세 리스트 업데이트
        updateRewardDetailList();
    }

    // 지갑리스트의 선택 목록을 초기화 한다.
    public void removeWalletCheckList(){
        this.walletCheckList.clear();
        walletListController.unCheckAll();
        hideToolGroup();
    }
    public void addWalletCheckList(WalletItemModel model){
        String apis = model.getApis().toString();
        String masternodeState = AppManager.getGeneralPropertiesData("masternode_state");
        String masternodeAddress = AppManager.getGeneralPropertiesData("masternode_address");
        walletCheckList.add(model);

        // Change masternode button img & color
        if(AppManager.getInstance().isMasterNode(model.getAddress())
            && SystemProperties.getDefault().getMasternodeKey() != null
            && AppManager.getGeneralPropertiesData("masternode_address").equals(model.getAddress())) {
            StyleManager.fontColorStyle(btnMasternode, StyleManager.AColor.Cb01e1e);
            iconMasternode.setImage(imageMasternodeRed);
        }else{
            StyleManager.fontColorStyle(btnMasternode, StyleManager.AColor.C999999);
            iconMasternode.setImage(imageMasternodeGrey);
        }

        // Show or hide mining & masternode button
        if(apis.equals("0000000000000000000") || apis.equals("0")) {
            showToolGroup(false, false);
            if(AppManager.getInstance().isLedger(model.getAddress())) {
                showLedgerToolGroup();
            }

        } else {
            boolean isPossibleMining = true;
            boolean isPossibleMasternode = false;

            // Set mining condition
            if(AppManager.getInstance().isMasterNode(model.getAddress())) {
                isPossibleMining = false;
            } else if(masternodeState.equals(Integer.toString(AppManager.MnState.REQUEST_MASTERNODE.num))) {
                if(model.getAddress().equals(masternodeAddress)) {
                    isPossibleMining = false;
                }
            }

            // Set masternode condition
            if(AppManager.getInstance().isMining(model.getAddress())) {
                isPossibleMasternode = false;
            } else {
                if (masternodeState != null && !masternodeState.equals("")) {
                    if(masternodeAddress == null || masternodeAddress.equals("")) {
                        if(apis.equals("50000000000000000000000")
                                || apis.equals("200000000000000000000000")
                                || apis.equals("500000000000000000000000")) {
                            isPossibleMasternode = true;
                        }

                    } else {
                        if(masternodeAddress.equals(model.getAddress())) {
                            if(!masternodeState.equals(Integer.toString(AppManager.MnState.CANCEL_MASTERNODE.num))) {
                                isPossibleMasternode = true;
                            }
                        }
                    }
                }
            }

            showToolGroup(isPossibleMining, isPossibleMasternode);
            if(AppManager.getInstance().isLedger(model.getAddress())) {
                showLedgerToolGroup();
            }
        }

        // Check knowledge key used
        if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(walletCheckList.get(0).getAddress()))){
            StyleManager.fontColorStyle(btnKnowledgeKey, StyleManager.AColor.C2b8a3e);
            iconKnowledgeKey.setImage(imageChangeProofKeyUsed);
        }else{
            StyleManager.fontColorStyle(btnKnowledgeKey, StyleManager.AColor.C999999);
            iconKnowledgeKey.setImage(imageChangeProofKey);
        }

        // Check mining and change button img & color
        if(model.getAddress().equals(AppManager.getInstance().getMiningWalletAddress())) {
            StyleManager.fontColorStyle(btnMiningWallet, StyleManager.AColor.Cb01e1e);
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
            StyleManager.fontColorStyle(btnMiningWallet, StyleManager.AColor.C999999);
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

        // reward detail
        initRewardDetail();

        this.tabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                selectedTotalAssetTab(index);
            }
        });
        this.tabMenuController.selectedMenu(TAB_TOP_TYPE_APIS);
        this.tabMenuController.setFontSize14(20);
        this.tabMenuController.setHSpace(20);

        this.walletListTabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                selectedWalletListTab(index);
            }
        });
        this.walletListTabMenuController.selectedMenu(TAB_LIST_TYPE_WALLET);
        this.walletListTabMenuController.setFontSize14(20);
        this.walletListTabMenuController.setHSpace(20);

        this.rewardTooltip1.setVisible(false);

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

                    if(!walletTableScrollPane.isPressed()) {
                        walletTableScrollPane.setVvalue(moveV);
                    }

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

                    if(!tokenTableScrollPane.isPressed()) {
                        tokenTableScrollPane.setVvalue(moveV);
                    }
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


    private boolean isScrolling = false;
    private void initRewardDetail(){
        myRewardsLabel1.textProperty().bind(myRewardsLabel.textProperty());
        rewarded1.textProperty().bind(rewarded.textProperty());
        rewardedLabel1.textProperty().bind(rewardedLabel.textProperty());

        rewardDetailRoot1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                drawRewardDetailNode();
                setVisibleRewardDetail(true);
            }
        });
        rewardDetailRoot.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setVisibleRewardDetail(false);
            }
        });
        rewardDetailScroll.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = rewardDetailScrollContent.getHeight() - rewardDetailScroll.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = rewardDetailScroll.getVvalue() + add;
                        if(moveV > rewardDetailScroll.getVmax()){
                            moveV = rewardDetailScroll.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = rewardDetailScroll.getVvalue() - add;
                        if(moveV < rewardDetailScroll.getVmin()){
                            moveV = rewardDetailScroll.getVmin();
                        }
                    }

                    if(!rewardDetailScroll.isPressed()) {
                        rewardDetailScroll.setVvalue(moveV);
                    }
                }
            }
        });


        ArrayList<KeyStoreDataExp> keyStoreDataExpArray = AppManager.getInstance().getKeystoreExpList();
        for(int i=0; i<keyStoreDataExpArray.size(); i++){
            addRewardDetail(keyStoreDataExpArray.get(i));
        }

        drawRewardDetailNode();
        setVisibleRewardDetail(false);
    }

    private void drawRewardDetailNode(){
        rewardDetailList.getChildren().clear();

        // sort
        Collections.sort(rewardDetails, new SortRewardDesc());

        for(int i=0; i<rewardDetails.size(); i++){
            RewardDetailItemController controller = (RewardDetailItemController)rewardDetails.get(i).getController();
//            if(i%2 == 0){
//                controller.setBackground("#ffffff");
//            }else{
//                controller.setBackground("#f8f8fb");
//            }
            rewardDetailList.getChildren().add(rewardDetails.get(i).getNode());
        }
    }

    /**
     * 리워드 많이 받은 순으로 정렬
     */
    public class SortRewardDesc implements Comparator<BaseFxmlController> {
        @Override
        public int compare(BaseFxmlController o1, BaseFxmlController o2) {
            RewardDetailItemController o1Ctrl = (RewardDetailItemController)o1.getController();
            RewardDetailItemController o2Ctrl = (RewardDetailItemController)o2.getController();
            return o2Ctrl.getReward().compareTo(o1Ctrl.getReward());
        }
    }

    /**
     * 지갑이름 오름 차순으로 정렬
     */
    public class SortAliasAsc implements Comparator<BaseFxmlController> {
        @Override
        public int compare(BaseFxmlController o1, BaseFxmlController o2) {
            RewardDetailItemController o1Ctrl = (RewardDetailItemController)o1.getController();
            RewardDetailItemController o2Ctrl = (RewardDetailItemController)o2.getController();
            return o1Ctrl.getAlias().compareTo(o2Ctrl.getAlias());
        }
    }

    private void addRewardDetail(KeyStoreDataExp data){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("wallet/reward_detail_item.fxml");
            RewardDetailItemController controller = (RewardDetailItemController)fxmlController.getController();
            controller.setData(data);
            rewardDetails.add(fxmlController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void setVisibleRewardDetail(boolean isVisible){
        rewordDetailPane.setVisible(isVisible);

        if(isVisible){
            GridPane.setMargin(rewardDetailScroll, new Insets(24, 0,0,0));
            rewardDetailScroll.setPrefHeight(-1);
            rewardDetailScroll.setVvalue(0);
        }else{
            GridPane.setMargin(rewardDetailScroll, new Insets(0, 0,0,0));
            rewardDetailScroll.setPrefHeight(0);
        }

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
