package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnMessage;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.config.AutumnMessage;
import com.github.czyzby.autumn.mvc.stereotype.preference.StageViewport;
import com.github.czyzby.kiwi.util.gdx.asset.lazy.provider.ObjectProvider;

import static com.github.czyzby.autumn.mvc.config.AutumnActionPriority.*;

@Component
public class ViewportService {
    private static final String PREF_KEY_UI_SCALE = "ui_scale";

    @StageViewport
    ObjectProvider<Viewport> viewportProvider = new ObjectProvider<Viewport>() {
        @Override
        public Viewport provide() {
            return viewport;
        }
    };

    @Inject App app;
    @Inject InterfaceService interfaceService;

    private Preferences prefs;
    private ScreenViewport viewport;
    private float scale = 1f;

    @Initiate(priority = VERY_HIGH_PRIORITY) void init() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
        scale = prefs.getFloat(PREF_KEY_UI_SCALE, scale);

        viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(scale);
    }

    @OnMessage(AutumnMessage.GAME_RESIZED) void onResize() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public void setScale(float scale) {
        if (this.scale == scale) return;

        this.scale = scale;
        prefs.putFloat(PREF_KEY_UI_SCALE, scale).flush();
        updateViewport();
    }

    public float getScale() {
        return scale;
    }

    public Viewport getViewport() {
        return viewport;
    }

    private void updateViewport() {
        viewport.setUnitsPerPixel(scale);

        ViewController currentController = interfaceService.getCurrentController();
        if (currentController != null) {
            currentController.getStage().cancelTouchFocus();
        }

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                app.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });
    }
}
