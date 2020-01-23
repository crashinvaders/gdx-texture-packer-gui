package com.crashinvaders.texturepackergui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;

public class BusyBar extends Widget {
    private final Style style;
    private final TiledDrawable patternDrawable;

    private float shift;

    public BusyBar(Style style) {
        this.style = style;
        patternDrawable = new TiledDrawable((TextureRegionDrawable) style.pattern);
    }

    @Override
    public float getPrefWidth() {
        return patternDrawable.getMinWidth();
    }

    @Override
    public float getPrefHeight() {
        return patternDrawable.getMinHeight();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        shift = (shift + delta * style.shiftSpeed) % patternDrawable.getMinWidth();
        Gdx.graphics.requestRendering();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.flush();
        if (clipBegin()) {
            Color c = getColor();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            patternDrawable.draw(batch, getX() - shift, getY(), getWidth() + shift, getHeight());
            if (isVisible()) Gdx.graphics.requestRendering();
            batch.flush();
            clipEnd();
        }
    }

    public static class Style {
        public Drawable pattern;
        /** Pixel per second */
        public float shiftSpeed = 32f;
    }
}
