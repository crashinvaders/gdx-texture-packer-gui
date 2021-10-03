package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.texturepacker.PageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.crashinvaders.texturepackergui.controllers.model.InputFile;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectSettingsModel;
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

        ProjectSettingsModel projSettings = node.getProject().getSettings();
        String settingsOrigExtension = pack.getSettings().atlasExtension;
        performPacking(projSettings, pack, pageFileWriter);
        pack.getSettings().atlasExtension = settingsOrigExtension;

        System.out.println("Packing is done");
    }
    
    private void performPacking(ProjectSettingsModel projSettings, PackModel packModel, PageFileWriter pageFileWriter) throws Exception {
        Array<ImageEntry> imageEntries = collectImageFiles(projSettings, packModel);
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

    private static Array<ImageEntry> collectImageFiles(ProjectSettingsModel projSettings, PackModel packModel) {
        final ImageEntryList images = new ImageEntryList();
        Array<InputFile> inputFiles = new Array<>(packModel.getInputFiles());
        inputFiles.sort(new Comparator<InputFile>() {
            @Override
            public int compare(InputFile l, InputFile r) {
                int comparison = 0;

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
                if (!inputFile.getFileHandle().exists()) {
                    System.out.println(String.format(
                            "WARNING: Input directory doesn't exist: \"%s\"",
                            inputFile.getFileHandle().path()));
                    continue;
                }

                final String dirPrefix = inputFile.getDirFilePrefix() != null ? inputFile.getDirFilePrefix() : "";

                class RecursiveCollector {
                    void collectImages(FileHandle fileHandle, String prefix, boolean recursive, boolean flattenPath) {
                        FileHandle[] children = fileHandle.list((FileFilter) new SuffixFileFilter(new String[]{".png", ".jpg", "jpeg"}));
                        for (FileHandle child : children) {
                            String name = InputFile.evalDefaultRegionName(child, packModel.isKeepInputFileExtensions());
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

                String regionName;
                if (Strings.isNotEmpty(inputFile.getRegionName())) {
                    regionName = inputFile.getRegionName();
                } else {
                    regionName = InputFile.evalDefaultRegionName(
                            inputFile.getFileHandle(),
                            packModel.isKeepInputFileExtensions());
                }

                ImageEntry imageEntry = new ImageEntry(fileHandle, regionName);
                if (inputFile.isProgrammaticNinePatch()) {
                    imageEntry.setNinePatch(inputFile.getNinePatchProps());
                }
                images.add(imageEntry);
            }
        }

        // Exclude all ignore entries
        for (InputFile inputFile : inputFiles) {
            if (inputFile.getType() != InputFile.Type.Ignore || inputFile.isDirectory()) continue;
            FileHandle fileHandle = inputFile.getFileHandle();
            // Remove input files depending both on file handle and image entry matches.
            images.remove(fileHandle);
            images.remove(new ImageEntry(fileHandle, fileHandle.nameWithoutExtension()));
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
            deleteProcessor.addInputRegex("(?i)" + prefix + "\\d*\\.(png|jpg|jpeg|ktx|zktx|basis)");
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

        /** The result name of the region in atlas. */
        final String regionName;

        //BEWARE: Programmatic only 9-patch related fields!
        boolean ninePatch = false;
        int[] splits, pads;

        public ImageEntry(FileHandle fileHandle, String name) {
            this.fileHandle = fileHandle;
            this.name = name;

            if (name.endsWith(".9")) {
                regionName = name.substring(0, name.length() - 2);
            } else {
                regionName = name;
            }
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

            return regionName.equals(that.regionName);
        }

        @Override
        public int hashCode() {
            return regionName.hashCode();
        }
    }

    private static class ImageEntryList {
        private final ObjectSet<ImageEntry> imageSet = new ObjectSet<>();

        public boolean add(ImageEntry image) {
            if (!image.fileHandle.exists()) {
                System.out.println(String.format(
                        "WARNING: Input file doesn't exist: \"%s\"",
                        image.fileHandle.path()));
                return false;
            }

            if (imageSet.contains(image)) {
                imageSet.remove(image);
                System.out.println(String.format(
                        "WARNING: Region: \"%s\" is listed twice. The last added configuration will be used - \"%s\"",
                        image.regionName,
                        image.fileHandle.path()));
            }
            return imageSet.add(image);
        }

        public boolean remove(ImageEntry image) {
            return imageSet.remove(image);
        }

        public boolean remove(FileHandle fileHandle) {
            boolean removed = false;
            ObjectSet.ObjectSetIterator<ImageEntry> iterator = imageSet.iterator();
            while (iterator.hasNext) {
                ImageEntry imageEntry = iterator.next();
                if (imageEntry.fileHandle.equals(fileHandle)) {
                    iterator.remove();
                    removed = true;
                }
            }
            return removed;
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
