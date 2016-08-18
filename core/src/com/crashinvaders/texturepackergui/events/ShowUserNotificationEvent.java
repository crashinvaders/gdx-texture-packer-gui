package com.crashinvaders.texturepackergui.events;

public class ShowUserNotificationEvent {

    private String message;
    private float duration = -1f;

    public String getMessage() {
        return message;
    }

    public ShowUserNotificationEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public float getDuration() {
        return duration;
    }

    public ShowUserNotificationEvent setDuration(float duration) {
        this.duration = duration;
        return this;
    }
}
