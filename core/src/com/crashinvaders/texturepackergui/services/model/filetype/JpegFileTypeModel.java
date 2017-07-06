package com.crashinvaders.texturepackergui.services.model.filetype;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.*;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.FileTypeType;
import com.crashinvaders.texturepackergui.utils.CommonUtils;

import java.io.StringWriter;

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

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("encoding", encoding.name());
            json.writeValue("quality", quality);
            json.writeObjectEnd();
            return buffer.toString();
        } finally {
            StreamUtils.closeQuietly(buffer);
        }
    }

    @Override
    public void deserializeState(String data) {
        if (data == null) return;

        JsonValue jsonValue = new JsonReader().parse(data);
        this.encoding = CommonUtils.findEnumConstantSafe(Pixmap.Format.class,
                jsonValue.getString("encoding", null), encoding);
        this.quality = jsonValue.getFloat("quality", quality);
    }
}
