package org.apis.gui.model;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

public class ContractModel extends BaseModel {
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
        return ByteUtil.hexStringToBytes(address);
    }

    public void setAddress(String address) {
        this.address = address;
        setIdenticon(IdenticonGenerator.createIcon(address));
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
