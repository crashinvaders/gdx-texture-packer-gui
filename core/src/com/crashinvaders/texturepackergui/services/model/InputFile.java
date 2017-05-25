package com.crashinvaders.texturepackergui.services.model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.texturepackergui.controllers.ninepatcheditor.NinePatchEditorModel;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

public class InputFile {
    private final Type type;
    private final FileHandle fileHandle;
    private final boolean directory;

    private EventDispatcher eventDispatcher;

    // Input directory fields
    private String dirFilePrefix;

    // Input file fields
    private String regionName;
    private final NinePatchProps ninePatchProps = new NinePatchProps();

    public InputFile(FileHandle fileHandle, Type type) {
        this.fileHandle = fileHandle;
        this.type = type;
        this.directory = fileHandle.isDirectory();
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

    public void setNinePatch(boolean ninePatch) {
        if (this.ninePatchProps.active == ninePatch) return;

        this.ninePatchProps.active = ninePatch;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public boolean isNinePatch() {
        return ninePatchProps.active;
    }

    @Override
    public String toString() {
        return fileHandle.toString();
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

    public static class NinePatchProps {
        public int left, right, top, bottom;
        public int padLeft = -1, padRight = -1, padTop = -1, padBottom = -1;

        boolean active = false;  // If true, this file will be treated as predefined ninepatch
    }
}
