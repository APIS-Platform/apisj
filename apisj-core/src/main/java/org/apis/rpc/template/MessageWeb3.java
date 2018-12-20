package org.apis.rpc.template;

import com.google.gson.internal.LinkedTreeMap;
import org.apis.util.ByteUtil;

import java.nio.charset.Charset;
import java.util.*;

public class MessageWeb3 {
    private final String UTF8 = "UTF-8";
    private String jsonrpc;
    private long id;
    private String method;
    private List<Object> params;

    public String getJsonRpcVersion() {
        return jsonrpc;
    }

    public long getId() {
        return id;
    }

    public byte[] getIdBytes() {
        return ByteUtil.longToBytes(getId());
    }

    public String getMethod() {
        return method;
    }

    public byte[] getMethodBytes() {
        return getMethod().getBytes(Charset.forName(UTF8));
    }

    public List<Object> getParams() {
        return params;
    }

    /**
     * 파라미터 변수들을 하나의 String으로 이어 붙인다.
     * 만약, 파라미터 중에 json object가 존재하는 경우, 키 값들을 오름차순으로 정렬한 뒤에 이어 붙인다.
     * @return 직렬화 된 parameters
     */
    public String getMergedParams() {
        StringBuilder merged = new StringBuilder();
        for(Object param : params) {
            if(param instanceof LinkedTreeMap) {
                LinkedTreeMap treeMap = (LinkedTreeMap) param;
                SortedMap<String, Object> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                sorted.putAll(treeMap);


                for (String key : sorted.keySet()) {
                    Object value = sorted.get(key);
                    if(value instanceof String) {
                        merged.append(key).append((String)value);
                    }
                    else if(value instanceof ArrayList) {
                        merged.append(key);
                        for(Object vv : (ArrayList)value) {
                            if(vv instanceof String) {
                                merged.append((String)vv);
                            }
                        }
                    }
                }
                continue;
            }

            if(param != null) {
                merged.append(param.toString());
            }
        }

        return merged.toString().replaceAll("/['\"]+/g", "").replaceAll("=", "").replaceAll(":", "").replaceAll(" ", "");
    }

    public byte[] getMergedParamsBytes() {
        return getMergedParams().getBytes(Charset.forName(UTF8));
    }

    @Override
    public String toString() {
        return "MessageWeb3{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", id=" + id +
                ", method='" + method + '\'' +
                ", params=" + params +
                '}';
    }
}
