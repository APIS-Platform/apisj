package org.apis.gui.controller.module;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabMenuController extends BaseViewController {
    @FXML private AnchorPane rootPane;
    @FXML private HBox menuList;

    private List<TabMenuItemController> items = new ArrayList<>();


    public void addItem(SimpleStringProperty text, int index){
        try {
            BaseFxmlController controller = new BaseFxmlController("module/tab_menu_item.fxml");
            TabMenuItemController itemController = (TabMenuItemController)controller.getController();
            itemController.setIndex(index);
            itemController.getTitle().textProperty().bind(text);
            itemController.setHandler(new TabMenuItemController.TabMenuItemImpl() {
                @Override
                public void onMouseClicked(String text, int index) {
                    for(int i=0; i<items.size(); i++){
                        if(items.get(i).getIndex() == index){
                            items.get(i).stateActive();
                        }else{
                            items.get(i).stateDefault();
                        }
                    }

                    if(handler != null){
                        handler.onMouseClicked(text, index);
                    }
                }
            });
            items.add(itemController);
            menuList.getChildren().add(controller.getNode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectedMenu(int index){
        items.get(index).stateActive();
        if(handler != null){
            handler.onMouseClicked(items.get(index).getTitle().getText(), index);
        }
    }

    private TabMenuImpl handler;
    public void setHandler(TabMenuImpl handler){
        this.handler = handler;
    }

    public void setHSpace(double space) {
        menuList.setSpacing(space);
    }

    public void setFontSize12(){
        setHSpace(20);
        for(TabMenuItemController itemController : items){
            itemController.setFontSize12();
        }
    }
    public void setFontSize14(){
        setHSpace(40);
        for(TabMenuItemController itemController : items){
            itemController.setFontSize14();
        }
    }

    public interface TabMenuImpl {
        void onMouseClicked(String text, int index);
    }
}
