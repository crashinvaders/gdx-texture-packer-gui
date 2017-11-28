
package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.util.LmlUtilities;

public abstract class ContainerPadLmlAttribute implements LmlAttribute<Container<?>> {
    @Override
    public Class<Container<?>> getHandledType() {
        // Double cast as there were a problem with generics - SomeClass.class cannot be returned as
        // <Class<SomeClass<?>>, even though casting never throws ClassCastException in the end.
        return (Class<Container<?>>) (Object) Container.class;
    }

    public static class Left extends ContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final Container<?> actor, final String rawAttributeData) {
            actor.padLeft(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class Right extends ContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final Container<?> actor, final String rawAttributeData) {
            actor.padRight(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class Top extends ContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final Container<?> actor, final String rawAttributeData) {
            actor.padTop(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class Bottom extends ContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final Container<?> actor, final String rawAttributeData) {
            actor.padBottom(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }

    public static class All extends ContainerPadLmlAttribute {
        @Override
        public void process(final LmlParser parser, final LmlTag tag, final Container<?> actor, final String rawAttributeData) {
            actor.pad(LmlUtilities.parseHorizontalValue(parser, tag.getParent(), actor, rawAttributeData));
        }
    }
}
