package org.apis.gui.model;

import org.apis.gui.manager.NotificationManager;
import org.apis.gui.model.base.BaseModel;

import java.awt.*;
import java.util.Date;

public class NotificationModel extends BaseModel {
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

    private NotificationManager.NotificationEvent event;

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

    public NotificationManager.NotificationEvent getEvent() {
        return event;
    }

    public void setEvent(NotificationManager.NotificationEvent event) {
        this.event = event;
    }
}
