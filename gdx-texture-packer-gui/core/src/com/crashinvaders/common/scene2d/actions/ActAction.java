package com.crashinvaders.common.scene2d.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActAction extends Action {

    private float delta;

    public void setDelta(float delta) {
        this.delta = delta;
    }

    @Override
    public void reset() {
        super.reset();
        delta = 0f;
    }

    @Override
    public boolean act(float delta) {
        final Actor target = getTarget();
        final float timeDelta = this.delta;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                target.act(timeDelta);
            }
        });
        return true;
    }
}
