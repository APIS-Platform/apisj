package org.apis.gui.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apis.gui.manager.AppManager;
import sun.nio.ch.SelectorImpl;

import java.math.BigInteger;
import java.util.Date;

public class MainModel {
    private SimpleStringProperty totalBalanceNatural = new SimpleStringProperty();
    private SimpleStringProperty totalBalanceDecimal = new SimpleStringProperty();
    private SimpleStringProperty pear = new SimpleStringProperty();
    private SimpleStringProperty block = new SimpleStringProperty();
    private SimpleStringProperty timestemp = new SimpleStringProperty();

    public MainModel(){
        setBalance("0");
        setPear("0");
        setBlock(0, 0);
        setTimestemp(new Date().getTime());
    }


    public void setBalance(String balance){
        String[] balanceSlipt = AppManager.addDotWidthIndex(balance).split("\\.");

        this.totalBalanceNatural.setValue(balanceSlipt[0]);
        this.totalBalanceDecimal.setValue("." + balanceSlipt[1]);
    }
    public void setBlock(int iLastBlock, int iBestBlock){ setBlock(""+iLastBlock, ""+iBestBlock); }
    public void setBlock(long iLastBlock, long iBestBlock){ setBlock(""+iLastBlock, ""+iBestBlock); }
    public void setBlock(String sLastBlock, String sBestBlock){
        BigInteger lastBlock = new BigInteger(sLastBlock);
        BigInteger bestBlock = new BigInteger(sBestBlock);
        BigInteger block = bestBlock.subtract(lastBlock);
        String sBlock = lastBlock.toString();
        if(block.toString().equals("0") == false){
            if(block.toString().indexOf("-") >= 0){
                sBlock = sBlock + "("+block.toString()+")";
            }else{
                sBlock = sBlock + "(+"+block.toString()+")";
            }
        }else if(lastBlock.toString().equals("0") && bestBlock.toString().equals("0")){
            sBlock = "-";
        }
        this.block.setValue(sBlock);
    }

    public void setTimestemp(String lastTime){ setTimestemp(Long.parseLong(lastTime)); }
    public void setTimestemp(long lastTime) {
        this.timestemp.setValue(AppManager.setBlockTimestamp(lastTime));
    }



    public String getTotalBalanceNatural() {
        return totalBalanceNatural.get();
    }

    public SimpleStringProperty totalBalanceNaturalProperty() {
        return totalBalanceNatural;
    }

    public String getTotalBalanceDecimal() {
        return totalBalanceDecimal.get();
    }

    public SimpleStringProperty totalBalanceDecimalProperty() {
        return totalBalanceDecimal;
    }

    public String getPear() {
        return pear.get();
    }

    public SimpleStringProperty pearProperty() {
        return pear;
    }

    public void setPear(String pear) {
        this.pear.set(pear);
    }

    public String getBlock() {
        return block.get();
    }

    public SimpleStringProperty blockProperty() {
        return block;
    }

    public void setBlock(String block) {
        this.block.set(block);
    }

    public String getTimestemp() {
        return timestemp.get();
    }

    public SimpleStringProperty timestempProperty() {
        return timestemp;
    }
}
