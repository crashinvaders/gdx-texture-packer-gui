package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.github.czyzby.kiwi.util.common.Strings;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class PackagingHandler {
    private final PackModel packModel;

    public PackagingHandler(PackModel packModel) {
        this.packModel = packModel;
    }

    public static Array<ImageEntry> collectImageFiles(PackModel packModel) {
        ObjectSet<ImageEntry> images = new ObjectSet<>();
        Array<InputFile> inputFiles = packModel.getInputFiles();

        // Collect all input images
        for (InputFile inputFile : inputFiles) {
            if (inputFile.getType() != InputFile.Type.Input) continue;
            if (!inputFile.isDirectory()) {
                // Singular file
                FileHandle fileHandle = inputFile.getFileHandle();
                String name = fileHandle.nameWithoutExtension();
                if (Strings.isNotEmpty(inputFile.getRegionName())) {
                    name = inputFile.getRegionName();
                }
                images.add(new ImageEntry(fileHandle, name));
            } else {
                // Directory
                FileHandle fileHandle = inputFile.getFileHandle();
                FileHandle[] children = fileHandle.list((FileFilter) new SuffixFileFilter(new String[]{".png", ".jpg", "jpeg"}));
                for (FileHandle child : children) {
                    String name = child.name();
                    if (Strings.isNotEmpty(inputFile.getDirFilePrefix())) {
                        name = inputFile.getDirFilePrefix() + name;
                    }
                    images.add(new ImageEntry(child, name));
                }
            }
        }
        // Exclude all ignore entries
        for (InputFile inputFile : inputFiles) {
            if (inputFile.getType() != InputFile.Type.Ignore || inputFile.isDirectory()) continue;
            images.remove(new ImageEntry(inputFile.getFileHandle(), null));
        }

        // Return result
        Array<ImageEntry> result = new Array<>();
        for (ImageEntry image : images) {
            result.add(image);
        }
        return result;
    }

    public void pack() {
        TexturePacker packer = new TexturePacker(packModel.getSettings());

        Array<ImageEntry> imageEntries = collectImageFiles(packModel);
        for (ImageEntry image : imageEntries) {
            BufferedImage bufferedImage = createBufferedImage(image.fileHandle);
            packer.addImage(bufferedImage, image.name);
        }

        packer.pack(new File(packModel.getOutputDir()), packModel.getName());

    }

    public BufferedImage createBufferedImage (FileHandle fileHandle) {
        File file = fileHandle.file();
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading image: " + file, ex);
        }
        if (image == null) throw new RuntimeException("Unable to read image: " + file);

        return image;
    }

    private static class ImageEntry {
        private final FileHandle fileHandle;
        private final String name;

        public ImageEntry(FileHandle fileHandle, String name) {
            this.fileHandle = fileHandle;
            this.name = name;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageEntry that = (ImageEntry) o;

            return fileHandle.equals(that.fileHandle);
        }

        @Override public int hashCode() {
            return fileHandle.hashCode();
        }
    }
}
