package com.crashinvaders.texturepackergui;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.crashinvaders.common.PrioritizedInputMultiplexer;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.shortcuts.GlobalShortcutHandler;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.context.Context;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.i18n.processor.AvailableLocalesAnnotationProcessor;
import com.github.czyzby.autumn.mvc.component.i18n.processor.I18nBundleAnnotationProcessor;
import com.github.czyzby.autumn.mvc.component.preferences.PreferencesService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.component.ui.processor.*;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.preference.Property;
import com.github.czyzby.autumn.mvc.stereotype.preference.StageViewport;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.autumn.processor.event.MessageDispatcher;
import com.github.czyzby.autumn.scanner.ClassScanner;
import com.github.czyzby.kiwi.util.gdx.GdxUtilities;
import com.github.czyzby.kiwi.util.gdx.asset.Disposables;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;
import com.github.czyzby.kiwi.util.tuple.immutable.Pair;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.Dtd;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;

import java.io.Writer;

public class App implements ApplicationListener {
    private static App instance;
    private final ClassScanner componentScanner;
    private final AppParams params;
    private final PrioritizedInputMultiplexer inputMultiplexer;

    private Array<Pair<Class<?>, ClassScanner>> componentScanners;
    private ContextDestroyer contextDestroyer;

    private InterfaceService interfaceService;
    private ModelService modelService;
    private LocaleService localeService;
    private GlobalShortcutHandler shortcutHandler;
    private ComponentExtractor componentExtractor;
    private EventDispatcher eventDispatcher;
    private MessageDispatcher messageDispatcher;

    /** Singleton accessor */
    public static App inst() {
        if (instance == null) {
            throw new NullPointerException("App is not initialized yet");
        }
        return instance;
    }
    public App(ClassScanner componentScanner, AppParams params) {
        this.componentScanner = componentScanner;
        this.params = params;
        componentScanners = GdxArrays.newArray();
        registerComponents(componentScanner, App.class);

        inputMultiplexer = new PrioritizedInputMultiplexer();
        inputMultiplexer.setMaxPointers(1);

        instance = this;
    }

    /**
     * Can be called only before {@link #create()} is invoked.
     * @param componentScanner used to scan for annotated classes.
     * @param scanningRoot root of the scanning.
     */
    protected void registerComponents(final ClassScanner componentScanner, final Class<?> scanningRoot) {
        componentScanners.add(new Pair<Class<?>, ClassScanner>(scanningRoot, componentScanner));
    }

    @Override
    public void create() {
        Gdx.input.setInputProcessor(inputMultiplexer);

        initiateContext();
        clearComponentScanners();

        FileChooser.setDefaultPrefsName(".gdxtexturepackergui/file_chooser.xml");

//        // Uncomment to update project's LML DTD schema
//        saveDtdSchema(Gdx.files.local("../lml.dtd"));
    }

    private void initiateContext() {
        final ContextInitializer initializer = new ContextInitializer();
        registerDefaultComponentAnnotations(initializer);
        addDefaultComponents(initializer);
        for (final Pair<Class<?>, ClassScanner> componentScanner : componentScanners) {
            initializer.scan(componentScanner.getFirst(), componentScanner.getSecond());
        }
        contextDestroyer = initializer.initiate();

        interfaceService.getParser().getData().addArgument("currentVersionCode", AppConstants.version.toString());
    }

    /** Invoked before context initiation.
     *
     * @param initializer should be used to register component annotations to scan for. */
    @SuppressWarnings("unchecked")
    protected void registerDefaultComponentAnnotations(final ContextInitializer initializer) {
        initializer.scanFor(ViewActionContainer.class, ViewDialog.class, View.class, StageViewport.class, Property.class);
    }

    /** Invoked before context initiation.
     *
     * @param initializer should be used to registered default components, created with plain old Java. */
    protected void addDefaultComponents(final ContextInitializer initializer) {
        initializer.addComponents(

                // PROCESSORS
                // Assets:
                new AssetService(), new SkinAssetAnnotationProcessor(),
                // Locale:
                localeService = new LocaleService(),
                // Settings:
                new I18nBundleAnnotationProcessor(), new PreferenceAnnotationProcessor(), new SkinAnnotationProcessor(),
                new StageViewportAnnotationProcessor(), new PreferencesService(),
                // Interface:
                new ViewAnnotationProcessor(), new ViewDialogAnnotationProcessor(),
                new ViewActionContainerAnnotationProcessor(), new ViewStageAnnotationProcessor(),
                new LmlMacroAnnotationProcessor(), new LmlParserSyntaxAnnotationProcessor(),
                new AvailableLocalesAnnotationProcessor(),

                // COMPONENTS
                // Interface:
                interfaceService = new InterfaceService(),
                new SkinService(),
                // Custom
                modelService = new ModelService(),
                shortcutHandler = new GlobalShortcutHandler(),
                componentExtractor = new ComponentExtractor());
    }

    private void clearComponentScanners() {
        componentScanners.clear();
        componentScanners = null;
    }

    @Override
    public void resize(final int width, final int height) {
        interfaceService.resize(width, height);
    }

    @Override
    public void render() {
        GdxUtilities.clearScreen();
        interfaceService.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resume() {
        interfaceService.resume();
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void pause() {
        interfaceService.pause();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        Disposables.disposeOf(contextDestroyer);
        VisUI.dispose();
    }

    /**
     * Uses current {@link LmlParser} to generate a DTD schema file with all supported tags, macros and attributes.
     * Should be used only during development: DTD allows to validate LML templates during creation (and add content
     * assist thanks to XML support in your IDE), but is not used in any way by the {@link LmlParser} in runtime.
     *
     * @param file path to the file where DTD schema should be saved. Advised to be local or absolute. Note that some
     *            platforms (GWT) do not support file saving - this method should be used on desktop platform and only
     *            during development.
     * @throws GdxRuntimeException when unable to save DTD schema.
     * @see Dtd
     */
    public void saveDtdSchema(final FileHandle file) {
        try {
            final LmlParser lmlParser = interfaceService.getParser();
            final Writer appendable = file.writer(false, "UTF-8");
            final boolean strict = lmlParser.isStrict();
            lmlParser.setStrict(false); // Temporary setting to non-strict to generate as much tags as possible.
            Dtd.saveSchema(lmlParser, appendable);
            appendable.close();
            lmlParser.setStrict(strict);
        } catch (final Exception exception) {
            throw new GdxRuntimeException("Unable to save DTD schema.", exception);
        }
    }

    //region Accessors
    public InterfaceService getInterfaceService() { return interfaceService; }
    public ModelService getModelService() { return modelService; }
    public EventDispatcher getEventDispatcher() { return eventDispatcher; }
    public MessageDispatcher getMessageDispatcher() { return messageDispatcher; }
    public AppParams getParams() { return params; }
    public PrioritizedInputMultiplexer getInput() { return inputMultiplexer; }
    public GlobalShortcutHandler getShortcuts() { return shortcutHandler; }
    public I18NBundle getI18n() { return localeService.getI18nBundle(); }
    //endregion

    /** This is utility component class that helps to get access to some system components for App class */
    @SuppressWarnings("WeakerAccess")
    private class ComponentExtractor {
        @Initiate() void extractComponents(EventDispatcher eventDispatcher, MessageDispatcher messageDispatcher) {
            App.this.eventDispatcher = eventDispatcher;
            App.this.messageDispatcher = messageDispatcher;
        }
    }
}
