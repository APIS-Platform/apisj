package org.apis.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PopupSyncController extends BasePopupController {
    private ScheduledFuture scheduledFuture;

    @FXML
    private Label label1;
    @FXML
    private Label subMessageLabel;

    private int count = 0;

    public void exit(){
        PopupManager.getInstance().hideMainPopup(0);
        scheduledFuture.cancel(true);
        scheduledFuture = null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setSubMessage(0, 0);

        scheduledFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(count == 4){
                            count = 0;
                        }
                        if(count == 0){
                            label1.textProperty().setValue("APIS node needs to sync, please wait");
                        }else if(count == 1){
                            label1.textProperty().setValue("APIS node needs to sync, please wait.");
                        }else if(count == 2){
                            label1.textProperty().setValue("APIS node needs to sync, please wait..");
                        }else if(count == 3){
                            label1.textProperty().setValue("APIS node needs to sync, please wait...");
                        }
                        count++;
                    }
                    catch (Error | Exception e) { }
            }});
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void setSubMessage(long lastBlock, long bestBlock){
        String sLastBlock = AppManager.comma(""+lastBlock);
        String sBestBlock = AppManager.comma(""+bestBlock);
        this.subMessageLabel.textProperty().setValue("Processing block "+sLastBlock+" of "+sBestBlock+".");
    }

}
