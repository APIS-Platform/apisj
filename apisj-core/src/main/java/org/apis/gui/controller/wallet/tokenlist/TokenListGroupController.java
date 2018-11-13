package org.apis.gui.controller.wallet.tokenlist;

import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.MainController;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyController;
import org.apis.gui.controller.popup.PopupMaskingController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.model.WalletItemModel;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TokenListGroupController extends BaseViewController {
    private String tokenAddress;

    private BaseFxmlController header;
    private List<BaseFxmlController> items = new ArrayList<>();
    private boolean isVisibleItemList = false;

    public TokenListGroupController(String tokenAddress){
        try {
            header = new BaseFxmlController("wallet/tokenlist/token_list_header.fxml");
            setTokenAddress(tokenAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateWallet(List<WalletItemModel> itemModels) {
        // header 데이터 변경
        TokenListHeadController headerCtrl = (TokenListHeadController)header.getController();
        headerCtrl.update();

        // 최대 갯수만큼 토큰 컨트롤러를 생성한다.
        int count = itemModels.size() - this.items.size();
        for (int i = count; i > 0; i--) {
            addItem(null);
        }

        // 생성된 토큰 컨트롤러가 더 많을 경우 컨트롤러 삭제
        count = this.items.size() - itemModels.size();
        for (int i = count; i > 0; i--) {
            this.items.remove(0);
        }

        // 지갑이름으로 오름차순
        if(items.size() > 1) {
            Collections.sort(items, new WalletAliasSortAsc());
        }

        // 데이터 넣기
        for(int i=0; i<itemModels.size(); i++){
            TokenListBodyController controller = (TokenListBodyController)this.items.get(i).getController();
            controller.setTokenAddress(this.tokenAddress);
            controller.setModel(itemModels.get(i));
        }
    }

    /**
     * 이 그룹에 아이템을 포함시킨다.
     * @param model 아이템을 포함 시킬 모델
     */
    public void addItem(WalletItemModel model){
        try {
            BaseFxmlController item = new BaseFxmlController("wallet/tokenlist/token_list_body.fxml");
            TokenListBodyController controller = ((TokenListBodyController)item.getController());
            controller.setTokenAddress(tokenAddress);
            controller.setModel(model);
            controller.setHandler(tokenBodyHandler);
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
     * 인자에 넣은 Pane에 Node를 추가한다.
     * 추가되는 순서는, header, itemList 순서이다.
     * @param parent Node들을 추가할 VBox
     */
    public void drawNode(VBox parent){
        if(header != null ) {
            String tokenName = "";
            tokenName = ((TokenListHeadController)header.getController()).getTokenName();
            if (tokenName.toLowerCase().indexOf(AppManager.getInstance().getSearchToken().get().toLowerCase()) >= 0) {
                // header
                parent.getChildren().add(header.getNode());

                // items
                for (int i = 0; i < items.size(); i++) {
                    parent.getChildren().add(items.get(i).getNode());
                }
            }
        }
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
        TokenListHeadController controller = (TokenListHeadController)header.getController();
        controller.setTokenAddress(this.tokenAddress);
        controller.setHandler(tokenHeaderHandler);
    }

    /**
     * 아이템을 모두 보여주거나 모두 숨길 수 있다.
     * @param isVisible {true : 모두 보임, false : 모두 숨김}
     */
    public void setVisibleItemList(boolean isVisible){
        this.isVisibleItemList = isVisible;

        TokenListHeadController headerController = (TokenListHeadController)header.getController();
        if(isVisible){
            headerController.setState(TokenListHeadController.HEADER_STATE_OPEN);
        }else{
            headerController.setState(TokenListHeadController.HEADER_STATE_CLOSE);
        }

        for(int i=0; i<this.items.size(); i++){
            TokenListBodyController controller = (TokenListBodyController)this.items.get(i).getController();
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
        TokenListHeadController controller = (TokenListHeadController)header.getController();
        WalletItemModel headerModel = (WalletItemModel)controller.getModel();
        return headerModel.getAlias();
    }

    /**
     * 아피스 보유랑을 반환한다.
     * @return
     */
    public BigInteger getValue() {
        TokenListHeadController controller = (TokenListHeadController) header.getController();
        return controller.getTokenValue();
    }

    /**
     * 어드레스 마스크를 반환한다.
     * 없을 경우, 공백("")을 반환한다.
     * @return
     */
    public String getMask() {
        TokenListHeadController controller = (TokenListHeadController)header.getController();
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
        return this.tokenAddress;
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
     * 토큰 자산으로 오름차순 정렬
     */
    public class WalletAliasSortAsc implements Comparator<BaseFxmlController> {
        @Override
        public int compare(BaseFxmlController o1, BaseFxmlController o2) {
            TokenListBodyController o1Ctrl = ((TokenListBodyController)o1.getController());
            TokenListBodyController o2Ctrl = ((TokenListBodyController)o2.getController());
            return o1Ctrl.getWalletName().toLowerCase().compareTo(o2Ctrl.getWalletName().toLowerCase());
        }
    }

    /**
     * 토큰 자산으로 내림차순 정렬
     */
    public class WalletAliasSortDesc implements Comparator<BaseFxmlController> {
        @Override
        public int compare(BaseFxmlController o1, BaseFxmlController o2) {
            TokenListBodyController o1Ctrl = ((TokenListBodyController)o1.getController());
            TokenListBodyController o2Ctrl = ((TokenListBodyController)o2.getController());
            return o2Ctrl.getWalletName().toLowerCase().compareTo(o1Ctrl.getWalletName().toLowerCase());
        }
    }

    /**
     * Header Event
     */
    private TokenListHeadController.TokenListHeadImpl tokenHeaderHandler = new TokenListHeadController.TokenListHeadImpl() {
        @Override
        public void onClickEvent(InputEvent event, String tokenAddress) {
            setVisibleItemList(!isVisibleItemList);
            if(handler != null){
                if(isVisibleItemList){
                    handler.onClickOpen(tokenAddress);
                }else{
                    handler.onClickClose(tokenAddress);
                }
            }
        }
    };

    private TokenListBodyController.TokenListBodyImpl tokenBodyHandler = new TokenListBodyController.TokenListBodyImpl() {
        @Override
        public void onClickTransfer(InputEvent event, WalletItemModel model) {
            AppManager.getInstance().guiFx.getMain().selectedHeader(MainController.MainTab.TRANSFER);
            AppManager.getInstance().guiFx.getTransfer().init(model.getId(), tokenAddress);
        }

        @Override
        public void onClickCopy(String address) {
            PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup("popup_copy.fxml", 0);
            controller.setCopyWalletAddress(address);
        }

        @Override
        public void onClickAddressMasking(InputEvent event, WalletItemModel model) {
            PopupMaskingController controller = (PopupMaskingController)PopupManager.getInstance().showMainPopup("popup_masking.fxml", 0);
            controller.setSelectAddress(model.getAddress());
        }
    };

    private TokenListGroupImpl handler;
    public void setHeader(TokenListGroupImpl handler){
        this.handler = handler;
    }
    public interface TokenListGroupImpl{
        void onClickOpen(String tokenAddress);
        void onClickClose(String tokenAddress);
    }
}
