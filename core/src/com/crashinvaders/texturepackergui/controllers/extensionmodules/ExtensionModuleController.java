package com.crashinvaders.texturepackergui.controllers.extensionmodules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.ExtensionModuleActivationChangedEvent;
import com.crashinvaders.texturepackergui.events.ExtensionModuleStatusChangedEvent;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

public abstract class ExtensionModuleController {

    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;

    private final Preferences prefsInstalledModules = Gdx.app.getPreferences(AppConstants.PREF_NAME_INSTALLED_MODULES);

    private final String moduleId;
    private final int requiredRevision;
    private final String keyName;
    private final String keyDesc;
    private Status status = Status.NOT_INSTALLED;
    // Means module was initialized on application startup
    private boolean activated = false;

    public ExtensionModuleController(String moduleId, int requiredRevision, String keyName, String keyDesc) {
        this.moduleId = moduleId;
        this.requiredRevision = requiredRevision;
        this.keyName = keyName;
        this.keyDesc = keyDesc;
    }

    public String getModuleId() {
        return moduleId;
    }

    public int getRequiredRevision() {
        return requiredRevision;
    }

    public String getName() {
        return localeService.getI18nBundle().get(keyName);
    }

    public String getDescription() {
        return localeService.getI18nBundle().get(keyDesc);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isActivated() {
        return activated;
    }

    public FileHandle getModuleDir() {
        return Gdx.files.external(AppConstants.MODULES_DIR + "/" + moduleId);
    }

    void setStatus(Status status, boolean notify) {
        if (this.status == status) return;
        this.status = status;
        if (notify) {
            eventDispatcher.postEvent(new ExtensionModuleStatusChangedEvent(this));
        }
    }

    void setActivated(boolean activated, boolean notify) {
        if (this.activated == activated) return;
        this.activated = activated;
        if (notify) {
            eventDispatcher.postEvent(new ExtensionModuleActivationChangedEvent(this));
        }
    }

    abstract void prepareInstallationJob(JobTaskQueue taskQueue, String fileUrl);
    abstract void prepareUninstallationJob(JobTaskQueue taskQueue);

    /** If the module is registered as installed, this method should check
     * if it's valid (check file integrity, specific conditions, etc). */
    abstract boolean validateInstalledModule();

    public enum Status {
        NOT_INSTALLED, INSTALLED, UPDATE_REQUIRED
    }
}
