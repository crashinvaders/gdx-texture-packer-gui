package com.crashinvaders.texturepackergui.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.PackDialogController;
import com.crashinvaders.texturepackergui.controllers.VersionCheckDialogController;
import com.crashinvaders.texturepackergui.events.PackListOrderChanged;
import com.crashinvaders.texturepackergui.events.ToastNotificationEvent;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogAdapter;
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@ViewActionContainer("global")
public class GlobalActions implements ActionContainer {

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject SkinService skinService;
    @Inject EventDispatcher eventDispatcher;
    @Inject ModelService modelService;
    @Inject ProjectSerializer projectSerializer;
    @Inject RecentProjectsRepository recentProjects;
    @Inject PackDialogController packDialogController;

    /** Common preferences */
    private Preferences prefs;
    private FileChooserHistory fileChooserHistory;

    @Initiate
    public void initialize() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
        fileChooserHistory = new FileChooserHistory(prefs);
    }

    @LmlAction("newPack") public void newPack() {
        Dialogs.showInputDialog(getStage(), getString("newPack"), null, new InputDialogAdapter() {
            @Override
            public void finished(String input) {
                PackModel pack = new PackModel();
                pack.setName(input);
                getProject().addPack(pack);
                getProject().setSelectedPack(pack);
            }
        });
    }

    @LmlAction("renamePack") public void renamePack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        Dialogs.InputDialog dialog = new Dialogs.InputDialog(getString("renamePack"), null, true, null, new InputDialogAdapter() {
            @Override
            public void finished(String input) {
                pack.setName(input);
            }
        });
        getStage().addActor(dialog.fadeIn());
        dialog.setText(pack.getName(), true);
    }

    @LmlAction("makeCopy") public void copyPack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

		Dialogs.InputDialog dialog = new Dialogs.InputDialog(getString("makeCopy"), null, true, null, new InputDialogAdapter() {
			@Override
			public void finished (String input) {
				PackModel newPack = new PackModel(pack);
				newPack.setName(input);
				getProject().addPack(newPack);
				getProject().setSelectedPack(newPack);
			}
		});
        getStage().addActor(dialog.fadeIn());
        dialog.setText(pack.getName(), true);
    }

    @LmlAction("deletePack") public void deletePack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        Dialogs.OptionDialog optionDialog = Dialogs.showOptionDialog(getStage(), getString("deletePack"), getString("dialogTextDeletePack", pack.getName()),
                Dialogs.OptionDialogType.YES_CANCEL, new OptionDialogAdapter() {
                    @Override
                    public void yes() {
                        getProject().removePack(pack);
                    }
                });
        optionDialog.closeOnEscape();
    }

    @LmlAction("movePackUp") public void movePackUp() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        packs.swap(idx, Math.max(idx-1, 0));

        eventDispatcher.postEvent(new PackListOrderChanged());
    }

    @LmlAction("movePackDown") public void movePackDown() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        packs.swap(idx, Math.min(idx+1, packs.size-1));

        eventDispatcher.postEvent(new PackListOrderChanged());
    }

    @LmlAction("selectNextPack") public void selectNextPack() {
        ProjectModel project = getProject();
        Array<PackModel> packs = project.getPacks();
        PackModel pack = project.getSelectedPack();

        if (pack == null) {
            if (packs.size > 0) {
                project.setSelectedPack(packs.first());
            }
            return;
        }

        int idx = packs.indexOf(pack, true) + 1;
        PackModel nextPack = packs.get(Math.min(idx, packs.size - 1));
        project.setSelectedPack(nextPack);
    }

    @LmlAction("selectPreviousPack") public void selectPreviousPack() {
        ProjectModel project = getProject();
        Array<PackModel> packs = project.getPacks();
        PackModel pack = project.getSelectedPack();

        if (pack == null) {
            if (packs.size > 0) {
                project.setSelectedPack(packs.peek());
            }
            return;
        }

        int idx = packs.indexOf(pack, true) - 1;
        PackModel prevPack = packs.get(Math.max(idx, 0));
        project.setSelectedPack(prevPack);
    }

    @LmlAction("packAll") public void packAll() {
        Array<PackModel> packs = getProject().getPacks();
        if (packs.size == 0) return;

        interfaceService.showDialog(packDialogController.getClass());
        packDialogController.launchPack(packs);
    }

    @LmlAction("packSelected") public void packSelected() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        interfaceService.showDialog(packDialogController.getClass());
        packDialogController.launchPack(pack);
    }

    @LmlAction("newProject") public void newProject() {
        //TODO check if there were any changes
        ProjectModel project = getProject();
        if (project.getPacks().size > 0) {
            Dialogs.showOptionDialog(getStage(), getString("dialogTitleNewProject"), getString("dialogTextNewProject"), Dialogs.OptionDialogType.YES_CANCEL, new OptionDialogAdapter() {
                @Override
                public void yes() {
                    modelService.setProject(new ProjectModel());
                }
            });
        } else {
            modelService.setProject(new ProjectModel());
        }
    }

    @LmlAction("openProject") public void openProject() {
        final ProjectModel project = getProject();
        FileHandle dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.PROJECT);
        if (FileUtils.fileExists(project.getProjectFile())) {
            dir = project.getProjectFile().parent();
        }

        final FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
		fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
			.rule(getString("projectFileDescription", AppConstants.PROJECT_FILE_EXT), AppConstants.PROJECT_FILE_EXT).get());
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                fileChooserHistory.putLastDir(FileChooserHistory.Type.PROJECT, chosenFile.parent());

                ProjectModel loadedProject = projectSerializer.loadProject(chosenFile);
                if (loadedProject != null) {
                    modelService.setProject(loadedProject);
                }
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    @LmlAction("saveProject") public void saveProject() {
        ProjectModel project = getProject();
        FileHandle projectFile = project.getProjectFile();

        // Check if project were saved before
        if (projectFile != null && projectFile.exists()) {
            projectSerializer.saveProject(project, projectFile);
        } else {
            saveProjectAs();
        }
    }

    @LmlAction("saveProjectAs") public void saveProjectAs() {
        final ProjectModel project = getProject();
        FileHandle projectFile = project.getProjectFile();
        FileHandle dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.PROJECT);
        if (FileUtils.fileExists(projectFile)) {
            dir = projectFile.parent();
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.SAVE);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
		fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
			.rule(getString("projectFileDescription", AppConstants.PROJECT_FILE_EXT), AppConstants.PROJECT_FILE_EXT).get());
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                fileChooserHistory.putLastDir(FileChooserHistory.Type.PROJECT, chosenFile.parent());

                if (chosenFile.extension().length() == 0) {
                    chosenFile = Gdx.files.getFileHandle(chosenFile.path()+"."+AppConstants.PROJECT_FILE_EXT, chosenFile.type());
                }

                getProject().setProjectFile(chosenFile);
                projectSerializer.saveProject(project, chosenFile);
            }
        });
        getStage().addActor(fileChooser.fadeIn());

        if (FileUtils.fileExists(projectFile)) { fileChooser.setSelectedFiles(projectFile); }
    }

    @LmlAction("pickInputDir") public void pickInputDir() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        FileHandle dir = FileUtils.obtainIfExists(pack.getInputDir());
        if (dir == null) {
            dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.INPUT_DIR);
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                fileChooserHistory.putLastDir(FileChooserHistory.Type.INPUT_DIR, chosenFile);
                pack.setInputDir(chosenFile.file().getAbsolutePath());
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    @LmlAction("pickOutputDir") public void pickOutputDir() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        FileHandle dir = FileUtils.obtainIfExists(pack.getOutputDir());
        if (dir == null) {
            dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.OUTPUT_DIR);
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                fileChooserHistory.putLastDir(FileChooserHistory.Type.OUTPUT_DIR, chosenFile);
                pack.setOutputDir(chosenFile.file().getAbsolutePath());
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    @LmlAction("copySettingsToAllPacks") public void copySettingsToAllPacks() {
        PackModel selectedPack = getSelectedPack();
        if (selectedPack == null) return;

        TexturePacker.Settings generalSettings = selectedPack.getSettings();
        Array<PackModel> packs = getProject().getPacks();
        for (PackModel pack : packs) {
            if (pack == selectedPack) continue;

            pack.setSettings(new TexturePacker.Settings(generalSettings));
        }

        eventDispatcher.postEvent(new ToastNotificationEvent()
                .message(getString("toastCopyAllSettings"))
                .duration(ToastNotificationEvent.DURATION_SHORT));
    }

    @LmlAction("checkForUpdates") public void checkForUpdates() {
        interfaceService.showDialog(VersionCheckDialogController.class);
    }

    @LmlAction("getCurrentVersion") public String getCurrentVersion() {
        return AppConstants.version.toString();
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

    /** Stores last used dir for specific actions */
    private static class FileChooserHistory {

        private final Preferences prefs;

        public FileChooserHistory(Preferences prefs) {
            this.prefs = prefs;
        }

        public FileHandle getLastDir(Type type) {
            String path = prefs.getString(type.prefKey, null);
            if (path == null || path.trim().length() == 0) return null;

            FileHandle fileHandle = Gdx.files.absolute(path);
            if (fileHandle.exists() && fileHandle.isDirectory()) {
                return fileHandle;
            } else {
                return null;
            }
        }

        public void putLastDir(Type type, FileHandle fileHandle) {
            String path = fileHandle.file().getAbsolutePath();
            prefs.putString(type.prefKey, path);
            prefs.flush();
        }


        public enum Type {
            PROJECT ("last_proj_dir"),
            INPUT_DIR ("last_input_dir"),
            OUTPUT_DIR ("last_output_dir");

            final String prefKey;

            Type(String prefKey) {
                this.prefKey = prefKey;
            }
        }
    }
}
