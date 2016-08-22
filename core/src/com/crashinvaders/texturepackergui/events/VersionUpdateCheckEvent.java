package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.services.versioncheck.VersionData;

public class VersionUpdateCheckEvent {

    private final Action action;
    private VersionData latestVersion;

    public VersionUpdateCheckEvent(Action action) {
        this.action = action;
    }

    public VersionUpdateCheckEvent latestVersion(VersionData latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

    public VersionData getLatestVersion() {
        return latestVersion;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        CHECK_STARTED,
        CHECK_FINISHED,
        FINISHED_ERROR,
        FINISHED_UP_TO_DATE,
        FINISHED_UPDATE_AVAILABLE,
    }
}
