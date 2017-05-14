package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;

class CompositionHolder extends WidgetGroup {
    private static final float SAVE_PADDING = 24f;

    private final SourceImage sourceImage;
    private final PixelGrid pixelGrid;
    private final PatchGrid patchGrid;
    private final BorderAreaDim borderAreaDim;

    private boolean firstLayout = true;

    public CompositionHolder(Skin skin, SourceImage sourceImage) {
        borderAreaDim = new BorderAreaDim(skin, sourceImage);
        borderAreaDim.setColor(new Color(0x00000080));
        this.addActor(borderAreaDim);

        this.sourceImage = sourceImage;
        this.addActor(sourceImage);

        pixelGrid = new PixelGrid(skin);
        pixelGrid.setPixelSize(sourceImage.getScale());
        sourceImage.addActor(pixelGrid);

        patchGrid = new PatchGrid(skin);
        patchGrid.setImageSize(sourceImage.getImageWidth(), sourceImage.getImageHeight());
        patchGrid.setPixelSize(sourceImage.getScale());
        sourceImage.addActor(patchGrid);

        sourceImage.setScaleListener(new SourceImage.ScaleListener() {
            @Override
            public void onScaleChanged(float scale) {
                pixelGrid.setPixelSize(scale);
                patchGrid.setPixelSize(scale);
            }
        });

        addListener(new PanListener());
        addListener(new ZoomListener());
    }

    @Override
    public void layout() {
        super.layout();

        sourceImage.setSize(sourceImage.getWidth(), sourceImage.getHeight());
        if (firstLayout) {
            firstLayout = false;
            sourceImage.setPosition(getWidth() * 0.5f, getHeight() * 0.5f, Align.center);
        }
        validateImagePosition();

        borderAreaDim.setBounds(0f, 0f, getWidth(), getHeight());
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        validateImagePosition();
    }

    private void validateImagePosition() {
        float x = sourceImage.getX();
        float y = sourceImage.getY();
        float width = sourceImage.getWidth() * sourceImage.getScaleX();
        float height = sourceImage.getHeight() * sourceImage.getScaleY();
        float availableWidth = getWidth();
        float availableHeight = getHeight();

        if (x < -width + SAVE_PADDING) sourceImage.setX(-width + SAVE_PADDING);
        if (x > availableWidth - SAVE_PADDING) sourceImage.setX(availableWidth - SAVE_PADDING);
        if (y < -height + SAVE_PADDING) sourceImage.setY(-height + SAVE_PADDING);
        if (y > availableHeight - SAVE_PADDING) sourceImage.setY(availableHeight - SAVE_PADDING);
    }

    private class PanListener extends InputListener {
        private float lastX, lastY;
        private boolean dragging;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (button != 0) return false;
            dragging = true;
            lastX = x;
            lastY = y;
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (dragging) {
                sourceImage.setPosition(sourceImage.getX() + x - lastX, sourceImage.getY() + y - lastY);
                validateImagePosition();
                lastX = x;
                lastY = y;
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (button != 0) return;

            dragging = false;
        }
    }

    private class ZoomListener extends InputListener {
        private final float[] zoomScales = new float[]{0.25f, 0.5f, 1f, 1.25f, 1.5f, 2f, 3f, 5f, 10f, 20f};
        private int zoomIndex = 3;

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            getStage().setScrollFocus(CompositionHolder.this);
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {

            float preWidth = sourceImage.getWidth() * sourceImage.getScaleX();
            float preHeight = sourceImage.getHeight() * sourceImage.getScaleY();
            float xNormalized = x < sourceImage.getX() ? 0f : x > sourceImage.getX()+preWidth ? 1f : (x- sourceImage.getX())/preWidth;
            float yNormalized = y < sourceImage.getY() ? 0f : y > sourceImage.getY()+preHeight ? 1f : (y- sourceImage.getY())/preHeight;

            zoomIndex = (Math.max(0, Math.min(zoomScales.length-1, zoomIndex - amount)));
            sourceImage.setScale(zoomScales[zoomIndex]);

            float postWidth = sourceImage.getWidth() * sourceImage.getScaleX();
            float postHeight = sourceImage.getHeight() * sourceImage.getScaleY();
            sourceImage.setPosition(
                    sourceImage.getX() + (preWidth - postWidth) * xNormalized,
                    sourceImage.getY() + (preHeight - postHeight) * yNormalized);

            validateImagePosition();
            return true;
        }
    }
}
