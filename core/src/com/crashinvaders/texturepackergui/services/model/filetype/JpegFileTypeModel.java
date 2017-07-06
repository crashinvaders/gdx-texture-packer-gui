package com.crashinvaders.texturepackergui.services.model.filetype;

import com.badlogic.gdx.graphics.Pixmap;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.FileTypeType;

public class JpegFileTypeModel extends FileTypeModel {
    private Pixmap.Format encoding = Pixmap.Format.RGBA8888;
    private float quality = 0.9f;

    @Override
    public FileTypeType getType() {
        return FileTypeType.JPEG;
    }

    public Pixmap.Format getEncoding() {
        return encoding;
    }

    public void setEncoding(Pixmap.Format encoding) {
        if (this.encoding == encoding) return;

        this.encoding = encoding;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, FileTypePropertyChangedEvent.Property.JPEG_ENCODING));
        }
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        if (this.quality == quality) return;

        this.quality = quality;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, FileTypePropertyChangedEvent.Property.JPEG_QUALITY));
        }
    }
}
