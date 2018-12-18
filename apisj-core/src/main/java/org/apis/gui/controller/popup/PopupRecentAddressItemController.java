package org.apis.gui.controller.popup;

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
import org.apis.util.AddressUtil;
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
    private String strAddress, strAddressMask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectIcon.setImage(imageUnCheck);

        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);

        address.setOnMouseEntered(event -> {
            this.address.setText(AddressUtil.getShortAddress(strAddress, 12));
        });
        address.setOnMouseExited(event -> {
            if(this.strAddressMask != null && this.strAddressMask.length() > 0){
                this.address.setText(strAddressMask);
            }else{
                this.address.setText(AddressUtil.getShortAddress(strAddress, 12));
            }
        });
    }
    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if("rootPane".equals(fxId)){
            if(handler != null){
                handler.onMouseClicked(strAddress);
            }
        }
    }

    public void setData(String address, String alias, long createdAt){

        this.alias.setText(alias);
        this.time.setText(AppManager.setBlockTimestamp(createdAt, TimeUtils.getRealTimestamp()));
        setAddress(address);
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
    public void setAddress(String address) {
        this.strAddress = address;
        this.strAddressMask = AppManager.getInstance().getMaskWithAddress(address);
        this.icon.setImage(IdenticonGenerator.createIcon(address));

        if(this.strAddressMask != null && this.strAddressMask.length() > 0){
            this.address.setText(strAddressMask);
        }else{
            this.address.setText(AddressUtil.getShortAddress(strAddress, 12));
        }
    }

    public String getAddress() {
        return this.strAddress;
    }

    public interface PopupRecentAddressItemImpl{
        void onMouseClicked(String address);
    }

}
