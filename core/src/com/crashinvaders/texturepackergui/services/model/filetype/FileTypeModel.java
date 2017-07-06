package com.crashinvaders.texturepackergui.services.model.filetype;

import com.github.czyzby.autumn.processor.event.EventDispatcher;

public abstract class FileTypeModel {
    protected EventDispatcher eventDispatcher;

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
}
