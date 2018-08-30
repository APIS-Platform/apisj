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
import org.apis.gui.manager.DBManager;
import org.apis.gui.manager.KeyStoreManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainFX extends Application  {
    private SystemTray tray;
    private TrayIcon trayIcon;
    private boolean firstTime;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);
        createTrayIcon(primaryStage);
        firstTime = true;

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

        AppManager.getInstance().start();
    }

    @Override
    public void stop() {
        if(!IntroController.getDeleteKeystoreFileFlag()) {
            KeyStoreManager.getInstance().deleteKeystore();
            System.exit(0);
        }
    }

    public void createTrayIcon(final Stage stage) {
        if(SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            java.awt.Image image = null;
            try {
                URL url  = new File("apisj-core/src/main/resources/image/ic_favicon@2x.png").toURI().toURL();

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
                                if(firstTime) {
                                    trayIcon.displayMessage("Some", "Message", TrayIcon.MessageType.INFO);
                                    firstTime = false;
                                }
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
            trayIcon = new TrayIcon(image, "APIS", popupMenu);
            // Set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            // Set the tray
            DBManager.getInstance().setTray(tray);
            DBManager.getInstance().setTrayIcon(trayIcon);
        }
    }

}
