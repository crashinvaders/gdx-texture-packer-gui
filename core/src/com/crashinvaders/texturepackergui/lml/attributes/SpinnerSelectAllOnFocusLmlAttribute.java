package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

public class SpinnerSelectAllOnFocusLmlAttribute implements LmlAttribute<Spinner> {

    @Override
    public Class<Spinner> getHandledType() {
        return Spinner.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Spinner spinner, final String rawAttributeData) {
        boolean value = parser.parseBoolean(rawAttributeData, spinner);
        if (value == false) return;

        spinner.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (focused) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            spinner.getTextField().selectAll();
                        }
                    });
                }
            }
        });
    }
}
