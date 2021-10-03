package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.common.statehash.StateHashable;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.kiwi.util.common.Strings;

public class InputFile implements StateHashable {
    private final Type type;
    private final FileHandle fileHandle;
    private final boolean directory;

    private EventDispatcher eventDispatcher;

    // Input directory fields
    private String dirFilePrefix;
    private boolean recursive;
    private boolean flattenPaths;

    // Input file fields
    private String regionName;
    private final NinePatchProps ninePatchProps = new NinePatchProps();

    public InputFile(FileHandle fileHandle, Type type) {
        this.fileHandle = fileHandle;
        this.type = type;

        // Determine if the file is a directory.
        // We cannot use just `fileHandle.isDirectory()`,
        // because in case the file doesn't exist it always returns `false`.
        if (fileHandle.exists()) {
            this.directory = fileHandle.isDirectory();
        } else {
            this.directory = fileHandle.extension().length() == 0;
        }
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public FileHandle getFileHandle() {
        return fileHandle;
    }

    public Type getType() {
        return type;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getDirFilePrefix() {
        return dirFilePrefix;
    }

    public void setDirFilePrefix(String dirFilePrefix) {
        this.dirFilePrefix = dirFilePrefix;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        if (this.recursive == recursive) return;

        this.recursive = recursive;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public boolean isFlattenPaths() {
        return flattenPaths;
    }

    public void setFlattenPaths(boolean flattenPaths) {
        if (this.flattenPaths == flattenPaths) return;

        this.flattenPaths = flattenPaths;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public NinePatchProps getNinePatchProps() {
        return ninePatchProps;
    }

    public void setProgrammaticNinePatch(boolean ninePatch) {
        if (this.ninePatchProps.active == ninePatch) return;

        this.ninePatchProps.active = ninePatch;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public boolean isProgrammaticNinePatch() {
        return ninePatchProps.active;
    }

    public boolean isFileBasedNinePatch() {
            return fileHandle.name().endsWith(".9.png");
    }

    public boolean isNinePatch() {
        return isProgrammaticNinePatch() || isFileBasedNinePatch();
    }

    public static String evalDefaultRegionName(FileHandle fileHandle, boolean keepExtension) {
        String result = fileHandle.nameWithoutExtension();
        if (result.endsWith(".9")) {
            result = result.substring(0, result.length() - 2);
        }
        if (keepExtension) {
            result += "." + fileHandle.extension();
        }
        return result;
    }

    @Override
    public String toString() {
        return fileHandle.toString();
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(type, fileHandle, directory, dirFilePrefix,
                recursive, flattenPaths, regionName, ninePatchProps);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InputFile inputFile = (InputFile) o;

        return fileHandle.equals(inputFile.fileHandle);
    }

    @Override
    public int hashCode() {
        return fileHandle.hashCode();
    }

    public enum Type {
        Input, Ignore
    }

    public static class NinePatchProps implements StateHashable {
        public int left, right, top, bottom;
        public int padLeft = -1, padRight = -1, padTop = -1, padBottom = -1;

        boolean active = false;  // If true, this file will be treated as predefined ninepatch

        @Override
        public int computeStateHash() {
            return StateHashUtils.computeHash(left, right, top, bottom, padLeft, padRight, padTop, padBottom, active);
        }
    }
}
