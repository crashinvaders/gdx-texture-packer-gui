package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;

/**
 * Same old {@link Container}, but gets shrunk when invisible
 */
public class ShrinkContainer<T extends Actor> extends Container<T> {

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        invalidateHierarchy();
    }

    @Override
    public float getPrefWidth() {
        if (!isVisible()) return 0f;
        return super.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        if (!isVisible()) return 0f;
        return super.getPrefHeight();
    }

    @Override
    public float getMinWidth() {
        if (!isVisible()) return 0f;
        return super.getMinWidth();
    }

    @Override
    public float getMinHeight() {
        if (!isVisible()) return 0f;
        return super.getMinHeight();
    }
}
