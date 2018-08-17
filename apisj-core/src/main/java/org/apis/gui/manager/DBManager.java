package org.apis.gui.manager;

import org.apis.gui.model.MyAddressModel;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DBManager {

    public ArrayList<String> addressGroupList = new ArrayList<>();
    public ArrayList<MyAddressModel> myAddressList = new ArrayList<>();

    private static DBManager ourInstance = new DBManager();
    public static DBManager getInstance() {
        return ourInstance;
    }
    private DBManager() { }

    public void loadData(){

    }

    public void saveData(){

    }
}
