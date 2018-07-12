package org.apis.rpc.template;

import org.apis.util.ByteUtil;
import org.apis.vm.DataWord;
import org.apis.vm.LogInfo;

import java.util.ArrayList;
import java.util.List;

public class LogInfoData
{
    private String address;
    private List<String> topicList = new ArrayList<>();
    private String data;

    public LogInfoData(LogInfo info) {
        this.address = ByteUtil.toHexString(info.getAddress());
        for(DataWord topic : info.getTopics()) {
            this.topicList.add(topic.toString());
        }
        this.data = ByteUtil.toHexString(info.getData());
    }

    public String getAddress() {
        return address;
    }

    public List<String> getTopicList() {
        return topicList;
    }

    public String getData() {
        return data;
    }
}
