package com.crashinvaders.texturepackergui.controllers.model.filetype;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent.Property;

import java.io.StringWriter;

public class BasisuFileTypeModel extends FileTypeModel {

    /** True to generate KTX2 container (and apply ZSTD super-compression to UASTC) */
    private boolean ktx2 = true;
    /** True to generate UASTC .basis file data, otherwise ETC1S */
    private boolean uastc = true;
    /** Compression level, from 0 to 5 (higher is slower) */
    private int compressionLevel = 2;
    /** Controls the quality level. It ranges from [1,255] */
    private int qualityLevel = 128;

    public boolean isKtx2() {
        return ktx2;
    }

    public void setKtx2(boolean ktx2) {
        if (this.ktx2 == ktx2) return;
        this.ktx2 = ktx2;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.BASIS_KTX2));
        }
    }

    public boolean isUastc() {
        return uastc;
    }

    public void setUastc(boolean uastc) {
        if (this.uastc == uastc) return;
        this.uastc = uastc;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.BASIS_UASTC));
        }
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel < 0 || compressionLevel > 5) {
            throw new IllegalArgumentException("Compression level should be in range of 0..5. Current value: " + compressionLevel);
        }

        if (this.compressionLevel == compressionLevel) return;
        this.compressionLevel = compressionLevel;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.BASIS_COMPRESSION_LEVEL));
        }
    }

    public int getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(int qualityLevel) {
        if (qualityLevel < 1 || qualityLevel > 255) {
            throw new IllegalArgumentException("Quality level should be in range of 1..255. Current value: " + qualityLevel);
        }

        if (this.qualityLevel == qualityLevel) return;
        this.qualityLevel = qualityLevel;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new FileTypePropertyChangedEvent(this, Property.BASIS_QUALITY_LEVEL));
        }
    }

    @Override
    public FileTypeType getType() {
        return FileTypeType.BASIS;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("ktx2", ktx2);
            json.writeValue("uastc", uastc);
            json.writeValue("compressionLevel", compressionLevel);
            json.writeValue("qualityLevel", qualityLevel);
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
        this.uastc = jsonValue.getBoolean("ktx2", this.ktx2);
        this.uastc = jsonValue.getBoolean("uastc", this.uastc);
        this.compressionLevel = jsonValue.getInt("compressionLevel", this.compressionLevel);
        this.qualityLevel = jsonValue.getInt("qualityLevel", this.qualityLevel);
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(uastc, compressionLevel, qualityLevel);
    }
}
