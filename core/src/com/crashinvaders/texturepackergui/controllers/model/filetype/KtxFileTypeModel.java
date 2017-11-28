package com.crashinvaders.texturepackergui.controllers.model.filetype;

import com.crashinvaders.texturepackergui.utils.KtxEtc2Processor;
import com.badlogic.gdx.utils.*;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent.Property;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.crashinvaders.texturepackergui.utils.CommonUtils;

import java.io.StringWriter;

public class KtxFileTypeModel extends FileTypeModel {
    private Format format = Format.ETC2;
    private EncodingETC1 encodingEtc1 = EncodingETC1.RGB;
    private EncodingETC2 encodingEtc2 = EncodingETC2.RGBA8;
    private boolean zipping = true;

    @Override
    public FileTypeType getType() {
        return FileTypeType.KTX;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        if (this.format == format) return;

        this.format = format;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.KTX_FORMAT));
        }
    }

    public EncodingETC1 getEncodingEtc1() {
        return encodingEtc1;
    }

    public void setEncodingEtc1(EncodingETC1 encodingEtc1) {
        if (this.encodingEtc1 == encodingEtc1) return;

        this.encodingEtc1 = encodingEtc1;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.KTX_ENCODING));
        }
    }

    public EncodingETC2 getEncodingEtc2() {
        return encodingEtc2;
    }

    public void setEncodingEtc2(EncodingETC2 encodingEtc2) {
        if (this.encodingEtc2 == encodingEtc2) return;

        this.encodingEtc2 = encodingEtc2;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.KTX_ENCODING));
        }
    }

    public boolean isZipping() {
        return zipping;
    }

    public void setZipping(boolean zipping) {
        if (this.zipping == zipping) return;

        this.zipping = zipping;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.KTX_ZIPPING));
        }
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("format", format.name());
            json.writeValue("encodingEtc1", encodingEtc1.name());
            json.writeValue("encodingEtc2", encodingEtc2.name());
            json.writeValue("zipping", zipping);
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
        this.format = CommonUtils.findEnumConstantSafe(Format.class,
                jsonValue.getString("format", null), this.format);
        this.encodingEtc1 = CommonUtils.findEnumConstantSafe(EncodingETC1.class,
                jsonValue.getString("encodingEtc1", null), this.encodingEtc1);
        this.encodingEtc2 = CommonUtils.findEnumConstantSafe(EncodingETC2.class,
                jsonValue.getString("encodingEtc2", null), this.encodingEtc2);
        this.zipping = jsonValue.getBoolean("zipping", this.zipping);
    }

    public enum Format {
        ETC1,
        ETC2
    }

    public enum EncodingETC1 {
        RGB,
        RGBA
    }

    public enum EncodingETC2 {
        RGB8(KtxEtc2Processor.PixelFormat.RGB8),
        SRGB8(KtxEtc2Processor.PixelFormat.SRGB8),
        RGBA8(KtxEtc2Processor.PixelFormat.RGBA8),
        SRGBA8(KtxEtc2Processor.PixelFormat.SRGBA8),
        RGB8A1(KtxEtc2Processor.PixelFormat.RGB8A1),
        SRGB8A1(KtxEtc2Processor.PixelFormat.SRGB8A1),
        R11(KtxEtc2Processor.PixelFormat.R11),
        RG11(KtxEtc2Processor.PixelFormat.RG11);

        public final KtxEtc2Processor.PixelFormat format;

        EncodingETC2(KtxEtc2Processor.PixelFormat format) {
            this.format = format;
        }
    }
}
