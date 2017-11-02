package com.crashinvaders.texturepackergui.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
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
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.kiwi.util.gdx.asset.lazy.provider.ObjectProvider;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.LmlSyntax;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileUtils;

import java.nio.ByteBuffer;
import java.util.Locale;

@SuppressWarnings("unused")
@Component
public class Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

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
        Skin skin = new Skin();
        {
            FreeTypeFontGenerator.setMaxTextureSize(2048);
            FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("VisOpenSansKerned.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter paramsDefault = new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramsDefault.color = new Color(0xffffffe8);
            paramsDefault.size = 15;
            paramsDefault.incremental = true;
            paramsDefault.renderCount = 1;
            paramsDefault.gamma = 1.0f;
            paramsDefault.hinting = FreeTypeFontGenerator.Hinting.Full;
            BitmapFont fontDefault = fontGenerator.generateFont(paramsDefault);
            FreeTypeFontGenerator.FreeTypeBitmapFontData ftFontData = (FreeTypeFontGenerator.FreeTypeBitmapFontData) fontDefault.getData();

            FileHandle cjkFontFile = Gdx.files.external(".gdxtexturepackergui/cjk-font/NotoSansCJK-Regular.ttc");
            if (cjkFontFile.exists()) {
                Gdx.app.log(TAG, "CJK font initialized");
                ftFontData.addGenerator(new FreeTypeFontGenerator(cjkFontFile));
            }

            skin.add("default-font", fontDefault, BitmapFont.class);
        }
        skin.addRegions(new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas")));
        skin.load(Gdx.files.internal("skin/uiskin.json"));
        VisUI.load(skin);
        skinService.addSkin("default", skin);

        for (BitmapFont font : skin.getAll(BitmapFont.class).values()) {
            font.getData().markupEnabled = true;
            font.getData().missingGlyph = font.getData().getGlyph((char)0xFFFD);
        }

        // Extracting all colors from the skin and importing them into global color collection
        for (ObjectMap.Entry<String, Color> entry : skin.getAll(Color.class)) {
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
        parser.getData().addArgument("projectExt", "."+AppConstants.PROJECT_FILE_EXT);
        parser.getData().addArgument("imageExt", "."+Strings.join(" .", AppConstants.IMAGE_FILE_EXT));


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
        if (!projectFile.exists()) {
            Gdx.app.error(TAG, "Project file: " + projectFile + " doesn't exists.");
            return;
        }

        ProjectModel project = projectSerializer.loadProject(projectFile);
        project.setProjectFile(projectFile);

        modelService.setProject(project);
    }
}