package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

/** Captures stage's scroll focus on hover. */
public class ScrollFocusCaptureInputListener extends InputListener {

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        Actor actor = event.getListenerActor();
        Stage stage = actor.getStage();
        if (stage == null) return;

        if (stage.getScrollFocus() != actor) {
            stage.setScrollFocus(actor);
        }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        Actor actor = event.getListenerActor();
        Stage stage = actor.getStage();
        if (stage == null) return;

        // Stage fires "exit" event upon touchUp() even if pointer is still over the actor.
        // This is simple workaround.
        if (x > 0 && y > 0 && x < actor.getWidth() && y < actor.getHeight()) return;

        if (stage.getScrollFocus() == actor) {
            stage.setScrollFocus(null);
        }
    }
}
