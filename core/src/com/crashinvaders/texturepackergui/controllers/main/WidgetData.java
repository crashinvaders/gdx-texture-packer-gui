package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;

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

    public enum compressionPng {
        none ("cmprsNone"),
        pngtastic ("cmprsPngtastic"),
        zopfli ("cmprsZopfli");

        private final String nameKey;

        compressionPng(String nameKey) {
            this.nameKey = nameKey;
        }

        @Override
        public String toString() {
            return App.inst().getI18n().get(nameKey);
        }
    }

    public enum compressionJpg {
        none ("cmprsNone");

        private final String nameKey;

        compressionJpg(String nameKey) {
            this.nameKey = nameKey;
        }

        @Override
        public String toString() {
            return App.inst().getI18n().get(nameKey);
        }
    }
}
