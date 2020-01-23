package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

class SourceImage extends Stack {

    private final Pixmap pixmap;
    private final Image image;
    private Texture texture;

    private boolean initialized = false;
    private float scale = 1f;

    private ScaleListener scaleListener;

    public SourceImage(Pixmap pixmap) {
        this.pixmap = pixmap;
        image = new Image();
        image.setScaling(Scaling.stretch);
        addActor(image);
    }

    public void setScaleListener(ScaleListener scaleListener) {
        this.scaleListener = scaleListener;
    }

    public void setScale(float scale) {
        if (this.scale == scale) return;
        this.scale = scale;

        invalidateHierarchy();

        if (scaleListener != null) {
            scaleListener.onScaleChanged(scale);
        }
    }

    public float getScale() {
        return scale;
    }

    public int getImageWidth() {
        return pixmap.getWidth();
    }

    public int getImageHeight() {
        return pixmap.getHeight();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if (!initialized && stage != null) {
            initialized = true;
            texture = new Texture(pixmap);
            image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        }
        if (initialized && stage == null) {
            initialized = false;
            texture.dispose();
            texture = null;
            image.setDrawable(null);
        }
    }

    @Override
    public float getPrefWidth() {
        return pixmap.getWidth() * scale;
    }

    @Override
    public float getPrefHeight() {
        return pixmap.getHeight() * scale;
    }

    public interface ScaleListener {
        void onScaleChanged(float scale);
    }
}
