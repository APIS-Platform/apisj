package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.ContractModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteListController implements Initializable {
    // Contract Address List isSelected Flag
    private static final boolean NOT_SELECTED = false;
    private static final boolean SELECTED = true;

    private boolean listSelectedFlag = NOT_SELECTED;

    @FXML
    private ImageView selectBtn, addrCircleImg;
    @FXML
    private GridPane listGrid;
    @FXML
    private Label name, address;

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
        listGrid.setStyle("-fx-border-color: #f2f2f2;");
        selectBtn.setImage(circleGrey);

        Rectangle clip = new Rectangle( this.addrCircleImg.getFitWidth()-0.5, this.addrCircleImg.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        addrCircleImg.setClip(clip);
    }

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("selectBtn")) {
            setSelected(!listSelectedFlag);

            if(handler != null){
                handler.changed(this, listSelectedFlag);
            }
        }
    }

    // 컨트렉트 수정
    public void onMouseClickedEdit(){
        PopupContractReadWriteModifyController controller = (PopupContractReadWriteModifyController)AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_modify.fxml",1);
        controller.setModel(this.model);
    }

    // 컨트렉트 삭제
    public void onMouseClickedDelete(){
        DBManager.getInstance().deleteContract(this.model.getAddressByte());
        AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_select.fxml", 0);
    }

    public void setModel(ContractModel model) {
        this.model = model;

        name.setText(this.model.getName());
        address.setText(this.model.getAddress());
        try {
            Image image = IdenticonGenerator.generateIdenticonsToImage(this.model.getAddress(), 128, 128);
            if(image != null){
                this.addrCircleImg.setImage(image);
                image = null;
            }
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSelected(boolean selected) {
        if(selected) {
            listGrid.setStyle("-fx-border-color: #f2f2f2; -fx-background-color: #ffffff;");
            selectBtn.setImage(checkCircleRed);
            listSelectedFlag = SELECTED;
        } else {
            listGrid.setStyle("-fx-border-color: #f2f2f2;");
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
}
