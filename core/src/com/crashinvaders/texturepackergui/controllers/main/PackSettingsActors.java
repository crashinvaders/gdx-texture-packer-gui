package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.crashinvaders.texturepackergui.views.ExpandEditTextButton;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

@SuppressWarnings("WeakerAccess")
public class PackSettingsActors {

    @LmlActor("btnCopySettings") Button btnCopySettings;
    @LmlActor("cboEncodingFormat") VisSelectBox cboEncodingFormat;
    @LmlActor("cboOutputFormat") VisSelectBox cboOutputFormat;
    @LmlActor("spnMinPageWidth") Spinner spnMinPageWidth;
    @LmlActor("spnMinPageHeight") Spinner spnMinPageHeight;
    @LmlActor("spnMaxPageWidth") Spinner spnMaxPageWidth;
    @LmlActor("spnMaxPageHeight") Spinner spnMaxPageHeight;
    @LmlActor("spnAlphaThreshold") Spinner spnAlphaThreshold;
    @LmlActor("cboMinFilter") VisSelectBox cboMinFilter;
    @LmlActor("cboMagFilter") VisSelectBox cboMagFilter;
    @LmlActor("spnPaddingX") Spinner spnPaddingX;
    @LmlActor("spnPaddingY") Spinner spnPaddingY;
    @LmlActor("cboWrapX") VisSelectBox cboWrapX;
    @LmlActor("cboWrapY") VisSelectBox cboWrapY;
    @LmlActor("spnJpegQuality") Spinner spnJpegQuality;
    @LmlActor("eetbScaleFactors") ExpandEditTextButton eetbScaleFactors;
    @LmlActor("cbUseFastAlgorithm") VisCheckBox cbUseFastAlgorithm;
    @LmlActor("cbEdgePadding") VisCheckBox cbEdgePadding;
    @LmlActor("cbStripWhitespaceX") VisCheckBox cbStripWhitespaceX;
    @LmlActor("cbStripWhitespaceY") VisCheckBox cbStripWhitespaceY;
    @LmlActor("cbAllowRotation") VisCheckBox cbAllowRotation;
    @LmlActor("cbIncludeSubdirs") VisCheckBox cbIncludeSubdirs;
    @LmlActor("cbBleeding") VisCheckBox cbBleeding;
    @LmlActor("cbDuplicatePadding") VisCheckBox cbDuplicatePadding;
    @LmlActor("cbForcePot") VisCheckBox cbForcePot;
    @LmlActor("cbUseAliases") VisCheckBox cbUseAliases;
    @LmlActor("cbIgnoreBlankImages") VisCheckBox cbIgnoreBlankImages;
    @LmlActor("cbDebug") VisCheckBox cbDebug;
    @LmlActor("cbUseIndices") VisCheckBox cbUseIndices;
    @LmlActor("cbPremultiplyAlpha") VisCheckBox cbPremultiplyAlpha;
    @LmlActor("cbGrid") VisCheckBox cbGrid;
    @LmlActor("cbSquare") VisCheckBox cbSquare;
}
