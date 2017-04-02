
package com.crashinvaders.texturepackergui.services.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent.Property;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.kiwi.util.common.Strings;

import java.io.File;
import java.util.Arrays;

public class PackModel {
    private static final String TAG = PackModel.class.getSimpleName();

//    private final SourceFileSet sourceFileSet = new SourceFileSet();
    private final Array<ScaleFactorModel> scaleFactors = new Array<>();
    private final Array<InputFile> sourceFiles = new Array<>();
    private Settings settings;
    private String name = "";
    private String filename = "";
    private String inputDir = "";
    private String outputDir = "";

    private EventDispatcher eventDispatcher;

    public PackModel() {
        settings = new Settings();
        settings.maxWidth = 2048; // Default settings.maxWidth value (1024) is outdated and 2048 is recommended
        settings.maxHeight = 2048; // Default settings.maxHeight value (1024) is outdated and 2048 is recommended

        scaleFactors.add(new ScaleFactorModel("", 1f));
    }

    public PackModel(PackModel pack) {
        settings = new Settings(pack.settings);
        //TODO remove this when LibGDX will apply fix for Settings
        settings.scale = Arrays.copyOf(settings.scale, settings.scale.length);
        settings.scaleSuffix = Arrays.copyOf(settings.scaleSuffix, settings.scaleSuffix.length);

        this.name = pack.name;
        this.filename = pack.filename;
        this.inputDir = pack.inputDir;
        this.outputDir = pack.outputDir;

        scaleFactors.addAll(pack.scaleFactors);
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void setName(String name) {
        if (Strings.equals(this.name, name)) return;

        this.name = name;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.NAME));
        }
    }

    public void setFilename(String filename) {
        if (Strings.equals(this.filename, filename)) return;

        this.filename = filename;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.FILENAME));
        }
    }

    public void setInputDir(String inputDir) {
        if (Strings.equals(this.inputDir, inputDir)) return;

        this.inputDir = inputDir;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.INPUT));
        }
    }

    public void setOutputDir(String outputDir) {
        if (Strings.equals(this.outputDir, outputDir)) return;

        this.outputDir = outputDir;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.OUTPUT));
        }
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getInputDir() {
        return inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

//    public SourceFileSet getSourceFileSet() {
//        return sourceFileSet;
//    }

    public Array<InputFile> getSourceFiles() {
        return sourceFiles;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        //TODO use Settings#set(Settings) when it will be available in the next version
        this.settings = new Settings(settings);
        //TODO remove this when LibGDX will apply fix for Settings
        this.settings.scale = Arrays.copyOf(settings.scale, settings.scale.length);
        this.settings.scaleSuffix = Arrays.copyOf(settings.scaleSuffix, settings.scaleSuffix.length);
    }

    public String getCanonicalName() {
        return name.trim().isEmpty() ? "unnamed" : name;
    }

    public String getCanonicalFilename() {
        if (!filename.trim().isEmpty()) {
            String name = filename;
            String extension = "";
            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex != -1 && dotIndex != filename.length()-1) {
                name = filename.substring(0, dotIndex);
                extension = filename.substring(dotIndex, filename.length());
            }
            return name + scaleFactors.first().getSuffix() + extension;
        } else {
            return getCanonicalName() + scaleFactors.first().getSuffix() + settings.atlasExtension;
        }
    }

    /**
     * @return may be null
     */
    public String getAtlasPath() {
        String atlasPath = null;
        if (outputDir != null && !outputDir.trim().isEmpty()) {
            String filename = getCanonicalFilename();
            atlasPath = outputDir + File.separator + filename;
        }
        return atlasPath;
    }

    public Array<ScaleFactorModel> getScaleFactors() {
        return scaleFactors;
    }

    public void setScaleFactors(Array<ScaleFactorModel> scaleFactors) {
        this.scaleFactors.clear();
        this.scaleFactors.addAll(scaleFactors);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.SCALE_FACTORS));
        }
    }

    public void addSourceFile(FileHandle fileHandle, InputFile.Type type) {
        addSourceFile(new InputFile(fileHandle, type));
    }
    public void addSourceFile(InputFile inputFile) {
        if (sourceFiles.contains(inputFile, false)) {
            Gdx.app.error(TAG, "File: " + inputFile + " is already added");
            return;
        }
        if (inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Ignore) {
            Gdx.app.error(TAG, "File: " + inputFile + " is a directory. Ignore files cannot be directories.");
            return;
        }
        sourceFiles.add(inputFile);
        inputFile.setEventDispatcher(eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(PackModel.this, Property.INPUT_FILE_ADDED)
                    .setInputFile(inputFile));
        }
    }

    public void removeSourceFile(final FileHandle fileHandle, final InputFile.Type type) {
        removeSourceFile(new InputFile(fileHandle, type));
    }
    public void removeSourceFile(InputFile inputFile) {
        if (!sourceFiles.contains(inputFile, false)) {
            Gdx.app.error(TAG, "File: " + inputFile + " wasn't added");
            return;
        }
        sourceFiles.removeValue(inputFile, false);
        inputFile.setEventDispatcher(null);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(PackModel.this, Property.INPUT_FILE_REMOVED)
                    .setInputFile(inputFile));
        }
    }

    @Override
    public String toString() {
        return name;
    }

}