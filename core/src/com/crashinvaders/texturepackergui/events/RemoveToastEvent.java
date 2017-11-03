package com.crashinvaders.texturepackergui.events;

import com.kotcrab.vis.ui.widget.toast.Toast;

public class RemoveToastEvent {

    private Toast toast;

    public Toast getToast() {
        return toast;
    }

    public RemoveToastEvent toast(Toast toast) {
        this.toast = toast;
        return this;
    }
}
