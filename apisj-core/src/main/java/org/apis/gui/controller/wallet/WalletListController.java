package org.apis.gui.controller.wallet;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.TokenRecord;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.TokenModel;
import org.apis.gui.model.WalletItemModel;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WalletListController implements Initializable {

    @FXML private VBox listBox;

    private Sort sortType = Sort.DEFAULT_ASC;
    private ListType listType = ListType.WALLET;
    private List<WalletListGroupController> walletGroupCtrls = new ArrayList<>();
    private List<WalletListGroupController> tokenGroupCtrls = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void sort(Sort sortType) {
        this.sortType = sortType;
    }

    /**
     * 현재 설정한 상태로 지갑을 다시 보여준다.
     */
    public void refresh() {
        listBox.getChildren().clear();

        if(listType == ListType.WALLET){
            for(WalletListGroupController controller : walletGroupCtrls){
                controller.drawNode(listBox);
                controller.refresh();
            }
        }else if(listType == ListType.TOKEN){
            for(WalletListGroupController controller : tokenGroupCtrls){
                controller.drawNode(listBox);
                controller.refresh();
            }
        }
    }

    /**
     * Wallet List에 WalletListGroup 을 추가한다.
     * @param model
     */
    public void updateWallet(WalletItemModel model) {

        boolean isUpdate = false;

        if(this.listType == ListType.WALLET){
            for(WalletListGroupController controller : walletGroupCtrls){
                if(((WalletItemModel)controller.getModel()).getId().equals(model.getId())){
                    isUpdate = true;
                    updateGroup(model);
                    break;
                }
            }

            // 새로운 데이터일 경우 insert
            if(!isUpdate){
                addGroup(model);
            }
        }else if(this.listType == ListType.TOKEN){
            // 최대 갯수만큼 토큰 컨트롤러를 생성한다.
            List<TokenModel> tokens = AppManager.getInstance().getTokens();
            int count = tokens.size() - this.tokenGroupCtrls.size();
            for(int i = count; i>0; i--){
                addGroup(null);
            }

            // 생성된 토큰 컨트롤러가 더 많을 경우 컨트롤러 삭제
            count =  this.tokenGroupCtrls.size() - tokens.size();
            for(int i=count; i>0; i--){
                this.tokenGroupCtrls.remove(0);
            }

            // 토큰 컨트롤러에 데이터 넣기
            for(int i=0; i<tokenGroupCtrls.size(); i++){
                WalletItemModel tokenModel = model.getClone();
                tokenModel.setTokenAddress(tokens.get(i).getTokenAddress());
                tokenGroupCtrls.get(i).setModel(tokenModel);
            }
        }

    }

    /**
     * 지갑을 추가한다.
     * @param model
     */
    private void addGroup(WalletItemModel model){

        if(listType == ListType.WALLET){
            WalletListGroupController group = new WalletListGroupController(WalletListGroupController.GroupType.WALLET);
            group.setModel(model);
            walletGroupCtrls.add(group);

        }else if(listType == ListType.TOKEN){
            try {
                WalletListGroupController group = new WalletListGroupController(WalletListGroupController.GroupType.TOKEN);
                group.setModel(model);
                tokenGroupCtrls.add(group);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 지갑을 업데이트 한다.
     * @param model
     */
    private void updateGroup(WalletItemModel model){

        if(listType == ListType.WALLET){
            for(int i=0; i<walletGroupCtrls.size(); i++){
                if(model.getId().equals(((WalletItemModel)walletGroupCtrls.get(i).getModel()).getId())){
                    walletGroupCtrls.get(i).setModel(model);
                    break;
                }
            }

        }else if(listType == ListType.TOKEN){
            for(int i=0; i<tokenGroupCtrls.size(); i++){
                if(model.getId().equals(((WalletItemModel)tokenGroupCtrls.get(i).getModel()).getId())){
                    tokenGroupCtrls.get(i).setModel(model);
                    break;
                }
            }
        }
    }


    public void setListType(ListType listType){
        this.listType = listType;
    }

    public enum Sort {
        /* 사용자설정 오름차순 */DEFAULT_ASC(0),
        /* 사용자설정 내림차순 */DEFAULT_DESC(1),
        /* 지갑이름 오름차순 */ALIAS_ASC(2),
        /* 지갑이름 내림차순 */ALIAS_DESC(3),
        /* 지갑수량 오름차순 */VALUE_ASC(4),
        /* 지갑수량 내림차순 */VALUE_DESC(5);
        int num;
        Sort(int num) {
            this.num = num;
        }
    }
    public enum ListType {
        /* 지갑기준 */WALLET(0),
        /* 토큰기준 */TOKEN(1);
        int num;
        ListType(int num) {
            this.num = num;
        }
    }

    private WalletListImpl handler;
    public void setHandler(WalletListImpl handler){
        this.handler = handler;
    }
    public interface WalletListImpl{
        void onChangeCheck(WalletItemModel model, boolean isChecked);
        void onClickOpen(WalletItemModel model, int index, int listType);
        void onClickClose(WalletItemModel model, int index, int listType);
    }
}
