package org.apis.rpc.template;

import org.apis.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

public class MasterNodeListInfo {
    List<String> generalAddressList = new ArrayList<>();
    List<String> majorAddressList = new ArrayList<>();
    List<String> privateAddressList = new ArrayList<>();

    public MasterNodeListInfo(List<byte[]> generalAddressList,
                              List<byte[]> majorAddressList,
                              List<byte[]> privateAddressList) {
        for(byte[] addressByte : generalAddressList) {
            this.generalAddressList.add(ByteUtil.toHexString(addressByte));
        }
        for(byte[] addressByte : majorAddressList) {
            this.majorAddressList.add(ByteUtil.toHexString(addressByte));
        }
        for(byte[] addressByte : privateAddressList) {
            this.privateAddressList.add(ByteUtil.toHexString(addressByte));
        }
    }
}
