package org.apis.gui.run;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class MainFX extends Application  {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-Bold.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-Light.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-Regular.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-SemiBold.ttf").toURI().toURL().toString(), 14 );

        File file = new File("apisj-core/src/main/resources/scene/intro.fxml");
        URL fileUrl = file.toURI().toURL();
        file = null;
        Parent root = FXMLLoader.load(fileUrl);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
