package org.apis.gui.controller.setting;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apis.gui.manager.StringManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingItemRadioController implements Initializable {
    @FXML private Label contents;
    @FXML private VBox radioVBox;
    @FXML private SettingItemRadioItemController mainnetItemController, testnetItemController, customItemController;

    private ArrayList<SettingItemRadioItemController> itemControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addRadioItem(SettingItemRadioItemController.SETTING_ITEM_RADIO_LABEL, "Mainnet", "1");
        addRadioItem(SettingItemRadioItemController.SETTING_ITEM_RADIO_LABEL, "Osiris - Testnet", "40000");
        addRadioItem(SettingItemRadioItemController.SETTING_ITEM_RADIO_TEXTFIELD, "Custom", null);

        radioGroup();
    }

    private void addRadioItem(String itemType, String name, String networkId) {
        if(name.equals("Mainnet")) {
            try {
                URL url = getClass().getClassLoader().getResource("scene/popup/setting_item_radio_item.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                AnchorPane item = loader.load();
                radioVBox.getChildren().add(item);

                this.mainnetItemController = (SettingItemRadioItemController)loader.getController();
                this.mainnetItemController.setType(itemType);
                this.mainnetItemController.setName(StringManager.getInstance().setting.mainnetLabel.get());
                this.mainnetItemController.setNetworkId(networkId);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(name.equals("Osiris - Testnet")) {
            try {
                URL url = getClass().getClassLoader().getResource("scene/popup/setting_item_radio_item.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                AnchorPane item = loader.load();
                radioVBox.getChildren().add(item);

                this.testnetItemController = (SettingItemRadioItemController)loader.getController();
                this.testnetItemController.setType(itemType);
                this.testnetItemController.setName(StringManager.getInstance().setting.testnetLabel.get());
                this.testnetItemController.setNetworkId(networkId);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(name.equals("Custom")) {
            try {
                URL url = getClass().getClassLoader().getResource("scene/popup/setting_item_radio_item.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                AnchorPane item = loader.load();
                radioVBox.getChildren().add(item);

                this.customItemController = (SettingItemRadioItemController)loader.getController();
                this.customItemController.setType(itemType);
                this.customItemController.setName(StringManager.getInstance().setting.customLabel.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void radioGroup() {
        itemControllers.add(mainnetItemController);
        itemControllers.add(testnetItemController);
        itemControllers.add(customItemController);

        for(int i=0; i<itemControllers.size(); i++) {
            SettingItemRadioItemController controller = itemControllers.get(i);
            controller.setHandler(new SettingItemRadioItemController.SettingItemRadioItemImpl() {
                @Override
                public void clicked() {
                    controller.check();
                    for(int j=0; j<itemControllers.size(); j++) {
                        if(itemControllers.get(j) != controller) {
                            itemControllers.get(j).uncheck();
                            if(controller != customItemController) {
                                customItemController.setText("");
                            }
                        } else {
                            if(controller == customItemController) {
                                customItemController.requestFocus();
                            }
                        }
                    }
                }
            });
        }
    }

    public void initCheck(String networkId) {
        switch(networkId) {
            case "1":
                mainnetItemController.getHandler().clicked();
                break;
            case "40000":
                testnetItemController.getHandler().clicked();
                break;
            default:
                customItemController.getHandler().clicked();
                customItemController.setText(networkId);
                break;
        }
    }

    public String getChecked() {
        if(mainnetItemController.isChecked()) {
            return "" + 1;
        } else if(testnetItemController.isChecked()) {
            return "" + 40000;
        } else {
            return customItemController.getText();
        }
    }

    public String getContents() {
        return this.contents.getText();
    }

    public void setContents(String contents) {
        this.contents.setText(contents);
    }
}
