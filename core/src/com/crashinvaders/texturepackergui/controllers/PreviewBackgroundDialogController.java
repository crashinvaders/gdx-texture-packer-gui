package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.common.Timer;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.color.BasicColorPicker;

@ViewDialog(id = "dialog_preview_background", value = "lml/preview/dialogPreviewBackground.lml")
public class PreviewBackgroundDialogController implements ActionContainer {
    private static final String TAG = PreviewBackgroundDialogController.class.getSimpleName();
    private static final float NOTIFICATION_CUTOFF = 0.25f;

    @Inject ModelService modelService;
    @ViewStage Stage stage;

    @LmlActor("root") VisDialog root;
    @LmlActor("colorPicker") BasicColorPicker colorPicker;

    private final Color selectedColor = new Color();
    private TimerAction timerAction;

    @LmlAfter void initView() {
        timerAction = new TimerAction(NOTIFICATION_CUTOFF, new Timer.Listener() {
            @Override
            public void onTimeUp() {
                updateProjectProperty();
            }
        });
        stage.addAction(timerAction);

        selectedColor.set(modelService.getProject().getPreviewBackgroundColor());
        colorPicker.setColor(selectedColor);
    }

    @LmlAction("onColorChanged") void onColorChanged(Color color) {
        if (root == null) return;

        selectedColor.set(color);
        timerAction.restartTimer();
    }

    private void updateProjectProperty() {
        modelService.getProject().setPreviewBackgroundColor(selectedColor);
    }

    private static class TimerAction extends Action {

        private final Timer timer = new Timer();

        public TimerAction(float duration, Timer.Listener listener) {
            timer.setTotalDuration(duration);
            timer.setListener(listener);
        }

        @Override
        public boolean act(float delta) {
            timer.update(delta);
            return false;
        }

        public void restartTimer() {
            timer.restart();
        }

        public Timer getTimer() {
            return timer;
        }
    }
}
