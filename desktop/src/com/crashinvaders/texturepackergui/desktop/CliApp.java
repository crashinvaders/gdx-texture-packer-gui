package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.projectserializer.ProjectSerializer;
import com.kotcrab.vis.ui.widget.file.FileUtils;

import java.io.File;

// This application adapter is only meant to work with the libGDX headless backend.
public class CliApp extends ApplicationAdapter {

    private static final String TAG = CliApp.class.getSimpleName();

    private final Arguments args;

    public CliApp(Arguments args) {
        this.args = args;
    }

    @Override
    public void create() {
        super.create();

        ProjectModel projectModel = loadProjectModel(args.project);

        System.out.println("Project loaded \"" + args.project + "\"");
        System.out.println("Total packs: " + projectModel.getPacks().size);
        for (PackModel pack : projectModel.getPacks()) {
            System.out.println("Pack: \"" + pack.getName() + "\"");
        }
    }

    private static ProjectModel loadProjectModel(File projectFile) {
        if (projectFile == null) {
            System.err.println("Path to a project file is not specified.");
            System.exit(1);
        }
        FileHandle fileHandle = FileUtils.toFileHandle(projectFile);
        if (fileHandle.isDirectory()) {
            System.err.println("Project file: " + fileHandle + " is a directory and cannot be loaded.");
            System.exit(1);
        }
        if (!fileHandle.exists()) {
            System.err.println("Project file: " + fileHandle + " doesn't exists.");
            System.exit(1);
        }

        return ProjectSerializer.loadProject(fileHandle);
    }
}
