package com.crashinvaders.texturepackergui.controllers.model.filetype;

import com.crashinvaders.common.statehash.StateHashable;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

public abstract class FileTypeModel implements StateHashable {
    protected EventDispatcher eventDispatcher;

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public abstract FileTypeType getType();
    public abstract String serializeState();
    public abstract void deserializeState(String data);
}
