package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.manager.AppManager;
import org.apis.util.TimeUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupRecentAddressItemController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private ImageView icon, selectIcon;
    @FXML private Label alias, address, time;

    private Image imageCheck = new Image("image/btn_circle_red@2x.png");
    private Image imageUnCheck = new Image("image/btn_circle_none@2x.png");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectIcon.setImage(imageUnCheck);

        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
    }
    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if("rootPane".equals(fxId)){
            if(handler != null){
                handler.onMouseClicked(address.getText().trim());
            }
        }
    }

    public void setData(String address, String alias, long createdAt){
        this.address.setText(address);
        this.alias.setText(alias);
        this.time.setText(AppManager.setBlockTimestamp(createdAt, TimeUtils.getRealTimestamp()));

        try {
            icon.setImage(IdenticonGenerator.generateIdenticonsToImage(address, 128, 128));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private PopupRecentAddressItemImpl handler;
    public void setHandler(PopupRecentAddressItemImpl handler){
        this.handler = handler;
    }

    public void setSelected(boolean isSelected){
        if(isSelected){
            selectIcon.setImage(imageCheck);
        }else{
            selectIcon.setImage(imageUnCheck);
        }
    }

    public String getAddress() {
        return this.address.getText().trim();
    }

    public interface PopupRecentAddressItemImpl{
        void onMouseClicked(String address);
    }

}
