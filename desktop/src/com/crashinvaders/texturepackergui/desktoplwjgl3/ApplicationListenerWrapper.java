package com.crashinvaders.texturepackergui.desktoplwjgl3;

import com.badlogic.gdx.ApplicationListener;

public class ApplicationListenerWrapper implements ApplicationListener {
    private final ApplicationListener application;

    public ApplicationListenerWrapper(ApplicationListener application) {
        this.application = application;
    }

    @Override
    public void create() {
        application.create();
    }

    @Override
    public void resize(int width, int height) {
        application.resize(width, height);
    }

    @Override
    public void render() {
        application.render();
    }

    @Override
    public void pause() {
        application.pause();
    }

    @Override
    public void resume() {
        application.resume();
    }

    @Override
    public void dispose() {
        application.dispose();
    }
}
