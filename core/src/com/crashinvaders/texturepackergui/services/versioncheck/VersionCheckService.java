package com.crashinvaders.texturepackergui.services.versioncheck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.VersionUpdateCheckEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;

@Component
@ViewActionContainer("versionCheckService")
public class VersionCheckService implements ActionContainer {
    private static final String GITHUB_OWNER = "crashinvaders";
    private static final String GITHUB_REPO = "gdx-texture-packer-gui";

    @Inject EventDispatcher eventDispatcher;

    private Json json;
    private boolean checkingInProgress;
    private VersionData lastVersion;

    @Initiate
    public void initialize() {
        json = new Json();
        json.setSerializer(VersionData.class, new VersionData.Serializer());
    }

    /**
     * Runs version update checking process. Listen for {@link VersionUpdateCheckEvent} to get result.
     */
    public void requestVersionCheck() {
        if (checkingInProgress) return;

        checkingInProgress = true;
        eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.CHECK_STARTED));

        Net.HttpRequest httpRequest = new Net.HttpRequest();
        httpRequest.setMethod(Net.HttpMethods.GET);
        httpRequest.setUrl("https://api.github.com/repos/"+GITHUB_OWNER+"/"+GITHUB_REPO+"/releases/latest");
        httpRequest.setTimeOut(10000);
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                try {
                    VersionData latestVersionData = json.fromJson(VersionData.class, result);
                    lastVersion = latestVersionData;

                    checkingInProgress = false;
                    eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.CHECK_FINISHED));
                    if (isVersionNewer(latestVersionData)) {
                        eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.FINISHED_UPDATE_AVAILABLE)
                                .latestVersion(latestVersionData));
                    } else {
                        eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.FINISHED_UP_TO_DATE));
                    }
                } catch (Exception e) {
                    checkingInProgress = false;
                    eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.CHECK_FINISHED));
                    eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.FINISHED_ERROR));
                }
            }

            @Override
            public void failed(Throwable t) {
                checkingInProgress = false;
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.CHECK_FINISHED));
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.FINISHED_ERROR));
            }

            @Override
            public void cancelled() {
                checkingInProgress = false;
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.CHECK_FINISHED));
                eventDispatcher.postEvent(new VersionUpdateCheckEvent(VersionUpdateCheckEvent.Action.FINISHED_ERROR));
            }
        });
    }

    public boolean isVersionNewer(VersionData versionData) {
        return AppConstants.version.isLower(versionData.getVersion());
    }

    @LmlAction("getReleasesPageUrl") public String getReleasesPageUrl() {
        return "https://github.com/"+GITHUB_OWNER+"/"+GITHUB_REPO+"/releases";
    }
}
