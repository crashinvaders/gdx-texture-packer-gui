package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;

public interface PackProcessor {
    String META_COMPRESSION_RATE = "compressionRate";

    void processPackage(ProjectModel projectModel, PackModel packModel, ObjectMap metadata) throws Exception;
}
