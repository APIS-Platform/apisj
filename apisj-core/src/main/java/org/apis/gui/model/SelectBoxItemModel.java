package org.apis.gui.model;

import com.google.zxing.WriterException;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.model.base.BaseModel;

import java.io.IOException;
import java.math.BigInteger;


public class SelectBoxItemModel extends BaseModel {
    private String keystoreId = "";
    private BigInteger balance = BigInteger.ZERO;
    private BigInteger mineral = BigInteger.ZERO;
    private SimpleStringProperty alias = new SimpleStringProperty();
    private SimpleStringProperty address = new SimpleStringProperty();
    private SimpleStringProperty mask = new SimpleStringProperty();
    private Image identicon;
    private String domainId = "";
    private String domain = "";
    private String apis = "";
    private boolean isUsedProofKey = false;

    public String getKeystoreId() { return keystoreId; }

    public void setKeystoreId(String keystoreId) { this.keystoreId = keystoreId; }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getMineral() {
        return mineral;
    }

    public void setMineral(BigInteger mineral) {
        this.mineral = mineral;
    }

    public String getAlias() {
        return alias.get();
    }

    public SimpleStringProperty aliasProperty() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias.set(alias);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
        setIdenticon(IdenticonGenerator.createIcon(address));
    }

    public String getMask() {
        return mask.get();
    }

    public SimpleStringProperty maskProperty() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask.set(mask);
    }

    public Image getIdenticon() {
        return identicon;
    }

    public void setIdenticon(Image identicon) {
        this.identicon = identicon;
    }

    public String getDomainId() {
        return (domainId != null && domainId.length() > 0) ? domainId : "-1";
    }

    public SelectBoxItemModel setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getDomain() {
        return (domain != null) ? domain : "";
    }

    public SelectBoxItemModel setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getApis() {
        return (apis != null && apis.length() > 0) ? apis : "0" ;
    }

    public SelectBoxItemModel setApis(String apis) {
        this.apis = apis;
        return this;
    }

    public void setUsedProofKey(boolean isUsedProofKey){
        this.isUsedProofKey = isUsedProofKey;
    }
    public boolean isUsedProofKey(){
        return isUsedProofKey;
    }

    public void set(SelectBoxItemModel model) {
        this.keystoreId = model.getKeystoreId();
        this.balance = model.getBalance();
        this.mineral = model.getMineral();
        this.alias = new SimpleStringProperty(model.getAlias());
        this.address = new SimpleStringProperty(model.getAddress());
        this.mask = new SimpleStringProperty(model.getMask());
        this.identicon  = model.getIdenticon();
        this.domainId = model.getDomainId();
        this.domain = model.getDomain();
        this.apis = model.getApis();
        this.isUsedProofKey = model.isUsedProofKey();
    }
}
