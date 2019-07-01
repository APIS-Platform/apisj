package org.apis.gui.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apis.gui.model.base.BaseModel;

public class PeersModel extends BaseModel {
    private final SimpleIntegerProperty nodeId = new SimpleIntegerProperty();
    private final SimpleStringProperty nodeService = new SimpleStringProperty();
    private final SimpleStringProperty userAgent = new SimpleStringProperty();
    private final SimpleStringProperty ping = new SimpleStringProperty();

    public PeersModel() {
        setNodeId(0);
        setNodeService("192.168.0.1:8080");
        setUserAgent("/Apis Core:0.8.810/");
        setPing("38 ms");
    }

    public PeersModel(int nodeId, String nodeService, String userAgent, String ping) {
        setNodeId(nodeId);
        setNodeService(nodeService);
        setUserAgent(userAgent);
        setPing(ping);
    }

    public void setNodeId(int nodeId) {
        this.nodeId.set(nodeId);
    }

    public int getNodeId() {
        return this.nodeId.get();
    }

    public SimpleIntegerProperty nodeIdProperty() {
        return this.nodeId;
    }

    public void setNodeService(String nodeService) {
        this.nodeService.set(nodeService);
    }

    public String getNodeService() {
        return this.nodeService.get();
    }

    public SimpleStringProperty nodeServiceProperty() {
        return this.nodeService;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent.set(userAgent);
    }

    public String getUserAgent() {
        return this.userAgent.get();
    }

    public SimpleStringProperty userAgentProperty() {
        return this.userAgent;
    }

    public void setPing(String ping) {
        this.ping.set(ping);
    }

    public String getPing() {
        return this.ping.get();
    }

    public SimpleStringProperty pingProperty() {
        return this.ping;
    }
}
