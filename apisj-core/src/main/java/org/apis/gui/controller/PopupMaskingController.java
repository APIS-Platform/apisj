package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.core.CallTransaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.HttpRequestManager;
import org.apis.gui.manager.StringManager;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupMaskingController implements Initializable {
    private String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"owners\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"getCountOfDefaultFeeChangeConfirms\",\"outputs\":[{\"name\":\"count\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"ownerChangeCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint24\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_domainId\",\"type\":\"uint32\"}],\"name\":\"getRegistrationFee\",\"outputs\":[{\"name\":\"registrationFee\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"faces\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"revokeDomainRegistrationConfirmation\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint24\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"ownerChangeConfirmations\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_defaultFee\",\"type\":\"uint256\"}],\"name\":\"registerDefaultFeeChange\",\"outputs\":[{\"name\":\"id\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint32\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"domainConfigChangeConfirms\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint24\"}],\"name\":\"requirementChanges\",\"outputs\":[{\"name\":\"requirement\",\"type\":\"uint16\"},{\"name\":\"executed\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"revokeDefaultFeeChangeConfirmation\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"executeDefaultFeeChange\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"isOwner\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_domainAddress\",\"type\":\"address\"},{\"name\":\"_domainFee\",\"type\":\"uint256\"},{\"name\":\"_foundationFee\",\"type\":\"uint256\"},{\"name\":\"_needApproval\",\"type\":\"bool\"},{\"name\":\"_isOpened\",\"type\":\"bool\"}],\"name\":\"registerDomain\",\"outputs\":[{\"name\":\"id\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"confirmDefaultFeeChange\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"domainConfigChangeCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_addressMask\",\"type\":\"string\"}],\"name\":\"getFaceAddress\",\"outputs\":[{\"name\":\"faceAddress\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_owners\",\"type\":\"address[]\"},{\"name\":\"_required\",\"type\":\"uint16\"}],\"name\":\"init\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint24\"}],\"name\":\"ownerChanges\",\"outputs\":[{\"name\":\"owner\",\"type\":\"address\"},{\"name\":\"isAdd\",\"type\":\"bool\"},{\"name\":\"executed\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_faceAddress\",\"type\":\"address\"},{\"name\":\"_name\",\"type\":\"string\"},{\"name\":\"_domainId\",\"type\":\"uint32\"}],\"name\":\"registerMask\",\"outputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"defaultFee\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"confirmDomainConfigChange\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"domainContractAddresses\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"confirmDomainRegistration\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"isDefaultFeeChangeConfirmed\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"revokeDomainConfigChangeConfirmation\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_changeId\",\"type\":\"uint24\"}],\"name\":\"confirmRequirementChange\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint24\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"requirementChangeConfirmations\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_domainId\",\"type\":\"uint32\"}],\"name\":\"getDomainInfo\",\"outputs\":[{\"name\":\"domainId\",\"type\":\"uint32\"},{\"name\":\"domainAddress\",\"type\":\"address\"},{\"name\":\"domainName\",\"type\":\"string\"},{\"name\":\"domainFee\",\"type\":\"uint256\"},{\"name\":\"foundationFee\",\"type\":\"uint256\"},{\"name\":\"needApproval\",\"type\":\"bool\"},{\"name\":\"isOpened\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_changeId\",\"type\":\"uint24\"}],\"name\":\"getCountOfRequirementChangeConfirms\",\"outputs\":[{\"name\":\"count\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"isDomainRegistered\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"defaultFeeChangeCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"registerOwnerRemove\",\"outputs\":[{\"name\":\"ownerChangeId\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"MAX_NAME_LENGTH\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_changeId\",\"type\":\"uint24\"}],\"name\":\"revokeOwnerChangeConfirmation\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"maskNames\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"name\":\"defaultFeeChanges\",\"outputs\":[{\"name\":\"registered\",\"type\":\"bool\"},{\"name\":\"defaultFee\",\"type\":\"uint256\"},{\"name\":\"executed\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"domainCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"name\":\"domainRegistrations\",\"outputs\":[{\"name\":\"domainAddress\",\"type\":\"address\"},{\"name\":\"domainFee\",\"type\":\"uint256\"},{\"name\":\"foundationFee\",\"type\":\"uint256\"},{\"name\":\"domainName\",\"type\":\"string\"},{\"name\":\"isOpened\",\"type\":\"bool\"},{\"name\":\"needApproval\",\"type\":\"bool\"},{\"name\":\"executed\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"domainConfigs\",\"outputs\":[{\"name\":\"domainAddress\",\"type\":\"address\"},{\"name\":\"domainFee\",\"type\":\"uint256\"},{\"name\":\"foundationFee\",\"type\":\"uint256\"},{\"name\":\"domainName\",\"type\":\"string\"},{\"name\":\"needApproval\",\"type\":\"bool\"},{\"name\":\"isOpened\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_domainId\",\"type\":\"uint256\"},{\"name\":\"_domainFee\",\"type\":\"uint256\"},{\"name\":\"_foundationFee\",\"type\":\"uint256\"},{\"name\":\"_needApproval\",\"type\":\"bool\"},{\"name\":\"_isOpened\",\"type\":\"bool\"}],\"name\":\"registerDomainConfigChange\",\"outputs\":[{\"name\":\"id\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"registerOwnerAdd\",\"outputs\":[{\"name\":\"ownerChangeId\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_changeId\",\"type\":\"uint24\"}],\"name\":\"getCountOfOwnerChangeConfirms\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint32\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"defaultFeeChangeConfirms\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_changeId\",\"type\":\"uint24\"}],\"name\":\"revokeRequirementChangeConfirmation\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_changeId\",\"type\":\"uint24\"}],\"name\":\"confirmOwnerChange\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"MAX_OWNER_COUNT\",\"outputs\":[{\"name\":\"\",\"type\":\"uint16\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"required\",\"outputs\":[{\"name\":\"\",\"type\":\"uint16\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"getCountOfDomainRegistrationConfirms\",\"outputs\":[{\"name\":\"count\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"sizeOfDomain\",\"outputs\":[{\"name\":\"size\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint32\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"domainRegistrationConfirms\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"name\":\"domainConfigChanges\",\"outputs\":[{\"name\":\"domainFee\",\"type\":\"uint256\"},{\"name\":\"foundationFee\",\"type\":\"uint256\"},{\"name\":\"domainId\",\"type\":\"uint256\"},{\"name\":\"registered\",\"type\":\"bool\"},{\"name\":\"isOpened\",\"type\":\"bool\"},{\"name\":\"needApproval\",\"type\":\"bool\"},{\"name\":\"executed\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"domainRegistrationCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"masks\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_faceAddress\",\"type\":\"address\"}],\"name\":\"getMaskName\",\"outputs\":[{\"name\":\"maskName\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_id\",\"type\":\"uint32\"}],\"name\":\"getCountOfDomainConfigChangeConfirms\",\"outputs\":[{\"name\":\"count\",\"type\":\"uint32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"requirementChangeCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint24\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_faceAddress\",\"type\":\"address\"}],\"name\":\"getMaskHash\",\"outputs\":[{\"name\":\"maskHash\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_requirement\",\"type\":\"uint16\"}],\"name\":\"registerRequirementChange\",\"outputs\":[{\"name\":\"requirementChangeId\",\"type\":\"uint24\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_newAddress\",\"type\":\"address\"}],\"name\":\"handOverMask\",\"outputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"fallback\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"face\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"mask\",\"type\":\"string\"}],\"name\":\"MaskAddition\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"mask\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"oldAddress\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"newAddress\",\"type\":\"address\"}],\"name\":\"MaskHandOver\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainRegistrationId\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"domainAddress\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"domainFee\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"foundationFee\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"domainName\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"isOpened\",\"type\":\"bool\"}],\"name\":\"DomainRegistrationSubmission\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainRegistrationId\",\"type\":\"uint256\"}],\"name\":\"DomainRegistrationConfirmation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainRegistrationId\",\"type\":\"uint256\"}],\"name\":\"DomainRegistrationRevocation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainRegistrationId\",\"type\":\"uint256\"}],\"name\":\"DomainRegistrationExecution\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"defaultFeeChangeId\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"aApis\",\"type\":\"uint256\"}],\"name\":\"DefaultFeeChangeSubmission\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"defaultFeeChangeId\",\"type\":\"uint256\"}],\"name\":\"DefaultFeeChangeConfirmation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"defaultFeeChangeId\",\"type\":\"uint256\"}],\"name\":\"DefaultFeeChangeRevocation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"defaultFeeChangeId\",\"type\":\"uint256\"}],\"name\":\"DefaultFeeChangeExecution\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainConfigChangeId\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"domainId\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"domainFee\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"foundationFee\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"isOpened\",\"type\":\"bool\"}],\"name\":\"DomainConfigChangeSubmission\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainConfigChangeId\",\"type\":\"uint256\"}],\"name\":\"DomainConfigChangeConfirmation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainConfigChangeId\",\"type\":\"uint256\"}],\"name\":\"DomainConfigChangeRevocation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"domainConfigChangeId\",\"type\":\"uint256\"}],\"name\":\"DomainConfigChangeExecution\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"ownerChangeId\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"message\",\"type\":\"string\"}],\"name\":\"OwnerChangeSubmission\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"changeId\",\"type\":\"uint256\"}],\"name\":\"OwnerChangeConfirmation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"changeId\",\"type\":\"uint256\"}],\"name\":\"OwnerChangeRevocation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"}],\"name\":\"OwnerAddition\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"}],\"name\":\"OwnerRemoval\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"requiredChangeId\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"require\",\"type\":\"uint256\"}],\"name\":\"RequirementChangeSubmission\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"changeId\",\"type\":\"uint256\"}],\"name\":\"RequirementChangeConfirmation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"changeId\",\"type\":\"uint256\"}],\"name\":\"RequirementChangeRevocation\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"changeId\",\"type\":\"uint256\"}],\"name\":\"RequirementChangeExecution\",\"type\":\"event\"}]";
    private byte[] contractAddress = Hex.decode("1000000000000000000000000000000000037449");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function setterFunction = contract.getByName("registerMask");
    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;

    private Image tab1On, tab1Off, tab2On, tab2Off;
    private Image introNavi,introNaviCircle;
    private Image downGreen = new Image("image/ic_check_green@2x.png");
    private Image downRed = new Image("image/ic_error_red@2x.png");

    @FXML private Pane tab1Line, tab2Line;
    @FXML private ImageView tab1Icon, tab2Icon;
    @FXML private Label tab1Label, tab2Label;
    @FXML private TabPane tabPane;
    @FXML private ImageView introNaviOne, introNaviTwo, introNaviThree, introNaviFour, addressMsgIcon;
    @FXML private TextField commercialDomainTextField, emailTextField, registerMaskingIdTextField;
    @FXML private TextArea commercialDomainMessage;
    @FXML private Label
            titleLabel, tab1TitleLabel, tab1SubTitleLabel, addressLabel, addressMsgLabel,
            tab2TitleLabel, tab2SubTitleLabel, domainLabel, domainMsgLabel,
            tab3TitleLabel, tab3SubTitleLabel, idLabel,
            successLabel, walletAddressLabel, aliasLabel, totalFeeLabel, payerLabel, payMsg1, payMsg2,
            tab5TitleLabel, tab5SubTitleLabel, tab7TitleLabel, tab7SubTitleLabel, tabComercialDomain1, tabPublicDomain1, tabComercialDomain2, tabPublicDomain2,
            cDomainMsg1, cDomainMsg2, cDomainMsg3, cDomainMsg4,
            pDomainMsg1, pDomainMsg2, pDomainMsg3, pDomainMsg4,
            tab6TitleLabel, tab6SubTitleLabel, cDomainLabel,
            pDomainLabel, purposeDomainLabel, selectDomainLabel,
            backBtn1, backBtn2, backBtn3, backBtn6, backBtn8, nextBtn1, nextBtn2, nextBtn3, payBtn, suggestingBtn, requestBtn,
            selectWalletAddress, maskId, maskValue
    ;

    @FXML private ApisSelectBoxController selectAddressController, selectDomainController, selectPayerController;
    @FXML private GasCalculatorMiniController gasCalculatorMiniController;


    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }
    public void languageSetting() {
        titleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTitle);
        tab1TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterMask);
        tab1SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasPlaseCheckAddress);
        addressLabel.textProperty().bind(StringManager.getInstance().popup.maskingAddress);
        addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg);
        tab1Label.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterMask);
        tab2Label.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);

        tab2TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterMask);
        tab2SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasPlaseSelectDomain);
        domainLabel.textProperty().bind(StringManager.getInstance().popup.maskingDomain);

        tab3TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterMask);
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
        pDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestDomain2);
        purposeDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestPurposeDomain);

        suggestingBtn.textProperty().bind(StringManager.getInstance().common.suggestingButton);
        requestBtn.textProperty().bind(StringManager.getInstance().common.requestButton);
    }
    public void settingLayoutData(){
        // step 1. 변경하려는 지갑주소 선택
        String address = selectAddressController.getAddress();
        String mineral = selectAddressController.getMineral();
        String mask = AppManager.getInstance().getMaskWithAddress(address);
        if(mask != null && mask.length() > 0){
            //이미존재
            setAddressState(false);
        }else{
            setAddressState(true);
        }

        // step 2. 도메인 선택
        String domain = selectDomainController.getDomain();
        String apis = selectDomainController.getValueApis();
        BigInteger value = selectDomainController.getValueApisToBigInt();
        setDomainMsgState(domain, apis);

        // step 3. 아이디 작성
        String maskingId = registerMaskingIdTextField.getText();
        Object[] args = new Object[3];
        args[0] = Hex.decode(address);   //_faceAddress
        args[1] = maskingId;   //_name
        args[2] = new BigInteger(selectDomainController.getDomainId());   //_domainId
        String preGasUsed = Long.toString(AppManager.getInstance().getPreGasUsed(abi, Hex.decode(address), contractAddress, value, setterFunction.name, args));
        gasCalculatorMiniController.setMineral(new BigInteger(mineral));
        gasCalculatorMiniController.setGasLimit(preGasUsed);


        selectWalletAddress.setText(address);
        selectDomainLabel.setText(domain);
        maskId.setText(maskingId+domain);
        maskValue.setText(apis+"APIS");
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

            String domain = commercialDomainTextField.getText();
            String message = commercialDomainMessage.getText();
            String email = emailTextField.getText();

            try {
                String response = HttpRequestManager.sendRequestPublicDomain(domain, message, email);
                System.out.println("response > \n" + response);
            }catch (MalformedURLException e){
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            AppManager.getInstance().guiFx.showMainPopup("popup_success.fxml", 1);
        }else if(id.equals("subTab1")){
            setSelectedTab(1);
            setStep(0);
        }else if(id.equals("subTab2")){
            setSelectedTab(1);
            setStep(2);
        }else if(id.equals("payBtn")){

            String faceAddress = selectAddressController.getAddress();
            String name = registerMaskingIdTextField.getText();
            String domainId = selectDomainController.getDomainId();

            String address = selectPayerController.getAddress();
            BigInteger value = selectDomainController.getValueApisToBigInt();
            String gasLimit = gasCalculatorMiniController.getGasLimit().toString();
            String gasPrice = gasCalculatorMiniController.getGasPrice().toString();

            Object[] args = new Object[3];
            args[0] = Hex.decode(faceAddress);   //_faceAddress
            args[1] = name;   //_name
            args[2] = new BigInteger(domainId);   //_domainId
            byte[] functionCallBytes = setterFunction.encode(args);

            System.out.println("payBtn faceAddress : "+faceAddress);
            System.out.println("payBtn name : "+name);
            System.out.println("payBtn domainId : "+domainId);
            System.out.println("payBtn address : "+address);
            System.out.println("payBtn value : "+value.toString());
            System.out.println("payBtn gasLimit : "+gasLimit);
            System.out.println("payBtn gasPrice : "+gasPrice);
            for(int i=0; i<args.length; i++){
                System.out.println("args["+i+"] : "+args[i]);
            }


            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) AppManager.getInstance().guiFx.showMainPopup("popup_contract_warning.fxml", 0);
            controller.setData(address, value.toString(), gasPrice, gasLimit, contractAddress, functionCallBytes);
            controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                @Override
                public void success() {
                    System.out.println("success");
                }
            });

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
        selectAddressController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {
            }
        });

        selectDomainController.init(ApisSelectBoxController.SELECT_BOX_TYPE_DOMAIN);
        selectDomainController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {
            }
        });

        registerMaskingIdTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                settingLayoutData();
            }
        });

        selectPayerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ONLY_ADDRESS);
        selectPayerController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                //settingLayoutData();
            }

            @Override
            public void onMouseClick() {
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

        this.commercialDomainTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // Focus in Function
                if(newValue) {
                    commercialDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
                // Focus out Function
                else {
                    commercialDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        });

        setSelectedTab(0);
        setStep(0);
    }

    public void setSelectAddress(String address){
        selectAddressController.selectedItemWithAddress(address);
        settingLayoutData();
    }
    public void setSelectWalletId(String id) {
        selectAddressController.selectedItemWithWalletId(id);
        settingLayoutData();
    }

    private void setAddressState(boolean isAvailable){
        addressMsgLabel.textProperty().unbind();
        if(isAvailable){
            addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg);
            this.addressMsgLabel.setTextFill(Color.web("#36b25b"));
            this.addressMsgIcon.setImage(downGreen);
            this.nextBtn1.setStyle(new JavaFXStyle(nextBtn1.getStyle()).add("-fx-background-color","#910000").toString());
        }else{
            addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg2);
            this.addressMsgLabel.setTextFill(Color.web("#910000"));
            this.addressMsgIcon.setImage(downRed);
            this.nextBtn1.setStyle(new JavaFXStyle(nextBtn1.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }
    }

    private void setDomainMsgState(String domain, String apis){
        domainMsgLabel.setText(domain + " is "+apis+"APIS");
    }
}
