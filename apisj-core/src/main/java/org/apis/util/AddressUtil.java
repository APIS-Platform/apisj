package org.apis.util;

import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

public class AddressUtil {
    /**
     * 입력된 주소의 앞 3글자, 뒷 3글자를 이어서 반환한다.
     * @param address Hex type address
     * @return aaa..zzz
     */
    public static String getShortAddress(String address) {
        return getShortAddress(Hex.decode(address));
    }

    /**
     * 입력된 주소의 앞 3글자, 뒷 3글자를 이어서 반환한다.
     * @param address Hex type address
     * @return aaa..zzz
     */
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


    public static String getIPAddress(String host) {
        if(host == null || host.isEmpty()) {
            return "";
        }
        String[] hostUnits = host.split("\\.");
        StringBuilder resultHost = new StringBuilder();
        for(int i = 0 ; i < hostUnits.length; i++) {
            if(i > 0) {
                resultHost.append(".");
            }
            resultHost.append(String.format("%03d", Integer.parseInt(hostUnits[i])));
        }
        return resultHost.toString();
    }

    public static boolean isAddress(String addressStr) {
        if(addressStr == null){
            return false;
        }

        try {
            addressStr = addressStr.replace("0x", "");

            byte[] address = Hex.decode(addressStr);

            return address.length == 20;
        } catch (DecoderException e) {
            return false;
        }
    }
}
