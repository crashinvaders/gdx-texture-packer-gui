package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.crashinvaders.common.scene2d.actions.ActionsExt;

class SettingsCommons {
    public static Action getSectionContentInAnimation() {
        return Actions.sequence(
                Actions.alpha(0f),
                ActionsExt.skipFrames(1),
                Actions.moveBy(72f, 0f),
                Actions.parallel(
                        Actions.fadeIn(0.15f),
                        Actions.moveBy(-72f, 0f, 0.25f, Interpolation.exp5Out)
                )
        );
    }
}
