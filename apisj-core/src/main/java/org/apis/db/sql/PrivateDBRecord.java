package org.apis.db.sql;

import java.util.List;

public class PrivateDBRecord {
    private List<AbiRecord> abiList;
    private List<AddressGroupRecord> addressGroupList;
    private List<ConnectAddressGroupRecord> connectAddressGroupList;
    private List<ContractRecord> contractList;
    private List<LedgerRecord> ledgerList;
    private List<MyAddressRecord> myAddressList;
    private List<RecentAddressRecord> recentAddressList;
    private List<TokenRecord> tokenList;

    public List<AbiRecord> getAbiList() {
        return this.abiList;
    }

    public void setAbiList(List<AbiRecord> abiList) {
        this.abiList = abiList;
    }

    public List<AddressGroupRecord> getAddressGroupList() {
        return this.addressGroupList;
    }

    public void setAddressGroupList(List<AddressGroupRecord> addrGrpslist) {
        this.addressGroupList = addrGrpslist;
    }

    public List<ConnectAddressGroupRecord> getConnectAddressGroupList() {
        return this.connectAddressGroupList;
    }

    public void setConnectAddressGroupList(List<ConnectAddressGroupRecord> connectAddressGroupList) {
        this.connectAddressGroupList = connectAddressGroupList;
    }

    public List<ContractRecord> getContractList() {
        return this.contractList;
    }

    public void setContractList(List<ContractRecord> contractList) {
        this.contractList = contractList;
    }

    public List<LedgerRecord> getLedgerList() {
        return this.ledgerList;
    }

    public void setLedgerList(List<LedgerRecord> ledgerList) {
        this.ledgerList = ledgerList;
    }

    public List<MyAddressRecord> getMyAddressList() {
        return this.myAddressList;
    }

    public void setMyAddressList(List<MyAddressRecord> myAddressList) {
        this.myAddressList =  myAddressList;
    }

    public List<RecentAddressRecord> getRecentAddressList() {
        return this.recentAddressList;
    }

    public void setRecentAddressList(List<RecentAddressRecord> recentAddressList) {
        this.recentAddressList = recentAddressList;
    }

    public List<TokenRecord> getTokenList() {
        return this.tokenList;
    }

    public void setTokenList(List<TokenRecord> tokenList) {
        this.tokenList = tokenList;
    }
}
