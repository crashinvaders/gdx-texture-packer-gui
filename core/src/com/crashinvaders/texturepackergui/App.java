package com.crashinvaders.texturepackergui;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;
import com.crashinvaders.common.PrioritizedInputMultiplexer;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.shortcuts.GlobalShortcutHandler;
import com.github.czyzby.autumn.annotation.Initiate;
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
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;

public class App implements ApplicationListener {
    private static final String TAG = App.class.getSimpleName();

    private static App instance;
    private final ClassScanner componentScanner;
    private final AppParams params;
    private final PrioritizedInputMultiplexer inputMultiplexer;
    private final DragDropManager dragDropManager = new DragDropManager();

    private ContextDestroyer contextDestroyer;

    private Context context;
    private InterfaceService interfaceService;
    private ModelService modelService;
    private LocaleService localeService;
    private GlobalShortcutHandler shortcutHandler;
    private ComponentExtractor componentExtractor;
    private EventDispatcher eventDispatcher;
    private MessageDispatcher messageDispatcher;

    /** Manually set inside {@link #pause()} and {@link #resume()} */
    private boolean paused;

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

        inputMultiplexer = new PrioritizedInputMultiplexer();
        inputMultiplexer.setMaxPointers(1);

        instance = this;
    }

    @Override
    public void create() {
        if (params.debug) {
            Gdx.app.log(TAG, "Application is running in DEBUG mode.");
        }

        Gdx.app.getGraphics().setContinuousRendering(false);

        Gdx.input.setInputProcessor(inputMultiplexer);

        initiateContext();

        FileChooser.setSaveLastDirectory(true);
        FileChooser.setDefaultPrefsName("file_chooser.xml");

        // Uncomment to update project's LML DTD schema
        // LmlUtils.saveDtdSchema(interfaceService.getParser(), Gdx.files.local("../lml.dtd"));
    }

    private void initiateContext() {
        final ContextInitializer initializer = new ContextInitializer();
        initializer.clearContextAfterInitiation(false);
        registerDefaultComponentAnnotations(initializer);
        addDefaultComponents(initializer);
        initializer.scan(this.getClass(), componentScanner);
        contextDestroyer = initializer.initiate();

        // Load LML template with common values and default attribute setup.
        interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/common.lml"));
        interfaceService.getParser().getData().addArgument("currentVersionCode", AppConstants.version.toString());
    }

    /** Invoked before context initiation.
     * @param initializer should be used to register component annotations to scan for. */
    @SuppressWarnings("unchecked")
    protected void registerDefaultComponentAnnotations(final ContextInitializer initializer) {
        initializer.scanFor(ViewActionContainer.class, ViewDialog.class, View.class, StageViewport.class, Property.class);
    }

    /** Invoked before context initiation.
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

    @Override
    public void resize(final int width, final int height) {
        interfaceService.resize(width, height);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void render() {
        if (paused) return;

        GdxUtilities.clearScreen();
        interfaceService.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resume() {
        paused = false;

        interfaceService.resume();
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void pause() {
        paused = true;

        interfaceService.pause();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        Disposables.disposeOf(contextDestroyer);
        VisUI.dispose(false);
    }

    public void restart() {
        inputMultiplexer.clear();
        dispose();
        create();
    }

    //region Accessors
    public Context getContext() { return context; }
    public DragDropManager getDragDropManager() { return dragDropManager; }
    public InterfaceService getInterfaceService() { return interfaceService; }
    public ModelService getModelService() { return modelService; }
    public EventDispatcher getEventDispatcher() { return eventDispatcher; }
    public MessageDispatcher getMessageDispatcher() { return messageDispatcher; }
    public AppParams getParams() { return params; }
    public PrioritizedInputMultiplexer getInput() { return inputMultiplexer; }
    public GlobalShortcutHandler getShortcuts() { return shortcutHandler; }
    public I18NBundle getI18n() { return localeService.getI18nBundle(); }
    //endregion

    /** This is utility component class that helps to get access to some system components for the {@link App} instance */
    @SuppressWarnings("WeakerAccess")
    private class ComponentExtractor {
        @Initiate() void extractComponents(Context context, EventDispatcher eventDispatcher, MessageDispatcher messageDispatcher) {
            App.this.context = context;
            App.this.eventDispatcher = eventDispatcher;
            App.this.messageDispatcher = messageDispatcher;
        }
    }
}
