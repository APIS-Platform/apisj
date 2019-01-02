package org.apis.rpc.template;

public class MasternodeCountData {

    private long generalEarly = 0;
    private long majorEarly = 0;
    private long privateEarly = 0;

    private long generalNormal = 0;
    private long majorNormal = 0;
    private long privateNormal = 0;

    private long generalLate = 0;
    private long majorLate = 0;
    private long privateLate = 0;

    public MasternodeCountData(long generalEarly, long majorEarly, long privateEarly, long generalNormal, long majorNormal, long privateNormal, long generalLate, long majorLate, long privateLate) {
        this.generalEarly = generalEarly;
        this.majorEarly = majorEarly;
        this.privateEarly = privateEarly;

        this.generalNormal = generalNormal;
        this.majorNormal = majorNormal;
        this.privateNormal = privateNormal;

        this.generalLate = generalLate;
        this.majorLate = majorLate;
        this.privateLate = privateLate;
    }


    @Override
    public String toString() {
        return "MasternodeCountData{" +
                "generalEarly=" + generalEarly +
                ", majorEarly=" + majorEarly +
                ", privateEarly=" + privateEarly +
                ", generalNormal=" + generalNormal +
                ", majorNormal=" + majorNormal +
                ", privateNormal=" + privateNormal +
                ", generalLate=" + generalLate +
                ", majorLate=" + majorLate +
                ", privateLate=" + privateLate +
                '}';
    }
}
