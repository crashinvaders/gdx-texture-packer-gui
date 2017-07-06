package com.crashinvaders.texturepackergui.services.model;

/** ;) */
public enum FileTypeType {
    PNG("png"),
    JPEG("jpeg"),
    KTX("ktx");

    public final String key;

    FileTypeType(String key) {
        this.key = key;
    }

    /** @return enum constant or null if not found */
    public static FileTypeType findByKey(String key) {
        if (key == null) return null;

        for (FileTypeType type : values()) {
            if (type.key.equals(key)) return type;
        }
        return null;
    }
}