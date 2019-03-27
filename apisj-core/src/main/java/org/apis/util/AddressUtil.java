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
            return addr.substring(0, length) + "..." + addr.substring(addr.length() - length, addr.length());
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

    /**
     * 입력된 마스크에 ! 도메인이 할당되어있는지 확인한다(ex. mining@!)
     * @param mask 어드레스 마스킹 주소
     * @return
     * TRUE : @! 도메인을 보유하고 있음<br/>
     * FALSE : 보유하고 있지 않음
     */
    public static boolean hasQDomain(String mask) {
        if(mask == null || mask.substring(mask.lastIndexOf("@") + 1).equals("!") == false) {
            return false;
        }
        return true;
    }
}
