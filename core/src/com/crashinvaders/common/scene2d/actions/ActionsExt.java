package com.crashinvaders.common.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.action;

public class ActionsExt {

    public static Action target(Actor target, Action action) {
        action.setTarget(target);
        return action;
    }

    public static Action unfocus() {
        return action(UnfocusAction.class);
    }

    public static Action unfocus(Actor actor) {
        UnfocusAction action = action(UnfocusAction.class);
        action.setTarget(actor);
        return action;
    }

    /** @see PostAction */
    public static PostAction post(Action action) {
        return PostAction.create(1, action);
    }

    /** @see PostAction */
    public static PostAction post(int skipFrames, Action action) {
        return PostAction.create(skipFrames, action);
    }

    /** Calls target#act(delta) */
    public static ActAction act(float delta) {
        ActAction action = action(ActAction.class);
        action.setDelta(delta);
        return action;
    }
}
