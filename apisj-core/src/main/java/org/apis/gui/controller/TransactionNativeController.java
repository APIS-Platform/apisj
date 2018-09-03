package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

    public void update() {}

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
