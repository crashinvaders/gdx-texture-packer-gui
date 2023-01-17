package com.crashinvaders.texturepackergui.controllers.versioncheck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.crashinvaders.SyncHttpResponseListener;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.VersionUpdateCheckEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;

import static com.crashinvaders.texturepackergui.AppConstants.GITHUB_OWNER;
import static com.crashinvaders.texturepackergui.AppConstants.GITHUB_REPO;
import static com.crashinvaders.texturepackergui.events.VersionUpdateCheckEvent.Action.*;

@Component
@ViewActionContainer("versionCheckService")
public class VersionCheckService implements ActionContainer {

    @Inject EventDispatcher eventDispatcher;

    private Json json;
    private volatile boolean checkingInProgress;
    private VersionData lastVersion;

    private final Object threadLock = new Object();

    @Initiate
    public void initialize() {
        json = new Json();
        json.setSerializer(VersionData.class, new VersionData.Serializer());
    }

    /**
     * Runs version update checking process. Listen for {@link VersionUpdateCheckEvent} to get result.
     */
    synchronized
    public void requestVersionCheck() {
        if (checkingInProgress) return;

        checkingInProgress = true;
        eventDispatcher.postEvent(new VersionUpdateCheckEvent(CHECK_STARTED));

        Net.HttpRequest httpRequest = new Net.HttpRequest();
        httpRequest.setMethod(Net.HttpMethods.GET);
        httpRequest.setUrl("https://api.github.com/repos/"+GITHUB_OWNER+"/"+GITHUB_REPO+"/releases/latest");
        httpRequest.setTimeOut(10000);
        Gdx.net.sendHttpRequest(httpRequest, new SyncHttpResponseListener() {

            private String responseString;

            @Override
            protected void handleResponseAsync(Net.HttpResponse httpResponse) {
                responseString = httpResponse.getResultAsString();
            }

            @Override
            public void handleResponseSync(final Net.HttpResponse httpResponse) {
                try {
                    VersionData latestVersionData = json.fromJson(VersionData.class, responseString);
                    lastVersion = latestVersionData;

                    checkingInProgress = false;
                    eventDispatcher.postEvent(new VersionUpdateCheckEvent(CHECK_FINISHED));
                    if (isVersionNewer(latestVersionData)) {
                        eventDispatcher.postEvent(new VersionUpdateCheckEvent(FINISHED_UPDATE_AVAILABLE)
                                .latestVersion(latestVersionData));
                    } else {
                        eventDispatcher.postEvent(new VersionUpdateCheckEvent(FINISHED_UP_TO_DATE));
                    }
                } catch (Exception e) {
                    checkingInProgress = false;
                    eventDispatcher.postEvent(new VersionUpdateCheckEvent(CHECK_FINISHED));
                    eventDispatcher.postEvent(new VersionUpdateCheckEvent(FINISHED_ERROR));
                }
            }

            @Override
            public void onFailed(Throwable t) {
                checkingInProgress = false;
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(CHECK_FINISHED));
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(FINISHED_ERROR));
            }

            @Override
            public void onCancelled() {
                checkingInProgress = false;
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(CHECK_FINISHED));
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(FINISHED_ERROR));
            }
        });
    }

    public boolean isVersionNewer(VersionData versionData) {
        return AppConstants.VERSION.isLower(versionData.getVersion());
    }

    @LmlAction("getReleasesPageUrl") public String getReleasesPageUrl() {
        return "https://github.com/"+GITHUB_OWNER+"/"+GITHUB_REPO+"/releases";
    }
}
