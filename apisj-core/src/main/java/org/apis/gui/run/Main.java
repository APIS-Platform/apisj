package org.apis.gui.run;

import org.apis.gui.manager.AppManager;
import org.apis.gui.view.APISWalletGUI;
import org.apis.keystore.*;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        APISWalletGUI gui = new APISWalletGUI();
        gui.start();

        AppManager.getInstance().setApisWalletGUI(gui);
        AppManager.getInstance().start();

    }
}
