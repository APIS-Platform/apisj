package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.db.sql.AccountRecord;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TransactionRecord;
import org.apis.gui.manager.AppManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionNativeController implements Initializable {
    @FXML
    private AnchorPane txDetailsAnchor;
    @FXML
    private VBox txList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tab Added
        AppManager.getInstance().guiFx.setTransactionNative(this);

        // Add List
        addList();
    }

    public void update() {

        List<AccountRecord> list = DBManager.getInstance().selectAccounts();
        for(int i=0; i<list.size();i++){
            System.out.println("TransactionNativeController getAddress : "+list.get(i).getAddress());
            System.out.println("TransactionNativeController getBalance : "+list.get(i).getBalance());
            System.out.println("TransactionNativeController getTitle : "+list.get(i).getTitle());

            List<TransactionRecord> transactions = DBManager.getInstance().selectTransactions(list.get(i).getAddress());
            for(int j=0; j<transactions.size(); j++){
                System.out.println("transactions.get(j).getHash() : " + transactions.get(j).getHash());
            }
        }



    }

    public void addList() {
        //item
        try {
            URL labelUrl = new File("apisj-core/src/main/resources/scene/transaction_native_list.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(labelUrl);
            AnchorPane item = loader.load();
            txList.getChildren().add(item);

            TransactionNativeListController itemController = (TransactionNativeListController)loader.getController();
            itemController.setHandler(new TransactionNativeListController.TransactionNativeListImpl() {
                @Override
                public void showDetails() {
                    txDetailsAnchor.setVisible(true);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
