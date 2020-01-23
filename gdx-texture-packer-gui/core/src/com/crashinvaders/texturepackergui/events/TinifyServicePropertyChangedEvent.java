package com.crashinvaders.texturepackergui.events;

public class TinifyServicePropertyChangedEvent {
    private final Property property;

    public TinifyServicePropertyChangedEvent(Property property) {
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }

    public enum Property {
        API_KEY,
        COMPRESSION_COUNT
    }
}
