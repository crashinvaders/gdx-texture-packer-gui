package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;

public class WidgetData {

    public static final Array<Pixmap.Format> textureFormats = Array.with(
            Pixmap.Format.RGBA8888,
            Pixmap.Format.RGB888,
            Pixmap.Format.RGBA4444,
            Pixmap.Format.RGB565,
            Pixmap.Format.Alpha
    );

    public static final Array<Texture.TextureFilter> textureFilters = Array.with(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.MipMap,
            Texture.TextureFilter.MipMapNearestNearest,
            Texture.TextureFilter.MipMapLinearNearest,
            Texture.TextureFilter.MipMapNearestLinear,
            Texture.TextureFilter.MipMapLinearLinear
    );

    public static final Array<Texture.TextureWrap> textureWraps = Array.with(
            Texture.TextureWrap.ClampToEdge,
            Texture.TextureWrap.Repeat
    );

    public static final Array<String> outputFormats = Array.with("png", "jpg");

    public enum CompressionPng {
        NONE (null, "compressionNone", false),
        PNGTASTIC (PngCompressionType.PNGTASTIC, "compressionPngtastic", true);
//        ZOPFLI (PngCompressionType.ZOPFLI, "compressionZopfli", true);

        public final PngCompressionType type;
        public final String nameKey;
        public final boolean hasSettings;

        CompressionPng(PngCompressionType type, String nameKey, boolean hasSettings) {
            this.type = type;
            this.nameKey = nameKey;
            this.hasSettings = hasSettings;
        }

        public static CompressionPng valueOf(PngCompressionType type) {
            for (int i = 0; i < values().length; i++) {
                CompressionPng value = values()[i];
                if (value.type == type) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Can't find constant for " + type);
        }

        @Override
        public String toString() {
            return App.inst().getI18n().get(nameKey);
        }
    }
}
