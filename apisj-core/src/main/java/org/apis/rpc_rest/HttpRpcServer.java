package org.apis.rpc_rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apis.facade.Apis;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.RPCServerManager;
import org.apis.util.ConsoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * app3js 라이브러리 중 app3-providers-http 연결을 위한 HTTP Server를 구현하였다.<br/>
 * 서버 접근을 위한 ID와 PW, Allowed IP 주소 등의 값은 WS RPC 서버와 같은 값을 공유한다.
 */
public class HttpRpcServer {

    private final Logger logger = LoggerFactory.getLogger("HTTP_RPC");
    private HttpServer mServer;
    private int port;
    private int nThreads;
    private Apis mApis;
    private String rpcId, rpcPw;
    private boolean isRunning;
    private boolean isEnabled;
    private List<String> allowedAddressList;

    public HttpRpcServer(Apis apis) throws IOException {
        this.mApis = apis;

        RPCServerManager rpcServerManager = RPCServerManager.getInstance();
        this.port = rpcServerManager.getHttpPort();
        this.nThreads = rpcServerManager.getHttpThreadPoolSize();
        this.rpcId = rpcServerManager.getId();
        this.rpcPw = rpcServerManager.getPassword();
        this.isEnabled = rpcServerManager.isHttpServerEnabled();
        this.isRunning = false;
        this.allowedAddressList = getAllowedAddressList(rpcServerManager.getWhitelist());

        mServer = HttpServer.create(new InetSocketAddress(this.port), 0);
        HttpContext rpcContext = mServer.createContext("/", new RpcHandler());

        // https://www.rgagnon.com/javadetails/java-do-basic-authentication-using-jdk-http-server.html
        rpcContext.setAuthenticator(new BasicAuthenticator("get") {
            @Override
            public Result authenticate(HttpExchange httpExchange) {
                if(httpExchange.getRequestHeaders().getFirst("origin") != null) {
                    httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", httpExchange.getRequestHeaders().getFirst("origin"));

                    if(httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POSPayload:T, OPTIONS");
                        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin,Content-Type,Authorization");

                        try {
                            httpExchange.sendResponseHeaders(204, -1);
                        } catch (IOException e) {
                            logger.warn("Error occurs in authenticate", e);
                            e.printStackTrace();
                        }
                    }
                }

                return super.authenticate(httpExchange);
            }

            @Override
            public boolean checkCredentials(String user, String password) {
                return user.equals(rpcId) && password.equals(rpcPw);
            }
        });
    }


    /**
     * HTTP RPC 서버를 구동시킨다.<br/>
     * 만약 이미 서버가 동작 중이거나 설정에서 기능을 비활성화 했을 경우 구동을 생략한다.
     * Run the HTTP RPC server.<br/>
     * If the server is already running or the feature is disabled in the configuration, do not start it.
     *
     */
    public void start() {
        if(!isRunning && isEnabled) {
            mServer.setExecutor(Executors.newFixedThreadPool(this.nThreads));
            mServer.start();

            isRunning = true;

            ConsoleUtil.printlnCyan("HTTP RPC Server is running (%d)", port);
        }
    }

    class RpcHandler implements HttpHandler {

        /**
         * RPC 요청을 처리한다.
         * Handles RPC requests.
         * @param httpExchange HttpExchange
         * @throws IOException If an error occurs while processing the response from the request
         */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                // check allowed host
                InetSocketAddress remote = httpExchange.getRemoteAddress();
                if(!checkAllowedAddress(remote)) {
                    writeErrorResponse(httpExchange, 403, RPCCommand.ERROR_CODE_WITHOUT_PERMISSION_IP, RPCCommand.ERROR_DEPORT_WITHOUT_PERMISSION_IP);
                    return;
                }


                // retrieve the request json data
                String data = getRequestData(httpExchange);
                String response = RPCCommand.conduct(mApis, data);
                if(response == null) {
                    response = "";
                }

                writeResponse(httpExchange, response);

            } catch (IOException e) {
                writeErrorResponse(httpExchange, 500, 500, "This command can not be processed by the RPC server");
                logger.warn(e.getMessage());
            }
        }

        /**
         * retrieve the request json string
         *
         * @param httpExchange HttpExchange
         * @return The requested JSON string
         * @throws IOException If an error occurs while retrieving the requested JSON string
         */
        private String getRequestData(HttpExchange httpExchange) throws IOException {
            InputStream is = httpExchange.getRequestBody();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = is.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            is.close();

            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        }

        /**
         * 클라이언트의 IP 주소에 대한 접근이 허용되어 있는지 확인한다.
         * Verify that access to the client's IP address is allowed.
         *
         * @param remote IP address of remote client
         * @return TRUE if allowed
         */
        private boolean checkAllowedAddress(InetSocketAddress remote) {
            if(allowedAddressList.indexOf("0.0.0.0") < 0) {
                String guestAddress = remote.getAddress().toString();
                guestAddress = guestAddress.replaceAll("[^0-9.]", "");

                if(allowedAddressList.indexOf(guestAddress) < 0) {
                    logger.warn(ConsoleUtil.colorBRed("The IP address of the client[{}] can not be found in the whitelist."), guestAddress);
                    return false;
                }
            }

            return true;
        }
    }



    /**
     * RPC 요청에 대한 응답을 전송한다.
     * Sends a response to the RPC request.
     *
     * @param httpExchange HttpExchange
     * @param response JSON type response to RPC request
     * @throws IOException If an error occurs during HTTP response creation
     */
    private void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        httpExchange.sendResponseHeaders(200, response.length());

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    /**
     * RPC 응답으로 에러 메시지를 전송한다.
     * It sends an error message in response to the RPC request.
     *
     * @param httpExchange HttpExchange
     * @param status HTTP status code
     * @param errorCode RPC error code
     * @param errorMsg RPC error message
     * @throws IOException If an error occurs during HTTP response creation
     */
    private void writeErrorResponse(HttpExchange httpExchange, int status, int errorCode, String errorMsg) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(RPCCommand.TAG_CODE, errorCode);
        jsonObject.addProperty(RPCCommand.TAG_ERROR, errorMsg);

        String response = new Gson().toJson(jsonObject);

        httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        httpExchange.sendResponseHeaders(status, response.length());

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }


    /**
     * String 형태(,로 구분)로 저장된 IP 주소들을 List 형식으로 변경한다.
     * Converts IP addresses stored in a comma(,) to a List object.
     *
     * @param addresses allowed IP addresses (
     * @return IP addresses converted to List objects
     */
    private List<String> getAllowedAddressList(String addresses) {
        List<String> addressList = new ArrayList<>();

        String[] addressArray = addresses.split(",");
        for(String address : addressArray) {
            address = address.trim();
            if(InetAddressValidator.getInstance().isValid(address)) {
                addressList.add(address);
            }
        }

        return addressList;
    }
}
