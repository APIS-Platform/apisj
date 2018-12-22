package org.apis.gui.controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apis.gui.common.OSInfo;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LoadingController extends BasePopupController {
    private ScheduledFuture scheduledFuture;

    private Parent root = null;

    @FXML
    private Label label;
    @FXML
    private Label subMessageLabel;

    private int count = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        scheduledFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(count == 4){
                            count = 0;
                        }
                        if(count == 0){
                            label.textProperty().setValue("Program is Starting. Please  wait");
                        }else if(count == 1){
                            label.textProperty().setValue("Program is Starting. Please  wait.");
                        }else if(count == 2){
                            label.textProperty().setValue("Program is Starting. Please  wait..");
                        }else if(count == 3){

                        }
                        count++;

                    }
                    catch (Error | Exception e) { }
                }
            });
        }, 0, 1, TimeUnit.SECONDS);

        //new IntroMove().run();

    }

    class IntroMove extends Thread {

        @Override
        public void run(){


            System.out.println("TEST : (1)");

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("TEST : (2)");
                        Stage primaryStage = new Stage();
                        AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);

                        if("true".equals(AppManager.getWindowPropertiesData("minimize_to_tray"))){
                            Platform.setImplicitExit(false);
                            AppManager.getInstance().createTrayIcon(primaryStage);
                        }
                        System.out.println("TEST : (3)");

                        // 아이콘 등록
                        if(OSInfo.getOs() == OSInfo.OS.MAC){
                            URL iconURL = getClass().getClassLoader().getResource("image/favicon_128.png");
                            java.awt.Image image = new ImageIcon(iconURL).getImage();
                            //com.apple.eawt.Application.getApplication().setDockIconImage(image);
                        } else {
                            primaryStage.getIcons().add(new Image("image/ic_favicon@2x.png"));
                        }
                        System.out.println("TEST : (4)");

                        int size = AppManager.getInstance().keystoreFileReadAll().size();
                        URL fileUrl = getClass().getClassLoader().getResource("scene/intro.fxml");
                        fileUrl = (size > 0) ? getClass().getClassLoader().getResource("scene/main.fxml") : fileUrl;
                        System.out.println("TEST : (5)");
                        if(fileUrl != null) {

                                if(root != null){

                                    primaryStage.setScene(new Scene(root));
                                    primaryStage.setResizable(false);
                                    primaryStage.setMinWidth(1280);
                                    primaryStage.setMinHeight(720);
                                    primaryStage.setWidth(1280);
                                    primaryStage.setHeight(720);
                                    primaryStage.setTitle("APIS Core Wallet");
                                    System.out.println("TEST : (6)");

                                    Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                                    primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
                                    primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
                                    primaryStage.show();
                                    System.out.println("TEST : (8)");
                                }

                            System.out.println("TEST : (10)");

                        }
                    }
                });

                // 블록 싱크 시작
                AppManager.getInstance().start();

        }
    }

}
