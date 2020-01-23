package com.crashinvaders.texturepackergui.controllers.model;

public enum EtcCompressionType {
    KTX("ktx");

    public final String key;

	EtcCompressionType(String key) {
        this.key = key;
    }

    /** @return enum constant or null if not found */
    public static EtcCompressionType findByKey(String key) {
        if (key == null) return null;

        for (EtcCompressionType type : values()) {
            if (type.key.equals(key)) return type;
        }
        return null;
    }
}
