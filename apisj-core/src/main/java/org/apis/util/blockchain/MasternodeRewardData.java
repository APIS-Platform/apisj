package org.apis.util.blockchain;

import java.math.BigInteger;

public class MasternodeRewardData {
    private BigInteger foundation;
    private BigInteger generalNormal;
    private BigInteger generalLate;
    private BigInteger majorNormal;
    private BigInteger majorLate;
    private BigInteger privateNormal;
    private BigInteger privateLate;
    private BigInteger total;

    public MasternodeRewardData(BigInteger foundation, BigInteger generalNormal, BigInteger generalLate, BigInteger majorNormal, BigInteger majorLate, BigInteger privateNormal, BigInteger privateLate, BigInteger total) {
        this.foundation = foundation;
        this.generalNormal = generalNormal;
        this.generalLate = generalLate;
        this.majorNormal = majorNormal;
        this.majorLate = majorLate;
        this.privateNormal = privateNormal;
        this.privateLate = privateLate;
        this.total = total;
    }

    public BigInteger getFoundation() {
        return foundation;
    }

    public BigInteger getGeneralNormal() {
        return generalNormal;
    }

    public BigInteger getGeneralLate() {
        return generalLate;
    }

    public BigInteger getMajorNormal() {
        return majorNormal;
    }

    public BigInteger getMajorLate() {
        return majorLate;
    }

    public BigInteger getPrivateNormal() {
        return privateNormal;
    }

    public BigInteger getPrivateLate() {
        return privateLate;
    }

    public BigInteger getTotal() {
        return total;
    }


    @Override
    public String toString() {
        return "MasternodeRewardData{\n" +
                "  foundation   =" + ApisUtil.readableApis(foundation, false) + "\n" +
                "  generalNormal=" + ApisUtil.readableApis(generalNormal, false) + "\n" +
                "  generalLate  =" + ApisUtil.readableApis(generalLate, false) + "\n" +
                "  majorNormal  =" + ApisUtil.readableApis(majorNormal, false) + "\n" +
                "  majorLate    =" + ApisUtil.readableApis(majorLate, false) + "\n" +
                "  privateNormal=" + ApisUtil.readableApis(privateNormal, false) + "\n" +
                "  privateLate  =" + ApisUtil.readableApis(privateLate, false) + "\n" +
                "  total        =" + ApisUtil.readableApis(total, false) + "\n" +
                '}';
    }
}
