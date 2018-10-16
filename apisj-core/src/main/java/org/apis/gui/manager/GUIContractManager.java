package org.apis.gui.manager;

import org.apis.core.CallTransaction;

public class GUIContractManager {
    private static GUIContractManager ourInstance = new GUIContractManager();

    public static GUIContractManager getInstance() {
        return ourInstance;
    }

    private GUIContractManager() {
    }

    public static boolean isReadMethod(CallTransaction.Function function){
        String funcStateMutability = (function.stateMutability != null) ? function.stateMutability.name() : "null";
        if("view".equals(funcStateMutability)
                || "pure".equals(funcStateMutability)){
            // Read
            return true;
        }else {
            // Write
            return false;
        }
    }
}
