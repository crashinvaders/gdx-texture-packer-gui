package com.crashinvaders.texturepackergui.services.extensionmodules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.net.HttpRequestHeader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.common.async.JobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.ModalTaskDialogController;
import com.crashinvaders.texturepackergui.events.ModuleRepositoryRefreshEvent;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleController.Status;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

@Component
public class ExtensionModuleRepositoryService {
    private static final String TAG = ExtensionModuleRepositoryService.class.getSimpleName();
    private static final String BASE_URL = "https://crashinvaders.github.io/gdx-texture-packer-gui/modules/";

    @Inject EventDispatcher eventDispatcher;

    private final Preferences prefsCommon = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
    private final Json json = new Json();
    private final ObjectMap<String, RepositoryModuleData> repositoryModules = new ObjectMap<>();
    private boolean checkingInProgress;

    @Initiate() void init() {
        requestRefreshRepositoryIfNeeded();
    }

    synchronized
    public void requestRefreshRepositoryIfNeeded() {
        //TODO add time delay check
        requestRefreshRepository();
    }

    synchronized
    public void requestRefreshRepository() {
        if (checkingInProgress) return;

        checkingInProgress = true;
        eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.REFRESH_STARTED));

        Gdx.net.sendHttpRequest(new HttpRequestBuilder().newRequest()
                .method(Net.HttpMethods.GET)
                .url(getRelativeUrl("modules.json"))
                .timeout(10000)
                .header(HttpRequestHeader.CacheControl, "no-cache")
                .build(),
                new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                try {
                    Array<RepositoryModuleData> newArray = json.fromJson(Array.class, RepositoryModuleData.class, result);
                    repositoryModules.clear();
                    for (RepositoryModuleData moduleData : newArray) {
                        repositoryModules.put(moduleData.name, moduleData);
                    }
                    checkingInProgress = false;
                    eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.REFRESH_FINISHED));
                    eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.FINISHED_SUCCESS));
                } catch (Exception e) {
                    checkingInProgress = false;
                    eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.REFRESH_FINISHED));
                    eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.FINISHED_ERROR));
                }
            }
            @Override
            public void failed(Throwable t) {
                checkingInProgress = false;
                eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.REFRESH_FINISHED));
                eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.FINISHED_ERROR));
            }
            @Override
            public void cancelled() {
                checkingInProgress = false;
                eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.REFRESH_FINISHED));
                eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.FINISHED_ERROR));
            }
        });
    }

    public RepositoryModuleData findModuleData(String moduleId) {
        return repositoryModules.get(moduleId);
    }

    public RepositoryModuleData.Revision findRevision(String moduleId, int revision) {
        RepositoryModuleData moduleData = repositoryModules.get(moduleId);
        if (moduleData == null) {
            return null;
        }
        RepositoryModuleData.Revision revisionData = moduleData.findRevision(revision);
        return revisionData;
    }

    /** @return URL relative to the module repository root */
    public String getRelativeUrl(String resource) {
        return BASE_URL + resource;
    }
}
