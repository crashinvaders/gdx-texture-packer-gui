package com.crashinvaders.texturepackergui.controllers.model;

public enum PngCompressionType {
    PNGTASTIC("pngtastic"),
    @Deprecated
    ZOPFLI("zopfli"),
    TINY_PNG("tinify"),
    /** Tommy Ettinger's image quantization algorithm implementation. */
    TE_PNG8("png8"),
    PNGQUANT("pngquant"),
    ;

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