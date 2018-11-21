package org.apis.gui.controller.smartcontrect;

import com.google.zxing.WriterException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Ellipse;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractFreezerController extends BaseViewController {
    @FXML private Label ctrtInputBtn;
    @FXML private ApisSelectBoxController contractCnstSelectorController;
    @FXML private GasCalculatorController tab3GasCalculatorController;
    @FXML private AnchorPane ctrtAddrText, ctrtAddrSelect;
    @FXML private TextField ctrtAddrTextField;
    @FXML private ImageView ctrtAddrImg;

    private Image greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");

    // Contract Constructor Address Input Flag
    private boolean isMyAddressSelected = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Making indent image circular
        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterY(12);
        ellipse.setCenterX(12);
        ctrtAddrImg.setClip(ellipse);

        contractCnstSelectorController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
        contractCnstSelectorController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {

            }

            @Override
            public void onMouseClick() {
                if(handler != null){
                    handler.onAction();
                }
            }
        });



        tab3GasCalculatorController.setHandler(new GasCalculatorController.GasCalculatorImpl() {
            @Override
            public void gasLimitTextFieldFocus(boolean isFocused) {
                if(handler != null){
                    handler.onAction();
                }
            }

            @Override
            public void gasLimitTextFieldChangeValue(String oldValue, String newValue){
                if(handler != null){
                    handler.onAction();
                }
            }

            @Override
            public void gasPriceSliderChangeValue(int value) {
                if(handler != null){
                    handler.onAction();
                }
            }

            @Override
            public void changeGasPricePopup(boolean isVisible){

            }
        });


        ctrtAddrTextField.focusedProperty().addListener(ctrtFocusListener);
        ctrtAddrTextField.textProperty().addListener(ctrtKeyListener);
    }


    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();


        if(fxid.equals("ctrtInputBtn")) {
            if(isMyAddressSelected) {
                ctrtInputBtn.setStyle("-fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #000000; -fx-text-fill: #ffffff; -fx-background-color: #000000;");
                ctrtAddrTextField.setText("");
                ctrtAddrImg.setImage(greyCircleAddrImg);
                ctrtAddrSelect.setVisible(false);
                ctrtAddrText.setVisible(true);
            } else {
                ctrtInputBtn.setStyle("-fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #999999; -fx-text-fill: #999999; -fx-background-color: #f2f2f2;");
                ctrtAddrSelect.setVisible(true);
                ctrtAddrText.setVisible(false);
            }

            isMyAddressSelected = !isMyAddressSelected;
        }
    }

    private ChangeListener<Boolean> ctrtFocusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue) {
                ctrtAddrTextField.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px;  -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #999999; -fx-background-color: #ffffff;");
            } else {
                ctrtAddrTextField.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px;  -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #d8d8d8; -fx-background-color: #f2f2f2;");
            }
        }
    };

    private ChangeListener<String> ctrtKeyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!ctrtAddrTextField.getText().matches("[0-9a-fA-F]*")) {
                ctrtAddrTextField.setText(ctrtAddrTextField.getText().replaceAll("[^0-9a-fA-F]", ""));
            }

            int maxlangth = 40;
            if (ctrtAddrTextField.getText().trim().length() > maxlangth) {
                ctrtAddrTextField.setText(ctrtAddrTextField.getText().trim().substring(0, maxlangth));
            }

            if (ctrtAddrTextField.getText() == null || ctrtAddrTextField.getText().trim().length() < maxlangth) {
                ctrtAddrImg.setImage(greyCircleAddrImg);
            } else {
                Image image = IdenticonGenerator.createIcon(ctrtAddrTextField.getText().trim());
                if (image != null) {
                    ctrtAddrImg.setImage(image);
                    image = null;
                }
            }
        }
    };

    private SmartContractFreezerImpl handler;
    public void setHandler(SmartContractFreezerImpl handler){
        this.handler = handler;
    }
    public interface SmartContractFreezerImpl {
        void onAction();
    }
}
