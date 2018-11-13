package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PopupTokenListController extends BasePopupController {

    @FXML
    private Label titleLabel, subTitleLabel, tokenListLabel, addTokenLabel, contractListLabel, editLabel, deleteLabel;

    @FXML
    private VBox list;
    @FXML
    private ScrollPane listPane;
    private PopupTokenAddController addController;

    private void languageSetting(){
        titleLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditTitle);
        subTitleLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditSubTitle);
        tokenListLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditTokenList);
        addTokenLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditAddToken);
        contractListLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditContractList);
        editLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditEdit);
        deleteLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditDelete);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        List<TokenRecord> list = DBManager.getInstance().selectTokens();
        if(list.size() == 0){
            listPane.setVisible(false);
        }else{
            listPane.setVisible(true);
        }
        for(int i=0; i<list.size(); i++){
            addItem(list.get(i));
        }
    }

    @FXML
    public void onMousePressedShowAddPopup() {
        PopupTokenAddController controller = (PopupTokenAddController)PopupManager.getInstance().showMainPopup("popup_token_add.fxml", zIndex);
        controller.setHandler(new PopupTokenAddController.PopupAddTokenImpl() {
            @Override
            public void add() {
                if(handler != null){
                    handler.change();
                }
            }
        });
        this.addController = controller;
    }

    @FXML
    public void onMouseReleasedShowAddPopup(){
        addController.requestFocus();
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
        if(fxid.equals("btnAddToken")){

        }
    }

    public void addItem(TokenRecord record){
        try {
            URL itemUrl = getClass().getClassLoader().getResource("scene/popup/popup_token_list_item.fxml");
            //header
            FXMLLoader loader = new FXMLLoader(itemUrl);
            Node node = loader.load();
            list.getChildren().add(node);
            PopupTokenListItemController controller = (PopupTokenListItemController)loader.getController();
            controller.setData(record);
            controller.setHandler(new PopupTokenListItemController.PopupTokenListImpl() {
                @Override
                public void onClickEdit() {
                    PopupTokenEditController controller = (PopupTokenEditController)PopupManager.getInstance().showMainPopup("popup_token_edit.fxml", zIndex);
                    controller.setData(record);


                    if(handler != null){
                        handler.change();
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            controller.requestFocus();
                        }
                    });
                }

                @Override
                public void onClickDelete() {
                    DBManager.getInstance().deleteToken(record.getTokenAddress());
                    list.getChildren().remove(node);
                    AppManager.getInstance().initTokens();

                    if(handler != null){
                        handler.change();
                    }
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void exit(){
        if(handler != null){
            handler.change();
        }
        super.exit();
    }


    private PopupTokenAddEditImpl handler;
    public void setHandler(PopupTokenAddEditImpl handler){
        this.handler = handler;
    }
    public interface PopupTokenAddEditImpl{
        void change();
    }
}
