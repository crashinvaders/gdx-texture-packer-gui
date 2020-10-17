package com.crashinvaders.texturepackergui.controllers.model;

import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;

public class ModelService {

    @Inject EventDispatcher eventDispatcher;

    private ProjectModel projectModel;
    private int lastProjectStateHash;

    @Initiate(priority = 128)
    public void init() {
        setProject(new ProjectModel());
    }

    @OnEvent(ProjectSerializerEvent.class) void onEvent(ProjectSerializerEvent event) {
        if (event.getAction() == ProjectSerializerEvent.Action.SAVED) {
            updateProjectStateHash();
        }
    }

    public ProjectModel getProject() {
        return projectModel;
    }

    public void setProject(ProjectModel projectModel) {
        if (projectModel == null) throw new NullPointerException("projectModel cannot be null");
        if (this.projectModel == projectModel) return;

        if (this.projectModel != null) {
            this.projectModel.setEventDispatcher(null);
        }

        projectModel.setEventDispatcher(eventDispatcher);
        this.projectModel = projectModel;

        updateProjectStateHash();

        eventDispatcher.postEvent(new ProjectInitializedEvent(this, projectModel));

        // Assign first available pack as selected
        if (projectModel.getPacks().size > 0) {
            projectModel.setSelectedPack(projectModel.getPacks().first());
        }
    }

    public boolean hasProjectChanges() {
        return lastProjectStateHash != projectModel.computeStateHash();
    }

    private void updateProjectStateHash() {
        this.lastProjectStateHash = projectModel.computeStateHash();
    }
}
