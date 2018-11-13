package org.apis.gui.controller.wallet.walletlist;

import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.MainController;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyController;
import org.apis.gui.controller.popup.PopupMaskingController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.model.TokenModel;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.keystore.KeyStoreDataExp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WalletListGroupController extends BaseViewController {
    private WalletItemModel model = new WalletItemModel();
    private BaseFxmlController header;
    private List<BaseFxmlController> items = new ArrayList<>();
    private boolean isVisibleItemList = false;

    public WalletListGroupController(){

        try {

            header = new BaseFxmlController("wallet/walletlist/wallet_list_header.fxml");
            WalletListHeadController controller = (WalletListHeadController)header.getController();
            controller.setHandler(headerHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<TokenModel> tokens = AppManager.getInstance().getTokens();
        for(int i=0; i<tokens.size(); i++){
            addItem(null);
        }
    }

    @Override
    public void setModel(BaseModel model) {
        if(model != null) {
            this.model.set((WalletItemModel)model);
            this.header.getController().setModel(model);

            List<TokenModel> tokens = AppManager.getInstance().getTokens();
            int count = tokens.size() - this.items.size();
            for (int i = count; i > 0; i--) {
                addItem(null);
            }

            // 생성된 토큰 컨트롤러가 더 많을 경우 컨트롤러 삭제
            count = this.items.size() - tokens.size();
            for (int i = count; i > 0; i--) {
                this.items.remove(0);
            }

            // 토큰 컨트롤러에 데이터 넣기
            for (int i = 0; i < items.size(); i++) {
                WalletListBodyController controller = (WalletListBodyController) items.get(i).getController();
                WalletItemModel newModel = ((WalletItemModel) model).getClone();
                newModel.setTokenAddress(tokens.get(i).getTokenAddress());
                controller.setModel(newModel);
            }
        }
    }

    @Override
    public BaseModel getModel(){
        return this.model;
    }

    /**
     * 토큰탭의 아이템을 지갑개수만큼 생성한다.
     */
    public void initBoyItems(){
        // 최대 갯수만큼 토큰 컨트롤러를 생성한다.
        ArrayList<KeyStoreDataExp> wallet = AppManager.getInstance().getKeystoreExpList();
        int count = wallet.size() - this.items.size();
        for (int i = count; i > 0; i--) {
            addItem(null);
        }

        // 생성된 토큰 컨트롤러가 더 많을 경우 컨트롤러 삭제
        count = this.items.size() - wallet.size();
        for (int i = count; i > 0; i--) {
            this.items.remove(0);
        }
    }

    /**
     * 이 그룹에 아이템을 포함시킨다.
     * @param model 아이템을 포함 시킬 모델
     */
    public void addItem(WalletItemModel model){
        try {
            BaseFxmlController item = new BaseFxmlController("wallet/walletlist/wallet_list_body.fxml");
            WalletListBodyController controller = ((WalletListBodyController)item.getController());
            controller.setModel(model);
            this.items.add(item);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 이 그룹의 아이템리스트를 반환한다.
     * @return
     */
    public List<BaseFxmlController> getItems() {
        return this.items;
    }

    /**
     * 체크박스를 선택한다.
     */
    public void check() {
        ((WalletListHeadController)this.header.getController()).setCheck(true);
    }

    /**
     * 체크박스의 선택을 취소한다.
     */
    public void unCheck(){
        ((WalletListHeadController)this.header.getController()).setCheck(false);
    }

    /**
     * 인자에 넣은 Pane에 Node를 추가한다.
     * 추가되는 순서는, header, itemList 순서이다.
     * @param parent Node들을 추가할 VBox
     */
    public void drawNode(VBox parent){
        if(header != null) {
            // header
            parent.getChildren().add(header.getNode());

            // items
            for (int i = 0; i < items.size(); i++) {
                String tokenName = "";
                if (((WalletItemModel) items.get(i).getController().getModel()).getTokenAddress() != null) {
                    for(TokenModel token : AppManager.getInstance().getTokens()){
                        if(token.getTokenAddress().equals(((WalletItemModel) items.get(i).getController().getModel()).getTokenAddress())){
                            tokenName = token.getTokenName();
                            break;
                        }
                    }

                }
                String searchToken = AppManager.getInstance().getSearchToken().get();
                searchToken = (searchToken != null) ? searchToken.toLowerCase() : "";
                if (tokenName.toLowerCase().indexOf(searchToken) >= 0) {
                    parent.getChildren().add(items.get(i).getNode());
                }
            }
        }
    }

    /**
     * 아이템을 모두 보여주거나 모두 숨길 수 있다.
     * @param isVisible {true : 모두 보임, false : 모두 숨김}
     */
    public void setVisibleItemList(boolean isVisible){
        this.isVisibleItemList = isVisible;
        WalletListHeadController headerController = (WalletListHeadController)header.getController();
        if(isVisible){
            headerController.setState(WalletListHeadController.HEADER_STATE_OPEN);
        }else{
            headerController.setState(WalletListHeadController.HEADER_STATE_CLOSE);
        }

        for(int i=0; i<this.items.size(); i++){
            WalletListBodyController controller = (WalletListBodyController)this.items.get(i).getController();
            if(isVisible){
                controller.show();
            }else{
                controller.hide();
            }
        }
    }

    /**
     * 현재 설정한 상태로 지갑을 다시 보여준다.
     */
    public void refresh() {
        setVisibleItemList(this.isVisibleItemList);
    }

    /**
     * 지갑주소를 반환한다.
     */
    public String getAlias(){
        WalletListHeadController controller = (WalletListHeadController)header.getController();
        WalletItemModel headerModel = (WalletItemModel)controller.getModel();
        return headerModel.getAlias();
    }

    /**
     * 아피스 보유랑을 반환한다.
     * @return
     */
    public BigInteger getValue() {
        WalletListHeadController controller = (WalletListHeadController)header.getController();
        WalletItemModel headerModel = (WalletItemModel)controller.getModel();
        return headerModel.getApis();
    }

    /**
     * 어드레스 마스크를 반환한다.
     * 없을 경우, 공백("")을 반환한다.
     * @return
     */
    public String getMask() {
        WalletListHeadController controller = (WalletListHeadController)header.getController();
        WalletItemModel headerModel = (WalletItemModel)controller.getModel();
        String mask = headerModel.getMask();
        if(mask == null){
            mask = "";
        }
        return mask;
    }

    /**
     * 토큰 주소를 반환한다.
     * @return
     */
    public String getTokenAddress() {
        WalletItemModel model = (WalletItemModel) getModel();
        return model.getTokenAddress();
    }

    /**
     * 토큰 이름을 반환한다
     * @return
     */
    public String getTokenName() {
        if(getTokenAddress().equals("-1")){
            return "APIS";
        }else if(getTokenAddress().equals("-2")){
            return "MINERAL";
        }
        return AppManager.getInstance().getTokenName(getTokenAddress());
    }


    /**
     * 토큰리스트 토큰 자산으로 오름차순
     */
    public void tokenValueSortASC() {
        //Collections.sort(items, new TokenValueSortAsc());
    }

    /**
     * 토큰리스트 토큰 자산으로 내림차순
     */
    public void tokenValueSortDESC() {
        //Collections.sort(items, new TokenValueSortDesc());
    }

    /**
     * 토큰 자산으로 오름차순 정렬
     */
    public class TokenValueSortAsc implements Comparator<BaseFxmlController> {
        @Override
        public int compare(BaseFxmlController o1, BaseFxmlController o2) {
            WalletListBodyController o1Ctrl = ((WalletListBodyController)o1.getController());
            WalletListBodyController o2Ctrl = ((WalletListBodyController)o2.getController());
            return o1Ctrl.getValue().compareTo(o2Ctrl.getValue());
        }
    }

    /**
     * 토큰 자산으로 내림차순 정렬
     */
    public class TokenValueSortDesc implements Comparator<BaseFxmlController> {
        @Override
        public int compare(BaseFxmlController o1, BaseFxmlController o2) {
            WalletListBodyController o1Ctrl = ((WalletListBodyController)o1.getController());
            WalletListBodyController o2Ctrl = ((WalletListBodyController)o2.getController());
            return o2Ctrl.getValue().compareTo(o1Ctrl.getValue());
        }
    }

    /**
     * Header Event
     */
    private WalletListHeadController.WalletListHeaderInterface headerHandler = new WalletListHeadController.WalletListHeaderInterface() {
        @Override
        public void onClickEvent(InputEvent event, WalletItemModel model) {
            setVisibleItemList(!isVisibleItemList);
            if(handler != null){
                if(isVisibleItemList){
                    handler.onClickOpen(model);
                }else{
                    handler.onClickClose(model);
                }
            }
        }

        @Override
        public void onClickTransfer(InputEvent event, WalletItemModel model) {
            AppManager.getInstance().guiFx.getMain().selectedHeader(MainController.MainTab.TRANSFER);
            AppManager.getInstance().guiFx.getTransfer().init(model.getId(), "-1");
        }

        @Override
        public void onChangeCheck(WalletItemModel model, boolean isChecked) {
            if(handler != null){
                handler.onChangeCheck(model, isChecked);
            }
        }

        @Override
        public void onClickCopy(String address, WalletItemModel model) {
            PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(null, "popup_copy.fxml", 0);
            controller.setCopyWalletAddress(address);
        }

        @Override
        public void onClickAddressMasking(InputEvent event, WalletItemModel model) {
            PopupMaskingController controller = (PopupMaskingController)PopupManager.getInstance().showMainPopup(null, "popup_masking.fxml", 0);
            controller.setSelectAddress(model.getAddress());
        }
    };

    private WalletListGroupImpl handler;
    public void setHeader(WalletListGroupImpl handler){
        this.handler = handler;
    }
    public interface WalletListGroupImpl{
        void onChangeCheck(WalletItemModel model, boolean isChecked);
        void onClickOpen(WalletItemModel model);
        void onClickClose(WalletItemModel model);
    }
}
