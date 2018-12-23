package org.apis.gui.run;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apis.gui.common.OSInfo;
import org.apis.gui.manager.AppManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainFX extends Application  {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){

        // 블록 싱크 시작
        // DB버전 설정하는 문제로, javafx GUI 실행보다 우선 실행되어야 한다.
        AppManager.getInstance().start();

        AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);
        if("true".equals(AppManager.getWindowPropertiesData("minimize_to_tray"))){
            Platform.setImplicitExit(false);
            AppManager.getInstance().createTrayIcon(primaryStage);
        }

        Font.loadFont(getClass().getClassLoader().getResource("font/NotoSansKR-Medium.otf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/NotoSansKR-Regular.otf").toString(), 14 );

        Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Regular.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Medium.ttf").toString(), 14 );

        Font.loadFont(getClass().getClassLoader().getResource("font/Barlow-Regular.ttf").toString(), 14);
        Font.loadFont(getClass().getClassLoader().getResource("font/Barlow-SemiBold.ttf").toString(), 14);

        // TODO : 사용가능한 폰트 출력
        for(String fontName : javafx.scene.text.Font.getFamilies()){
            //System.out.println("fontName : "+fontName);
        }

        int size = AppManager.getInstance().keystoreFileReadAll().size();
        URL fileUrl = getClass().getClassLoader().getResource("scene/intro.fxml");

        fileUrl = (size > 0) ? getClass().getClassLoader().getResource("scene/main.fxml") : fileUrl;

        if(fileUrl != null) {

            if(OSInfo.getOs() == OSInfo.OS.MAC){
                URL iconURL = getClass().getClassLoader().getResource("image/favicon_128.png");

                java.awt.Image image = new ImageIcon(iconURL).getImage();

//                com.apple.eawt.Application.getApplication().setDockIconImage(image);
            } else {
                primaryStage.getIcons().add(new Image("image/ic_favicon@2x.png"));
            }

            try {
                Parent root = FXMLLoader.load(fileUrl);
                primaryStage.setScene(new Scene(root));
                primaryStage.setResizable(false);
                primaryStage.setMinWidth(1280);
                primaryStage.setMinHeight(720);
                primaryStage.setTitle("APIS Core Wallet");

                primaryStage.show();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

    }

    @Override
    public void stop() {
        System.exit(0);
    }

}
