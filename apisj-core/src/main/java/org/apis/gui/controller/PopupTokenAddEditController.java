package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupTokenAddEditController implements Initializable {

    @FXML
    private Label noBtn, yesBtn;

    @FXML
    private Label titleLabel, subTitleLabel, tokenListLabel, addTokenLabel, contractListLabel, editLabel, deleteLabel, selectLabel;

    public void exit() { AppManager.getInstance().guiFx.hideMainPopup(0); }

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
        selectLabel.textProperty().bind(StringManager.getInstance().popup.tokenAddEditSelect);

        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.yesButton);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        addItem();
        addItem();
        addItem();
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("yesBtn")) {

        }

        else if(fxid.equals("btnAddToken")){
            AppManager.getInstance().guiFx.showMainPopup("popup_add_token.fxml",0);
        }
    }

    public void addItem(){
        try {
            URL itemUrl = getClass().getClassLoader().getResource("scene/popup_token_list.fxml");
            //header
            FXMLLoader loader = new FXMLLoader(itemUrl);
            Node node = loader.load();
            list.getChildren().add(node);
            PopupTokenListController controller = (PopupTokenListController)loader.getController();
            controller.setHandler(new PopupTokenListController.PopupTokenListImpl() {
                @Override
                public void onClickEdit() {
                    AppManager.getInstance().guiFx.showMainPopup("popup_add_token.fxml",0);
                }

                @Override
                public void onClickDelete() {
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
