package org.apis.gui.controller.smartcontract;

import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractCanvasController extends BaseViewController {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    private SmartContractCanvasImpl handler;
    public void setHandler(SmartContractCanvasImpl handler){
        this.handler = handler;
    }
    public interface SmartContractCanvasImpl{
        void onAction();
    }
}
