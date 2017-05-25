package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.MutableInt;

public class PatchGrid extends WidgetGroup {
    protected static final float EXTRA_TOUCH_SPACE = 16f;
    protected static final float FILL_COLOR_ALPHA = 0.25f;
    private static final Color tmpColor = new Color();
    private static final Vector2 tmpVec2 = new Vector2();

    protected final Array<PatchLine> patchLines = new Array<>();
    protected final Color primaryColor;
    protected final GridValues values;
    protected final Drawable whiteDrawable;
    protected final Drawable gridNodeDrawable;
    protected final PatchLine left, right, top, bottom;
    protected final LineDragListener lineDragListener;

    protected float pixelSize = 1f;
    private int imageWidth, imageHeight;
    private boolean disabled;

    public PatchGrid(Skin skin, Color primaryColor, GridValues values) {
        this.primaryColor = new Color(primaryColor);
        this.values = values;
        setTouchable(Touchable.enabled);

        whiteDrawable = skin.getDrawable("white");
        gridNodeDrawable = skin.getDrawable("custom/nine-patch-gird-node");

        left = new PatchLine(values.left, whiteDrawable, primaryColor);
        right = new PatchLine(values.right, whiteDrawable, primaryColor);
        top = new PatchLine(values.top, whiteDrawable, primaryColor);
        bottom = new PatchLine(values.bottom, whiteDrawable, primaryColor);
        patchLines.addAll(left, right, top, bottom);

        addActor(left);
        addActor(right);
        addActor(top);
        addActor(bottom);

        lineDragListener = new LineDragListener();
        addListener(lineDragListener);
    }

    public GridValues getValues() {
        return values;
    }

