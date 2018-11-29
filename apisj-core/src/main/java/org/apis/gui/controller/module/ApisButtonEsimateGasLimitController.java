package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisButtonEsimateGasLimitController extends BaseViewController {

    @FXML private ImageView icon;
    @FXML private Label label;

    private boolean isCompiled = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        label.textProperty().bind(StringManager.getInstance().common.esimateGasLimitButton);
    }

    @FXML
    private void onMouseClicked(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(handler != null){
            handler.onMouseClicked(this);
        }
    }

    @FXML
    private void onMouseEntered(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(isCompiled) {
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C910000);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }else{
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }
    }

    @FXML
    private void onMouseExited(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(isCompiled) {
            icon.setImage(ImageManager.icEstimateGasLimit);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.Cffffff);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(label, StyleManager.AColor.C910000);
        }else{
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }

    }

    @FXML
    private void onMousePressed(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(isCompiled) {
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C810000);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C810000);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }else{
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }
    }

    @FXML
    private void onMouseReleased(javafx.scene.input.InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(isCompiled) {
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C910000);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }else{
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }
    }

    public void setCompiled(boolean isCompiled){
        this.isCompiled = isCompiled;

        if(isCompiled) {
            icon.setImage(ImageManager.icEstimateGasLimit);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.Cffffff);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C910000);
            StyleManager.fontColorStyle(label, StyleManager.AColor.C910000);
        }else{
            icon.setImage(ImageManager.icEstimateGasLimitHover);
            StyleManager.backgroundColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.borderColorStyle(label, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(label, StyleManager.AColor.Cffffff);
        }
    }

    public boolean getCompiled(){
        return isCompiled;
    }

    private ApisButtonEsimateGasLimitImpl handler;
    public void setHandler(ApisButtonEsimateGasLimitImpl handler){
        this.handler = handler;
    }
    public interface ApisButtonEsimateGasLimitImpl {
        void onMouseClicked(ApisButtonEsimateGasLimitController controller);
    }
}
