package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.model.EtcCompressionType;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;
import com.crashinvaders.texturepackergui.controllers.model.filetype.FileTypeModel;

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

    public enum PngCompression {
        NONE (null, "none", false),
        PNGTASTIC (PngCompressionType.PNGTASTIC, "pngtastic", true),
        TINY_PNG (PngCompressionType.TINY_PNG, "tinyPng", true),
        TE_PNG8(PngCompressionType.TE_PNG8, "tommyEttingerPng8", true),
        PNGQUANT(PngCompressionType.PNGQUANT, "pngquant", true),
//        ZOPFLI (PngCompressionType.ZOPFLI, "zopfli", true),
        ;

        public final PngCompressionType type;
        public final String nameKey;
        public final boolean hasSettings;

        PngCompression(PngCompressionType type, String nameKey, boolean hasSettings) {
            this.type = type;
            this.nameKey = nameKey;
            this.hasSettings = hasSettings;
        }

        public static PngCompression valueOf(PngCompressionType type) {
            for (int i = 0; i < values().length; i++) {
                PngCompression value = values()[i];
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

    public enum FileType {
        PNG("fileTypePng", FileTypeType.PNG),
        JPEG("fileTypeJpeg", FileTypeType.JPEG),
        BASIS("fileTypeBasisu", FileTypeType.BASIS),
        ;

        public final String nameKey;
        public final FileTypeType modelType;

        FileType(String nameKey, FileTypeType modelType) {
            this.nameKey = nameKey;
            this.modelType = modelType;
        }

        public static FileType valueOf(FileTypeModel model) {
            for (int i = 0; i < values().length; i++) {
                FileType value = values()[i];
                if (value.modelType == model.getType()) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Can't find constant for " + model);
        }

        @Override
        public String toString() {
            return App.inst().getI18n().get(nameKey);
        }
    }
}
