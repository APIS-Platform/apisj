package org.apis.rpc.adapter;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

public class CanvasAdapter {
    private WebEngine engine;
    private boolean isLogging = false;

    public CanvasAdapter(WebEngine engine) {
        this(engine, false);
    }

    public CanvasAdapter(WebEngine engine, boolean isLogging) {
        this.engine = engine;
        this.isLogging = isLogging;
    }

    public boolean isLogging() {
        return isLogging;
    }

    public void setLogging(boolean logging) {
        isLogging = logging;
    }

    public void send(String message) {
        send(message, isLogging);
    }

    public void send(String message, boolean logging) {
        Platform.runLater(() -> {
            engine.executeScript("canvasProvider.onMessage('" + message + "');");
            if(logging) {
                engine.executeScript("console.log('" + message + "');");
            }
        });
    }
}
