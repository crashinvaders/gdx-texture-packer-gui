package com.crashinvaders.common.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class UnfocusAction extends Action {

    private boolean done = false;

    @Override
    public void restart() {
        super.restart();
        done = false;
    }

    @Override
    public boolean act(float delta) {
        if (done) return true;

        Actor target = getTarget();
        Stage stage = target.getStage();
        if (stage != null) {
            stage.unfocus(target);
        }
        done = true;
        return true;
    }
}
