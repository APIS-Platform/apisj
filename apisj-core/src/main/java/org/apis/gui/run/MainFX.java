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
import org.apis.gui.controller.IntroController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainFX extends Application  {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);
        if("true".equals(AppManager.getWindowPropertiesData("minimize_to_tray"))){
            Platform.setImplicitExit(false);
            createTrayIcon(primaryStage);
        }

        Font.loadFont(getClass().getClassLoader().getResource("font/OpenSans-Bold.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/OpenSans-Light.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/OpenSans-Regular.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/OpenSans-SemiBold.ttf").toString(), 14 );

        Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Bold.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Light.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Regular.ttf").toString(), 14 );
        Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Medium.ttf").toString(), 14 );

        // TODO : 사용가능한 폰트 출력
//        for(String fontName : javafx.scene.text.Font.getFamilies()){
//            System.out.println("fontName : "+fontName);
//        }


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

            Parent root = FXMLLoader.load(fileUrl);
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.setTitle("APIS Core Wallet");
            primaryStage.show();

        }

        AppManager.getInstance().start();
    }

    @Override
    public void stop() {
        if(!IntroController.getDeleteKeystoreFileFlag()) {
            KeyStoreManager.getInstance().deleteKeystore();
        }

        System.exit(0);
    }

    public void createTrayIcon(final Stage stage) {
        if(SystemTray.isSupported()) {
            java.awt.Image image = null;
            try {
                URL url  = getClass().getClassLoader().getResource("image/ic_favicon@2x.png");

                image = ImageIO.read(url);
                image = image.getScaledInstance(16,16,0);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(SystemTray.isSupported()) {
                                stage.hide();
                                SystemTray.getSystemTray().getTrayIcons()[SystemTray.getSystemTray().getTrayIcons().length-1].displayMessage("Some", "Message", TrayIcon.MessageType.INFO);
                            } else {
                                System.exit(0);
                            }
                        }
                    });
                }
            });

            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                        }
                    });
                }
            };

            // Create a Popup Menu
            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            MenuItem closeItem = new MenuItem("Close");

            showItem.addActionListener(showListener);
            closeItem.addActionListener(closeListener);

            popupMenu.add(showItem);
            popupMenu.add(closeItem);

            // Construct a TrayIcon
            try {
                TrayIcon trayIcon = new TrayIcon(image, "APIS", popupMenu);
                trayIcon.addActionListener(showListener);
                for(int i=0; i<SystemTray.getSystemTray().getTrayIcons().length; i++){
                    SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[i]);
                }
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

}
