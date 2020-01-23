package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.PackModel;

/**
 * Usually fires when texture packing was successful
 */
public class PackAtlasUpdatedEvent {

    private PackModel pack;

    public PackAtlasUpdatedEvent(PackModel pack) {
        this.pack = pack;
    }

    public PackModel getPack() {
        return pack;
    }
}
