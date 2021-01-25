package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.PackModel;

/**
 * Fires when texture packing operation has finished successfully.
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
