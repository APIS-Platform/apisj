package org.apis.gui.manager;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {
    public static final Image apisIcon = new Image("image/ic_apis@2x.png");
    public static final Image mineraIcon = new Image("image/ic_mineral@2x.png");
    public static final Image hintImageCheck = new Image("image/ic_check_green@2x.png");
    public static final Image hintImageError = new Image("image/ic_error_red@2x.png");

    public static final Image tooltipReward = new Image("image/tooltip_reward@2x.png");

    public static final Image btnChangeName = new Image("image/btn_wright@2x.png");
    public static final Image btnChangeNameHover = new Image("image/btn_wright_hover@2x.png");
    public static final Image btnChangePassword  = new Image("image/btn_unlock@2x.png");
    public static final Image btnChangePasswordHover  = new Image("image/btn_unlock_hover@2x.png");
    public static final Image btnBackupWallet  = new Image("image/btn_share@2x.png");
    public static final Image btnBackupWalletHover  = new Image("image/btn_share_hover@2x.png");
    public static final Image btnRemoveWallet = new Image("image/btn_remove@2x.png");
    public static final Image btnRemoveWalletHover = new Image("image/btn_remove_hover@2x.png");
    public static final Image btnMiningGrey = new Image("image/ic_miningwallet_grey@2x.png");
    public static final Image btnMiningRed = new Image("image/ic_miningwallet_red@2x.png");

    public static final Image icSortNONE = new Image("image/ic_sort_none@2x.png");
    public static final Image icSortASC = new Image("image/ic_sort_up@2x.png");
    public static final Image icSortDESC = new Image("image/ic_sort_down@2x.png");




    public static ImageView imageViewRectangle30(ImageView imageView){
        Rectangle clip = new Rectangle(imageView.getFitWidth() - 0.5, imageView.getFitHeight() - 0.5);

        clip.setArcWidth(30);
        clip.setArcHeight(30);
        imageView.setClip(clip);

        return imageView;
    }


    private static Map<String, Image> identicons = new HashMap<>();
    public static Image getIdenticons(String address) {
        Image image = identicons.get(address);
        if(image == null){
            image = IdenticonGenerator.createIcon(address);
        }
        return image;
    }
}
