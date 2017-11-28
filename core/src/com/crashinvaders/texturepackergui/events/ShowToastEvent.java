package com.crashinvaders.texturepackergui.events;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.common.scene2d.visui.ToastTable;

public class ShowToastEvent {
    public static final float DURATION_SHORT = 4f;
    public static final float DURATION_LONG = 10f;
    public static final float DURATION_INDEFINITELY = -1f;

    private String message;
    private float duration = DURATION_SHORT;
    private ToastTable content;

    public String getMessage() { return message; }
    public float getDuration() { return duration; }
    public ToastTable getContent() { return content; }

    public ShowToastEvent message(String message) {
        this.message = message;
        return this;
    }
    public ShowToastEvent duration(float duration) {
        this.duration = duration;
        return this;
    }
    public ShowToastEvent content(Actor content) {
        ToastTable toastTable = new ToastTable();
        toastTable.add(content).grow();
        this.content = toastTable;
        return this;
    }
    public ShowToastEvent content(ToastTable content) {
        this.content = content;
        return this;
    }
}
