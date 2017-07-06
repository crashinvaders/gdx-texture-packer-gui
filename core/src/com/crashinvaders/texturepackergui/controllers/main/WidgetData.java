package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.services.model.EtcCompressionType;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.JpegFileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.KtxFileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.PngFileTypeModel;
import com.github.czyzby.kiwi.util.gdx.reflection.Reflection;

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
        TINY_PNG (PngCompressionType.TINY_PNG, "tinyPng", true);
//        ZOPFLI (PngCompressionType.ZOPFLI, "zopfli", true);

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
    
    public enum CompressionEtc {
        NONE (null, "none", false),
        KTX (EtcCompressionType.KTX, "fileTypeKtx", true);
//        ZKTX (EtcCompressionType.ZKTX, "compressionZktx", true);

        public final EtcCompressionType type;
        public final String nameKey;
        public final boolean hasSettings;

        CompressionEtc(EtcCompressionType type, String nameKey, boolean hasSettings) {
            this.type = type;
            this.nameKey = nameKey;
            this.hasSettings = hasSettings;
        }

        public static CompressionEtc valueOf(EtcCompressionType type) {
            for (int i = 0; i < values().length; i++) {
            	CompressionEtc value = values()[i];
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
        PNG("fileTypePng", PngFileTypeModel.class),
        JPEG("fileTypeJpeg", JpegFileTypeModel.class),
        KTX("fileTypeKtx", KtxFileTypeModel.class),
        ;

        public final String nameKey;
        public final Class<? extends FileTypeModel> modelClass;

        FileType(String nameKey, Class<? extends FileTypeModel> modelClass) {
            this.nameKey = nameKey;
            this.modelClass = modelClass;
        }

        public <T extends FileTypeModel> T createModel() {
            return (T)Reflection.newInstance(modelClass);
        }

        public static FileType valueOf(FileTypeModel model) {
            Class<? extends FileTypeModel> modelClass = model.getClass();
            for (int i = 0; i < values().length; i++) {
                FileType value = values()[i];
                if (value.modelClass == modelClass) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Can't find constant for " + modelClass.getSimpleName());
        }

        @Override
        public String toString() {
            return App.inst().getI18n().get(nameKey);
        }
    }

//    public enum PngEncoding {
//        RGBA8888("RGBA8888"),
//        RGB888("RGB888"),
//        RGBA4444("RGBA4444"),
//        RGB565("RGB565"),
//        Alpha("Alpha"),
//        ;
//
//        private final String key;
//
//        PngEncoding(String key) {
//            this.key = key;
//        }
//    }
}
