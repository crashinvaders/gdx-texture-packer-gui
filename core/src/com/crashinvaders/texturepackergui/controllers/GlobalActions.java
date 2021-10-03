package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.extensionmodules.CjkFontExtensionModule;
import com.crashinvaders.texturepackergui.controllers.main.MainController;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ModelUtils;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.ninepatcheditor.NinePatchToolController;
import com.crashinvaders.texturepackergui.controllers.packing.PackDialogController;
import com.crashinvaders.texturepackergui.controllers.projectserializer.ProjectSerializer;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.utils.AppIconProvider;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.SystemUtils;
import com.crashinvaders.texturepackergui.utils.WidgetUtils;
import com.crashinvaders.texturepackergui.views.dialogs.OptionDialog;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogAdapter;
import com.kotcrab.vis.ui.util.dialog.OptionDialogAdapter;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;

//TODO move model logic code to ModelUtils
@ViewActionContainer("global")
public class GlobalActions implements ActionContainer {
    private static final String TAG = GlobalActions.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject SkinService skinService;
    @Inject EventDispatcher eventDispatcher;
    @Inject ModelService modelService;
    @Inject ModelUtils modelUtils;
    @Inject ProjectSerializer projectSerializer;
    @Inject RecentProjectsRepository recentProjects;
    @Inject MainController mainController;
    @Inject PackDialogController packDialogController;
    @Inject NinePatchToolController ninePatchToolController;
    @Inject public CommonDialogs commonDialogs;

    /** Common preferences */
    private Preferences prefs;
    private FileChooserHistory fileChooserHistory;

