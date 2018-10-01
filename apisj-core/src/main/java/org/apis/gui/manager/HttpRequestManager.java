package org.apis.gui.manager;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequestManager {
    private static final String REQUEST_PUBLIC_DOMAIN_URL ="http://wagi.xyz:1989/me/";

    private static String sendRequest(URL url, Map<String, Object> params) throws MalformedURLException, UnsupportedEncodingException, ProtocolException, IOException {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);
        String response = sb.toString();
        return response;
    }

    /**
     *
     * ex)
     * Map<String, Object> params = new LinkedHashMap<>();
     *             params.put("domain" , domain);
     *             params.put("message" , message);
     *             params.put("email" , email);
     * String response = HttpRequestManager.sendRequestPublicDomain(params);
     *
     * @param params
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     * @throws ProtocolException
     * @throws IOException
     */
    public static String sendRequestPublicDomain(Map<String, Object> params) throws MalformedURLException, UnsupportedEncodingException, ProtocolException, IOException{
        String result = null;
        URL url = new URL(REQUEST_PUBLIC_DOMAIN_URL);
        result = sendRequest(url,params);
        return result;

    }

    public static String sendRequestPublicDomain(String domain, String message, String email) throws MalformedURLException, UnsupportedEncodingException, ProtocolException, IOException{
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("domain" , domain);
        params.put("message" , message);
        params.put("email" , email);
        return sendRequestPublicDomain(params);
    }
}
