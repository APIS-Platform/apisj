package org.apis.gui.view;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apis.gui.common.FileDrop;
import org.apis.gui.jsinterface.JavaScriptInterface;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.jsinterface.APISConsole;
import org.apis.keystore.KeyStoreData;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;


public class APISWalletGUI {

    private static final int WEBVIEW_SIZE_WIDTH = 1280;
    private static final int WEBVIEW_SIZE_HEIGHT = 720;

    private static final String APP_TITLE = "APIS CORE";
    private static final String INDEX_HTML_PATH = "/webView/index.html";

    private MainFrame mainFrame;
    private WebView webView;
    private WebEngine webEngine;
    private JFXPanel fxPanel;
    private JPanel dragAndDropPanel;

    private int createWalletCompleteFlag = 0;

    /* ==============================================
     *
     *  Swing Component Class
     *
     * ============================================== */

    private class MainFrame extends JFrame{

        public MainFrame(String title){
            // Frame get statusbar height
            Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            int taskBarHeight = scrnSize.height - winSize.height;

            this.setTitle(title);
            //this.setSize(WEBVIEW_SIZE_WIDTH+6,WEBVIEW_SIZE_HEIGHT+29);
            this.setSize(WEBVIEW_SIZE_WIDTH,WEBVIEW_SIZE_HEIGHT);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    if(createWalletCompleteFlag == 0) {
                        KeyStoreManager.getInstance().deleteKeystore();
                    }
                    System.exit(0);
                }
            });
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            this.setUndecorated(true);
            this.setLocationRelativeTo(null);
            this.setResizable(false);
            this.setVisible(true);
            Container contentPane = this.getContentPane();
            contentPane.setLayout(null);
            contentPane.setBackground(Color.decode("#FFFFFF"));
        }
    }

    private class ShadowPane extends JPanel {

        public ShadowPane() {
            //setLayout(new BorderLayout());
            setOpaque(false);
            //setBackground(Color.BLACK);
            //setBorder(new EmptyBorder(0, 0, 10, 10));
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
//            Graphics2D g2d = (Graphics2D) g.create();
//            g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
//            g2d.fillRect(10, 10, getWidth(), getHeight());
//            g2d.dispose();
        }
    }

    public static class FrameDragListener extends MouseAdapter {
        private final JFrame frame;
        private Point mouseDownCompCoords = null;
        private boolean mouseMoveFrameFlag = false;

        public FrameDragListener(JFrame frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
            mouseMoveFrameFlag = false;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
            if(mouseDownCompCoords.y >= 0 && mouseDownCompCoords.y <= 24) {
                mouseMoveFrameFlag = true;
            }
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            if(mouseMoveFrameFlag) {
                frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        }
    }


    private void initLayout() {
        // Create MainFrame
        mainFrame = new MainFrame(APP_TITLE);

        // Add Panel
        fxPanel = new JFXPanel();
        fxPanel.setBounds(0,0, mainFrame.getContentPane().getSize().width, mainFrame.getContentPane().getSize().height);
        mainFrame.getContentPane().add(fxPanel);

        // Add Mouse Event in Panel
        FrameDragListener frameDragListener = new FrameDragListener(mainFrame);
        fxPanel.addMouseListener(frameDragListener);
        fxPanel.addMouseMotionListener(frameDragListener);

        // Add drag and drop Panel
        this.dragAndDropPanel = new JPanel();
        this.dragAndDropPanel.setBackground(Color.BLACK);
        this.dragAndDropPanel.setBounds(WEBVIEW_SIZE_WIDTH/2 + 94,361,464,84);
        this.dragAndDropPanel.setOpaque(false);
        this.dragAndDropPanel.setVisible(false);
        fxPanel.add(this.dragAndDropPanel);

        new FileDrop(this.dragAndDropPanel, new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {

                if(APISWalletGUI.this.webEngine != null){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(files.length > 0) {
                                File file = new File(files[0].getAbsolutePath());
                                if(file.isFile()) {
                                    String resultCode = "FileException";
                                    String fileName = file.getName();

                                    KeyStoreManager.getInstance().setKeystoreFile(file);

                                    if(file.exists()) {
                                        long l = file.length();
                                        if(l > 10240) {
                                            resultCode = "IncorrectFileForm";
                                        }else{
                                            try {
                                                String allText = AppManager.fileRead(file);
                                                KeyStoreData keyStoreData = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreData.class);
                                                KeyStoreManager.getInstance().setKeystoreFullpath(KeyStoreManager.getInstance().getDefaultKeystoreDirectory()+"/"+fileName);
                                                KeyStoreManager.getInstance().setKeystoreJsonData(allText.toString().toLowerCase());
                                                KeyStoreManager.getInstance().setKeystoreJsonObject(keyStoreData);

                                                resultCode = "CorrectFileForm";
                                            } catch (com.google.gson.JsonSyntaxException e) {
                                                resultCode = "IncorrectFileForm";
                                            } catch (IOException e) {
                                                System.out.println("file read failed (FileName : " + file.getName() + ")");
                                            }
                                        }
                                    }

                                    APISWalletGUI.this.webEngine.executeScript("dragAndDropOpenFileReader('"+fileName+"','"+resultCode+"');");
                                }
                            }
                        }
                    });

                }
            }
        });
    }

    public void start(){
        // keystore file read all
        int fileSize = AppManager.getInstance().keystoreFileReadAll().size();

        // layout setting
        initLayout();

        // add javascript interface
        APISConsole console = new APISConsole();
        JavaScriptInterface apisWallet = new JavaScriptInterface(this);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                // WebView setting
                webView = new WebView();
                webView.setContextMenuEnabled(false);
                webEngine = webView.getEngine();
                webEngine.getLoadWorker().stateProperty().addListener(
                        new ChangeListener<Worker.State>() {
                            @Override
                            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                                // Add JSObject in Javascript
                                JSObject window = (JSObject)webEngine.executeScript("window");
                                window.setMember("console", console);
                                window.setMember("app", apisWallet);

                            }
                        }
                );

                webEngine.setOnAlert(new EventHandler<WebEvent<String>>(){

                    @Override
                    public void handle(WebEvent<String> arg0) {
                        System.out.println("[JavaScript Alert] - " + arg0.getData());
                    }

                });

                // Path setup
                URL mainURL = this.getClass().getResource(INDEX_HTML_PATH);
                // Load URL from setting Path
                webEngine.load(mainURL.toExternalForm());
                fxPanel.setScene(new Scene(webView));

            }
        });

    }

    public void createWalletComplete() {
        this.createWalletCompleteFlag = 1;
    }
    public void setVisibleDragAndDropPanel(boolean visible){
        this.dragAndDropPanel.setVisible(visible);
    }


    /* ==============================================
     *
     *  Getter Setter
     *
     * ============================================== */
    public JFrame getMainFrame(){return this.mainFrame;}
    public JFXPanel getMainPanel(){return this.fxPanel;}
    public WebEngine getWebEngine(){return this.webEngine;}
    public JFXPanel getFxPanel(){return this.fxPanel;}
    public int getCreateWalletCompleteFlag() {return this.createWalletCompleteFlag;}
}

