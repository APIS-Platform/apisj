package org.apis.gui.view;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class PlusWebViewSample implements ActionListener {
    private JFrame mainFrame;
    private JFXPanel fxPanel;
    private Container contentPane;
    private JPanel swingPanel;
    private JLabel aLabel, bLabel, cPlusDLabel;
    private JButton aPlusBButton;
    private JTextField aTextField, bTextField, cPlusDTextField;
    private Border border;
    private WebView webView;
    private WebEngine webEngine;
    private String result;

    public PlusWebViewSample() {
        mainFrame = new JFrame();
        contentPane = mainFrame.getContentPane();
        contentPane.setBackground(Color.white);
        mainFrame.setTitle("Plus ABCD");
        mainFrame.setLayout(null);
        mainFrame.setSize(800,450);
        mainFrame.setDefaultCloseOperation(mainFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        comInit();
        mainFrame.setVisible(true);
    }

    private void comInit() {
        swingPaneSetting();
        htmlPaneSetting();
    }

    private void swingPaneSetting() {
        swingPanel = new JPanel();
        swingPanel.setLayout(null);
        swingPanel.setSize(375,402);
        swingPanel.setLocation(5,5);
        swingPanel.setBackground(Color.white);
        border = BorderFactory.createLineBorder(Color.black);
        swingPanel.setBorder(border);

        aLabel = new JLabel("A");
        aLabel.setBounds(17,17,10,10);
        swingPanel.add(aLabel);

        aTextField = new JTextField();
        aTextField.setBounds(35,10,330,25);
        aTextField.setBorder(border);
        swingPanel.add(aTextField);

        bLabel = new JLabel("B");
        bLabel.setBounds(17,52,10,10);
        swingPanel.add(bLabel);

        bTextField = new JTextField();
        bTextField.setBounds(35,45,330,25);
        bTextField.setBorder(border);
        swingPanel.add(bTextField);

        aPlusBButton = new JButton("A + B");
        aPlusBButton.setBounds(35,80,330,25);
        aPlusBButton.setFocusPainted(false);
        aPlusBButton.addActionListener(this);
        swingPanel.add(aPlusBButton);

        cPlusDLabel = new JLabel("C + D");
        cPlusDLabel.setBounds(5,122,30,10);
        swingPanel.add(cPlusDLabel);

        cPlusDTextField = new JTextField();
        cPlusDTextField.setBounds(35,115,330,25);
        cPlusDTextField.setBorder(border);
        swingPanel.add(cPlusDTextField);

        contentPane.add(swingPanel);
    }

    private void htmlPaneSetting()  {
        border = BorderFactory.createLineBorder(Color.black);

        fxPanel = new JFXPanel();
        fxPanel.setBounds(405,5,375,402);
        fxPanel.setBorder(border);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // WebView setting
                webView = new WebView();
                webEngine = webView.getEngine();
                fxPanel.setScene(new Scene(webView));

                // Path setup
                URL plusURL = this.getClass().getResource("/webView/plus.html");
                System.out.println(plusURL);
                // Load URL from settings path
                webEngine.load(plusURL.toExternalForm());
                // Load URL directly
//                webEngine.load("http://css3test.com");

                webEngine.getLoadWorker().stateProperty().addListener(
                        new ChangeListener<Worker.State>() {
                            @Override
                            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldValue, Worker.State newValue) {
                                if(newValue == Worker.State.SUCCEEDED) {
                                    // Create JSObject for communication with Javascript
                                    JSObject window = (JSObject)webEngine.executeScript("window");
                                    window.setMember("app",new JavaApplication());
                                }
                            }
                        }
                );
            }
        });

        contentPane.add(fxPanel);
    }

    public class JavaApplication {
        // Validate the values after gets values from Javascript
        public void cPlusD(String cNum, String dNum) {
            int numberFlag = 0;

            for(char c : cNum.toCharArray()) {
                if(!Character.isDigit(c)) numberFlag = 1;
            }

            for(char c : dNum.toCharArray()) {
                if(!Character.isDigit(c)) numberFlag = 1;
            }

            if(!(cNum.equals("") || dNum.equals("")) && numberFlag==0) {
                result = Integer.toString(Integer.parseInt(cNum) + Integer.parseInt(dNum));
            } else {
                result = "";
            }

            cPlusDTextField.setText(result);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== aPlusBButton) {
            int numberFlag = 0;

            for(char c : aTextField.getText().toCharArray()) {
                if(!Character.isDigit(c)) numberFlag = 1;
            }

            for(char c : bTextField.getText().toCharArray()) {
                if(!Character.isDigit(c)) numberFlag = 1;
            }

            if(!(aTextField.getText().equals("") || bTextField.getText().equals("")) && numberFlag==0) {
                result = Integer.toString(Integer.parseInt(aTextField.getText()) + Integer.parseInt(bTextField.getText()));
            } else {
                result = "";
            }

            // Validate values and pass them to Javascript
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webEngine.executeScript("aPlusB('"+result+"')");
                }
            });
        }
    }

}
