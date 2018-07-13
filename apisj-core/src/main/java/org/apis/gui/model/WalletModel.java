package org.apis.gui.model;

import javafx.beans.property.SimpleStringProperty;
import org.apis.gui.manager.AppManager;

public class WalletModel {

    public static final int UNIT_TYPE_APIS = WalletItemModel.UNIT_TYPE_APIS;
    public static final int UNIT_TYPE_MINERAL = WalletItemModel.UNIT_TYPE_MINERAL;

    private SimpleStringProperty totalMainNatural = new SimpleStringProperty();
    private SimpleStringProperty totalMainDecimal = new SimpleStringProperty();
    private SimpleStringProperty totalSubNatural = new SimpleStringProperty();
    private SimpleStringProperty totalSubDecimal = new SimpleStringProperty();
    private SimpleStringProperty totalApisNatural = new SimpleStringProperty();
    private SimpleStringProperty totalApisDecimal = new SimpleStringProperty();
    private SimpleStringProperty totalMineralNatural = new SimpleStringProperty();
    private SimpleStringProperty totalMineralDecimal = new SimpleStringProperty();

    public WalletModel(){
        setBalance("0");
        setMineral("0");
    }

    public void setBalance(String balance){
        String[] balanceSlipt = AppManager.addDotWidthIndex(balance).split("\\.");

        this.totalApisNatural.setValue(balanceSlipt[0]);
        this.totalApisDecimal.setValue("." + balanceSlipt[1]);
    }
    public void setMineral(String balance){
        String[] balanceSlipt = AppManager.addDotWidthIndex(balance).split("\\.");

        this.totalMineralNatural.setValue(balanceSlipt[0]);
        this.totalMineralDecimal.setValue("." + balanceSlipt[1]);
    }

    public void setTotalType(int unitType){
        totalMainNatural.unbind();
        totalMainDecimal.unbind();
        totalSubNatural.unbind();
        totalSubDecimal.unbind();
        switch (unitType){
            case UNIT_TYPE_APIS :
                totalMainNatural.bind(totalApisNatural);
                totalMainDecimal.bind(totalApisDecimal);
                totalSubNatural.bind(totalMineralNatural);
                totalSubDecimal.bind(totalMineralDecimal);
                break;
            case UNIT_TYPE_MINERAL:
                totalMainDecimal.bind(totalMineralNatural);
                totalMainDecimal.bind(totalMineralDecimal);
                totalSubNatural.bind(totalApisNatural);
                totalSubNatural.bind(totalApisDecimal);
                break;
        }
    }

    public String getTotalMainNatural() {
        return totalMainNatural.get();
    }

    public SimpleStringProperty totalMainNaturalProperty() {
        return totalMainNatural;
    }

    public String getTotalMainDecimal() {
        return totalMainDecimal.get();
    }

    public SimpleStringProperty totalMainDecimalProperty() {
        return totalMainDecimal;
    }

    public String getTotalSubNatural() {
        return totalSubNatural.get();
    }

    public SimpleStringProperty totalSubNaturalProperty() {
        return totalSubNatural;
    }

    public String getTotalSubDecimal() {
        return totalSubDecimal.get();
    }

    public SimpleStringProperty totalSubDecimalProperty() {
        return totalSubDecimal;
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

    public String getTotalApisDecimal() {
        return totalApisDecimal.get();
    }

    public SimpleStringProperty totalApisDecimalProperty() {
        return totalApisDecimal;
    }

    public void setTotalApisDecimal(String totalApisDecimal) {
        this.totalApisDecimal.set(totalApisDecimal);
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

    public String getTotalMineralDecimal() {
        return totalMineralDecimal.get();
    }

    public SimpleStringProperty totalMineralDecimalProperty() {
        return totalMineralDecimal;
    }

    public void setTotalMineralDecimal(String totalMineralDecimal) {
        this.totalMineralDecimal.set(totalMineralDecimal);
    }
}
