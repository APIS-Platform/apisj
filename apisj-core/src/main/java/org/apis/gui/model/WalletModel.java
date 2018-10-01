package org.apis.gui.model;

import javafx.beans.property.SimpleStringProperty;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class WalletModel {

    public static final int UNIT_TYPE_APIS = WalletItemModel.UNIT_TYPE_APIS;
    public static final int UNIT_TYPE_MINERAL = WalletItemModel.UNIT_TYPE_MINERAL;

    private SimpleStringProperty totalTitle = new SimpleStringProperty();
    private SimpleStringProperty totalSubTitle = new SimpleStringProperty();

    private SimpleStringProperty totalMainNatural = new SimpleStringProperty();
    private SimpleStringProperty totalMainUnit = new SimpleStringProperty();

    private SimpleStringProperty totalSubNatural = new SimpleStringProperty();
    private SimpleStringProperty totalSubUnit = new SimpleStringProperty();

    private SimpleStringProperty totalApisNatural = new SimpleStringProperty();
    private SimpleStringProperty totalMineralNatural = new SimpleStringProperty();

    private SimpleStringProperty reward = new SimpleStringProperty();

    public WalletModel(){
        setBalance("0");
        setMineral("0");
    }

    public void setBalance(String balance){
        this.totalApisNatural.setValue(ApisUtil.readableApis(new BigInteger(balance), ',', false));
    }
    public void setMineral(String balance){
        this.totalMineralNatural.setValue(ApisUtil.readableApis(new BigInteger(balance), ',', false));
    }

    public void setTotalType(int unitType){
        totalTitle.unbind();
        totalSubTitle.unbind();
        totalMainNatural.unbind();
        totalSubNatural.unbind();
        switch (unitType){
            case UNIT_TYPE_APIS :
                totalTitle.bind(StringManager.getInstance().wallet.totalAmount);
                totalSubTitle.bind(StringManager.getInstance().wallet.totalMineralSubAmount);
                totalMainNatural.bind(totalApisNatural);
                totalMainUnit.setValue("APIS");
                totalSubNatural.bind(totalMineralNatural);
                totalSubUnit.setValue("MNR");
                break;
            case UNIT_TYPE_MINERAL:
                totalTitle.bind(StringManager.getInstance().wallet.totalMineralAmount);
                totalSubTitle.bind(StringManager.getInstance().wallet.totalSubAmount);
                totalMainNatural.bind(totalMineralNatural);
                totalMainUnit.setValue("MNR");
                totalSubNatural.bind(totalApisNatural);
                totalSubUnit.setValue("APIS");
                break;
        }
    }

    public String getTotalTitle() { return totalTitle.get(); }

    public SimpleStringProperty totalTitleProperty() { return totalTitle; }

    public void setTotalTitle(String totalTitle) { this.totalTitle.set(totalTitle);  }

    public String getTotalSubTitle() { return totalSubTitle.get(); }

    public SimpleStringProperty totalSubTitleProperty() { return totalSubTitle; }

    public void setTotalSubTitle(String totalSubTitle) { this.totalSubTitle.set(totalSubTitle); }

    public String getTotalMainUnit() { return totalMainUnit.get(); }

    public SimpleStringProperty totalMainUnitProperty() { return totalMainUnit; }

    public void setTotalMainUnit(String totalMainUnit) { this.totalMainUnit.set(totalMainUnit); }

    public String getTotalSubUnit() { return totalSubUnit.get(); }

    public SimpleStringProperty totalSubUnitProperty() { return totalSubUnit; }

    public void setTotalSubUnit(String totalSubUnit) { this.totalSubUnit.set(totalSubUnit); }

    public String getTotalMainNatural() {
        return totalMainNatural.get();
    }

    public SimpleStringProperty totalMainNaturalProperty() {
        return totalMainNatural;
    }

    public String getTotalSubNatural() {
        return totalSubNatural.get();
    }

    public SimpleStringProperty totalSubNaturalProperty() {
        return totalSubNatural;
    }

    public String getTotalApisNatural() {
        return totalApisNatural.get();
    }

    public SimpleStringProperty totalApisNaturalProperty() {
        return totalApisNatural;
    }

    public void setTotalApisNatural(String totalApisNatural) {
        this.totalApisNatural.set(totalApisNatural);
    }

    public String getTotalMineralNatural() {
        return totalMineralNatural.get();
    }

    public SimpleStringProperty totalMineralNaturalProperty() {
        return totalMineralNatural;
    }

    public void setTotalMineralNatural(String totalMineralNatural) {
        this.totalMineralNatural.set(totalMineralNatural);
    }

    public String getReward() {
        return reward.get();
    }

    public SimpleStringProperty rewardProperty() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward.set(reward);
    }
}
