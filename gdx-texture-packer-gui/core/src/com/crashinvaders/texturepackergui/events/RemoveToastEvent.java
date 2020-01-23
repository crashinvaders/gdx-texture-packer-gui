package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.common.scene2d.visui.Toast;

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
