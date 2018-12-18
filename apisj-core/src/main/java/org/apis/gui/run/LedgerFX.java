package org.apis.gui.run;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class LedgerFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Ledger Control");

        URL fileUrl = getClass().getClassLoader().getResource("scene/ledger.fxml");
        Parent root = FXMLLoader.load(fileUrl);
        stage.setScene(new Scene(root));
        stage.setMinWidth(560);
        stage.setMinHeight(560);
        stage.show();
    }
}
