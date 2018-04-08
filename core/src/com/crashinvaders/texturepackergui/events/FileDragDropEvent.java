package com.crashinvaders.texturepackergui.events;

public class FileDragDropEvent {

    private final Action action;

    public FileDragDropEvent(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        START_DRAGGING,
        STOP_DRAGGING,
    }
}
