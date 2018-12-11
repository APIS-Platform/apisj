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
        public static String C888888 = "#888888";
        public static String C2b8a3e = "#2b8a3e";
        public static String Cd8d8d8 = "#d8d8d8";
        public static String Cffffff = "#ffffff";
        public static String Cf2f2f2 = "#f2f2f2";
        public static String Cfafafa = "#fafafa";
        public static String C2b2b2b = "#2b2b2b";
        public static String C000000 = "#000000";
        public static String C202020 = "#202020";
        public static String Cf8f8f8 = "#f8f8f8";
        public static String Cc8c8c8 = "#c8c8c8";
        public static String C36b25b = "#36b25b";

    }

    public static class AFontSize{
        public static JavaFXStyle Size12 = new JavaFXStyle().add("-fx-font-size", "12px");
        public static JavaFXStyle Size13 = new JavaFXStyle().add("-fx-font-size", "13px");
        public static JavaFXStyle Size14 = new JavaFXStyle().add("-fx-font-size", "14px");
        public static JavaFXStyle Size16 = new JavaFXStyle().add("-fx-font-size", "16px");
        public static JavaFXStyle Size18 = new JavaFXStyle().add("-fx-font-size", "18px");
        public static JavaFXStyle Size20 = new JavaFXStyle().add("-fx-font-size", "20px");
    }

    public static class Standard {
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "'Noto Sans KR Regular'");
        public static JavaFXStyle Regular12 = new JavaFXStyle(Regular).add(AFontSize.Size12);
        public static JavaFXStyle Regular14 = new JavaFXStyle(Regular).add(AFontSize.Size14);

        public static JavaFXStyle SemiBold = new JavaFXStyle().add("-fx-font-family", "'Noto Sans KR Medium'");
        public static JavaFXStyle SemiBold12 = new JavaFXStyle(SemiBold).add(AFontSize.Size12);
        public static JavaFXStyle SemiBold13 = new JavaFXStyle(SemiBold).add(AFontSize.Size13);
        public static JavaFXStyle SemiBold14 = new JavaFXStyle(SemiBold).add(AFontSize.Size14);
    }

    public static class Hex {
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "'Roboto Mono'");
        public static JavaFXStyle Medium = new JavaFXStyle().add("-fx-font-family", "'Roboto Mono Medium'");
    }
}