    @Initiate
    public void initialize() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
        fileChooserHistory = new FileChooserHistory(prefs);
    }

    @LmlAction("resetViewFocus") public void resetViewFocus() {
        FocusManager.resetFocus(getStage());
    }

	@LmlAction("newPack") public void newPack() {
        commonDialogs.newPack();
	}

    @LmlAction("renamePack") public void renamePack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        VisDialog dialog = WidgetUtils.createInputDialog(getString("renamePack"), null, pack.getName(), true, new InputDialogAdapter() {
            @Override
            public void finished(String input) {
                pack.setName(input);
            }
        });

        getStage().addActor(dialog.fadeIn());
    }

    @LmlAction({"makeCopy", "copyPack"}) public void copyPack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        commonDialogs.copyPack(pack);
    }

    @LmlAction("deletePack") public void deletePack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        OptionDialog optionDialog = OptionDialog.show(getStage(), getString("deletePack"), getString("dialogTextDeletePack", pack.getName()),
                Dialogs.OptionDialogType.YES_CANCEL, new OptionDialogAdapter() {
                    @Override
                    public void yes() {
                        modelUtils.selectClosestPack(pack);
                        getProject().removePack(pack);
                    }
                });
        optionDialog.closeOnEscape();
    }

    @LmlAction("movePackUp") public void movePackUp() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        modelUtils.movePackUp(pack);
    }

    @LmlAction("movePackDown") public void movePackDown() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        modelUtils.movePackDown(pack);
    }

    @LmlAction("selectNextPack") public void selectNextPack() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        modelUtils.selectNextPack(pack);
    }

    @LmlAction("selectPreviousPack") public void selectPreviousPack() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        modelUtils.selectPrevPack(pack);
    }

    @LmlAction("packAll") public void packAll() {
        ProjectModel project = getProject();
        Array<PackModel> packs = getProject().getPacks();
        if (packs.size == 0) return;

        interfaceService.showDialog(packDialogController.getClass());
        packDialogController.launchPack(project, packs);

    }

    @LmlAction("packSelected") public void packSelected() {
        ProjectModel project = getProject();
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        interfaceService.showDialog(packDialogController.getClass());
        packDialogController.launchPack(project, pack);
    }

    @LmlAction("newProject") public void newProject() {
        commonDialogs.checkUnsavedChanges(new Runnable() {
            @Override
            public void run() {
                modelService.setProject(new ProjectModel());
            }
        });
    }

    @LmlAction("openProject") public void openProject() {
        final ProjectModel project = getProject();
        FileHandle dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.PROJECT);
        if (FileUtils.fileExists(project.getProjectFile())) {
            dir = project.getProjectFile().parent();
        }

        final FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
		fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
			.rule(getString("projectFileDescription", AppConstants.PROJECT_FILE_EXT), AppConstants.PROJECT_FILE_EXT).get());
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                final FileHandle chosenFile = file.first();
                commonDialogs.checkUnsavedChanges(new Runnable() {
                    @Override
                    public void run() {
                        loadProject(chosenFile);
                    }
                });
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    public void loadProject(FileHandle projectFile) {
        if (projectFile == null) { throw new IllegalArgumentException("Project file cannot be null"); }

        fileChooserHistory.putLastDir(FileChooserHistory.Type.PROJECT, projectFile.parent());

        ProjectModel loadedProject = projectSerializer.loadProject(projectFile);
        if (loadedProject != null) {
            modelService.setProject(loadedProject);
        }
    }

    @LmlAction("saveProject") public void saveProject() {
        resetViewFocus();

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
        resetViewFocus();

        final ProjectModel project = getProject();
        FileHandle projectFile = project.getProjectFile();
        FileHandle dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.PROJECT);
        if (FileUtils.fileExists(projectFile)) {
            dir = projectFile.parent();
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.SAVE);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
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

    @LmlAction("pickOutputDir") public void pickOutputDir() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        FileHandle dir = FileUtils.obtainIfExists(pack.getOutputDir());
        if (dir == null) {
            dir = fileChooserHistory.getLastDir(FileChooserHistory.Type.OUTPUT_DIR);
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
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

    //TODO move model logic code to ModelUtils
    @LmlAction("copySettingsToAllPacks") public void copySettingsToAllPacks() {
        PackModel selectedPack = getSelectedPack();
        if (selectedPack == null) return;

        TexturePacker.Settings generalSettings = selectedPack.getSettings();
        Array<PackModel> packs = getProject().getPacks();
        for (PackModel pack : packs) {
            if (pack == selectedPack) continue;

            pack.setSettings(generalSettings);
        }

        eventDispatcher.postEvent(new ShowToastEvent()
                .message(getString("toastCopyAllSettings"))
                .duration(ShowToastEvent.DURATION_SHORT));
    }

    @LmlAction("checkForUpdates") public void checkForUpdates() {
        interfaceService.showDialog(VersionCheckDialogController.class);
    }

    @LmlAction("getCurrentVersion") public String getCurrentVersion() {
        return AppConstants.version.toString();
    }

    @LmlAction("launchTextureUnpacker") public void launchTextureUnpacker() {
        interfaceService.showDialog(TextureUnpackerDialogController.class);
    }

    @LmlAction("launchNinePatchTool") public void launchNinePatchTool() {
        ninePatchToolController.initiateFromFilePicker();
    }

    @LmlAction("changePreviewBackground") public void changePreviewBackground() {
        interfaceService.showDialog(PreviewBackgroundDialogController.class);
    }

    @LmlAction("showExtensionModulesDialog") public void showExtensionModulesDialog() {
        interfaceService.showDialog(ExtensionModulesDialogController.class);
    }

    @LmlAction("showUiScalingDialog") public void showUiScalingDialog() {
        interfaceService.showDialog(InterfaceScalingDialogController.class);
    }

    @LmlAction("restartApplication") public void restartApplication() {
        Gdx.app.log(TAG, "Restarting the application...");
        FileHandle projectFile = modelService.getProject().getProjectFile();
        if (projectFile != null && projectFile.exists()) {
            App.inst().getParams().startupProject = projectFile.file();
        }
        Gdx.app.postRunnable(() -> App.inst().restart());
    }

    @LmlAction("getSystemNameText") String getSystemNameText() {
        return SystemUtils.getPrintString();
    }

    @LmlAction("editCustomHotkeys") void editCustomHotkeys() {
        FileHandle userHotkeyFile = Gdx.files.external(AppConstants.EXTERNAL_DIR + "/hotkeys_user.txt");
        if (!userHotkeyFile.exists()) {
            Gdx.files.internal("hotkeys_user.txt").copyTo(userHotkeyFile);
        }
        try {
            Desktop.getDesktop().open(userHotkeyFile.file());
        } catch (IOException e) {
            Gdx.app.error(TAG, "Error opening " + userHotkeyFile, e);
        }
    }
    @LmlAction public void showMenuFile() {
        mainController.showMenuFile();
    }
    @LmlAction public void showMenuPack() {
        mainController.showMenuPack();
    }
    @LmlAction public void showMenuTools() {
        mainController.showMenuTools();
    }
    @LmlAction public void showMenuHelp() {
        mainController.showMenuHelp();
    }

    @LmlAction("changeLanguageEn") public void changeLanguageEn() {
        changeLanguage(AppConstants.LOCALE_EN);
    }
    @LmlAction("changeLanguageDe") public void changeLanguageDe() {
        changeLanguage(AppConstants.LOCALE_DE);
    }
    @LmlAction("changeLanguageRu") public void changeLanguageRu() {
        changeLanguage(AppConstants.LOCALE_RU);
    }
    @LmlAction("changeLanguageZhTw") public void changeLanguageZhTw() {
        if (commonDialogs.checkExtensionModuleActivated(CjkFontExtensionModule.class)) {
            changeLanguage(AppConstants.LOCALE_ZH_TW);
        }
    }

    public void changeLanguage(Locale locale) {
        if (localeService.getCurrentLocale().equals(locale)) return;

        Locales.setLocale(locale);
        localeService.setCurrentLocale(locale);
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