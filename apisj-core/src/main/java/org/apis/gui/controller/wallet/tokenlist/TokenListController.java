package org.apis.gui.controller.wallet.tokenlist;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.wallet.walletlist.WalletListGroupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.model.TokenModel;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.controller.wallet.WalletController.Sort;

import java.net.URL;
import java.util.*;

public class TokenListController extends BaseViewController {

    @FXML private VBox listBox;

    private Sort tokenSortType = Sort.DEFAULT_ASC;
    private List<TokenListGroupController> tokenGroupCtrls = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for(TokenModel token : AppManager.getInstance().getTokens()){
            TokenListGroupController controller = new TokenListGroupController(token.getTokenAddress());
            tokenGroupCtrls.add(controller);

            controller.setHeader(new TokenListGroupController.TokenListGroupImpl() {
                @Override
                public void onClickOpen(String tokenAddress) {
                    for (int i = 0; i < tokenGroupCtrls.size(); i++) {

                        if (tokenGroupCtrls.get(i).getTokenAddress().equals(tokenAddress)) {
                            tokenGroupCtrls.get(i).setVisibleItemList(true);
                        } else {
                            tokenGroupCtrls.get(i).setVisibleItemList(false);
                        }
                    }
                }

                @Override
                public void onClickClose(String tokenAddress) {
                    for (int i = 0; i < tokenGroupCtrls.size(); i++) {
                        tokenGroupCtrls.get(i).setVisibleItemList(false);
                    }
                }
            });

        }

    }

    /**
     * 현재 설정한 상태로 지갑을 다시 보여준다.
     */
    public void refresh() {
        listBox.getChildren().clear();

        for(TokenListGroupController controller : tokenGroupCtrls){
            controller.drawNode(listBox);
            controller.refresh();
        }
    }

    public void updateWallet(List<WalletItemModel> itemModels){


        for(int i=0; i<tokenGroupCtrls.size(); i++){
            tokenGroupCtrls.get(i).updateWallet(itemModels);
        }

        refresh();
    }

    public void tokenSort(Sort sortType) {
        this.tokenSortType = sortType;

        if(this.tokenSortType == Sort.ALIAS_ASC || this.tokenSortType == Sort.ALIAS_DESC){

            List<TokenListGroupController> t1 = new ArrayList<>();
            List<TokenListGroupController> t2 = new ArrayList<>();

            // APIS와 미네랄은 정렬에 포함시키지 않음
            for(int i=0; i<tokenGroupCtrls.size(); i++){
                if(tokenGroupCtrls.get(i).getTokenAddress().equals("-1") || tokenGroupCtrls.get(i).getTokenAddress().equals("-2")){
                    t1.add(tokenGroupCtrls.get(i));
                }else{
                    t2.add(tokenGroupCtrls.get(i));
                }
            }

            // 그외 토큰은 정렬
            if(this.tokenSortType == Sort.ALIAS_ASC){
                Collections.sort(t2, new TokenNameAsc());
            }else {
                Collections.sort(t2, new TokenNameDesc());
            }

            tokenGroupCtrls.clear();
            for(int i=0; i < t1.size(); i++){
                tokenGroupCtrls.add(t1.get(i));
            }
            for(int i=0; i < t2.size(); i++){
                tokenGroupCtrls.add(t2.get(i));
            }

        }else if(this.tokenSortType == Sort.VALUE_ASC || this.tokenSortType == Sort.VALUE_DESC){

            List<TokenListGroupController> t1 = new ArrayList<>();
            List<TokenListGroupController> t2 = new ArrayList<>();

            // APIS와 미네랄은 정렬에 포함시키지 않음
            for(int i=0; i<tokenGroupCtrls.size(); i++){
                if(tokenGroupCtrls.get(i).getTokenAddress().equals("-1") || tokenGroupCtrls.get(i).getTokenAddress().equals("-2")){
                    t1.add(tokenGroupCtrls.get(i));
                }else{
                    t2.add(tokenGroupCtrls.get(i));
                }
            }

            // 그외 토큰은 정렬
            if(this.tokenSortType == Sort.VALUE_ASC){
                Collections.sort(t2, new TokenValueAsc());
            }else {
                Collections.sort(t2, new TokenValueDesc());
            }

            tokenGroupCtrls.clear();
            for(int i=0; i < t1.size(); i++){
                tokenGroupCtrls.add(t1.get(i));
            }
            for(int i=0; i < t2.size(); i++){
                tokenGroupCtrls.add(t2.get(i));
            }
        }
        refresh();
    }

    public Sort getTokenSort() {
        return this.tokenSortType;
    }

    /**
     * 지갑이름 오름차순 정렬
     */
    public class AliasAsc implements Comparator<WalletListGroupController> {
        @Override
        public int compare(WalletListGroupController o1, WalletListGroupController o2) {
            return o1.getAlias().toLowerCase().compareTo(o2.getAlias().toLowerCase());
        }
    }

    /**
     * 지갑이름 내림차순 정렬
     */
    public class AliasDesc implements Comparator<WalletListGroupController> {
        @Override
        public int compare(WalletListGroupController o1, WalletListGroupController o2) {
            return o2.getAlias().toLowerCase().compareTo(o1.getAlias().toLowerCase());
        }
    }

    /**
     * 어드레스마스킹 오름차순 정렬
     * 어드레스마스킹이 없을 경우 지갑이름 오름차순 정렬
     */
    public class MaskAsc implements Comparator<WalletListGroupController> {
        @Override
        public int compare(WalletListGroupController o1, WalletListGroupController o2) {
            if(o1.getMask().equals(o2.getMask())){
                return o1.getAlias().toLowerCase().compareTo(o2.getAlias().toLowerCase());
            }else{
                return o1.getMask().compareTo(o2.getMask());
            }
        }
    }

    /**
     * 어드레스마스킹 내림차순 정렬
     * 어드레스마스킹이 없을 경우 지갑이름 내림차순 정렬
     */
    public class MaskDesc implements Comparator<WalletListGroupController> {
        @Override
        public int compare(WalletListGroupController o1, WalletListGroupController o2) {
            if(o1.getMask().equals(o2.getMask())){
                return o2.getAlias().toLowerCase().compareTo(o1.getAlias().toLowerCase());
            }else{
                return o2.getMask().compareTo(o1.getMask());
            }
        }
    }

    /**
     * 아피스보유량 오름차순 정렬
     */
    public class ValueAsc implements Comparator<WalletListGroupController> {
        @Override
        public int compare(WalletListGroupController o1, WalletListGroupController o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }

    /**
     * 아피스보유량 내림차순 정렬
     */
    public class ValueDesc implements Comparator<WalletListGroupController> {
        @Override
        public int compare(WalletListGroupController o1, WalletListGroupController o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * 토큰이름 오름차순 정렬
     */
    public class TokenNameAsc implements Comparator<TokenListGroupController> {
        @Override
        public int compare(TokenListGroupController o1, TokenListGroupController o2) {
            return o1.getTokenName().toLowerCase().compareTo(o2.getTokenName().toLowerCase());
        }
    }

    /**
     * 토큰이름 내림차순 정렬
     */
    public class TokenNameDesc implements Comparator<TokenListGroupController> {
        @Override
        public int compare(TokenListGroupController o1, TokenListGroupController o2) {
            return o2.getTokenName().toLowerCase().compareTo(o1.getTokenName().toLowerCase());
        }
    }

    /**
     * 토큰 총 자산 오름차순 정렬
     */
    public class TokenValueAsc implements Comparator<TokenListGroupController> {
        @Override
        public int compare(TokenListGroupController o1, TokenListGroupController o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }

    /**
     * 토큰 총 자산 내림차순 정렬
     */
    public class TokenValueDesc implements Comparator<TokenListGroupController> {
        @Override
        public int compare(TokenListGroupController o1, TokenListGroupController o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * 토큰이름 오름차순 정렬
     */
    public class ModelTokenNameAsc implements Comparator<TokenModel> {
        @Override
        public int compare(TokenModel o1, TokenModel o2) {
            return o1.getTokenName().toLowerCase().compareTo(o2.getTokenName().toLowerCase());
        }
    }

    /**
     * 토큰이름 내림차순 정렬
     */
    public class ModelTokenNameDesc implements Comparator<TokenModel> {
        @Override
        public int compare(TokenModel o1, TokenModel o2) {
            return o2.getTokenName().toLowerCase().compareTo(o1.getTokenName().toLowerCase());
        }
    }

    private TokenListImpl handler;
    public void setHandler(TokenListImpl handler){
        this.handler = handler;
    }
    public interface TokenListImpl{
        void onChangeCheck(WalletItemModel model, boolean isChecked);
        void onClickOpen(WalletItemModel model, int index, int listType);
        void onClickClose(WalletItemModel model, int index, int listType);
    }
}
