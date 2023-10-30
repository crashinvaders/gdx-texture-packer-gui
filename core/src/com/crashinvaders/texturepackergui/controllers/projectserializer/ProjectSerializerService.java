package com.crashinvaders.texturepackergui.controllers.projectserializer;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SerializationException;
import com.crashinvaders.common.ApplicationLoggerWrapper;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.controllers.model.*;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

import static com.crashinvaders.texturepackergui.utils.CommonUtils.fetchMessageStack;
import static com.crashinvaders.texturepackergui.utils.CommonUtils.splitAndTrim;

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
        // Wrap orig logger in order to display non-critical error messages as warning notifications.
        ApplicationLogger origLogger = Gdx.app.getApplicationLogger();
        Gdx.app.setApplicationLogger(new ErrorNotificationLogger(eventDispatcher, origLogger));
        try {
            project = ProjectSerializer.loadProject(file);
        } catch (SerializationException e) {
            Gdx.app.setApplicationLogger(origLogger);
            Gdx.app.error(TAG, "Error during project loading.", e);
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(localeService.getI18nBundle().format("toastProjectLoadError", file.path()))
                    .duration(ShowToastEvent.DURATION_LONG)
                    .click(() -> ErrorDialogController.show(e))
            );
            return null;
        } finally {
            Gdx.app.setApplicationLogger(origLogger);
        }

        eventDispatcher.postEvent(new ProjectSerializerEvent(ProjectSerializerEvent.Action.LOADED, project, file));
        return project;
    }

    /** Displays error messages as notifications on UI. */
    private static class ErrorNotificationLogger extends ApplicationLoggerWrapper {

        private final EventDispatcher eventDispatcher;

        public ErrorNotificationLogger(EventDispatcher eventDispatcher, ApplicationLogger wrapped) {
            super(wrapped);
            this.eventDispatcher = eventDispatcher;
        }

        @Override
        public void error(String tag, String message) {
            super.error(tag, message);

            displayWarningNotification(message);
        }

        @Override
        public void error(String tag, String message, Throwable exception) {
            super.error(tag, message, exception);

            displayWarningNotification(message);
        }

        private void displayWarningNotification(String message) {
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(message)
                    .duration(ShowToastEvent.DURATION_LONG)
            );
        }
    }
}
