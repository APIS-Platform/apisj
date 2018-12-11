package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StyleManager;
import org.apis.gui.model.ContractModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteListController extends BaseViewController{
    // Contract Address List isSelected Flag
    private static final boolean NOT_SELECTED = false;
    private static final boolean SELECTED = true;

    private boolean listSelectedFlag = NOT_SELECTED;

    @FXML private ImageView selectBtn, addrCircleImg;
    @FXML private GridPane listGrid;
    @FXML private Label name, address;

    private Image circleGrey, checkCircleRed;

    private ContractModel model;
    private PopupContractReadWriteListImpl handler;

    public PopupContractReadWriteListImpl getHandler() {
        return handler;
    }

    public void setHandler(PopupContractReadWriteListImpl handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        circleGrey = new Image("image/btn_circle_none@2x.png");
        checkCircleRed = new Image("image/btn_circle_red@2x.png");

        listSelectedFlag = NOT_SELECTED;
        listGrid.setStyle("-fx-border-color: #f8f8fb;");
        selectBtn.setImage(circleGrey);

        Rectangle clip = new Rectangle( this.addrCircleImg.getFitWidth()-0.5, this.addrCircleImg.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        addrCircleImg.setClip(clip);
    }

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("selectBtn") || fxid.equals("listGrid")) {
            setSelected(!listSelectedFlag);
            if(handler != null){
                handler.changed(this, listSelectedFlag);
            }
            event.consume();
        }
    }

    // 컨트렉트 수정
    public void onMouseClickedEdit(){
        PopupContractReadWriteModifyController controller = (PopupContractReadWriteModifyController)PopupManager.getInstance().showMainPopup(listGrid, "popup_contract_read_write_modify.fxml",1);
        controller.setModel(this.model);
        controller.setContractSelectHandler(this.contractSelectHandler);
    }

    // 컨트렉트 삭제
    public void onMouseClickedDelete(){
        DBManager.getInstance().deleteContract(this.model.getAddressByte());
        PopupContractReadWriteSelectController controller = (PopupContractReadWriteSelectController)PopupManager.getInstance().showMainPopup(listGrid, "popup_contract_read_write_select.fxml", 0);
        controller.setHandler(this.contractSelectHandler);
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (ContractModel)model;

        name.setText(this.model.getName());
        address.setText(this.model.getAddress());
        addrCircleImg.setImage(this.model.getIdenticon());

        if(AppManager.getInstance().isFrozen(address.getText())) {
            StyleManager.fontColorStyle(address, StyleManager.AColor.C4871ff);
        }
    }

    public void setSelected(boolean selected) {
        if(selected) {
            listGrid.setStyle("-fx-border-color: #f8f8fb; -fx-background-color: #ffffff;");
            selectBtn.setImage(checkCircleRed);
            listSelectedFlag = SELECTED;
        } else {
            listGrid.setStyle("-fx-border-color: #f8f8fb;");
            selectBtn.setImage(circleGrey);
            listSelectedFlag = NOT_SELECTED;
        }
    }

    public ContractModel getModel() {
        return this.model;
    }

    public interface PopupContractReadWriteListImpl{
        void changed(PopupContractReadWriteListController obj, boolean isSelected);
    }

    PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl contractSelectHandler;
    public void setContractSelectHandler(PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl contractSelectHandler) {
        this.contractSelectHandler = contractSelectHandler;
    }
}
