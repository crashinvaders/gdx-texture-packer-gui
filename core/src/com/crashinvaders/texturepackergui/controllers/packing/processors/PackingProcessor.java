package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.texturepacker.PageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.github.czyzby.kiwi.util.common.Strings;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.regex.Pattern;

public class PackingProcessor implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        PageFileWriter pageFileWriter = node.getPageFileWriter();

        if (pageFileWriter == null) {
            throw new IllegalStateException("PageFileWriter is not set. Looks like something is wrong with file type processor setup.");
        }

        System.out.println("Packing is started");

        String settingsOrigExtension = pack.getSettings().atlasExtension;
        performPacking(pack, pageFileWriter);
        pack.getSettings().atlasExtension = settingsOrigExtension;

        System.out.println("Packing is done");
    }
    
    private void performPacking(PackModel packModel, PageFileWriter pageFileWriter) throws Exception {
        Array<ImageEntry> imageEntries = collectImageFiles(packModel);
        if (imageEntries.size == 0) {
            throw new IllegalStateException("No images to pack");
        }

        deleteOldFiles(packModel);
        String filename = obtainFilename(packModel);

        TexturePacker packer = new TexturePacker(packModel.getSettings(), pageFileWriter);
        for (ImageEntry image : imageEntries) {
            if (image.ninePatch) {
                packer.addImage(image.fileHandle.file(), image.name, image.splits, image.pads);
            } else {
                packer.addImage(image.fileHandle.file(), image.name);
            }
        }
        packer.pack(new File(packModel.getOutputDir()), filename);
    }

    private static Array<ImageEntry> collectImageFiles(PackModel packModel) {
        final ImageEntryList images = new ImageEntryList();
        Array<InputFile> inputFiles = new Array<>(packModel.getInputFiles());
        inputFiles.sort(new Comparator<InputFile>() {
            @Override
            public int compare(InputFile l, InputFile r) {
                int comparison;

                comparison = l.getType().compareTo(r.getType());
                if (comparison != 0) return comparison;

                comparison = Boolean.compare(l.isRecursive(), r.isRecursive());
                if (comparison != 0) return comparison;

                comparison = Boolean.compare(l.isFlattenPaths(), r.isFlattenPaths());
                if (comparison != 0) return comparison;

                return 0;
            }
        });

        // Collect input images from directories
        for (InputFile inputFile : inputFiles) {
            if (inputFile.getType() == InputFile.Type.Input && inputFile.isDirectory()) {
                final String dirPrefix = inputFile.getDirFilePrefix() != null ? inputFile.getDirFilePrefix() : "";

                class RecursiveCollector {
                    void collectImages(FileHandle fileHandle, String prefix, boolean recursive, boolean flattenPath) {
                        FileHandle[] children = fileHandle.list((FileFilter) new SuffixFileFilter(new String[]{".png", ".jpg", "jpeg"}));
                        for (FileHandle child : children) {
                            String name = child.nameWithoutExtension();
                            name = prefix + name;
                            images.add(new ImageEntry(child, name));
                        }

                        if (recursive) {
                            FileHandle[] subDirs = fileHandle.list((FileFilter) DirectoryFileFilter.DIRECTORY);
                            for (FileHandle subDir : subDirs) {
                                String nextPrefix = prefix;
                                if (!flattenPath) {
                                    nextPrefix += subDir.name() + "/";
                                }
                                collectImages(subDir, nextPrefix, recursive, flattenPath);
                            }
                        }
                    }
                }
                new RecursiveCollector().collectImages(inputFile.getFileHandle(), dirPrefix, inputFile.isRecursive(), inputFile.isFlattenPaths());
            }
        }

        // Collect all singular input images
        for (InputFile inputFile : inputFiles) {
            if (inputFile.getType() == InputFile.Type.Input && !inputFile.isDirectory()) {
                FileHandle fileHandle = inputFile.getFileHandle();
                String name = fileHandle.nameWithoutExtension();

                String regionName = inputFile.getRegionName();
                if (Strings.isNotEmpty(regionName)) {
                    name = regionName;
                }

                ImageEntry imageEntry = new ImageEntry(fileHandle, name);
                if (inputFile.isProgrammaticNinePatch()) {
                    imageEntry.setNinePatch(inputFile.getNinePatchProps());
                }
                images.add(imageEntry);
            }
        }

        // Exclude all ignore entries
        for (InputFile inputFile : inputFiles) {
            if (inputFile.getType() != InputFile.Type.Ignore || inputFile.isDirectory()) continue;
            images.remove(new ImageEntry(inputFile.getFileHandle(), null));
        }

        return images.getAsArray();
    }

    private static void deleteOldFiles(PackModel packModel) throws Exception {
        String filename = obtainFilename(packModel);

        TexturePacker.Settings settings = packModel.getSettings();
        String atlasExtension = settings.atlasExtension == null ? "" : settings.atlasExtension;
        atlasExtension = Pattern.quote(atlasExtension);

        for (int i = 0, n = settings.scale.length; i < n; i++) {
            FileProcessor deleteProcessor = new FileProcessor() {
                protected void processFile (Entry inputFile) throws Exception {
                    Files.delete(inputFile.inputFile.toPath());
                }
            };
            deleteProcessor.setRecursive(false);

            String scaledPackFileName = settings.getScaledPackFileName(filename, i);
            File packFile = new File(scaledPackFileName);

            String prefix = filename;
            int dotIndex = prefix.lastIndexOf('.');
            if (dotIndex != -1) prefix = prefix.substring(0, dotIndex);
            deleteProcessor.addInputRegex("(?i)" + prefix + "\\d*\\.(png|jpg|jpeg|ktx|zktx)");
            deleteProcessor.addInputRegex("(?i)" + prefix + atlasExtension);

            File outputRoot = new File(packModel.getOutputDir());
            String dir = packFile.getParent();
            if (dir == null)
                deleteProcessor.process(outputRoot, null);
            else if (new File(outputRoot + "/" + dir).exists()) //
                deleteProcessor.process(outputRoot + "/" + dir, null);
        }
    }

    private static String obtainFilename(PackModel packModel) {
        String filename = packModel.getCanonicalFilename();
        if (filename.lastIndexOf(".") > -1) {
            String extension = filename.substring(filename.lastIndexOf("."));
            filename = filename.substring(0, filename.lastIndexOf("."));
            packModel.getSettings().atlasExtension = extension;
        } else {
            packModel.getSettings().atlasExtension = "";
        }
        return filename;
    }

    private static BufferedImage createBufferedImage (FileHandle fileHandle) {
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
        final FileHandle fileHandle;
        final String name;

        // Nine patch related fields
        boolean ninePatch = false;
        int[] splits, pads;

        public ImageEntry(FileHandle fileHandle, String name) {
            this.fileHandle = fileHandle;
            this.name = name;
        }

        public void setNinePatch(InputFile.NinePatchProps npp) {
            setNinePatch(npp.left, npp.right, npp.top, npp.bottom, npp.padLeft, npp.padRight, npp.padTop, npp.padBottom);
        }
        public void setNinePatch(int left, int right, int top, int bottom, int padLeft, int padRight, int padTop, int padBottom) {
            ninePatch = true;
            splits = new int[]{left, right, top, bottom};
            pads = new int[]{padLeft, padRight, padTop, padBottom};
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageEntry that = (ImageEntry) o;

            return fileHandle.equals(that.fileHandle);
        }

        @Override
        public int hashCode() {
            return fileHandle.hashCode();
        }
    }

    private static class ImageEntryList {
        private final ObjectSet<ImageEntry> imageSet = new ObjectSet<>();

        public boolean add(ImageEntry image) {
            if (imageSet.contains(image)) {
                imageSet.remove(image);
                System.out.println("File: " + image.fileHandle + " is listed twice (last added configuration will be used)");
            }
            return imageSet.add(image);
        }

        public boolean remove(ImageEntry image) {
            return imageSet.remove(image);
        }

        public boolean contains(ImageEntry image) {
            return imageSet.contains(image);
        }

        public Array<ImageEntry> getAsArray() {
            Array<ImageEntry> result = new Array<>();
            for (ImageEntry image : imageSet) {
                result.add(image);
            }
            return result;
        }
    }
}
