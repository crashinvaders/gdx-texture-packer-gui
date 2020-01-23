package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Container that can scale its child while works correct in layout.
 * This group uses transform matrix to render its child properly.
 * The child won't be aware of scaling and may look distorted if X and Y scales are not the same.
 */
public class TransformScalableWrapper<T extends Actor> extends WidgetGroup {
    private static final Vector2 tmpVec2 = new Vector2();

    private T actor;
    private float origWidth = 0f;
    private float origHeight = 0f;

    public TransformScalableWrapper() {
        super.setTransform(true);
    }

    public TransformScalableWrapper(T actor) {
        this();
        setActor(actor);
    }

    @Override
    public void layout() {
        super.layout();

        if (actor != null) {
            actor.setPosition(0f, 0f);
            if (actor instanceof Layout) {
                Layout layout = (Layout) actor;
                actor.setSize(layout.getPrefWidth(), layout.getPrefHeight());
            } else {
                actor.setSize(origWidth, origHeight);
            }
        }
    }

    @Override
    public float getPrefWidth() {
        if (actor == null) return 0f;

        if (actor instanceof Layout) {
            Layout layout = (Layout) actor;
            return layout.getPrefWidth() * getScaleX();
        } else {
            return origWidth * getScaleX();
        }
    }

    @Override
    public float getPrefHeight() {
        if (actor == null) return 0f;

        if (actor instanceof Layout) {
            Layout layout = (Layout) actor;
            return layout.getPrefHeight() * getScaleY();
        } else {
            return origHeight * getScaleY();
        }
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
        invalidateHierarchy();
    }

    @Override
    public void setScaleY(float scaleY) {
        super.setScaleY(scaleY);
        invalidateHierarchy();
    }

    @Override
    public void setScale(float scaleXY) {
        super.setScale(scaleXY);
        invalidateHierarchy();
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        super.setScale(scaleX, scaleY);
        invalidateHierarchy();
    }

    @Override
    protected void drawDebugBounds(ShapeRenderer shapes) {
        if (!getDebug()) return;
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(getStage().getDebugColor());
        shapes.rect(getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1f, 1f, getRotation());
    }

    @Override
    public Actor hit (float x, float y, boolean touchable) {
        if (touchable && getTouchable() == Touchable.disabled) return null;
        Vector2 point = tmpVec2;
        SnapshotArray<Actor> children = getChildren();
        Actor[] childrenArray = children.items;
        for (int i = children.size - 1; i >= 0; i--) {
            Actor child = childrenArray[i];
            if (!child.isVisible()) continue;
            child.parentToLocalCoordinates(point.set(x, y));
            Actor hit = child.hit(point.x, point.y, touchable);
            if (hit != null) return hit;
        }

        if (touchable && getTouchable() != Touchable.enabled) return null;
        return x >= 0 && x < getWidth() / getScaleX() && y >= 0 && y < getHeight() / getScaleY() ? this : null;
    }

    public void setActor(T actor) {
        if (this.actor == actor) return;

        if (this.actor != null) {
            super.removeActor(this.actor, true);
        }

        if (actor != null) {
            this.actor = actor;
            origWidth = actor.getWidth();
            origHeight = actor.getHeight();
            super.addActor(actor);
        }
    }

    public T getActor() {
        return actor;
    }

    @Deprecated
    @Override
    public void setTransform(boolean transform) {
        throw new UnsupportedOperationException("Transform is always enabled for this widget.");
    }

    //region Omitted child related methods
    @Deprecated
    @Override
    public void addActor(Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void addActorAt(int index, Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void addActorBefore(Actor actorBefore, Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void addActorAfter(Actor actorAfter, Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean removeActor(Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean removeActor(Actor actor, boolean unfocus) {
        throw new UnsupportedOperationException();
    }
    //endregion
}
