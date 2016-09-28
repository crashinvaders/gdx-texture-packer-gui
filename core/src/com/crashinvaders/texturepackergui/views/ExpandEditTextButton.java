package com.crashinvaders.texturepackergui.views;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.StringBuilder;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.TableLmlTag;
import com.github.czyzby.lml.parser.impl.tag.builder.TextLmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.kotcrab.vis.ui.widget.VisLabel;

public class ExpandEditTextButton extends Button {

    private final Style style;
    private final VisLabel label;
    private Cell labelCell;
    private Cell expandIconCell;

    public ExpandEditTextButton(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Style.class));
    }

    public ExpandEditTextButton(String text, Style style) {
        super(style);
        this.style = style;

        label = new VisLabel(text, style.labelStyle);
        label.setAlignment(Align.left);
        label.setEllipsis(true);

        labelCell = add(label).growX().left().width(new LabelCellWidthValue());

        if (style.expandIcon != null) {
            Image image = new Image(style.expandIcon);
            image.setScaling(Scaling.none);

            expandIconCell = add(image).padLeft(4f);
        }
    }

    @Override
    public Style getStyle() {
        return style;
    }

    public VisLabel getLabel() {
        return label;
    }

    public Cell getLabelCell() {
        return labelCell;
    }

    public void setText(CharSequence newText) {
        label.setText(newText);
    }

    public StringBuilder getText() {
        return label.getText();
    }

    public static class Style extends ButtonStyle {
        public VisLabel.LabelStyle labelStyle;
        /** Optional. */
        public Drawable expandIcon;

        public Style() {
        }

        public Style(Drawable up, Drawable down, Drawable checked, VisLabel.LabelStyle labelStyle) {
            super(up, down, checked);
            this.labelStyle = labelStyle;
        }

        public Style(Style style) {
            super(style);
            this.labelStyle = style.labelStyle;
        }
    }

    public static class LmlTag extends TableLmlTag {

        public LmlTag(LmlParser parser, com.github.czyzby.lml.parser.tag.LmlTag parentTag, java.lang.StringBuilder rawTagData) {
            super(parser, parentTag, rawTagData);
        }

        @Override
        protected TextLmlActorBuilder getNewInstanceOfBuilder() {
            return new TextLmlActorBuilder();
        }

        @Override
        protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
            return getNewInstanceOfActor((TextLmlActorBuilder)builder);
        }

        protected Actor getNewInstanceOfActor(final TextLmlActorBuilder builder) {
            return new ExpandEditTextButton(builder.getText(), getSkin(builder), builder.getStyleName());
        }
    }

    public static class LmlTagProvider implements com.github.czyzby.lml.parser.tag.LmlTagProvider {
        @Override
        public com.github.czyzby.lml.parser.tag.LmlTag create(final LmlParser parser, final com.github.czyzby.lml.parser.tag.LmlTag parentTag, final java.lang.StringBuilder rawTagData) {
            return new LmlTag(parser, parentTag, rawTagData);
        }
    }

    private class LabelCellWidthValue extends Value {
        @Override
        public float get(Actor context) {
            float extraSpace = 0f;
            if (expandIconCell != null) {
                extraSpace += expandIconCell.getPrefWidth();
                extraSpace += expandIconCell.getPadLeft() + expandIconCell.getPadRight();
            }
            return getWidth() - 8f - extraSpace;
        }
    }
}
