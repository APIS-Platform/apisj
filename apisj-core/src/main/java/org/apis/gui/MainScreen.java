package org.apis.gui;

import javax.swing.*;

public class MainScreen {
    private JPanel panelMain;

    public MainScreen() {

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainScreen");
        frame.setContentPane(new MainScreen().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("APIS CORE");
    }
}
