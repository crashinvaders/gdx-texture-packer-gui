
package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.common.statehash.StateHashable;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent.Property;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.kiwi.util.common.Strings;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class PackModel implements StateHashable {
    private static final String TAG = PackModel.class.getSimpleName();

    private final Array<ScaleFactorModel> scaleFactors = new Array<>();
    private final Array<InputFile> inputFiles = new Array<>();
    private final Settings settings = new Settings();
    private String name = "";
    private String filename = "";
    private String outputDir = "";
    private boolean keepInputFileExtensions = false;

    private EventDispatcher eventDispatcher;

    public PackModel() {
        scaleFactors.add(new ScaleFactorModel("", 1f, TexturePacker.Resampling.bicubic));
    }

    public PackModel(PackModel pack) {
        settings.set(pack.settings);

        this.name = pack.name;
        this.filename = pack.filename;
        this.outputDir = pack.outputDir;
        this.keepInputFileExtensions = pack.keepInputFileExtensions;

        scaleFactors.addAll(pack.scaleFactors);

        inputFiles.addAll(pack.inputFiles);
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;

        for (InputFile inputFile : inputFiles) {
            inputFile.setEventDispatcher(eventDispatcher);
        }
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

    public void setOutputDir(String outputDir) {
        if (Strings.equals(this.outputDir, outputDir)) return;

        this.outputDir = outputDir;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.OUTPUT));
        }
    }

    public void setKeepInputFileExtensions(boolean value) {
        if (this.keepInputFileExtensions == value) return;

        this.keepInputFileExtensions = value;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.KEEP_FILE_EXTENSIONS));
        }
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public Array<InputFile> getInputFiles() {
        return inputFiles;
    }

    public boolean isKeepInputFileExtensions() {
        return keepInputFileExtensions;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings.set(settings);
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

    public void addInputFile(FileHandle fileHandle, InputFile.Type type) {
        addInputFile(new InputFile(fileHandle, type));
    }
    public void addInputFile(InputFile inputFile) {
        if (inputFiles.contains(inputFile, false)) {
            Gdx.app.error(TAG, "File: " + inputFile + " is already added");
            return;
        }
        if (inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Ignore) {
            Gdx.app.error(TAG, "File: " + inputFile + " is a directory. Ignore files cannot be directories.");
            return;
        }
        inputFiles.add(inputFile);
        inputFile.setEventDispatcher(eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(PackModel.this, Property.INPUT_FILE_ADDED)
                    .setInputFile(inputFile));
        }
    }

    public void removeInputFile(final FileHandle fileHandle, final InputFile.Type type) {
        removeInputFile(new InputFile(fileHandle, type));
    }
    public void removeInputFile(InputFile inputFile) {
        // Since we use equals and not == operator to compare InputFile values,
        // we have to find real value and don't use reference from parameter.
        int index = inputFiles.indexOf(inputFile, false);
        InputFile actualInputFile = index >= 0 ? inputFiles.get(index) : null;

        if (actualInputFile == null) {
            Gdx.app.error(TAG, "File: " + inputFile + " wasn't added");
            return;
        }
        inputFiles.removeValue(actualInputFile, false);
        actualInputFile.setEventDispatcher(null);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(PackModel.this, Property.INPUT_FILE_REMOVED)
                    .setInputFile(actualInputFile));
        }
    }

    /**
     * Manually fires an event about data change for any of {@link #settings} fields.
     */
    public void fireSettingsChangedEvent() {
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.SETTINGS));
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int computeStateHash() {
        int settingsHash = computeSettingsStateHash(settings);
        return StateHashUtils.computeHash(scaleFactors, inputFiles, settingsHash, name, filename, outputDir, keepInputFileExtensions);
    }

    private static int computeSettingsStateHash(Settings s) {
        int result = Objects.hash(s.pot, s.multipleOfFour, s.paddingX, s.paddingY, s.edgePadding, s.duplicatePadding,
                s.rotation, s.minWidth, s.minHeight, s.maxWidth, s.maxHeight,
                s.square, s.stripWhitespaceX, s.stripWhitespaceY, s.alphaThreshold,
                s.filterMin, s.filterMag, s.wrapX, s.wrapY, s.format, s.alias, s.ignoreBlankImages,
                s.fast, s.debug, s.silent, s.combineSubdirectories, s.ignore, s.flattenPaths,
                s.premultiplyAlpha, s.useIndexes, s.bleed, s.bleedIterations, s.limitMemory,
                s.grid, s.atlasExtension, s.prettyPrint, s.legacyOutput);
        result = 31 * result + Arrays.hashCode(s.scale);
        result = 31 * result + Arrays.hashCode(s.scaleSuffix);
        result = 31 * result + Arrays.hashCode(s.scaleResampling);
        return result;
    }
}