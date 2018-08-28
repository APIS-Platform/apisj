package org.apis.gui.manager;

import org.apis.gui.model.MyAddressModel;

import java.awt.*;
import java.util.ArrayList;

public class DBManager {

    public ArrayList<String> addressGroupList = new ArrayList<>();
    public ArrayList<MyAddressModel> myAddressList = new ArrayList<>();

    // Settings
    private String userNum = "5";
    private String port = "";
    private String whiteList = "";
    private String Id = "";
    private String Pw = "";
    private boolean startWalletWithLogIn = true;
    private boolean enableLogEvent = false;
    private boolean minimizeToTray = false;
    private SystemTray tray;
    private TrayIcon trayIcon;

    private static DBManager ourInstance = new DBManager();
    public static DBManager getInstance() {
        return ourInstance;
    }
    private DBManager() { }

    public void loadData(){

    }

    public void saveData(){

    }

    /* ==============================================
     *  DBManager Getter Setter
     * ============================================== */
    public String getUserNum() {
        return userNum;
    }

    public void setUserNum(String userNum) {
        this.userNum = userNum;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(String whiteList) {
        this.whiteList = whiteList;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getPw() {
        return Pw;
    }

    public void setPw(String pw) {
        Pw = pw;
    }

    public boolean isStartWalletWithLogIn() {
        return startWalletWithLogIn;
    }

    public void setStartWalletWithLogIn(boolean startWalletWithLogIn) {
        this.startWalletWithLogIn = startWalletWithLogIn;
    }

    public boolean isEnableLogEvent() {
        return enableLogEvent;
    }

    public void setEnableLogEvent(boolean enableLogEvent) {
        this.enableLogEvent = enableLogEvent;
    }

    public boolean isMinimizeToTray() {
        return minimizeToTray;
    }

    public void setMinimizeToTray(boolean minimizeToTray) {
        this.minimizeToTray = minimizeToTray;
    }

    public SystemTray getTray() {
        return tray;
    }

    public void setTray(SystemTray tray) {
        this.tray = tray;
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }
}
