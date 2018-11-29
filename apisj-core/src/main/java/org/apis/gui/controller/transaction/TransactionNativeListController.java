package org.apis.gui.controller.transaction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.db.sql.ContractRecord;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TransactionRecord;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.LogInfo;
import org.apis.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionNativeListController extends BaseViewController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label blockNumber, hash, from, to, state, value, fee, time;
    @FXML
    private ImageView arrowImg;

    private Image failArrowImg, pendingArrowImg, successArrowImg;
    private TransactionRecord record;

    String strHash, strFrom, strTo, strTime;
    String[] splitTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Image Setting
        failArrowImg = new Image("image/ic_fail_arrow@2x.png");
        pendingArrowImg = new Image("image/ic_pending_arrow@2x.png");
        successArrowImg = new Image("image/ic_success_arrow@2x.png");

        // Underline Setting
        hash.setOnMouseEntered(event -> hash.setUnderline(true));
        from.setOnMouseEntered(event -> from.setUnderline(true));
        to.setOnMouseEntered(event -> to.setUnderline(true));
        hash.setOnMouseExited(event -> hash.setUnderline(false));
        from.setOnMouseExited(event -> from.setUnderline(false));
        to.setOnMouseExited(event -> to.setUnderline(false));

        time.setOnMouseEntered(event -> {
            if(strTime.indexOf("day") >= 0) {
                setTime(splitTime[0]+"\n"+splitTime[1]);
            }else{
                setTime(strTime);
            }});
        time.setOnMouseExited(event -> {
            if(strTime.indexOf("day") >= 0) {
                setTime(splitTime[0]);
            }else{
                setTime(strTime);
            }});

    }

    @FXML
    public void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("rootPane")){
            this.handler.showDetails(record);

        }else if(fxid.equals("hash")) {
            this.handler.showDetails(record);
            event.consume();

        } else if(fxid.equals("from")) {
            this.handler.searchText(record, strFrom);
            event.consume();

        } else if(fxid.equals("to")) {
            this.handler.searchText(record, strTo);
            event.consume();

        }
    }

    public void setStatus(int status, String receiver) {
        if(status == 0) {
            this.state.textProperty().bind(StringManager.getInstance().transaction.listBlockFail);
            this.state.setTextFill(Color.web("#fa5252"));
            if(receiver == null || receiver.length() == 0) {
                this.arrowImg.setVisible(false);
            } else {
                this.arrowImg.setImage(failArrowImg);
                this.arrowImg.setVisible(true);
            }

        } else if(status == 1) {
            this.state.textProperty().bind(StringManager.getInstance().transaction.listBlockSuccess);
            this.state.setTextFill(Color.web("#51cf66"));
            if(receiver == null || receiver.length() == 0) {
                this.arrowImg.setVisible(false);
            } else {
                this.arrowImg.setImage(successArrowImg);
                this.arrowImg.setVisible(true);
            }

        } else {
            this.state.textProperty().bind(StringManager.getInstance().transaction.listBlockPending);
            this.state.setTextFill(Color.web("#ff922b"));
            if(receiver == null || receiver.length() == 0) {
                this.arrowImg.setVisible(false);
            } else {
                this.arrowImg.setImage(pendingArrowImg);
                this.arrowImg.setVisible(true);
            }
        }
    }

    public void setTransactionRecord(TransactionRecord transactionRecord){
        this.record = transactionRecord;



        // Value Setting
        BigInteger value = record.getAmount();
        String valueString;
        if(value == null || value.toString().equals("0")) {
            value = BigInteger.ZERO;
            valueString = value.toString();
        } else {
            valueString = ApisUtil.readableApis(value, ',', true);
        }

        // Calculate Fee
        BigInteger gasUsed = record.getGasUsed();
        gasUsed = (gasUsed == null) ? BigInteger.ZERO : gasUsed;
        BigInteger gasPrice = (record.getGasPrice() != null) ? record.getGasPrice() : BigInteger.ZERO;
        BigInteger mineral = (record.getMineralUsed() != null) ? record.getMineralUsed() : BigInteger.ZERO;
        BigInteger fee = gasUsed.multiply(gasPrice).subtract(mineral);
        String feeString;
        if(fee.toString().indexOf('-') >= 0 || fee.toString().equals("0")) {
            fee = BigInteger.ZERO;
            feeString = fee.toString();
        } else {
            feeString = ApisUtil.readableApis(fee, ',', true);
        }
        strTime = AppManager.getInstance().getBlockTimeToString(record.getBlock_number());
        long lTime = AppManager.getInstance().getBlockTimeLong(record.getBlock_number());
        splitTime = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date(lTime * 1000)).split(" ");

        setBlockNumber(record.getBlock_number());
        setHash(record.getHash());
        setStatus(record.getStatus(), record.getReceiver());
        setFrom(record.getSender());
        setTo(record.getReceiver());
        setValue(valueString);
        setFee(feeString);
        if(strTime.indexOf("day") >= 0) {
            setTime(splitTime[0]);
        }else{
            setTime(strTime);
        }


    }


    public String getBlockNumber (){
        return this.blockNumber.getText();
    }

    public void setBlockNumber(long blockNumber){
        this.blockNumber.setText(Long.toString(blockNumber));
    }

    public String getHash() {
        return hash.getText();
    }

    public void setHash(String hash) {
        this.strHash = hash;
        this.hash.setText(AddressUtil.getShortAddress(hash, 6));
    }

    public String getFrom() {
        return from.getText();
    }

    public void setFrom(String from) {
        this.strFrom = from;
        this.from.setText(AddressUtil.getShortAddress(from, 8));
    }

    public String getTo() {
        return to.getText();
    }

    public void setTo(String to) {
        this.strTo = to;
        this.to.setText(AddressUtil.getShortAddress(to, 8));
    }

    public String getSatate() {
        return state.getText();
    }

    public void setState(String block) {
        this.state.setText(block);
    }

    public String getValue() {
        return value.getText();
    }

    public void setValue(String value) {
        this.value.setText(value);
    }

    public String getFee() {
        return fee.getText();
    }

    public void setFee(String fee) {
        this.fee.setText(fee);
    }

    public String getTime() {
        return time.getText();
    }

    public void setTime(String time) {
        this.time.setText(time);
    }

    public void setBgColor(String bgColor) {
        rootPane.setStyle("-fx-background-color: "+bgColor+";");
    }


    private TransactionNativeListImpl handler;
    public void setHandler(TransactionNativeListImpl handler) {
        this.handler = handler;
    }
    public interface TransactionNativeListImpl {
        void showDetails(TransactionRecord record);
        void searchText(TransactionRecord record, String searchText);
    }
}
