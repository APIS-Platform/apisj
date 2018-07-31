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
        public SimpleStringProperty searchApisAndTokens = new SimpleStringProperty();

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
            searchApisAndTokens.set(StringManager.this.getString("common_search_apis_and_tokens", "Search by APIS & Tokens"));
        }
    }

    public class Intro implements StringManagerImpl{
        public SimpleStringProperty phaseOneTitle = new SimpleStringProperty();
        public SimpleStringProperty phaseOneMenu1 = new SimpleStringProperty();
        public SimpleStringProperty phaseOneMenu2 = new SimpleStringProperty();

        public SimpleStringProperty cwPhaseTwoTitle = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseTwoMenu1 = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseTwoMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseThreeTitle = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseThreeMenu1 = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseThreeMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseFourTitle = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseFourMenu1 = new SimpleStringProperty();
        public SimpleStringProperty cwPhaseFourMenu1Comment = new SimpleStringProperty();

        public SimpleStringProperty lwPhaseTwoTitle = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseTwoMenu1 = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseTwoMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseThreeTitle = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseThreeMenu1 = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseThreeMenu1Comment = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseThreeTitle2 = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseThreeMenu2 = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseThreeMenu2Comment = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseFourTitle = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseFourMenu1 = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseFourMenu1Comment = new SimpleStringProperty();

        public SimpleStringProperty lwPhaseTwoListItem1 = new SimpleStringProperty();
        public SimpleStringProperty lwPhaseTwoListItem2 = new SimpleStringProperty();

        public SimpleStringProperty walletNameLabel = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordLabel = new SimpleStringProperty();
        public SimpleStringProperty confirmPasswordLabel = new SimpleStringProperty();

        public SimpleStringProperty popupSuccessTitle = new SimpleStringProperty();
        public SimpleStringProperty popupSuccessComment = new SimpleStringProperty();
        public SimpleStringProperty popupCautionTitle = new SimpleStringProperty();
        public SimpleStringProperty popupCautionComment = new SimpleStringProperty();

        @Override
        public void update() {
            phaseOneTitle.set(StringManager.this.getString("intro_phase_one_title", "SELECT YOUR WALLET"));
            phaseOneMenu1.set(StringManager.this.getString("intro_phase_one_menu_1", "Create Wallet"));
            phaseOneMenu2.set(StringManager.this.getString("intro_phase_one_menu_2", "LOAD Wallet"));

            cwPhaseTwoTitle.set(StringManager.this.getString("intro_cw_phase_two_title", "NAME & PASSWORD"));
            cwPhaseTwoMenu1.set(StringManager.this.getString("intro_cw_phase_two_menu_1", "Create Wallet"));
            cwPhaseTwoMenu1Comment.set(StringManager.this.getString("intro_cw_phase_two_menu_1_comment", "Enter a wallet name and a password."));
            cwPhaseThreeTitle.set(StringManager.this.getString("intro_cw_phase_three_title", "WALLET BACKUP FILE"));
            cwPhaseThreeMenu1.set(StringManager.this.getString("intro_cw_phase_three_menu_1", "Create Wallet"));
            cwPhaseThreeMenu1Comment.set(StringManager.this.getString("intro_cw_phase_three_menu_1_comment", "Pay a special attention to your keystore files."));
            cwPhaseFourTitle.set(StringManager.this.getString("intro_cw_phase_four_title", "PRIVATE KEY"));
            cwPhaseFourMenu1.set(StringManager.this.getString("intro_cw_phase_four_menu_1", "Create Wallet"));
            cwPhaseFourMenu1Comment.set(StringManager.this.getString("intro_cw_phase_four_menu_1_comment", "Print & Copy your private key."));

            lwPhaseTwoTitle.set(StringManager.this.getString("intro_lw_phase_two_title", "LOAD WALLET"));
            lwPhaseTwoMenu1.set(StringManager.this.getString("intro_lw_phase_two_menu_1", "Load Wallet"));
            lwPhaseTwoMenu1Comment.set(StringManager.this.getString("intro_lw_phase_two_menu_1_comment", "How would you like to load your wallet?"));
            lwPhaseThreeTitle.set(StringManager.this.getString("intro_lw_phase_three_title", "SELECT WALLET FILE"));
            lwPhaseThreeMenu1.set(StringManager.this.getString("intro_lw_phase_three_menu_1", "Load Wallet"));
            lwPhaseThreeMenu1Comment.set(StringManager.this.getString("intro_lw_phase_three_menu_1_comment", "Select your Keystore file and enter your password."));
            lwPhaseThreeTitle2.set(StringManager.this.getString("intro_lw_phase_three_title_2", "PRIVATE KEY"));
            lwPhaseThreeMenu2.set(StringManager.this.getString("intro_lw_phase_three_menu_2", "Load Wallet"));
            lwPhaseThreeMenu2Comment.set(StringManager.this.getString("intro_lw_phase_three_menu_2_comment", "Write down your private key."));
            lwPhaseFourTitle.set(StringManager.this.getString("intro_lw_phase_four_title", "PRIVATE KEY"));
            lwPhaseFourMenu1.set(StringManager.this.getString("intro_lw_phase_four_menu_1", "Load Wallet"));
            lwPhaseFourMenu1Comment.set(StringManager.this.getString("intro_lw_phase_four_menu_1_comment", "Please enter the new wallet name and the new password."));

            lwPhaseTwoListItem1.set(StringManager.this.getString("intro_lw_phase_two_list_item_1", "Select Wallet file"));
            lwPhaseTwoListItem2.set(StringManager.this.getString("intro_lw_phase_two_list_item_2", "Private key"));

            walletNameLabel.set(StringManager.this.getString("intro_wallet_name_label", "Wallet Name"));
            walletPasswordLabel.set(StringManager.this.getString("intro_wallet_password_label", "Wallet Password"));
            confirmPasswordLabel.set(StringManager.this.getString("intro_confirm_password_label", "Confirm Password"));

            popupSuccessTitle.set(StringManager.this.getString("intro_popup_success_title", "Success!"));
            popupSuccessComment.set(StringManager.this.getString("intro_popup_success_comment", "Download Keystore files. Always keep your Keystore files in a secure location."));
            popupCautionTitle.set(StringManager.this.getString("intro_popup_caution_title", "Caution!"));
            popupCautionComment.set(StringManager.this.getString("intro_popup_caution_comment", "Do you want to proceed without downloading the Keystore file?"));

        }
    }

    public class Main implements StringManagerImpl{
        public SimpleStringProperty tabWallet = new SimpleStringProperty();
        public SimpleStringProperty tabTransfer = new SimpleStringProperty();
        public SimpleStringProperty tabSmartContract = new SimpleStringProperty();
        public SimpleStringProperty tabTransaction = new SimpleStringProperty();
        public SimpleStringProperty tabAddressMasking = new SimpleStringProperty();
        public SimpleStringProperty footerTotal = new SimpleStringProperty();
        public SimpleStringProperty footerPeers = new SimpleStringProperty();
        public SimpleStringProperty footerTimer = new SimpleStringProperty();

        @Override
        public void update() {
            tabWallet.set(StringManager.this.getString("main_tab_wallet", "Wallet"));
            tabTransfer.set(StringManager.this.getString("main_tab_transfer", "Transfer"));
            tabSmartContract.set(StringManager.this.getString("main_tab_smartcontract", "Smart Contract"));
            tabTransaction.set(StringManager.this.getString("main_tab_transaction", "Transaction"));
            tabAddressMasking.set(StringManager.this.getString("main_tab_addressmasking", "Address Masking"));
            footerTotal.set(StringManager.this.getString("main_footer_total", "total"));
            footerPeers.set(StringManager.this.getString("main_footer_peers", "peers"));
            footerTimer.set(StringManager.this.getString("main_footer_timer", "since last block"));
        }
    }

    public class Wallet implements StringManagerImpl{
        public SimpleStringProperty totalAsset = new SimpleStringProperty();
        public SimpleStringProperty totalAmount = new SimpleStringProperty();
        public SimpleStringProperty totalMineralSubAmount = new SimpleStringProperty();
        public SimpleStringProperty totalMineralAmount = new SimpleStringProperty();
        public SimpleStringProperty totalSubAmount = new SimpleStringProperty();
        public SimpleStringProperty totalTransfer = new SimpleStringProperty();
        public SimpleStringProperty myRewards = new SimpleStringProperty();
        public SimpleStringProperty rewarded = new SimpleStringProperty();
        public SimpleStringProperty nowStaking = new SimpleStringProperty();
        public SimpleStringProperty howToGetRewardedWithApis = new SimpleStringProperty();
        public SimpleStringProperty createButton = new SimpleStringProperty();
        public SimpleStringProperty miningButton = new SimpleStringProperty();
        public SimpleStringProperty tabApis = new SimpleStringProperty();
        public SimpleStringProperty tabMineral = new SimpleStringProperty();
        public SimpleStringProperty tabWallet = new SimpleStringProperty();
        public SimpleStringProperty tabAppAndTokens = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderName = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderAddressMasking = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderAmount = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderTransfer = new SimpleStringProperty();

        @Override
        public void update() {
            totalAsset.set(StringManager.this.getString("wallet_total_asset", "Total Asset"));
            totalAmount.set(StringManager.this.getString("wallet_total_amount", "Amount"));
            totalMineralSubAmount.set(StringManager.this.getString("wallet_total_mineral_sub_amount", "Mineral (APIS Transfer fee)"));
            totalMineralAmount.set(StringManager.this.getString("wallet_total_mineral_amount", "Mineral Amount"));
            totalSubAmount.set(StringManager.this.getString("wallet_total_sub_amount", "APIS AMOUNT"));
            totalTransfer.set(StringManager.this.getString("wallet_total_transfer", "Transfer"));
            myRewards.set(StringManager.this.getString("wallet_my_rewards", "My rewards"));
            rewarded.set(StringManager.this.getString("wallet_rewarded", "rewarded"));
            nowStaking.set(StringManager.this.getString("wallet_now_staking", "Now Staking"));
            howToGetRewardedWithApis.set(StringManager.this.getString("wallet_how_to_get_rewarded_with_apis", "How to get rewarded with APIS?"));
            createButton.set(StringManager.this.getString("wallet_create_button", "Create Wallet"));
            miningButton.set(StringManager.this.getString("wallet_mining_button", "Mining Wallet"));
            tabApis.set(StringManager.this.getString("wallet_tab_apis", "APIS"));
            tabMineral.set(StringManager.this.getString("wallet_tab_mineral", "Mineral"));
            tabWallet.set(StringManager.this.getString("wallet_tab_wallet", "Wallet"));
            tabAppAndTokens.set(StringManager.this.getString("wallet_tab_apis_and_tokens", "APIS & TOKENS"));
            tableHeaderName.set(StringManager.this.getString("wallet_table_header_name", "name"));
            tableHeaderAddressMasking.set(StringManager.this.getString("wallet_table_header_address_masking", "address masking"));
            tableHeaderAmount.set(StringManager.this.getString("wallet_table_header_amount", "APIS amount"));
            tableHeaderTransfer.set(StringManager.this.getString("wallet_table_header_transfer", "transfer"));
        }
    }
}
