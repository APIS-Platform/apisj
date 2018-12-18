package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class LedgerController implements Initializable {
    @FXML private TextArea rawDataTextArea, signedTextArea;
    @FXML private Label generateBtn, sendBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rawDataTextArea.setText("");
        signedTextArea.setText("");
    }

    @FXML
    public void onMousePressed(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("generateBtn")) {
            generateBtn.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #ffffff; -fx-text-fill: #b01e1e;");

        } else if(fxid.equals("sendBtn")) {
            sendBtn.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #ffffff; -fx-text-fill: #b01e1e;");
        }
    }

    @FXML
    public void onMouseReleased(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("generateBtn")) {
            generateBtn.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #b01e1e; -fx-text-fill: #ffffff;");
            generate();

        } else if(fxid.equals("sendBtn")) {
            sendBtn.setStyle("-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:12px; " +
                    "-fx-border-color: #b01e1e; -fx-background-color: #b01e1e; -fx-text-fill: #ffffff;");
            send();
        }
    }

    /**
     * GENERATE CLICKED
     **/
    private void generate() {
        System.out.println("GENERATE CLICKED");
    }

    /**
     * SEND CLICKED
     **/
    private void send() {
        System.out.println("SEND CLICKED");
    }

}
