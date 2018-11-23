package org.apis.rpc;

import org.apis.facade.Ethereum;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

public class RPCServerManager {
    private static RPCServerManager sManager = null;

    private Ethereum ethereum;

    private int port;
    private String id;
    private String password;
    private boolean isAvailable;
    private int maxConnection;
    private String whitelist;

    private RPCWebSocketServer rpcServer = null;
    private boolean isRun = false;

    public static RPCServerManager getInstance(Ethereum ethereum) throws IOException {
        if(sManager == null) {
            sManager = new RPCServerManager(ethereum);
        }
        return sManager;
    }

    private RPCServerManager(Ethereum ethereum) throws IOException {
        this.ethereum = ethereum;

        Properties prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        InputStream input = new FileInputStream("config/rpc.properties");
        prop.load(input);

        try {
            this.port = Integer.parseInt(prop.getProperty("port"));
        } catch (NumberFormatException e) {
            this.port = 0;
        }
        this.id = prop.getProperty("id");
        this.password = prop.getProperty("password");
        this.isAvailable = Boolean.parseBoolean(prop.getProperty("use_rpc"));
        this.whitelist = prop.getProperty("allow_ip");
        try {
            this.maxConnection = Integer.parseInt(prop.getProperty("max_connections"));
        } catch(NumberFormatException e) {
            this.maxConnection = -1;
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void startServer() {
        rpcServer = new RPCWebSocketServer(port, id, password, ethereum);
        rpcServer.setConnectionRule(whitelist, maxConnection);

        rpcServer.start();
        isRun = true;
    }

    public void stopServer() {
        try {
            if(rpcServer != null) {
                rpcServer.stop();
                rpcServer = null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        isRun = false;
    }

    @Override
    public String toString() {
        return "RPCServerManager{" +
                "ethereum=" + ethereum +
                ", port=" + port +
                ", id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", isAvailable=" + isAvailable +
                ", maxConnection=" + maxConnection +
                ", whitelist='" + whitelist + '\'' +
                ", rpcServer=" + rpcServer +
                '}';
    }
}
