package com.crashinvaders.texturepackergui.events;

public class ModuleRepositoryRefreshEvent {

    private final Action action;

    public ModuleRepositoryRefreshEvent(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        REFRESH_STARTED,
        REFRESH_FINISHED,
        FINISHED_ERROR,
        FINISHED_SUCCESS,
    }
}
