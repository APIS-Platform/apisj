package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupMaskingController implements Initializable {

    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;

    private Image tab1On, tab1Off, tab2On, tab2Off;
    private Image introNavi,introNaviCircle;
    @FXML
    private Pane tab1Line, tab2Line;
    @FXML
    private ImageView tab1Icon, tab2Icon;
    @FXML
    private Label tab1Label, tab2Label;
    @FXML
    private TabPane tabPane;
    @FXML
    private ImageView introNaviOne, introNaviTwo, introNaviThree, introNaviFour;
    @FXML
    private Label backBtn1, backBtn2, backBtn3, backBtn6, backBtn8, nextBtn1, nextBtn2, nextBtn3, payBtn, suggestingBtn, requestBtn;

    @FXML
    private Label titleLabel,
            tab1TitleLabel, tab1SubTitleLabel, addressLabel, addressMsgLabel,
            tab2TitleLabel, tab2SubTitleLabel, domainLabel, domainMsgLabel,
            tab3TitleLabel, tab3SubTitleLabel, idLabel,
            successLabel, walletAddressLabel, aliasLabel, totalFeeLabel, payerLabel, payMsg1, payMsg2,
            tab5TitleLabel, tab5SubTitleLabel, tab7TitleLabel, tab7SubTitleLabel, tabComercialDomain1, tabPublicDomain1, tabComercialDomain2, tabPublicDomain2,
            cDomainMsg1, cDomainMsg2, cDomainMsg3, cDomainMsg4,
            pDomainMsg1, pDomainMsg2, pDomainMsg3, pDomainMsg4,
            tab6TitleLabel, tab6SubTitleLabel, cDomainLabel,
            tab8TitleLabel, tab8SubTitleLabel, pDomainLabel, purposeDomainLabel

    ;

    @FXML
    private ApisSelectBoxController selectAddressController, selectDomainController;


    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }
    public void languageSetting() {
        titleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTitle);
        tab1TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterAlias);
        tab1SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasPlaseCheckAddress);
        addressLabel.textProperty().bind(StringManager.getInstance().popup.maskingAddress);
        addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg);
        tab1Label.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterAlias);
        tab2Label.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);

        tab2TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterAlias);
        tab2SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasPlaseSelectDomain);
        domainLabel.textProperty().bind(StringManager.getInstance().popup.maskingDomain);
        domainMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasDomainMsg);

        tab3TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterAlias);
        tab3SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasPlaseInputId);
        idLabel.textProperty().bind(StringManager.getInstance().popup.maskingId);


        successLabel.textProperty().bind(StringManager.getInstance().popup.maskingSuccess);
        walletAddressLabel.textProperty().bind(StringManager.getInstance().popup.maskingWalletAddress);
        aliasLabel.textProperty().bind(StringManager.getInstance().popup.maskingAlias);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().popup.maskingTotalFee);
        payerLabel.textProperty().bind(StringManager.getInstance().popup.maskingPayer);
        payMsg1.textProperty().bind(StringManager.getInstance().popup.maskingPayMsg1);
        payMsg2.textProperty().bind(StringManager.getInstance().popup.maskingPayMsg2);

        backBtn1.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn2.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn3.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn6.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn8.textProperty().bind(StringManager.getInstance().common.backButton);
        nextBtn1.textProperty().bind(StringManager.getInstance().common.nextButton);
        nextBtn2.textProperty().bind(StringManager.getInstance().common.nextButton);
        nextBtn3.textProperty().bind(StringManager.getInstance().common.nextButton);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);

        tab5TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);
        tab5SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRegisterDomainMsg);
        tab7TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);
        tab7SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRegisterDomainMsg);
        tabComercialDomain1.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomain);
        tabPublicDomain1.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomain);
        tabComercialDomain2.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomain);
        tabPublicDomain2.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomain);
        cDomainMsg1.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg1);
        cDomainMsg2.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg2);
        cDomainMsg3.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg3);
        cDomainMsg4.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg4);
        pDomainMsg1.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg1);
        pDomainMsg2.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg2);
        pDomainMsg3.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg3);
        pDomainMsg4.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg4);

        tab6TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRequestCommercialDomain);
        tab6SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRequestCommercialDomainMsg);
        cDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingRequestCommercialDomain2);
        tab8TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestDomain);
        tab8SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestDomainMsg);
        pDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestDomain2);
        purposeDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestPurposeDomain);

        suggestingBtn.textProperty().bind(StringManager.getInstance().common.suggestingButton);
        requestBtn.textProperty().bind(StringManager.getInstance().common.requestButton);
    }

    public void setSelectedTab(int index){
        this.cusorTabIndex = index;

        tab1Line.setVisible(false);
        tab1Icon.setImage(tab1Off);
        tab1Label.setStyle("-fx-font-family: 'Open Sans Regular'; -fx-font-size:12px; ");
        tab1Label.setTextFill(Color.web("#999999"));

        tab2Line.setVisible(false);
        tab2Icon.setImage(tab2Off);
        tab2Label.setStyle("-fx-font-family: 'Open Sans Regular'; -fx-font-size:12px; ");
        tab2Label.setTextFill(Color.web("#999999"));

        if(index == 0){
            tab1Icon.setImage(tab1On);
            tab1Line.setVisible(true);
            tab1Label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; ");
            tab1Label.setTextFill(Color.web("#910000"));

            introNaviOne.setVisible(true);
            introNaviTwo.setVisible(true);
            introNaviThree.setVisible(true);
            introNaviFour.setVisible(true);

        }else if(index == 1){
            tab2Icon.setImage(tab2On);
            tab2Line.setVisible(true);
            tab2Label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; ");
            tab2Label.setTextFill(Color.web("#910000"));

            introNaviOne.setVisible(false);
            introNaviTwo.setVisible(false);
            introNaviThree.setVisible(false);
            introNaviFour.setVisible(false);
        }

        setStep(0);
    }
    public void setStep(int step){
        this.cusorStepIndex = step;

        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(this.cusorTabIndex*4 + step);

        setNavi(this.cusorStepIndex );

        if(this.cusorTabIndex*4 + step < 0){
            exit();
        }
    }

    public void setNavi(int step){
        introNaviOne.setImage(introNaviCircle);
        introNaviTwo.setImage(introNaviCircle);
        introNaviThree.setImage(introNaviCircle);
        introNaviFour.setImage(introNaviCircle);

        introNaviOne.fitWidthProperty().setValue(6);
        introNaviTwo.fitWidthProperty().setValue(6);
        introNaviThree.fitWidthProperty().setValue(6);
        introNaviFour.fitWidthProperty().setValue(6);

        if(step == 0){
            introNaviOne.setImage(introNavi);
            introNaviOne.fitWidthProperty().setValue(24);
        }else if(step == 1){
            introNaviTwo.setImage(introNavi);
            introNaviTwo.fitWidthProperty().setValue(24);
        }else if(step == 2){
            introNaviThree.setImage(introNavi);
            introNaviThree.fitWidthProperty().setValue(24);
        }else if(step == 3){
            introNaviFour.setImage(introNavi);
            introNaviFour.fitWidthProperty().setValue(24);
        }
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        System.out.println("id : "+id );
        if(id.equals("tab1")){
            setSelectedTab(0);
        }else if(id.equals("tab2")){
            setSelectedTab(1);
        }else if(id.indexOf("backBtn") >= 0){
            setStep(this.cusorStepIndex-1);
        }else if(id.indexOf("nextBtn") >= 0){
            setStep(this.cusorStepIndex+1);
        }else if(id.equals("suggestingBtn")){
            AppManager.getInstance().guiFx.showMainPopup("popup_email_address.fxml", 1);
        }else if(id.equals("requestBtn")){
            AppManager.getInstance().guiFx.showMainPopup("popup_success.fxml", 1);
        }else if(id.equals("subTab1")){
            setSelectedTab(1);
            setStep(0);
        }else if(id.equals("subTab2")){
            setSelectedTab(1);
            setStep(2);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        tab1On = new Image("image/ic_registeralias_red@2x.png");
        tab1Off = new Image("image/ic_registeralias_grey@2x.png");
        tab2On = new Image("image/ic_registeralias_red@2x.png");
        tab2Off = new Image("image/ic_registeralias_grey@2x.png");
        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");

        selectAddressController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        selectAddressController.setHandler(new ApisSelectBoxController.SelectEvent() {
            @Override
            public void onSelectItem() {

            }
        });

        selectDomainController.init(ApisSelectBoxController.SELECT_BOX_TYPE_DOMAIN);
        selectDomainController.setHandler(new ApisSelectBoxController.SelectEvent() {
            @Override
            public void onSelectItem() {

            }
        });

        // Tab Pane Direction Key Block
        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.LEFT
                    || event.getCode() == KeyCode.RIGHT
                    || event.getCode() == KeyCode.UP
                    || event.getCode() == KeyCode.DOWN) {
                if(tabPane.isFocused()){
                    event.consume();
                }else{
                }
            }
        });

        setSelectedTab(0);
        setStep(0);
    }
}
