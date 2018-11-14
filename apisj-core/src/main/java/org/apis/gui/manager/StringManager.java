package org.apis.gui.manager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

public class StringManager {
    private ResourceBundle bundle;

    public Common common = new Common();
    public Module module = new Module();
    public Intro intro = new Intro();
    public Main main = new Main();
    public Wallet wallet = new Wallet();
    public Transfer transfer = new Transfer();
    public SmartContract smartContract = new SmartContract();
    public Transaction transaction = new Transaction();
    public AddressMasking addressMasking = new AddressMasking();
    public Popup popup = new Popup();
    public Setting setting = new Setting();
    public ContractPopup contractPopup = new ContractPopup();
    public MyAddress myAddress = new MyAddress();
    public RecentAddress recentAddress = new RecentAddress();
    public AddressInfo addressInfo = new AddressInfo();

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
        module.update();
        intro.update();
        main.update();
        wallet.update();
        transfer.update();
        smartContract.update();
        transaction.update();
        addressMasking.update();
        popup.update();
        setting.update();
        contractPopup.update();
        myAddress.update();
        recentAddress.update();
        addressInfo.update();
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
        public SimpleStringProperty closeButton = new SimpleStringProperty();
        public SimpleStringProperty saveButton = new SimpleStringProperty();
        public SimpleStringProperty editButton = new SimpleStringProperty();
        public SimpleStringProperty selectButton = new SimpleStringProperty();
        public SimpleStringProperty noButton = new SimpleStringProperty();
        public SimpleStringProperty yesButton = new SimpleStringProperty();
        public SimpleStringProperty addButton = new SimpleStringProperty();
        public SimpleStringProperty backButton = new SimpleStringProperty();
        public SimpleStringProperty nextButton = new SimpleStringProperty();
        public SimpleStringProperty payButton = new SimpleStringProperty();
        public SimpleStringProperty confirmButton = new SimpleStringProperty();
        public SimpleStringProperty requestButton = new SimpleStringProperty();
        public SimpleStringProperty suggestingButton = new SimpleStringProperty();
        public SimpleStringProperty searchApisAndTokens = new SimpleStringProperty();
        public SimpleStringProperty directInputButton = new SimpleStringProperty();

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
            closeButton.set(StringManager.this.getString("common_close_button", "Close"));
            saveButton.set(StringManager.this.getString("common_save_button", "Save"));
            editButton.set(StringManager.this.getString("common_edit_button", "Edit"));
            selectButton.set(StringManager.this.getString("common_select_button", "Select"));
            noButton.set(StringManager.this.getString("common_no_button", "No"));
            yesButton.set(StringManager.this.getString("common_yes_button", "Yes"));
            addButton.set(StringManager.this.getString("common_add_button", "Add"));
            backButton.set(StringManager.this.getString("common_back_button", "Back"));
            nextButton.set(StringManager.this.getString("common_next_button", "Next"));
            payButton.set(StringManager.this.getString("commom_pay_button", "Pay"));
            confirmButton.set(StringManager.this.getString("common_confirm_button", "Confirm"));
            searchApisAndTokens.set(StringManager.this.getString("common_search_apis_and_tokens", "Search by APIS & Tokens"));
            requestButton.set(StringManager.this.getString("common_request_button","Request"));
            suggestingButton.set(StringManager.this.getString("common_suggesting_button","Suggesting"));
            directInputButton.set(StringManager.this.getString("common_direct_input_button","direct input"));

        }
    }

    public class Module implements StringManagerImpl{
        public SimpleStringProperty selectWallet = new SimpleStringProperty();
        public SimpleStringProperty amountToSend = new SimpleStringProperty();
        public SimpleStringProperty apisTotal = new SimpleStringProperty();

        public SimpleStringProperty gasPriceTitle = new SimpleStringProperty();
        public SimpleStringProperty gasPriceFormula = new SimpleStringProperty();
        public SimpleStringProperty gasPriceLabel = new SimpleStringProperty();
        public SimpleStringProperty gasLimitLabel = new SimpleStringProperty();
        public SimpleStringProperty detailLabel = new SimpleStringProperty();
        public SimpleStringProperty detailContentsFee = new SimpleStringProperty();
        public SimpleStringProperty detailContentsTotal = new SimpleStringProperty();
        public SimpleStringProperty tab1DefaultLabel = new SimpleStringProperty();
        public SimpleStringProperty tab1LowLabel = new SimpleStringProperty();
        public SimpleStringProperty tab1HighLabel = new SimpleStringProperty();

        @Override
        public void update() {
            selectWallet.set(StringManager.this.getString("module_select_wallet", "Select Wallet Name"));
            amountToSend.set(StringManager.this.getString("module_amount_to_send", "Amount to Send"));
            apisTotal.set(StringManager.this.getString("module_total_apis", "* APIS Total : "));

            gasPriceTitle.set(StringManager.this.getString("module_gas_price_title", "Gas Fee "));
            gasPriceFormula.set(StringManager.this.getString("module_gas_price_formula", "Gas price x Gas Limit "));
            gasPriceLabel.set(StringManager.this.getString("module_gas_price_label", "Gas price :"));
            gasLimitLabel.set(StringManager.this.getString("module_gas_limit_label", "Gas Limit"));
            detailLabel.set(StringManager.this.getString("module_detail_label", "Detail"));
            detailContentsFee.set(StringManager.this.getString("module_detail_contents_fee", "(Fee)"));
            detailContentsTotal.set(StringManager.this.getString("module_detail_contents_total", "(Total)"));
            tab1DefaultLabel.set(StringManager.this.getString("module_tab_1_default_label", "(DEFAULT)"));
            tab1LowLabel.set(StringManager.this.getString("module_tab_1_low_label", "Low (slow transfer)"));
            tab1HighLabel.set(StringManager.this.getString("module_tab_1_high_label", "High (fast transfer)"));

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
        public SimpleStringProperty totalBuyMineral = new SimpleStringProperty();
        public SimpleStringProperty myRewards = new SimpleStringProperty();
        public SimpleStringProperty rewarded = new SimpleStringProperty();
        public SimpleStringProperty nowStaking = new SimpleStringProperty();
        public SimpleStringProperty howToGetRewardedWithApis = new SimpleStringProperty();
        public SimpleStringProperty createButton = new SimpleStringProperty();
        public SimpleStringProperty miningButton = new SimpleStringProperty();
        public SimpleStringProperty masternodeButton = new SimpleStringProperty();
        public SimpleStringProperty tokenButton = new SimpleStringProperty();
        public SimpleStringProperty tabApis = new SimpleStringProperty();
        public SimpleStringProperty tabMineral = new SimpleStringProperty();
        public SimpleStringProperty tabWallet = new SimpleStringProperty();
        public SimpleStringProperty tabAppAndTokens = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderName = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderAddressMasking = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderAmount = new SimpleStringProperty();
        public SimpleStringProperty tableHeaderTransfer = new SimpleStringProperty();
        public SimpleStringProperty changeWalletName = new SimpleStringProperty();
        public SimpleStringProperty changeWalletPassword = new SimpleStringProperty();
        public SimpleStringProperty changeProofKey = new SimpleStringProperty();
        public SimpleStringProperty backupWallet = new SimpleStringProperty();
        public SimpleStringProperty removeWallet = new SimpleStringProperty();

        @Override
        public void update() {
            totalAsset.set(StringManager.this.getString("wallet_total_asset", "Total Asset"));
            totalAmount.set(StringManager.this.getString("wallet_total_amount", "Amount"));
            totalMineralSubAmount.set(StringManager.this.getString("wallet_total_mineral_sub_amount", "Mineral (APIS Transfer fee)"));
            totalMineralAmount.set(StringManager.this.getString("wallet_total_mineral_amount", "Mineral Amount"));
            totalSubAmount.set(StringManager.this.getString("wallet_total_sub_amount", "APIS AMOUNT"));
            totalTransfer.set(StringManager.this.getString("wallet_total_transfer", "Transfer"));
            totalBuyMineral.set(StringManager.this.getString("wallet_total_buy_mineral", "BUY MINERAL"));
            myRewards.set(StringManager.this.getString("wallet_my_rewards", "My rewards"));
            rewarded.set(StringManager.this.getString("wallet_rewarded", "rewarded"));
            nowStaking.set(StringManager.this.getString("wallet_now_staking", "Now Staking"));
            howToGetRewardedWithApis.set(StringManager.this.getString("wallet_how_to_get_rewarded_with_apis", "How to get rewarded with APIS?"));
            createButton.set(StringManager.this.getString("wallet_create_button", "Create Wallet"));
            miningButton.set(StringManager.this.getString("wallet_mining_button", "Mining Wallet"));
            masternodeButton.set(StringManager.this.getString("wallet_masternode_button", "Masternode"));
            tokenButton.set(StringManager.this.getString("wallet_token_button", "Token"));
            tabApis.set(StringManager.this.getString("wallet_tab_apis", "APIS"));
            tabMineral.set(StringManager.this.getString("wallet_tab_mineral", "Mineral"));
            tabWallet.set(StringManager.this.getString("wallet_tab_wallet", "Wallet"));
            tabAppAndTokens.set(StringManager.this.getString("wallet_tab_apis_and_tokens", "APIS & TOKENS"));
            tableHeaderName.set(StringManager.this.getString("wallet_table_header_name", "name"));
            tableHeaderAddressMasking.set(StringManager.this.getString("wallet_table_header_address_masking", "address masking"));
            tableHeaderAmount.set(StringManager.this.getString("wallet_table_header_amount", "amount"));
            tableHeaderTransfer.set(StringManager.this.getString("wallet_table_header_transfer", "transfer"));
            changeWalletName.set(StringManager.this.getString("wallet_tooltip_changewalletname","Change Wallet Name"));
            changeWalletPassword.set(StringManager.this.getString("wallet_tooltip_changewalletpassword","Change Wallet Password"));
            changeProofKey.set(StringManager.this.getString("wallet_tooltip_changeproofkey","Change Wallet Proof Key"));
            backupWallet.set(StringManager.this.getString("wallet_tooltip_backupwallet","Backup Wallet"));
            removeWallet.set(StringManager.this.getString("wallet_tooltip_removewallet","Remove Wallet"));
        }
    }

    public class Transfer implements StringManagerImpl{
        public SimpleStringProperty title = new SimpleStringProperty();
        public SimpleStringProperty selectWalletName = new SimpleStringProperty();
        public SimpleStringProperty amountToSend = new SimpleStringProperty();
        public SimpleStringProperty transferAmount = new SimpleStringProperty();
        public SimpleStringProperty total = new SimpleStringProperty();
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
            total.set(StringManager.this.getString("transfer_total", "* Total : "));
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

    public class SmartContract implements StringManagerImpl {
        public SimpleStringProperty tabTitle = new SimpleStringProperty();
        public SimpleStringProperty tabLabel1 = new SimpleStringProperty();
        public SimpleStringProperty tabLabel2 = new SimpleStringProperty();
        public SimpleStringProperty tabLabel3 = new SimpleStringProperty();
        public SimpleStringProperty tabLabel4 = new SimpleStringProperty();
        public SimpleStringProperty selectWallet = new SimpleStringProperty();
        public SimpleStringProperty amountToSend = new SimpleStringProperty();
        public SimpleStringProperty sideTabLabel1 = new SimpleStringProperty();
        public SimpleStringProperty sideTabLabel2 = new SimpleStringProperty();
        public SimpleStringProperty textareaMessage = new SimpleStringProperty();
        public SimpleStringProperty selectContract = new SimpleStringProperty();
        public SimpleStringProperty readWriteContract = new SimpleStringProperty();
        public SimpleStringProperty solidityCode = new SimpleStringProperty();

        @Override
        public void update() {
            tabTitle.set(StringManager.this.getString("smart_contract_tab_title", "Smart Contract"));
            tabLabel1.set(StringManager.this.getString("smart_contract_tab_label_1", "Deploy"));
            tabLabel2.set(StringManager.this.getString("smart_contract_tab_label_2", "Call / Send"));
            tabLabel3.set(StringManager.this.getString("smart_contract_tab_label_3", "Contract Updater"));
            tabLabel4.set(StringManager.this.getString("smart_contract_tab_label_4", "Canvas"));
            selectWallet.set(StringManager.this.getString("smart_contract_select_wallet", "Select Wallet Name"));
            amountToSend.set(StringManager.this.getString("smart_contract_amount_to_send", "Amount to Send"));
            sideTabLabel1.set(StringManager.this.getString("smart_contract_side_tab_label_1", "Solidity Contract"));
            sideTabLabel2.set(StringManager.this.getString("smart_contract_side_tab_label_2", "Contract byte code"));
            textareaMessage.set(StringManager.this.getString("smart_contract_textarea_message", "Message"));
            selectContract.set(StringManager.this.getString("smart_contract_select_contract", "Select Contract"));
            readWriteContract.set(StringManager.this.getString("smart_contract_read_write_contract", "Read / Write Contract"));
            solidityCode.set(StringManager.this.getString("smart_contract_solidity_code", "Solidity Code"));
        }
    }

    public class Transaction implements StringManagerImpl {
        public SimpleStringProperty searchTransactionsLabel = new SimpleStringProperty();
        public SimpleStringProperty dropBoxLabel = new SimpleStringProperty();
        public SimpleStringProperty transactionsLabel = new SimpleStringProperty();
        public SimpleStringProperty browseAllTx = new SimpleStringProperty();
        public SimpleStringProperty pageLabel = new SimpleStringProperty();
        public SimpleStringProperty hashLabel = new SimpleStringProperty();
        public SimpleStringProperty blockLabel = new SimpleStringProperty();
        public SimpleStringProperty fromLabel = new SimpleStringProperty();
        public SimpleStringProperty toLabel = new SimpleStringProperty();
        public SimpleStringProperty forLabel = new SimpleStringProperty();
        public SimpleStringProperty valueLabel = new SimpleStringProperty();
        public SimpleStringProperty feeLabel = new SimpleStringProperty();
        public SimpleStringProperty timeLabel = new SimpleStringProperty();
        public SimpleStringProperty transferLabel = new SimpleStringProperty();
        public SimpleStringProperty listBlockPending = new SimpleStringProperty();
        public SimpleStringProperty listBlockSuccess = new SimpleStringProperty();
        public SimpleStringProperty listBlockFail = new SimpleStringProperty();
        public SimpleStringProperty detailsLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsHashLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsNonceLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsBlockConfirmLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsConfirmedInLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsConfirmedInUnit = new SimpleStringProperty();
        public SimpleStringProperty detailsContractAddrLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsInternalTxLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsTokenTransferedLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsMineralLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsChargedFeeLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsGasLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsErrorLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsDataLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsInputDataLabel = new SimpleStringProperty();
        public SimpleStringProperty detailsEventLogsLabel = new SimpleStringProperty();
        public SimpleStringProperty selectAllLabel = new SimpleStringProperty();

        @Override
        public void update() {
            searchTransactionsLabel.set(StringManager.this.getString("transaction_search_label", "Search Transaction"));
            dropBoxLabel.set(StringManager.this.getString("transaction_drop_box_label", "Select your wallet address"));
            transactionsLabel.set(StringManager.this.getString("transaction_transactions_label", "Transactions"));
            browseAllTx.set(StringManager.this.getString("transaction_browse_all_tx", "Browse all APIS Transactions"));
            pageLabel.set(StringManager.this.getString("transaction_page_label", "Page"));
            hashLabel.set(StringManager.this.getString("transaction_hash_label", "Hash"));
            blockLabel.set(StringManager.this.getString("transaction_block_label", "Block"));
            fromLabel.set(StringManager.this.getString("transaction_from_label", "From"));
            toLabel.set(StringManager.this.getString("transaction_to_label", "To"));
            forLabel.set(StringManager.this.getString("transaction_for_label", "for"));
            valueLabel.set(StringManager.this.getString("transaction_value_label", "Value"));
            feeLabel.set(StringManager.this.getString("transaction_fee_label", "Fee"));
            timeLabel.set(StringManager.this.getString("transaction_time_label", "Time"));
            transferLabel.set(StringManager.this.getString("transaction_transfer_label", "TRANSFER"));
            listBlockPending.set(StringManager.this.getString("transaction_list_block_pending", "Pending.."));
            listBlockSuccess.set(StringManager.this.getString("transaction_list_block_success", "Success"));
            listBlockFail.set(StringManager.this.getString("transaction_list_block_fail", "Fail"));
            detailsLabel.set(StringManager.this.getString("transaction_details_label", "Transaction details"));
            detailsHashLabel.set(StringManager.this.getString("transaction_details_hash_label", "Hash :"));
            detailsNonceLabel.set(StringManager.this.getString("transaction_details_nonce_label", "Nonce"));
            detailsBlockConfirmLabel.set(StringManager.this.getString("transaction_details_block_confirm_label", " Confirmations"));
            detailsConfirmedInLabel.set(StringManager.this.getString("transaction_details_confirmed_in_label", "Confirmed In"));
            detailsConfirmedInUnit.set(StringManager.this.getString("transaction_details_confirmed_in_unit", " Seconds"));
            detailsContractAddrLabel.set(StringManager.this.getString("transaction_details_contract_addr_label", "Contract address"));
            detailsInternalTxLabel.set(StringManager.this.getString("transaction_details_internal_tx_label", "Internal Transaction"));
            detailsTokenTransferedLabel.set(StringManager.this.getString("transaction_details_tokens_transfered_label", "Tokens Transfered"));
            detailsMineralLabel.set(StringManager.this.getString("transaction_details_mineral_label", "Mineral"));
            detailsChargedFeeLabel.set(StringManager.this.getString("transaction_details_charged_fee_label", "Charged fee"));
            detailsGasLabel.set(StringManager.this.getString("transaction_details_gas_label", "Gas Price / Limit / Used"));
            detailsErrorLabel.set(StringManager.this.getString("transaction_details_error_label", "Error"));
            detailsDataLabel.set(StringManager.this.getString("transaction_details_data_label", "Data"));
            detailsInputDataLabel.set(StringManager.this.getString("transaction_details_input_data_label", "Input Data"));
            detailsEventLogsLabel.set(StringManager.this.getString("transaction_details_event_logs_label", "Event Logs"));
            selectAllLabel.set(StringManager.this.getString("transaction_drop_select_all_label", "Select All"));
        }
    }

    public class AddressMasking implements StringManagerImpl {
        public SimpleStringProperty tabTitle = new SimpleStringProperty();
        public SimpleStringProperty tabRegisterMask = new SimpleStringProperty();
        public SimpleStringProperty tabHandOverMask = new SimpleStringProperty();
        public SimpleStringProperty tabRegisterDomain = new SimpleStringProperty();
        public SimpleStringProperty registerAddressLabel = new SimpleStringProperty();
        public SimpleStringProperty registerAddressDesc = new SimpleStringProperty();
        public SimpleStringProperty registerAddressMsg = new SimpleStringProperty();
        public SimpleStringProperty registerAddressMsg2 = new SimpleStringProperty();
        public SimpleStringProperty selectDomainLabel = new SimpleStringProperty();
        public SimpleStringProperty selectDomainDesc = new SimpleStringProperty();
        public SimpleStringProperty selectDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty registerIdLabel = new SimpleStringProperty();
        public SimpleStringProperty registerIdPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty totalFeeTitle = new SimpleStringProperty();
        public SimpleStringProperty totalFeeAddress = new SimpleStringProperty();
        public SimpleStringProperty totalFeeAlias = new SimpleStringProperty();
        public SimpleStringProperty totalFeeMask = new SimpleStringProperty();
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
        public SimpleStringProperty payer = new SimpleStringProperty();
        public SimpleStringProperty totalApis = new SimpleStringProperty();

        @Override
        public void update() {
            tabTitle.set(StringManager.this.getString("address_masking_tab_title", "Address Masking"));
            tabRegisterMask.set(StringManager.this.getString("address_masking_tab_register_mask", "Register Mask"));
            tabHandOverMask.set(StringManager.this.getString("address_masking_tab_hand_over_mask", "Hand Over Mask"));
            tabRegisterDomain.set(StringManager.this.getString("address_masking_tab_register_domain", "Register Domain"));
            registerAddressLabel.set(StringManager.this.getString("address_masking_register_address_label", "Address"));
            registerAddressDesc.set(StringManager.this.getString("address_masking_register_address_desc", "Please check if the address is registered."));
            registerAddressMsg.set(StringManager.this.getString("address_masking_register_address_msg", "This address is available"));
            registerAddressMsg2.set(StringManager.this.getString("address_masking_register_address_msg2", "This address is already in use"));
            selectDomainLabel.set(StringManager.this.getString("address_masking_select_domain_label", "Select Domain"));
            selectDomainDesc.set(StringManager.this.getString("address_masking_select_domain_desc", "Please select a domain."));
            selectDomainMsg.set(StringManager.this.getString("address_masking_select_domain_msg", "@shop is 10APIS"));
            registerIdLabel.set(StringManager.this.getString("address_masking_register_id_label", "ID"));
            registerIdPlaceholder.set(StringManager.this.getString("address_masking_register_id_placeholder", "Please enter at least 10 characters."));
            totalFeeTitle.set(StringManager.this.getString("address_masking_total_fee_title", "Total Fee"));
            totalFeeAddress.set(StringManager.this.getString("address_masking_total_fee_address", "Wallet Address :"));
            totalFeeAlias.set(StringManager.this.getString("address_masking_total_fee_alias", "Alias :"));
            totalFeeMask.set(StringManager.this.getString("address_masking_total_fee_mask", "Mask :"));
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
            payer.set(StringManager.this.getString("address_masking_payer", "Payer"));
            totalApis.set(StringManager.this.getString("address_masking_total_apis", "* Total :"));
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

        public SimpleStringProperty backupWalletTitle = new SimpleStringProperty();
        public SimpleStringProperty backupWalletDownload = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPrivateKey = new SimpleStringProperty();
        public SimpleStringProperty backupWalletFooterComment = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordTitle = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordSubTitle = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordPassword = new SimpleStringProperty();
        public SimpleStringProperty backupWalletPasswordYes  = new SimpleStringProperty();

        public SimpleStringProperty removeWalletTitle = new SimpleStringProperty();
        public SimpleStringProperty removeWalletSubTitle = new SimpleStringProperty();
        public SimpleStringProperty removeWalletNo = new SimpleStringProperty();
        public SimpleStringProperty removeWalletYes = new SimpleStringProperty();
        public SimpleStringProperty removeWalletPasswordTitle = new SimpleStringProperty();
        public SimpleStringProperty removeWalletPasswordSubTitle = new SimpleStringProperty();
        public SimpleStringProperty removeWalletPasswordPassword = new SimpleStringProperty();
        public SimpleStringProperty removeWalletPasswordYes  = new SimpleStringProperty();

        public SimpleStringProperty miningWalletConfirmTitle = new SimpleStringProperty();
        public SimpleStringProperty miningWalletConfirmSubTitle = new SimpleStringProperty();
        public SimpleStringProperty miningWaleltConfirmAddress = new SimpleStringProperty();
        public SimpleStringProperty miningWalletConfirmPassword = new SimpleStringProperty();
        public SimpleStringProperty miningWalletConfirmStart = new SimpleStringProperty();
        public SimpleStringProperty miningWalletConfirmStop = new SimpleStringProperty();
        public SimpleStringProperty miningWalletConfirmAddressComment = new SimpleStringProperty();
        public SimpleStringProperty miningWalletTitle = new SimpleStringProperty();
        public SimpleStringProperty miningWalletSubTitle = new SimpleStringProperty();
        public SimpleStringProperty miningWalletAddress = new SimpleStringProperty();
        public SimpleStringProperty miningWalletAddressComment = new SimpleStringProperty();
        public SimpleStringProperty miningWalletSelect = new SimpleStringProperty();

        public SimpleStringProperty masternodeTitle = new SimpleStringProperty();
        public SimpleStringProperty masternodeWalletAddrLabel = new SimpleStringProperty();
        public SimpleStringProperty masternodePasswordLabel = new SimpleStringProperty();
        public SimpleStringProperty masternodeRecipientLabel = new SimpleStringProperty();
        public SimpleStringProperty masternodeDirectInput = new SimpleStringProperty();
        public SimpleStringProperty masternodeRecipientPlaceholder = new SimpleStringProperty();
        public SimpleStringProperty masternodeRecipientDesc1 = new SimpleStringProperty();
        public SimpleStringProperty masternodeRecipientDesc2 = new SimpleStringProperty();
        public SimpleStringProperty masternodeStartMasternode = new SimpleStringProperty();

        public SimpleStringProperty successTitle = new SimpleStringProperty();
        public SimpleStringProperty successSubTitle = new SimpleStringProperty();
        public SimpleStringProperty successYes = new SimpleStringProperty();
        public SimpleStringProperty failTitle = new SimpleStringProperty();
        public SimpleStringProperty failSubTitle = new SimpleStringProperty();

        public SimpleStringProperty maskingTitle = new SimpleStringProperty();
        public SimpleStringProperty maskingTabRegisterMask = new SimpleStringProperty();
        public SimpleStringProperty maskingTabRegisterDomain = new SimpleStringProperty();
        public SimpleStringProperty maskingAddress = new SimpleStringProperty();
        public SimpleStringProperty maskingDomain = new SimpleStringProperty();
        public SimpleStringProperty maskingId = new SimpleStringProperty();
        public SimpleStringProperty maskingPay = new SimpleStringProperty();
        public SimpleStringProperty maskingAliasPlaseCheckAddress = new SimpleStringProperty();
        public SimpleStringProperty maskingAliasPlaseSelectDomain =  new SimpleStringProperty();
        public SimpleStringProperty maskingAliasPlaseInputId = new SimpleStringProperty();
        public SimpleStringProperty maskingAliasAddressMsg = new SimpleStringProperty();
        public SimpleStringProperty maskingAliasAddressMsg2 = new SimpleStringProperty();
        public SimpleStringProperty maskingAliasDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty maskingSuccess  = new SimpleStringProperty();
        public SimpleStringProperty maskingWalletAddress = new SimpleStringProperty();
        public SimpleStringProperty maskingAlias = new SimpleStringProperty();
        public SimpleStringProperty maskingTotalFee = new SimpleStringProperty();
        public SimpleStringProperty maskingPayer = new SimpleStringProperty();
        public SimpleStringProperty maskingPayMsg1 = new SimpleStringProperty();
        public SimpleStringProperty maskingPayMsg2 = new SimpleStringProperty();
        public SimpleStringProperty maskingRegisterDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty maskingCommercialDomain = new SimpleStringProperty();
        public SimpleStringProperty maskingCommercialDomainMsg1 = new SimpleStringProperty();
        public SimpleStringProperty maskingCommercialDomainMsg2 = new SimpleStringProperty();
        public SimpleStringProperty maskingCommercialDomainMsg3 = new SimpleStringProperty();
        public SimpleStringProperty maskingCommercialDomainMsg4 = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicDomain = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicDomainMsg1 = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicDomainMsg2 = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicDomainMsg3 = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicDomainMsg4 = new SimpleStringProperty();
        public SimpleStringProperty maskingRequestCommercialDomain = new SimpleStringProperty();
        public SimpleStringProperty maskingRequestCommercialDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty maskingRequestCommercialDomain2 = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicRequestDomain = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicRequestDomainMsg = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicRequestDomain2 = new SimpleStringProperty();
        public SimpleStringProperty maskingPublicRequestPurposeDomain = new SimpleStringProperty();

        public SimpleStringProperty tokenAddEditTitle = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditSubTitle = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditTokenList = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditContractList = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditEdit = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditDelete = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditSelect = new SimpleStringProperty();
        public SimpleStringProperty tokenAddEditAddToken = new SimpleStringProperty();

        public SimpleStringProperty tokenEditEditTokenTitle = new SimpleStringProperty();
        public SimpleStringProperty tokenEditEditTokenDesc = new SimpleStringProperty();
        public SimpleStringProperty tokenEditContractAddrLabel = new SimpleStringProperty();
        public SimpleStringProperty tokenEditNameLabel = new SimpleStringProperty();
        public SimpleStringProperty tokenEditNamePlaceholder = new SimpleStringProperty();
        public SimpleStringProperty tokenEditMinNumLabel = new SimpleStringProperty();
        public SimpleStringProperty tokenEditPreviewLabel = new SimpleStringProperty();
        public SimpleStringProperty tokenEditNoBtn = new SimpleStringProperty();
        public SimpleStringProperty tokenEditEditBtn = new SimpleStringProperty();

        public SimpleStringProperty tokenAddAddTokenTitle = new SimpleStringProperty();
        public SimpleStringProperty tokenAddAddTokenDesc = new SimpleStringProperty();
        public SimpleStringProperty tokenAddAddBtn = new SimpleStringProperty();

        public SimpleStringProperty copyPkTitle = new SimpleStringProperty();
        public SimpleStringProperty copyPkSubTitle = new SimpleStringProperty();
        public SimpleStringProperty copyWalletAddressTitle = new SimpleStringProperty();
        public SimpleStringProperty copyWalletAddressSubTitle = new SimpleStringProperty();
        public SimpleStringProperty copyTxHashTitle = new SimpleStringProperty();
        public SimpleStringProperty copyTxHashSubTitle = new SimpleStringProperty();

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

            backupWalletTitle.set(StringManager.this.getString("popup_backup_wallet_title", "Backup Wallet"));
            backupWalletDownload.set(StringManager.this.getString("popup_backup_wallet_download_keystore", "Download the keystore file (wallet backup file)"));
            backupWalletPrivateKey.set(StringManager.this.getString("popup_backup_wallet_privatekey", "Private key"));
            backupWalletFooterComment.set(StringManager.this.getString("popup_backup_wallet_footer_comment", "You can load your wallet using the Keystore file or your private key. Please backup your Keystore file or private key."));

            backupWalletPasswordTitle.set(StringManager.this.getString("popup_backup_wallet_password_title", "Backup Wallet"));
            backupWalletPasswordSubTitle.set(StringManager.this.getString("popup_backup_wallet_password_sub_title", "Write down your wallet password."));
            backupWalletPasswordPassword.set(StringManager.this.getString("popup_backup_wallet_password_password", "Wallet Password"));
            backupWalletPasswordYes.set(StringManager.this.getString("popup_backup_wallet_password_yes", "Yes"));

            removeWalletTitle.set(StringManager.this.getString("popup_remove_wallet_title", "Remove Wallet!"));
            removeWalletSubTitle.set(StringManager.this.getString("popup_remove_wallet_sub_title", "Are you sure you want to remove your wallet?"));
            removeWalletNo.set(StringManager.this.getString("popup_remove_wallet_no", "No"));
            removeWalletYes.set(StringManager.this.getString("popup_remove_wallet_yes", "Yes"));

            removeWalletPasswordTitle.set(StringManager.this.getString("popup_remove_wallet_password_title", "Remove Wallet"));
            removeWalletPasswordSubTitle.set(StringManager.this.getString("popup_remove_wallet_password_sub_title", "Write down your wallet password."));
            removeWalletPasswordPassword.set(StringManager.this.getString("popup_remove_wallet_password_password", "Wallet Password"));
            removeWalletPasswordYes.set(StringManager.this.getString("popup_remove_wallet_password_yes", "Yes"));

            miningWalletConfirmTitle.set(StringManager.this.getString("popup_mining_wallet_confirm_title", "Confirm Password"));
            miningWalletConfirmSubTitle.set(StringManager.this.getString("popup_mining_wallet_confirm_sub_title", "Write down your wallet password."));
            miningWaleltConfirmAddress.set(StringManager.this.getString("popup_mining_wallet_confirm_address", "Mining Wallet Address"));
            miningWalletConfirmAddressComment.set(StringManager.this.getString("popup_mining_wallet_confirm_address_comment", "The address is unregisterd."));
            miningWalletConfirmPassword.set(StringManager.this.getString("popup_mining_wallet_confirm_password", "Password"));
            miningWalletConfirmStart.set(StringManager.this.getString("popup_mining_wallet_confirm_start", "Strart Mining"));
            miningWalletConfirmStop.set(StringManager.this.getString("popup_mining_wallet_confirm_stop", "Stop Mining"));
            miningWalletTitle.set(StringManager.this.getString("popup_mining_wallet_title", "Mining Wallet"));
            miningWalletSubTitle.set(StringManager.this.getString("popup_mining_wallet_sub_title", "You can choose only 1 wallet"));
            miningWalletAddress.set(StringManager.this.getString("popup_mining_wallet_address", "Address"));
            miningWalletAddressComment.set(StringManager.this.getString("popup_mining_wallet_address_comment", "The address is unregisterd."));
            miningWalletSelect.set(StringManager.this.getString("popup_mining_wallet_select", "Select"));

            masternodeTitle.set(StringManager.this.getString("popup_masternode_title", "Masternode"));
            masternodeWalletAddrLabel.set(StringManager.this.getString("popup_masternode_wallet_addr_label", "Masternode Wallet Address"));
            masternodePasswordLabel.set(StringManager.this.getString("popup_masternode_password_label", "Password"));
            masternodeRecipientLabel.set(StringManager.this.getString("popup_masternode_recipient_label", "Recipient"));
            masternodeDirectInput.set(StringManager.this.getString("popup_masternode_direct_input", "direct input"));
            masternodeRecipientPlaceholder.set(StringManager.this.getString("popup_masternode_recipient_placeholder", "please enter your address"));
            masternodeRecipientDesc1.set(StringManager.this.getString("popup_masternode_recipient_desc_1", "To be a masternode,"));
            masternodeRecipientDesc2.set(StringManager.this.getString("popup_masternode_recipient_desc_2", "please set your balance to exact 50,000, 200,000, 500,000 APIS."));
            masternodeStartMasternode.set(StringManager.this.getString("popup_masternode_start_masternode", "Start Masternode"));

            successTitle.set(StringManager.this.getString("popup_success_title", "Success!"));
            successSubTitle.set(StringManager.this.getString("popup_success_sub_title", "Your request has been received successfully."));
            successYes.set(StringManager.this.getString("popup_success_yes", "Yes"));
            failTitle.set(StringManager.this.getString("popup_fail_title", "[EN]실패!!!"));
            failSubTitle.set(StringManager.this.getString("popup_fail_sub_title", "[EN]아래와 같은 이유로 트랜잭션 전송에 실패하였습니다."));

            maskingTitle.set(StringManager.this.getString("popup_masking_title", "Address Masking"));
            maskingTabRegisterMask.set(StringManager.this.getString("popup_masking_tab_register_mask", "Register Masking"));
            maskingTabRegisterDomain.set(StringManager.this.getString("popup_masking_tab_register_domain", "Register Domain"));
            maskingAddress.set(StringManager.this.getString("popup_masking_address", "Address"));
            maskingDomain.set(StringManager.this.getString("popup_masking_domain", "Domain"));
            maskingId.set(StringManager.this.getString("popup_masking_id", "ID"));
            maskingPay.set(StringManager.this.getString("popup_masking_pay", "PAY"));
            maskingAliasPlaseCheckAddress.set(StringManager.this.getString("popup_masking_alias_please_checkaddress", "Please check if the address is registered."));
            maskingAliasPlaseSelectDomain.set(StringManager.this.getString("popup_masking_alias_please_selectdomain", "Please select a domain."));
            maskingAliasAddressMsg.set(StringManager.this.getString("popup_masking_alias_address_msg", "This address is available"));
            maskingAliasAddressMsg2.set(StringManager.this.getString("popup_masking_alias_address_msg_2", "This address is already in use"));
            maskingAliasDomainMsg.set(StringManager.this.getString("popup_masking_alias_domain_msg", "This address is available"));
            maskingAliasPlaseInputId.set(StringManager.this.getString("popup_masking_alias_please_inputid", "Please input a ID"));
            maskingSuccess.set(StringManager.this.getString("popup_masking_success", "SUCCESS!"));
            maskingWalletAddress.set(StringManager.this.getString("popup_masking_wallet_address", "Wallet Address"));
            maskingAlias.set(StringManager.this.getString("popup_masking_alias", "Alias"));
            maskingTotalFee.set(StringManager.this.getString("popup_masking_total_fee", "Total Fee"));
            maskingPayer.set(StringManager.this.getString("popup_masking_payer", "Payer"));
            maskingPayMsg1.set(StringManager.this.getString("popup_masking_pay_msg1", "It may take one or more minutes"));
            maskingPayMsg2.set(StringManager.this.getString("popup_masking_pay_msg2", "for the alias to be registered."));
            maskingRegisterDomainMsg.set(StringManager.this.getString("popup_masking_register_domain_msg", "You can request a public domain registration or register a commercial domain."));
            maskingCommercialDomain.set(StringManager.this.getString("popup_masking_commercial_domain", "Commercial domain"));
            maskingCommercialDomainMsg1.set(StringManager.this.getString("popup_masking_commercial_domain_msg1", "Commercial domains can only be registered by the administrator's approval."));
            maskingCommercialDomainMsg2.set(StringManager.this.getString("popup_masking_commercial_domain_msg2", "In order to register a commercial domain"));
            maskingCommercialDomainMsg3.set(StringManager.this.getString("popup_masking_commercial_domain_msg3", "you need to prove ownership of the business."));
            maskingCommercialDomainMsg4.set(StringManager.this.getString("popup_masking_commercial_domain_msg4", "There is a fee for register a commercial domain."));
            maskingPublicDomain.set(StringManager.this.getString("popup_masking_public_domain", "Public domain"));
            maskingPublicDomainMsg1.set(StringManager.this.getString("popup_masking_public_domain_msg1", "Public domain is available to anyone."));
            maskingPublicDomainMsg2.set(StringManager.this.getString("popup_masking_public_domain_msg2", "The proposed public domain is registered through voting by the masternodes."));
            maskingPublicDomainMsg3.set(StringManager.this.getString("popup_masking_public_domain_msg3", "There is a fee proposing a public domain, and a fee will be refunded "));
            maskingPublicDomainMsg4.set(StringManager.this.getString("popup_masking_public_domain_msg4", "if you register as a domain."));
            maskingRequestCommercialDomain.set(StringManager.this.getString("popup_masking_request_commercial_domain", "Request  a commercial domain"));
            maskingRequestCommercialDomainMsg.set(StringManager.this.getString("popup_masking_request_commercial_domain_msg", "Please check if the domain is registered."));
            maskingRequestCommercialDomain2.set(StringManager.this.getString("popup_masking_request_commercial_domain2", "commercial domain"));
            maskingPublicRequestDomain.set(StringManager.this.getString("popup_masking_public_request_domain", "Public domain Request"));
            maskingPublicRequestDomainMsg.set(StringManager.this.getString("popup_masking_public_request_domain_msg", "Please check if the domain is registered."));
            maskingPublicRequestDomain2.set(StringManager.this.getString("popup_masking_public_request_domain2", "Public domain"));
            maskingPublicRequestPurposeDomain.set(StringManager.this.getString("popup_masking_public_request_purpose_domain", "Purpose of this requested domain"));

            tokenAddEditTitle.set(StringManager.this.getString("popup_token_add_edit_title", "Token Add / Edit"));
            tokenAddEditSubTitle.set(StringManager.this.getString("popup_token_add_edit_sub_title", "you must register the addresses of the tokens in this list."));
            tokenAddEditTokenList.set(StringManager.this.getString("popup_token_add_edit_token_list", "Token list"));
            tokenAddEditContractList.set(StringManager.this.getString("popup_token_add_edit_contract_list", "Contract list"));
            tokenAddEditEdit.set(StringManager.this.getString("popup_token_add_edit_edit", "Edit"));
            tokenAddEditDelete.set(StringManager.this.getString("popup_token_add_edit_delete", "Delete"));
            tokenAddEditSelect.set(StringManager.this.getString("popup_token_add_edit_Select", "Select"));
            tokenAddEditAddToken.set(StringManager.this.getString("popup_token_add_edit_add_token", "ADD Token"));

            tokenEditEditTokenTitle.set(StringManager.this.getString("popup_token_edit_edit_token_title", "Edit Token"));
            tokenEditEditTokenDesc.set(StringManager.this.getString("popup_token_edit_edit_token_desc", "Edit Token"));
            tokenEditContractAddrLabel.set(StringManager.this.getString("popup_token_edit_contract_addr_label", "Token Contract Address"));
            tokenEditNameLabel.set(StringManager.this.getString("popup_token_edit_name_label", "Token Name"));
            tokenEditNamePlaceholder.set(StringManager.this.getString("popup_token_edit_name_placeholder", "Token"));
            tokenEditMinNumLabel.set(StringManager.this.getString("popup_token_edit_min_num_label", "Minimum number of decimal places"));
            tokenEditPreviewLabel.set(StringManager.this.getString("popup_token_edit_preview_label", "Preview"));
            tokenEditNoBtn.set(StringManager.this.getString("popup_token_edit_no_btn", "No"));
            tokenEditEditBtn.set(StringManager.this.getString("popup_token_edit_edit_btn", "Edit"));

            tokenAddAddTokenTitle.set(StringManager.this.getString("popup_token_add_add_token_title", "ADD Token"));
            tokenAddAddTokenDesc.set(StringManager.this.getString("popup_token_add_add_token_desc", "ADD Token"));
            tokenAddAddBtn.set(StringManager.this.getString("popup_token_add_add_btn", "Add"));

            copyPkTitle.set(StringManager.this.getString("popup_copy_pk_title", "COPY Private Key!"));
            copyPkSubTitle.set(StringManager.this.getString("popup_copy_pk_sub_title", "You can paste that APIS PRIVATE KEY"));
            copyWalletAddressTitle.set(StringManager.this.getString("popup_copy_wallet_address_title", "COPY Wallet Address!"));
            copyWalletAddressSubTitle.set(StringManager.this.getString("popup_copy_wallet_address_sub_title", "You can paste that APIS ADDRESS"));
            copyTxHashTitle.set(StringManager.this.getString("popup_copy_tx_hash_title", "COPY Transaction Hash!"));
            copyTxHashSubTitle.set(StringManager.this.getString("popup_copy_tx_hash_sub_title", "You can paste that TRANSACTION HASH"));
        }
    }

    public class Setting implements StringManagerImpl {
        public SimpleStringProperty settingsTitle = new SimpleStringProperty();
        public SimpleStringProperty settingsDesc = new SimpleStringProperty();
        public SimpleStringProperty userNumTitle = new SimpleStringProperty();
        public SimpleStringProperty userNumDesc = new SimpleStringProperty();
        public SimpleStringProperty rpcTitle = new SimpleStringProperty();
        public SimpleStringProperty rpcPortLabel = new SimpleStringProperty();
        public SimpleStringProperty rpcWhiteListLabel = new SimpleStringProperty();
        public SimpleStringProperty rpcIdLabel = new SimpleStringProperty();
        public SimpleStringProperty rpcPwLabel = new SimpleStringProperty();
        public SimpleStringProperty generalTitle = new SimpleStringProperty();
        public SimpleStringProperty startWalletWithLogInLabel = new SimpleStringProperty();
        public SimpleStringProperty enableLogEventLabel = new SimpleStringProperty();
        public SimpleStringProperty windowTitle = new SimpleStringProperty();
        public SimpleStringProperty minimizeToTrayLabel = new SimpleStringProperty();
        public SimpleStringProperty cancelBtn = new SimpleStringProperty();
        public SimpleStringProperty saveBtn = new SimpleStringProperty();
        public SimpleStringProperty rewardSoundLabel = new SimpleStringProperty();

        @Override
        public void update() {
            settingsTitle.set(StringManager.this.getString("setting_settings_title", "Settings"));
            settingsDesc.set(StringManager.this.getString("setting_settings_desc", "You can make APIS PC wallet even more easier"));
            userNumTitle.set(StringManager.this.getString("setting_user_num_title", "Limited number of users"));
            userNumDesc.set(StringManager.this.getString("setting_user_num_desc", "You can set up to 5 people."));
            rpcTitle.set(StringManager.this.getString("setting_rpc_title", "RPC"));
            rpcPortLabel.set(StringManager.this.getString("setting_rpc_port_label", "Port"));
            rpcWhiteListLabel.set(StringManager.this.getString("setting_rpc_white_list_label", "White List"));
            rpcIdLabel.set(StringManager.this.getString("setting_rpc_id_label", "ID"));
            rpcPwLabel.set(StringManager.this.getString("setting_rpc_pw_label", "Password"));
            generalTitle.set(StringManager.this.getString("setting_general_title", "General"));
            startWalletWithLogInLabel.set(StringManager.this.getString("setting_start_wallet_with_log_in_label", "Start APIS Wallet with system log in"));
            enableLogEventLabel.set(StringManager.this.getString("setting_enable_log_event_label", "Enable log event"));
            windowTitle.set(StringManager.this.getString("setting_window_title", "Window"));
            minimizeToTrayLabel.set(StringManager.this.getString("setting_minimize_to_tray_label", "Minimize to tray, no taskbar"));
            cancelBtn.set(StringManager.this.getString("setting_cancel_btn", "Cancel"));
            saveBtn.set(StringManager.this.getString("setting_save_btn", "Save"));
            rewardSoundLabel.set(StringManager.this.getString("setting_reward_sound_on_off", "Reward Sound On/Off"));
        }
    }

    public class ContractPopup implements StringManagerImpl {
        public SimpleStringProperty readWriteTitle = new SimpleStringProperty();
        public SimpleStringProperty readWriteCreate = new SimpleStringProperty();
        public SimpleStringProperty addrLabel = new SimpleStringProperty();
        public SimpleStringProperty nameLabel = new SimpleStringProperty();
        public SimpleStringProperty namePlaceholder = new SimpleStringProperty();
        public SimpleStringProperty jsonInterfaceLabel = new SimpleStringProperty();
        public SimpleStringProperty noBtn = new SimpleStringProperty();
        public SimpleStringProperty createBtn = new SimpleStringProperty();
        public SimpleStringProperty readWriteSelect = new SimpleStringProperty();
        public SimpleStringProperty newLabel = new SimpleStringProperty();
        public SimpleStringProperty listLabel = new SimpleStringProperty();
        public SimpleStringProperty editLabel = new SimpleStringProperty();
        public SimpleStringProperty deleteLabel = new SimpleStringProperty();
        public SimpleStringProperty selectLabel = new SimpleStringProperty();
        public SimpleStringProperty yesBtn = new SimpleStringProperty();
        public SimpleStringProperty readWriteModify = new SimpleStringProperty();
        public SimpleStringProperty modifyBtn = new SimpleStringProperty();
        public SimpleStringProperty warningTitle = new SimpleStringProperty();
        public SimpleStringProperty warningDesc = new SimpleStringProperty();
        public SimpleStringProperty amountToSendLabel = new SimpleStringProperty();
        public SimpleStringProperty gasLimitLabel = new SimpleStringProperty();
        public SimpleStringProperty generateTxBtn = new SimpleStringProperty();
        public SimpleStringProperty rawTxLabel = new SimpleStringProperty();
        public SimpleStringProperty signedTxLabel = new SimpleStringProperty();
        public SimpleStringProperty walletPasswordLabel = new SimpleStringProperty();

        @Override
        public void update() {
            readWriteTitle.set(StringManager.this.getString("contract_popup_read_write_title", "Contract Read / Write"));
            readWriteCreate.set(StringManager.this.getString("contract_popup_read_write_create", "Create Smart contract"));
            addrLabel.set(StringManager.this.getString("contract_popup_addr_label", "Contract Address"));
            nameLabel.set(StringManager.this.getString("contract_popup_name_label", "Contract Name"));
            namePlaceholder.set(StringManager.this.getString("contract_popup_name_placeholder", "Contract Name"));
            jsonInterfaceLabel.set(StringManager.this.getString("contract_popup_json_interface_label", "JSON Interface"));
            noBtn.set(StringManager.this.getString("contract_popup_no_btn", "No"));
            createBtn.set(StringManager.this.getString("contract_popup_create_btn", " Create"));
            readWriteSelect.set(StringManager.this.getString("contract_popup_read_write_select", "Select Existing Contract Address"));
            newLabel.set(StringManager.this.getString("contract_popup_new_label", "New Contract"));
            listLabel.set(StringManager.this.getString("contract_popup_list_label", "Contract list"));
            editLabel.set(StringManager.this.getString("contract_popup_edit_label", "Edit"));
            deleteLabel.set(StringManager.this.getString("contract_popup_delete_label", "Delete"));
            selectLabel.set(StringManager.this.getString("contract_popup_select_label", "Select"));
            yesBtn.set(StringManager.this.getString("contract_popup_yes_btn", "YES"));
            readWriteModify.set(StringManager.this.getString("contract_popup_read_write_modify", "Edit smart contract"));
            modifyBtn.set(StringManager.this.getString("contract_popup_modify_btn", "Modify"));
            warningTitle.set(StringManager.this.getString("contract_popup_warning_title", "Warning!"));
            warningDesc.set(StringManager.this.getString("contract_popup_warning_desc", "You are about to execute a function on contract."));
            amountToSendLabel.set(StringManager.this.getString("contract_popup_amount_to_send_label", "Amount to Send In most cases you should leave this as 0."));
            gasLimitLabel.set(StringManager.this.getString("contract_popup_gas_limit_label", "Gas Limit"));
            generateTxBtn.set(StringManager.this.getString("contract_popup_generate_tx_btn", "Generate Transaction"));
            rawTxLabel.set(StringManager.this.getString("contract_popup_raw_tx_label", "Raw Transaction"));
            signedTxLabel.set(StringManager.this.getString("contract_popup_signed_tx_label", "Signed Transaction"));
            walletPasswordLabel.set(StringManager.this.getString("contract_popup_wallet_password_label", "Wallet Password"));


        }
    }

    public class MyAddress implements StringManagerImpl {
        public SimpleStringProperty title = new SimpleStringProperty();
        public SimpleStringProperty subTitle = new SimpleStringProperty();
        public SimpleStringProperty topAddBtn = new SimpleStringProperty();
        public SimpleStringProperty myAddressList = new SimpleStringProperty();
        public SimpleStringProperty edit = new SimpleStringProperty();
        public SimpleStringProperty delete = new SimpleStringProperty();
        public SimpleStringProperty select = new SimpleStringProperty();
        public SimpleStringProperty searchPlaceHolder = new SimpleStringProperty();

        public SimpleStringProperty editTitle = new SimpleStringProperty();
        public SimpleStringProperty editSubTitle = new SimpleStringProperty();
        public SimpleStringProperty editWalletAddress = new SimpleStringProperty();
        public SimpleStringProperty editWalletName = new SimpleStringProperty();
        public SimpleStringProperty editGroup = new SimpleStringProperty();

        public SimpleStringProperty registerTitle = new SimpleStringProperty();
        public SimpleStringProperty registerSubTitle = new SimpleStringProperty();
        public SimpleStringProperty registerWalletAddress = new SimpleStringProperty();
        public SimpleStringProperty registerWalletName = new SimpleStringProperty();
        public SimpleStringProperty registerGroup = new SimpleStringProperty();

        public SimpleStringProperty addGroup = new SimpleStringProperty();

        public SimpleStringProperty addGroupTitle = new SimpleStringProperty();
        public SimpleStringProperty addGroupSubTitle = new SimpleStringProperty();

        @Override
        public void update() {
            title.set(StringManager.this.getString("myaddress_popup_title", "My Address"));
            subTitle.set(StringManager.this.getString("myaddress_popup_sub_title", "select your address"));
            topAddBtn.set(StringManager.this.getString("myaddress_popup_add_button", "My Address"));
            myAddressList.set(StringManager.this.getString("myaddress_popup_list_title", "My address list"));
            edit.set(StringManager.this.getString("myaddress_popup_edit", "Edit"));
            delete.set(StringManager.this.getString("myaddress_popup_delete", "Delete"));
            select.set(StringManager.this.getString("myaddress_popup_select", "Select"));
            searchPlaceHolder.set(StringManager.this.getString("myaddresS_popup_search_placeholder", "Search by Wallet name / address masking / wallet address"));

            editTitle.set(StringManager.this.getString("myaddress_popup_edit_title", "Edit"));
            editSubTitle.set(StringManager.this.getString("myaddress_popup_edit_sub_title", "Please adjust the registered form"));
            editWalletAddress.set(StringManager.this.getString("myaddress_popup_edit_wallet_address", "Wallet Address"));
            editWalletName.set(StringManager.this.getString("myaddress_popup_edit_wallet_name", "Wallet Name"));
            editGroup.set(StringManager.this.getString("myaddress_popup_edit_group", "Group"));

            registerTitle.set(StringManager.this.getString("myaddress_popup_register_title", "Register"));
            registerSubTitle.set(StringManager.this.getString("myaddress_popup_register_sub_title", "Please adjust the registered form"));
            registerWalletAddress.set(StringManager.this.getString("myaddress_popup_register_wallet_address", "Wallet Address"));
            registerWalletName.set(StringManager.this.getString("myaddress_popup_register_wallet_name", "Wallet Name"));
            registerGroup.set(StringManager.this.getString("myaddress_popup_register_group", "Group"));

            addGroup.set(StringManager.this.getString("myaddress_popup_add_group", "Add Group"));

            addGroupTitle.set(StringManager.this.getString("myaddress_popup_add_group_title", "Add Group"));
            addGroupSubTitle.set(StringManager.this.getString("myaddress_popup_add_group_sub_title", "please enter the group name"));

        }
    }

    public class RecentAddress implements StringManagerImpl {
        public SimpleStringProperty title = new SimpleStringProperty();
        public SimpleStringProperty subTitle = new SimpleStringProperty();
        public SimpleStringProperty address = new SimpleStringProperty();
        public SimpleStringProperty address2 = new SimpleStringProperty();
        public SimpleStringProperty list = new SimpleStringProperty();
        public SimpleStringProperty time = new SimpleStringProperty();
        public SimpleStringProperty select = new SimpleStringProperty();

        @Override
        public void update() {
            title.set(StringManager.this.getString("recentaddress_popup_title", "Recent Address"));
            subTitle.set(StringManager.this.getString("recentaddress_popup_sub_title", "select your recent transfer address"));
            address.set(StringManager.this.getString("recentaddress_popup_address", "Recent Address"));
            address2.set(StringManager.this.getString("recentaddress_popup_address2", "Up to 10 of the addresses sent in the last 3 months are retrieved."));
            list.set(StringManager.this.getString("recentaddress_popup_list", "Recent list"));
            time.set(StringManager.this.getString("recentaddress_popup_time", "Time"));
            select.set(StringManager.this.getString("recentaddress_popup_select", "Select"));
        }
    }

    public class AddressInfo implements StringManagerImpl {
        public SimpleStringProperty title = new SimpleStringProperty();
        public SimpleStringProperty subTitle = new SimpleStringProperty();
        public SimpleStringProperty noResultTitle = new SimpleStringProperty();
        public SimpleStringProperty noResultSubTitle = new SimpleStringProperty();
        public SimpleStringProperty searchPlaceHolder = new SimpleStringProperty();

        @Override
        public void update() {
            title.set(StringManager.this.getString("addressinfo_title", "Address Info"));
            subTitle.set(StringManager.this.getString("addressinfo_sub_title", "Here are the results of your search"));
            noResultTitle.set(StringManager.this.getString("addressinfo_no_result_title", "There is no result"));
            noResultSubTitle.set(StringManager.this.getString("addressinfo_no_result_sub_title", "Please search by wallet address"));
            searchPlaceHolder.set(StringManager.this.getString("addressinfo_placeholder", "Search by wallet address"));
        }
    }
}