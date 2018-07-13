package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.WalletModel;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class WalletController  implements Initializable {

    @FXML
    private Label totalAssetLabel1, totalAssetLabel2, totalAssetLabel3;
    @FXML
    private Pane totalAssetLinePane1, totalAssetLinePane2, totalAssetLinePane3;

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
    private WalletListController walletListBodyController;

    private ArrayList<Label> totalAssetLabels = new ArrayList<>();
    private ArrayList<Pane> totalAssetLines = new ArrayList<>();
    private ArrayList<Label> walletListLabels = new ArrayList<>();
    private ArrayList<Pane> walletListLines = new ArrayList<>();
    private ArrayList<ImageView> tooltips = new ArrayList<>();
    private ArrayList<WalletItemModel> walletListModels = new ArrayList<>();

    private Image imageChangeName, imageChangeNameHover;
    private Image imageChangePassword, imageChangePasswordHover;
    private Image imageBakcup, imageBakcupHover;
    private Image imageRemove, imageRemoveHover;

    private WalletModel model = new WalletModel();

    public void initImageLoad(){

        this.imageChangeName = new Image("image/btn_wright@2x.png");
        this.imageChangeNameHover = new Image("image/btn_wright_hover@2x.png");
        this.imageChangePassword = new Image("image/btn_unlock@2x.png");
        this.imageChangePasswordHover = new Image("image/btn_unlock_hover@2x.png");
        this.imageBakcup = new Image("image/btn_share@2x.png");
        this.imageBakcupHover = new Image("image/btn_share_hover@2x.png");
        this.imageRemove = new Image("image/btn_remove@2x.png");
        this.imageRemoveHover = new Image("image/btn_remove_hover@2x.png");
    }

    public void initLayoutTotalAsset(){
        this.totalMainNatureLabel.textProperty().bind(this.model.totalMainNaturalProperty());
        this.totalMainDecimalLabel.textProperty().bind(this.model.totalMainDecimalProperty());
        this.totalSubNatureLabel.textProperty().bind(this.model.totalSubNaturalProperty());
        this.totalSubDecimalLabel.textProperty().bind(this.model.totalSubDecimalProperty());
    }

    public void initLayoutTotalAssetTab(){
        this.totalAssetLabels.add(this.totalAssetLabel1);
        this.totalAssetLabels.add(this.totalAssetLabel2);
        this.totalAssetLabels.add(this.totalAssetLabel3);

        this.totalAssetLines.add(this.totalAssetLinePane1);
        this.totalAssetLines.add(this.totalAssetLinePane2);
        this.totalAssetLines.add(this.totalAssetLinePane3);

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

    public void selectedWalletListTab(int index){

        // change header active
        setWalletListTabActive(index);
    }

    public void hideToolTipAll(){
        for(int i=0; i<this.tooltips.size(); i++){
            this.tooltips.get(i).setVisible(false);
        }
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
            AppManager.getInstance().guiFx.showPopup("popup_change_wallet_name.fxml", 0);
        }else if(id.equals("btnChangePasswordWallet")) {
            AppManager.getInstance().guiFx.showPopup("popup_change_wallet_password.fxml", 0);
        }else if(id.equals("btnBackupWallet")) {
            AppManager.getInstance().guiFx.showPopup("popup_backup_wallet.fxml", 0);
        }else if(id.equals("btnRemoveWallet")) {
            AppManager.getInstance().guiFx.showPopup("popup_remove_wallet.fxml", 0);
        }else if(id.equals("btnMiningWallet")){
            AppManager.getInstance().guiFx.showPopup("popup_mining_wallet.fxml", 0);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // init image loading
        initImageLoad();

        initLayoutTotalAssetTab();
        initLayoutWalletListTab();

        initLayoutTotalAsset();

        selectedTotalAssetTab(0);
        selectedWalletListTab(0);


        walletListModels.add(new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS));
        walletListModels.add(new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS));
        walletListModels.add(new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS));
        walletListModels.add(new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS));
        walletListModels.add(new WalletItemModel().setHeaderUnitType(WalletItemModel.UNIT_TYPE_APIS));

        BigInteger bigTotalApis = new BigInteger("0");
        BigInteger bigTotalMineral = new BigInteger("0");
        String apis, mineral;
        String[] apisSplit, mineralSplit;
        for(int i=0; i<walletListModels.size(); i++){
            apis = "1000000000000000000";
            mineral = "4000000000000";
            bigTotalApis = bigTotalApis.add(new BigInteger(apis));
            bigTotalMineral = bigTotalMineral.add(new BigInteger(mineral));
            apisSplit = AppManager.addDotWidthIndex(apis).split("\\.");
            mineralSplit = AppManager.addDotWidthIndex(mineral).split("\\.");
            walletListModels.get(i).aliasProperty().setValue("Walelt Alias "+(i+1));
            walletListModels.get(i).addressProperty().setValue("ABCDEF123456ABCDEF123456ABCDEF123456");

            walletListModels.get(i).apisNaturalProperty().setValue(apisSplit[0]);
            walletListModels.get(i).apisDecimalProperty().setValue("."+apisSplit[1]);
            walletListModels.get(i).mineralNaturalProperty().setValue(mineralSplit[0]);
            walletListModels.get(i).mineralDecimalProperty().setValue("."+mineralSplit[1]);
            walletListBodyController.addCreateWalletListItem(walletListModels.get(i));
        }
        apisSplit = AppManager.addDotWidthIndex(bigTotalApis.toString()).split("\\.");
        mineralSplit = AppManager.addDotWidthIndex(bigTotalMineral.toString()).split("\\.");
        model.setTotalType(WalletModel.UNIT_TYPE_APIS);
        model.setTotalApisNatural(apisSplit[0]);
        model.setTotalApisDecimal("."+apisSplit[1]);
        model.setTotalMineralNatural(mineralSplit[0]);
        model.setTotalMineralDecimal("."+mineralSplit[1]);

        walletListBodyController.setOpenItem(0);
    }
}
