package org.apis.gui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TransactionRecord;
import javafx.scene.paint.Color;
import org.apis.gui.manager.AppManager;
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
    private Label dropBoxLabel, currentPageNum, totalPageNum;
    @FXML
    private ImageView dropBoxImg;
    @FXML
    private TransactionNativeDetailsController detailsController;

    private Image dropDownImg, dropUpImg;
    private boolean dropBoxMouseFlag = false;
    private boolean dropBoxBtnFlag = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Image Setting
        dropDownImg = new Image("image/btn_drop_down@2x.png");
        dropUpImg = new Image("image/btn_drop_up@2x.png");

        // Tab Added
        AppManager.getInstance().guiFx.setTransactionNative(this);

        // Add Drop List
        addDropList();
        // Add List

        // Initiate DropBox
        dropBoxList.setVisible(false);

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
    }

    public void update() {
        addDropList();
    }

    public void addDropList() {
        addrList.getChildren().clear();
        for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++) {
            addDropItem(i);
        }
    }

    public void addDropItem(int walletNum) {
        //item
        try {
            URL labelUrl = new File("apisj-core/src/main/resources/scene/transaction_native_drop_list.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            addrList.getChildren().add(item);

            TransactionNativeDropListController itemController = (TransactionNativeDropListController)loader.getController();
            itemController.setWalletAddr(AppManager.getInstance().getKeystoreExpList().get(walletNum).address);
            itemController.setAddrMasking(AppManager.getInstance().getKeystoreExpList().get(walletNum).mask);
            itemController.setHandler(new TransactionNativeDropListController.TransactionNativeDropListImpl() {
                @Override
                public void setDropLabel() {
                    pageList.getChildren().clear();
                    String addr = itemController.getWalletAddr()+" ("+itemController.getAddrMasking()+")";
                    dropBoxLabel.setText(addr);
                    dropBoxLabel.setTextFill(Color.web("#ffffff"));
                    dropBoxList.setVisible(false);
                    dropBoxImg.setImage(dropDownImg);

                    //db
                    byte[] address = Hex.decode(itemController.getWalletAddr());
                    List<TransactionRecord> list = DBManager.getInstance().selectTransactions(address);

                    // Pagination
                    pageList.getChildren().clear();
                    int rowSize = 3;
                    int pageSize = 2;
                    int currentPage = 3;
                    int startPage = 1;
                    int endPage = 1;
                    int totalPage = 1;
                    int totalTxCount = list.size();

                    totalPage = totalTxCount / rowSize;
                    if(totalTxCount == 0) {
                        totalPage = 1;
                    }
                    if(totalTxCount % rowSize > 0) {
                        totalPage++;
                    }

                    startPage = currentPage / pageSize;
                    if(currentPage == 0) {
                        startPage = 1;
                    }
                    if(currentPage % pageSize > 0) {
                        startPage = startPage * pageSize + 1;
                    } else {
                        startPage = startPage * pageSize - (pageSize -1);
                    }
                    // Same Operation with below
                    // startPage = (currentPage - 1) / pageSize * pageSize + 1;

                    endPage = startPage + pageSize - 1;
                    if(totalPage < endPage) {
                        endPage = totalPage;
                    }

                    currentPageNum.setText(Integer.toString(currentPage));
                    totalPageNum.setText(Integer.toString(totalPage));

                    list = DBManager.getInstance().selectTransactions(address, rowSize, (currentPage - 1) * rowSize);

                    // Page Num Button Setting
                    addPageList(startPage, endPage);

                    // Add list table
                    addList(list);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPageList(int startPage, int endPage) {
        for(int i = startPage; i < endPage+1; i++) {
            addPageItem(i);
        }
    }

    public void addPageItem(int pageNum) {
        URL labelUrl = null;
        try {
            labelUrl = new File("apisj-core/src/main/resources/scene/transaction_native_page_num.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            pageList.getChildren().add(item);

            TransactionNativePageNumController itemController = (TransactionNativePageNumController)loader.getController();
            itemController.setPageNum(Integer.toString(pageNum));

//            itemController.isSelected(true);
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
                labelUrl = new File("apisj-core/src/main/resources/scene/transaction_native_list_empty.fxml").toURI().toURL();
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
                addItem(list.get(i));
            }
        }
    }

    public void addItem(TransactionRecord record) {
        //item
        try {
            URL labelUrl = new File("apisj-core/src/main/resources/scene/transaction_native_list.fxml").toURI().toURL();
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
            BigInteger fee = gasLimit.multiply(record.getGasPrice()).subtract(record.getMineralUsed());
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
            itemController.setHash(record.getHash());
            itemController.setStatus(record.getStatus(), record.getReceiver());
            itemController.setFrom(record.getSender());
            itemController.setTo(record.getReceiver());
            itemController.setValue(valueString);
            itemController.setFee(feeString);

            itemController.setHandler(new TransactionNativeListController.TransactionNativeListImpl() {
                @Override
                public void showDetails() {
                    txDetailsAnchor.setVisible(true);
                    detailsController.setTxHashLabel(record.getHash());
                    detailsController.setBlockNum(record.getBlock_number());
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

}
