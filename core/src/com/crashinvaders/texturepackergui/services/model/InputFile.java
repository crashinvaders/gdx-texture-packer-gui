package com.crashinvaders.texturepackergui.services.model;

import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

public class InputFile {
    private final Type type;
    private FileHandle fileHandle;

    private EventDispatcher eventDispatcher;

    // Input directory fields
    private String dirFilePrefix;

    // Input file fields
    private String fileNameOverwrite;


    public InputFile(FileHandle fileHandle, Type type) {
        this.fileHandle = fileHandle;
        this.type = type;
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public FileHandle getFileHandle() {
        return fileHandle;
    }

    public void setFileHandle(FileHandle fileHandle) {
        this.fileHandle = fileHandle;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public Type getType() {
        return type;
    }

    public boolean isDirectory() {
        return fileHandle.isDirectory();
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

    public String getFileNameOverwrite() {
        return fileNameOverwrite;
    }

    public void setFileNameOverwrite(String fileNameOverwrite) {
        this.fileNameOverwrite = fileNameOverwrite;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new InputFilePropertyChangedEvent(this));
        }
    }

    public enum Type {
        Input, Ignore
    }

    @Override
    public String toString() {
        return fileHandle.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InputFile that = (InputFile) o;

        if (type != that.type) return false;
        return fileHandle != null ? fileHandle.equals(that.fileHandle) : that.fileHandle == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (fileHandle != null ? fileHandle.hashCode() : 0);
        return result;
    }
}
