package org.apis.gui.manager;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import org.apis.gui.common.IdenticonGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {
    public static final Image apisIcon = new Image("image/ic_apis@2x.png");
    public static final Image mineraIcon = new Image("image/ic_mineral@2x.png");

    private static Map<String, Image> identicons = new HashMap<>();
    public static Image getIdenticons(String address) {
        Image image = identicons.get(address);
        if(image == null){
            try {
                image = IdenticonGenerator.generateIdenticonsToImage(address, 128, 128);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }
}
