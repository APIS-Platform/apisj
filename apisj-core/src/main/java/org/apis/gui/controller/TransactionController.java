package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionController implements Initializable {

    private final String URL_TRANSACTION = "http://35.197.153.64/transactions";

    @FXML
    private WebView webView;
    private WebEngine webEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setTransaction(this);

        webEngine = webView.getEngine();
        webEngine.load(URL_TRANSACTION);
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                        // load success
                        if(newState == Worker.State.SUCCEEDED){
                        }
                    }
                }
        );
    }

    public void update() {
    }
}
