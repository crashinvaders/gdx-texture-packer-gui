package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

@SuppressWarnings("WeakerAccess")
public class PackSettingsController {

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
    @LmlActor("chkUseFastAlgorithm") VisCheckBox chkUseFastAlgorithm;
    @LmlActor("chkEdgePadding") VisCheckBox chkEdgePadding;
    @LmlActor("chkStripWhitespaceX") VisCheckBox chkStripWhitespaceX;
    @LmlActor("chkStripWhitespaceY") VisCheckBox chkStripWhitespaceY;
    @LmlActor("chkAllowRotation") VisCheckBox chkAllowRotation;
    @LmlActor("chkIncludeSubdirs") VisCheckBox chkIncludeSubdirs;
    @LmlActor("chkBleeding") VisCheckBox chkBleeding;
    @LmlActor("chkDuplicatePadding") VisCheckBox chkDuplicatePadding;
    @LmlActor("chkForcePot") VisCheckBox chkForcePot;
    @LmlActor("chkUseAliases") VisCheckBox chkUseAliases;
    @LmlActor("chkIgnoreBlankImages") VisCheckBox chkIgnoreBlankImages;
    @LmlActor("chkDebug") VisCheckBox chkDebug;
    @LmlActor("chkUseIndices") VisCheckBox chkUseIndices;
    @LmlActor("chkPremultiplyAlpha") VisCheckBox chkPremultiplyAlpha;
    @LmlActor("chkSquared") VisCheckBox chkSquared;
}
