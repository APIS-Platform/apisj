package org.apis.gui.model;

import javafx.beans.property.SimpleStringProperty;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class MainModel extends BaseModel {
    private SimpleStringProperty peer = new SimpleStringProperty();
    private SimpleStringProperty block = new SimpleStringProperty();
    private SimpleStringProperty timestemp = new SimpleStringProperty();

    public MainModel(){
        setPeer("0");
        setBlock(0, 0);
        setTimestemp(0, 0);
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
                sBlock = AppManager.comma(sBlock) + "("+AppManager.comma(block.toString())+")";
            }else{
                sBlock = AppManager.comma(sBlock) + "("+AppManager.comma(block.toString())+")";
            }
        }else if(lastBlock.toString().equals("0") && bestBlock.toString().equals("0")){
            sBlock = "-";
        }else{
            sBlock = AppManager.comma(sBlock);
        }
        this.block.setValue(sBlock);
    }

    public void setTimestemp(String lastTime, String nowTime){ setTimestemp(Long.parseLong(lastTime), Long.parseLong(nowTime)); }
    public void setTimestemp(long lastTime, long nowTime) {
        if(lastTime == 0 && nowTime == 0){
            this.timestemp.setValue("-");
        }else{
            this.timestemp.setValue(AppManager.setBlockTimestamp(lastTime, nowTime));
        }
    }

    public String getPeer() {
        return peer.get();
    }

    public SimpleStringProperty peerProperty() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer.set(peer);
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
