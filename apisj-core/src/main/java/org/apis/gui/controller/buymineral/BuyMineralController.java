package org.apis.gui.controller.buymineral;

import javafx.fxml.FXML;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class BuyMineralController extends BasePopupController {

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_BUY_MINERAL);
    private byte[] buyMineralAddress =  AppManager.getInstance().constants.getBUY_MINERAL();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionBuyMNR = contract.getByName("buyMNR");
    private CallTransaction.Function functionCalcMNR = contract.getByName("calcMNR");

    @FXML private BuyMineralBodyController bodyController;
    @FXML private BuyMineralReceiptController receiptController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bodyController.setHandelr(new BuyMineralBodyController.BuyMineralBodyImpl() {
            @Override
            public void settingLayoutData() {
                BuyMineralController.this.settingLayoutData();
            }
        });

        receiptController.setHandler(new BuyMineralReceiptController.BuyMineralReceiptImpl() {
            @Override
            public void transfer() {
                String beneficiaryAddress = bodyController.getBeneficiaryAddress();
                String fromAddress = bodyController.getPayerAddress();
                BigInteger value = bodyController.getValue();
                BigInteger gasPrice = bodyController.getGasPrice();
                BigInteger gasLimit = bodyController.getGasLimit();

                System.out.println("beneficiaryAddress : "+beneficiaryAddress);
                System.out.println("fromAddress : "+fromAddress);
                System.out.println("value : "+value);
                System.out.println("gasPrice : "+gasPrice);
                System.out.println("gasLimit : "+gasLimit);

                Object[] args = new Object[1];
                args[0] = Hex.decode(beneficiaryAddress);
                byte[] functionCallBytes = functionBuyMNR.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup("popup_contract_warning.fxml", 0);
                controller.setData(fromAddress, value.toString(), gasPrice.toString(), gasLimit.toString(), buyMineralAddress, functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });
    }

    public void settingLayoutData(){
        String beneficiary = bodyController.getBeneficiaryAddress();
        String mask = bodyController.getMask();
        String totalFee = bodyController.getTotalFee();
        String payerAddress = bodyController.getPayerAddress();
        receiptController.setAddress(beneficiary);
        receiptController.setMask(mask);
        receiptController.setTotalFee(totalFee);
        receiptController.setPayerAddress(payerAddress);
    }
}
