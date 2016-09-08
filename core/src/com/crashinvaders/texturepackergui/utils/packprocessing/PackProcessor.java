package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.crashinvaders.texturepackergui.services.model.PackModel;

public interface PackProcessor {
    void processPackage(PackModel packModel) throws Exception;
}
