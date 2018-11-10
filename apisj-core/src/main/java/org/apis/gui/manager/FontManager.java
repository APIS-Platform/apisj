package org.apis.gui.manager;

import javafx.scene.Node;
import org.apis.gui.common.JavaFXStyle;

public class FontManager {

    public static void fontStyle(Node node, JavaFXStyle style){
        node.setStyle(new JavaFXStyle(node.getStyle()).add(style).toString());
    }

    /**
     * 싱글톤
     */
    private static FontManager ourInstance = new FontManager();
    public static FontManager getInstance() { return ourInstance; }
    private FontManager() {
    }

    public static class AFontColor{
        public static JavaFXStyle C910000 = new JavaFXStyle().add("-fx-text-fill", "#910000");
        public static JavaFXStyle C999999 = new JavaFXStyle().add("-fx-text-fill", "#999999");
    }

    public static class AFontSize{
        public static JavaFXStyle Size12 = new JavaFXStyle().add("-fx-font-size", "12px");
        public static JavaFXStyle Size14 = new JavaFXStyle().add("-fx-font-size", "14px");
        public static JavaFXStyle Size16 = new JavaFXStyle().add("-fx-font-size", "16px");
        public static JavaFXStyle Size18 = new JavaFXStyle().add("-fx-font-size", "18px");
        public static JavaFXStyle Size20 = new JavaFXStyle().add("-fx-font-size", "20px");
    }

    public static class Standard {
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "Open Sans Regular");

        public static JavaFXStyle SemiBold = new JavaFXStyle().add("-fx-font-family", "Open Sans SemiBold");
        public static JavaFXStyle SemiBold12 = new JavaFXStyle(SemiBold).add(AFontSize.Size12);
        public static JavaFXStyle SemiBold14 = new JavaFXStyle(SemiBold).add(AFontSize.Size14);
    }

    public static class Hex {
        public static JavaFXStyle Regular = new JavaFXStyle().add("-fx-font-family", "Roboto Mono");
        public static JavaFXStyle Medium = new JavaFXStyle().add("-fx-font-family", "Roboto Mono Medium");
    }
}

