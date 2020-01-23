package com.crashinvaders.texturepackergui.controllers.main;

import com.crashinvaders.texturepackergui.views.ExpandEditTextButton;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@SuppressWarnings("WeakerAccess")
public class PackSettingsActors {

    @LmlActor("skbMinPageWidth") SeekBar skbMinPageWidth;
    @LmlActor("skbMinPageHeight") SeekBar skbMinPageHeight;
    @LmlActor("skbMaxPageWidth") SeekBar skbMaxPageWidth;
    @LmlActor("skbMaxPageHeight") SeekBar skbMaxPageHeight;
    @LmlActor("skbAlphaThreshold") SeekBar skbAlphaThreshold;
    @LmlActor("cboMinFilter") VisSelectBox cboMinFilter;
    @LmlActor("cboMagFilter") VisSelectBox cboMagFilter;
    @LmlActor("skbPaddingX") SeekBar skbPaddingX;
    @LmlActor("skbPaddingY") SeekBar skbPaddingY;
    @LmlActor("cboWrapX") VisSelectBox cboWrapX;
    @LmlActor("cboWrapY") VisSelectBox cboWrapY;
    @LmlActor("eetbScaleFactors") ExpandEditTextButton eetbScaleFactors;
    @LmlActor("cbUseFastAlgorithm") VisCheckBox cbUseFastAlgorithm;
    @LmlActor("cbEdgePadding") VisCheckBox cbEdgePadding;
    @LmlActor("cbStripWhitespaceX") VisCheckBox cbStripWhitespaceX;
    @LmlActor("cbStripWhitespaceY") VisCheckBox cbStripWhitespaceY;
    @LmlActor("cbAllowRotation") VisCheckBox cbAllowRotation;
    @LmlActor("cbBleeding") VisCheckBox cbBleeding;
    @LmlActor("cbDuplicatePadding") VisCheckBox cbDuplicatePadding;
    @LmlActor("cbForcePot") VisCheckBox cbForcePot;
    @LmlActor("cbForceMof") VisCheckBox cbForceMof;
    @LmlActor("cbUseAliases") VisCheckBox cbUseAliases;
    @LmlActor("cbIgnoreBlankImages") VisCheckBox cbIgnoreBlankImages;
    @LmlActor("cbDebug") VisCheckBox cbDebug;
    @LmlActor("cbUseIndices") VisCheckBox cbUseIndices;
    @LmlActor("cbPremultiplyAlpha") VisCheckBox cbPremultiplyAlpha;
    @LmlActor("cbGrid") VisCheckBox cbGrid;
    @LmlActor("cbSquare") VisCheckBox cbSquare;
    @LmlActor("cbLimitMemory") VisCheckBox cbLimitMemory;
}
