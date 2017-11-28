package com.crashinvaders.texturepackergui.controllers.extensionmodules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.net.HttpRequestHeader;
import com.badlogic.gdx.utils.*;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.ModuleRepositoryRefreshEvent;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

import java.util.Date;

@Component
public class ExtensionModuleRepositoryService {
    private static final String TAG = ExtensionModuleRepositoryService.class.getSimpleName();
    private static final String BASE_URL = "https://crashinvaders.github.io/gdx-texture-packer-gui/modules/";
    private static final String PREF_KEY_LAST_CHECK = "lastModuleRepoCheck";
    private static final int CACHE_LIFE = 1000*60*60*24; // One day in millis

    @Inject EventDispatcher eventDispatcher;

    private final ObjectMap<String, RepositoryModuleData> repositoryModules = new ObjectMap<>();
    private final Preferences prefsCommon = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
    private final Json json = new Json();

    private final FileHandle modulesDir = Gdx.files.external(AppConstants.MODULES_DIR);
    private final FileHandle repoCacheFile = modulesDir.child("repoCache.json");

    private boolean checkingInProgress;

    @Initiate(priority = AutumnActionPriority.TOP_PRIORITY) void init() {
        modulesDir.mkdirs();

        if (repoCacheFile.exists()) {
            Array<RepositoryModuleData> newArray = json.fromJson(Array.class, RepositoryModuleData.class, repoCacheFile);
            repositoryModules.clear();
            for (RepositoryModuleData moduleData : newArray) {
                repositoryModules.put(moduleData.name, moduleData);
            }
            Gdx.app.log(TAG, "Cached data was loaded");
        }

        requestRefreshRepositoryIfNeeded();
    }

    synchronized
    public void requestRefreshRepositoryIfNeeded() {
        long lastCheckDate = prefsCommon.getLong(PREF_KEY_LAST_CHECK);
        long currentDate = new Date().getTime();
        if (Math.abs(currentDate - lastCheckDate) > CACHE_LIFE) {
            Gdx.app.log(TAG, "Cached data is outdated.");
            requestRefreshRepository();
        }
    }

    synchronized
    public void requestRefreshRepository() {
        if (checkingInProgress) return;

        Gdx.app.log(TAG, "Requesting new data from remote server.");

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
                try {
                    String result = httpResponse.getResultAsString();

                    // Cache result into local file
                    FileUtils.saveTextToFile(repoCacheFile, result);

                    // Update in-memory data
                    Array<RepositoryModuleData> newArray = json.fromJson(Array.class, RepositoryModuleData.class, result);
                    repositoryModules.clear();
                    for (RepositoryModuleData moduleData : newArray) {
                        repositoryModules.put(moduleData.name, moduleData);
                    }
                    prefsCommon.putLong(PREF_KEY_LAST_CHECK, new Date().getTime()).flush();

                    Gdx.app.log(TAG, "Data was loaded from remote server.");

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
