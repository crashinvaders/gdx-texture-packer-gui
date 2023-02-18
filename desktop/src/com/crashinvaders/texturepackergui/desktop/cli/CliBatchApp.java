package com.crashinvaders.texturepackergui.desktop.cli;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.StringBuilder;
import com.crashinvaders.common.autumn.DependencyInjectionService;
import com.crashinvaders.texturepackergui.controllers.TinifyService;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.ScaleFactorModel;
import com.crashinvaders.texturepackergui.controllers.packing.PackingProcessorUtils;
import com.crashinvaders.texturepackergui.controllers.projectserializer.ProjectSerializer;
import com.crashinvaders.texturepackergui.desktop.CliBatchArguments;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingManager;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.scanner.FixedClassScanner;
import com.kotcrab.vis.ui.widget.file.FileUtils;

import java.io.File;

// This application adapter is only meant to work with the libGDX headless backend.
public class CliBatchApp extends ApplicationAdapter {
    private static final String TAG = CliBatchApp.class.getSimpleName();

    private final CliBatchArguments args;

    // Autumn components.
    private ModelService modelService;
    private TinifyService tinifyService;

    private ContextDestroyer contextDestroyer;

    public CliBatchApp(CliBatchArguments args) {
        this.args = args;
    }

    @Override
    public void create() {
        super.create();

        Gdx.app.setLogLevel(args.debug ? Application.LOG_DEBUG : Application.LOG_INFO);

        Gdx.app.debug(TAG, "App is running in the batch mode.");

        contextDestroyer = initiateContext();

        // Load project.
        {
            ProjectModel projectModel = loadProjectModel(args.project);
            modelService.setProject(projectModel);
            Gdx.app.debug(TAG, "Project loaded: \"" + args.project + "\"");
            Gdx.app.debug(TAG, "Total atlases: " + projectModel.getPacks().size);
        }

        // Print all the atlases and exit.
        if (args.listAtlases) {
            ProjectModel projectModel = modelService.getProject();

            for (PackModel pack : projectModel.getPacks()) {
                System.out.println(pack.getName());
            }

            return;
        }

        // Process packs.
        {
            ProjectModel projectModel = modelService.getProject();

            if (projectModel.getPacks().size == 0) {
                Gdx.app.error(TAG, "The project has no atlases.");
                System.exit(1);
            }

            String[] packNames = args.packNames;

            Array<PackModel> packs = new Array<>();
            if (packNames.length == 0) {
                // If atlas names are not specified, pack everything.
                packs.addAll(projectModel.getPacks());
            } else {
                for (String packName : args.packNames) {
                    PackModel packModel = findAtlasWithName(projectModel, packName);
                    if (packModel == null) {
                        Gdx.app.error(TAG, "Cannot find an atlas with the name \"" + packName + "\"");
                        System.exit(1);
                    }
                    packs.add(packModel);
                }
            }

            Gdx.app.log(TAG, "Start processing atlases: " + String.join(", ", collectAtlasNames(packs)));
            packAtlases(projectModel, packs);
        }
    }

    /** Initialize minimalistic Autumn component set that is sufficient for atlas processing. */
    private ContextDestroyer initiateContext() {
        final ContextInitializer initializer = new ContextInitializer();
        initializer.clearContextAfterInitiation(false);

        initializer.addComponents(
                new DependencyInjectionService(),

                modelService = new ModelService(),
                tinifyService = new TinifyService()
        );

        initializer.scan(this.getClass(), new FixedClassScanner()); // Dummy scanner.
        return initializer.initiate();
    }

    @Override
    public void dispose() {
        super.dispose();
        contextDestroyer.dispose();
    }

    private static ProjectModel loadProjectModel(File projectFile) {
        if (projectFile == null) {
            Gdx.app.error(TAG, "Path to a project file is not specified.");
            System.exit(1);
        }
        FileHandle fileHandle = FileUtils.toFileHandle(projectFile);
        if (fileHandle.isDirectory()) {
            Gdx.app.error(TAG, "Project file: " + fileHandle + " is a directory and cannot be loaded.");
            System.exit(1);
        }
        if (!fileHandle.exists()) {
            Gdx.app.error(TAG, "Project file: " + fileHandle + " doesn't exists.");
            System.exit(1);
        }

        return ProjectSerializer.loadProject(fileHandle);
    }

    private void packAtlases(ProjectModel projectModel, Array<PackModel> packs) {
        PackProcessingManager packProcessingManager = new PackProcessingManager(
                PackingProcessorUtils.prepareRegularProcessorSequence(tinifyService),
                new PackProcessingManager.ListenerAdapter(),
                args.threads);

        Array<PackProcessingNode> nodes = PackingProcessorUtils.prepareProcessingNodes(projectModel, packs);
        for (int i = 0; i < nodes.size; i++) {
            PackProcessingNode node = nodes.get(i);
            packProcessingManager.postProcessingNode(node);
        }

        packProcessingManager.execute();
    }

    @Null
    private static PackModel findAtlasWithName(ProjectModel projectModel, String name) {
        Array<PackModel> packs = projectModel.getPacks();
        for (int i = 0; i < packs.size; i++) {
            PackModel packModel = packs.get(i);
            if (packModel.getName().equals(name))
                return packModel;
        }
        return null;
    }

    private static String[] collectAtlasNames(Array<PackModel> packModels) {
        Array<String> atlasNames = new Array<>();
        for (PackModel packModel : packModels) {
            atlasNames.add(packModel.getName());
        }
        return atlasNames.toArray(String.class);
    }
}
