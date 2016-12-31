package com.crashinvaders.texturepackergui.config;

import com.crashinvaders.texturepackergui.config.attributes.*;
import com.crashinvaders.texturepackergui.config.tags.FixedFloatSpinnerLmlTagProvider;
import com.crashinvaders.texturepackergui.config.tags.FixedIntSpinnerLmlTagProvider;
import com.crashinvaders.texturepackergui.config.tags.ShrinkContainerLmlTag;
import com.crashinvaders.texturepackergui.views.ExpandEditTextButton;
import com.crashinvaders.texturepackergui.views.canvas.Canvas;
import com.github.czyzby.lml.vis.parser.impl.VisLmlSyntax;

public class AppLmlSyntax extends VisLmlSyntax {

    @Override
    protected void registerActorTags() {
        super.registerActorTags();

        addTagProvider(new Canvas.CanvasLmlTagProvider(), "canvas");
        addTagProvider(new FixedIntSpinnerLmlTagProvider(), "intSpinner");
        addTagProvider(new FixedFloatSpinnerLmlTagProvider(), "floatSpinner");
        addTagProvider(new ShrinkContainerLmlTag.Provider(), "shrinkContainer");
        addTagProvider(new ExpandEditTextButton.LmlTagProvider(), "expandEditTextButton");
    }

    @Override
    protected void registerCommonAttributes() {
        super.registerCommonAttributes();

        addAttributeProcessor(new OnRightClickLmlAttribute(), "onRightClick", "rightClick");
        addAttributeProcessor(new OnDoubleClickLmlAttribute(), "onDoubleClick", "doubleClick");
        addAttributeProcessor(new ImageDrawableLmlAttribute(), "image", "drawable");
        addAttributeProcessor(new TooltipLmlAttribute(), "visTooltip", "tooltip");
    }

    @Override
    protected void registerTableAttributes() {
        super.registerTableAttributes();

        addAttributeProcessor(new TableTiledBackgroundLmlAttribute(), "bgTiled", "backgroundTiled");
    }

    @Override
    protected void registerContainerAttributes() {
        super.registerContainerAttributes();

//        addAttributeProcessor(new ContainerPadLeftLmlAttribute(), "padLeft");
//        addAttributeProcessor(new ContainerPadRightLmlAttribute(), "padRight");
//        addAttributeProcessor(new ContainerPadTopLmlAttribute(), "padTop");
//        addAttributeProcessor(new ContainerPadBottomLmlAttribute(), "padBottom");

        addAttributeProcessor(new ShrinkContainerPadLmlAttribute.Top(), "padTop");
        addAttributeProcessor(new ShrinkContainerPadLmlAttribute.Left(), "padLeft");
        addAttributeProcessor(new ShrinkContainerPadLmlAttribute.Bottom(), "padBottom");
        addAttributeProcessor(new ShrinkContainerPadLmlAttribute.Right(), "padRight");
        addAttributeProcessor(new ShrinkContainerPadLmlAttribute.All(), "pad");
    }

    @Override
    protected void registerMenuAttributes() {
        super.registerMenuAttributes();

        addAttributeProcessor(new ShortcutOnChangeLmlAttribute(), "onchange", "change");
    }
}
