package com.crashinvaders.texturepackergui.services.model;

public enum PageFileType {
    PNG("png"),
    JPEG("jpeg"),
    KTX("ktx");

    public final String key;

    PageFileType(String key) {
        this.key = key;
    }

    /** @return enum constant or null if not found */
    public static PageFileType findByKey(String key) {
        if (key == null) return null;

        for (PageFileType type : values()) {
            if (type.key.equals(key)) return type;
        }
        return null;
    }
}
