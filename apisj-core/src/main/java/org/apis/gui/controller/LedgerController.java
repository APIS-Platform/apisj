package org.apis.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.paint.Color;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Apis;
import org.apis.facade.ApisFactory;
import org.apis.hid.HIDDevice;
import org.apis.hid.HIDModule;
import org.apis.hid.template.DeviceData;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;

import javax.usb.UsbException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LedgerController implements Initializable {
    @FXML private TextArea rawDataTextArea, signedTextArea;
    @FXML private Label btnSign, btnSend, txtStatus, txtBlock;
    @FXML private Button btnCheck, btnAddress;
    @FXML private TextField editPath, editAddress;

    private static Apis mApis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rawDataTextArea.setText("");
        signedTextArea.setText("");

        mApis = ApisFactory.createEthereum();
        mApis.addListener(mListener);
    }

    @FXML
    public void onMousePressed(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("btnSign")) {
            btnSign.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #ffffff; -fx-text-fill: #b01e1e;");

        } else if(fxid.equals("btnSend")) {
            btnSend.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #ffffff; -fx-text-fill: #b01e1e;");
        } else if(fxid.equals("btnCheck")) {

        }
    }

    Transaction unsingedTx;
    Transaction signedTx = null;
    HIDDevice ledger = null;
    String senderPath = null;
    byte[] senderAddress = null;

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        switch(fxid) {
            case "btnSign":
                String rawTx = rawDataTextArea.getText();
                byte[] raw = ByteUtil.hexStringToBytes(rawTx);
                unsingedTx = new Transaction(raw);
                ConsoleUtil.printlnPurple(unsingedTx.toString());

                try {
                    signedTx = new Transaction(ledger.signTransaction(senderPath, unsingedTx, senderAddress));
                    Transaction newTx = new Transaction(signedTx.getEncoded());
                    signedTextArea.setText(String.format("%s\n------\n%s\n------\n%s", ByteUtil.toHexString(signedTx.getEncoded()), signedTx.toString().replaceAll(" ", "\n"), newTx.toString().replaceAll(" ", "\n")));
                } catch (Exception e) {
                    signedTextArea.setText(e.getMessage());
                }

                break;
            case "btnCheck":
                HIDModule module = HIDModule.getInstance();
                module.loadDeviceList();

                List<DeviceData> devices = module.getDeviceList();
                if(devices.size() == 1) {
                    txtStatus.setTextFill(Color.web("#32CD32"));
                    txtStatus.setText("렛저를 찾았습니다!");

                    try {
                        ledger = new HIDDevice(module.getDeviceList().get(0).getDevice());
                    } catch (UsbException e) {
                        e.printStackTrace();
                    }
                }
                else if(devices.size() > 1) {
                    txtStatus.setTextFill(Color.web("#DC143C"));
                    txtStatus.setText("렛저를 하나만 연결해주세요!");
                }
                else {
                    txtStatus.setTextFill(Color.web("#DC143C"));
                    txtStatus.setText("렛저를 찾지 못했습니다.");
                }
                break;
            case "btnSend":
                if(signedTx != null) {
                    mApis.submitTransaction(signedTx);
                    txtStatus.setText("전송했습니다.");
                }
                break;

            case "btnAddress":
                if(ledger == null) {
                    txtStatus.setText("렛저와 연결이 필요합니다.");
                    return;
                }

                String path = editPath.getText();
                if(path.isEmpty()) {
                    txtStatus.setText("올바른 주소 경로를 입력해주세요.");
                    return;
                }

                senderAddress = ledger.getAddress(path, false, false);

                senderPath = path;
                editAddress.setText(ByteUtil.toHexString(senderAddress));
                break;
        }
    }

    @FXML
    public void onMouseReleased(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("btnSign")) {
            btnSign.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #b01e1e; -fx-text-fill: #ffffff;");

        } else if(fxid.equals("btnSend")) {
            btnSend.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #b01e1e; -fx-text-fill: #ffffff;");
        }
    }



    private EthereumListener mListener = new EthereumListenerAdapter() {
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            Platform.runLater(() -> {
                long best = mApis.getSyncStatus().getBlockBestKnown();
                txtBlock.setText(String.format("%d / %d", block.getNumber(), best));
            });
        }
    };
}
