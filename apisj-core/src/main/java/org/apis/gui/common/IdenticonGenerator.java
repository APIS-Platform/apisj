package org.apis.gui.common;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.google.common.primitives.Doubles.concat;

public class IdenticonGenerator {
    private static final int size = 8;
    private static long[] randseed = new long[4];

    public static Image createIcon(String address) {
        return createIcon(address, 8);
    }

    public static Image createIcon(String address, int scale) {
        seedrand(address);
        HSL color = HSL.createColor();
        HSL bgColor = HSL.createColor();
        HSL spotColor = HSL.createColor();

        double[] imgdata = createImageData();
        try{
            return createCanvas(imgdata, color, bgColor, spotColor, scale);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static double[] createImageData() {
        int width = size;
        int height = size;

        double dataWidth = Math.ceil(width / 2);
        double mirrorWidth = width - dataWidth;

        double[] data = new double[size * size];
        int dataCount = 0;
        for (int y = 0; y < height; y++) {
            double[] row = new double[(int) dataWidth];
            for (int x = 0; x < dataWidth; x++) {
                row[x] = Math.floor(rand() * 2.3d);

            }
            double[] r = Arrays.copyOfRange(row, 0, (int) mirrorWidth);
            r = reverse(r);
            row = concat(row, r);

            for (int i = 0; i < row.length; i++) {
                data[dataCount] = row[i];
                dataCount++;
            }
        }

        return data;
    }


    private static Image createCanvas(double[] imgData, HSL color, HSL bgcolor, HSL spotcolor, int scale) throws WriterException, IOException {
        int width = (int) Math.sqrt(imgData.length);

        int w = width * scale;
        int h = width * scale;


        //finalImage = op.filter(identicon, finalImage);

        int[] background = toRGB((int) bgcolor.h, (int) bgcolor.s, (int) bgcolor.l);
        int[] main = toRGB((int) color.h, (int) color.s, (int) color.l);
        int[] scolor = toRGB((int) spotcolor.h, (int) spotcolor.s, (int) spotcolor.l);

        BufferedImage identicon = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = identicon.getRaster();


        for(int x=0; x<w; x++){
            for(int y=0; y<h; y++){
                raster.setPixel(x, y, background);
            }
        }

        for (int i = 0; i < imgData.length; i++) {
            int row = (int) Math.floor(i / width);
            int col = i % width;
            int[] _color = (imgData[i] == 1.0d) ? main : scolor;

            if (imgData[i] > 0d) {
                for(int x=col * scale; x<(col * scale) + scale; x++){
                    for(int y=row * scale; y<(row * scale) + scale; y++){
                        raster.setPixel(x, y, _color);
                    }
                }
            }
        }


        //Scale image to the size you want
        BufferedImage finalImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(1, 1);

        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        finalImage = op.filter(identicon, finalImage);

        if(finalImage != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage) finalImage, "png", out);
            out.flush();
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            return new javafx.scene.image.Image(in);
        }else{
            System.out.println("finalImage : "+finalImage);
            return null;
        }
    }

    private static int[] toRGB(float h, float s, float l) {
        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        return new int[] {red, green, blue, 255};
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0) h += 1;
        if (h > 1) h -= 1;
        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }
        if (2 * h < 1) {
            return q;
        }
        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }
        return p;
    }

    private static void seedrand(String seed) {
        for (int i = 0; i < randseed.length; i++) {
            randseed[i] = 0;
        }
        for (int i = 0; i < seed.length(); i++) {
            long test = randseed[i % 4] << 5;
            if (test > Integer.MAX_VALUE << 1 || test < Integer.MIN_VALUE << 1)
                test = (int) test;

            long test2 = test - randseed[i % 4];
            randseed[i % 4] = (test2 + Character.codePointAt(seed, i));
        }

        for (int i = 0; i < randseed.length; i++)
            randseed[i] = (int) randseed[i];
    }



    static class HSL {
        double h, s, l;

        public HSL(double h, double s, double l) {
            this.h = h;
            this.s = s;
            this.l = l;
        }

        @Override
        public String toString() {
            return "HSL [h=" + h + ", s=" + s + ", l=" + l + "]";
        }

        public static HSL createColor(){
            double h = Math.floor(rand() * 360d);
            double s = ((rand() * 60d) + 40d);
            double l = ((rand() + rand() + rand() + rand()) * 25d);
            return new HSL(h, s, l);
        }
    }

    private static double rand() {
        int t = (int) (randseed[0] ^ (randseed[0] << 11));
        randseed[0] = randseed[1];
        randseed[1] = randseed[2];
        randseed[2] = randseed[3];
        randseed[3] = (randseed[3] ^ (randseed[3] >> 19) ^ t ^ (t >> 8));
        double t1 = Math.abs(randseed[3]);

        return (t1 / Integer.MAX_VALUE);
    }

    private static double[] reverse(double[] data) {
        for (int i = 0; i < data.length / 2; i++) {
            double temp = data[i];
            data[i] = data[data.length - i - 1];
            data[data.length - i - 1] = temp;
        }
        return data;
    }
}
