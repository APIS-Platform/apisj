package org.apis.gui.controller.base;

import javafx.fxml.FXML;
import javafx.scene.Node;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class BasePopupController extends BaseViewController {
    protected Node parentNode;
    protected int zIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex);
        parentRequestFocus();
    }

    public void setZIndex(int zIndex){
        this.zIndex = zIndex;
    }

    public void setParent(Node node){
        this.parentNode = node;
    }

    protected void parentRequestFocus(){
        if(this.parentNode != null){
            this.parentNode.requestFocus();
        }
    }

}
