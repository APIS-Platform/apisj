package org.apis.gui.controller.popup;

import com.google.zxing.WriterException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.ContractModel;
import org.apis.gui.model.base.BaseModel;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteModifyController extends BasePopupController {

    @FXML private AnchorPane rootPane;
    @FXML private TextField contractAddressTextField, contractNameTextField;
    @FXML private GridPane contractAddressBg;
    @FXML private ImageView addrCircleImg;

    // Multilingual Support Label
    @FXML private Label readWriteTitle, readWriteModify, addrLabel, nameLabel, jsonInterfaceLabel, noBtn, modifyBtn;
    @FXML private TextArea abiTextarea;

    private Image greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");

    private ContractModel model;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        Rectangle clip = new Rectangle( this.addrCircleImg.getFitWidth()-0.5, this.addrCircleImg.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        addrCircleImg.setClip(clip);

        contractAddressTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    contractAddressBg.setStyle(new JavaFXStyle(contractAddressBg.getStyle()).add("-fx-background-color", "#ffffff").toString());
                    contractAddressTextField.setStyle(new JavaFXStyle(contractAddressTextField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                }else{
                    contractAddressBg.setStyle(new JavaFXStyle(contractAddressBg.getStyle()).add("-fx-background-color", "#f2f2f2").toString());
                    contractAddressTextField.setStyle(new JavaFXStyle(contractAddressTextField.getStyle()).add("-fx-background-color", "#f2f2f2").toString());
                }
            }
        });
        contractAddressTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!contractAddressTextField.getText().matches("[0-9a-fA-F]*")) {
                    contractAddressTextField.setText(contractAddressTextField.getText().replaceAll("[^0-9a-fA-F]", ""));
                }

                int maxlangth = 40;
                if(contractAddressTextField.getText().trim().length() > maxlangth){
                    contractAddressTextField.setText(contractAddressTextField.getText().substring(0, maxlangth));
                }

                if(newValue.length() >= maxlangth){
                    Image image = IdenticonGenerator.createIcon(newValue);
                    if(image != null){
                        addrCircleImg.setImage(image);
                        image = null;
                    }
                }else{
                    addrCircleImg.setImage(greyCircleAddrImg);
                }

            }
        });

    }

    public void languageSetting() {
        readWriteTitle.textProperty().bind(StringManager.getInstance().contractPopup.readWriteTitle);
        readWriteModify.textProperty().bind(StringManager.getInstance().contractPopup.readWriteModify);
        addrLabel.textProperty().bind(StringManager.getInstance().contractPopup.addrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().contractPopup.nameLabel);
        contractNameTextField.promptTextProperty().bind(StringManager.getInstance().contractPopup.namePlaceholder);
        jsonInterfaceLabel.textProperty().bind(StringManager.getInstance().contractPopup.jsonInterfaceLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        modifyBtn.textProperty().bind(StringManager.getInstance().contractPopup.modifyBtn);
    }

    public void modifyBtnClicked() {
        String address = contractAddressTextField.getText().trim();
        String name = contractNameTextField.getText().trim();
        String abi = this.abiTextarea.getText().trim();

        if(! address.equals(this.model.getAddress())){
            // 주소가 변경될 경우 기존 데이터 삭제
            DBManager.getInstance().deleteContract(this.model.getAddressByte());
        }
        DBManager.getInstance().updateContract(Hex.decode(address), name,null, abi, null);
        PopupManager.getInstance().hideMainPopup(1);
        PopupContractReadWriteSelectController controller = (PopupContractReadWriteSelectController)PopupManager.getInstance().showMainPopup(rootPane, "popup_contract_read_write_select.fxml", 0);
        controller.setHandler(this.contractSelectHandler);
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (ContractModel)model;

        contractAddressTextField.setText(this.model.getAddress());
        contractNameTextField.setText(this.model.getName());
        abiTextarea.setText(this.model.getAbi());
        addrCircleImg.setImage(this.model.getIdenticon());
        Image image = IdenticonGenerator.createIcon(this.model.getAddress());
        if(image != null){
            addrCircleImg.setImage(image);
            image = null;
        }
    }

    PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl contractSelectHandler;
    public void setContractSelectHandler(PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl contractSelectHandler) {
        this.contractSelectHandler = contractSelectHandler;
    }

}
