package org.apis.rpc.template;

import com.google.gson.annotations.SerializedName;

public class ResultSyncingBlcokData {
    @SerializedName("startingBlock")
    private String startingBlock;

    @SerializedName("currentBlock")
    private String currentBlock;

    @SerializedName("highestBlock")
    private String highestBlock;

    public ResultSyncingBlcokData(String start, String current, String highest) {
        this.startingBlock = start;
        this.currentBlock = current;
        this.highestBlock = highest;
    }

    public ResultSyncingBlcokData(long start, long current, long highest) {
        this.startingBlock = objectToHexString(start);
        this.currentBlock = objectToHexString(current);
        this.highestBlock = objectToHexString(highest);
    }

    public String objectToHexString(Object object) {
        return String.format("0x%08X", object);
    }
}
