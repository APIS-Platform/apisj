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
        try {
            setIdenticon(IdenticonGenerator.generateIdenticonsToImage(address, 128, 128));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return domainId;
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
        return apis;
    }

    public SelectBoxItemModel setApis(String apis) {
        this.apis = apis;
        return this;
    }
}
