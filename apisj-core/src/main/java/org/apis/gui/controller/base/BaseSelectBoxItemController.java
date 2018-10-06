package org.apis.gui.controller.base;

import org.apis.gui.model.SelectBoxItemModel;

public class BaseSelectBoxItemController extends BaseViewController {

    protected BaseSelectBoxItemImpl handler;
    public void setHandler(BaseSelectBoxItemImpl handler) {
        this.handler = handler;
    }
    public interface BaseSelectBoxItemImpl{
        void onMouseClicked(SelectBoxItemModel itemModel);
    }
}
