package org.apis.rpc.template;

import org.apis.util.ByteUtil;
import org.apis.vm.DataWord;
import org.apis.vm.LogInfo;

public class LogInfoData
{
    private String address;
    private String[] topics;
    private String data;
    private String blockHash;
    private String transactionHash;
    private String logIndex;
    private long blockNumber;
    private int transactionIndex;

    public LogInfoData(LogInfo info, String blockHash, String transactionHash, int logIndex, long blockNumber, int transactionIndex) {
        this(info);
        this.blockHash = blockHash;
        this.transactionHash = transactionHash;
        this.logIndex = ByteUtil.toHexString0x(ByteUtil.intToBytesNoLeadZeroes(logIndex));
        this.blockNumber = blockNumber;
        this.transactionIndex = transactionIndex;
    }

    public LogInfoData(LogInfo info) {
        this.address = ByteUtil.toHexString0x(info.getAddress());

        topics = new String[info.getTopics().size()];
        for(int i = 0; i < info.getTopics().size(); i++) {
            DataWord topic = info.getTopics().get(i);
            this.topics[i] = ByteUtil.toHexString0x(topic.getData());
        }

        this.data = ByteUtil.toHexString0x(info.getData());
    }

    public String getAddress() {
        return address;
    }

    public String getData() {
        return data;
    }
}
