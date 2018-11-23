package org.apis.hid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StateCode {
    private static final Map<Integer, String> codes;
    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(0x63c0, "PIN_REMAINING_ATTEMPTS");
        map.put(0x6700, "INCORRECT_LENGTH");
        map.put(0x6981, "COMMAND_INCOMPATIBLE_FILE_STRUCTURE");
        map.put(0x6982, "SECURITY_STATUS_NOT_SATISFIED");
        map.put(0x6985, "CONDITIONS_OF_USE_NOT_SATISFIED");
        map.put(0x6a80, "INCORRECT_DATA");
        map.put(0x6a84, "NOT_ENOUGH_MEMORY_SPACE");
        map.put(0x6a88, "REFERENCED_DATA_NOT_FOUND");
        map.put(0x6a89, "FILE_ALREADY_EXISTS");
        map.put(0x6b00, "INCORRECT_P1_P2");
        map.put(0x6d00, "INS_NOT_SUPPORTED");
        map.put(0x6e00, "CLA_NOT_SUPPORTED");
        map.put(0x6f00, "TECHNICAL_PROBLEM");
        map.put(0x9000, "OK");
        map.put(0x9240, "MEMORY_PROBLEM");
        map.put(0x9400, "NO_EF_SELECTED");
        map.put(0x9402, "INVALID_OFFSET");
        map.put(0x9404, "FILE_NOT_FOUND");
        map.put(0x9408, "INCONSISTENT_FILE");
        map.put(0x9484, "ALGORITHM_NOT_SUPPORTED");
        map.put(0x9485, "INVALID_KCV");
        map.put(0x9802, "CODE_NOT_INITIALIZED");
        map.put(0x9804, "ACCESS_CONDITION_NOT_FULFILLED");
        map.put(0x9808, "CONTRADICTION_SECRET_CODE_STATUS");
        map.put(0x9810, "CONTRADICTION_INVALIDATION");
        map.put(0x9840, "CODE_BLOCKED");
        map.put(0x9850, "MAX_VALUE_REACHED");
        map.put(0x6300, "GP_AUTH_FAILED");
        map.put(0x6f42, "LICENSING");
        map.put(0x6faa, "HALTED");

        codes = Collections.unmodifiableMap(map);
    }

    public static boolean isError(int code) {
        return code != 0x9000;
    }

    public static String stateMessage(int code) {
        String message = codes.get(code);
        if(message == null) {
            return "";
        }
        return message;
    }
}
