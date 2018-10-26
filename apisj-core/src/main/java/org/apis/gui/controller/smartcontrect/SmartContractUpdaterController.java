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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractUpdaterController extends BaseViewController {
    @FXML private Label ctrtInputBtn1;
    @FXML private ImageView ctrtAddrImg1;
    @FXML private AnchorPane ctrtAddrText1, ctrtAddrSelect1;
    @FXML private TextField ctrtAddrTextField1;
    @FXML private ApisSelectBoxController contractCnstSelector1Controller;
    private Image greyCircleAddrImg = new Image("image/ic_circle_grey@2x.png");

    private boolean isMyAddressSelected1 = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Ellipse ellipse1 = new Ellipse(12, 12);
        ellipse1.setCenterY(12);
        ellipse1.setCenterX(12);
        ctrtAddrImg1.setClip(ellipse1);



        // Contract Constructor Address Listener
        ctrtAddrTextField1.focusedProperty().addListener(ctrtFocusListener);
        ctrtAddrTextField1.textProperty().addListener(ctrtKeyListener);
        contractCnstSelector1Controller.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        contractCnstSelector1Controller.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onMouseClick() {
            }

            @Override
            public void onSelectItem() {
                if(hander != null){
                    hander.onAction();
                }
            }
        });

    }
    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("ctrtInputBtn1")) {
            if(isMyAddressSelected1) {
                ctrtInputBtn1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #000000; -fx-text-fill: #ffffff; -fx-background-color: #000000;");
                ctrtAddrTextField1.setText("");
                ctrtAddrImg1.setImage(greyCircleAddrImg);
                ctrtAddrSelect1.setVisible(false);
                ctrtAddrText1.setVisible(true);
            } else {
                ctrtInputBtn1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #999999; -fx-text-fill: #999999; -fx-background-color: #f2f2f2;");
                ctrtAddrSelect1.setVisible(true);
                ctrtAddrText1.setVisible(false);
            }

            isMyAddressSelected1 = !isMyAddressSelected1;
        }
    }


    private ChangeListener<Boolean> ctrtFocusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(ctrtAddrTextField1.isVisible()) {
                ctrtAddrTextField1.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px;  -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #d8d8d8; -fx-background-color: #f2f2f2;");
            }else{
                ctrtAddrTextField1.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px;  -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; " +
                        "-fx-border-color: #d8d8d8; -fx-background-color: #f2f2f2;");
            }
        }
    };

    private ChangeListener<String> ctrtKeyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!ctrtAddrTextField1.getText().matches("[0-9a-fA-F]*")) {
                ctrtAddrTextField1.setText(ctrtAddrTextField1.getText().replaceAll("[^0-9a-fA-F]", ""));
            }

            int maxlangth = 40;
            if (ctrtAddrTextField1.getText().trim().length() > maxlangth) {
                ctrtAddrTextField1.setText(ctrtAddrTextField1.getText().trim().substring(0, maxlangth));
            }

            if (ctrtAddrTextField1.getText() == null || ctrtAddrTextField1.getText().trim().length() < maxlangth) {
                ctrtAddrImg1.setImage(greyCircleAddrImg);
            } else {
                try {
                    Image image = IdenticonGenerator.generateIdenticonsToImage(ctrtAddrTextField1.getText().trim(), 128, 128);
                    if (image != null) {
                        ctrtAddrImg1.setImage(image);
                        image = null;
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private SmartContractUpdaterImpl hander;
    public void setHandler(SmartContractUpdaterImpl hander){
        this.hander = hander;
    }
    public interface SmartContractUpdaterImpl {
        void onAction();
    }
}
