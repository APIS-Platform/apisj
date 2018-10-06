package org.apis.gui.controller.wallet;

import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.MainController;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupCopyWalletAddressController;
import org.apis.gui.controller.popup.PopupMaskingController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WalletListGroupController extends BaseViewController {
    private WalletItemModel model;
    private GroupType groupType = GroupType.WALLET;
    private BaseFxmlController header;
    private List<BaseFxmlController> items = new ArrayList<>();
    private boolean isVisibleItemList = false;

    public WalletListGroupController(GroupType groupType){
        this.groupType = groupType;

        try {
            header = new BaseFxmlController("wallet/wallet_list_header.fxml");
            WalletListHeadController controller = (WalletListHeadController)header.getController();
            controller.setGroupType(this.groupType);
            controller.setHandler(headerHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;
        this.header.getController().setModel(model);
        for(int i=0; i<this.model.getTokens().size(); i++){
            boolean hasToken = false;
            for(int j=0; j<this.items.size(); j++) {

                WalletListBodyController bodyController = (WalletListBodyController) this.items.get(j).getController();


                if (this.model.getTokens().get(i).getTokenAddress().equals(bodyController.getModel().getTokenAddress())) {
                    // update item
                    bodyController.setModel(this.model.getClone().setCusorTokenIndex(i));

                    hasToken = true;
                    break;
                }
            }
            // add item
            if(hasToken == false){
                addItem(this.model.getClone().setCusorTokenIndex(i));
            }
        }
    }

    /**
     * 이 그룹에 아이템을 포함시킨다.
     * @param model 아이템을 포함 시킬 모델
     */
    public void addItem(WalletItemModel model){
        try {
            BaseFxmlController item = new BaseFxmlController("wallet/wallet_list_body.fxml");
            item.getController().setModel(model);
            this.items.add(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            for(int i=0; i<items.size(); i++){
                parent.getChildren().add(items.get(i).getNode());
            }
        }
    }

    /**
     * 아이템을 모두 보여주거나 모두 숨길 수 있다.
     * @param isVisible {true : 모두 보임, false : 모두 숨김}
     */
    public void setVisibleItemList(boolean isVisible){
        this.isVisibleItemList = isVisible;
        for(int i=0; i<this.items.size(); i++){
            WalletListBodyController controller = (WalletListBodyController)this.items.get(i).getController();
            if(isVisible){
                controller.show();
            }else{
                controller.hide();
            }
        }
    }

    public WalletItemModel getModel(){
        return this.model;
    }

    /**
     * 현재 설정한 상태로 지갑을 다시 보여준다.
     */
    public void refresh() {
        setVisibleItemList(this.isVisibleItemList);
    }


    /**
     * Header Event
     */
    private WalletListHeadController.WalletListHeaderInterface headerHandler = new WalletListHeadController.WalletListHeaderInterface() {
        @Override
        public void onClickEvent(InputEvent event, WalletItemModel model) {
            setVisibleItemList(!isVisibleItemList);
        }

        @Override
        public void onClickTransfer(InputEvent event, WalletItemModel model) {
            AppManager.getInstance().guiFx.getMain().selectedHeader(MainController.MainTab.TRANSFER);
            AppManager.getInstance().guiFx.getTransfer().init(model.getId());
        }

        @Override
        public void onChangeCheck(WalletItemModel model, boolean isChecked) {

        }

        @Override
        public void onClickCopy(String address, WalletItemModel model) {
            PopupCopyWalletAddressController controller = (PopupCopyWalletAddressController)PopupManager.getInstance().showMainPopup("popup_copy_wallet_address.fxml", 0);
            controller.setAddress(address);
        }

        @Override
        public void onClickAddressMasking(InputEvent event, WalletItemModel model) {
            PopupMaskingController controller = (PopupMaskingController)PopupManager.getInstance().showMainPopup("popup_masking.fxml", 0);
            controller.setSelectAddress(model.getAddress());
        }
    };


    public enum GroupType {
        /* 지갑기준 */WALLET(0),
        /* 토큰기준 */TOKEN(1);
        int num;
        GroupType(int num) {
            this.num = num;
        }
    }
}
