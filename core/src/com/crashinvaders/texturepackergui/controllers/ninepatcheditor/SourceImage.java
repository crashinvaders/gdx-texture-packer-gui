package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

class SourceImage extends Group {

    private final Pixmap pixmap;
    private Texture texture;

    private boolean initialized = false;
    private float scale = 1f;

    public SourceImage(Pixmap pixmap) {
        this.pixmap = pixmap;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if (!initialized && stage != null) {
            initialized = true;
            texture = new Texture(pixmap);
        }
        if (initialized && stage == null) {
            initialized = false;
            texture.dispose();
            texture = null;
        }
    }

    @Override
    public float getWidth() {
        return pixmap.getWidth() * scale;
    }

    @Override
    public float getHeight() {
        return pixmap.getHeight() * scale;
    }

    public void setScale(float scale) {
        if (this.scale == scale) return;

        this.scale = scale;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        Color col = getColor();
        batch.setColor(col.r, col.g, col.b, col.a * parentAlpha);

        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
