package org.apis.gui.controller.base;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public class BaseFxmlController {
    private URL fxmlUrl;
    private Node node;
    private BaseViewController controller;
    private FXMLLoader loader;

    public BaseFxmlController(String path) throws IOException {
        fxmlUrl = getClass().getClassLoader().getResource("scene/" + path);

        this.loader = new FXMLLoader(fxmlUrl);
        this.node = this.loader.load();
        this.controller = this.loader.getController();
    }

    public Node getNode(){
        return this.node;
    }
    public BaseViewController getController(){
        return this.controller;
    }
}
