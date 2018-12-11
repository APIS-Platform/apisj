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

    public static final Image btnAddressInfo = new Image("image/btn_addressinfo@2x.png");
    public static final Image btnAddressInfoHover = new Image("image/btn_addressinfo_hover@2x.png");
    public static final Image btnSetting = new Image("image/btn_setting@2x.png");;
    public static final Image btnSettingHover = new Image("image/btn_setting_hover@2x.png");
    public static final Image icCircleHalfShow = new Image("image/ic_circle_half_show@2x.png");
    public static final Image icCircleHalfHover = new Image("image/ic_circle_half_hover@2x.png");

    public static final Image btnSearchTokenOut = new Image("image/btn_search_none@2x.png");
    public static final Image btnSearchTokenIn = new Image("image/btn_search_red@2x.png");
    public static final Image btnChangeName = new Image("image/btn_chage_walletname@2x.png");
    public static final Image btnChangeNameHover = new Image("image/btn_chage_walletname_hover@2x.png");
    public static final Image btnChangePassword  = new Image("image/btn_chage_walletpassword@2x.png");
    public static final Image btnChangePasswordHover  = new Image("image/btn_chage_walletpassword_hover@2x.png");

    public static final Image btnChangeProofKey = new Image("image/ic_tool_knowledgekey@2x.png");
    public static final Image btnChangeProofKeyHover = new Image("image/ic_tool_knowledgekey_hover@2x.png");
    public static final Image btnChangeProofKeyUsed = new Image("image/ic_tool_knowledgekey_click@2x.png");

    public static final Image btnBackupWallet  = new Image("image/btn_backupwallet@2x.png");
    public static final Image btnBackupWalletHover  = new Image("image/btn_backupwallet_hover@2x.png");
    public static final Image btnRemoveWallet = new Image("image/btn_deletewallet@2x.png");
    public static final Image btnRemoveWalletHover = new Image("image/btn_deletewallet_hover@2x.png");
    public static final Image btnMiningGrey = new Image("image/ic_miningwallet_grey@2x.png");
    public static final Image btnMiningRed = new Image("image/ic_miningwallet_red@2x.png");

    public static final Image btnAddTransfer = new Image("image/btn_transfer@2x.png");
    public static final Image btnAddTransferHover = new Image("image/btn_transfer_hover@2x.png");
    public static final Image icAddAddressMasking = new Image("image/btn_addressmasking_plus@2x.png");
    public static final Image icAddAddressMaskingHover = new Image("image/btn_addressmasking_plus_hover@2x.png");

    public static final Image btnKeyDelete = new Image("image/btn_keydelete_none@2x.png");
    public static final Image btnKeyDeleteHover = new Image("image/btn_keydelete_hover@2x.png");

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

    public static final Image bgRegisterMask = new Image("image/bg_registermask-none@2x.png");
    public static final Image bgHandOverMask = new Image("image/bg_handovermask-none@2x.png");
    public static final Image bgRegisterDomain = new Image("image/bg_registerdomain-none@2x.png");

    public static final Image bgRegisterMaskHover = new Image("image/bg_registermask_hover@2x.png");
    public static final Image bgHandOverMaskHover = new Image("image/bg_handovermask_hover@2x.png");
    public static final Image bgRegisterDomainHover = new Image("image/bg_registerdomain_hover@2x.png");

    public static final Image btnLeftBack = new Image("image/btn_back_card_none@2x.png");
    public static final Image btnLeftBackHover = new Image("image/btn_back_card_hover@2x.png");

    public static final Image btnPreGasUsed = new Image("image/btn_estimate@2x.png");
    public static final Image btnPreGasUsedHover = new Image("image/btn_estimate_click@2x.png");

    public static final Image icEstimateGasLimit = new Image("image/ic_estimate_gaslimit@2x.png");
    public static final Image icEstimateGasLimitHover = new Image("image/ic_estimate_gaslimit_hover@2x.png");

    public static final Image icCrcleNone = new Image("image/ic_circle_grey@2x.png");
    public static final Image circleCrossGreyCheckBtn = new Image("image/ic_circle_cross_grey@2x.png");
    public static final Image circleCrossRedCheckBtn = new Image("image/ic_circle_cross_red@2x.png");
    public static final Image errorRed = new Image("image/ic_error_red@2x.png");
    public static final Image greenCheckBtn = new Image("image/ic_check@2x.png");
    public static final Image passwordPublic = new Image("image/ic_public@2x.png");
    public static final Image passwordPrivate = new Image("image/ic_private@2x.png");
    public static final Image keyboardBlack = new Image("image/ic_keyboard_black.png");
    public static final Image keyboardGray = new Image("image/ic_keyboard_gray.png");

    public static final Image icBackWhite = new Image("image/ic_back_w@2x.png");
    public static final Image icBackRed = new Image("image/ic_back_r@2x.png");



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

        if(address.length() < 20){
            image = ImageManager.icCrcleNone;
        }
        return image;
    }
}
