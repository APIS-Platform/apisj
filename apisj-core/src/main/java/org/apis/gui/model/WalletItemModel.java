package org.apis.gui.model;

import com.google.zxing.WriterException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *  하나의 지갑에 대한 정보를 담고 있다.
 */
public class WalletItemModel extends BaseModel {

    private String alias;
    private String address;
    private BigInteger apis = BigInteger.ZERO;
    private BigInteger mineral = BigInteger.ZERO;
    private String mask;
    private boolean mining;
    private boolean masterNode;
    private String keystoreJsonData;
    private String tokenAddress;
    private boolean isUsedProofKey;

    public WalletItemModel(){

    }


    public WalletItemModel getClone(){
        WalletItemModel model = new WalletItemModel();

        model.setAlias(this.alias);
        model.setAddress(this.address);
        model.setApis(this.apis);
        model.setMineral(this.mineral);
        model.setMask(this.mask);
        model.setMining(this.mining);
        model.setMasterNode(this.masterNode);
        model.setKeystoreJsonData(this.keystoreJsonData);
        model.setTokenAddress(this.tokenAddress);

        return model;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getApis() {
        return apis;
    }

    public void setApis(BigInteger apis) {
        this.apis = apis;
    }

    public BigInteger getMineral() {
        return mineral;
    }

    public void setMineral(BigInteger mineral) {
        this.mineral = mineral;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public boolean isMining() {
        return mining;
    }

    public void setMining(boolean mining) {
        this.mining = mining;
    }

    public boolean isMasterNode() {
        return masterNode;
    }

    public void setMasterNode(boolean masterNode) {
        this.masterNode = masterNode;
    }

    public String getKeystoreJsonData() {
        return keystoreJsonData;
    }

    public void setKeystoreJsonData(String keystoreJsonData) {
        this.keystoreJsonData = keystoreJsonData;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public void setUsedProofKey(boolean isUsedProofKey){
        this.isUsedProofKey = isUsedProofKey;
    }

    public boolean isUsedProofKey(){
        return this.isUsedProofKey;
    }

    public void set(WalletItemModel model) {

        this.alias = model.getAlias();
        this.address = model.getAddress();
        this.apis = model.getApis();
        this.mineral = model.getMineral();
        this.mask = model.getMask();
        this.mining = model.isMining();
        this.masterNode = model.isMasterNode();
        this.mining = model.isMining();
        this.keystoreJsonData = model.getKeystoreJsonData();
        this.tokenAddress = model.getTokenAddress();
        this.isUsedProofKey = model.isUsedProofKey();
    }
}
