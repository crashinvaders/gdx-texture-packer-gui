package com.badlogic.gdx.tools.texturepacker;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class JpegPageFileWriter implements PageFileWriter {

    private final float quality;

    public JpegPageFileWriter(float quality) {
        this.quality = quality;
    }

    @Override
    public boolean isBleedingSupported() {
        return false;
    }

    @Override
    public String getFileExtension() {
        return "jpg";
    }

    @Override
    public void saveToFile(TexturePacker.Settings settings, BufferedImage image, File file) throws IOException {
        ImageOutputStream ios = null;
        try {
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            newImage.getGraphics().drawImage(image, 0, 0, null);
            image = newImage;

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ios = ImageIO.createImageOutputStream(file);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            if (ios != null) {
                try { ios.close(); } catch (Exception ignored) { }
            }
        }
    }
}
