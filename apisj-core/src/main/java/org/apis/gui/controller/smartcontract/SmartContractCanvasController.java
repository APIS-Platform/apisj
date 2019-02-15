package org.apis.gui.controller.smartcontract;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SmartContractCanvasController extends BaseViewController {
    @FXML private GridPane blankGrid;
    @FXML private AnchorPane bgAnchor, webViewAnchor;
    @FXML private WebView webView;
    @FXML private ImageView blankIcon;
    @FXML private Label blankLabel;

    private WebEngine webEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        webEngine = webView.getEngine();
        webEngine.load("http://www.google.com");

        webView.getEngine().setUserStyleSheetLocation(getClass().getClassLoader().getResource("scene/css/webView.css").toString());

        blankIcon.setImage(ImageManager.icBlankPagePc);
        blankIcon.setFitWidth(180);
        webViewAnchor.setVisible(false);
        blankGrid.setVisible(true);
    }

    private void languageSetting() {
        blankLabel.textProperty().bind(StringManager.getInstance().smartContract.canvasBlankLabel);
    }

    public void showHideView() {
        if(blankGrid.isVisible()) {
            blankGrid.setVisible(false);
            webViewAnchor.setVisible(true);
        } else {
            webViewAnchor.setVisible(false);
            blankGrid.setVisible(true);
        }
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
