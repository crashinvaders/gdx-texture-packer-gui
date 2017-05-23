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

public class PatchGrid extends WidgetGroup {
    private static final float EXTRA_TOUCH_SPACE = 16f;
    private static final Color colorFill = new Color(0x99e55040);
    private static final Color tmpColor = new Color();
    private static final Vector2 tmpVec2 = new Vector2();

    private final Drawable white;
    private final PatchLine left, right, top, bottom;
    private final Array<PatchLine> patchLines = new Array<>();
    private final LineDragListener lineDragListener;
    private float pixelSize = 1f;
    private int imageWidth, imageHeight;

    public PatchGrid(Skin skin) {
        white = skin.getDrawable("white");
        setTouchable(Touchable.enabled);

        left = new PatchLine(white);
        right = new PatchLine(white);
        top = new PatchLine(white);
        bottom = new PatchLine(white);
        patchLines.addAll(left, right, top, bottom);

        addActor(left);
        addActor(right);
        addActor(top);
        addActor(bottom);

        lineDragListener = new LineDragListener();
        addListener(lineDragListener);
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

    @Override
    public void layout() {
        left.setBounds(left.getValue() * pixelSize, 0f, 0f, getHeight());
        right.setBounds(right.getValue() * pixelSize, 0f, 0f, getHeight());
        top.setBounds(0f, top.getValue() * pixelSize, getWidth(), 0f);
        bottom.setBounds(0f, bottom.getValue() * pixelSize, getWidth(), 0f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (lineDragListener.isHovered()) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
            Vector2 localCoord = screenToLocalCoordinates(tmpVec2.set(screenX, screenY));
            updateLinesHover(localCoord.x, localCoord.y);
        }

        batch.setColor(tmpColor.set(colorFill).mul(getColor()).mul(1f,1f,1f,parentAlpha));
        white.draw(batch, getX(), getY() + bottom.getY(), getWidth(), top.getY() - bottom.getY());
        white.draw(batch, getX() + left.getX(), getY(), right.getX() - left.getX(), getHeight());
//        white.draw(batch, getX() + left.getX(), getY() + bottom.getY(), right.getX() - left.getX(), top.getY() - bottom.getY());

        super.draw(batch, parentAlpha);
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

    private void validateLinePositions() {
        if (top.dragging && top.getY() < bottom.getY()) {
            top.setY(bottom.getY() + pixelSize);
        }
        if (bottom.dragging && bottom.getY() > top.getY()) {
            bottom.setY(top.getY() - pixelSize);
        }
        if (right.dragging && right.getX() < left.getX()) {
            right.setX(left.getX() + pixelSize);
        }
        if (left.dragging && left.getX() > right.getX()) {
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
        private boolean dragging;
        private boolean ignoreNextExitEvent;

        public boolean isHovered() {
            return hovered;
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

    private static class PatchLine extends Actor {
        private static final Color colorRegular = new Color(0x99e550ff);
        private static final Color colorInteracting = new Color(0xffffffff);
        private static final Rectangle tmpRect = new Rectangle();

        private final Drawable drawable;
        private boolean interacting = false;
        private int value = 0;

        public PatchLine(Drawable drawable) {
            this.drawable = drawable;
            setTouchable(Touchable.disabled);
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float thickness = 3f;

            batch.setColor(tmpColor.set(interacting ? colorInteracting : colorRegular).mul(getColor()).mul(1f,1f,1f,parentAlpha));
            if (getWidth() > getHeight()) {
                // Horizontal line
                drawable.draw(batch, getX()-thickness*0.5f, getY()-thickness*0.5f, getWidth()+thickness, thickness);
            } else {
                // Vertical line
                drawable.draw(batch, getX()-thickness*0.5f, getY()-thickness*0.5f, thickness, getHeight()+thickness);
            }
        }

        /** Position in parent's coordinates */
        public void updateHover(float x, float y) {
            interacting = checkHit(x, y);
            return;
        }

        /** Position in parent's coordinates */
        public boolean checkHit(float x, float y) {
            return tmpRect.set(
                    getX() - EXTRA_TOUCH_SPACE,
                    getY() - EXTRA_TOUCH_SPACE,
                    getWidth() + EXTRA_TOUCH_SPACE *2f,
                    getHeight() + EXTRA_TOUCH_SPACE *2f)
                    .contains(x, y);
        }

        //region Dragging
        private float dragOffsetX, dragOffsetY;
        private boolean dragging = false;

        public void startDragging(float x, float y) {
            dragOffsetX = getX() - x;
            dragOffsetY = getY() - y;
            dragging = true;
        }

        public void drag(float x, float y) {
            setPosition(x + dragOffsetX, y + dragOffsetY);
        }

        public void endDragging(float x, float y) {
            dragging = false;
        }
        //endregion
    }
}
