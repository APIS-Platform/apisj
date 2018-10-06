package org.apis.gui.manager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.apis.gui.controller.base.BasePopupController;

import java.io.IOException;

public class PopupManager {
    // singleton
    private static PopupManager ourInstance = new PopupManager();
    public static PopupManager getInstance() {
        return ourInstance;
    }
    private PopupManager() { }

    // field
    private GridPane mainPopup0, mainPopup1, mainPopup2, mainPopup3;

    // method
    public Object showMainPopup(String fxmlName, int zIndex){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("scene/popup/"+fxmlName));
            Node popup = loader.load();
            BasePopupController controller = loader.getController();
            controller.setZIndex(zIndex);
            popup.setVisible(true);
            if(zIndex == -1) {
                this.mainPopup0.getChildren().clear();
                this.mainPopup0.add(popup , 0 ,0 );
                this.mainPopup0.setVisible(true);
            } else if(zIndex == 0){
                this.mainPopup1.getChildren().clear();
                this.mainPopup1.add(popup , 0 ,0 );
                this.mainPopup1.setVisible(true);
            }else if(zIndex == 1){
                this.mainPopup2.getChildren().clear();
                this.mainPopup2.add(popup , 0 ,0 );
                this.mainPopup2.setVisible(true);
            }else if(zIndex == 2){
                this.mainPopup3.getChildren().clear();
                this.mainPopup3.add(popup , 0 ,0 );
                this.mainPopup3.setVisible(true);
            }
            return controller;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void hideMainPopup(int zIndex){
        if(zIndex == -1) {
            this.mainPopup0.getChildren().clear();
            this.mainPopup0.setVisible(false);
        } else if(zIndex == 0){
            this.mainPopup1.getChildren().clear();
            this.mainPopup1.setVisible(false);
        }else if(zIndex == 1){
            this.mainPopup2.getChildren().clear();
            this.mainPopup2.setVisible(false);
        }else if(zIndex == 2){
            this.mainPopup3.getChildren().clear();
            this.mainPopup3.setVisible(false);
        }
    }

    public void setMainPopup0(GridPane popup){ this.mainPopup0 = popup; }
    public void setMainPopup1(GridPane popup){ this.mainPopup1 = popup; }
    public void setMainPopup2(GridPane popup){ this.mainPopup2 = popup; }
    public void setMainPopup3(GridPane popup){ this.mainPopup3 = popup; }
}
