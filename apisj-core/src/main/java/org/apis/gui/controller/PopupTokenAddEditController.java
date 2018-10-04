package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PopupTokenAddEditController extends BasePopupController {

    @FXML
    private Label titleLabel, subTitleLabel, tokenListLabel, addTokenLabel, contractListLabel, editLabel, deleteLabel;

    @FXML
    private VBox list;

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
        for(int i=0; i<list.size(); i++){
            addItem(list.get(i));
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
        if(fxid.equals("btnAddToken")){
            PopupAddTokenController controller = (PopupAddTokenController)PopupManager.getInstance().showMainPopup("popup_add_token.fxml", zIndex);
        }
    }

    public void addItem(TokenRecord record){
        try {
            URL itemUrl = getClass().getClassLoader().getResource("scene/popup_token_list.fxml");
            //header
            FXMLLoader loader = new FXMLLoader(itemUrl);
            Node node = loader.load();
            list.getChildren().add(node);
            PopupTokenListController controller = (PopupTokenListController)loader.getController();
            controller.setData(record);
            controller.setHandler(new PopupTokenListController.PopupTokenListImpl() {
                @Override
                public void onClickEdit() {
                    PopupEditTokenController controller = (PopupEditTokenController)PopupManager.getInstance().showMainPopup("popup_edit_token.fxml", zIndex);
                    controller.setData(record);
                }

                @Override
                public void onClickDelete() {
                    DBManager.getInstance().deleteToken(record.getTokenAddress());
                    list.getChildren().remove(node);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
