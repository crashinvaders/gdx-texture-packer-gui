package com.crashinvaders.texturepackergui.config.tags;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.vis.parser.impl.tag.builder.StringRangeLmlActorBuilder;
import com.github.czyzby.lml.vis.parser.impl.tag.spinner.AbstractSpinnerLmlTag;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.SpinnerModel;

import java.math.BigDecimal;

/**
 * Temporary patch for https://github.com/kotcrab/vis-editor/issues/219
 *
 * @author MJ
 * @author Metaphore
 */
public class FixedFloatSpinnerLmlTag extends AbstractSpinnerLmlTag {
    private FloatSpinnerModel model;

    public FixedFloatSpinnerLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected LmlActorBuilder getNewInstanceOfBuilder() {
        return new StringRangeLmlActorBuilder();
    }

    @Override
    protected SpinnerModel createModel(final LmlActorBuilder builder) {
        final StringRangeLmlActorBuilder rangeBuilder = (StringRangeLmlActorBuilder) builder;
        return new FloatSpinnerModel(rangeBuilder.getValue(), rangeBuilder.getMin(), rangeBuilder.getMax(), rangeBuilder.getStep(), 2) {
            @Override
            public void textChanged() {
                super.textChanged();

                String text = spinner.getTextField().getText();
                if (checkInputBounds(text)) {
                    spinner.notifyValueChanged(true);
                }
            }

            private boolean checkInputBounds (String input) {
                try {
                    BigDecimal x = new BigDecimal(input);
                    return x.compareTo(getMin()) >= 0 && x.compareTo(getMax()) <= 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
    }

    @Override
    protected void handlePlainTextLine(final String plainTextLine) {
        try {
            model.setValue(new BigDecimal(getParser().parseString(plainTextLine, getActor())), false);
        } catch (final NumberFormatException exception) {
            getParser().throwErrorIfStrict("Invalid spinner data, big decimal expected: " + plainTextLine, exception);
        }
    }
}
