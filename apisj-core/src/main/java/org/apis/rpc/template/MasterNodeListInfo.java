package org.apis.rpc.template;

import java.util.ArrayList;
import java.util.List;

public class MasterNodeListInfo {
    List<String> generalAddressList = new ArrayList<>();
    List<String> majorAddressList = new ArrayList<>();
    List<String> privateAddressList = new ArrayList<>();

    public MasterNodeListInfo(List<String> generalAddressList,
                              List<String> majorAddressList,
                              List<String> privateAddressList) {
        this.generalAddressList = generalAddressList;
        this.majorAddressList = majorAddressList;
        this.privateAddressList = privateAddressList;
    }
}
