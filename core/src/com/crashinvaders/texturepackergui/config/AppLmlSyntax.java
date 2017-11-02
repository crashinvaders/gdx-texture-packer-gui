package com.crashinvaders.texturepackergui.config;

import com.crashinvaders.texturepackergui.config.attributes.*;
import com.crashinvaders.texturepackergui.config.tags.*;
import com.crashinvaders.texturepackergui.views.ExpandEditTextButton;
import com.crashinvaders.texturepackergui.views.canvas.Canvas;
import com.github.czyzby.lml.vis.parser.impl.VisLmlSyntax;

public class AppLmlSyntax extends VisLmlSyntax {

    @Override
    protected void registerActorTags() {
        super.registerActorTags();

        addTagProvider(new GroupLmlTag.TagProvider(), "group");
        addTagProvider(new Canvas.CanvasLmlTagProvider(), "canvas");
        addTagProvider(new FixedIntSpinnerLmlTagProvider(), "intSpinner");
        addTagProvider(new FixedFloatSpinnerLmlTagProvider(), "floatSpinner");
        addTagProvider(new ShrinkContainerLmlTag.TagProvider(), "shrinkContainer");
        addTagProvider(new ExpandEditTextButton.TagProvider(), "expandEditTextButton");
        addTagProvider(new BusyBarLmlTag.TagProvider(), "busybar");
    }

    @Override
    protected void registerCommonAttributes() {
        super.registerCommonAttributes();

        addAttributeProcessor(new PatchedOnClickLmlAttribute(), "onClick", "click");
        addAttributeProcessor(new OnRightClickLmlAttribute(), "onRightClick", "rightClick");
        addAttributeProcessor(new OnDoubleClickLmlAttribute(), "onDoubleClick", "doubleClick");
        addAttributeProcessor(new TimeThresholdChangeListenerLmlAttribute(), "delayedChange", "delayedOnChange", "timeThresholdChange");
        addAttributeProcessor(new ImageDrawableLmlAttribute(), "image", "drawable");
        addAttributeProcessor(new TooltipLmlAttribute(), "visTooltip", "tooltip");
        addAttributeProcessor(new KeyboardFocusChangedLmlAttribute(), "keyboardFocus");
        addAttributeProcessor(new OriginLmlAttribute(), "origin");
        addAttributeProcessor(new HexColorLmlAttribute(), "hexColor");
    }


    @Override
    protected void registerTableAttributes() {
        super.registerTableAttributes();

        addAttributeProcessor(new TableTiledBackgroundLmlAttribute(), "bgTiled", "backgroundTiled");
    }

    @Override
    protected void registerContainerAttributes() {
        super.registerContainerAttributes();

        addAttributeProcessor(new ContainerPadLmlAttribute.Top(), "containerPadTop");
        addAttributeProcessor(new ContainerPadLmlAttribute.Left(), "containerPadLeft");
        addAttributeProcessor(new ContainerPadLmlAttribute.Bottom(), "containerPadBottom");
        addAttributeProcessor(new ContainerPadLmlAttribute.Right(), "containerPadRight");
        addAttributeProcessor(new ContainerPadLmlAttribute.All(), "containerPad");
    }

    @Override
    protected void registerMenuAttributes() {
        super.registerMenuAttributes();

        addAttributeProcessor(new ShortcutOnChangeLmlAttribute(), "onchange", "change");
        addAttributeProcessor(new MenuItemFillImageLmlAttribute(), "fillImage");
    }

    @Override
    protected void registerLabelAttributes() {
        super.registerLabelAttributes();

        addAttributeProcessor(new LabelFontScaleLmlAttribute(), "fontScale");
    }
}
