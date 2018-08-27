package org.apis.gui.run;

import com.beust.jcommander.WrappedParameter;
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
import javassist.*;
import org.apis.gui.common.OSInfo;
import org.apis.gui.controller.IntroController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;


public class MainFX extends Application  {
    private boolean isFirstStartTrayIcon;  // 프로그램이 처음으로 TrayIcon으로 이동됐는지 체크
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, InstantiationException {
        this.primaryStage = primaryStage;
        System.out.println(primaryStage);
        // AppManager에 primaryStage 저장
        AppManager.getInstance().guiFx.setPrimaryStage(primaryStage);

        // TrayIcon 생성
        createTrayIcon(primaryStage, "ic_favicon@2x.png");


        // 프로그램에 사용하는 폰트 불러오기
        initLoadFont();

        // 사용가능한 폰트 불러오기
//        for(String fontName : javafx.scene.text.Font.getFamilies()){
//            System.out.println("fontName : "+fontName);
//        }

        int size = AppManager.getInstance().keystoreFileReadAll().size();
        URL fileUrl = new File("apisj-core/src/main/resources/scene/intro.fxml").toURI().toURL();
        fileUrl = (size > 0) ? new File("apisj-core/src/main/resources/scene/main.fxml").toURI().toURL() : fileUrl;

        if(fileUrl != null) {

            if(OSInfo.getOs() == OSInfo.OS.MAC){
                try {
                    URL url = new File("apisj-core/src/main/resources/image/favicon_128.png").toURI().toURL();

                    Class util = Class.forName("com.apple.eawt.Application");
                    Method getApplication = util.getMethod("getApplication", new Class[0]);
                    Object application = getApplication.invoke(util);

                    // setDockIconImage
                    Class dockImageParams[] = new Class[1];
                    dockImageParams[0] = java.awt.Image.class;
                    Method setDockIconImage = util.getMethod("setDockIconImage", dockImageParams);
                    setDockIconImage.invoke(application, Toolkit.getDefaultToolkit().getImage(url));

                    //
                    Class appEventParams[] = new Class[1];
                    appEventParams[0] = Class.forName("com.apple.eawt.AppEventListener");
                    Method addAppEventListener = util.getMethod("addAppEventListener", appEventParams);

                    //
                    Object appReOpenedListener = m2("org.apis.gui.run.AppReOpenedListenerNew"
                            , "public void appReOpened(com.apple.eawt.AppEvent.AppReOpenedEvent arg0)" +
                                    "{" +
                                    " if(this.stage != null){" +
                                    "   this.stage.show(); " +
                                    " } " +
                                    "}");

                    //
                    addAppEventListener.invoke(application, appReOpenedListener);

                    Class testParmas[] = new Class[1];
                    testParmas[0] = Class.forName("javafx.stage.Stage");
                    Class test = Class.forName("org.apis.gui.run.AppReOpenedListenerNew");
                    Method testMethod = test.getMethod("setStage", testParmas);
                    testMethod.invoke(appReOpenedListener, primaryStage);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (CannotCompileException e) {
                    e.printStackTrace();
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


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

    public Object m2(String className, String methodBody)
            throws CannotCompileException, InstantiationException, IllegalAccessException, NotFoundException {
        // get the pool
        ClassPool classPool = ClassPool.getDefault();

        // this seems optional, but if it isn't Main.class (my test class) should be replaced with this.getClass()
        classPool.insertClassPath(new ClassClassPath(MainFX.class));

        // get the helper class
        CtClass[] helperInterface = new CtClass[1];
        CtClass helperClass = classPool.get("com.apple.eawt.AppReOpenedListener");
        helperInterface[0] = helperClass;

        // create a new class
        CtClass newCtClass = classPool.makeClass(className);

        // make it  child of Helper
        // newCtClass.setSuperclass(helperClass);
        newCtClass.setInterfaces(helperInterface);


        CtField field = new CtField(classPool.getCtClass("com.apple.eawt.AppReOpenedListener"), "stage", newCtClass);
        field.setModifiers(Modifier.PRIVATE);
        newCtClass.addField( field );

        // stage setter
        newCtClass.addMethod( CtNewMethod.make("public void setStage(javafx.stage.Stage stage){ this.stage = stage; } ", newCtClass));

        // this overrides the method in Helper
        newCtClass.addMethod(CtNewMethod.make(methodBody, newCtClass));

        // get a new instance
        Class<?> newClass = newCtClass.toClass();
        Object newClassInstance = newClass.newInstance();

        return newClassInstance;
    }

    /*
    * initLoadFont
    *   프로그램에 사용하는 폰트 불러오기
    * */
    private void initLoadFont() throws MalformedURLException {
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
    }


    @Override
    public void stop() {
        if(!IntroController.getDeleteKeystoreFileFlag()) {
            KeyStoreManager.getInstance().deleteKeystore();
            System.exit(0);
        }
    }


    // 트레이 아이콘 생성
    public void createTrayIcon(final Stage stage, String iconName) {
        isFirstStartTrayIcon = true;

        // 트레이아이콘 사용여부 체크
        if(SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = null;
            try {
                URL url  = new File("apisj-core/src/main/resources/image/"+iconName).toURI().toURL();

                image = ImageIO.read(url);
                image = image.getScaledInstance(16,16,0);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            };

            final ActionListener showListener = new ActionListener() {
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
            final TrayIcon trayIcon = new TrayIcon(image, "APIS", popupMenu);
            // Set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            // Add the tray image
            try {
                Platform.setImplicitExit(false);
                tray.add(trayIcon);
            } catch (AWTException e) {
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

                                // window 일 경우, 처음 트레이아이콘으로 이동할 때 윈도우 메시지를 띄운다.
                                if(OSInfo.getOs() == OSInfo.OS.WINDOWS) {
                                    if (isFirstStartTrayIcon) {
                                        trayIcon.displayMessage("Some", "Message", TrayIcon.MessageType.INFO);
                                        isFirstStartTrayIcon = false;
                                    }
                                }

                            } else {
                                System.exit(0);
                            }
                        }
                    });
                }
            });

        }
    }



}
