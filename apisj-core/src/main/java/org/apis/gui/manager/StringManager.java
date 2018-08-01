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
    public Transfer transfer = new Transfer();
    public AddressMasking addressMasking = new AddressMasking();
    public Popup popup = new Popup();

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
        transfer.update();
        addressMasking.update();
        popup.update();
    }


    public String getString(String key, String placeHolder){
        String result = placeHolder;

        if(bundle != null) {
            try {
                String str = bundle.getString(key);
                if(str != null && str.length() > 0){
                    result = new String(bundle.getString(key).getBytes("8859_1"), "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e){
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

    public class Transfer implements StringManagerImpl{
        public SimpleStringProperty title = new SimpleStringProperty();
        public SimpleStringProperty selectWalletName = new SimpleStringProperty();
        public SimpleStringProperty amountToSend = new SimpleStringProperty();
        public SimpleStringProperty transferAmount = new SimpleStringProperty();
        public SimpleStringProperty fee = new SimpleStringProperty();
        public SimpleStringProperty feeComment = new SimpleStringProperty();
        public SimpleStringProperty total = new SimpleStringProperty();
        public SimpleStringProperty totalMineral = new SimpleStringProperty();
        public SimpleStringProperty detail = new SimpleStringProperty();
        public SimpleStringProperty apisFee = new SimpleStringProperty();
        public SimpleStringProperty low = new SimpleStringProperty();
        public SimpleStringProperty high = new SimpleStringProperty();
        public SimpleStringProperty gaspriceComment1 = new SimpleStringProperty();
        public SimpleStringProperty gaspriceComment2 = new SimpleStringProperty();
        public SimpleStringProperty recevingAddress = new SimpleStringProperty();
        public SimpleStringProperty myAddress = new SimpleStringProperty();
        public SimpleStringProperty recentAddress = new SimpleStringProperty();
        public SimpleStringProperty recevingAddressPlaceHolder = new SimpleStringProperty();
        public SimpleStringProperty detailTransferAmount = new SimpleStringProperty();
        public SimpleStringProperty detailFee = new SimpleStringProperty();
        public SimpleStringProperty detailTotalWithdrawal = new SimpleStringProperty();
        public SimpleStringProperty detailAfterBalance = new SimpleStringProperty();
        public SimpleStringProperty detailGaspriceComment1 = new SimpleStringProperty();
        public SimpleStringProperty detailGaspriceComment2 = new SimpleStringProperty();
        public SimpleStringProperty transferButton = new SimpleStringProperty();

        @Override
        public void update() {
            title.set(StringManager.this.getString("transfer_title", "Transfer"));
            selectWalletName.set(StringManager.this.getString("transfer_select_wallet_name", "Select Wallet Name"));
            amountToSend.set(StringManager.this.getString("transfer_amount_to_send", "Amount to Send"));
            transferAmount.set(StringManager.this.getString("transfer_transfer_amount", "Transfer Amount"));
            fee.set(StringManager.this.getString("transfer_fee", "Fee"));
            feeComment.set(StringManager.this.getString("transfer_fee_comment", "( APIS Gas Price - Total MINERAL )"));
            total.set(StringManager.this.getString("transfer_total", "* Total : "));
            totalMineral.set(StringManager.this.getString("transfer_total_mineral", "* Total MINERAL"));
            detail.set(StringManager.this.getString("transfer_detail", "Detail"));
            apisFee.set(StringManager.this.getString("transfer_apis_fee", "APIS (Fee)"));
            low.set(StringManager.this.getString("transfer_low", "Low (slow transfer)"));
            high.set(StringManager.this.getString("transfer_high", "High (fast transfer)"));
            gaspriceComment1.set(StringManager.this.getString("transfer_gasprice_comment_1", "This is the maximum amount that will be used to process this transaction."));
            gaspriceComment2.set(StringManager.this.getString("transfer_gasprice_comment_2", "Your transaction will be registered in the block chain within approximately 20 seconds"));
            recevingAddress.set(StringManager.this.getString("transfer_receving_address", "Receving Address"));
            myAddress.set(StringManager.this.getString("transfer_my_address_button", "My Address"));
            recentAddress.set(StringManager.this.getString("transfer_recent_address_button", "Recent Address"));
            recevingAddressPlaceHolder.set(StringManager.this.getString("transfer_receving_address_placeholder", "Write Reving Address"));
            detailTransferAmount.set(StringManager.this.getString("transfer_detail_transfer_amount", "Transfer Amount"));
            detailFee.set(StringManager.this.getString("transfer_detail_fee", "(+) Fee"));
            detailTotalWithdrawal.set(StringManager.this.getString("transfer_detail_total_withdrawal", "Total Withdrawal"));
            detailAfterBalance.set(StringManager.this.getString("transfer_detail_after_balance", "After Balance"));
            detailGaspriceComment1.set(StringManager.this.getString("transfef_detail_gasprice_comment_1", "Please check the amount and the address."));
            detailGaspriceComment2.set(StringManager.this.getString("transfef_detail_gasprice_comment_2", "You CANNOT cancel the transaction after you confirm."));
            transferButton.set(StringManager.this.getString("transfer_transfer_button", "Transfer"));
        }
    }

    public class AddressMasking implements StringManagerImpl {
        public SimpleStringProperty tabTitle = new SimpleStringProperty();
        public SimpleStringProperty tabLabel1 = new SimpleStringProperty();
        public SimpleStringProperty tabLabel2 = new SimpleStringProperty();
        public SimpleStringProperty registerAddressLabel = new SimpleStringProperty();
        public SimpleStringProperty registerAddressDesc = new SimpleStringProperty();
        public SimpleStringProperty registerAddressMsg = new SimpleStringProperty();
        public SimpleStringProperty selectDomainLabel = new SimpleStringProperty();
        public SimpleStringProperty selectDomainDesc = new SimpleStringProperty();
        public SimpleStringProperty selectDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty registerIdLabel = new SimpleStringProperty();
        public SimpleStringProperty registerIdDesc = new SimpleStringProperty();
        public SimpleStringProperty registerIdPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty totalFeeTitle = new SimpleStringProperty();
        public SimpleStringProperty totalFeeAddress = new SimpleStringProperty();
        public SimpleStringProperty totalFeeAlias = new SimpleStringProperty();
        public SimpleStringProperty totalFeeLabel = new SimpleStringProperty();
        public SimpleStringProperty totalFeePayer = new SimpleStringProperty();
        public SimpleStringProperty totalFeeDesc = new SimpleStringProperty();
        public SimpleStringProperty totalFeePayBtn = new SimpleStringProperty();
        public SimpleStringProperty registerDomainLabel = new SimpleStringProperty();
        public SimpleStringProperty registerDomainDesc = new SimpleStringProperty();
        public SimpleStringProperty sideTabLabel1 = new SimpleStringProperty();
        public SimpleStringProperty sideTabLabel2 = new SimpleStringProperty();
        public SimpleStringProperty sideTab1Desc1 = new SimpleStringProperty();
        public SimpleStringProperty sideTab1Desc2 = new SimpleStringProperty();
        public SimpleStringProperty sideTab1Desc3 = new SimpleStringProperty();
        public SimpleStringProperty sideTab2Desc1 = new SimpleStringProperty();
        public SimpleStringProperty sideTab2Desc2 = new SimpleStringProperty();
        public SimpleStringProperty sideTab2Desc3 = new SimpleStringProperty();
        public SimpleStringProperty sideTab2Desc4 = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainTitle = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainDesc = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainDesc1 = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainDesc2 = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainDesc3 = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty commercialDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty fileFormMsg = new SimpleStringProperty();
        public SimpleStringProperty emailAddrLabel = new SimpleStringProperty();
        public SimpleStringProperty emailPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty emailDesc1 = new SimpleStringProperty();
        public SimpleStringProperty emailDesc2 = new SimpleStringProperty();
        public SimpleStringProperty emailDesc3 = new SimpleStringProperty();
        public SimpleStringProperty requestBtnLabel = new SimpleStringProperty();
        public SimpleStringProperty publicDomainTitle = new SimpleStringProperty();
        public SimpleStringProperty publicDomainDesc = new SimpleStringProperty();
        public SimpleStringProperty publicDomainDesc1 = new SimpleStringProperty();
        public SimpleStringProperty publicDomainDesc2 = new SimpleStringProperty();
        public SimpleStringProperty publicDomainDesc3 = new SimpleStringProperty();
        public SimpleStringProperty publicDomainDesc4 = new SimpleStringProperty();
        public SimpleStringProperty publicDomainPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty publicDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty publicMessageTitle = new SimpleStringProperty();
        public SimpleStringProperty publicMessageDesc = new SimpleStringProperty();
        public SimpleStringProperty publicTextareaPlaceholder = new SimpleStringProperty();

        @Override
        public void update() {
            tabTitle.set(StringManager.this.getString("address_masking_tab_title", "Address Masking"));
            tabLabel1.set(StringManager.this.getString("address_masking_tab_label_1", "Register Alias"));
            tabLabel2.set(StringManager.this.getString("address_masking_tab_label_2", "Register Domain"));
            registerAddressLabel.set(StringManager.this.getString("address_masking_register_address_label", "Address"));
            registerAddressDesc.set(StringManager.this.getString("address_masking_register_address_desc", "Please check if the address is registered."));
            registerAddressMsg.set(StringManager.this.getString("address_masking_register_address_msg", "This address is available"));
            selectDomainLabel.set(StringManager.this.getString("address_masking_select_domain_label", "Select Domain"));
            selectDomainDesc.set(StringManager.this.getString("address_masking_select_domain_desc", "Please select a domain."));
            selectDomainMsg.set(StringManager.this.getString("address_masking_select_domain_msg", "@shop is 10APIS"));
            registerIdLabel.set(StringManager.this.getString("address_masking_register_id_label", "ID"));
            registerIdDesc.set(StringManager.this.getString("address_masking_register_id_desc", "Please input a ID"));
            registerIdPlaceholder.set(StringManager.this.getString("address_masking_register_id_placeholder", "Please enter at least 10 characters."));
            totalFeeTitle.set(StringManager.this.getString("address_masking_total_fee_title", "Total Fee"));
            totalFeeAddress.set(StringManager.this.getString("address_masking_total_fee_address", "Wallet Address :"));
            totalFeeAlias.set(StringManager.this.getString("address_masking_total_fee_alias", "Alias :"));
            totalFeeLabel.set(StringManager.this.getString("address_masking_total_fee_label", "Total Fee"));
            totalFeePayer.set(StringManager.this.getString("address_masking_total_fee_payer", "Payer :"));
            totalFeeDesc.set(StringManager.this.getString("address_masking_total_fee_desc", "It may take one or more minutes for the alias to be registered."));
            totalFeePayBtn.set(StringManager.this.getString("address_masking_total_fee_pay_btn", "PAY"));
            registerDomainLabel.set(StringManager.this.getString("address_masking_register_domain_label", "Register Domain"));
            registerDomainDesc.set(StringManager.this.getString("address_masking_register_domain_desc", "You can request a public domain registration or register a commercial domain."));
            sideTabLabel1.set(StringManager.this.getString("address_masking_side_tab_label_1", "Commercial domain"));
            sideTabLabel2.set(StringManager.this.getString("address_masking_side_tab_label_2", "Public domain"));
            sideTab1Desc1.set(StringManager.this.getString("address_masking_side_tab_1_desc_1", "Commercial domains can only be registered by the administrator's approval."));
            sideTab1Desc2.set(StringManager.this.getString("address_masking_side_tab_1_desc_2", "In order to register a commercial domain you need to prove ownership of the business."));
            sideTab1Desc3.set(StringManager.this.getString("address_masking_side_tab_1_desc_3", "There is a fee for register a commercial domain."));
            sideTab2Desc1.set(StringManager.this.getString("address_masking_side_tab_2_desc_1", "Public domain is available to anyone."));
            sideTab2Desc2.set(StringManager.this.getString("address_masking_side_tab_2_desc_2", "The proposed public domain is registered through voting by the masternodes."));
            sideTab2Desc3.set(StringManager.this.getString("address_masking_side_tab_2_desc_3", "There is a fee proposing a public domain, and a fee will be refunded"));
            sideTab2Desc4.set(StringManager.this.getString("address_masking_side_tab_2_desc_4", "if you register as a domain."));
            commercialDomainTitle.set(StringManager.this.getString("address_masking_commercial_domain_title", "commercial domain"));
            commercialDomainDesc.set(StringManager.this.getString("address_masking_commercial_domain_desc", "Please enter the commercial domain."));
            commercialDomainDesc1.set(StringManager.this.getString("address_masking_side_tab_1_desc_1", "Commercial domains can only be registered by the administrator's approval."));
            commercialDomainDesc2.set(StringManager.this.getString("address_masking_side_tab_1_desc_2", "In order to register a commercial domain you need to prove ownership of the business."));
            commercialDomainDesc3.set(StringManager.this.getString("address_masking_side_tab_1_desc_3", "There is a fee for register a commercial domain."));
            commercialDomainPlaceholder.set(StringManager.this.getString("address_masking_commercial_domain_placeholder", "Please enter the commercial domain"));
            commercialDomainMsg.set(StringManager.this.getString("address_masking_commercial_domain_msg", "Special Characters are not allowed in commercial domain."));
            fileFormMsg.set(StringManager.this.getString("address_masking_file_form_msg", "File : starbucks.docs"));
            emailAddrLabel.set(StringManager.this.getString("address_masking_email_addr_label", "E-mail Address"));
            emailPlaceholder.set(StringManager.this.getString("address_masking_email_placeholder", "Please enter your e-mail"));
            emailDesc1.set(StringManager.this.getString("address_masking_email_desc_1", "We are informing the "));
            emailDesc2.set(StringManager.this.getString("address_masking_email_desc_2", "charged amount "));
            emailDesc3.set(StringManager.this.getString("address_masking_email_desc_3", "via Email."));
            requestBtnLabel.set(StringManager.this.getString("address_masking_request_btn_label", "Request"));
            publicDomainTitle.set(StringManager.this.getString("address_masking_public_domain_title", "Public domain"));
            publicDomainDesc.set(StringManager.this.getString("address_masking_public_domain_desc", "Please check if the domain is registered."));
            publicDomainDesc1.set(StringManager.this.getString("address_masking_side_tab_2_desc_1", "Public domain is available to anyone."));
            publicDomainDesc2.set(StringManager.this.getString("address_masking_side_tab_2_desc_2", "The proposed public domain is registered through voting by the masternodes."));
            publicDomainDesc3.set(StringManager.this.getString("address_masking_side_tab_2_desc_3", "There is a fee proposing a public domain, and a fee will be refunded"));
            publicDomainDesc4.set(StringManager.this.getString("address_masking_side_tab_2_desc_4", "if you register as a domain."));
            publicDomainPlaceholder.set(StringManager.this.getString("address_masking_public_domain_placeholder", "Please enter the public domain"));
            publicDomainMsg.set(StringManager.this.getString("address_masking_public_domain_msg", "@hospital is available."));
            publicMessageTitle.set(StringManager.this.getString("address_masking_public_message_title", "Message"));
            publicMessageDesc.set(StringManager.this.getString("address_masking_public_message_desc", "Purpose of this requested domain."));
            publicTextareaPlaceholder.set(StringManager.this.getString("address_masking_public_textarea_placeholder", "Please enter the message"));
        }
    }

    public class Popup implements StringManagerImpl{
        public SimpleStringProperty changeWalletNameTitle = new SimpleStringProperty();
        public SimpleStringProperty changeWalletNameSubTitle = new SimpleStringProperty();
        public SimpleStringProperty changeWalletNameName = new SimpleStringProperty();
        public SimpleStringProperty changeWalletNameChange = new SimpleStringProperty();
        public SimpleStringProperty changeWalletPasswordTitle = new SimpleStringProperty();
        public SimpleStringProperty changeWalletPasswordSubTitle = new SimpleStringProperty();
        public SimpleStringProperty changeWalletPasswordCurrentPw = new SimpleStringProperty();
        public SimpleStringProperty changeWalletPasswordNewPw = new SimpleStringProperty();
        public SimpleStringProperty changeWalletPasswordChange = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordTitle = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordSubTitle = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordPassword = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordYes  = new SimpleStringProperty();
        public SimpleStringProperty backupWalletTitle = new SimpleStringProperty();
        public SimpleStringProperty backupWalletDownload = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPrivateKey = new SimpleStringProperty();
        public SimpleStringProperty backupWalletFooterComment = new SimpleStringProperty();
        public SimpleStringProperty removeWalletTitle = new SimpleStringProperty();
        public SimpleStringProperty removeWalletSubTitle = new SimpleStringProperty();
        public SimpleStringProperty removeWalletNo = new SimpleStringProperty();
        public SimpleStringProperty removeWalletYes = new SimpleStringProperty();


        @Override
        public void update() {
            changeWalletNameTitle.set(StringManager.this.getString("popup_change_wallet_name_title", "Change Wallet Name"));
            changeWalletNameSubTitle.set(StringManager.this.getString("popup_change_wallet_name_sub_title", "You may change your wallet name."));
            changeWalletNameName.set(StringManager.this.getString("popup_change_wallet_name_name", "Wallet Name"));
            changeWalletNameChange.set(StringManager.this.getString("popup_change_wallet_name_button", "Change"));

            changeWalletPasswordTitle.set(StringManager.this.getString("popup_change_wallet_password_title", "Change Wallet Password"));
            changeWalletPasswordSubTitle.set(StringManager.this.getString("popup_change_wallet_password_sub_title", "You may change your wallet password."));
            changeWalletPasswordCurrentPw.set(StringManager.this.getString("popup_change_wallet_password_current_pw", "Current Password"));
            changeWalletPasswordNewPw.set(StringManager.this.getString("popup_change_wallet_password_new_pw", "New Password"));
            changeWalletPasswordChange.set(StringManager.this.getString("popup_change_wallet_password_button", "Change"));

            backupWalletPasswordTitle.set(StringManager.this.getString("popup_backup_wallet_password_title", "Backup Wallet"));
            backupWalletPasswordSubTitle.set(StringManager.this.getString("popup_backup_wallet_password_sub_title", "Write down your wallet password."));
            backupWalletPasswordPassword.set(StringManager.this.getString("popup_backup_wallet_password_password", "Wallet Password"));
            backupWalletPasswordYes.set(StringManager.this.getString("popup_backup_wallet_password_yes", "Yes"));

            backupWalletTitle.set(StringManager.this.getString("popup_backup_wallet_title", "Backup Wallet"));
            backupWalletDownload.set(StringManager.this.getString("popup_backup_wallet_download_keystore", "Download the keystore file (wallet backup file)"));
            backupWalletPrivateKey.set(StringManager.this.getString("popup_backup_wallet_privatekey", "Private key"));
            backupWalletFooterComment.set(StringManager.this.getString("popup_backup_wallet_footer_comment", "You can load your wallet using the Keystore file or your private key. Please backup your Keystore file or private key."));

            removeWalletTitle.set(StringManager.this.getString("popup_remove_wallet_title", "Remove Wallet!"));
            removeWalletSubTitle.set(StringManager.this.getString("popup_remove_wallet_sub_title", "Are you sure you want to remove your wallet?"));
            removeWalletNo.set(StringManager.this.getString("popup_remove_wallet_no", "No"));
            removeWalletYes.set(StringManager.this.getString("popup_removE_wallet_yes", "Yes"));
        }
    }
}
