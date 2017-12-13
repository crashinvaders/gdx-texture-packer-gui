package com.crashinvaders.common.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.crashinvaders.common.Timer;

/**
 * Action that works just like regular {@link ChangeListener}, but accumulates change events for certain time threshold.
 */
public class TimeThresholdChangeListenerAction extends Action implements Timer.Listener {

    private final Timer timer = new Timer();
    private final DelayedChangeListener delayedChangeListener;
    private final ChangeListener changeListener;

    /**
     * @param threshold in seconds
     */
    public TimeThresholdChangeListenerAction(final float threshold, DelayedChangeListener delayedChangeListener) {
        this.delayedChangeListener = delayedChangeListener;
        this.timer.setListener(this);

        this.changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                timer.start(threshold, TimeThresholdChangeListenerAction.this);
            }
        };
    }

    @Override
    public void setTarget(Actor target) {
        if (target != null) {
            target.addListener(changeListener);
        } else {
            // If present, forcefully finish last delayed change event.
            if (timer.isRunning()) {
                timer.update(timer.getTimeLeft()+1f);
            }
            this.target.removeListener(changeListener);
        }

        super.setTarget(target);
    }

    @Override
    public boolean act(float delta) {
        timer.update(delta);
        return false;
    }

    @Override
    public void onTimeUp() {
        delayedChangeListener.onActorChanged(target);
    }

    public interface DelayedChangeListener {
        void onActorChanged(Actor actor);
    }
}
