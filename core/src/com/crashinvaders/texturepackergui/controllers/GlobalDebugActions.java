package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;

import java.util.Locale;

@ViewActionContainer("debug")
public class GlobalDebugActions implements ActionContainer {

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject EventDispatcher eventDispatcher;
    @Inject ModelService modelService;

    @LmlAction({"reloadView", "reloadScreen"}) public void reloadScreen() {
        interfaceService.reload();
    }

    @LmlAction("reloadLocalization") public void reloadLocalization() {
        String language = localeService.getCurrentLocale().getLanguage();
        I18NBundle bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale(language));
        interfaceService.getParser().getData().addI18nBundle("default", bundle);
    }

    @LmlAction("reloadAppForce") public void reloadAppForce() {
        Gdx.app.postRunnable(() -> App.inst().restart());
    }

    @LmlAction("simulateCrash") public void simulateCrash() {
        throw new GdxRuntimeException("This is simulated debug exception.");
    }

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }
    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }

    private PackModel getSelectedPack() {
        return getProject().getSelectedPack();
    }

    private ProjectModel getProject() {
        return modelService.getProject();
    }

    private Stage getStage() {
        return interfaceService.getCurrentController().getStage();
    }
}
