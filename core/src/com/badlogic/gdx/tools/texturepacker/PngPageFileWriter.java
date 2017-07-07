package com.badlogic.gdx.tools.texturepacker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PngPageFileWriter implements PageFileWriter {

    @Override
    public boolean isBleedingSupported() {
        return true;
    }

    @Override
    public String getFileExtension() {
        return "png";
    }

    @Override
    public void saveToFile(TexturePacker.Settings settings, BufferedImage image, File file) throws IOException {
        if (settings.premultiplyAlpha) {
            image.getColorModel().coerceData(image.getRaster(), true);
        }
        ImageIO.write(image, "png", file);
    }
}
