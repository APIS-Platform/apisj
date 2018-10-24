package org.apis.gui.controller.wallet.walletlist;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.wallet.tokenlist.TokenListGroupController;
import org.apis.gui.controller.wallet.WalletController.Sort;
import org.apis.gui.model.TokenModel;
import org.apis.gui.model.WalletItemModel;

import java.net.URL;
import java.util.*;

public class WalletListController extends BaseViewController {

    @FXML private VBox listBox;

    private Sort walletSortType = Sort.DEFAULT_ASC;
    private List<WalletListGroupController> walletGroupCtrls = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    @Override
    public void update(){

    }

    public void walletSort(Sort sortType) {
        this.walletSortType = sortType;

        if(this.walletSortType == Sort.ALIAS_ASC){
            Collections.sort(walletGroupCtrls, new AliasAsc());

        }else if(this.walletSortType == Sort.ALIAS_DESC){
            Collections.sort(walletGroupCtrls, new AliasDesc());

        }else if(this.walletSortType == Sort.MASK_ASC || this.walletSortType == Sort.MASK_DESC){
            List<WalletListGroupController> m1 = new ArrayList<>();
            List<WalletListGroupController> m2 = new ArrayList<>();

            // 마스크 존재여부로 나눔
            for(int i=0; i<walletGroupCtrls.size(); i++){
                if(walletGroupCtrls.get(i).getMask().length() > 0){
                    m1.add(walletGroupCtrls.get(i));
                }else{
                    m2.add(walletGroupCtrls.get(i));
                }
            }

            // 나뉜 두 그룹을 각각 정렬
            if(this.walletSortType == Sort.MASK_ASC){
                Collections.sort(m1, new MaskAsc());
                Collections.sort(m2, new MaskAsc());
            }else{
                Collections.sort(m1, new MaskDesc());
                Collections.sort(m2, new MaskDesc());
            }

            // 그룹 합치기
            walletGroupCtrls.clear();
            for(int i=0; i<m1.size(); i++){
                walletGroupCtrls.add(m1.get(i));
            }
            for(int i=0; i<m2.size(); i++){
                walletGroupCtrls.add(m2.get(i));
            }

        }else if(this.walletSortType == Sort.VALUE_ASC){
            Collections.sort(walletGroupCtrls, new ValueAsc());

        }else if(this.walletSortType == Sort.VALUE_DESC){
            Collections.sort(walletGroupCtrls, new ValueDesc());

        }
        refresh();
    }

    public Sort getWalletSort() {
        return this.walletSortType;
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

    /**
     * 현재 설정한 상태로 지갑을 다시 보여준다.
     */
    public void refresh() {
        listBox.getChildren().clear();
        for(WalletListGroupController controller : walletGroupCtrls){
            controller.drawNode(listBox);
            controller.refresh();
        }
    }

    /**
     * Wallet List에 WalletListGroup 을 추가한다.
     * @param model
     */
    public void updateWallet(WalletItemModel model, int index) {
        boolean isUpdate = false;

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

    }

    /**
     * 지갑을 추가한다.
     * @param model
     */
    private void addGroup(WalletItemModel model){

        WalletListGroupController group = new WalletListGroupController();
        group.setModel(model);
        group.setHeader(new WalletListGroupController.WalletListGroupImpl() {
            @Override
            public void onChangeCheck(WalletItemModel model, boolean isChecked) {
                if(handler != null){
                    handler.onChangeCheck(model, isChecked);
                }
            }

            @Override
            public void onClickOpen(WalletItemModel model) {
                for (int i = 0; i < walletGroupCtrls.size(); i++) {
                    if (model.getId().equals(((WalletItemModel) walletGroupCtrls.get(i).getModel()).getId())) {
                        walletGroupCtrls.get(i).setVisibleItemList(true);
                    } else {
                        walletGroupCtrls.get(i).setVisibleItemList(false);
                    }
                }
            }

            @Override
            public void onClickClose(WalletItemModel model) {
                for (int i = 0; i < walletGroupCtrls.size(); i++) {
                    walletGroupCtrls.get(i).setVisibleItemList(false);
                }
            }
        });

        walletGroupCtrls.add(group);
    }

    /**
     * 지갑을 업데이트 한다.
     * @param model
     */
    private void updateGroup(WalletItemModel model){

        for(int i=0; i<walletGroupCtrls.size(); i++){
            if(model.getId().equals(((WalletItemModel)walletGroupCtrls.get(i).getModel()).getId())){
                walletGroupCtrls.get(i).setModel(model);
                break;
            }
        }
    }

    /**
     * 지갑을 삭제 한다.
     * @param id : wallet id
     */
    public void removeWallet(String id) {
        for(int i=0; i<walletGroupCtrls.size(); i++){
            if(((WalletItemModel)walletGroupCtrls.get(i).getModel()).getId().equals(id)){
                walletGroupCtrls.remove(i);
                break;
            }
        }
    }

    /**
     * 체크박스에 체크를 표기한다.
     * @param model
     */
    public void check(WalletItemModel model) {
        for(int i=0; i<walletGroupCtrls.size(); i++){
            if(((WalletItemModel)walletGroupCtrls.get(i).getModel()).getAddress().equals(model.getAddress())){
                walletGroupCtrls.get(i).check();
                break;
            }
        }
    }

    /**
     * 체크박스 표기를 모두 해제한다.
     */
    public void unCheckAll() {
        for(int i=0; i<walletGroupCtrls.size(); i++){
            walletGroupCtrls.get(i).unCheck();
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
