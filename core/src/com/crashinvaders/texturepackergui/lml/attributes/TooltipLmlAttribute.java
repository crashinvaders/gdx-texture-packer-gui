package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * @see com.github.czyzby.lml.parser.impl.attribute.TooltipLmlAttribute
 * Adds line wrapping for long text.
 */
public class TooltipLmlAttribute implements LmlAttribute<Actor> {
    private static final float LINE_WRAP_THRESHOLD = 400f;

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        VisLabel lblText = new VisLabel(parser.parseString(rawAttributeData, actor));
        lblText.setAlignment(Align.center);
        boolean needLineWrap = lblText.getPrefWidth() > LINE_WRAP_THRESHOLD;
        if (needLineWrap) {
            lblText.setWrap(true);
        }

        final Tooltip tooltip = new Tooltip();
        tooltip.clearChildren(); // Removing empty cell with predefined paddings.
        Cell<VisLabel> tooltipCell = tooltip.add(lblText).center().pad(0f, 4f, 2f, 4f);
        if (needLineWrap) { tooltipCell.width(LINE_WRAP_THRESHOLD); }
        tooltip.pack();
        tooltip.setTarget(actor);
    }
}
