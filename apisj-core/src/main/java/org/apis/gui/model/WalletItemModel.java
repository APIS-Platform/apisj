package org.apis.gui.model;

import javafx.beans.property.SimpleStringProperty;
import org.apis.gui.manager.AppManager;

public class WalletItemModel {

    public static final int UNIT_TYPE_APIS = 0;
    public static final int UNIT_TYPE_MINERAL = 1;

    public static final String UNIT_TYPE_STRING_APIS = "APIS";
    public static final String UNIT_TYPE_STRING_MINERAL = "MNR";
    public static final String WALLET_NAME_APIS = "APIS";
    public static final String WALLET_NAME_MINERAL = "MINERAL";


    private SimpleStringProperty id = new SimpleStringProperty();
    private SimpleStringProperty alias = new SimpleStringProperty();
    private SimpleStringProperty address = new SimpleStringProperty();
    private SimpleStringProperty unit = new SimpleStringProperty();
    private SimpleStringProperty natural = new SimpleStringProperty();
    private SimpleStringProperty decimal = new SimpleStringProperty();
    private SimpleStringProperty apisNatural = new SimpleStringProperty();
    private SimpleStringProperty apisDecimal = new SimpleStringProperty();
    private SimpleStringProperty mineralNatural = new SimpleStringProperty();
    private SimpleStringProperty mineralDecimal = new SimpleStringProperty();

    private String keystoreJsonData;

    public WalletItemModel(){
        setBalance("0");
        setMineral("0");
    }

    public WalletItemModel setHeaderUnitType(int unitType) {
        natural.unbind();
        decimal.unbind();
        switch (unitType) {
            case UNIT_TYPE_APIS:
                natural.bind(apisNatural);
                decimal.bind(apisDecimal);
                this.unit.setValue(UNIT_TYPE_STRING_APIS);
                break;
            case UNIT_TYPE_MINERAL:
                natural.bind(mineralNatural);
                decimal.bind(mineralDecimal);
                this.unit.setValue(UNIT_TYPE_STRING_MINERAL);
                break;
        }
        return this;
    }
    public void setBalance(String balance){
        String[] balanceSlipt = AppManager.addDotWidthIndex(balance).split("\\.");

        this.apisNatural.setValue(balanceSlipt[0]);
        this.apisDecimal.setValue("." + balanceSlipt[1]);
    }
    public void setMineral(String balance){
        String[] balanceSlipt = AppManager.addDotWidthIndex(balance).split("\\.");

        this.mineralNatural.setValue(balanceSlipt[0]);
        this.mineralDecimal.setValue("." + balanceSlipt[1]);
    }

    public String getId() { return id.get(); }

    public SimpleStringProperty idProperty() { return id; }

    public void setId(String id) { this.id.set(id);}

    public String getAlias() {
        return alias.get();
    }

    public SimpleStringProperty aliasProperty() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias.set(alias);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getUnit() {
        return unit.get();
    }

    public SimpleStringProperty unitProperty() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit.set(unit);
    }

    public String getNatural() {
        return natural.get();
    }

    public SimpleStringProperty naturalProperty() {
        return natural;
    }

    public void setNatural(String natural) {
        this.natural.set(natural);
    }

    public String getDecimal() {
        return decimal.get();
    }

    public SimpleStringProperty decimalProperty() {
        return decimal;
    }

    public void setDecimal(String decimal) {
        this.decimal.set(decimal);
    }

    public String getApisNatural() {
        return apisNatural.get();
    }

    public SimpleStringProperty apisNaturalProperty() {
        return apisNatural;
    }

    public void setApisNatural(String apisNatural) {
        this.apisNatural.set(apisNatural);
    }

    public String getApisDecimal() {
        return apisDecimal.get();
    }

    public SimpleStringProperty apisDecimalProperty() {
        return apisDecimal;
    }

    public void setApisDecimal(String apisDecimal) {
        this.apisDecimal.set(apisDecimal);
    }

    public String getMineralNatural() {
        return mineralNatural.get();
    }

    public SimpleStringProperty mineralNaturalProperty() {
        return mineralNatural;
    }

    public void setMineralNatural(String mineralNatural) {
        this.mineralNatural.set(mineralNatural);
    }

    public String getMineralDecimal() {
        return mineralDecimal.get();
    }

    public SimpleStringProperty mineralDecimalProperty() {
        return mineralDecimal;
    }

    public void setMineralDecimal(String mineralDecimal) {
        this.mineralDecimal.set(mineralDecimal);
    }

    public void setKeystoreJsonData(String keystoreJsonData) { this.keystoreJsonData = keystoreJsonData; }

    public String getKstoreJsonData() { return this.keystoreJsonData; }
}
