package org.apis.gui.controller.addressmasking;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.gui.controller.base.BaseViewController;

public class AddressMaskingHandOverReceiptController extends BaseViewController {

    @FXML private Label fromAddress, toAddress, mask, value;

    @FXML
    public void onMouseClicked(InputEvent event){
        String fxId = ((Node)event.getSource()).getId();
        if(handler != null){
            handler.transfer();
        }
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress.setText(fromAddress);
    }
    public void setToAddress(String toAddress) {
        this.toAddress.setText(toAddress);
    }
    public void setMask(String mask) {
        this.mask.setText(mask);
    }
    public void setValue(String value) {
        this.value.setText(value);
    }


    private AddressMaskingHandOverReceiptImpl handler;
    public void setHandler(AddressMaskingHandOverReceiptImpl handler){
        this.handler = handler;
    }
    public interface AddressMaskingHandOverReceiptImpl{
        void transfer();
    }
}
