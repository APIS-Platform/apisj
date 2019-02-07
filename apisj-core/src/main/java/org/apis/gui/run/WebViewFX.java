package org.apis.gui.run;
import com.google.gson.internal.LinkedTreeMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apis.contract.EstimateTransaction;
import org.apis.contract.EstimateTransactionResult;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.crypto.ECKey;
import org.apis.facade.Apis;
import org.apis.facade.ApisFactory;
import org.apis.facade.ApisImpl;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.adapter.CanvasAdapter;
import org.apis.rpc.template.MessageApp3;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.ResourceFileUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.apis.rpc.RPCJsonUtil.createJson;

public class WebViewFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private JavaConnector javaConnector = new JavaConnector();

    private static Apis mApis;

    private byte[] from;

    private WebEngine webEngine;
    private CanvasAdapter canvasAdapter;

    public void start(Stage stage) {
        stage.setTitle("Smart Contract on Canvas");

        mApis = ApisFactory.createEthereum();

        WebView webView = new WebView();
        webEngine = webView.getEngine();
        canvasAdapter = new CanvasAdapter(webEngine, true);

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            ConsoleUtil.printlnBlue("WebEngine newValue : " + newValue);

            // if(Worker.State.READY == newValue) {}

            // if(Worker.State.SCHEDULED == newValue) {}

            if(Worker.State.RUNNING == newValue) {
                webEngine.executeScript(
                        "console.log = function(message)\n" +
                        "{\n" +
                        "    canvas.log(message);\n" +
                        "};");

                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("apisProvider", javaConnector);

                webEngine.executeScript(loadUnderscoreJS());
                webEngine.executeScript(loadCanvasProviderJS());
            }

            // if(Worker.State.SUCCEEDED == newValue) {}
        });

        webEngine.load("http://192.168.0.63:3001");
        //webEngine.load("http://207.148.108.113/floro.php");

        AnchorPane anchorPane = new AnchorPane(webView);
        Scene scene = new Scene(anchorPane, 1280, 720);

        AnchorPane.setTopAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);

        stage.setScene(scene);
        stage.show();
    }


    /**
     * app3js의 givenProvider를 활성화하는 자바스크립트 코드를 불러온다.
     * @return javascript source code
     */
    private String loadCanvasProviderJS() {
        String filename = "js/CanvasProvider.js";
        return ResourceFileUtil.readFile(filename);
    }

    private String loadUnderscoreJS() {
        String filename = "js/underscore-min.js";
        return ResourceFileUtil.readFile(filename);
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public class JavaConnector {

        public void send(String payload) {
            ConsoleUtil.printlnPurple(payload);

            Platform.runLater(() -> {
                // method를 확인해야한다. 서명 관련된 method일 경우, 서명 팝업을 띄워줘야한다.
                // 새로 서명 후, 로우 트랜잭션을 교체해서 집어넣어야한다.
                MessageApp3 message = RPCCommand.parseMessage(payload);

                if(message.getMethod() != null && message.getMethod().equalsIgnoreCase("apis_sendTransaction")) {

                    Repository repo = ((Repository)mApis.getRepository()).getSnapshotTo(mApis.getBlockchain().getBestBlock().getStateRoot());
                    LinkedTreeMap params = (LinkedTreeMap) message.getParams().get(0);

                    // TODO 사용자가 어떤 주소로 트랜잭션을 전송할지 sender 변수에 주소를 선택해둬야 한다.
                    ECKey sender = ECKey.DUMMY;
                    from = sender.getAddress();


                    byte[] to = ByteUtil.hexStringToBytes((String) params.get("to"));
                    byte[] gasPrice = ByteUtil.hexStringToBytes((String) params.get("gasPrice"));
                    byte[] nonce;
                    byte[] value;
                    byte[] data = ByteUtil.EMPTY_BYTE_ARRAY;
                    byte[] toMask = null;
                    byte[] gasLimit = ByteUtil.EMPTY_BYTE_ARRAY;

                    // nonce 확인
                    if(params.keySet().contains("nonce")) {
                        nonce = ByteUtil.hexStringToBytes((String) params.get("nonce"));
                    } else {
                        nonce = ByteUtil.bigIntegerToBytes(repo.getNonce(from));
                    }

                    // data
                    if(params.keySet().contains("data")) {
                        data = ByteUtil.hexStringToBytes((String) params.get("data"));
                    }

                    // mask
                    String mask = repo.getMaskByAddress(to);
                    if(mask != null && !mask.isEmpty()) {
                        toMask = mask.getBytes(StandardCharsets.UTF_8);
                    }

                    // value
                    if(params.keySet().contains("value")) {
                        value = ByteUtil.hexStringToBytes((String) params.get("value"));
                    } else {
                        value = ByteUtil.bigIntegerToBytes(BigInteger.ZERO);
                    }

                    // gaslimit
                    if(params.keySet().contains("gasLimit")) {
                        gasLimit = ByteUtil.hexStringToBytes((String) params.get("gasLimit"));
                    } else {
                        // data 가 없는 경우, 기본 트랜잭션 가스 리밋을 적용한다.
                        if(FastByteComparisons.equal(data, ByteUtil.EMPTY_BYTE_ARRAY)) {
                            gasLimit = ByteUtil.longToBytesNoLeadZeroes(200_000L);
                        }
                    }

                    Transaction tx;
                    if(toMask == null) {
                        tx = new Transaction(nonce, gasPrice, gasLimit, to, value, data, mApis.getChainIdForNextBlock());
                    } else {
                        tx = new Transaction(nonce, gasPrice, gasLimit, to, toMask, value, data, mApis.getChainIdForNextBlock());
                    }
                    tx.sign(sender);

                    EstimateTransactionResult result = EstimateTransaction.getInstance((ApisImpl) mApis).estimate(tx);

                    if(!result.isSuccess()) {
                        // 에러 메시지 전달
                        String response = createJson(message.getId(), message.getMethod(), null, "This transactions is highly likely to fail.\n" + result.getError());
                        canvasAdapter.send(response);
                    }
                    else {
                        if (FastByteComparisons.equal(gasLimit, ByteUtil.EMPTY_BYTE_ARRAY)) {
                            gasLimit = ByteUtil.longToBytesNoLeadZeroes(result.getGasUsed());

                            if (toMask == null) {
                                tx = new Transaction(nonce, gasPrice, gasLimit, to, value, data, mApis.getChainIdForNextBlock());
                            } else {
                                tx = new Transaction(nonce, gasPrice, gasLimit, to, toMask, value, data, mApis.getChainIdForNextBlock());
                            }

                            // TODO 이 시점에 팝업을 띄워서 서명할 수 있도록 해야한다.
                            tx.sign(sender);

                            //mApis.submitTransaction(tx);

                            // txHash 전달
                            String response = createJson(message.getId(), message.getMethod(), ByteUtil.toHexString0x(tx.getHash()));
                            canvasAdapter.send(response);
                        }
                    }
                }

                else {
                    String response = RPCCommand.conduct(mApis, payload, canvasAdapter);
                    if(response != null) {
                        ConsoleUtil.printlnRed(response);
                    }

                    canvasAdapter.send(response);
                }
            });
        }

        public void log(String message) {
            ConsoleUtil.printlnBlue("Console : " + message);
        }
    }
}
