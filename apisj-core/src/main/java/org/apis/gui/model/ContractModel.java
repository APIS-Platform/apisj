package org.apis.gui.model;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import org.apis.gui.common.IdenticonGenerator;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

public class ContractModel {
    private String name;
    private String address;
    private String abi;
    private Image identicon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public byte[] getAddressByte(){
        return Hex.decode(address);
    }

    public void setAddress(String address) {
        this.address = address;
        try {
            setIdenticon(IdenticonGenerator.generateIdenticonsToImage(address, 128, 128));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public Image getIdenticon() {
        return identicon;
    }

    public void setIdenticon(Image identicon) {
        this.identicon = identicon;
    }
}
