package com.crashinvaders.texturepackergui.services.model;

import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;

public class ModelService {
    private ProjectModel projectModel;

    @Inject EventDispatcher eventDispatcher;

    @Initiate(priority = 128)
    public void init() {
        setProject(new ProjectModel());
    }

    public ProjectModel getProject() {
        return projectModel;
    }

    public void setProject(ProjectModel projectModel) {
        if (projectModel == null) throw new NullPointerException("projectModel cannot be null");
        if (this.projectModel == projectModel) return;

        projectModel.setEventDispatcher(eventDispatcher);
        this.projectModel = projectModel;

        eventDispatcher.postEvent(new ProjectInitializedEvent(this, projectModel));

        // Assign first available pack as selected
        if (projectModel.getPacks().size > 0) {
            projectModel.setSelectedPack(projectModel.getPacks().first());
        }
    }
}
