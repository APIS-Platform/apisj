package org.apis.gui.run;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apis.gui.common.OSInfo;
import org.apis.gui.controller.IntroController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

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

            if(OSInfo.getOs() == OSInfo.OS.MAC){
                URL iconURL = new File("apisj-core/src/main/resources/image/favicon_128.png").toURI().toURL();
                java.awt.Image image = new ImageIcon(iconURL).getImage();

//                com.apple.eawt.Application.getApplication().setDockIconImage(image);
            } else {
                primaryStage.getIcons().add(new Image("image/ic_favicon@2x.png"));
            }

            Parent root = FXMLLoader.load(fileUrl);
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
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
