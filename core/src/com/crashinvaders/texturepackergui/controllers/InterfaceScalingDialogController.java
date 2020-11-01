package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnMessage;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewDialogShower;
import com.github.czyzby.autumn.mvc.config.AutumnMessage;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;

@ViewDialog(id = InterfaceScalingDialogController.VIEW_ID, value = "lml/dialogInterfaceScaling.lml")
public class InterfaceScalingDialogController implements ActionContainer, ViewDialogShower {
    public static final String VIEW_ID = "InterfaceScalingDialogController";

    @Inject ViewportService viewportService;

    @LmlActor("dialogRoot") VisDialog dialogRoot;
    @LmlActor("lblUiScale") Label lblUiScale;
    @LmlActor("sliderUiScale") Slider sliderUiScale;

    private boolean viewInitialized = false;

    @LmlAfter
    void initView() {
        viewInitialized = true;
    }

    @Override
    public void doBeforeShow(Window dialog) {
        float scale = 1f / viewportService.getScale();

        lblUiScale.setText(formatScaleValue(scale));
        sliderUiScale.setValue(scale);
    }

    @OnMessage(AutumnMessage.GAME_RESIZED) void onResize() {
        if (viewInitialized) {
            dialogRoot.centerWindow();
        }
    }

    @LmlAction("onUiScaleChanged") void onUiScaleChanged() {
        float uiScale = sliderUiScale.getValue();
        lblUiScale.setText(formatScaleValue(uiScale));
    }

    @LmlAction("applyNewUiScale") void applyNewUiScale() {
        float uiScale = 1f / sliderUiScale.getValue();
        viewportService.setScale(uiScale);
    }

    private static String formatScaleValue(float uiScale) {
        return String.format("%.0f%%", uiScale*100f);
    }
}
