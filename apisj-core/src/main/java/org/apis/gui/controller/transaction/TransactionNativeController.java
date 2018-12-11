package org.apis.gui.controller.transaction;

import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.db.sql.ContractRecord;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.db.sql.TransactionRecord;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxRowsizeController;
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.LogInfo;
import org.apis.vm.program.InternalTransaction;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionNativeController extends BaseViewController {
    @FXML private AnchorPane txDetailsAnchor, blockInfoAnchor, txAnchor;
    @FXML private VBox txList;
    @FXML private HBox pageList;
    @FXML private GridPane firstPageBtn, prePageBtn, nextPageBtn, lastPageBtn;
    @FXML private Label currentPageNum, totalPageNum, transactionLabel, assetSearchAddressLabel;
    @FXML private TransactionNativeDetailsController detailsController;
    @FXML private TransactionNativeBlockInfoController blockInfoController;
    @FXML private TextField searchTextField;
    @FXML private ApisSelectBoxRowsizeController selectRowSizeController;
    @FXML private GridPane bgBannerPane;
    @FXML private ImageView bgBanner, iconDownDown;
    @FXML private VBox bannerDetailList;
    @FXML private ScrollPane bannerDetailScroll;

    // Multilingual Support Label
    @FXML
    private Label transactionsLabel, browseAllTx, blockNumLabel, pageLabel, hashLabel, stateLabel, fromLabel, toLabel,
                  valueLabel, feeLabel, timeLabel, btnMyAddress, btnRecentAddress;

    // Select the values of each variable
    private int pageSize = 5;
    private int currentPage = 1;
    private int startPage = 1;
    private int endPage = 1;
    private int totalPage = 1;
    private BaseFxmlController listEmptyitem;
    private ArrayList<BaseFxmlController> items = new ArrayList<>();
    private ArrayList<BaseFxmlController> pages = new ArrayList<>();
    private ArrayList<BaseFxmlController> bannerDetails = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Tab Added
        AppManager.getInstance().guiFx.setTransactionNative(this);

        // Hide Details
        detailsController.setHandler(new TransactionNativeDetailsController.TransactionNativeDetailsImpl() {
            @Override
            public void hideDetails() {
                hideDetail();
            }
        });

        selectRowSizeController.setHandler(new ApisSelectBoxRowsizeController.ApisSelectBoxRowsizeImpl() {
            @Override
            public void onChange(int size) {
                refreshPage(1);
            }
        });

        searchTextField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                refreshPage(currentPage);
            }
        });

        // init items max size : 50
        for(int i=0; i<50; i++){
            try {
                items.add(new BaseFxmlController("transaction/transaction_native_list.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // init page group
        for(int i=0; i<pageSize; i++){
            try {
                pages.add(new BaseFxmlController("transaction/transaction_native_page_num.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bgBanner.fitWidthProperty().bind(bgBannerPane.widthProperty());
        bgBanner.fitHeightProperty().bind(bgBannerPane.heightProperty());

        drawBannerDetailNode();

        refreshPage(1);
    }

    public void addBannerDetail(String tokenAddress){
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("transaction/transaction_native_banner_detail.fxml");
            TransactionNativeBannerDetailController controller = (TransactionNativeBannerDetailController)fxmlController.getController();
            controller.setTokenAddress(tokenAddress);
            bannerDetails.add(fxmlController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void drawBannerDetailNode(){
        bannerDetailList.getChildren().clear();
        bannerDetails.clear();

        addBannerDetail("-1"); // APIS
        addBannerDetail("-2"); // MINERAL

        List<TokenRecord> tokens = DBManager.getInstance().selectTokens();
        for(int i=0 ;i<tokens.size(); i++){
            addBannerDetail(ByteUtil.toHexString(tokens.get(i).getTokenAddress()));
        }
        if(tokens.size() == 0){
            iconDownDown.setVisible(false);
        }else{
            iconDownDown.setVisible(true);
        }


        for(int i=0; i<bannerDetails.size(); i++){
            TransactionNativeBannerDetailController controller = (TransactionNativeBannerDetailController)bannerDetails.get(i).getController();
            if(i%2 == 0){
                controller.setBackground("#ffffff");
            }else{
                controller.setBackground("#f8f8fb");
            }
            bannerDetailList.getChildren().add(bannerDetails.get(i).getNode());
        }
    }


    public void languageSetting() {
        transactionsLabel.textProperty().bind(StringManager.getInstance().transaction.transactionsLabel);
        browseAllTx.textProperty().bind(StringManager.getInstance().transaction.browseAllTx);
        pageLabel.textProperty().bind(StringManager.getInstance().transaction.pageLabel);
        blockNumLabel.textProperty().bind(StringManager.getInstance().transaction.blockNumLabel);
        hashLabel.textProperty().bind(StringManager.getInstance().transaction.hashLabel);
        stateLabel.textProperty().bind(StringManager.getInstance().transaction.stateLabel);
        fromLabel.textProperty().bind(StringManager.getInstance().transaction.fromLabel);
        toLabel.textProperty().bind(StringManager.getInstance().transaction.toLabel);
        valueLabel.textProperty().bind(StringManager.getInstance().transaction.valueLabel);
        feeLabel.textProperty().bind(StringManager.getInstance().transaction.feeLabel);
        timeLabel.textProperty().bind(StringManager.getInstance().transaction.timeLabel);
        transactionLabel.textProperty().bind(StringManager.getInstance().transaction.transactionsLabel);
        assetSearchAddressLabel.textProperty().bind(StringManager.getInstance().transaction.assetSearchAddressLabel);
        btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);
    }

    public void update() {
        refreshPage(currentPage);
    }

    public void addPageList(int startPage, int endPage) {
        pageList.getChildren().clear();
        for(int i = startPage; i < endPage+1; i++) {
            pageList.getChildren().add(addPageItem(i));
        }
    }

    public Node addPageItem(int pageNum) {
        TransactionNativePageNumController itemController = (TransactionNativePageNumController)pages.get((pageNum-1)%pageSize).getController();
        itemController.setPageNum(Integer.toString(pageNum));
        itemController.setHandler(new TransactionNativePageNumController.TransactionNativePageNumImpl() {
            @Override
            public void movePage(int pageNum) {
                refreshPage(pageNum);
            }
        });
        if(currentPage == pageNum) {
            itemController.isSelected(true);
        }else{
            itemController.isSelected(false);
        }
        return pages.get((pageNum-1)%pageSize).getNode();
    }

    public void addList(List<TransactionRecord> list) {
        txList.getChildren().clear();

        if(list.isEmpty()) {
            if(listEmptyitem == null){
                try {
                    listEmptyitem = new BaseFxmlController("transaction/transaction_native_list_empty.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            txList.getChildren().add(listEmptyitem.getNode());
        } else {
            for (int i = 0; i < list.size(); i++) {
                txList.getChildren().add(addItem(list.get(i), i));
            }
        }
    }

    public Node addItem(TransactionRecord record2, int index) {
        //item
        final TransactionRecord record = AppManager.getInstance().initTransactionRecord(record2);

        if(record == null){
            return null;
        }

        // Transaction List Setting
        TransactionNativeListController itemController = (TransactionNativeListController)items.get(index).getController();
        itemController.setTransactionRecord(record);
        itemController.setBgColor((index % 2 == 0) ? "#f8f8fb" : "#ffffff");
        itemController.setHandler(new TransactionNativeListController.TransactionNativeListImpl() {
            @Override
            public void searchText(TransactionRecord record, String searchText){
                if(searchText.length() > 0) {
                    searchTextField.setText(searchText);
                    refreshPage(1);
                }else{
                    settingDetail(record);
                }
            }
            @Override
            public void showDetails(TransactionRecord record) {
                settingDetail(record);
            }
        });


        return items.get(index).getNode();
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("firstPageBtn")) {
            refreshPage(1);

        } else if(fxid.equals("prePageBtn")) {
            refreshPage(startPage - pageSize);

        } else if(fxid.equals("nextPageBtn")) {
            refreshPage(endPage + 1);

        } else if(fxid.equals("lastPageBtn")) {
            refreshPage(totalPage);

        } else if(fxid.equals("btnMyAddress")){
            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup(null, "popup_my_address.fxml", 0);
            controller.setHandler(new PopupMyAddressController.PopupMyAddressImpl() {
                @Override
                public void onClickYes(String address) {
                    searchTextField.setText(address);
                    refreshPage(1);
                }
            });

        } else if(fxid.equals("btnRecentAddress")){
            PopupRecentAddressController controller = (PopupRecentAddressController)PopupManager.getInstance().showMainPopup(null, "popup_recent_address.fxml", 0);
            controller.setHandler(new PopupRecentAddressController.PopupRecentAddressImpl() {
                @Override
                public void onMouseClickYes(String address) {
                    searchTextField.setText(address);
                    refreshPage(currentPage);
                }
            });

        }else if(fxid.equals("iconDownDown")){
            double w1w2 = bannerDetailList.getHeight() - bannerDetailScroll.getHeight();
            double size = 80; // 이동하고 싶은 거리 (height)
            double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
            double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue
            double moveV = bannerDetailScroll.getVvalue();
            int index = (int) ( moveV / add );
            if(moveV >= bannerDetailScroll.getVmax()){
                moveV = 0;
            }else{
                moveV = add * (index + 1);
            }
            bannerDetailScroll.setVvalue(moveV);
        }else if(fxid.equals("searchBtn")){
            refreshPage(currentPage);
        }
    }

    public void settingDetail(TransactionRecord record){
// Internal Transaction
        List<InternalTransaction> internalTransactions = AppManager.getInstance().getInternalTransactions(record.getHash());

        String[] internalTxValueString = null, internalTxFrom = null, internalTxTo = null;
        BigInteger[] internalTxValue;

        if(internalTransactions != null && internalTransactions.size() != 0) {
            internalTxValueString = new String[internalTransactions.size()];
            internalTxValue = new BigInteger[internalTransactions.size()];
            internalTxFrom = new String[internalTransactions.size()];
            internalTxTo = new String[internalTransactions.size()];

            for(int i = 0; i < internalTransactions.size(); i++){
                internalTxFrom[i] = ByteUtil.toHexString((byte[]) internalTransactions.get(i).getSender());
                internalTxTo[i] = ByteUtil.toHexString((byte[]) internalTransactions.get(i).getReceiveAddress());
                internalTxValue[i] = ByteUtil.bytesToBigInteger(internalTransactions.get(i).getValue());

                if (internalTxValue[i].compareTo(BigInteger.ZERO) == 0) {
                    internalTxValue[i] = BigInteger.ZERO;
                    internalTxValueString[i] = internalTxValue.toString();
                } else {
                    internalTxValueString[i] = ApisUtil.readableApis(internalTxValue[i], ',', true);
                }
            }
        }

        // Get Event Logs
        List<LogInfo> events = AppManager.getInstance().getEventData(record.getHash());

        String eventLogsString = "", tempString = "";
        if(events != null && events.size() != 0) {
            for (int i = 0; i < events.size(); i++) {
                // Known event processing
                CallTransaction.Contract contract = null;
                CallTransaction.Invocation event = null;

                for (int k = 0; k < 8; k++) {
                    contract = new CallTransaction.Contract(ContractLoader.readABI(k));
                    event = contract.parseEvent(events.get(i));
                    if (event != null) break;
                }

                // Select DBManager to find json if exists
                ContractRecord contractRecord = DBManager.getInstance().selectContract(events.get(i).getAddress());
                if(contractRecord != null) {
                    contract = new CallTransaction.Contract(contractRecord.getAbi());
                    event = contract.parseEvent(events.get(i));
                }

                tempString = "[" + i + "]\t" + "Address: " + ByteUtil.toHexString(events.get(i).getAddress());

                if(event != null) {
                    String inputParams = "";

                    for(int l = 0; l < event.function.inputs.length; l++) {
                        inputParams = inputParams + event.function.inputs[l].getType() + " " + event.function.inputs[l].name;
                        if(l != event.function.inputs.length - 1) {
                            inputParams = inputParams + ", ";
                        }
                    }
                    tempString = tempString + "\n\tFunction: " + event.function.name + "(" + inputParams + ")";
                }

                for (int j = 0; j < events.get(i).getTopics().size(); j++) {
                    if (j == 0) {
                        tempString = tempString + "\n\tTopics   [" + j + "] " + events.get(i).getTopics().get(j);
                    } else {
                        tempString = tempString + "\n\t\t     [" + j + "] " + events.get(i).getTopics().get(j);
                    }
                }

                tempString = tempString + "\n\tData      " + ByteUtil.toHexString(events.get(i).getData());

                if (i == 0) {
                    eventLogsString = eventLogsString + tempString;
                } else {
                    eventLogsString = eventLogsString + "\n\n" + tempString;
                }
            }
        }

        // Get Token Transfer
        ArrayList<Object[]> tokenTransferList = AppManager.getInstance().getTokenTransfer(record.getHash());
        String[] tokenValueString = null, tokenFrom = null, tokenToValue = null;
        BigInteger[] tokenValue;

        if(tokenTransferList != null && tokenTransferList.size() != 0) {
            tokenValueString = new String[tokenTransferList.size()];
            tokenValue = new BigInteger[tokenTransferList.size()];
            tokenFrom = new String[tokenTransferList.size()];
            tokenToValue = new String[tokenTransferList.size()];

            for(int i = 0; i < tokenTransferList.size(); i++){
                tokenFrom[i] = ByteUtil.toHexString((byte[]) tokenTransferList.get(i)[0]);
                tokenToValue[i] = ByteUtil.toHexString((byte[]) tokenTransferList.get(i)[1]);
                tokenValue[i] = (BigInteger) tokenTransferList.get(i)[2];

                if (tokenValue[i].compareTo(BigInteger.ZERO) == 0) {
                    tokenValue[i] = BigInteger.ZERO;
                    tokenValueString[i] = tokenValue.toString();
                } else {
                    tokenValueString[i] = ApisUtil.readableApis(tokenValue[i], ',', true);
                }
            }
        }

        // Get original Value
        BigInteger value = record.getAmount();
        String valueString;
        if(value.compareTo(BigInteger.ZERO) == 0) {
            value = BigInteger.ZERO;
            valueString = value.toString();
        } else {
            valueString = ApisUtil.readableApis(value, ',', true);
        }

        // Calculate original Fee
        BigInteger gasUsed = record.getGasUsed();
        BigInteger fee = gasUsed.multiply(record.getGasPrice());
        BigInteger chargedFee = fee.subtract(record.getMineralUsed());
        String feeString = ApisUtil.readableApis(fee, ',', true);
        String chargedFeeString ;
        if(chargedFee.toString().indexOf('-') >= 0 || chargedFee.toString().equals("0")) {
            chargedFee = BigInteger.ZERO;
            chargedFeeString = chargedFee.toString();
        } else {
            chargedFeeString = ApisUtil.readableApis(chargedFee, ',', true);
        }

        // Get Mineral
        String mnr = ApisUtil.readableApis(record.getMineralUsed(), ',', true);

        // Get GasPrice
        BigInteger gasPrice = record.getGasPrice();
        String quotient = gasPrice.divide(new BigInteger("1000000000")).toString();
        String remainder = gasPrice.subtract(new BigInteger(quotient).multiply(new BigInteger("1000000000"))).toString();
        String gasPriceString = (remainder.equals("0")) ? AppManager.comma(quotient) : AppManager.comma(quotient) + "." + remainder;

        //
        // Detail Controller
        //
        detailsController.setTxHashLabel(record.getHash());
        detailsController.setNonce(record.getNonce());
        long lTime = AppManager.getInstance().getBlockTimeLong(record.getBlock_number());
        String timeToString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(lTime * 1000));
        detailsController.setTime(timeToString);
        detailsController.setBlockValue(record.getBlock_number());
        detailsController.setBlockConfirm(AppManager.getInstance().getBestBlock() - record.getBlock_number());
        detailsController.setFrom(record.getSender());
        detailsController.setTo(record.getReceiver());
        detailsController.setContractAddr(record.getContractAddress());
        if(internalTransactions != null && internalTransactions.size() != 0) {
            detailsController.setInternalTxFromValue(internalTxFrom);
            detailsController.setInternalTxToValue(internalTxTo);
            detailsController.setInternalTxValue(internalTxValueString);
        }
        if(tokenTransferList != null && tokenTransferList.size() != 0) {
            detailsController.setTokenFrom(tokenFrom);
            detailsController.setTokenToValue(tokenToValue);
            detailsController.setTokenValueValue(tokenValueString);
        }
        detailsController.setValue(valueString);
        detailsController.setFee(feeString);
        detailsController.setChargedFee(chargedFeeString);
        detailsController.setMineral(mnr);
        detailsController.setGasPrice(gasPriceString);
        detailsController.setGasLimit(record.getGasLimit());
        detailsController.setGasUsed(record.getGasUsed().longValue());
        detailsController.setEventLogs(eventLogsString);
        detailsController.setOriginalData(record.getData());
        detailsController.setError(record.getError());
        detailsController.init();

        showDetail();
    }

    public void refreshPage(int currentPage) {
        String searchText = searchTextField.getText();

//        if(searchTextField.getText() != null && searchTextField.getText().length() > 0) {
//            try {
//                if ( AddressUtil.isAddress(searchText) ) {
//                    searchText =
//                } else {
//
//                    String mask = AppManager.getInstance().getAddressWithMask(searchTextField.getText());
//                    if(mask != null){
//                        address = Hex.decode(mask);
//                    }else{
//                        address = Hex.decode(searchTextField.getText());
//                    }
//                }
//            }catch (DecoderException e){
//                e.printStackTrace();
//                address = null;
//            }
//        }
//
//        if(address != null){
//            System.out.println("address : " + ByteUtil.toHexString(address));
//        }else{
//            System.out.println("address = null");
//        }

        long totalTxCount = DBManager.getInstance().selectTransactionsAllCount(searchText);
        long rowSize = selectRowSizeController.getSelectSize();

        // Calculate total page number
        totalPage = (int)(totalTxCount / rowSize);
        if(totalTxCount == 0) {
            totalPage = 1;
        }
        if(totalTxCount % rowSize > 0) {
            totalPage++;
        }
        // Overflow exception processing
        if(currentPage > totalPage) {
            currentPage = totalPage;
        }

        if(currentPage > totalPage) {
            currentPage = totalPage;
        }
        startPage = currentPage / pageSize * pageSize;
        if(currentPage % pageSize > 0) {
            startPage++;
        } else {
            startPage = startPage - pageSize + 1;
        }
        if(currentPage <= 0) {
            currentPage = 1;
            startPage = 1;
        }
        // Same Operation with above
        // startPage = (currentPage - 1) / pageSize * pageSize + 1;

        endPage = startPage + pageSize - 1;
        if(totalPage < endPage) {
            endPage = totalPage;
        }
        if(currentPage > endPage) {
            currentPage = endPage;
        }

        this.currentPage = currentPage;
        currentPageNum.setText(Integer.toString(currentPage));
        totalPageNum.setText(Integer.toString(totalPage));

        List<TransactionRecord> list = DBManager.getInstance().selectTransactions(searchText, rowSize, (currentPage - 1) * rowSize);

        // Page Num Button Setting
        addPageList(startPage, endPage);

        // Add list table
        addList(list);

        // Banner Detail Data

        drawBannerDetailNode();
        for(int i=0; i<bannerDetails.size(); i++){
            TransactionNativeBannerDetailController controller = (TransactionNativeBannerDetailController)bannerDetails.get(i).getController();

            if(searchText != null) {
                controller.setAddress(searchText);
            }else{
                controller.setAddress("");
            }
        }
    }

    public void showDetail(){
        txDetailsAnchor.setVisible(true);
    }
    public void hideDetail(){
        txDetailsAnchor.setVisible(false);
    }
    public void showBlockInfo(){
        blockInfoAnchor.setVisible(true);
    }
    public void hideBlockInfo(){
        blockInfoAnchor.setVisible(false);
    }
}
