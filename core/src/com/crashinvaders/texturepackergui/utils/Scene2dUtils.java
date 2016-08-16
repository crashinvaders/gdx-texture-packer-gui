package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

@SuppressWarnings("WeakerAccess")
public class Scene2dUtils {
    private static final Vector2 tmpVec2 = new Vector2();
    private static final InputEvent tmpInputEvent = new InputEvent();

    public static void simulateClick(Actor actor, int button, int pointer) {
        simulateClick(actor, button, pointer, 0f, 0f);
    }

    public static void simulateClick(Actor actor, int button, int pointer, float localX, float localY) {
        Vector2 pos = actor.stageToLocalCoordinates(tmpVec2.set(localX, localY));
        simulateClickGlobal(actor, button, pointer, pos.x, pos.y);
    }

    public static void simulateClickGlobal(Actor actor, int button, int pointer, float stageX, float stageY) {
        InputEvent event = tmpInputEvent;
        event.setStage(actor.getStage());
        event.setButton(button);
        event.setPointer(pointer);
        event.setStageX(stageX);
        event.setStageY(stageY);
        event.setType(InputEvent.Type.touchDown);
        actor.fire(event);
        event.setType(InputEvent.Type.touchUp);
        actor.fire(event);
        event.reset();
    }
}
