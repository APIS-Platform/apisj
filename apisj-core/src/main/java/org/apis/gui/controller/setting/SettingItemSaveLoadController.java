package org.apis.gui.controller.setting;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.apis.db.sql.*;
import org.apis.gui.controller.popup.PopupSuccessController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.util.ByteUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class SettingItemSaveLoadController implements Initializable {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label contents, saveBtn, loadBtn;
    private PrivateDBRecord privateDBRecord;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
        buttonHover();

        // Save back-up json file
        saveBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String path = AppManager.getInstance().openDirectoryReader();
                if(path != null) {
                    privateDBRecord = new PrivateDBRecord();

                    List<AbiRecord> abiList = DBManager.getInstance().selectAbis();
                    List<AddressGroupRecord> addressGroupList = DBManager.getInstance().selectAddressGroups();
                    List<ConnectAddressGroupRecord> connectAddressGroupList = DBManager.getInstance().selectConnectAddressGroups();
                    List<ContractRecord> contractList = DBManager.getInstance().selectContracts();
                    List<LedgerRecord> ledgerList = DBManager.getInstance().selectLedgers();
                    List<MyAddressRecord> myAddressList = DBManager.getInstance().selectMyAddress();
                    List<RecentAddressRecord> recentAddressList = DBManager.getInstance().selectRecentAddress();
                    List<TokenRecord> tokenList = DBManager.getInstance().selectTokens();

                    privateDBRecord.setAbiList(abiList);
                    privateDBRecord.setAddressGroupList(addressGroupList);
                    privateDBRecord.setConnectAddressGroupList(connectAddressGroupList);
                    privateDBRecord.setContractList(contractList);
                    privateDBRecord.setLedgerList(ledgerList);
                    privateDBRecord.setMyAddressList(myAddressList);
                    privateDBRecord.setRecentAddressList(recentAddressList);
                    privateDBRecord.setTokenList(tokenList);

                    // list를 json 형식으로 변형
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        // 파일을 저장한다.
                        Date now = new Date();
                        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
                        mapper.writeValue(new File(path + "/" + "apis-core_contact_" + date.format(now) + ".bak"), privateDBRecord);

                        PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(bgAnchor, "popup_success.fxml", 2);
                        controller.setSubTitle(StringManager.getInstance().popup.successSubTitleFile);
                        controller.requestFocusYesButton();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Load back-up json file
        loadBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                FileChooser chooser = new FileChooser();
                File selectedFile = chooser.showOpenDialog(AppManager.getInstance().guiFx.getPrimaryStage());
                privateDBRecord = new PrivateDBRecord();

                // Validate json file form
                if(selectedFile != null
                    && selectedFile.exists()
                    && selectedFile.length() <= 50*1024*1024) {

                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), "UTF8"));
                        StringBuffer stringBuffer = new StringBuffer();
                        String currentLine, allText;

                        while((currentLine = br.readLine()) != null) {
                            stringBuffer.append(currentLine.trim());
                        }
                        br.close();

                        allText = new String(stringBuffer);

                        if(allText.length() > 0) {
                            ObjectMapper mapper = new ObjectMapper();
                            JSONParser parser = new JSONParser();
                            JSONObject jsonObj = (JSONObject) parser.parse(allText);
                            String jsonStr = mapper.writeValueAsString(jsonObj);

                            // Remove garbage value
                            int error = jsonStr.indexOf("{");
                            jsonStr = jsonStr.substring(error, jsonStr.length());

                            // Convert json file to record class
                            privateDBRecord = mapper.readValue(jsonStr, PrivateDBRecord.class);

                            List<AbiRecord> abiList = privateDBRecord.getAbiList();
                            List<AddressGroupRecord> addressGroupList = privateDBRecord.getAddressGroupList();
                            List<ConnectAddressGroupRecord> connectAddressGroupList = privateDBRecord.getConnectAddressGroupList();
                            List<ContractRecord> contractList = privateDBRecord.getContractList();
                            List<LedgerRecord> ledgerList = privateDBRecord.getLedgerList();
                            List<MyAddressRecord> myAddressList = privateDBRecord.getMyAddressList();
                            List<RecentAddressRecord> recentAddressList = privateDBRecord.getRecentAddressList();
                            List<TokenRecord> tokenList = privateDBRecord.getTokenList();

                            // Insert or Update DB tables with imported file
                            for(AbiRecord abi : abiList) {
                                DBManager.getInstance().updateAbi(abi.getCreator(), abi.getContractAddress(), abi.getAbi(), abi.getContractName());
                            }
                            for(AddressGroupRecord addrGrp : addressGroupList) {
                                DBManager.getInstance().updateAddressGroup(addrGrp.getGroupName());
                            }
                            for(ConnectAddressGroupRecord cntAddrGrp : connectAddressGroupList) {
                                DBManager.getInstance().updateConnectAddressGroup(cntAddrGrp.getAddress(), cntAddrGrp.getGroupName());
                            }
                            for(ContractRecord contract : contractList) {
                                DBManager.getInstance().updateContract(contract.getAddress(), contract.getTitle(), contract.getMask(), contract.getAbi(), contract.getCanvas_url());
                            }
                            for(LedgerRecord ledger : ledgerList) {
                                DBManager.getInstance().updateLedgers(ledger.getAddress(), ledger.getPath(), ledger.getAlias());
                            }
                            for(MyAddressRecord myAddr : myAddressList) {
                                DBManager.getInstance().updateMyAddress(myAddr.getAddress(), myAddr.getAlias(), myAddr.getExist());
                            }
                            for(RecentAddressRecord recentAddr : recentAddressList) {
                                DBManager.getInstance().updateRecentAddress(recentAddr.getTxHash(), recentAddr.getAddress(), recentAddr.getAlias());
                            }
                            for(TokenRecord token : tokenList) {
                                DBManager.getInstance().updateTokens(token.getTokenAddress(), token.getTokenName(), token.getTokenSymbol(), token.getDecimal(), token.getTotalSupply());
                            }

                            PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(bgAnchor, "popup_success.fxml", 2);
                            controller.setSubTitle(StringManager.getInstance().popup.successSubTitleLoad);
                            controller.requestFocusYesButton();
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        failedPopup();
                    } catch (IOException e) {
                        e.printStackTrace();
                        failedPopup();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        failedPopup();
                    } catch (Exception e) {
                        e.printStackTrace();
                        failedPopup();
                    }
                } else {
                    if(selectedFile != null) {
                        failedPopup();
                    }
                }
            }
        });
    }

    private void failedPopup() {
        PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(bgAnchor, "popup_success.fxml", 2);
        controller.setTitle(StringManager.getInstance().popup.failTitle);
        controller.setSubTitle(StringManager.getInstance().popup.failSubTitleLoad);
        controller.setTitleColor(StyleManager.AColor.Cb01e1e);
        controller.requestFocusYesButton();
    }

    private void buttonHover() {
        saveBtn.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StyleManager.backgroundColorStyle(saveBtn, StyleManager.AColor.C910000);
            }
        });
        saveBtn.setOnMouseExited(event -> {
            StyleManager.backgroundColorStyle(saveBtn, StyleManager.AColor.Cb01e1e);
        });

        loadBtn.setOnMouseEntered(event -> {
            StyleManager.backgroundColorStyle(loadBtn, StyleManager.AColor.Cb01e1e);
            StyleManager.fontColorStyle(loadBtn, StyleManager.AColor.Cffffff);
        });
        loadBtn.setOnMouseExited(event -> {
            StyleManager.backgroundColorStyle(loadBtn, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(loadBtn, StyleManager.AColor.Cb01e1e);
        });
    }

    private void languageSetting() {
        saveBtn.textProperty().bind(StringManager.getInstance().setting.saveBtn);
        loadBtn.textProperty().bind(StringManager.getInstance().setting.loadBtn);
    }

    public String getContents() {
        return this.contents.getText();
    }

    public void setContents(String contents) {
        this.contents.setText(contents);
    }
}
