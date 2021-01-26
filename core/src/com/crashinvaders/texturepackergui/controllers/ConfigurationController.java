package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.AppParams;
import com.crashinvaders.texturepackergui.controllers.extensionmodules.CjkFontExtensionModule;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.projectserializer.ProjectSerializer;
import com.crashinvaders.texturepackergui.lml.AppLmlSyntax;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Destroy;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.component.ui.action.ActionProvider;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.stereotype.preference.AvailableLocales;
import com.github.czyzby.autumn.mvc.stereotype.preference.I18nBundle;
import com.github.czyzby.autumn.mvc.stereotype.preference.I18nLocale;
import com.github.czyzby.autumn.mvc.stereotype.preference.LmlParserSyntax;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.LmlSyntax;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileUtils;

import java.util.Locale;

import static com.github.czyzby.autumn.mvc.config.AutumnActionPriority.HIGH_PRIORITY;
import static com.github.czyzby.autumn.mvc.config.AutumnActionPriority.VERY_HIGH_PRIORITY;

@SuppressWarnings("unused")
@Component
public class ConfigurationController {
    private static final String TAG = ConfigurationController.class.getSimpleName();
    private static final char UNKNOWN_CHARACTER = (char) 0xFFFD;

    @LmlParserSyntax LmlSyntax syntax = new AppLmlSyntax();

    /** These i18n-related fields will allow {@link LocaleService} to save game's locale in preferences file. Locale
     * changing actions will be automatically added to LML templates - see settings.lml. */
    @I18nLocale(propertiesPath = AppConstants.PREF_NAME_COMMON, defaultLocale = "en") String localePreference = "locale";
    @AvailableLocales String[] availableLocales = new String[] { "en", "ru", "de", "zh_TW" };
    @I18nBundle String bundlePath = "i18n/bundle";

    /** Keep track of all initialized FreeType font generators to dispose them later. */
    private final Array<FreeTypeFontGenerator> ftFontGenerators = new Array<>();

