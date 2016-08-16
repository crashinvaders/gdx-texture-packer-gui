package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.services.model.PackModel;

public class PackPropertyChangedEvent {

    private final PackModel pack;
    private final Property property;

    public PackPropertyChangedEvent(PackModel pack, Property property) {
        this.pack = pack;
        this.property = property;
    }

    public PackModel getPack() {
        return pack;
    }

    public Property getProperty() {
        return property;
    }

    public enum Property {
        NAME,
        INPUT,
        OUTPUT,
        FILENAME
    }
}
