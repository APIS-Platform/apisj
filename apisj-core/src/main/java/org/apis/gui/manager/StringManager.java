package org.apis.gui.manager;

import javafx.beans.property.SimpleStringProperty;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

public class StringManager {
    private ResourceBundle bundle;

    public Common common = new Common();
    public Intro intro = new Intro();
    public Main main = new Main();
    public Wallet wallet = new Wallet();

    private static StringManager ourInstance = new StringManager();
    public static StringManager getInstance() {
        return ourInstance;
    }

    private StringManager() {
        changeBundleEng();
    }
    public void changeBundleKor(){
        setBundle(ResourceBundle.getBundle("lang/string", new Locale("ko","KR")));
    }
    public void changeBundleEng(){
        setBundle(ResourceBundle.getBundle("lang/string", new Locale("en","US")));
    }
    private void setBundle(ResourceBundle bundle){
        this.bundle = bundle;
        common.update();
        intro.update();
        main.update();
        wallet.update();

    }


    public String getString(String key, String placeHolder){
        String result = placeHolder;

        if(bundle != null) {
            String str = bundle.getString(key);
            if(str != null && str.length() > 0){
                try {
                    result = new String(bundle.getString(key).getBytes("8859_1"), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public interface StringManagerImpl{
        void update();
    }

    public class Common implements StringManagerImpl{
        public SimpleStringProperty walletNamePlaceholder = new SimpleStringProperty();
        public SimpleStringProperty passwordPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty walletNameNull = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordNull = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordCheck = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordNotMatch = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordNotKeystoreMatch = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordMinSize = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordCombination = new SimpleStringProperty();
        public SimpleStringProperty noButton = new SimpleStringProperty();
        public SimpleStringProperty yesButton = new SimpleStringProperty();
        public SimpleStringProperty confirmButton = new SimpleStringProperty();

        @Override
        public void update(){
            walletNameNull.set(StringManager.this.getString("common_wallet_name_null", "Enter new wallet name."));
            walletPasswordNull.set(StringManager.this.getString("common_wallet_password_null", "Please enter your password."));
            walletPasswordCheck.set(StringManager.this.getString("common_wallet_password_check", "Please check your password."));
            walletPasswordNotMatch.set(StringManager.this.getString("common_wallet_password_notmatch", "Password does not match the confirm password."));
            walletPasswordMinSize.set(StringManager.this.getString("common_wallet_password_minsize", "Password must contain at least 8 characters."));
            walletPasswordCombination.set(StringManager.this.getString("common_wallet_password_combination", "Password must contain a combination of letters, numbers, and special characters."));
            walletPasswordNotKeystoreMatch.set(StringManager.this.getString("common_wallet_password_not_keystore_match", "Password does not match to the selected Keystore file."));
            walletNamePlaceholder.set(StringManager.this.getString("common_wallet_name_placeholder", "Wallet Name"));
            passwordPlaceholder.set(StringManager.this.getString("common_password_placeholder", "At least 8 characters including letters, numbers, and special characters."));
            noButton.set(StringManager.this.getString("common_no_button", "No"));
            yesButton.set(StringManager.this.getString("common_yes_button", "Yes"));
            confirmButton.set(StringManager.this.getString("common_confirm_button", "Confirm"));
        }
    }

    public class Intro implements StringManagerImpl{
        public SimpleStringProperty introPhaseOneTitle = new SimpleStringProperty();
        public SimpleStringProperty introPhaseOneMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introPhaseOneMenu2 = new SimpleStringProperty();

        public SimpleStringProperty introCwPhaseTwoTitle = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseTwoMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseTwoMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseThreeTitle = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseThreeMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseThreeMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseFourTitle = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseFourMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introCwPhaseFourMenu1Comment = new SimpleStringProperty();

        public SimpleStringProperty introLwPhaseTwoTitle = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseTwoMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseTwoMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseThreeTitle = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseThreeMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseThreeMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseThreeTitle2 = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseThreeMenu2 = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseThreeMenu2Comment = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseFourTitle = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseFourMenu1 = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseFourMenu1Comment = new SimpleStringProperty();

        public SimpleStringProperty introLwPhaseTwoListItem1 = new SimpleStringProperty();
        public SimpleStringProperty introLwPhaseTwoListItem2 = new SimpleStringProperty();

        public SimpleStringProperty introWalletNameLabel = new SimpleStringProperty();
        public SimpleStringProperty introWalletPasswordLabel = new SimpleStringProperty();
        public SimpleStringProperty introConfirmPasswordLabel = new SimpleStringProperty();

        public SimpleStringProperty introPopupSuccessTitle = new SimpleStringProperty();
        public SimpleStringProperty introPopupSuccessComment = new SimpleStringProperty();
        public SimpleStringProperty introPopupCautionTitle = new SimpleStringProperty();
        public SimpleStringProperty introPopupCautionComment = new SimpleStringProperty();

        @Override
        public void update() {
            introPhaseOneTitle.set(StringManager.this.getString("intro_phase_one_title", "SELECT YOUR WALLET"));
            introPhaseOneMenu1.set(StringManager.this.getString("intro_phase_one_menu_1", "Create Wallet"));
            introPhaseOneMenu2.set(StringManager.this.getString("intro_phase_one_menu_2", "LOAD Wallet"));

            introCwPhaseTwoTitle.set(StringManager.this.getString("intro_cw_phase_two_title", "NAME & PASSWORD"));
            introCwPhaseTwoMenu1.set(StringManager.this.getString("intro_cw_phase_two_menu_1", "Create Wallet"));
            introCwPhaseTwoMenu1Comment.set(StringManager.this.getString("intro_cw_phase_two_menu_1_comment", "Enter a wallet name and a password."));
            introCwPhaseThreeTitle.set(StringManager.this.getString("intro_cw_phase_three_title", "WALLET BACKUP FILE"));
            introCwPhaseThreeMenu1.set(StringManager.this.getString("intro_cw_phase_three_menu_1", "Create Wallet"));
            introCwPhaseThreeMenu1Comment.set(StringManager.this.getString("intro_cw_phase_three_menu_1_comment", "Pay a special attention to your keystore files."));
            introCwPhaseFourTitle.set(StringManager.this.getString("intro_cw_phase_four_title", "PRIVATE KEY"));
            introCwPhaseFourMenu1.set(StringManager.this.getString("intro_cw_phase_four_menu_1", "Create Wallet"));
            introCwPhaseFourMenu1Comment.set(StringManager.this.getString("intro_cw_phase_four_menu_1_comment", "Print & Copy your private key."));

            introLwPhaseTwoTitle.set(StringManager.this.getString("intro_lw_phase_two_title", "LOAD WALLET"));
            introLwPhaseTwoMenu1.set(StringManager.this.getString("intro_lw_phase_two_menu_1", "Load Wallet"));
            introLwPhaseTwoMenu1Comment.set(StringManager.this.getString("intro_lw_phase_two_menu_1_comment", "How would you like to load your wallet?"));
            introLwPhaseThreeTitle.set(StringManager.this.getString("intro_lw_phase_three_title", "SELECT WALLET FILE"));
            introLwPhaseThreeMenu1.set(StringManager.this.getString("intro_lw_phase_three_menu_1", "Load Wallet"));
            introLwPhaseThreeMenu1Comment.set(StringManager.this.getString("intro_lw_phase_three_menu_1_comment", "Select your Keystore file and enter your password."));
            introLwPhaseThreeTitle2.set(StringManager.this.getString("intro_lw_phase_three_title_2", "PRIVATE KEY"));
            introLwPhaseThreeMenu2.set(StringManager.this.getString("intro_lw_phase_three_menu_2", "Load Wallet"));
            introLwPhaseThreeMenu2Comment.set(StringManager.this.getString("intro_lw_phase_three_menu_2_comment", "Write down your private key."));
            introLwPhaseFourTitle.set(StringManager.this.getString("intro_lw_phase_four_title", "PRIVATE KEY"));
            introLwPhaseFourMenu1.set(StringManager.this.getString("intro_lw_phase_four_menu_1", "Load Wallet"));
            introLwPhaseFourMenu1Comment.set(StringManager.this.getString("intro_lw_phase_four_menu_1_comment", "Please enter the new wallet name and the new password."));

            introLwPhaseTwoListItem1.set(StringManager.this.getString("intro_lw_phase_two_list_item_1", "Select Wallet file"));
            introLwPhaseTwoListItem2.set(StringManager.this.getString("intro_lw_phase_two_list_item_2", "Private key"));

            introWalletNameLabel.set(StringManager.this.getString("intro_wallet_name_label", "Wallet Name"));
            introWalletPasswordLabel.set(StringManager.this.getString("intro_wallet_password_label", "Wallet Password"));
            introConfirmPasswordLabel.set(StringManager.this.getString("intro_confirm_password_label", "Confirm Password"));

            introPopupSuccessTitle.set(StringManager.this.getString("intro_popup_success_title", "Success!"));
            introPopupSuccessComment.set(StringManager.this.getString("intro_popup_success_comment", "Download Keystore files. Always keep your Keystore files in a secure location."));
            introPopupCautionTitle.set(StringManager.this.getString("intro_popup_caution_title", "Caution!"));
            introPopupCautionComment.set(StringManager.this.getString("intro_popup_caution_comment", "Do you want to proceed without downloading the Keystore file?"));

        }
    }

    public class Main implements StringManagerImpl{
        public SimpleStringProperty mainTabWallet = new SimpleStringProperty();
        public SimpleStringProperty mainTabTransfer = new SimpleStringProperty();
        public SimpleStringProperty mainTabSmartContract = new SimpleStringProperty();
        public SimpleStringProperty mainTabTransaction = new SimpleStringProperty();
        public SimpleStringProperty mainTabAddressMasking = new SimpleStringProperty();
        public SimpleStringProperty mainFooterTotal = new SimpleStringProperty();
        public SimpleStringProperty mainFooterPeers = new SimpleStringProperty();
        public SimpleStringProperty mainFooterTimer = new SimpleStringProperty();

        @Override
        public void update() {
            mainTabWallet.set(StringManager.this.getString("main_tab_wallet", "Wallet"));
            mainTabTransfer.set(StringManager.this.getString("main_tab_transfer", "Transfer"));
            mainTabSmartContract.set(StringManager.this.getString("main_tab_smartcontract", "Smart Contract"));
            mainTabTransaction.set(StringManager.this.getString("main_tab_transaction", "Transaction"));
            mainTabAddressMasking.set(StringManager.this.getString("main_tab_addressmasking", "Address Masking"));
            mainFooterTotal.set(StringManager.this.getString("main_footer_total", "total"));
            mainFooterPeers.set(StringManager.this.getString("main_footer_peers", "peers"));
            mainFooterTimer.set(StringManager.this.getString("main_footer_timer", "since last block"));
        }
    }

    public class Wallet implements StringManagerImpl{
        public SimpleStringProperty walletTotalAsset = new SimpleStringProperty();
        public SimpleStringProperty walletTotalAmount = new SimpleStringProperty();
        public SimpleStringProperty walletTotalMineralSubAmount = new SimpleStringProperty();
        public SimpleStringProperty walletTotalMineralAmount = new SimpleStringProperty();
        public SimpleStringProperty walletTotalSubAmount = new SimpleStringProperty();
        public SimpleStringProperty walletTotalTransfer = new SimpleStringProperty();
        public SimpleStringProperty walletMyRewards = new SimpleStringProperty();

        @Override
        public void update() {
            walletTotalAsset.set(StringManager.this.getString("wallet_total_asset", "Total Asset"));
            walletTotalAmount.set(StringManager.this.getString("wallet_total_amount", "Amount"));
            walletTotalMineralSubAmount.set(StringManager.this.getString("wallet_total_mineral_sub_amount", "Mineral (APIS Transfer fee)"));
            walletTotalMineralAmount.set(StringManager.this.getString("wallet_total_mineral_amount", "Mineral Amount"));
            walletTotalSubAmount.set(StringManager.this.getString("wallet_total_sub_amount", "APIS AMOUNT"));
            walletTotalTransfer.set(StringManager.this.getString("wallet_total_transfer", "Transfer"));
            walletMyRewards.set(StringManager.this.getString("wallet_my_rewards", "My rewards"));
        }
    }
}
