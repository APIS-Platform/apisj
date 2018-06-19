package org.apis.gui.run;

import org.apache.commons.lang3.StringUtils;
import org.apis.config.SystemProperties;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.gui.manager.AppManager;
import org.apis.gui.view.APISWalletGUI;

public class Main {
    public static void main(String[] args) {
        APISWalletGUI gui = new APISWalletGUI();
        gui.start();

        //AppManager.getInstance().setApisWalletGUI(gui);
        //AppManager.getInstance().start();
    }
}
