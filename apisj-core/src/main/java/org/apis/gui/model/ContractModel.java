package org.apis.gui.model;

import org.spongycastle.util.encoders.Hex;

public class ContractModel {
    private String name;
    private String address;
    private String abi;

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
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }
}
