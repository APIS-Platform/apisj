package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.common.QRCodeGenerator;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupPrintPrivatekeyController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private ImageView bgImage, walletAddressQRCode, walletPrivateKeyQRCode;
    @FXML
    private Label walletAddressLabel, walletPrivateKeyLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void init(byte[] address, byte[] privatekey){
        walletAddressLabel.textProperty().setValue(Hex.toHexString(address));
        walletPrivateKeyLabel.textProperty().setValue(Hex.toHexString(privatekey));

        try {
            walletAddressQRCode.setImage(QRCodeGenerator.generateQRCodeImage(address, 100, 100));
            walletPrivateKeyQRCode.setImage(QRCodeGenerator.generateQRCodeImage(privatekey, 100, 100));

        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
