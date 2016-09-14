
package com.crashinvaders.texturepackergui.config.attributes;

import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.util.LmlUtilities;

public abstract class ShrinkContainerPadLmlAttribute implements LmlAttribute<ShrinkContainer<?>> {
    @Override
    public Class<ShrinkContainer<?>> getHandledType() {
        // Double cast as there were a problem with generics - SomeClass.class cannot be returned as
        // <Class<SomeClass<?>>, even though casting never throws ClassCastException in the end.
        return (Class<ShrinkContainer<?>>) (Object) ShrinkContainer.class;
    }

    public static class Left extends ShrinkContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final ShrinkContainer<?> actor, final String rawAttributeData) {
            actor.padLeft(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class Right extends ShrinkContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final ShrinkContainer<?> actor, final String rawAttributeData) {
            actor.padRight(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class Top extends ShrinkContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final ShrinkContainer<?> actor, final String rawAttributeData) {
            actor.padTop(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class Bottom extends ShrinkContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final ShrinkContainer<?> actor, final String rawAttributeData) {
            actor.padBottom(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class All extends ShrinkContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final ShrinkContainer<?> actor, final String rawAttributeData) {
            actor.pad(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }
}
