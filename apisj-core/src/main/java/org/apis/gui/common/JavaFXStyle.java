package org.apis.gui.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFXStyle {

    public static final String KEY_BG_COLOR = "-fx-background-color";
    public static final String KEY_BORDER_COLOR = "-fx-border-color";

    private Map<String, String> styleList = new HashMap();

    /*
    * 생성자
    * */
    public JavaFXStyle(){}
    public JavaFXStyle(String style){ init(style); }
    public JavaFXStyle(JavaFXStyle style){ init(style.toString()); }

    /*
    * 초기화 함수
    * */
    public JavaFXStyle init(String style){
        String[] list = style.split(";");
        String key, value;
        String[] keyValue = null;
        for(int i=0; i<list.length; i++){
            if(list[i].length() > 0 && (keyValue = list[i].split(":")) != null && keyValue.length >= 2){
                key = keyValue[0].trim();
                value = keyValue[1].trim();
                styleList.put(key, value);
            }
        }

        return this;
    }

    /*
    * 스타일 추가 함수
    * */
    public JavaFXStyle add(String key, String value){
        styleList.put(key.trim(), value.trim());
        return this;
    }

    /*
    * 스타일 삭제 함수
    * */
    public JavaFXStyle remove(String key){
        styleList.remove(key.trim());
        return this;
    }

    @Override
    public String toString() {
        StringBuffer style = new StringBuffer();
        List<String> keyList = new ArrayList<String>(styleList.keySet());
        for(int i=0; i<styleList.size(); i++){
            style.append(keyList.get(i));
            style.append(":");
            style.append(styleList.get(keyList.get(i)));
            style.append(";");
        }
        return style.toString();
    }
}
