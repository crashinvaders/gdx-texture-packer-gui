package com.crashinvaders.texturepackergui.config.tags;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.vis.parser.impl.tag.builder.IntRangeLmlActorBuilder;
import com.github.czyzby.lml.vis.parser.impl.tag.spinner.AbstractSpinnerLmlTag;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.SpinnerModel;

/**
 * Temporary patch for https://github.com/kotcrab/vis-editor/issues/219
 *
 * @author MJ
 * @author Metaphore
 */
public class FixedIntSpinnerLmlTag extends AbstractSpinnerLmlTag {
    private IntSpinnerModel model;

    public FixedIntSpinnerLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected LmlActorBuilder getNewInstanceOfBuilder() {
        return new IntRangeLmlActorBuilder();
    }

    @Override
    protected SpinnerModel createModel(final LmlActorBuilder builder) {
        final IntRangeLmlActorBuilder rangeBuilder = (IntRangeLmlActorBuilder) builder;
        return new IntSpinnerModel(rangeBuilder.getValue(), rangeBuilder.getMin(), rangeBuilder.getMax(), rangeBuilder.getStep()) {
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
                    float x = Integer.parseInt(input);
                    return x >= getMin() && x <= getMax();
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
    }

    @Override
    protected void handlePlainTextLine(final String plainTextLine) {
        model.setValue(getParser().parseInt(plainTextLine, getActor()), false);
    }
}
