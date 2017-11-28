package com.crashinvaders.texturepackergui.controllers.extensionmodules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.crashinvaders.common.async.JobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.common.scene2d.visui.Toast;
import com.crashinvaders.common.scene2d.visui.ToastTable;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.ModalTaskDialogController;
import com.crashinvaders.texturepackergui.events.RemoveToastEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.controllers.extensionmodules.ExtensionModuleController.Status;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

@Component
public class ExtensionModuleManagerService {
    private static final String TAG = ExtensionModuleManagerService.class.getSimpleName();
    private static final String BASE_URL = "https://crashinvaders.github.io/gdx-texture-packer-gui/modules/";

    @Inject EventDispatcher eventDispatcher;
    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject ModalTaskDialogController moduleTaskDialog;
    @Inject ErrorDialogController errorDialog;
    @Inject ExtensionModuleRepositoryService moduleRepository;

    private final Preferences prefsInstalledModules = Gdx.app.getPreferences(AppConstants.PREF_NAME_INSTALLED_MODULES);
    private final ArrayMap<String, ExtensionModuleController> moduleControllers = new ArrayMap<>();

    private Toast prevRestartToast;

    @Initiate(priority = AutumnActionPriority.TOP_PRIORITY)
    void initModuleControllers(CjkFontExtensionModule jcfFont) {
        moduleControllers.put(jcfFont.getModuleId(), jcfFont);

        for (int i = 0; i < moduleControllers.size; i++) {
            ExtensionModuleController module = moduleControllers.getValueAt(i);
            int installedRevision = prefsInstalledModules.getInteger(module.getModuleId(), -1);
            if (installedRevision >= 0) {
                if (installedRevision == module.getRequiredRevision()) {
                    module.setStatus(Status.INSTALLED, false);
                    module.setActivated(true, false);
                } else {
                    module.setStatus(Status.UPDATE_REQUIRED, false);
                }
            }
        }
    }

//    @OnEvent(ModuleRepositoryRefreshEvent.class) void onEvent(ModuleRepositoryRefreshEvent event) {
//        // Check if repository was updated
//        if (event.getAction() == ModuleRepositoryRefreshEvent.Action.FINISHED_SUCCESS) {
//
//        }
//    }

    public Array<ExtensionModuleController> getModuleControllers() {
        Array<ExtensionModuleController> result = new Array<>();
        for (int i = 0; i < moduleControllers.size; i++) {
            result.add(moduleControllers.getValueAt(i));
        }
        return result;
    }

    public ExtensionModuleController findModuleController(String moduleId) {
        return moduleControllers.get(moduleId);
    }

    public void installModule(final String moduleId) {
        final ExtensionModuleController moduleController = moduleControllers.get(moduleId);
        if (moduleController == null) {
            Gdx.app.error(TAG, "Module cannot be installed",
                    new IllegalArgumentException("There is no ExtensionModuleController with such id: " + moduleId));
            return;
        }
        if (moduleController.getStatus() != Status.NOT_INSTALLED) {
            Gdx.app.error(TAG, "Module cannot be installed",
                    new IllegalStateException("Module " + moduleId + " cannot be installed because it has status: " + moduleController.getStatus()));
            return;
        }
        final int requiredRevision = moduleController.getRequiredRevision();
        RepositoryModuleData.Revision revisionData = moduleRepository.findRevision(moduleId, requiredRevision);
        if (revisionData == null) {
            Gdx.app.error(TAG, "Module cannot be installed",
                    new IllegalStateException("Module repository doesn't have required revision: " + moduleId + " " + requiredRevision));
            return;
        }

        ModalTaskDialogController.DialogData dialogData = new ModalTaskDialogController.DialogData();
        dialogData.message(getString("emTaskInstalling", moduleController.getName()));
        dialogData.cancelBehavior(ModalTaskDialogController.CancelBehavior.CANCEL_BACKGROUND);
        JobTaskQueue taskQueue = dialogData.getTaskQueue();
        moduleController.prepareInstallationJob(taskQueue, moduleRepository.getRelativeUrl(revisionData.file));

        dialogData.listener(new JobTask.Listener() {
            @Override
            public void onSucceed() {
                addInstalledEntry(moduleId, requiredRevision);
                moduleController.setStatus(Status.INSTALLED, true);
                showRestartToast();
            }
            @Override
            public void onFailed(String failMessage, Exception failException) {
                errorDialog.setError(failException);
                interfaceService.showDialog(errorDialog.getClass());
            }
            @Override
            public void onCanceled() {
                // Do nothing
            }
        });

        moduleTaskDialog.showDialog(dialogData);
    }

