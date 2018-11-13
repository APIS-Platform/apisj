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

    public static final Image btnSearchTokenOut = new Image("image/btn_search_none@2x.png");
    public static final Image btnSearchTokenIn = new Image("image/btn_search_red@2x.png");
    public static final Image btnChangeName = new Image("image/btn_chage_walletname@2x.png");
    public static final Image btnChangeNameHover = new Image("image/btn_chage_walletname_hover@2x.png");
    public static final Image btnChangePassword  = new Image("image/btn_chage_walletpassword@2x.png");
    public static final Image btnChangePasswordHover  = new Image("image/btn_chage_walletpassword_hover@2x.png");

    public static final Image btnChangeProofKey = new Image("image/btn_knowledge@2x.png");
    public static final Image btnChangeProofKeyHover = new Image("image/btn_knowledge_hover@2x.png");

    public static final Image btnBackupWallet  = new Image("image/btn_backupwallet@2x.png");
    public static final Image btnBackupWalletHover  = new Image("image/btn_backupwallet_hover@2x.png");
    public static final Image btnRemoveWallet = new Image("image/btn_deletewallet@2x.png");
    public static final Image btnRemoveWalletHover = new Image("image/btn_deletewallet_hover@2x.png");
    public static final Image btnMiningGrey = new Image("image/ic_miningwallet_grey@2x.png");
    public static final Image btnMiningRed = new Image("image/ic_miningwallet_red@2x.png");

    public static final Image btnAddTransfer = new Image("image/btn_transfer@2x.png");
    public static final Image btnAddTransferHover = new Image("image/btn_transfer_hover@2x.png");
    public static final Image btnAddAddressMasking = new Image("image/btn_addressmasking@2x.png");
    public static final Image btnAddAddressMaskingHover = new Image("image/btn_addressmasking_hover@2x.png");

    public static final Image icSortNONE = new Image("image/ic_sort_none@2x.png");
    public static final Image icSortASC = new Image("image/ic_sort_up@2x.png");
    public static final Image icSortDESC = new Image("image/ic_sort_down@2x.png");


    public static final Image icFold = new Image("image/btn_fold@2x.png");
    public static final Image icUnFold = new Image("image/btn_unfold@2x.png");
    public static final Image icCheck = new Image("image/btn_circle_red@2x.png");
    public static final Image icCheckGrayLine = new Image("image/btn_circle_gray_line@2x.png");
    public static final Image icUnCheck = new Image("image/btn_circle_none@2x.png");

    public static final Image icCheckGreen = new Image("image/ic_check_green@2x.png");
    public static final Image icErrorRed = new Image("image/ic_error_red@2x.png");

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
