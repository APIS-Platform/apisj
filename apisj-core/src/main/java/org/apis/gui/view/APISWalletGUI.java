package org.apis.gui.view;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import org.apis.gui.common.OSInfo;
import org.apis.gui.jsinterface.JavaScriptInterface;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.jsinterface.APISConsole;
import org.apis.keystore.KeyStoreData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;


public class APISWalletGUI {

    public static final int WEBVIEW_SIZE_WIDTH = 1280;
    public static final int WEBVIEW_SIZE_HEIGHT = 720;

    public static final String APP_TITLE = "APIS CORE";
    public static final String INDEX_HTML_PATH = "/webView/index.html";

    private MainFrame mainFrame;
    private WebView webView;
    private WebEngine webEngine;
    private JFXPanel fxPanel;

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
            this.setUndecorated(true);
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    if(createWalletCompleteFlag == 0) {
                        KeyStoreManager.getInstance().deleteKeystore();
                    }
                    System.exit(0);
                }
            });
            this.setLocationRelativeTo(null);
            this.setResizable(false);
            this.setVisible(true);

            Container contentPane = this.getContentPane();
            contentPane.setLayout(null);
            contentPane.setBackground(Color.decode("#FFFFFF"));
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
        fxPanel.setBounds(0,0,mainFrame.getContentPane().getSize().width, mainFrame.getContentPane().getSize().height);
        mainFrame.getContentPane().add(fxPanel);

        // Add Mouse Event in Panel
        FrameDragListener frameDragListener = new FrameDragListener(mainFrame);
        fxPanel.addMouseListener(frameDragListener);
        fxPanel.addMouseMotionListener(frameDragListener);

    }

    public void start(){
        // keystore file read all
        int fileSize = AppManager.getInstance().keystoreFileReadAll().size();
        System.out.println("fileSize : " + fileSize);

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

    /* ==============================================
     *
     *  Chooser Option Method
     *
     * ============================================== */

    //openDirectoryChooser(getDefaultDirectory());
    public File openDirectoryChooser(File fileDirectory){

        File keystoreDir = fileDirectory;

        // Open DirectoryChooser
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(keystoreDir);
        File folder = chooser.showDialog(null);
        return folder; //folder.getPath();
    }

    // File Read
    public boolean openFileChooser() {
        boolean result = true;

        File file = KeyStoreManager.getInstance().getDefaultKeystoreDirectory();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(file);

        File openFile = fileChooser.showOpenDialog(null);
        if(openFile != null) {
            KeyStoreManager.getInstance().createKeyStoreFileLoad(openFile);
        }else{
            System.out.println("피일선택안됨.");
        }

        return result;
    }

    public void createWalletComplete() {
        this.createWalletCompleteFlag = 1;
    }

    /* ==============================================
     *
     *  Getter Setter
     *
     * ============================================== */
    public JFrame getMainFrame(){return this.mainFrame;}
    public JFXPanel getMainPanel(){return this.fxPanel;}
    public WebEngine getWebEngine(){return this.webEngine;}
    public int getCreateWalletCompleteFlag() {return this.createWalletCompleteFlag;}
}
