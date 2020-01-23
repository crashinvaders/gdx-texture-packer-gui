package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;

public class ProjectInitializedEvent {

    private final ModelService model;
    private final ProjectModel projectModel;

    public ProjectInitializedEvent(ModelService model, ProjectModel projectModel) {
        this.model = model;
        this.projectModel = projectModel;
    }

    public ModelService getModel() {
        return model;
    }

    public ProjectModel getProject() {
        return projectModel;
    }
}
