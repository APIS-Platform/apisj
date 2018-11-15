package org.apis.gui.controller.popup;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.AddressLabelController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.manager.*;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class PopupProofOfKnowledgeController extends BasePopupController {
    private String abi =  ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
    private byte[] contractAddress = Hex.decode("1000000000000000000000000000000000037449");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function setterFunction = contract.getByName("registerMask");
    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;

    private Image introNavi,introNaviCircle;
    private Image checkGreen = ImageManager.icCheckGreen;;
    private Image errorRed = ImageManager.icErrorRed;

    @FXML private AnchorPane rootPane;
    @FXML private TabPane tabPane;
    @FXML private ImageView introNaviOne, introNaviTwo;
    @FXML private Label title, subTitle, timeLabel, currentPasswordLabel,newPasswordLabel,transferAmountLabel,detailFeeLabel,withdrawalLabel,afterBalanceLabel,payMsg1,payMsg2 ;
    @FXML private ApisTextFieldController currentFieldController, newFieldController, reFieldController;
    @FXML private Label backBtn1, backBtn2, nextBtn1, payBtn;

    @FXML private GasCalculatorController gasCalculatorMiniController;

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().proofKey.title);
        subTitle.textProperty().bind(StringManager.getInstance().proofKey.subTitle);
        currentPasswordLabel.textProperty().bind(StringManager.getInstance().proofKey.currentPassword);
        newPasswordLabel.textProperty().bind(StringManager.getInstance().proofKey.newPassword);
        payMsg1.textProperty().bind(StringManager.getInstance().proofKey.payMsg1);
        payMsg2.textProperty().bind(StringManager.getInstance().proofKey.payMsg2);

        transferAmountLabel.textProperty().bind(StringManager.getInstance().common.transferAmount);
        detailFeeLabel.textProperty().bind(StringManager.getInstance().common.transferDetailFee);
        withdrawalLabel.textProperty().bind(StringManager.getInstance().common.withdrawal);
        afterBalanceLabel.textProperty().bind(StringManager.getInstance().common.afterBalance);


        backBtn1.textProperty().bind(StringManager.getInstance().common.closeButton);
        backBtn2.textProperty().bind(StringManager.getInstance().common.backButton);
        nextBtn1.textProperty().bind(StringManager.getInstance().common.nextButton);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);


        currentFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
    }
    public void settingLayoutData(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm");
        int utc = TimeZone.getDefault().getRawOffset()/1000/3600;
        this.timeLabel.textProperty().setValue(dateFormat.format(new Date()).toUpperCase()+"(UTC+"+utc+")");
    }

    public void setSelectedTab(int index){
        this.cusorTabIndex = index;

        if(index == 0){
            introNaviOne.setVisible(true);
            introNaviTwo.setVisible(true);

        }else if(index == 1){
            introNaviOne.setVisible(false);
            introNaviTwo.setVisible(false);
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

        introNaviOne.fitWidthProperty().setValue(6);
        introNaviTwo.fitWidthProperty().setValue(6);

        if(step == 0){
            introNaviOne.setImage(introNavi);
            introNaviOne.fitWidthProperty().setValue(24);
        }else if(step == 1){
            introNaviTwo.setImage(introNavi);
            introNaviTwo.fitWidthProperty().setValue(24);
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
            PopupManager.getInstance().showMainPopup(rootPane, "popup_email_address.fxml", 1);
        }else if(id.equals("requestBtn")){

            PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml", 1);
        }else if(id.equals("subTab1")){
            setSelectedTab(1);
            setStep(0);
        }else if(id.equals("subTab2")){
            setSelectedTab(1);
            setStep(2);
        }else if(id.equals("payBtn")){
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");

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
