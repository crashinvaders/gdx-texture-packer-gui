package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;

class CompositionHolder extends WidgetGroup {
    private static final float SAVE_PADDING = 24f;

    private final SourceImage sourceImage;

    public CompositionHolder(SourceImage sourceImage) {
        this.sourceImage = sourceImage;
        addActor(sourceImage);

        addListener(new PanListener());
    }

    @Override
    public void layout() {
        super.layout();

        sourceImage.setPosition(getWidth()*0.5f, getHeight()*0.5f, Align.center);
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
}
