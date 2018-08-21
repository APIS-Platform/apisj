package org.apis.gui.model;

import java.util.ArrayList;

public class MyAddressModel {
    private String address;
    private String alias;
    private ArrayList<String> groupList;

    public MyAddressModel(String address, String alias, ArrayList<String> groupList){
        this.address = address;
        this.alias = alias;
        this.groupList = groupList;

        if(this.groupList == null){
            this.groupList = new ArrayList<>();
        }
    }

    public void addGroup(String group){
        this.groupList.add(group);
    }

    public void removeGroup(String group){
        this.groupList.remove(group);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ArrayList<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<String> groupList) {
        this.groupList = groupList;
    }
}
