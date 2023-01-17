package com.crashinvaders.texturepackergui.controllers.projectserializer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.crashinvaders.common.Version;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.model.filetype.*;
import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.controllers.model.*;
import com.crashinvaders.texturepackergui.utils.PathUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static com.crashinvaders.texturepackergui.utils.CommonUtils.splitAndTrim;
import static com.crashinvaders.texturepackergui.utils.FileUtils.loadTextFromFile;
import static com.crashinvaders.texturepackergui.utils.FileUtils.saveTextToFile;

@Component
public class ProjectSerializerService {
    private static final String TAG = ProjectSerializerService.class.getSimpleName();

    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;

    public void saveProject(ProjectModel project, FileHandle file) {
        try {
            ProjectSerializer.saveProject(project, file);
        } catch (SerializationException e) {
            Gdx.app.error(TAG, "Error during project saving.", e);
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(localeService.getI18nBundle().format("toastProjectSaveError", project.getProjectFile().path()))
                    .duration(ShowToastEvent.DURATION_LONG)
                    .click(() -> ErrorDialogController.show(e))
            );
            return;
        }

        eventDispatcher.postEvent(new ProjectSerializerEvent(ProjectSerializerEvent.Action.SAVED, project, file));
    }

    public ProjectModel loadProject(FileHandle file) {
        final ProjectModel project;
        try {
            project = ProjectSerializer.loadProject(file);
        } catch (SerializationException e) {
            Gdx.app.error(TAG, "Error during project loading.", e);
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(localeService.getI18nBundle().format("toastProjectLoadError", file.path()))
                    .duration(ShowToastEvent.DURATION_LONG)
                    .click(() -> ErrorDialogController.show(e))
            );
            return null;
        }

        eventDispatcher.postEvent(new ProjectSerializerEvent(ProjectSerializerEvent.Action.LOADED, project, file));
        return project;
    }
}
