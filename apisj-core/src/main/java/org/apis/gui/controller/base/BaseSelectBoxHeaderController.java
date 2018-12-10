package org.apis.gui.controller.base;

import org.apis.gui.model.SelectBoxItemModel;

import java.math.BigInteger;

public class BaseSelectBoxHeaderController extends BaseViewController {

    protected SelectBoxItemModel itemModel;

    public String getAddress(){
        if(this.itemModel != null) {
            return this.itemModel.getAddress();
        }else{
            return null;
        }
    }
    public String getAlias(){
        if(this.itemModel != null) {
            return this.itemModel.getAlias();
        }else{
            return null;
        }
    }
    public BigInteger getBalance() {
        if(this.itemModel != null) {
            return this.itemModel.getBalance();
        }else{
            return BigInteger.ZERO;
        }
    }
    public BigInteger getMineral() {
        if (this.itemModel != null) {
            return this.itemModel.getMineral();
        } else {
            return BigInteger.ZERO;
        }
    }
    public String getMask() {
        if(this.itemModel != null) {
            return this.itemModel.getMask();
        }else{
            return null;
        }
    }

    public String getDomainId(){
        if(this.itemModel != null) {
            return this.itemModel.getDomainId();
        }else{
            return "-1";
        }
    }
    public String getDomain() {
        if (this.itemModel != null) {
            return this.itemModel.getDomain();
        } else {
            return null;
        }
    }
    public String getApis(){
        if (this.itemModel != null) {
            return this.itemModel.getApis();
        } else {
            return "0";
        }
    }
}
