package com.crashinvaders.texturepackergui.config.attributes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class OnDoubleClickLmlAttribute implements LmlAttribute<Actor> {
    private static final long SECOND_CLICK_TIME = 250000000L; // 250ms

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        final ActorConsumer<?, Params> action = parser.parseAction(rawAttributeData, tmpParams);
        if (action == null) {
            parser.throwError("Could not find action for: " + rawAttributeData + " with actor: " + actor);
        }
        actor.addListener(new ClickListener(0) {
            private boolean firstClickCaught = false;
            private long lastClickTime = 0;

            @Override
            public void clicked(final InputEvent event, final float x, final float y) {
                long currentEventTime = Gdx.input.getCurrentEventTime();
                long deltaTime = currentEventTime - lastClickTime;
                lastClickTime = currentEventTime;

                if (!firstClickCaught) {
                    firstClickCaught = true;
                } else {
                    if (deltaTime < SECOND_CLICK_TIME) {
                        firstClickCaught = false;

                        tmpParams.actor = actor;
                        tmpParams.x = x;
                        tmpParams.y = y;
                        tmpParams.stageX = event.getStageX();
                        tmpParams.stageY = event.getStageY();
                        action.consume(tmpParams);
                        tmpParams.reset();
                    }
                }
            }
        });
    }

    private static Params tmpParams = new Params();
    public static class Params {
        public Actor actor;
        public float x, y;
        public float stageX, stageY;

        public void reset() {
            actor = null;
        }
    }
}
