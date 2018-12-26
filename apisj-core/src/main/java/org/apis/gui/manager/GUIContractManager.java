package org.apis.gui.manager;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apis.core.CallTransaction;
import org.apis.solidity.SolidityType;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

    public static Object convertStringToArray(CallTransaction.Param param, String strData){
        strData = strData.replaceAll("\\[","").replaceAll("]","").replaceAll("\"","").replaceAll(" ", "");
        String[] dataSplit = strData.split(",");

        if(param.type.getCanonicalName().indexOf("int") >=0){
            List<BigInteger> list = new ArrayList<>();
            for(int j=0; j<dataSplit.length; j++){
                if(dataSplit[j].length() != 0){
                    list.add(new BigInteger(dataSplit[j]));
                }
            }
            return list;
        } else if(param.type.getCanonicalName().indexOf("bool") >=0) {
            List<Boolean> list = new ArrayList<>();
            for(int j=0; j<dataSplit.length; j++){
                if(dataSplit[j].length() != 0){
                    list.add(Boolean.parseBoolean(dataSplit[j]));
                }
            }
            return list;
        } else {
            List<String> list = new ArrayList<>();
            for(int j=0; j<dataSplit.length; j++){
                if(dataSplit[j].length() != 0){
                    list.add(dataSplit[j]);
                }
            }
            return list;
        }
    }
    public static String convertArrayToString(CallTransaction.Param param, Object[] array){
        if(param.type.getCanonicalName().indexOf("int") >=0){
            List<BigInteger> list = new ArrayList<>();
            for(int j=0; j<array.length;j++){
                list.add(new BigInteger(""+array[j]));
            }
            return list.toString();
        }else if(param.type.getCanonicalName().indexOf("address") >=0){
            List<String> list = new ArrayList<>();
            for(int j=0; j<array.length;j++){
                list.add(Hex.toHexString((byte[]) array[j]));
            }
            return list.toString();
        }else if(param.type.getCanonicalName().indexOf("bool") >=0){
            List<Boolean> list = new ArrayList<>();
            for(int j=0; j<array.length;j++){
                list.add((Boolean)array[j]);
            }
            return list.toString();
        }else{
            List<String> list = new ArrayList<>();
            for(int j=0; j<array.length;j++){
                list.add((String)array[j]);
            }
            return list.toString();
        }
    }

    public static Object[] getContractArgs(CallTransaction.Param[] params, ArrayList<Object> dataParams){
        Object[] args = new Object[params.length];
        for (int i = 0; i < dataParams.size(); i++) {
            if(params[i].type instanceof SolidityType.BoolType){
                SimpleBooleanProperty property = (SimpleBooleanProperty) dataParams.get(i);
                args[i] = property.get();
            }else if(params[i].type instanceof SolidityType.StringType){
                SimpleStringProperty property = (SimpleStringProperty) dataParams.get(i);
                args[i] = property.get();
            }else if(params[i].type instanceof SolidityType.ArrayType){
                SimpleStringProperty property = (SimpleStringProperty) dataParams.get(i);
                String strData = property.get();
                args[i] = GUIContractManager.convertStringToArray(params[i], strData);
            }else if(params[i].type instanceof SolidityType.FunctionType){

            }else if(params[i].type instanceof SolidityType.BytesType){
                SimpleStringProperty property = (SimpleStringProperty) dataParams.get(i);
                if(property.get().length() == 0){
                    args[i] = ByteUtil.hexStringToBytes("0");
                }else{
                    args[i] = ByteUtil.hexStringToBytes(property.get());
                }
                args[i] = ByteUtil.hexStringToBytes(property.get());
            }else if(params[i].type instanceof SolidityType.AddressType){
                SimpleStringProperty property = (SimpleStringProperty) dataParams.get(i);
                if(property.get().length() == 0){
                    args[i] = ByteUtil.hexStringToBytes("0000000000000000000000000000000000000000");
                }else{
                    args[i] = ByteUtil.hexStringToBytes(property.get());
                }
            }else if(params[i].type instanceof SolidityType.IntType){
                SimpleStringProperty property = (SimpleStringProperty) dataParams.get(i);
                BigInteger integer = new BigInteger((property.get() == null || property.get().equals(""))?"0":property.get());
                args[i] = integer;
            }else if(params[i].type instanceof SolidityType.Bytes32Type){
                SimpleStringProperty property = (SimpleStringProperty) dataParams.get(i);
                if(property.get().length() == 0){
                    args[i] = ByteUtil.hexStringToBytes("0");
                }else{
                    args[i] = ByteUtil.hexStringToBytes(property.get());
                }
                args[i] = ByteUtil.hexStringToBytes(property.get());
            }
        }
        return args;
    }
}
