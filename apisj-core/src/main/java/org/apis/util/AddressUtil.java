package org.apis.util;

import org.spongycastle.util.encoders.Hex;

public class AddressUtil {
    public static String getShortAddress(String address) {
        return getShortAddress(Hex.decode(address));
    }

    public static String getShortAddress(byte[] address) {
        return getShortAddress(address, 3);
    }

    public static String getShortAddress(String address, int length) {
        return getShortAddress(Hex.decode(address), length);
    }

    public static String getShortAddress(byte[] address, int length) {
        if(address == null || address.length == 0) {
            return "";
        } else {
            String addr = Hex.toHexString(address);
            return addr.substring(0, length) + ".." + addr.substring(addr.length() - length, addr.length());
        }
    }
}
