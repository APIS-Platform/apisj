package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.ContractModel;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteModifyController implements Initializable {

    @FXML
    private TextField contractAddressTextField, contractNameTextField;
    @FXML
    private GridPane contractAddressBg;
    @FXML
    private ImageView addrCircleImg;

    // Multilingual Support Label
    @FXML
    private Label readWriteTitle, readWriteModify, addrLabel, nameLabel, jsonInterfaceLabel, noBtn, modifyBtn;
    @FXML
    private TextArea abiTextarea;
    private Image greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");

    private ContractModel model;
    public void exit() { AppManager.getInstance().guiFx.hideMainPopup(1); }

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
                int maxlangth = 40;
                if(contractAddressTextField.getText().length() > maxlangth){
                    contractAddressTextField.setText(contractAddressTextField.getText().substring(0, maxlangth));
                }

                if(newValue.length() >= maxlangth){
                    try {
                        Image image = IdenticonGenerator.generateIdenticonsToImage(newValue, 128, 128);
                        if(image != null){
                            addrCircleImg.setImage(image);
                            image = null;
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
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
        String address = contractAddressTextField.getText();
        String name = contractNameTextField.getText();
        String abi = this.abiTextarea.getText();

        if(! address.equals(this.model.getAddress())){
            // 주소가 변경될 경우 기존 데이터 삭제
            DBManager.getInstance().deleteContract(this.model.getAddressByte());
        }
        DBManager.getInstance().updateContract(Hex.decode(address), name,null, abi, null);
        AppManager.getInstance().guiFx.hideMainPopup(1);
        PopupContractReadWriteSelectController controller = (PopupContractReadWriteSelectController)AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_select.fxml", 0);
        controller.setHandler(this.contractSelectHandler);
        System.out.println("this.contractSelectHandler : "+this.contractSelectHandler);
    }

    public void setModel(ContractModel model) {
        this.model = model;

        contractAddressTextField.setText(this.model.getAddress());
        contractNameTextField.setText(this.model.getName());
        abiTextarea.setText(this.model.getAbi());

        try {
            Image image = IdenticonGenerator.generateIdenticonsToImage(this.model.getAddress(), 128, 128);
            if(image != null){
                addrCircleImg.setImage(image);
                image = null;
            }
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl contractSelectHandler;
    public void setContractSelectHandler(PopupContractReadWriteSelectController.PopupContractReadWriteSelectImpl contractSelectHandler) {
        this.contractSelectHandler = contractSelectHandler;
    }

}
