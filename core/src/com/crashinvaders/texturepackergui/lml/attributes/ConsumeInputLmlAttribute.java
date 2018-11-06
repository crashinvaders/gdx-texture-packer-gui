package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class ConsumeInputLmlAttribute implements LmlAttribute<Actor> {
    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(LmlParser parser, LmlTag tag, final Actor actor, String rawAttributeData) {
        if (!parser.parseBoolean(rawAttributeData)) return;

        actor.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return true;
            }
        });

        // Post for one frame to the layout to be fully parsed and actor added to the stage.
        actor.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                actor.getStage().setKeyboardFocus(actor);
            }
        }));
    }
}
