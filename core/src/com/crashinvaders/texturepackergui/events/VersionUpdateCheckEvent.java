package com.crashinvaders.texturepackergui.events;

public class VersionUpdateCheckEvent {

    private final Action action;

    public VersionUpdateCheckEvent(Action action) {
        this.action = action;
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
