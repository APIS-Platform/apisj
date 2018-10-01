package org.apis.gui.model;

import com.google.zxing.WriterException;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import org.apis.gui.common.IdenticonGenerator;

import java.io.IOException;


public class SelectBoxWalletItemModel {
    private String keystoreId = "";
    private String balance = "";
    private String mineral = "";
    private SimpleStringProperty alias = new SimpleStringProperty();
    private SimpleStringProperty address = new SimpleStringProperty();
    private SimpleStringProperty mask = new SimpleStringProperty();
    private Image identicon;

    public String getKeystoreId() { return keystoreId; }

    public void setKeystoreId(String keystoreId) { this.keystoreId = keystoreId; }

    public String getBalance() { return balance; }

    public void setBalance(String balance) { this.balance = balance; }

    public String getMineral() { return mineral; }

    public void setMineral(String mineral) { this.mineral = mineral; }

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
}
