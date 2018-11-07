package com.crashinvaders.texturepackergui.views.canvas.widgets.preview;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener;
import com.crashinvaders.texturepackergui.views.canvas.model.AtlasModel;

public class PreviewHolder extends WidgetGroup {
    private static final float SAVE_PADDING = 24f;
    private static final Rectangle TMP_RECT = new Rectangle();
    private static final int[] ZOOM_LEVELS = {5, 10, 16, 25, 33, 50, 66, 100, 150, 200, 300, 400, 600, 800, 1000};
    private static final int DEFAULT_ZOOM_INDEX = 7;

    private final Skin skin;
    private final NoPageHint noPageHint;
    private PageGroup pageGroup;
    private boolean pageMoved;

    private int zoomIndex = DEFAULT_ZOOM_INDEX;
    private Listener listener;

    public PreviewHolder(Skin skin) {
        this.skin = skin;

        noPageHint = new NoPageHint(skin);
        addActor(noPageHint);

        addActor(new OuterFade(skin));
        addListener(new PanZoomListener());
        addListener(new ScrollFocusCaptureInputListener());
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if (stage != null) {
            stage.setScrollFocus(this);
        }
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();

        if (!pageMoved) {
            fitPageAtCenter();
        }
        fixPagePosition();
    }

    public void setPage(AtlasModel atlas, int pageIndex) {
        if (atlas == null) throw new IllegalArgumentException("atlas cannot be null");
        if (pageGroup != null && pageGroup.getPage().getPageIndex() == pageIndex) return;

        if (pageGroup != null) { removeActor(pageGroup); }
        pageGroup = new PageGroup(skin, atlas.getPages().get(pageIndex));
        addActor(pageGroup);

        pageMoved = false;
        noPageHint.setVisible(false);

        fitPageAtCenter();
        fixPagePosition();

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void reset() {
        setZoomIndex(DEFAULT_ZOOM_INDEX);
        pageMoved = false;
        if (pageGroup != null) {
            removeActor(pageGroup);
            pageGroup = null;
        }
        noPageHint.setVisible(true);
    }

    private void setZoomIndex(int zoomIndex) {
        if (pageGroup == null) return;

        this.zoomIndex = zoomIndex;
        int zoomPercentage = ZOOM_LEVELS[zoomIndex];
        float pageScale = (float)zoomPercentage / 100f;
        pageGroup.setScale(pageScale);

        if (listener != null) {
            listener.onZoomChanged(ZOOM_LEVELS[zoomIndex]);
        }
    }

    private boolean isHitPage(float x, float y) {
        if (pageGroup == null) return false;

        return TMP_RECT.set(
                pageGroup.getX(),
                pageGroup.getY(),
                pageGroup.getWidth() * pageGroup.getScaleX(),
                pageGroup.getHeight() * pageGroup.getScaleY())
                .contains(x, y);
    }

    private void fixPagePosition() {
        if (pageGroup == null) return;

        float x = pageGroup.getX();
        float y = pageGroup.getY();
        float width = pageGroup.getWidth() * pageGroup.getScaleX();
        float height = pageGroup.getHeight() * pageGroup.getScaleY();
        float availableWidth = getWidth();
        float availableHeight = getHeight();

        if (x < -width + SAVE_PADDING) pageGroup.setX(-width + SAVE_PADDING);
        if (x > availableWidth - SAVE_PADDING) pageGroup.setX(availableWidth - SAVE_PADDING);
        if (y < -height + SAVE_PADDING) pageGroup.setY(-height + SAVE_PADDING);
        if (y > availableHeight - SAVE_PADDING) pageGroup.setY(availableHeight - SAVE_PADDING);
    }

    private void fitPageAtCenter() {
        if (pageGroup == null) return;
        if (getWidth() <= 0f || getHeight() <= 0f) return;

        for (int i = ZOOM_LEVELS.length - 1; i >= 0; i--) {
            float zoomScale = (float)ZOOM_LEVELS[i] / 100f;
            float width = pageGroup.getWidth() * zoomScale;
            float height = pageGroup.getHeight() * zoomScale;

            if (width <= getWidth() && height <= getHeight()) {
                setZoomIndex(i);
                break;
            }
        }

        pageGroup.setPosition(
                (getWidth() - pageGroup.getWidth() * pageGroup.getScaleX()) * 0.5f,
                (getHeight() - pageGroup.getHeight() * pageGroup.getScaleY()) * 0.5f);
    }

    public interface Listener {
        void onZoomChanged(int percentage);
    }

    private class PanZoomListener extends InputListener {
        private final Vector2 tmpCoords = new Vector2();
        private final Vector2 lastPos = new Vector2();
        private boolean dragging = false;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            getStage().setScrollFocus(PreviewHolder.this);

            if (pageGroup == null) return false;
            if (button != 0) return false;
//            if (!isHitPage(x, y)) return false;

            dragging = true;
            lastPos.set(x, y);
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (dragging) {
                pageGroup.setPosition(
                        pageGroup.getX() - lastPos.x + x,
                        pageGroup.getY() - lastPos.y + y);

                fixPagePosition();
                lastPos.set(x, y);
                pageMoved = true;
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (button != 0) return;

            dragging = false;
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            if (pageGroup == null) return false;

            float preWidth = pageGroup.getWidth() * pageGroup.getScaleX();
            float preHeight = pageGroup.getHeight() * pageGroup.getScaleY();
            float xNormalized = x < pageGroup.getX() ? 0f : x > pageGroup.getX()+preWidth ? 1f : (x- pageGroup.getX())/preWidth;
            float yNormalized = y < pageGroup.getY() ? 0f : y > pageGroup.getY()+preHeight ? 1f : (y- pageGroup.getY())/preHeight;

            setZoomIndex(Math.max(0, Math.min(ZOOM_LEVELS.length-1, zoomIndex - amount)));

            float postWidth = pageGroup.getWidth() * pageGroup.getScaleX();
            float postHeight = pageGroup.getHeight() * pageGroup.getScaleY();
            pageGroup.setPosition(
                    pageGroup.getX() + (preWidth - postWidth) * xNormalized,
                    pageGroup.getY() + (preHeight - postHeight) * yNormalized);

            fixPagePosition();
            return true;
        }
    }

    /** Draws fade outside of page (when  page is present) */
    private class OuterFade extends Widget {
        private final Color COLOR_DIM = new Color(0x00000040);

        private final TextureRegion whiteTexture;

        public OuterFade(Skin skin) {
            whiteTexture = skin.getRegion("white");
            setFillParent(true);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if (pageGroup == null) return;

            Color col;
            float x = pageGroup.getX();
            float y = pageGroup.getY();
            float width = pageGroup.getWidth() * pageGroup.getScaleX();
            float height = pageGroup.getHeight() * pageGroup.getScaleY();

            // Fading all around page
            col = COLOR_DIM;
            batch.setColor(col.r, col.g, col.b, col.a * getColor().a * parentAlpha);
            batch.draw(whiteTexture,
                    getX() + 0f,
                    getY() + y + height,
                    getWidth(),
                    getHeight());
            batch.draw(whiteTexture,
                    getX() + 0f,
                    getY() + y - getHeight(),
                    getWidth(),
                    getHeight());
            batch.draw(whiteTexture,
                    getX() + 0f,
                    getY() + y,
                    x,
                    height);
            batch.draw(whiteTexture,
                    getX() + x + width,
                    getY() + y,
                    getWidth(),
                    height);
        }
    }
}
