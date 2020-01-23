package com.crashinvaders.texturepackergui.providers;

import com.crashinvaders.texturepackergui.App;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

@Provider
public class AppProvider implements DependencyProvider<App> {

    @Override
    public Class<App> getDependencyType() {
        return App.class;
    }

    @Override
    public App provide() {
        return App.inst();
    }
}
