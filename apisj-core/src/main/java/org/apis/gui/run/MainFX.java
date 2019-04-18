package org.apis.gui.run;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apis.cli.CLIStart;
import org.apis.config.SystemProperties;
import org.apis.gui.common.OSInfo;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.manager.AppManager;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainFX extends Application  {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){

        try {
            // init config file
            new CLIStart();

            BaseFxmlController fxmlController = new BaseFxmlController("loading.fxml");
            Parent loading = (Parent)fxmlController.getNode();
            Scene loadingScene = new Scene(loading);
            loadingScene.setFill(Color.TRANSPARENT);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setTitle("APIS Core Wallet");
            primaryStage.setScene(loadingScene);
            primaryStage.setResizable(false);
            primaryStage.setMinWidth(560);
            primaryStage.setMinHeight(280);
            primaryStage.setWidth(560);
            primaryStage.setHeight(280);
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
            primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
            setIcon(primaryStage);
            primaryStage.show();
            AppManager.getInstance().guiFx.setLoadingStage(primaryStage);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.exit(0);
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }

        new IntroMove().start();


    }

    class IntroMove extends Thread {

        @Override
        public void run() {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Stage primaryStage = new Stage();

                    // 블록 싱크 시작
                    // DB버전 설정하는 문제로, javafx GUI 실행보다 우선 실행되어야 한다.
                    AppManager.getInstance().start();

                    AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);
                    if ("true".equals(AppManager.getWindowPropertiesData("minimize_to_tray"))) {
                        Platform.setImplicitExit(false);
                        AppManager.getInstance().createTrayIcon(primaryStage);
                    }

                    Font.loadFont(getClass().getClassLoader().getResource("font/NotoSansKR-Medium.otf").toString(), 14);
                    Font.loadFont(getClass().getClassLoader().getResource("font/NotoSansKR-Regular.otf").toString(), 14);

                    Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Regular.ttf").toString(), 14);
                    Font.loadFont(getClass().getClassLoader().getResource("font/RobotoMono-Medium.ttf").toString(), 14);

                    Font.loadFont(getClass().getClassLoader().getResource("font/Barlow-Regular.ttf").toString(), 14);
                    Font.loadFont(getClass().getClassLoader().getResource("font/Barlow-SemiBold.ttf").toString(), 14);

                    // TODO : 사용가능한 폰트 출력
                    for (String fontName : javafx.scene.text.Font.getFamilies()) {
                        //System.out.println("fontName : "+fontName);
                    }

                    int size = AppManager.getInstance().keystoreFileReadAll().size();
                    URL fileUrl = getClass().getClassLoader().getResource("scene/intro.fxml");
                    fileUrl = (size > 0) ? getClass().getClassLoader().getResource("scene/main.fxml") : fileUrl;

                    if (fileUrl != null) {
                        setIcon(primaryStage);
                        try {
                            int width = 1280, height = 760;

                            Parent root = FXMLLoader.load(fileUrl);
                            if(AppManager.getGeneralPropertiesData("network_id").equals("1")) {
                                primaryStage.setTitle("APIS Core Wallet v" + SystemProperties.getDefault().projectVersion());
                            } else {
                                primaryStage.setTitle("APIS Core Wallet v" + SystemProperties.getDefault().projectVersion() + " (TESTNET)");
                            }
                            primaryStage.setScene(new Scene(root));
                            primaryStage.setResizable(false);
                            primaryStage.setMaxWidth(width);
                            primaryStage.setMaxHeight(height);
                            primaryStage.setMinWidth(width);
                            primaryStage.setMinHeight(height);
                            primaryStage.setWidth(width);
                            primaryStage.setHeight(height);
                            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                            primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
                            primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);

                            primaryStage.show();
                            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                @Override
                                public void handle(WindowEvent event) {
                                    System.exit(0);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        }
    }

    public void setIcon(Stage stage){
        if (OSInfo.getOs() == OSInfo.OS.MAC) {
            URL iconURL = getClass().getClassLoader().getResource("image/favicon_128.png");
            java.awt.Image image = new ImageIcon(iconURL).getImage();
            try {

                // Get the toolkit
                MenuToolkit tk = MenuToolkit.toolkit();

                Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("apis-core");
                tk.setApplicationMenu(defaultApplicationMenu);
                defaultApplicationMenu.getItems().get(1).setText("Hide all the otters");
                // Create a new menu bar
                MenuBar bar = new MenuBar();
                bar.getMenus().add(tk.createDefaultApplicationMenu("apis-core"));
                tk.setGlobalMenuBar(bar);

                // className = "demon.model.MailList"
                Class clsMailList = Class.forName("com.apple.eawt.Application"); // 클래스로딩
                Object obj = clsMailList.newInstance(); // 인스턴스 생성
                ArrayList allSetMethods = allSetMethods(clsMailList); // 모든 set 함수를 가져오는 자체함수

                for (int i=0; i < allSetMethods.size(); i++) {
                    Method method = (Method) allSetMethods.get(i);
                    method.setAccessible(true);
                    if("setDockIconImage".equals(method.getName())){
                        method.invoke(obj, new Object[]{image});
                    }
                    method.setAccessible(false);
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        } else {
            stage.getIcons().add(new Image("image/ic_favicon@2x.png"));
        }
    }


    public ArrayList allSetMethods(Class claz){
        ArrayList methods = new ArrayList();
        ArrayList returnMethods = new ArrayList();
        Class parent = claz;

        do{
            methods.addAll(Arrays.asList(claz.getDeclaredMethods()));
            parent = parent.getSuperclass();
        } while(!parent.equals(Object.class));

        for (int i=0; i < methods.size(); i++){
            Method method = (Method)methods.get(i);
            if (method.getName().substring(0,3).equals("set")){
                returnMethods.add(method);
            }
        }
        return returnMethods;
    }

    @Override
    public void stop() {
        System.exit(0);
    }

}
