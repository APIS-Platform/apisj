package org.apis.gui.run;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apis.contract.ContractLoader;
import org.apis.facade.Apis;
import org.apis.facade.ApisFactory;
import org.apis.rpc.RPCCommand;
import org.apis.util.ConsoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebViewFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private JavaConnector javaConnector = new JavaConnector();

    private static final Logger logger = LoggerFactory.getLogger("webViewFx");
    private static Apis mApis;

    private WebEngine webEngine;

    public void start(Stage stage) {
        stage.setTitle("Smart Contract on Canvas");

        mApis = ApisFactory.createEthereum();

        WebView webView = new WebView();
        webEngine = webView.getEngine();

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

                webEngine.executeScript(loadCanvasProviderJS());
            }

            // if(Worker.State.SUCCEEDED == newValue) {}
        });



        webEngine.load("http://192.168.0.63:3000");
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
        try (InputStream is = ContractLoader.class.getClassLoader().getResourceAsStream("js/CanvasProvider.js")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }

            return out.toString();
        } catch (Exception e) {
            logger.error("Problem loading contract file from js/CanvasProvider.js");
            e.printStackTrace();
        }

        return null;
    }


    private void onMessage(String result) {
        webEngine.executeScript("canvasProvider.onMessage('" + result + "');");
        webEngine.executeScript("console.log('" + result + "');");
    }


    public class JavaConnector {

        public void send(String payload) {
            ConsoleUtil.printlnPurple(payload);

            Platform.runLater(() -> {
                String response = RPCCommand.conduct(mApis, payload);
                ConsoleUtil.printlnRed(response);
                onMessage(response);
            });
        }

        public void log(String message) {
            ConsoleUtil.printlnBlue("Console : " + message);
        }
    }
}
