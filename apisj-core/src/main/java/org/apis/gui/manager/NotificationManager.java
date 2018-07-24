package org.apis.gui.manager;

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

    public class NotificationModel{
        private String title;
        private String subTitle;
        private String text;
        private String tag;
        private Date time;
        private Image icon;

        public NotificationModel(String title, String subTitle, String text, String tag){
            this.title = title;
            this.subTitle = subTitle;
            this.text = text;
            this.tag = tag;
            this.time = new Date();
        }

        private NotificationEvent event;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Image getIcon() {
            return icon;
        }

        public void setIcon(Image icon) {
            this.icon = icon;
        }

        public NotificationEvent getEvent() {
            return event;
        }

        public void setEvent(NotificationEvent event) {
            this.event = event;
        }
    }

    public interface NotificationEvent{
        void onClickEvent();
    }
}
