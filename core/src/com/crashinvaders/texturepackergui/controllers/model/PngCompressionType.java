package com.crashinvaders.texturepackergui.controllers.model;

public enum PngCompressionType {
    PNGTASTIC("pngtastic"),
    ZOPFLI("zopfli"),
    TINY_PNG("tinify"),
    PNG8("png8");

    public final String key;

    PngCompressionType(String key) {
        this.key = key;
    }

    /** @return enum constant or null if not found */
    public static PngCompressionType findByKey(String key) {
        if (key == null) return null;
        for (PngCompressionType type : values()) {
            if (type.key.equals(key)) return type;
        }
        return null;
    }
}