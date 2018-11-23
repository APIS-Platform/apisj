package org.apis.gui.controller.setting;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apis.gui.common.OSInfo;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.popup.PopupSuccessController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SettingController extends BasePopupController {
    private String fileName = null;
    @FXML private Label userNumLabel, cancelBtn, saveBtn;
    @FXML private ImageView rpcBtnIcon, generalBtnIcon, windowBtnIcon;
    @FXML private Label settingsTitle, settingsDesc, userNumTitle, userNumDesc, rpcTitle, generalTitle, windowTitle;
    @FXML private VBox rpcVBox, generalVBox, windowVBox;
    @FXML private SettingItemBtnController startWalletWithLogInBtnController, enableLogEventBtnController, minimizeToTrayBtnController, rewardSaveBtnController;
    @FXML private SettingItemInputController portInputController, whiteListInputController, idInputController, passwordInputController;
    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane gridPane, bodyScrollPaneContentPane;

    private boolean isScrolling;

    private Image downGrayIcon, upGrayIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        fileName = getClass().getResource("").getPath() + "CreateShortcut.vbs";
        fileName = "CreateShortcut.vbs";
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@" + fileName);
        languageSetting();

        // Initialize Images
        downGrayIcon = new Image("image/ic_down_black@2x.png");
        upGrayIcon = new Image("image/ic_up_gray@2x.png");

        openRpc();
        openGeneral();
        openWindow();

        // Initiate items
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "Port");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "White List");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_TEXT, "ID");
        addRpcItem(SettingItemInputController.SETTING_ITEM_INPUT_PASS, "Password");
        addGeneralItem("startWalletWithLogIn");
        addGeneralItem("enableLogEvent");
        addGeneralItem("rewardSave");
        addWindowItem("minimizeToTray");

        setItemsUnderLine();

        loadSettingData();


        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = bodyScrollPaneContentPane.getHeight() - bodyScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = bodyScrollPane.getVvalue() + add;
                        if(moveV > bodyScrollPane.getVmax()){
                            moveV = bodyScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = bodyScrollPane.getVvalue() - add;
                        if(moveV < bodyScrollPane.getVmin()){
                            moveV = bodyScrollPane.getVmin();
                        }
                    }

                    if(!bodyScrollPane.isPressed()) {
                        bodyScrollPane.setVvalue(moveV);
                    }
                }
            }
        });

    }

    public void languageSetting() {
        this.settingsTitle.textProperty().bind(StringManager.getInstance().setting.settingsTitle);
        this.settingsDesc.textProperty().bind(StringManager.getInstance().setting.settingsDesc);
        this.userNumTitle.textProperty().bind(StringManager.getInstance().setting.userNumTitle);
        this.userNumDesc.textProperty().bind(StringManager.getInstance().setting.userNumDesc);
        this.rpcTitle.textProperty().bind(StringManager.getInstance().setting.rpcTitle);
        this.generalTitle.textProperty().bind(StringManager.getInstance().setting.generalTitle);
        this.windowTitle.textProperty().bind(StringManager.getInstance().setting.windowTitle);
        this.saveBtn.textProperty().bind(StringManager.getInstance().common.saveButton);
        this.cancelBtn.textProperty().bind(StringManager.getInstance().common.backButton);
    }

    private void loadSettingData() {
        Properties prop = AppManager.getRPCProperties();
        userNumLabel.setText(prop.getProperty("max_connections"));
        portInputController.setTextField(prop.getProperty("port"));
        whiteListInputController.setTextField(prop.getProperty("allow_ip"));
        idInputController.setTextField(prop.getProperty("id"));
        passwordInputController.setTextField(prop.getProperty("password"));

        prop = AppManager.getGeneralProperties();
        startWalletWithLogInBtnController.setSelected(prop.getProperty("in_system_log").equals("true"));
        enableLogEventBtnController.setSelected(prop.getProperty("enable_event_log").equals("true"));
        rewardSaveBtnController.setSelected(prop.getProperty("reward_sound").equals("true"));

        prop = AppManager.getWindowProperties();
        minimizeToTrayBtnController.setSelected(prop.getProperty("minimize_to_tray").equals("true"));
    }

    private void addRpcItem(String inputFlag, String contentsId) {
        if(contentsId.equals("Port")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.portInputController = (SettingItemInputController)loader.getController();
                this.portInputController.setInput(inputFlag);
                this.portInputController.setContents(StringManager.getInstance().setting.rpcPortLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("White List")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.whiteListInputController = (SettingItemInputController)loader.getController();
                this.whiteListInputController.setInput(inputFlag);
                this.whiteListInputController.setContents(StringManager.getInstance().setting.rpcWhiteListLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("ID")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.idInputController = (SettingItemInputController)loader.getController();
                this.idInputController.setInput(inputFlag);
                this.idInputController.setContents(StringManager.getInstance().setting.rpcIdLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("Password")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_input.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                rpcVBox.getChildren().add(item);

                this.passwordInputController = (SettingItemInputController)loader.getController();
                this.passwordInputController.setInput(inputFlag);
                this.passwordInputController.setContents(StringManager.getInstance().setting.rpcPwLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addGeneralItem(String contentsId) {
        if(contentsId.equals("startWalletWithLogIn")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.startWalletWithLogInBtnController = (SettingItemBtnController)loader.getController();
                this.startWalletWithLogInBtnController.setContents(StringManager.getInstance().setting.startWalletWithLogInLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("enableLogEvent")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.enableLogEventBtnController = (SettingItemBtnController)loader.getController();
                this.enableLogEventBtnController.setContents(StringManager.getInstance().setting.enableLogEventLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(contentsId.equals("rewardSave")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                generalVBox.getChildren().add(item);

                this.rewardSaveBtnController = (SettingItemBtnController)loader.getController();
                this.rewardSaveBtnController.setContents(StringManager.getInstance().setting.rewardSoundLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addWindowItem(String contentsId) {
        if(contentsId.equals("minimizeToTray")) {
            try {
                URL labelUrl = getClass().getClassLoader().getResource("scene/popup/setting_item_btn.fxml");
                FXMLLoader loader = new FXMLLoader(labelUrl);
                AnchorPane item = loader.load();
                windowVBox.getChildren().add(item);

                this.minimizeToTrayBtnController = (SettingItemBtnController)loader.getController();
                this.minimizeToTrayBtnController.setContents(StringManager.getInstance().setting.minimizeToTrayLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setItemsUnderLine() {
        for(int i=0; rpcVBox.getChildren().size()>i; i++) {
            if(i != rpcVBox.getChildren().size()-1) {
                rpcVBox.getChildren().get(i).setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #d8d8d8;");
            }
        }

        for(int i=0; generalVBox.getChildren().size()>i; i++) {
            if(i != generalVBox.getChildren().size()-1) {
                generalVBox.getChildren().get(i).setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #d8d8d8;");
            }
        }

        for(int i=0; windowVBox.getChildren().size()>i; i++) {
            if(i != windowVBox.getChildren().size()-1) {
                windowVBox.getChildren().get(i).setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #d8d8d8;");
            }
        }
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("rpcHeader")) {
            if(rpcVBox.isVisible()) {
                closeRpc();
            } else {
                openRpc();
            }

        } else if(fxid.equals("generalHeader")) {
            if(generalVBox.isVisible()) {
                closeGeneral();
            } else {
                openGeneral();
            }

        } else if(fxid.equals("windowHeader")) {
            if(windowVBox.isVisible()) {
                closeWindow();
            } else {
                openWindow();
            }

        } else if(fxid.equals("userNumMinus")) {
            int num = Integer.parseInt(userNumLabel.getText());
            if(num > 1) num--;
            userNumLabel.setText(Integer.toString(num));

        } else if(fxid.equals("userNumPlus")) {
            int num = Integer.parseInt(userNumLabel.getText());
            if(num < 5) num++;
            userNumLabel.setText(Integer.toString(num));

        } else if(fxid.equals("cancelBtn")) {
            exit();
            //PopupManager.getInstance().hideMainPopup(-1);

        } else if(fxid.equals("saveBtn")) {

            Properties prop = AppManager.getRPCProperties();
            prop.setProperty("port", portInputController.getTextField().trim());
            prop.setProperty("id", idInputController.getTextField().trim());
            prop.setProperty("password", passwordInputController.getTextField().trim());
            prop.setProperty("max_connections", userNumLabel.getText());
            prop.setProperty("allow_ip", whiteListInputController.getTextField().trim());
            AppManager.saveRPCProperties();

            prop = AppManager.getGeneralProperties();
            prop.setProperty("in_system_log", ""+startWalletWithLogInBtnController.isSelected());
            prop.setProperty("enable_event_log", ""+enableLogEventBtnController.isSelected());
            prop.setProperty("reward_sound", ""+rewardSaveBtnController.isSelected());
            AppManager.saveGeneralProperties();

            // 윈도우 시작프로그램 등록
            if (OSInfo.getOs() == OSInfo.OS.WINDOWS) {
                if ("true".equals(prop.getProperty("in_system_log"))) {
                    String txt =
                        "Set oWS = WScript.createObject(\"WScript.Shell\")\r\n" +
                        "If WScript.Arguments.Length = 0 Then\r\n" +
                        "   Set ObjShell = CreateObject(\"Shell.Application\")\r\n" +
                        "   ObjShell.ShellExecute \"wscript.exe\" _\r\n" +
                        "   , \"\"\"\" & WScript.ScriptFullName & \"\"\" RunAsAdministrator\", , \"runas\", 1\r\n" +
                        "WScript.Quit\r\n" +
                        "End if\r\n" +
                        "sysdrive = oWS.ExpandEnvironmentStrings(\"%SYSTEMDRIVE%\")\r\n" +
                        "sLinkFile = sysdrive + \"\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\StartUp\\apis.lnk\"\r\n" +
                        "Set oLink = oWS.CreateShortcut(sLinkFile)\r\n" +
                        "strPath = WScript.ScriptFullName\r\n" +
                        "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\r\n" +
                        "Set objFile = objFSO.GetFile(strPath)\r\n" +
                        "vbsFolder = objFSO.GetParentFolderName(objFile)\r\n" +
                        "exeFolder = objFSO.GetParentFolderName(vbsFolder)\r\n" +
                        "oLink.TargetPath = exeFolder & \"\\apis-core.exe\"\r\n" +
                        "oLink.Save";

                    File file = null;
                    try {
                        file = new File(fileName);
                        FileWriter fw = new FileWriter(file, false);
                        fw.write(txt);
                        fw.flush();
                        fw.close();

                        String[] cmd = new String[]{"powershell.exe", "Start-Process",
                                "-verb runAs cscript",
                                file.getAbsolutePath()};
                        ProcessBuilder builder = new ProcessBuilder(cmd);
                        Process proc = builder.start();

                        proc.waitFor();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        String[] cmd = new String[]{"powershell.exe", "del",
                            "\"$env:SystemDrive\\ProgramData\\Microsoft\\Windows\\'Start Menu'\\Programs\\StartUp\\apis.lnk\""};
                        ProcessBuilder builder = new ProcessBuilder(cmd);
                        Process proc = builder.start();
                        proc.waitFor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            prop = AppManager.getWindowProperties();
            prop.setProperty("minimize_to_tray", ""+minimizeToTrayBtnController.isSelected());
            AppManager.saveWindowProperties();

            if("true".equals(prop.getProperty("minimize_to_tray"))){
                Platform.setImplicitExit(false);
                createTrayIcon(AppManager.getInstance().guiFx.getPrimaryStage());
            }else{
                Platform.setImplicitExit(true);
                for(int i = 0; i<SystemTray.getSystemTray().getTrayIcons().length; i++){
                    SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[i]);
                }
            }

            //exit();
            PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(null, "popup_success.fxml",zIndex+1);
            controller.setHandler(new PopupSuccessController.PopupSuccessImpl() {
                @Override
                public void confirm() {
                    File file = new File(fileName);
                    file.delete();
                }
            });
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
        if(fxid.equals("saveBtn")){
            StyleManager.backgroundColorStyle(saveBtn, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(saveBtn, StyleManager.AColor.C910000);

        }

    }

    @FXML
    public void onMouseExited(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();
        if(fxid.equals("saveBtn")){
            StyleManager.backgroundColorStyle(saveBtn, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(saveBtn, StyleManager.AColor.Cffffff);
        }
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

    public void openRpc() {
        rpcBtnIcon.setImage(upGrayIcon);
        rpcVBox.setVisible(true);
        rpcVBox.prefHeightProperty().setValue(-1);
    }

    public void closeRpc() {
        rpcBtnIcon.setImage(downGrayIcon);
        rpcVBox.setVisible(false);
        rpcVBox.prefHeightProperty().setValue(0);
    }

    public void openGeneral() {
        generalBtnIcon.setImage(upGrayIcon);
        generalVBox.setVisible(true);
        generalVBox.prefHeightProperty().setValue(-1);
    }

    public void closeGeneral() {
        generalBtnIcon.setImage(downGrayIcon);
        generalVBox.setVisible(false);
        generalVBox.prefHeightProperty().setValue(0);
    }

    public void openWindow() {
        windowBtnIcon.setImage(upGrayIcon);
        windowVBox.setVisible(true);
        windowVBox.prefHeightProperty().setValue(-1);
    }

    public void closeWindow() {
        windowBtnIcon.setImage(downGrayIcon);
        windowVBox.setVisible(false);
        windowVBox.prefHeightProperty().setValue(0);
    }

}
