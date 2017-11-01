package com.crashinvaders.texturepackergui.services.extensionmodules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.crashinvaders.texturepackergui.events.ModuleRepositoryRefreshEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

@Component
public class ModuleRepositoryService {
    private static final String BASE_URL = "https://crashinvaders.github.io/gdx-texture-packer-gui/modules/";

    @Inject EventDispatcher eventDispatcher;

    private final Json json = new Json();
    private final Array<ModuleData> modules = new Array<>();
    private boolean checkingInProgress;

    @Initiate
    void init() {
        requestRefresh();
    }

    /** @return URL relative to the module repository root */
    public String getRelativeUrl(String resource) {
        return BASE_URL + resource;
    }

    public Array<ModuleData> getModules() {
        return modules;
    }

    synchronized
    public void requestRefresh() {
        if (checkingInProgress) return;

        checkingInProgress = true;
        eventDispatcher.postEvent(new ModuleRepositoryRefreshEvent(ModuleRepositoryRefreshEvent.Action.REFRESH_STARTED));

        Net.HttpRequest httpRequest = new Net.HttpRequest();
        httpRequest.setMethod(Net.HttpMethods.GET);
        httpRequest.setUrl(getRelativeUrl("modules.json"));
        httpRequest.setTimeOut(10000);
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                try {
                    Array newArray = json.fromJson(Array.class, ModuleData.class, result);
                    modules.clear();
                    modules.addAll(newArray);
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
}