    @Initiate(priority = VERY_HIGH_PRIORITY)
    public void initiateSkin(AssetService assetService, final SkinService skinService, CjkFontExtensionModule cjkFontModule) {
        Skin skin = new Skin();
        {
            // FreeType incremental behavior is an awesome flexible solution, but a VERY memory hungry one.
            // So we will avoid it if it's not required.
            boolean incrementalFonts = cjkFontModule.isActivated();
            Gdx.app.log(TAG, "Incremental fonts are " + (incrementalFonts ? "enabled" : "disabled"));

            String defaultCharacters = FreeTypeFontGenerator.DEFAULT_CHARS;
            // Special characters
            defaultCharacters += UNKNOWN_CHARACTER;
            // Russian characters
            defaultCharacters += "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя№";

            // The less the page size, the less memory will be eaten (IDK why FreeType really works in that way).
            FreeTypeFontGenerator.setMaxTextureSize(1024);
            FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("VisOpenSansKerned.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter paramsDefault = new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramsDefault.color = new Color(0xffffffe8);
            paramsDefault.size = 15;
            paramsDefault.incremental = incrementalFonts;
            paramsDefault.renderCount = 1;
            paramsDefault.gamma = 1.0f;
            paramsDefault.hinting = FreeTypeFontGenerator.Hinting.Full;
            paramsDefault.characters = defaultCharacters;
            paramsDefault.magFilter = Texture.TextureFilter.Linear;
            paramsDefault.minFilter = Texture.TextureFilter.Linear;
            FreeTypeFontGenerator.FreeTypeFontParameter paramsSmall = new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramsSmall.color = new Color(0xffffffe8);
            paramsSmall.size = 12;
            paramsSmall.incremental = incrementalFonts;
            paramsSmall.renderCount = 1;
            paramsSmall.gamma = 0.5f;
            paramsSmall.hinting = FreeTypeFontGenerator.Hinting.Full;
            paramsSmall.characters = defaultCharacters;
            paramsSmall.magFilter = Texture.TextureFilter.Linear;
            paramsSmall.minFilter = Texture.TextureFilter.Linear;
            FreeTypeFontGenerator.FreeTypeFontParameter paramsBig = new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramsBig.color = new Color(0xffffffe8);
            paramsBig.size = 22;
            paramsBig.incremental = incrementalFonts;
            paramsBig.renderCount = 1;
            paramsBig.gamma = 0.75f;
            paramsBig.hinting = FreeTypeFontGenerator.Hinting.Full;
            paramsBig.characters = defaultCharacters;
            paramsBig.magFilter = Texture.TextureFilter.Linear;
            paramsBig.minFilter = Texture.TextureFilter.Linear;

            BitmapFont fontDefault = fontGenerator.generateFont(paramsDefault);
            BitmapFont fontSmall = fontGenerator.generateFont(paramsSmall);
            BitmapFont fontBig = fontGenerator.generateFont(paramsBig);

            if (incrementalFonts) {
                ftFontGenerators.add(fontGenerator); // Dispose font generator on application termination
            } else {
                fontGenerator.dispose();
                fontGenerator = null;
            }

            if (cjkFontModule.isActivated()) {
                Gdx.app.log(TAG, "Skin initialized with CJK font.");
                FreeTypeFontGenerator cjkFontGenerator = new FreeTypeFontGenerator(cjkFontModule.getFontFile());
                ftFontGenerators.add(cjkFontGenerator); // Dispose font generator on application termination

                FreeTypeFontGenerator.FreeTypeBitmapFontData ftFontDataDefault = (FreeTypeFontGenerator.FreeTypeBitmapFontData) fontDefault.getData();
                ftFontDataDefault.addGenerator(cjkFontGenerator);
                FreeTypeFontGenerator.FreeTypeBitmapFontData ftFontDataSmall = (FreeTypeFontGenerator.FreeTypeBitmapFontData) fontSmall.getData();
                ftFontDataSmall.addGenerator(cjkFontGenerator);
                FreeTypeFontGenerator.FreeTypeBitmapFontData ftFontDataBig = (FreeTypeFontGenerator.FreeTypeBitmapFontData) fontBig.getData();
                ftFontDataBig.addGenerator(cjkFontGenerator);
            }

            skin.add("default-font", fontDefault, BitmapFont.class);
            skin.add("small-font", fontSmall, BitmapFont.class);
            skin.add("big-font", fontBig, BitmapFont.class);
        }
        skin.addRegions(new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas")));
        skin.load(Gdx.files.internal("skin/uiskin.json"));
        VisUI.load(skin);
        skinService.addSkin("default", skin);

        for (BitmapFont font : skin.getAll(BitmapFont.class).values()) {
            BitmapFont.BitmapFontData fontData = font.getData();
            fontData.markupEnabled = true;
            fontData.missingGlyph = fontData.getGlyph(UNKNOWN_CHARACTER);

            // If missing glyph is set, bitmap font returns it in place of '\r' character.
            // We forcefully replace it with NBSP glyph for now.
            if (fontData.getGlyph('\r') == fontData.missingGlyph) {
                fontData.setGlyph('\r', fontData.getGlyph('\u00A0'));
            }

            // For some reason TAB glyph doesn't get generated.
            // So we create it manually using 4 times wider space glyph.
            if (fontData.getGlyph('\t') == fontData.missingGlyph) {
                BitmapFont.Glyph spaceGlyph = fontData.getGlyph(' ');
                BitmapFont.Glyph tabGlyph = new BitmapFont.Glyph();
                tabGlyph.id = '\t';
                tabGlyph.srcX = spaceGlyph.srcX;
                tabGlyph.srcY = spaceGlyph.srcY;
                tabGlyph.width = spaceGlyph.width;
                tabGlyph.height = spaceGlyph.height;
                tabGlyph.u = spaceGlyph.u;
                tabGlyph.v = spaceGlyph.v;
                tabGlyph.u2 = spaceGlyph.u2;
                tabGlyph.v2 = spaceGlyph.v2;
                tabGlyph.xoffset = spaceGlyph.xoffset;
                tabGlyph.yoffset = spaceGlyph.yoffset;
                tabGlyph.xadvance = spaceGlyph.xadvance * 4;
                tabGlyph.kerning = spaceGlyph.kerning;
                tabGlyph.fixedWidth = spaceGlyph.fixedWidth;
                fontData.setGlyph('\t', tabGlyph);
            }
        }

        // Extracting all colors from the skin and importing them into global color collection
        for (ObjectMap.Entry<String, Color> entry : skin.getAll(Color.class)) {
            Colors.put(entry.key, entry.value);
        }
    }

    @Destroy void disposeFreeTypeFontGenerators() {
        for (int i = 0; i < ftFontGenerators.size; i++) {
            ftFontGenerators.get(i).dispose();
        }
        ftFontGenerators.clear();
    }

    @Initiate(priority = HIGH_PRIORITY)
    public void initVisUiI18n(final InterfaceService interfaceService, final LocaleService localeService) {
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

    @Initiate(priority = HIGH_PRIORITY)
    public void initializeInterface(InterfaceService interfaceService) {
        InterfaceService.DEFAULT_FADING_TIME = 0.15f;

        TooltipManager tooltipManager = interfaceService.getParser().getData().getDefaultTooltipManager();
        tooltipManager.initialTime = 0.75f;
        tooltipManager.hideAll();

        LmlParser parser = interfaceService.getParser();
        parser.getData().addArgument("projectExt", "."+AppConstants.PROJECT_FILE_EXT);
        parser.getData().addArgument("imageExt", "."+Strings.join(" .", (Object[]) AppConstants.IMAGE_FILE_EXT));

        interfaceService.setShowingActionProvider(new ActionProvider() {
            @Override
            public Action provideAction(final ViewController forController, final ViewController connectedView) {
                return Actions.sequence(
                        Actions.alpha(0f),
                        Actions.fadeIn(InterfaceService.DEFAULT_FADING_TIME),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                App.inst().getInput().addProcessor(forController.getStage(), 100);
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
    public void setupInitialProject(ModelService modelService, ProjectSerializer projectSerializer) {
        // Post to the next frame to make sure all the systems/controllers are initialized.
        Gdx.app.postRunnable(() -> {
            AppParams params = App.inst().getParams();
            if (params.startupProject == null) return;

            FileHandle projectFile = FileUtils.toFileHandle(params.startupProject);
            if (!projectFile.exists()) {
                Gdx.app.error(TAG, "Project file: " + projectFile + " doesn't exists.");
                return;
            }

            ProjectModel project = projectSerializer.loadProject(projectFile);
            if (project != null) {
                project.setProjectFile(projectFile);
                modelService.setProject(project);
            }
        });
    }
}