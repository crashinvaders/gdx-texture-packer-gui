package com.crashinvaders.common.awt;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageTools {
    private ImageTools() { }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Image loadImage(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            return image;
        } catch (IOException ex) {
            throw new RuntimeException("Can't read image: " + imagePath);
        }
    }

    public static Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    public static ImageIcon createImageIcon(String imagePath, int w, int h) {
        return new ImageIcon(getScaledImage(loadImage(imagePath), w, h));
    }
}