    public void uninstallModule(final String moduleId) {
        final ExtensionModuleController moduleController = moduleControllers.get(moduleId);
        if (moduleController == null) {
            Gdx.app.error(TAG, "Module cannot be uninstalled",
                    new IllegalArgumentException("There is no ExtensionModuleController with such id: " + moduleId));
            return;
        }
        if (moduleController.getStatus() != Status.INSTALLED && moduleController.getStatus() != Status.UPDATE_REQUIRED) {
            Gdx.app.error(TAG, "Module cannot be uninstalled",
                    new IllegalStateException("Module " + moduleId + " cannot be uninstalled because it has status: " + moduleController.getStatus()));
            return;
        }

        ModalTaskDialogController.DialogData dialogData = new ModalTaskDialogController.DialogData();
        dialogData.message(getString("emTaskRemoving", moduleController.getName()));
        dialogData.cancelBehavior(ModalTaskDialogController.CancelBehavior.CANCEL_BACKGROUND);
        JobTaskQueue taskQueue = dialogData.getTaskQueue();
        moduleController.prepareUninstallationJob(taskQueue);

        dialogData.listener(new JobTask.Listener() {
            @Override
            public void onSucceed() {
                // Do nothing
            }
            @Override
            public void onFailed(String failMessage, Exception failException) {
                errorDialog.setError(failException);
                interfaceService.showDialog(errorDialog.getClass());
            }
            @Override
            public void onCanceled() {
                // Do nothing
            }
        });

        removeInstalledEntry(moduleId);
        moduleController.setStatus(Status.NOT_INSTALLED, true);
        moduleController.setActivated(false, true);
        showRestartToast();
        moduleTaskDialog.showDialog(dialogData);
    }

    public void updateModule(final String moduleId) {
        final ExtensionModuleController moduleController = moduleControllers.get(moduleId);
        if (moduleController == null) {
            Gdx.app.error(TAG, "Module cannot be updated",
                    new IllegalArgumentException("There is no ExtensionModuleController with such id: " + moduleId));
            return;
        }
        if (moduleController.getStatus() != Status.UPDATE_REQUIRED) {
            Gdx.app.error(TAG, "Module cannot be updated",
                    new IllegalStateException("Module " + moduleId + " cannot be updated because it has status: " + moduleController.getStatus()));
            return;
        }
        final int requiredRevision = moduleController.getRequiredRevision();
        RepositoryModuleData.Revision revisionData = moduleRepository.findRevision(moduleId, requiredRevision);
        if (revisionData == null) {
            Gdx.app.error(TAG, "Module cannot be updated",
                    new IllegalStateException("Module repository doesn't have required revision: " + moduleId + " " + requiredRevision));
            return;
        }

        ModalTaskDialogController.DialogData dialogData = new ModalTaskDialogController.DialogData();
        dialogData.message(getString("emTaskUpdating", moduleController.getName()));
        dialogData.cancelBehavior(ModalTaskDialogController.CancelBehavior.CANCEL_BACKGROUND);
        JobTaskQueue taskQueue = dialogData.getTaskQueue();
        moduleController.prepareUninstallationJob(taskQueue);
        moduleController.prepareInstallationJob(taskQueue, moduleRepository.getRelativeUrl(revisionData.file));

        dialogData.listener(new JobTask.Listener() {
            @Override
            public void onSucceed() {
                addInstalledEntry(moduleId, requiredRevision);
                moduleController.setStatus(Status.INSTALLED, true);
                showRestartToast();
            }
            @Override
            public void onFailed(String failMessage, Exception failException) {
                errorDialog.setError(failException);
                interfaceService.showDialog(errorDialog.getClass());
            }
            @Override
            public void onCanceled() {
                // Do nothing
            }
        });

        removeInstalledEntry(moduleId);
        moduleController.setStatus(Status.NOT_INSTALLED, true);
        moduleController.setActivated(false, true);
        moduleTaskDialog.showDialog(dialogData);
    }

    private void addInstalledEntry(String moduleId, int revision) {
        prefsInstalledModules.putInteger(moduleId, revision);
        prefsInstalledModules.flush();
    }

    private void removeInstalledEntry(String moduleId) {
        prefsInstalledModules.remove(moduleId);
        prefsInstalledModules.flush();
    }

    private void showRestartToast() {
        if (prevRestartToast != null) {
            eventDispatcher.postEvent(new RemoveToastEvent().toast(prevRestartToast));
        }
        ToastTable toastTable = new ToastTable();
        Actor content = interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/toastRestartRequired.lml")).first();
        toastTable.add(content).grow();
        eventDispatcher.postEvent(new ShowToastEvent()
                .content(toastTable)
                .duration(ShowToastEvent.DURATION_INDEFINITELY));
        prevRestartToast = toastTable.getToast();
    }

    //region Utility methods
    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }

    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }
}
