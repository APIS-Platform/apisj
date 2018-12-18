package org.apis.gui.run;
import com.mohamnag.fxwebview_debugger.DevToolsDebuggerServer;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apis.util.ConsoleUtil;

public class WebViewFx  extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private JSObject javascriptConnector;
    private JavaConnector javaConnector = new JavaConnector();

    public void start(Stage stage) throws Exception {
        stage.setTitle("WebView");


        WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            ConsoleUtil.printlnRed("Old : " + oldValue);
            ConsoleUtil.printlnRed("New : " + newValue);
            if(Worker.State.READY == newValue) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("apisProvider", javaConnector);
            }

            if(Worker.State.SUCCEEDED == newValue) {
                webEngine.executeScript("console.log = function(message)\n" +
                        "{\n" +
                        "    apisProvider.log(message);\n" +
                        "};");

                //webEngine.executeScript("global.apisProvider = \"12312\";");

                // set an interface object named 'javaConnector' in the web engine's page
                //JSObject window = (JSObject) webEngine.executeScript("window");
                //window.setMember("apisProvider", javaConnector);

                // get the Javascript connector object.
                //javascriptConnector = (JSObject) webEngine.executeScript("alert(apisProvider)");
            }
        });

        DevToolsDebuggerServer.startDebugServer(webEngine.impl_getDebugger(), 51742);
        /*Class webEngineClazz = WebEngine.class;

        Field debuggerField = webEngineClazz.getDeclaredField("debugger");
        debuggerField.setAccessible(true);

        Debugger debugger = (Debugger)debuggerField.get(webView.getEngine());
        DevToolsDebuggerServer.startDebugServer();*/

        webEngine.load("http://192.168.0.73:3000");

        AnchorPane anchorPane = new AnchorPane(webView);
        Scene scene = new Scene(anchorPane, 1280, 720);

        AnchorPane.setTopAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);

        stage.setScene(scene);
        stage.show();
    }

    public class JavaConnector {
        public boolean sendAsync = false;

        public void toLowerCase(String value) {
            if(null != value) {
                javascriptConnector.call("showResult", value.toLowerCase());
            }
        }

        public void send(String ddd) {
            ConsoleUtil.printlnPurple(ddd);
        }

        public void log(String message) {
            ConsoleUtil.printlnBlue(message);
        }
    }
}
