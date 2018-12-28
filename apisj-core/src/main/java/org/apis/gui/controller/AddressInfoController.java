package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddressInfoController extends BaseViewController {
    private final int ITEM_TYPE_LABEL = 0;
    private final int ITEM_TYPE_HEXDATA = 1;
    private final int ITEM_TYPE_LINK = 2;
    private final int ITEM_TYPE_READONLY_TEXT = 3;
    private final int ITEM_TYPE_READONLY_HEXDATA = 4;

    @FXML private VBox list;
    @FXML private AnchorPane rootPane;
    @FXML private GridPane rootGridPane;
    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane listPane, placeHolderPane;
    @FXML private TextField searchText;
    @FXML private Label title, subTitle, btnSearch, noResultTitle, noResultSubTitle;

    private boolean isScrolling = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        languageSetting();

        AppManager.settingTextFieldStyle(searchText);

        searchText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                searchAddress(searchText.getText());
            }
        });
        searchText.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE){
                    exit();
                }
            }
        });
        rootPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE){
                    exit();
                }
            }
        });
        rootGridPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE){
                    exit();
                }
            }
        });

        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = list.getHeight() - bodyScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = bodyScrollPane.getVvalue() + add;
                        if(moveV > bodyScrollPane.getVmax()){
                            moveV = bodyScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = bodyScrollPane.getVvalue() - add;
                        if(moveV < bodyScrollPane.getVmin()){
                            moveV = bodyScrollPane.getVmin();
                        }
                    }

                    if(!bodyScrollPane.isPressed()) {
                        bodyScrollPane.setVvalue(moveV);
                    }
                }
            }
        });

        listPane.setVisible(false);
        placeHolderPane.setVisible(true);

    }

    public void languageSetting(){
        title.textProperty().bind(StringManager.getInstance().addressInfo.title);
        subTitle.textProperty().bind(StringManager.getInstance().addressInfo.subTitle);
        btnSearch.textProperty().bind(StringManager.getInstance().common.searchButton);
        noResultTitle.textProperty().bind(StringManager.getInstance().addressInfo.noResultTitle);
        noResultSubTitle.textProperty().bind(StringManager.getInstance().addressInfo.noResultSubTitle);
        searchText.promptTextProperty().bind(StringManager.getInstance().addressInfo.searchPlaceHolder);
    }

    public void update(){

    }

    public void exit(){
        if(handler != null){
            handler.close();
        }
    }

    public void searchAddress(String addressAndMask){
        if(addressAndMask.indexOf("0x") == 0 && addressAndMask.indexOf("@") == -1){
            addressAndMask = addressAndMask.replaceAll("0x","");
        }

        list.getChildren().clear();

        String address = "", mask = "";
        if(AddressUtil.isAddress(addressAndMask)){
            address = addressAndMask;
            mask = AppManager.getInstance().getMaskWithAddress(address);
        }else{
            address = AppManager.getInstance().getAddressWithMask(addressAndMask);
            mask = addressAndMask;
        }

        if(address != null && address.length() == 40) {

            BigInteger txNonce = AppManager.getInstance().getTxNonce(address);
            BigInteger apis = AppManager.getInstance().getBalance(address);
            BigInteger mineral = AppManager.getInstance().getMineral(address);
            BigInteger tokenValue = BigInteger.ZERO;
            List<TokenRecord> tokenRecordList = DBManager.getInstance().selectTokens();

            addItem("Address", address, ITEM_TYPE_READONLY_HEXDATA);
            addItem("Mask", mask, ITEM_TYPE_READONLY_TEXT);
            addItem("Transaction", txNonce + " txns", ITEM_TYPE_READONLY_TEXT);
            addItem("APIS", ApisUtil.readableApis(apis, ',', true) + " APIS", ITEM_TYPE_READONLY_TEXT);
            addItem("MNR", ApisUtil.readableApis(mineral, ',', true) + " MNR", ITEM_TYPE_READONLY_TEXT);
            addItem("MasterNode", ""+AppManager.getInstance().isMasterNode(address), ITEM_TYPE_READONLY_TEXT);

            for (int i = 0; i < tokenRecordList.size(); i++) {
                tokenValue = AppManager.getInstance().getTokenValue(ByteUtil.toHexString(tokenRecordList.get(i).getTokenAddress()), address);
                addItem(tokenRecordList.get(i).getTokenName(), ApisUtil.readableApis(tokenValue, ',', true) + " " + tokenRecordList.get(i).getTokenSymbol(), ITEM_TYPE_READONLY_TEXT);
            }
            listPane.setVisible(true);
            placeHolderPane.setVisible(false);
        }else{

            listPane.setVisible(false);
            placeHolderPane.setVisible(true);
        }

    }

    public void addItem(String title, String text, int type){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("popup/popup_address_info_item.fxml");
            AddressInfoItemController itemController = (AddressInfoItemController)fxmlController.getController();
            itemController.setTitle(title);
            if(type == ITEM_TYPE_LABEL) {
                itemController.setLabel(text);
            }else if(type ==  ITEM_TYPE_HEXDATA){
                itemController.setHexData(text);
            }else if(type ==  ITEM_TYPE_READONLY_TEXT){
                itemController.setReadOnly(text);
            }else if(type ==  ITEM_TYPE_READONLY_HEXDATA){
                itemController.setReadOnlyHexData(text);
            }

            if(list.getChildren().size() % 2 == 0){
                itemController.setBackground("#f8f8fb");
            }else{
                itemController.setBackground("#ffffff");
            }

            list.getChildren().add(fxmlController.getNode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestFocus(){
        searchText.requestFocus();
    }

    @FXML
    public void onMouseClicked(){
        searchAddress(searchText.getText());
    }

    private AddressInfoImpl handler;
    public void setHandler(AddressInfoImpl handler){
        this.handler = handler;
    }
    public interface AddressInfoImpl{
        void close();
    }
}
