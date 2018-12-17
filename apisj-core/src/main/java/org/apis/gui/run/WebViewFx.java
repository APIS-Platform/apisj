package org.apis.gui.run;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebViewFx  extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("WebView");

        WebView webView = new WebView();
        webView.getEngine().load("http://google.com");

        AnchorPane anchorPane = new AnchorPane(webView);
        Scene scene = new Scene(anchorPane, 1280, 720);

        AnchorPane.setTopAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);

        stage.setScene(scene);
        stage.show();
    }
}
