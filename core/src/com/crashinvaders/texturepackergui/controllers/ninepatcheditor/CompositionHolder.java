package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener;
import com.crashinvaders.common.scene2d.Scene2dUtils;

class CompositionHolder extends WidgetGroup {
    private static final float SAVE_PADDING = 24f;
    private static final float PG_VISIBILITY_SCALE_THRESHOLD = 5f;

    private final SourceImage sourceImage;
    private final NinePatchEditorModel model;
    private final PixelGrid pixelGrid;
    private final Stack patchGridGroup;
    private final PatchGrid patchGrid;
    private final PatchGrid contentGrid;
    private final BorderAreaDim borderAreaDim;

    private boolean firstLayout = true;
    private float prevWidth = -1f, prevHeight = -1f;

    public CompositionHolder(Skin skin, final SourceImage sourceImage, NinePatchEditorModel model) {
        this.model = model;
        borderAreaDim = new BorderAreaDim(skin, sourceImage);
        borderAreaDim.setColor(new Color(0x00000080));
        this.addActor(borderAreaDim);

        this.sourceImage = sourceImage;
        this.addActor(sourceImage);

        pixelGrid = new PixelGrid(skin);
        pixelGrid.setPixelSize(sourceImage.getScale());
        pixelGrid.setVisible(model.zoomModel.getCurrentScale() > PG_VISIBILITY_SCALE_THRESHOLD);
        sourceImage.addActor(pixelGrid);

        patchGridGroup = new Stack();

        patchGrid = new PatchGrid(skin, skin.getColor("nine-patch-lines-patch"), model.patchValues);
        patchGrid.setImageSize(sourceImage.getImageWidth(), sourceImage.getImageHeight());
        patchGrid.setPixelSize(sourceImage.getScale());
        patchGridGroup.addActor(patchGrid);

        contentGrid = new ContentGrid(skin, skin.getColor("nine-patch-lines-content"), model.contentValues);
        contentGrid.setImageSize(sourceImage.getImageWidth(), sourceImage.getImageHeight());
        contentGrid.setPixelSize(sourceImage.getScale());
        patchGridGroup.addActor(contentGrid);

        sourceImage.add(patchGridGroup);

        sourceImage.setScaleListener(new SourceImage.ScaleListener() {
            @Override
            public void onScaleChanged(float scale) {
                pixelGrid.setPixelSize(scale);
                patchGrid.setPixelSize(scale);
                contentGrid.setPixelSize(scale);
            }
        });

        model.zoomModel.addListener(new ZoomModel.ChangeListener() {
            @Override
            public void onZoomIndexChanged(int zoomIndex, float scale) {
                sourceImage.setScale(scale);
                pixelGrid.setVisible(scale > PG_VISIBILITY_SCALE_THRESHOLD);
            }
        });

        addListener(new PanListener());
        addListener(new ZoomListener());
        addListener(new ScrollFocusCaptureInputListener());

        activatePatchGrid();
    }

    public void activatePatchGrid() {
        contentGrid.setDisabled(true);
        patchGrid.setDisabled(false);
        patchGrid.toFront();
    }

    public void activateContentGird() {
        patchGrid.setDisabled(true);
        contentGrid.setDisabled(false);
        contentGrid.toFront();
    }

    @Override
    public void layout() {
        super.layout();

        sourceImage.setSize(sourceImage.getPrefWidth(), sourceImage.getPrefHeight());
        if (firstLayout) {
            firstLayout = false;
            setupOptimalZoom();
            sourceImage.setSize(sourceImage.getPrefWidth(), sourceImage.getPrefHeight());
            sourceImage.setPosition(getWidth()*0.5f, getHeight()*0.5f, Align.center);

        }
        validateImagePosition();

        borderAreaDim.setBounds(0f, 0f, getWidth(), getHeight());
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();

        // Position source image while keeping location ratio
        {
            if (prevWidth > 0f && prevHeight > 0f) {
                float xFactor = sourceImage.getX(Align.center) / prevWidth;
                float yFactor = sourceImage.getY(Align.center) / prevHeight;
                sourceImage.getY(Align.center);
                sourceImage.setPosition(getWidth() * xFactor, getHeight() * yFactor, Align.center);
            }
            prevWidth = getWidth();
            prevHeight = getHeight();
        }

        validateImagePosition();
    }

    private void setupOptimalZoom() {
        float[] zoomScales = model.zoomModel.getScales();

        for (int i = zoomScales.length - 1; i >= 0; i--) {
            float zoomScale = zoomScales[i];
            float width = sourceImage.getImageWidth() * zoomScale;
            float height = sourceImage.getImageHeight() * zoomScale;

            if (width <= getWidth() && height <= getHeight()) {
                model.zoomModel.setIndex(i);
                sourceImage.setScale(zoomScale);
                break;
            }
        }
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
            // Dragging is only allowed for left and middle mouse buttons
            if (button == 0 || button == 2) {
                dragging = true;
                lastX = x;
                lastY = y;
                return true;
            }
            return false;
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
        @Override
        public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {

            float preWidth = sourceImage.getPrefWidth();
            float preHeight = sourceImage.getPrefHeight();
            float xNormalized = x < sourceImage.getX() ? 0f : x > sourceImage.getX()+preWidth ? 1f : (x- sourceImage.getX())/preWidth;
            float yNormalized = y < sourceImage.getY() ? 0f : y > sourceImage.getY()+preHeight ? 1f : (y- sourceImage.getY())/preHeight;

            if (!Scene2dUtils.containsLocal(CompositionHolder.this, x, y)) {
                xNormalized = yNormalized = 0.5f;
            }

            int zoomIndex = model.zoomModel.getIndex();
            model.zoomModel.setIndex((int) (zoomIndex - amountY));

            float postWidth = sourceImage.getPrefWidth();
            float postHeight = sourceImage.getPrefHeight();
            sourceImage.setPosition(
                    sourceImage.getX() + (preWidth - postWidth) * xNormalized,
                    sourceImage.getY() + (preHeight - postHeight) * yNormalized);

            return true;
        }
    }
}