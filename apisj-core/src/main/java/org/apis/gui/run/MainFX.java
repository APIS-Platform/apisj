package org.apis.gui.run;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apis.core.Repository;
import org.apis.gui.controller.IntroController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class MainFX extends Application  {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);

        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-Bold.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-Light.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-Regular.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/OpenSans-SemiBold.ttf").toURI().toURL().toString(), 14 );

        Font.loadFont(new File("apisj-core/src/main/resources/font/RobotoMono-Bold.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/RobotoMono-Light.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/RobotoMono-Regular.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/RobotoMono-Medium.ttf").toURI().toURL().toString(), 14 );

        Font.loadFont(new File("apisj-core/src/main/resources/font/NotoSansKR-Bold.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/NotoSansKR-Light.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/NotoSansKR-Regular.ttf").toURI().toURL().toString(), 14 );
        Font.loadFont(new File("apisj-core/src/main/resources/font/NotoSansKR-Medium.ttf").toURI().toURL().toString(), 14 );

        for(String fontName : javafx.scene.text.Font.getFamilies()){
            System.out.println("fontName : "+fontName);
        }


        int size = AppManager.getInstance().keystoreFileReadAll().size();
        URL fileUrl = new File("apisj-core/src/main/resources/scene/intro.fxml").toURI().toURL();
        fileUrl = (size > 0) ? new File("apisj-core/src/main/resources/scene/main.fxml").toURI().toURL() : fileUrl;

        if(fileUrl != null) {
            Parent root = FXMLLoader.load(fileUrl);
            primaryStage.setScene(new Scene(root));
            //primaryStage.setResizable(false);
            primaryStage.show();
        }

        //AppManager.getInstance().start();
    }

    @Override
    public void stop() {
        if(!IntroController.getDeleteKeystoreFileFlag()) {
            KeyStoreManager.getInstance().deleteKeystore();
            System.exit(0);
        }
    }

}
