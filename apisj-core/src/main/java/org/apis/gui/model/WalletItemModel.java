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

    private String id;
    private String alias;
    private String address;
    private BigInteger apis = BigInteger.ZERO;
    private BigInteger mineral = BigInteger.ZERO;
    private BigInteger totalApis = BigInteger.ZERO;
    private BigInteger totalMineral = BigInteger.ZERO;
    private String mask;
    private boolean mining;
    private boolean masterNode;
    private String keystoreJsonData;

    private List<TokenModel> tokens = new ArrayList<>();
    private int cusorTokenIndex = 0; //0:APIS, 1:Mineral

    public WalletItemModel(){
        TokenModel apis = new TokenModel();
        apis.setTokenName("APIS");
        apis.setTokenSymbol("APIS");
        apis.setTokenAddress("-1");
        apis.setTokenValue(BigInteger.ZERO);
        tokens.add(apis);


        TokenModel mineral = new TokenModel();
        mineral.setTokenName("MINERAL");
        mineral.setTokenSymbol("MNR");
        mineral.setTokenAddress("-2");
        mineral.setTokenValue(BigInteger.ZERO);
        tokens.add(mineral);

        List<TokenRecord> tokenRecords = DBManager.getInstance().selectTokens();
        for(TokenRecord record : tokenRecords){
            TokenModel token = new TokenModel();
            token.setTokenName(record.getTokenName());
            token.setTokenSymbol(record.getTokenSymbol());
            token.setTokenAddress(ByteUtil.toHexString(record.getTokenAddress()));
            token.setTokenValue(BigInteger.ZERO);
            tokens.add(token);
        }
    }


    public WalletItemModel getClone(){
        WalletItemModel model = new WalletItemModel();

        model.setId(this.id);
        model.setAlias(this.alias);
        model.setAddress(this.address);
        model.setApis(this.apis);
        model.setMineral(this.mineral);
        model.setTotalApis(this.totalApis);
        model.setTotalMineral(this.totalMineral);
        model.setMask(this.mask);
        model.setMining(this.mining);
        model.setMasterNode(this.masterNode);
        model.setKeystoreJsonData(this.keystoreJsonData);
        model.setTokens(this.tokens);
        model.setCusorTokenIndex(this.cusorTokenIndex);

        return model;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BigInteger getTotalApis() {
        return totalApis;
    }

    public void setTotalApis(BigInteger totalApis) {
        this.totalApis = totalApis;
    }

    public BigInteger getTotalMineral() {
        return totalMineral;
    }

    public void setTotalMineral(BigInteger totalMineral) {
        this.totalMineral = totalMineral;
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

    public List<TokenModel> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenModel> tokens) {
        this.tokens = tokens;
    }

    public int getCusorTokenIndex() {
        return cusorTokenIndex;
    }

    public WalletItemModel setCusorTokenIndex(int cusorTokenIndex) {
        this.cusorTokenIndex = cusorTokenIndex;
        return this;
    }

    public String getTokenName() {
        return this.tokens.get(cusorTokenIndex).getTokenName();
    }

    public String getTokenSymbol() {
        return this.tokens.get(cusorTokenIndex).getTokenSymbol();
    }

    public String getTokenAddress() {
        return this.tokens.get(cusorTokenIndex).getTokenAddress();
    }

    public BigInteger getTokenValue() {
        if(cusorTokenIndex == 0){
            return getApis();
        } else if(cusorTokenIndex == 1){
            return getMineral();
        } else {
            return this.tokens.get(cusorTokenIndex).getTokenValue();
        }
    }
}
