package com.crashinvaders.common.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.DelegateAction;

/**
 * Skips N amount of frames before executes wrapped action.
 * Typical usecase is when you need execute action after parent actor has been laid out.
 */
public class PostAction extends DelegateAction {
    private int framesLeft;

    public static PostAction create(int skipFrames, Action action) {
        PostAction postAction = Actions.action(PostAction.class);
        postAction.setAction(action);
        postAction.framesLeft = skipFrames;
        return postAction;
    }

    @Override
    public void reset() {
        super.reset();
        framesLeft = 0;
    }

    @Override
    protected boolean delegate(float delta) {
        if (framesLeft > 0) {
            framesLeft--;
            return false;
        }

        if (action == null) return true;
        return action.act(delta);
    }
}
