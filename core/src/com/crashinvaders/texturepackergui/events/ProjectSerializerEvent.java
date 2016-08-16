package com.crashinvaders.texturepackergui.events;

import com.badlogic.gdx.files.FileHandle;

public class ProjectSerializerEvent {

    private final Action action;
    private final FileHandle file;

    public ProjectSerializerEvent(Action action, FileHandle file) {
        this.action = action;
        this.file = file;
    }

    public Action getAction() {
        return action;
    }

    public FileHandle getFile() {
        return file;
    }

    public enum Action { SAVED, LOADED }
}
