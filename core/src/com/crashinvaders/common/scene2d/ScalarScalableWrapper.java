package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

/**
 * Container that can scale its child while works correct in layout.
 * This group overrides scale related methods and use internal scale values that are used to compute child's size (to layouts and other native Scene2D mechanics scale of this actor always will be 1.0f).
 * The child will receive different size values depending on the scale value and should layout properly.
 */
public class ScalarScalableWrapper<T extends Actor> extends WidgetGroup {

    private T actor;
    private float origWidth = 0f;
    private float origHeight = 0f;
    private float scaleX = 1f;
    private float scaleY = 1f;

    public ScalarScalableWrapper() {
        super.setTransform(false);
    }

    public ScalarScalableWrapper(T actor) {
        this();
        setActor(actor);
    }

    @Override
    public void layout() {
        super.layout();

        if (actor != null) {
            actor.setPosition(0f, 0f);
            actor.setSize(getWidth(), getHeight());
        }
    }

    @Override
    public float getPrefWidth() {
        if (actor == null) return 0f;

        if (actor instanceof Layout) {
            Layout layout = (Layout) actor;
            return layout.getPrefWidth() * scaleX;
        } else {
            return origWidth * scaleX;
        }
    }

    @Override
    public float getPrefHeight() {
        if (actor == null) return 0f;

        if (actor instanceof Layout) {
            Layout layout = (Layout) actor;
            return layout.getPrefHeight() * scaleY;
        } else {
            return origHeight * scaleY;
        }
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

    @Override
    public float getScaleX() {
        return scaleX;
    }

    @Override
    public float getScaleY() {
        return scaleY;
    }

    @Override
    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
        invalidateHierarchy();
    }

    @Override
    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
        invalidateHierarchy();
    }

    @Override
    public void setScale(float scaleXY) {
        this.setScale(scaleXY, scaleXY);
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        invalidateHierarchy();
    }

    @Override
    public void scaleBy(float scale) {
        this.scaleBy(scale, scaleY);
    }

    @Override
    public void scaleBy(float scaleX, float scaleY) {
        this.scaleX += scaleX;
        this.scaleY += scaleY;
        invalidateHierarchy();
    }

    @Deprecated
    @Override
    public void setTransform(boolean transform) {
        throw new UnsupportedOperationException();
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
