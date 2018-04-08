package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Timer;

/** Accumulates changes for specified time threshold and then fires {@link #onChanged()} virtual method. */
public class TimeThresholdChangeListener implements EventListener {

    private final Timer timer = new Timer();
    private final Timer.Task timerTask;
    private final ChangeListener changeListener;
    private final FocusListener focusListener;

    /**
     * @param threshold in seconds
     */
    public TimeThresholdChangeListener(final float threshold) {
        this.timerTask = new Timer.Task() {
            @Override
            public void run() {
                onChanged();
            }
        };
        this.changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (timerTask.isScheduled()) {
                    timerTask.cancel();
                }
                timer.scheduleTask(timerTask, threshold);
            }
        };
        this.focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused && timerTask.isScheduled()) {
                    // Focus lost - immediately send change event if pending
                    timerTask.cancel();
                    onChanged();
                }
            }
        };
    }

    @Override
    public boolean handle(Event event) {
        return changeListener.handle(event) || focusListener.handle(event);
    }

    /** Virtual method that should be overridden to handle delayed change event. */
    public void onChanged() {

    }
}
