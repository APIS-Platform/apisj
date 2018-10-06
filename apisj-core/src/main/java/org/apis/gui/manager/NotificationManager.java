package org.apis.gui.manager;

import org.apis.gui.model.NotificationModel;
import org.apis.gui.model.base.BaseModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

public class NotificationManager {
    private ArrayList<NotificationModel> list = new ArrayList<>();

    private static NotificationManager ourInstance = new NotificationManager();
    public static NotificationManager getInstance() {
        return ourInstance;
    }
    private NotificationManager() {
        list.add(new NotificationModel("APIS transfer","added 4 upgraded", "Components and a ton more goodies!", "transfer ·  APIS"));
        list.add(new NotificationModel(null,null, "2.0 is fresh out of the oven, with\nComponents and a ton more goodies!", "notice ·  APIS"));
    }

    public int getSize(){return this.list.size();}
    public ArrayList<NotificationModel> getList(){ return this.list; }


    public interface NotificationEvent{
        void onClickEvent();
    }
}
