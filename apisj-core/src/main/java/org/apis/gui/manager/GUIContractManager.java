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

    public static byte[] getTransferData(CompilationResult.ContractMetadata metadata, CallTransaction.Function function, ArrayList<Object> contractParams){
        // 데이터 불러오기
        Object[] args = new Object[function.inputs.length];
        for (int i = 0; i < contractParams.size(); i++) {
            if(function.inputs[i].type instanceof SolidityType.BoolType){
                SimpleBooleanProperty property = (SimpleBooleanProperty) contractParams.get(i);
                args[i] = property.get();
            }else if(function.inputs[i].type instanceof SolidityType.StringType){
                SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                args[i] = property.get();
            }else if(function.inputs[i].type instanceof SolidityType.ArrayType){
                SimpleStringProperty property = (SimpleStringProperty)contractParams.get(i);
                String strData = property.get();
                strData = strData.replaceAll("\\[","").replaceAll("]","").replaceAll("\"","");
                String[] dataSplit = strData.split(",");

                if(function.inputs[i].type.getCanonicalName().indexOf("int") >=0){
                    List<BigInteger> list = new ArrayList<>();
                    for(int j=0; j<dataSplit.length; j++){
                        if(dataSplit[j].length() != 0){
                            list.add(new BigInteger(dataSplit[j]));
                        }
                    }
                    args[i] = list;
                }else{
                    List<String> list = new ArrayList<>();
                    for(int j=0; j<dataSplit.length; j++){
                        if(dataSplit[j].length() != 0){
                            list.add(dataSplit[j]);
                        }
                    }
                    args[i] = list;
                }
            }else if(function.inputs[i].type instanceof SolidityType.FunctionType){

            }else if(function.inputs[i].type instanceof SolidityType.BytesType){
                SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                args[i] = Hex.decode(property.get());
            }else if(function.inputs[i].type instanceof SolidityType.AddressType){
                SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                args[i] = Hex.decode(property.get());
            }else if(function.inputs[i].type instanceof SolidityType.IntType){
                SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                BigInteger integer = new BigInteger((property.get().length() == 0) ? "0" : property.get());
                args[i] = integer;
            }else if(function.inputs[i].type instanceof SolidityType.Bytes32Type){
                SimpleStringProperty property = (SimpleStringProperty) contractParams.get(i);
                args[i] = Hex.decode(property.get());
            }

        }

        return ByteUtil.merge(Hex.decode(metadata.bin), function.encodeArguments(args));
    }
}
