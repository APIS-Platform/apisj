package org.apis.rpc.template;

import org.apis.util.ByteUtil;

import java.nio.charset.Charset;
import java.util.List;

public class MessageWeb3 {
    private final String UTF8 = "UTF-8";
    private String jsonrpc;
    private long id;
    private String method;
    private List<String> params;

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

    public List<String> getParams() {
        return params;
    }

    public String getMergedParams() {
        StringBuilder merged = new StringBuilder();
        for(String param : params) {
            merged.append(param);
        }

        return merged.toString();
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
