package org.apis.gui.manager;

import javafx.scene.Node;
import org.apis.gui.common.JavaFXStyle;

public class StyleManager {

    public static void fontColorStyle(Node node, String color){
        node.setStyle(new JavaFXStyle(node.getStyle()).add(new JavaFXStyle().add("-fx-text-fill", color)).toString());
    }
    public static void fontStyle(Node node, JavaFXStyle style){
        node.setStyle(new JavaFXStyle(node.getStyle()).add(style).toString());
    }
    public static void fontStyle(Node node, JavaFXStyle type, JavaFXStyle size, String lang){
        fontStyleType(node, type, lang);
        fontStyleSize(node, size, lang);
    }
    public static void fontStyleType(Node node, JavaFXStyle type, String lang) {
        JavaFXStyle _style = new JavaFXStyle(node.getStyle()).add(type);

        if(lang.equals("CN") || lang.equals("TW")){
            if(type.compareFamily(Standard.Regular) || type.compareFamily(Standard.SemiBold)) {
                _style.add(Standard.Regular);
            } else if(type.compareFamily(Hex.Regular) || type.compareFamily(Hex.Medium)) {
                _style.add(Hex.Regular);
            } else if(type.compareFamily(Barlow.Regular) || type.compareFamily(Barlow.SemiBold)) {
                _style.add(Barlow.Regular);
            }
        }

        node.setStyle(_style.toString());
    }
    public static void fontStyleSize(Node node, JavaFXStyle size, String lang) {
        JavaFXStyle _style = new JavaFXStyle(node.getStyle()).add(size);

        if(lang.equals("CN") || lang.equals("TW")){
            if(size == AFontSize.Size8
                    || size == AFontSize.Size10
                    || size == AFontSize.Size12 ){
                _style.add(AFontSize.Size14);
            }
        }

        node.setStyle(_style.toString());
    }
    public static void backgroundColorStyle(Node node, String color){
        node.setStyle(new JavaFXStyle(node.getStyle()).add(new JavaFXStyle().add("-fx-background-color", color)).toString());
    }
    public static void borderColorStyle(Node node, String color){
        node.setStyle(new JavaFXStyle(node.getStyle()).add(new JavaFXStyle().add("-fx-border-color", color)).toString());
    }



    /**
     * 싱글톤
     */
    private static StyleManager ourInstance = new StyleManager();
    public static StyleManager getInstance() { return ourInstance; }
    private StyleManager() {
    }
    public static class AColor {
        public static String Ca61c1c = "#a61c1c";
        public static String Cb01e1e = "#b01e1e";
        public static String C999999 = "#999999";
        public static String C2b8a3e = "#2b8a3e";
        public static String C4871ff = "#4871ff";
        public static String C353535 = "#353535";
        public static String Cd8d8d8 = "#d8d8d8";
        public static String Cffffff = "#ffffff";
        public static String Cf8f8fb = "#f8f8fb";
        public static String C2b2b2b = "#2b2b2b";
        public static String C000000 = "#000000";
        public static String C202020 = "#202020";
        public static String Cc8c8c8 = "#c8c8c8";
        public static String C36b25b = "#36b25b";
        public static String Ce2e2e2 = "#e2e2e2";
        public static String Cefefef = "#efefef";
        public static String Cc1c1c1 = "#c1c1c1";
        public static String C910000 = "#910000";
        public static String Ccfcfcf = "#cfcfcf";

    }

    public static class AFontSize{
        public static JavaFXStyle Size8 = new JavaFXStyle().add("-fx-font-size", "8px");
        public static JavaFXStyle Size10 = new JavaFXStyle().add("-fx-font-size", "10px");
        public static JavaFXStyle Size12 = new JavaFXStyle().add("-fx-font-size", "12px");
        public static JavaFXStyle Size14 = new JavaFXStyle().add("-fx-font-size", "14px");
        public static JavaFXStyle Size16 = new JavaFXStyle().add("-fx-font-size", "16px");
        public static JavaFXStyle Size18 = new JavaFXStyle().add("-fx-font-size", "18px");
        public static JavaFXStyle Size20 = new JavaFXStyle().add("-fx-font-size", "20px");
        public static JavaFXStyle Size32 = new JavaFXStyle().add("-fx-font-size", "32px");
    }

    public static class Standard {
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "'Noto Sans CJK JP Regular'");
        public static JavaFXStyle Regular8 = new JavaFXStyle(Regular).add(AFontSize.Size8);
        public static JavaFXStyle Regular10 = new JavaFXStyle(Regular).add(AFontSize.Size10);
        public static JavaFXStyle Regular12 = new JavaFXStyle(Regular).add(AFontSize.Size12);
        public static JavaFXStyle Regular14 = new JavaFXStyle(Regular).add(AFontSize.Size14);

        public static JavaFXStyle SemiBold = new JavaFXStyle().add("-fx-font-family", "'Noto Sans CJK JP Medium'");
        public static JavaFXStyle SemiBold10 = new JavaFXStyle(SemiBold).add(AFontSize.Size10);
        public static JavaFXStyle SemiBold12 = new JavaFXStyle(SemiBold).add(AFontSize.Size12);
        public static JavaFXStyle SemiBold14 = new JavaFXStyle(SemiBold).add(AFontSize.Size14);
    }

    public static class Hex {
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "'Roboto Mono'");
        public static JavaFXStyle Regular10 = new JavaFXStyle(Regular).add(AFontSize.Size10);
        public static JavaFXStyle Medium = new JavaFXStyle().add("-fx-font-family", "'Roboto Mono Medium'");
        public static JavaFXStyle Medium10 = new JavaFXStyle(Medium).add(AFontSize.Size10);

    }

    public static class Barlow{
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "'Barlow'");
        public static JavaFXStyle Regular12 = new JavaFXStyle(Regular).add(AFontSize.Size12);
        public static JavaFXStyle Regular14 = new JavaFXStyle(Regular).add(AFontSize.Size14);
        public static JavaFXStyle Regular32 = new JavaFXStyle(Regular).add(AFontSize.Size32);


        public static JavaFXStyle SemiBold = new JavaFXStyle().add("-fx-font-family", "'Barlow SemiBold'");
        public static JavaFXStyle SemiBold12 = new JavaFXStyle(SemiBold).add(AFontSize.Size12);
        public static JavaFXStyle SemiBold14 = new JavaFXStyle(SemiBold).add(AFontSize.Size14);
    }
}

