package com.crashinvaders.texturepackergui.lml;

import com.crashinvaders.common.scene2d.lml.AnimatedImage;
import com.crashinvaders.common.scene2d.lml.tags.PatchedVisTextFieldLmlTag;
import com.crashinvaders.texturepackergui.lml.attributes.*;
import com.crashinvaders.texturepackergui.lml.attributes.seekbar.*;
import com.crashinvaders.texturepackergui.lml.tags.*;
import com.crashinvaders.texturepackergui.lml.tags.seekbar.FloatSeekBarLmlTag;
import com.crashinvaders.texturepackergui.lml.tags.seekbar.IntSeekBarLmlTag;
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
        addTagProvider(new TransformScalableWrapperLmlTag.TagProvider(), "transformScalable");
        addTagProvider(new ScalarScalableWrapperLmlTag.TagProvider(), "scalarScalable");
        addTagProvider(new AnimatedImage.LmlTag.Provider(), "animatedImage");
        addTagProvider(new PatchedVisTextFieldLmlTag.Provider(), "textField", "visTextField");
        addTagProvider(new IntSeekBarLmlTag.Provider(), "intSeekBar");
        addTagProvider(new FloatSeekBarLmlTag.Provider(), "floatSeekBar");
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();

        registerSeekBarAttributes();
    }

    @Override
    protected void registerBuildingAttributes() {
        super.registerBuildingAttributes();

        // IntSeekBarLmlBuilder:
        addBuildingAttributeProcessor(new IntSeekBarMaxLmlAttribute(), "max");
        addBuildingAttributeProcessor(new IntSeekBarMinLmlAttribute(), "min");
        addBuildingAttributeProcessor(new IntSeekBarStepLmlAttribute(), "step");
        addBuildingAttributeProcessor(new IntSeekBarValueLmlAttribute(), "value");

        // FloatSeekBarLmlBuilder:
        addBuildingAttributeProcessor(new FloatSeekBarMaxLmlAttribute(), "max");
        addBuildingAttributeProcessor(new FloatSeekBarMinLmlAttribute(), "min");
        addBuildingAttributeProcessor(new FloatSeekBarStepLmlAttribute(), "step");
        addBuildingAttributeProcessor(new FloatSeekBarValueLmlAttribute(), "value");
        addBuildingAttributeProcessor(new FloatSeekBarPrecisionLmlAttribute(), "precision");
    }

    @Override
    protected void registerCommonAttributes() {
        super.registerCommonAttributes();

        addAttributeProcessor(new PatchedOnClickLmlAttribute(), "onClick", "click");
        addAttributeProcessor(new OnRightClickLmlAttribute(), "onRightClick", "rightClick");
        addAttributeProcessor(new OnDoubleClickLmlAttribute(), "onDoubleClick", "doubleClick");
        addAttributeProcessor(new OnTouchUpLmlAttribute(), "onTouchUp", "touchUp");
        addAttributeProcessor(new ScrollFocusCaptureLmlAttribute(), "scrollCapture");
        addAttributeProcessor(new TimeThresholdChangeListenerLmlAttribute(), "delayedChange", "delayedOnChange", "timeThresholdChange");
        addAttributeProcessor(new TooltipLmlAttribute(), "visTooltip", "tooltip");
        addAttributeProcessor(new KeyboardFocusChangedLmlAttribute(), "keyboardFocus");
        addAttributeProcessor(new OriginLmlAttribute(), "origin");
        addAttributeProcessor(new HexColorLmlAttribute(), "hexColor");
        addAttributeProcessor(new OnBackPressedLmlAttribute(), "back", "onBack", "onEscape");
    }

    @Override
    protected void registerImageAttributes() {
        super.registerImageAttributes();

        addAttributeProcessor(new ImageTiledLmlAttribute(), "tiled");
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

    @Override
    protected void registerSpinnerAttributes() {
        super.registerSpinnerAttributes();

        addAttributeProcessor(new SpinnerSelectAllOnFocusLmlAttribute(), "selectAllOnFocus");
    }

    protected void registerSeekBarAttributes() {
        addAttributeProcessor(new SeekBarChangePolicyLmlAttribute(), "changePolicy");
    }
}
