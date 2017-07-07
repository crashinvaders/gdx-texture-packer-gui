package com.badlogic.gdx.tools.texturepacker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface PageFileWriter {
    boolean isBleedingSupported();
    String getFileExtension();
    void saveToFile(TexturePacker.Settings settings, BufferedImage bufferedImage, File file) throws IOException;
}
