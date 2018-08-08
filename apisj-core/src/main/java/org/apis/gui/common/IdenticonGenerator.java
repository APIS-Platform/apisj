package org.apis.gui.common;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by grender on 1/07/17.
 * https://gist.github.com/GrenderG/caaae6de29e456438a6f9bd6394ca566#file-identicongenerator-java
 */

public class IdenticonGenerator {

    // New Method
    public static Image generateIdenticonsToImage(String text, int image_width, int image_height) throws WriterException, IOException {
        BufferedImage bufferedImage = generateIdenticons(text, image_width, image_height);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) bufferedImage, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }


    public static BufferedImage generateIdenticons(String text, int image_width, int image_height) {
        int width = 9, height = 9;

        byte[] hash = text.getBytes();

        BufferedImage identicon = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = identicon.getRaster();

        int[] background = new int[] {hash[0] & 255, hash[2] & 255, hash[4] & 255, hash[6] & 255};
        int[] foreground = new int[] {hash[1] & 255, hash[3] & 255, hash[5] & 255, 255};

        for(int x = 0; x < width; x++) {
            //Enforce horizontal symmetry
            int i = x < (width / 2) + 1 ? x : (width - 1) - x;
            for(int y = 0; y < height; y++) {
                int[] pixelColor;
                //toggle pixels based on bit being on/off
                if ((hash[i] >> y & 1) == 1)
                    pixelColor = foreground;
                else
                    pixelColor = background;
                raster.setPixel(x, y, pixelColor);
            }
        }

        BufferedImage finalImage = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_ARGB);

        //Scale image to the size you want
        AffineTransform at = new AffineTransform();
        at.scale(image_width / width, image_height / height);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        finalImage = op.filter(identicon, finalImage);

        return finalImage;
    }

    public static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static String md5Hex(String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveImage(BufferedImage bufferedImage, String name) {
        File outputfile = new File(name + ".png");
        try {
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Pass the text as arg");
            System.exit(1);
        }
        String text = args[0];
        String md5 = md5Hex(text.toLowerCase());
        saveImage(generateIdenticons(md5, 500, 500), md5);
    }
}
