package com.crashinvaders.texturepackergui.events;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class ShowUserNotificationEvent {
    public static final float DURATION_SHORT = 4f;
    public static final float DURATION_LONG = 10f;
    public static final float DURATION_INDEFINITELY = -1f;

    private String message;
    private float duration = DURATION_SHORT;
    private Actor content;

    public String getMessage() { return message; }
    public float getDuration() { return duration; }
    public Actor getContent() { return content; }

    public ShowUserNotificationEvent message(String message) {
        this.message = message;
        return this;
    }
    public ShowUserNotificationEvent duration(float duration) {
        this.duration = duration;
        return this;
    }
    public ShowUserNotificationEvent content(Actor content) {
        this.content = content;
        return this;
    }
}
