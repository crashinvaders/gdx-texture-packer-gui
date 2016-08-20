package com.crashinvaders.texturepackergui.services.versioncheck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.crashinvaders.texturepackergui.AppConstants;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;

@Component
public class VersionCheckService {
    private static final String GITHUB_OWNER = "libgdx";
    private static final String GITHUB_REPO = "libgdx";

    private Json json;
    private boolean checkingInProgress;
    private VersionData lastVersion;

    @Initiate
    public void initialize() {
        json = new Json();
        json.setSerializer(VersionData.class, new VersionData.Serializer());
    }

    public void obtainLatestVersionInfo(final VersionCheckListener listener) {
        checkingInProgress = true;

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
                    listener.onResult(latestVersionData);
                } catch (Exception e) {
                    listener.onError(e);
                } finally {
                    checkingInProgress = false;
                }
            }

            @Override
            public void failed(Throwable t) {
                listener.onError(t);
                checkingInProgress = false;
            }

            @Override
            public void cancelled() {
                listener.onError(new RuntimeException("Canceled"));
                checkingInProgress = false;
            }
        });
    }

    public boolean isVersionNever(VersionData versionData) {
        return AppConstants.version.isLower(versionData.getVersion());
    }

    public interface VersionCheckListener {
        void onResult(VersionData data);
        void onError(Throwable throwable);
    }

}
