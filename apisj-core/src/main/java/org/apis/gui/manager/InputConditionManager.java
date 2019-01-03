package org.apis.gui.manager;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class InputConditionManager {
    private static InputConditionManager ourInstance = new InputConditionManager();

    public static InputConditionManager getInstance() {
        return ourInstance;
    }

    private InputConditionManager() {
    }

    public static String returnFilterAddress(String address){
        // remove "0x"
        if(address.indexOf("0x") == 0){
            address = address.replaceAll("0x", "");
        }

        // check hex code
        if (!address.matches("[0-9a-fA-F]*")) {
            address = address.replaceAll("[^0-9a-fA-F]", "");
        }

        // check length
        int maxLength = 40;
        if(address.length() > maxLength){
            address = address.substring(0, maxLength);
        }

        return address;
    }

    public static String returnFilterNumber(String number){
        number = number.replaceAll("[^0-9.]","");
        String[] numbers = number.split("\\.");
        if(numbers.length == 0){
        }else if(numbers.length == 1){
            if(number.indexOf(".") < 0){
            }else if(number.indexOf(".") == 0){
            }else if(number.indexOf(".") == number.length()-1){
                number = numbers[0]+".";
            }else{
                number = numbers[0];
            }
        }else if(numbers.length > 1){
            number = numbers[0] + "." + numbers[1];
        }
        return number;
    }

    public static String returnFilterInteger(String number) {
        number = number.replaceAll("[^0-9]", "");

        return number;
    }


    public static String returnFilterApis(String value, ApisUtil.Unit from, ApisUtil.Unit to){
        return ApisUtil.convert(value, from, to, ',',false);
    }


    public static ChangeListener<String> addressListener() {
        return (observable, oldValue, newValue) -> {
            StringProperty string = (StringProperty) observable;
            string.setValue(returnFilterAddress(newValue));
        };
    }

    public static ChangeListener<String> onlyNumberListener() {
        return (observable, oldValue, newValue) -> {
            StringProperty string = (StringProperty) observable;
            string.set(returnFilterNumber(newValue));
        };
    }

    public static ChangeListener<String> onlyIntegerListener() {
        return (observable, oldValue, newValue) -> {
            StringProperty string = (StringProperty) observable;
            string.set(returnFilterInteger(newValue));
        };
    }
}
