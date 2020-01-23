package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

class PixelGrid extends Widget {
    private static final int MAJOR_LINE_STEP = 8;
    private static final Color majorColor = new Color(0xffffff80);
    private static final Color minorColor = new Color(0xffffff40);
    private static final Color tmpColor = new Color();

    private final Drawable white;
    private float pixelSize = 1f;
    private int xLines, yLines;

    public PixelGrid(Skin skin) {
        white = skin.getDrawable("white");
        setTouchable(Touchable.disabled);
    }

    public void setPixelSize(float pixelSize) {
        this.pixelSize = pixelSize;
        invalidate();
    }

    @Override
    public void layout() {
        super.layout();

        xLines = (int)(getWidth() / pixelSize) + 1;
        yLines = (int)(getHeight() / pixelSize) + 1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.setColor(tmpColor.set(getColor()).mul(1f, 1f, 1f, parentAlpha));
        float lineThickness = 1f;

        for (int i = 0; i < xLines; i++) {
            batch.setColor(tmpColor.set(i%MAJOR_LINE_STEP==0 ? majorColor : minorColor)
                    .mul(getColor())
                    .mul(1f, 1f, 1f, parentAlpha));
            white.draw(batch, MathUtils.round(getX() + i * pixelSize - lineThickness*0.5f), getY(), lineThickness, getHeight());
        }
        for (int i = 0; i < yLines; i++) {
            batch.setColor(tmpColor.set(i%MAJOR_LINE_STEP==0 ? majorColor : minorColor)
                    .mul(getColor())
                    .mul(1f, 1f, 1f, parentAlpha));
            white.draw(batch, getX(), MathUtils.round(getY() + i * pixelSize - lineThickness*0.5f), getWidth(), lineThickness);
        }
    }
}
