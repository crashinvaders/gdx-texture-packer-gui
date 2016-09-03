package com.crashinvaders.texturepackergui.events;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.widget.toast.ToastTable;

public class ToastNotificationEvent {
    public static final float DURATION_SHORT = 4f;
    public static final float DURATION_LONG = 10f;
    public static final float DURATION_INDEFINITELY = -1f;

    private String message;
    private float duration = DURATION_SHORT;
    private ToastTable content;

    public String getMessage() { return message; }
    public float getDuration() { return duration; }
    public ToastTable getContent() { return content; }

    public ToastNotificationEvent message(String message) {
        this.message = message;
        return this;
    }
    public ToastNotificationEvent duration(float duration) {
        this.duration = duration;
        return this;
    }
    public ToastNotificationEvent content(Actor content) {
        ToastTable toastTable = new ToastTable();
        toastTable.add(content).grow();
        this.content = toastTable;
        return this;
    }
    public ToastNotificationEvent content(ToastTable content) {
        this.content = content;
        return this;
    }
}
