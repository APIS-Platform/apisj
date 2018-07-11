package org.apis.gui.model;

import javafx.beans.property.SimpleStringProperty;

public class WalletModel {
    private SimpleStringProperty alias = new SimpleStringProperty();
    private SimpleStringProperty address = new SimpleStringProperty();
    private SimpleStringProperty natural = new SimpleStringProperty();
    private SimpleStringProperty decimal = new SimpleStringProperty();
    private SimpleStringProperty unit = new SimpleStringProperty();


    public SimpleStringProperty getAlias(){ return this.alias; }
    public SimpleStringProperty getAddress(){ return this.address; }
    public SimpleStringProperty getNatural(){ return this.natural; }
    public SimpleStringProperty getDecimal(){ return this.decimal; }
    public SimpleStringProperty getUnit(){ return this.unit; }
}
