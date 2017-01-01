package com.crashinvaders.texturepackergui.events;

import com.kotcrab.vis.ui.widget.toast.Toast;

public class RemoveToastEvent {
    public static final float DURATION_SHORT = 4f;
    public static final float DURATION_LONG = 10f;
    public static final float DURATION_INDEFINITELY = -1f;

    private Toast toast;

    public Toast getToast() {
        return toast;
    }

    public RemoveToastEvent toast(Toast toast) {
        this.toast = toast;
        return this;
    }
}
