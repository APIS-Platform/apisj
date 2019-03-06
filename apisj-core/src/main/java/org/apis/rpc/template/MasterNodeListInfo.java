package org.apis.rpc.template;

import org.apis.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

public class MasterNodeListInfo {
    /** General Normal */
    private List<String> generalNormal = new ArrayList<>();
    /* Major Normal */
    private List<String> majorNormal = new ArrayList<>();
    /* Private Normal */
    private List<String> PrivateNormal = new ArrayList<>();

    /* General Late */
    private List<String> GeneralLate = new ArrayList<>();
    /* Major Late */
    private List<String> MajorLate = new ArrayList<>();
    /* Private Late */
    private List<String> PrivateLate = new ArrayList<>();

    public MasterNodeListInfo(List<byte[]> generalNormal,
                              List<byte[]> majorNormal,
                              List<byte[]> PrivateNormal,
                              List<byte[]> GeneralLate,
                              List<byte[]> MajorLate,
                              List<byte[]> PrivateLate) {
        for(byte[] addressByte : generalNormal) {
            this.generalNormal.add(ByteUtil.toHexString(addressByte));
        }
        for(byte[] addressByte : majorNormal) {
            this.majorNormal.add(ByteUtil.toHexString(addressByte));
        }
        for(byte[] addressByte : PrivateNormal) {
            this.PrivateNormal.add(ByteUtil.toHexString(addressByte));
        }

        for(byte[] addressByte : GeneralLate) {
            this.GeneralLate.add(ByteUtil.toHexString(addressByte));
        }
        for(byte[] addressByte : MajorLate) {
            this.MajorLate.add(ByteUtil.toHexString(addressByte));
        }
        for(byte[] addressByte : PrivateLate) {
            this.PrivateLate.add(ByteUtil.toHexString(addressByte));
        }
    }


    @Override
    public String toString() {
        return "MasterNodeListInfo{" +
                "generalNormal=" + generalNormal +
                ", majorNormal=" + majorNormal +
                ", PrivateNormal=" + PrivateNormal +
                ", GeneralLate=" + GeneralLate +
                ", MajorLate=" + MajorLate +
                ", PrivateLate=" + PrivateLate +
                '}';
    }
}