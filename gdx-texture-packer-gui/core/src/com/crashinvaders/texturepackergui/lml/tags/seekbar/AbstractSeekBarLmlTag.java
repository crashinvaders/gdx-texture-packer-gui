package com.crashinvaders.texturepackergui.lml.tags.seekbar;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBarModel;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractActorLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;

public abstract class AbstractSeekBarLmlTag extends AbstractActorLmlTag {
    public AbstractSeekBarLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected SeekBar getNewInstanceOfActor(final LmlActorBuilder builder) {
        SeekBar.Style style = getSkin(builder).get(builder.getStyleName(), SeekBar.Style.class);
        return new SeekBar(createModel(builder), style);
    }

    /** @param builder used to build the widget, returned by {@link #getNewInstanceOfBuilder()}.
     * @return a new instance of {@link SeekBarModel}, handling seek bar's values. */
    protected abstract SeekBarModel createModel(LmlActorBuilder builder);

    /** @return wrapped actor, casted for convenience. */
    protected SeekBar getSeekBar() {
        return (SeekBar) getActor();
    }

    @Override
    protected void handleValidChild(final LmlTag childTag) {
        getParser().throwErrorIfStrict("SeekBars cannot have children. Found child: " + childTag.getActor()
                + " with tag: " + childTag.getTagName());
    }

    @Override
    protected void handlePlainTextLine(String plainTextLine) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean hasComponentActors() {
        return true;
    }

    @Override
    protected Actor[] getComponentActors(final Actor actor) {
        SeekBar seekBar = (SeekBar) actor;
        return new Actor[] {
                seekBar.getTextField(),
                seekBar.getSlider()
        };
    }
}
