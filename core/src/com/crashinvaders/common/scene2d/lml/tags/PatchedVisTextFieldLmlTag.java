package com.crashinvaders.common.scene2d.lml.tags;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.widget.PatchedVisTextField;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractNonParentalActorLmlTag;
import com.github.czyzby.lml.parser.impl.tag.builder.TextLmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.github.czyzby.lml.util.LmlUtilities;

/** Handles {@link PatchedVisTextField} actor. Appends plain text between tags to itself. Mapped to "textField",
 * "visTextField".
 *
 * @author MJ */
public class PatchedVisTextFieldLmlTag extends AbstractNonParentalActorLmlTag {
    public PatchedVisTextFieldLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected TextLmlActorBuilder getNewInstanceOfBuilder() {
        return new TextLmlActorBuilder();
    }

    @Override
    protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
        final TextLmlActorBuilder textBuilder = (TextLmlActorBuilder) builder;
        return getNewInstanceOfTextField(textBuilder);
    }

    /** @param textBuilder contains initial text data and style.
     * @return a new instance of {@link PatchedVisTextField}. */
    protected PatchedVisTextField getNewInstanceOfTextField(final TextLmlActorBuilder textBuilder) {
        return new PatchedVisTextField(textBuilder.getText(), textBuilder.getStyleName());
    }

    /** @return casted actor. */
    protected PatchedVisTextField getTextField() {
        return (PatchedVisTextField) getActor();
    }

    @Override
    protected void handlePlainTextLine(final String plainTextLine) {
        final PatchedVisTextField textField = getTextField();
        final String textToAppend = getParser().parseString(plainTextLine, getActor());
        if (Strings.isEmpty(textField.getText())) {
            textField.setText(textToAppend);
        } else {
            if (LmlUtilities.isMultiline(textField)) {
                textField.appendText('\n' + textToAppend);
            } else {
                textField.appendText(textToAppend);
            }
        }
    }

    public static class Provider implements LmlTagProvider {
        @Override
        public LmlTag create(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
            return new PatchedVisTextFieldLmlTag(parser, parentTag, rawTagData);
        }
    }
}
