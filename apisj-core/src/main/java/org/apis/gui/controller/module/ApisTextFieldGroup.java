package org.apis.gui.controller.module;

import java.util.ArrayList;

public class ApisTextFieldGroup {
    private ArrayList<ApisTextFieldController> list = new ArrayList<ApisTextFieldController>();

    public void add(ApisTextFieldController controller) {
        list.add(controller);

        controller.setVisibleHandler(new ApisTextFieldController.ApisTextFieldImpl() {
            @Override
            public void show(ApisTextFieldController controller) {
                for(int i = 0; i < list.size(); i++) {
                    if(controller != list.get(i)) {
                        list.get(i).oskClose();
                    }
                }
            }

            @Override
            public void close(ApisTextFieldController controller) {

            }
        });
    }

}
