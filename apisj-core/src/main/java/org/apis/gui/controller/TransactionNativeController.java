package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TransactionRecord;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionNativeController implements Initializable {
    @FXML
    private AnchorPane txDetailsAnchor, dropBoxList, txAnchor, dropBoxBtn;
    @FXML
    private VBox txList, addrList;
    @FXML
    private HBox pageList;
    @FXML
    private GridPane firstPageBtn, prePageBtn, nextPageBtn, lastPageBtn;
    @FXML
    private Label dropBoxLabel, currentPageNum, totalPageNum;
    @FXML
    private ImageView dropBoxImg;
    @FXML
    private TransactionNativeDetailsController detailsController;

    // Multilingual Support Label
    @FXML
    private Label selectWalletLabel, transactionsLabel, browseAllTx, pageLabel, hashLabel, blockLabel, fromLabel, toLabel,
                  valueLabel, feeLabel, timeLabel;

    private Image dropDownImg, dropUpImg;
    private boolean dropBoxMouseFlag = false;
    private boolean dropBoxBtnFlag = false;

    // Select the values of each variable
    private int rowSize = 7;
    private int pageSize = 5;
    private int currentPage = 1;
    private int startPage = 1;
    private int endPage = 1;
    private int totalPage = 1;
    private TransactionNativeDropListController selectedItemCtrl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        // Image Setting
        dropDownImg = new Image("image/btn_drop_down@2x.png");
        dropUpImg = new Image("image/btn_drop_up@2x.png");

        // Tab Added
        AppManager.getInstance().guiFx.setTransactionNative(this);

        // Hide Details
        detailsController.setHandler(new TransactionNativeDetailsController.TransactionNativeDetailsImpl() {
            @Override
            public void hideDetails() {
                txDetailsAnchor.setVisible(false);
            }
        });

        dropBoxList.setOnMouseEntered(event -> dropBoxMouseFlag = true);
        dropBoxList.setOnMouseExited(event -> dropBoxMouseFlag = false);
        dropBoxLabel.setOnMouseEntered(event -> dropBoxBtnFlag = true);
        dropBoxLabel.setOnMouseExited(event -> dropBoxBtnFlag = false);
        txAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!dropBoxMouseFlag && !dropBoxBtnFlag) {
                    dropBoxList.setVisible(false);
                    dropBoxImg.setImage(dropDownImg);
                }
            }
        });

        this.init();
    }

    public void init() {
        // Add Address Drop List
        addDropList();

        // Initiate DropBox
        dropBoxList.setVisible(false);
        dropBoxLabel.textProperty().bind(StringManager.getInstance().transaction.dropBoxLabel);
        dropBoxLabel.setOpacity(0.5);

        // Select all
        try {
            selectedItemCtrl = null;
            List<TransactionRecord> list = DBManager.getInstance().selectTransactions(null);
            pageList.getChildren().clear();
            setPaginationVariable(list.size());
        }catch (Exception e){

        }
    }

    public void languageSetting() {
        selectWalletLabel.textProperty().bind(StringManager.getInstance().transaction.selectWalletLabel);
        dropBoxLabel.textProperty().bind(StringManager.getInstance().transaction.dropBoxLabel);
        transactionsLabel.textProperty().bind(StringManager.getInstance().transaction.transactionsLabel);
        browseAllTx.textProperty().bind(StringManager.getInstance().transaction.browseAllTx);
        pageLabel.textProperty().bind(StringManager.getInstance().transaction.pageLabel);
        hashLabel.textProperty().bind(StringManager.getInstance().transaction.hashLabel);
        blockLabel.textProperty().bind(StringManager.getInstance().transaction.blockLabel);
        fromLabel.textProperty().bind(StringManager.getInstance().transaction.fromLabel);
        toLabel.textProperty().bind(StringManager.getInstance().transaction.toLabel);
        valueLabel.textProperty().bind(StringManager.getInstance().transaction.valueLabel);
        feeLabel.textProperty().bind(StringManager.getInstance().transaction.feeLabel);
        timeLabel.textProperty().bind(StringManager.getInstance().transaction.timeLabel);
    }

    public void update() {
        addDropList();
    }

    public void addDropList() {
        addrList.getChildren().clear();
        addDropItemDefault();
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            addDropItem(i);
        }
    }

    public void addDropItemDefault() {
        //item
        try {
            URL labelUrl = getClass().getClassLoader().getResource("scene/transaction_native_drop_list_all.fxml");
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            addrList.getChildren().add(item);

            TransactionNativeDropListAllController itemController = (TransactionNativeDropListAllController)loader.getController();
            itemController.setHandler(new TransactionNativeDropListAllController.TransactionNativeDropListAllImpl() {
                @Override
                public void setDropLabel() {
                    selectedItemCtrl = null;
                    pageList.getChildren().clear();
                    dropBoxLabel.textProperty().unbind();
                    dropBoxLabel.textProperty().bind(StringManager.getInstance().transaction.selectAllLabel);
                    dropBoxLabel.setTextFill(Color.web("#ffffff"));
                    dropBoxLabel.setOpacity(1.0);
                    dropBoxList.setVisible(false);
                    dropBoxImg.setImage(dropDownImg);

                    //db
                    List<TransactionRecord> list = DBManager.getInstance().selectTransactions(null);

                    // Pagination
                    pageList.getChildren().clear();
                    int totalTxCount = list.size();

                    // Refresh Page
                    setPaginationVariable(totalTxCount);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDropItem(int walletNum) {
        //item
        try {
            URL labelUrl = getClass().getClassLoader().getResource("scene/transaction_native_drop_list.fxml");
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            addrList.getChildren().add(item);

            TransactionNativeDropListController itemController = (TransactionNativeDropListController)loader.getController();
            itemController.setWalletAddr(AppManager.getInstance().getKeystoreExpList().get(walletNum).address);
            itemController.setAddrMasking(AppManager.getInstance().getKeystoreExpList().get(walletNum).mask);
            itemController.setHandler(new TransactionNativeDropListController.TransactionNativeDropListImpl() {
                @Override
                public void setDropLabel() {
                    selectedItemCtrl = itemController;

                    pageList.getChildren().clear();
                    String addr = itemController.getWalletAddr()+" ("+itemController.getAddrMasking()+")";
                    dropBoxLabel.textProperty().unbind();
                    dropBoxLabel.setText(addr);
                    dropBoxLabel.setTextFill(Color.web("#ffffff"));
                    dropBoxLabel.setOpacity(1.0);
                    dropBoxList.setVisible(false);
                    dropBoxImg.setImage(dropDownImg);

                    //db
                    byte[] address = Hex.decode(itemController.getWalletAddr());
                    List<TransactionRecord> list = DBManager.getInstance().selectTransactions(address);

                    // Pagination
                    pageList.getChildren().clear();
                    int totalTxCount = list.size();

                    // Refresh Page
                    setPaginationVariable(totalTxCount);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPaginationVariable(int totalTxCount) {
        // Calculate total page number
        totalPage = totalTxCount / rowSize;
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

        refreshPage(1);
    }

    public void addPageList(int startPage, int endPage) {
        pageList.getChildren().clear();
        for(int i = startPage; i < endPage+1; i++) {
            addPageItem(i);
        }
    }

    public void addPageItem(int pageNum) {
        URL labelUrl = null;
        try {
            labelUrl = getClass().getClassLoader().getResource("scene/transaction_native_page_num.fxml");
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            pageList.getChildren().add(item);

            TransactionNativePageNumController itemController = (TransactionNativePageNumController)loader.getController();
            itemController.setPageNum(Integer.toString(pageNum));
            itemController.setHandler(new TransactionNativePageNumController.TransactionNativePageNumImpl() {
                @Override
                public void movePage(int pageNum) {
                    refreshPage(pageNum);
                }
            });
            if(currentPage == pageNum) {
                itemController.isSelected(true);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addList(List<TransactionRecord> list) {
        txList.getChildren().clear();

        if(list.isEmpty()) {
            URL labelUrl = null;
            try {
                labelUrl = getClass().getClassLoader().getResource("scene/transaction_native_list_empty.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                txList.getChildren().add(item);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            for (int i = 0; i < list.size(); i++) {
                if(i % 2 != 0) {
                    addItem(list.get(i), "#f2f2f2");
                } else {
                    addItem(list.get(i), "transparent");
                }
            }
        }
    }

    public void addItem(TransactionRecord record, String bgColor) {
        //item
        try {
            URL labelUrl = getClass().getClassLoader().getResource("scene/transaction_native_list.fxml");
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            txList.getChildren().add(item);

            // Value Setting
            BigInteger value = record.getAmount();
            String valueString;
            if(value.toString().equals("0")) {
                value = new BigInteger("0");
                valueString = value.toString();
            } else {
                valueString = AppManager.addDotWidthIndex(value.toString());
                String[] valueSplit = valueString.split("\\.");

                valueSplit[0] = AppManager.comma(valueSplit[0]);
                valueSplit[1] = valueSplit[1].substring(0, 8);
                valueString = valueSplit[0] + "." + valueSplit[1];
            }

            // Calculate Fee
            BigInteger gasLimit = new BigInteger(Long.toString(record.getGasLimit()));
            BigInteger fee = gasLimit.multiply(record.getGasPrice());//.subtract(record.getMineralUsed());
            String feeString;
            if(fee.toString().indexOf('-') >= 0 || fee.toString().equals("0")) {
                fee = new BigInteger("0");
                feeString = fee.toString();
            } else {
                feeString = AppManager.addDotWidthIndex(fee.toString());
                String[] feeSplit = feeString.split("\\.");

                feeSplit[0] = AppManager.comma(feeSplit[0]);
                feeSplit[1] = feeSplit[1].substring(0, 8);
                feeString = feeSplit[0] + "." + feeSplit[1];
            }

            // Transaction List Setting
            TransactionNativeListController itemController = (TransactionNativeListController)loader.getController();
            itemController.setBgColor(bgColor);
            itemController.setHash(record.getHash());
            itemController.setStatus(record.getStatus(), record.getReceiver());
            itemController.setFrom(record.getSender());
            itemController.setTo(record.getReceiver());
            itemController.setValue(valueString);
            itemController.setFee(feeString);

            itemController.setHandler(new TransactionNativeListController.TransactionNativeListImpl() {
                @Override
                public void showDetails() {
                    // Get original Value
                    BigInteger value = record.getAmount();
                    String valueString;
                    if(value.toString().equals("0")) {
                        value = new BigInteger("0");
                        valueString = value.toString();
                    } else {
                        valueString = AppManager.addDotWidthIndex(value.toString());
                        String[] valueSplit = valueString.split("\\.");
                        valueString = AppManager.comma(valueSplit[0]) + "." + valueSplit[1];
                    }

                    // Calculate original Fee
                    BigInteger gasLimit = new BigInteger(Long.toString(record.getGasLimit()));
                    BigInteger fee = gasLimit.multiply(record.getGasPrice());
                    BigInteger chargedFee = fee.subtract(record.getMineralUsed());
                    String feeString = AppManager.addDotWidthIndex(fee.toString());
                    String chargedFeeString ;
                    if(chargedFee.toString().indexOf('-') >= 0 || chargedFee.toString().equals("0")) {
                        chargedFee = new BigInteger("0");
                        chargedFeeString = chargedFee.toString();
                    } else {
                        chargedFeeString = AppManager.addDotWidthIndex(chargedFee.toString());
                        String[] chargedFeeSplit = chargedFeeString.split("\\.");
                        chargedFeeString = AppManager.comma(chargedFeeSplit[0]) + "." + chargedFeeSplit[1];
                    }

                    // Get Mineral
                    String mnr = AppManager.addDotWidthIndex(record.getMineralUsed().toString());

                    // Get GasPrice
                    BigInteger gasPrice = record.getGasPrice();
                    String quotient = gasPrice.divide(new BigInteger("1000000000")).toString();
                    String remainder = gasPrice.subtract(new BigInteger(quotient).multiply(new BigInteger("1000000000"))).toString();
                    String gasPriceString = (remainder.equals("0")) ? AppManager.comma(quotient) : AppManager.comma(quotient) + "." + remainder;

                    txDetailsAnchor.setVisible(true);
                    detailsController.setTxHashLabel(record.getHash());
                    detailsController.setNonce(record.getNonce());
                    detailsController.setBlockValue(record.getBlock_number());
                    detailsController.setBlockConfirm(AppManager.getInstance().getBestBlock() - record.getBlock_number());
                    detailsController.setFrom(record.getSender());
                    detailsController.setTo(record.getReceiver());
                    detailsController.setContractAddr(record.getContractAddress());
                    detailsController.setValue(valueString);
                    detailsController.setFee(feeString);
                    detailsController.setChargedFee(chargedFeeString);
                    detailsController.setMineral(mnr);
                    detailsController.setGasPrice(gasPriceString);
                    detailsController.setGasLimit(record.getGasLimit());
                    detailsController.setGasUsed(record.getGasUsed());
                    detailsController.setError(record.getError());
                    detailsController.init();
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void dropBoxBtnClicked(InputEvent event) {
        if(dropBoxList.isVisible()) {
            dropBoxList.setVisible(false);
            dropBoxImg.setImage(dropDownImg);
        } else {
            dropBoxList.setVisible(true);
            dropBoxImg.setImage(dropUpImg);
        }
        event.consume();
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
        }
    }

    public void refreshPage(int currentPage) {
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

        byte[] address = null;
        if(selectedItemCtrl != null) {
            address = Hex.decode(selectedItemCtrl.getWalletAddr());
        }
        List<TransactionRecord> list = DBManager.getInstance().selectTransactions(address, rowSize, (currentPage - 1) * rowSize);

        // Page Num Button Setting
        addPageList(startPage, endPage);

        // Add list table
        addList(list);
    }

}
