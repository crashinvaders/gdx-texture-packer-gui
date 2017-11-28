package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;

public class ProjectPropertyChangedEvent {

    private final ProjectModel project;
    private final Property property;

    public ProjectPropertyChangedEvent(ProjectModel project, Property property) {
        this.project = project;
        this.property = property;
    }

    public ProjectModel getProject() {
        return project;
    }

    public Property getProperty() {
        return property;
    }

    public enum Property {
        SELECTED_PACK,
        PACKS,
        FILE_TYPE,
        PREVIEW_BG_COLOR,
    }
}
