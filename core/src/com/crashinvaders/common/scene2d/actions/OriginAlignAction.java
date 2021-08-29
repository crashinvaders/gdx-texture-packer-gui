package com.crashinvaders.common.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class OriginAlignAction extends Action {

    private int align;

    public void setAlign(int align) {
        this.align = align;
    }

    @Override
    public void reset() {
        super.reset();
        align = 0;
    }

    @Override
    public boolean act(float delta) {
        final Actor target = getTarget();
        target.setOrigin(align);
        return true;
    }
}