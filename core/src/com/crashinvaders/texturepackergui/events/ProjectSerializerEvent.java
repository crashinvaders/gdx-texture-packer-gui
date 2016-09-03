package com.crashinvaders.texturepackergui.events;

import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;

public class ProjectSerializerEvent {

    private final Action action;
    private final ProjectModel project;
    private final FileHandle file;

    public ProjectSerializerEvent(Action action, ProjectModel project, FileHandle file) {
        this.action = action;
        this.project = project;
        this.file = file;
    }

    public Action getAction() {
        return action;
    }

    public ProjectModel getProject() {
        return project;
    }

    public FileHandle getFile() {
        return file;
    }

    public enum Action { SAVED, LOADED }
}
