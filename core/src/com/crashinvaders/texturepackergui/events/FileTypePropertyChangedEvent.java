package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.filetype.FileTypeModel;

public class FileTypePropertyChangedEvent {

    private final FileTypeModel model;
    private final Property property;

    public FileTypePropertyChangedEvent(FileTypeModel model, Property property) {
        this.model = model;
        this.property = property;
    }

    public FileTypeModel getModel() {
        return model;
    }

    public Property getProperty() {
        return property;
    }

    public enum Property {
        PNG_ENCODING,
        PNG_COMPRESSION,

        JPEG_ENCODING,
        JPEG_QUALITY,

        KTX_FORMAT,
        KTX_ENCODING,
        KTX_ZIPPING,

        BASIS_KTX2,
        BASIS_UASTC,
        BASIS_COMPRESSION_LEVEL,
        BASIS_QUALITY_LEVEL,
        ;
    }
}
