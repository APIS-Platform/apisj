package org.apis.util;

import org.spongycastle.util.encoders.Hex;

public class AddressUtil {
    public static String getShortAddress(byte[] address) {
        return getShortAddress(address, 3);
    }

    public static String getShortAddress(byte[] address, int length) {
        String addr = Hex.toHexString(address);
        return addr.substring(0, length) + ".." + addr.substring(addr.length() - length, addr.length());
    }
}
