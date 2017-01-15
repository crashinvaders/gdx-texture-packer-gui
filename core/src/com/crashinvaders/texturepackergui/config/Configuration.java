package com.crashinvaders.texturepackergui.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.AppParams;
import com.crashinvaders.texturepackergui.services.projectserializer.ProjectSerializer;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.component.ui.action.ActionProvider;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.autumn.mvc.stereotype.preference.*;
import com.github.czyzby.kiwi.util.gdx.asset.lazy.provider.ObjectProvider;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.LmlSyntax;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileUtils;

import java.util.Locale;

@SuppressWarnings("unused")
@Component
public class Configuration {

    @LmlParserSyntax LmlSyntax syntax = new AppLmlSyntax();

    @StageViewport
    ObjectProvider<Viewport> viewportProvider = new ObjectProvider<Viewport>() {
        @Override
        public Viewport provide() {
            return new ScreenViewport();
        }
    };

    /** These i18n-related fields will allow {@link LocaleService} to save game's locale in preferences file. Locale
     * changing actions will be automatically added to LML templates - see settings.lml. */
    @I18nLocale(propertiesPath = AppConstants.PREF_NAME_COMMON, defaultLocale = "en") String localePreference = "locale";
    @AvailableLocales String[] availableLocales = new String[] { "en", "ru", "de" };
    @I18nBundle String bundlePath = "i18n/bundle";

    @Initiate(priority = AutumnActionPriority.LOW_PRIORITY)
    public void initVisUiI18n(InterfaceService interfaceService, final LocaleService localeService) {
        Locales.setLocale(localeService.getCurrentLocale());
        interfaceService.setActionOnBundlesReload(new Runnable() {
            @Override
            public void run() {
                Locale locale = localeService.getCurrentLocale();
                Locales.setButtonBarBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/visui/buttonbar"), locale));
                Locales.setColorPickerBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/visui/colorpicker"), locale));
                Locales.setDialogsBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/visui/dialogs"), locale));
                Locales.setFileChooserBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/visui/filechooser"), locale));
                Locales.setTabbedPaneBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/visui/tabbedpane"), locale));
            }
        });
    }

    @Initiate(priority = AutumnActionPriority.TOP_PRIORITY)
    public void initiateSkin(final SkinService skinService) {
        VisUI.load("skin/uiskin.json");
        Skin skin = VisUI.getSkin();
        skinService.addSkin("default", skin);
        skin.getFont("default-font").getData().markupEnabled = true;
        skin.getFont("small-font").getData().markupEnabled = true;

        // Extracting all colors from the skin and importing them into global color collection
        for (ObjectMap.Entry<String, Color> entry : skinService.getSkin().getAll(Color.class)) {
            Colors.put(entry.key, entry.value);
        }
    }

    @Initiate(priority = AutumnActionPriority.TOP_PRIORITY)
    public void initializeInterface(InterfaceService interfaceService) {
        InterfaceService.DEFAULT_FADING_TIME = 0.15f;

        TooltipManager tooltipManager = interfaceService.getParser().getData().getDefaultTooltipManager();
        tooltipManager.initialTime = 0.75f;
        tooltipManager.hideAll();

        LmlParser parser = interfaceService.getParser();
        parser.parseTemplate(Gdx.files.internal("lml/titledPane.lml"));

        interfaceService.setShowingActionProvider(new ActionProvider() {
            @Override
            public Action provideAction(final ViewController forController, final ViewController connectedView) {
                return Actions.sequence(
                        Actions.alpha(0f),
                        Actions.fadeIn(InterfaceService.DEFAULT_FADING_TIME),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                App.inst().getInput().addProcessor(forController.getStage(), 0);
                            }
                        }));
            }
        });

        interfaceService.setHidingActionProvider(new ActionProvider() {
            @Override
            public Action provideAction(final ViewController forController, final ViewController connectedView) {
                return Actions.sequence(
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                App.inst().getInput().removeProcessor(forController.getStage());
                            }
                        }),
                        Actions.fadeOut(InterfaceService.DEFAULT_FADING_TIME));
            }
        });
    }

    // Try load initial project
    @Initiate(priority = -1000)
    public void startupProject(ModelService modelService, ProjectSerializer projectSerializer) {
        AppParams params = App.inst().getParams();
        if (params.startupProject == null) return;

        FileHandle projectFile = FileUtils.toFileHandle(params.startupProject);
        if (!projectFile.exists()) return;

        ProjectModel project = projectSerializer.loadProject(projectFile);
        project.setProjectFile(projectFile);

        modelService.setProject(project);
    }
}