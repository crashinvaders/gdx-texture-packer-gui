package com.crashinvaders.texturepackergui.config.attributes;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.attribute.container.AbstractSharedContainerAndCellLmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.util.LmlUtilities;

public class ContainerPadRightLmlAttribute extends AbstractSharedContainerAndCellLmlAttribute {
    @Override
    protected void applyToContainer(final LmlParser parser, final LmlTag tag, final Container<?> actor,
                                    final String rawAttributeData) {
        actor.padRight(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
    }
    @Override
    protected void applyToCell(final Container<?> actor, final Cell<?> cell) {
        cell.padRight(actor.getPadRight()); // Any could do, height(Value) sets all.
    }
}
