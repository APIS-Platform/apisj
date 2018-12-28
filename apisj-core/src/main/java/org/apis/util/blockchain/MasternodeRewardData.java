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

    public MasternodeRewardData(BigInteger foundation, BigInteger generalNormal, BigInteger generalLate, BigInteger majorNormal, BigInteger majorLate, BigInteger privateNormal, BigInteger privateLate) {
        this.foundation = foundation;
        this.generalNormal = generalNormal;
        this.generalLate = generalLate;
        this.majorNormal = majorNormal;
        this.majorLate = majorLate;
        this.privateNormal = privateNormal;
        this.privateLate = privateLate;
        this.total = foundation.add(generalNormal).add(generalLate).add(majorNormal).add(majorNormal).add(privateNormal).add(privateLate);
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
        return "MasternodeRewardData{" +
                "foundation=" + foundation +
                ", generalNormal=" + generalNormal +
                ", generalLate=" + generalLate +
                ", majorNormal=" + majorNormal +
                ", majorLate=" + majorLate +
                ", privateNormal=" + privateNormal +
                ", privateLate=" + privateLate +
                ", total=" + total +
                '}';
    }
}
