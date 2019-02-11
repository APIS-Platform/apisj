package org.apis.gui.controller.smartcontract;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractCanvasController extends BaseViewController {
    @FXML private AnchorPane bgAnchor;
    @FXML private ImageView blankIcon;
    @FXML private Label blankLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        blankIcon.setImage(ImageManager.icBlankPagePc);
        blankIcon.setFitWidth(180);
    }

    private void languageSetting() {
        blankLabel.textProperty().bind(StringManager.getInstance().smartContract.canvasBlankLabel);
    }

    public void setBgAnchorWidth(double width) {
        this.bgAnchor.setPrefWidth(width);
    }

    public void setBlankIconImage(Image image) {
        this.blankIcon.setImage(image);
    }

    public void setBlankIconWidth(double width) {
        this.blankIcon.setFitWidth(width);
    }

    private SmartContractCanvasImpl handler;
    public void setHandler(SmartContractCanvasImpl handler){
        this.handler = handler;
    }
    public interface SmartContractCanvasImpl{
        void onAction();
    }
}