    public void setImageSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;
        setValues(0, width, height, 0);
    }

    public void setPixelSize(float pixelSize) {
        this.pixelSize = pixelSize;
    }

    public void setValues(int left, int right, int top, int bottom) {
        this.left.setValue(left);
        this.right.setValue(right);
        this.top.setValue(top);
        this.bottom.setValue(bottom);
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        setTouchable(disabled ? Touchable.disabled : Touchable.enabled);

        for (int i = 0; i < patchLines.size; i++) {
            patchLines.get(i).setDisabled(disabled);
        }

        // Forcefully remove hover from all lines
        updateLinesHover(-1f, -1f);
    }

    @Override
    public void layout() {
        left.setBounds(left.getValue() * pixelSize, 0f, 0f, getHeight());
        right.setBounds(right.getValue() * pixelSize, 0f, 0f, getHeight());
        top.setBounds(0f, top.getValue() * pixelSize, getWidth(), 0f);
        bottom.setBounds(0f, bottom.getValue() * pixelSize, getWidth(), 0f);

        validateLinePositions();

        // Manually update hover state
        {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
            Vector2 localCoord = screenToLocalCoordinates(tmpVec2.set(screenX, screenY));
            updateLinesHover(localCoord.x, localCoord.y);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!disabled && lineDragListener.isHovered() && !lineDragListener.isDragging()) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
            Vector2 localCoord = screenToLocalCoordinates(tmpVec2.set(screenX, screenY));
            updateLinesHover(localCoord.x, localCoord.y);
        }

        if (!disabled) drawAreaGraphics(batch, parentAlpha);

        super.draw(batch, parentAlpha);

        drawGridNodes(batch, parentAlpha);
    }

    protected void drawAreaGraphics(Batch batch, float parentAlpha) {
        batch.setColor(tmpColor.set(primaryColor.r, primaryColor.g, primaryColor.b, FILL_COLOR_ALPHA)
                .mul(getColor())
                .mul(1f, 1f, 1f, parentAlpha));
        if (!disabled) {
            // Fill both height and width rectangles
            whiteDrawable.draw(batch, getX(), getY() + bottom.getY(), getWidth(), top.getY() - bottom.getY());
            whiteDrawable.draw(batch, getX() + left.getX(), getY(), right.getX() - left.getX(), getHeight());
        } else {
            // Fill only central rectangle
            whiteDrawable.draw(batch, getX() + left.getX(), getY() + bottom.getY(), right.getX() - left.getX(), top.getY() - bottom.getY());
        }
    }

    protected void drawGridNodes(Batch batch, float parentAlpha) {
        if (disabled) return;

        batch.setColor(tmpColor.set(getColor()).mul(1f, 1f, 1f, parentAlpha));

        float width = gridNodeDrawable.getMinWidth();
        float height = gridNodeDrawable.getMinHeight();
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        gridNodeDrawable.draw(batch, getX() + left.getX() - halfWidth, getY() + bottom.getY() - halfHeight, width, height);
        gridNodeDrawable.draw(batch, getX() + right.getX() - halfWidth, getY() + bottom.getY() - halfHeight, width, height);
        gridNodeDrawable.draw(batch, getX() + left.getX() - halfWidth, getY() + top.getY() - halfHeight, width, height);
        gridNodeDrawable.draw(batch, getX() + right.getX() - halfWidth, getY() + top.getY() - halfHeight, width, height);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (touchable && getTouchable() == Touchable.disabled) return null;
        Vector2 point = tmpVec2;
        Actor[] childrenArray = getChildren().items;
        for (int i = getChildren().size - 1; i >= 0; i--) {
            Actor child = childrenArray[i];
            if (!child.isVisible()) continue;
            child.parentToLocalCoordinates(point.set(x, y));
            Actor hit = child.hit(point.x, point.y, touchable);
            if (hit != null) return hit;
        }

        // Extend regular touch area by EXTRA_TOUCH_SPACE (to allow line snap beyond PatchGrid borders)
        if (touchable && this.getTouchable() != Touchable.enabled) return null;
        return x >= -EXTRA_TOUCH_SPACE &&
                x < getWidth() + EXTRA_TOUCH_SPACE *2f &&
                y >= -EXTRA_TOUCH_SPACE &&
                y < getHeight() + EXTRA_TOUCH_SPACE *2f ?
                this : null;
    }

    protected void validateLinePositions() {
        if (top.dragging && top.getY() < bottom.getY() + pixelSize) {
            top.setY(bottom.getY() + pixelSize);
        }
        if (bottom.dragging && bottom.getY() > top.getY() - pixelSize) {
            bottom.setY(top.getY() - pixelSize);
        }
        if (right.dragging && right.getX() < left.getX() + pixelSize) {
            right.setX(left.getX() + pixelSize);
        }
        if (left.dragging && left.getX() > right.getX() - pixelSize) {
            left.setX(right.getX() - pixelSize);
        }

        left.setPosition(MathUtils.clamp(MathUtils.round(left.getX()/pixelSize)*pixelSize, 0f, getWidth()), 0f);
        right.setPosition(MathUtils.clamp(MathUtils.round(right.getX()/pixelSize)*pixelSize, 0f, getWidth()), 0f);
        top.setPosition(0f, MathUtils.clamp(MathUtils.round(top.getY()/pixelSize)*pixelSize, 0f, getHeight()));
        bottom.setPosition(0f, MathUtils.clamp(MathUtils.round(bottom.getY()/pixelSize)*pixelSize, 0f, getHeight()));
    }

    private void updateLinesHover(float x, float y) {
        for (int i = 0; i < patchLines.size; i++) {
            patchLines.get(i).updateHover(x, y);
        }
    }

    private class LineDragListener extends InputListener {
        private final Array<PatchLine> draggingLines = new Array<>();
        private boolean hovered;
        private boolean ignoreNextExitEvent;

        public boolean isHovered() {
            return hovered;
        }

        public boolean isDragging() {
            return draggingLines.size > 0;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (button==0) {
                for (PatchLine patchLine : patchLines) {
                    if (patchLine.checkHit(x, y)) {
                        draggingLines.add(patchLine);
                        patchLine.startDragging(x, y);
                    }
                }
                if (draggingLines.size > 0) {
                    event.handle();
                    event.stop();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (draggingLines.size > 0) {
                for (int i = 0; i < draggingLines.size; i++) {
                    draggingLines.get(i).drag(x, y);
                }
                validateLinePositions();
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (draggingLines.size > 0) {
                for (PatchLine patchLine : draggingLines) {
                    patchLine.endDragging(x, y);
                }
                draggingLines.clear();
                updateLineValuesFromPosition();
                validateLinePositions();
            }

            // Stage fires "exit" event upon touchUp() even if pointer is still over the actor.
            // This is simple workaround.
            if (hovered) ignoreNextExitEvent = true;
        }

        private void updateLineValuesFromPosition() {
            left.setValue(MathUtils.round(left.getX()/pixelSize));
            right.setValue(MathUtils.round(right.getX()/pixelSize));
            top.setValue(MathUtils.round(top.getY()/pixelSize));
            bottom.setValue(MathUtils.round(bottom.getY()/pixelSize));
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            hovered = true;
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (ignoreNextExitEvent) {
                ignoreNextExitEvent = false;
                return;
            }

            hovered = false;
            updateLinesHover(x, y);
        }
    }

    protected static class PatchLine extends Actor {
        private static final Rectangle tmpRect = new Rectangle();

        protected final MutableInt value;
        protected final Drawable drawable;
        private final Color primaryColor;
        private float thickness = 3f;

        /** Determines whether line is horizontal or vertical */
        protected boolean horizontal;

        protected boolean disabled;
        protected boolean hovered;

        public PatchLine(MutableInt value, Drawable drawable, Color primaryColor) {
            this.value = value;
            this.drawable = drawable;
            this.primaryColor = primaryColor;
            setTouchable(Touchable.disabled);

            updateVisualState();
        }

        public int getValue() {
            return value.get();
        }

        public void setValue(int value) {
            this.value.set(value);
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;

            updateVisualState();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.setColor(tmpColor.set(getColor()).mul(1f,1f,1f,parentAlpha));
            if (horizontal) {
                // Horizontal line
                drawable.draw(batch, getX()-thickness*0.5f, getY()-thickness*0.5f, getWidth()+thickness, thickness);
            } else {
                // Vertical line
                drawable.draw(batch, getX()-thickness*0.5f, getY()-thickness*0.5f, thickness, getHeight()+thickness);
            }
        }

        @Override
        protected void sizeChanged() {
            horizontal = getWidth() > getHeight();
            updateVisualState();
        }

        /** Position in parent's coordinates */
        public void updateHover(float x, float y) {
            if (disabled) return;

            boolean hit = checkHit(x, y);
            if (hovered != hit) {
                hovered = hit;
                updateVisualState();
            }
        }

        /** Position in parent's coordinates */
        public boolean checkHit(float x, float y) {
            if (disabled) return false;

            return tmpRect.set(
                    getX() - EXTRA_TOUCH_SPACE,
                    getY() - EXTRA_TOUCH_SPACE,
                    getWidth() + EXTRA_TOUCH_SPACE *2f,
                    getHeight() + EXTRA_TOUCH_SPACE *2f)
                    .contains(x, y);
        }

        private void updateVisualState() {
            if (disabled) {
                this.setColor(primaryColor);
                thickness = 2f;
            } else if (hovered || dragging) {
                this.setColor(Color.WHITE);
                thickness = 4f;
            } else {
                this.setColor(primaryColor);
                thickness = 4f;
            }
        }

        //region Dragging
        protected float dragOffsetX, dragOffsetY;
        protected boolean dragging = false;

        public void startDragging(float x, float y) {
            dragOffsetX = getX() - x;
            dragOffsetY = getY() - y;
            dragging = true;

            updateVisualState();
        }

        public void drag(float x, float y) {
            setPosition(x + dragOffsetX, y + dragOffsetY);
        }

        public void endDragging(float x, float y) {
            dragging = false;

            updateVisualState();
        }
        //endregion
    }
}
