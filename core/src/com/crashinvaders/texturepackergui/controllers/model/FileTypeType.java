package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.utils.Null;

/** ;) */
public enum FileTypeType {
    PNG("png"),
    JPEG("jpeg"),
    BASIS("basis"),
    ;

    public final String key;

    FileTypeType(String key) {
        this.key = key;
    }

    /** @return enum constant or null if not found */
    @Null
    public static FileTypeType findByKey(String key) {
        if (key == null) return null;

        for (FileTypeType type : values()) {
            if (type.key.equals(key)) return type;
        }
        return null;
    }
}