package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.texturepackergui.config.attributes.OnRightClickLmlAttribute;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.RecentProjectsUpdatedEvent;
import com.crashinvaders.texturepackergui.services.RecentProjectsRepository;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.ProjectSerializer;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.Scene2dUtils;
import com.crashinvaders.texturepackergui.views.canvas.Canvas;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewResizer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.annotation.LmlInject;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.ToastManager;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import java.math.BigDecimal;

@SuppressWarnings("WeakerAccess")
@View(id = "main", value = "lml/main.lml", first = true)
public class MainController implements ActionContainer, ViewResizer {

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;
    @Inject LocaleService localeService;
    @Inject ProjectSerializer projectSerializer;
    @Inject SkinService skinService;
    @Inject RecentProjectsRepository recentProjects;

    @LmlActor("splitPane") VisSplitPane splitPane;
    @LmlActor("canvasContainer") Container canvasContainer;
    @LmlActor("canvas") Canvas canvas;
    @LmlInject() ProjectConfigController viewsPacks;
    @LmlInject() PackSettingsController viewsSettings;
    @LmlInject() FileMenuController fileMenu;
    @LmlInject() PackMenuController packMenu;
    @LmlInject() HelpMenuController helpMenu;

    private ToastManager toastManager;

    /** Indicates that view is ready */
    private boolean initialized;

    @SuppressWarnings("unchecked")
    @LmlAfter
    void initialize() {
        initialized = true;

        viewsSettings.cboEncodingFormat.setItems(WidgetData.textureFormats);
        viewsSettings.cboOutputFormat.setItems(WidgetData.outputFormats);
        viewsSettings.cboMinFilter.setItems(WidgetData.textureFilters);
        viewsSettings.cboMagFilter.setItems(WidgetData.textureFilters);
        viewsSettings.cboWrapX.setItems(WidgetData.textureWraps);
        viewsSettings.cboWrapY.setItems(WidgetData.textureWraps);

        toastManager = new ToastManager(getStage());
        toastManager.setAlignment(Align.bottomRight);

        canvas.setCallback(new Canvas.Callback() {
            @Override
            public void atlasError(PackModel pack) {
                //TODO supply with details
                toastManager.show("Error loading atlas for pack \"" + pack.getName() + "\"", 2f);
            }
        });

        updatePackList();
        updateViewsFromPack(getSelectedPack());
        updateCanvas();
        updateRecentProjects();
    }

    @Override
    public void resize(Stage stage, int width, int height) {
        final Viewport viewport = stage.getViewport();
        viewport.update(width, height, true);

        toastManager.resize();
    }

    //region Events
    @OnEvent(ProjectInitializedEvent.class)
    public boolean onEvent(ProjectInitializedEvent event) {
        if (initialized) {
            updatePackList();
            updateViewsFromPack(event.getProjectModel().getSelectedPack());
        }
        return OnEvent.KEEP;
    }

    @OnEvent(ProjectPropertyChangedEvent.class)
    public boolean onEvent(ProjectPropertyChangedEvent event) {
        if (initialized) {
            switch (event.getProperty()) {
                case SELECTED_PACK:
                    updateViewsFromPack(event.getProject().getSelectedPack());
                    updateCanvas();
                    break;
                case PACKS:
                    updatePackList();
                    break;
            }
        }
        return OnEvent.KEEP;
    }

    @OnEvent(PackPropertyChangedEvent.class)
    public boolean onEvent(PackPropertyChangedEvent event) {
        if (initialized) {
            switch (event.getProperty()) {
                case NAME:
                    if (event.getPack() == getSelectedPack()) {
                        updateViewsFromPack(event.getPack());
                    }
                    break;
            }
        }
        return OnEvent.KEEP;
    }

    @OnEvent(RecentProjectsUpdatedEvent.class)
    public boolean onEvent(RecentProjectsUpdatedEvent event) {
        if (initialized) {
            updateRecentProjects();
        }
        return OnEvent.KEEP;
    }

    //endregion

    //region Actions
    @LmlAction("reloadScreen")
    void reloadScreen() {
        interfaceService.reload();
    }

    @LmlAction("newPack") void newPack() {
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

    @LmlAction("renamePack") void renamePack() {
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

    @LmlAction("copyPack") void copyPack() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        Dialogs.InputDialog dialog = new Dialogs.InputDialog(getString("copyPack"), null, true, null, new InputDialogAdapter() {
            @Override
            public void finished(String input) {
                PackModel newPack = new PackModel(pack);
                newPack.setName(input);
                getProject().addPack(newPack);
                getProject().setSelectedPack(newPack);
            }
        });
        getStage().addActor(dialog.fadeIn());
        dialog.setText(pack.getName(), true);
    }

    @LmlAction("deletePack") void deletePack() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        getProject().removePack(pack);
    }

    @LmlAction("movePackUp") void movePackUp() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        packs.swap(idx, Math.max(idx-1, 0));
        viewsPacks.listPacks.getItems().swap(idx, Math.max(idx-1, 0));
    }

    @LmlAction("movePackDown") void movePackDown() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        Array<PackModel> packs = getProject().getPacks();
        int idx = packs.indexOf(pack, true);
        packs.swap(idx, Math.min(idx+1, packs.size-1));
        viewsPacks.listPacks.getItems().swap(idx, Math.min(idx+1, packs.size-1));
    }

    @LmlAction("packAll") void packAll() {
        Array<PackModel> packs = getProject().getPacks();
        if (packs.size == 0) return;

        PackDialog dialog = new PackDialog();
        dialog.setCompletionListener(new PackDialog.CompletionListener() {
            @Override
            public void onComplete() {
                updateCanvas();
            }
        });
        getStage().addActor(dialog.fadeIn());
        dialog.launchPack(packs);

        updateCanvas();
    }

    @LmlAction("packSelected") void packSelected() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        PackDialog dialog = new PackDialog();
        dialog.setCompletionListener(new PackDialog.CompletionListener() {
            @Override
            public void onComplete() {
                updateCanvas();
            }
        });
        getStage().addActor(dialog.fadeIn());
        dialog.launchPack(pack);

        updateCanvas();
    }

    @LmlAction("newProject") void newProject() {
        //TODO check if there were any changes

        modelService.setProject(new ProjectModel());
    }

    @LmlAction("openProject") void openProject() {
        final ProjectModel project = getProject();
        FileHandle dir = null;
        if (FileUtils.fileExists(project.getProjectFile())) {
            dir = project.getProjectFile().parent();
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true).rule(getString("projectFileDescription"), "tpproj").get());
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                ProjectModel loadedProject = projectSerializer.loadProject(chosenFile);
                modelService.setProject(loadedProject);
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    @LmlAction("saveProject") void saveProject() {
        ProjectModel project = getProject();
        FileHandle projectFile = project.getProjectFile();

        // Check if project were saved before
        if (projectFile != null && projectFile.exists()) {
            projectSerializer.saveProject(project, projectFile);
        } else {
            saveProjectAs();
        }
    }

    @LmlAction("saveProjectAs") void saveProjectAs() {
        final ProjectModel project = getProject();
        FileHandle projectFile = project.getProjectFile();
        FileHandle dir = null;
        if (FileUtils.fileExists(projectFile)) {
            dir = projectFile.parent();
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.SAVE);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true).rule(getString("projectFileDescription"), "tpproj").get());
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                getProject().setProjectFile(chosenFile);

                projectSerializer.saveProject(project, chosenFile);
            }
        });
        getStage().addActor(fileChooser.fadeIn());

        if (FileUtils.fileExists(projectFile)) { fileChooser.setSelectedFiles(projectFile); }
    }

    @LmlAction("pickInputDir") void pickInputDir() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        FileHandle dir = FileUtils.obtainIfExists(pack.getInputDir());

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                viewsPacks.edtInputDir.setText(chosenFile.path());
                pack.setInputDir(chosenFile.file().getAbsolutePath());
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    @LmlAction("pickOutputDir") void pickOutputDir() {
        final PackModel pack = getSelectedPack();
        if (pack == null) return;

        FileHandle dir = FileUtils.obtainIfExists(pack.getOutputDir());

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                viewsPacks.edtOutputDir.setText(chosenFile.path());
                pack.setOutputDir(chosenFile.file().getAbsolutePath());
            }
        });
        getStage().addActor(fileChooser.fadeIn());
    }

    @LmlAction("copySettingsToAllPacks") void copySettingsToAllPacks() {
        PackModel selectedPack = getSelectedPack();
        if (selectedPack == null) return;

        TexturePacker.Settings generalSettings = selectedPack.getSettings();
        Array<PackModel> packs = getProject().getPacks();
        for (PackModel pack : packs) {
            if (pack == selectedPack) continue;

            pack.setSettings(new TexturePacker.Settings(generalSettings));
        }

        toastManager.show(getString("copyAllSettingsToast"), 2f);
    }

    @LmlAction("onPackListSelectionChanged") void onPackListSelectionChanged(final VisList list) {
        final PackModel selectedPack = (PackModel) list.getSelected();
        if (getSelectedPack() == selectedPack) return;

        final ProjectModel project = getProject();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                project.setSelectedPack(selectedPack);
            }
        });

        //TODO check if selection within scrollbar and scroll to it if not
    }

    @LmlAction("onPackListRightClick") void onPackListRightClick(final OnRightClickLmlAttribute.Params params) {
        VisList list = (VisList)params.actor;

        // Simulate left click to trigger selection logic
        Scene2dUtils.simulateClickGlobal(list, 0, 0, params.stageX, params.stageY);

        final PackModel pack = (PackModel) list.getSelected();

        PopupMenu popupMenu = parseLml(Gdx.files.internal("lml/packListMenu.lml"));

        MenuItem menuItem;
        menuItem = popupMenu.findActor("miRename");
        menuItem.setDisabled(pack == null);
        menuItem = popupMenu.findActor("miDelete");
        menuItem.setDisabled(pack == null);
        menuItem = popupMenu.findActor("miCopy");
        menuItem.setDisabled(pack == null);
        menuItem = popupMenu.findActor("miMoveUp");
        menuItem.setDisabled(pack == null);
        menuItem = popupMenu.findActor("miMoveDown");
        menuItem.setDisabled(pack == null);
        menuItem = popupMenu.findActor("miPackSelected");
        menuItem.setDisabled(pack == null);
        menuItem = popupMenu.findActor("miPackAll");
        menuItem.setDisabled(getProject().getPacks().size == 0);
        menuItem = popupMenu.findActor("miCopySettingsToAllPacks");
        menuItem.setDisabled(pack == null);

        popupMenu.showMenu(getStage(), params.stageX, params.stageY);
    }

    @LmlAction("onInputDirTextChanged") void onInputDirTextChanged(final VisTextField textField) {
        if (getSelectedPack() == null) return;

        final String text = textField.getText();
        final PackModel pack = getSelectedPack();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                pack.setInputDir(text);
            }
        });
    }

    @LmlAction("onOutputDirTextChanged") void onOutputDirTextChanged(final VisTextField textField) {
        if (getSelectedPack() == null) return;

        final String text = textField.getText();
        final PackModel pack = getSelectedPack();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                pack.setOutputDir(text);
            }
        });
    }

    @LmlAction("onPackFilenameTextChanged") void onPackFilenameTextChanged(VisTextField textField) {
        if (getSelectedPack() == null) return;

        final String text = textField.getText();
        final PackModel pack = getSelectedPack();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                pack.setFilename(text);
            }
        });
    }

    @LmlAction("onSettingsChbChecked") void onSettingsChbChecked(VisCheckBox checkBox) {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        TexturePacker.Settings settings = pack.getSettings();
        switch (checkBox.getName()) {
            case "chkUseFastAlgorithm": settings.fast = checkBox.isChecked(); break;
            case "chkEdgePadding": settings.edgePadding = checkBox.isChecked(); break;
            case "chkStripWhitespaceX": settings.stripWhitespaceX = checkBox.isChecked(); break;
            case "chkStripWhitespaceY": settings.stripWhitespaceY = checkBox.isChecked(); break;
            case "chkAllowRotation": settings.rotation = checkBox.isChecked(); break;
            case "chkIncludeSubdirs": settings.combineSubdirectories = checkBox.isChecked(); break;
            case "chkBleeding": settings.bleed = checkBox.isChecked(); break;
            case "chkDuplicatePadding": settings.duplicatePadding = checkBox.isChecked(); break;
            case "chkForcePot": settings.pot = checkBox.isChecked(); break;
            case "chkUseAliases": settings.alias = checkBox.isChecked(); break;
            case "chkIgnoreBlankImages": settings.ignoreBlankImages = checkBox.isChecked(); break;
            case "chkDebug": settings.debug = checkBox.isChecked(); break;
            case "chkUseIndices": settings.useIndexes = checkBox.isChecked(); break;
            case "chkPremultiplyAlpha": settings.premultiplyAlpha = checkBox.isChecked(); break;
        }
    }

    @LmlAction("onSettingsIntSpinnerChanged") void onSettingsIntSpinnerChanged(Spinner spinner) {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        TexturePacker.Settings settings = pack.getSettings();
        IntSpinnerModel model = (IntSpinnerModel) spinner.getModel();
        switch (spinner.getName()) {
            case "spnMinPageWidth": settings.minWidth = model.getValue(); break;
            case "spnMinPageHeight": settings.minHeight = model.getValue(); break;
            case "spnMaxPageWidth": settings.maxWidth = model.getValue(); break;
            case "spnMaxPageHeight": settings.maxHeight = model.getValue(); break;
            case "spnAlphaThreshold": settings.alphaThreshold = model.getValue(); break;
            case "spnPaddingX": settings.paddingX = model.getValue(); break;
            case "spnPaddingY": settings.paddingY = model.getValue(); break;
        }
    }

    @LmlAction("onSettingsFloatSpinnerChanged") void onSettingsFloatSpinnerChanged(Spinner spinner) {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        TexturePacker.Settings settings = pack.getSettings();
        FloatSpinnerModel model = (FloatSpinnerModel) spinner.getModel();
        switch (spinner.getName()) {
            case "spnJpegQuality": settings.jpegQuality = model.getValue().floatValue(); break;
        }
    }

    @LmlAction("onSettingsCboChanged") void onSettingsCboChanged(VisSelectBox selectBox) {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        TexturePacker.Settings settings = pack.getSettings();
        Object value = selectBox.getSelected();
        switch (selectBox.getName()) {
            case "cboEncodingFormat": settings.format = (Pixmap.Format) value; break;
            case "cboOutputFormat": settings.outputFormat = (String) value; break;
            case "cboMinFilter": settings.filterMin = (Texture.TextureFilter) value; break;
            case "cboMagFilter": settings.filterMag = (Texture.TextureFilter) value; break;
            case "cboWrapX": settings.wrapX = (Texture.TextureWrap) value; break;
            case "cboWrapY": settings.wrapY = (Texture.TextureWrap) value; break;
        }
    }

    @LmlAction("checkForUpdates") void checkForUpdates() {
        //TODO implement it
    }
    //endregion

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }
    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }

    private Stage getStage() {
        return interfaceService.getCurrentController().getStage();
    }

    private PackModel getSelectedPack() {
        return getProject().getSelectedPack();
    }

    private ProjectModel getProject() {
        return modelService.getProject();
    }

    @SuppressWarnings("unchecked")
    private <T extends Actor> T parseLml(FileHandle fileHandle) {
        LmlParser parser = interfaceService.getParser();
        parser.getData().addActionContainer(this.getClass().getSimpleName(), this);
        T actor = (T) parser.parseTemplate(fileHandle).first();
        parser.getData().removeActionContainer(this.getClass().getSimpleName());
        return actor;
    }

    private void updateViewsFromPack(PackModel pack) {
        if (viewsPacks.listPacks.getSelected() != pack) {
            viewsPacks.listPacks.setSelected(pack);
        }

        if (pack != null) {
            viewsPacks.edtInputDir.setText(pack.getInputDir());
            viewsPacks.edtOutputDir.setText(pack.getOutputDir());
            viewsPacks.edtFileName.setText(pack.getFilename());
            viewsPacks.edtFileName.setMessageText(pack.getName() + ".atlas");
        } else {
            viewsPacks.edtInputDir.setText(null);
            viewsPacks.edtOutputDir.setText(null);
            viewsPacks.edtFileName.setText(null);
        }

        if (pack != null) {
            TexturePacker.Settings settings = pack.getSettings();

            viewsSettings.chkUseFastAlgorithm.setChecked(settings.fast);
            viewsSettings.chkEdgePadding.setChecked(settings.edgePadding);
            viewsSettings.chkStripWhitespaceX.setChecked(settings.stripWhitespaceX);
            viewsSettings.chkStripWhitespaceY.setChecked(settings.stripWhitespaceY);
            viewsSettings.chkAllowRotation.setChecked(settings.rotation);
            viewsSettings.chkIncludeSubdirs.setChecked(settings.combineSubdirectories);
            viewsSettings.chkBleeding.setChecked(settings.bleed);
            viewsSettings.chkDuplicatePadding.setChecked(settings.duplicatePadding);
            viewsSettings.chkForcePot.setChecked(settings.pot);
            viewsSettings.chkUseAliases.setChecked(settings.alias);
            viewsSettings.chkIgnoreBlankImages.setChecked(settings.ignoreBlankImages);
            viewsSettings.chkDebug.setChecked(settings.debug);
            viewsSettings.chkUseIndices.setChecked(settings.useIndexes);
            viewsSettings.chkPremultiplyAlpha.setChecked(settings.premultiplyAlpha);

            ((IntSpinnerModel)viewsSettings.spnMinPageWidth.getModel()).setValue(settings.minWidth, false);
            ((IntSpinnerModel)viewsSettings.spnMinPageHeight.getModel()).setValue(settings.minHeight, false);
            ((IntSpinnerModel)viewsSettings.spnMaxPageWidth.getModel()).setValue(settings.maxWidth, false);
            ((IntSpinnerModel)viewsSettings.spnMaxPageHeight.getModel()).setValue(settings.maxHeight, false);
            ((IntSpinnerModel)viewsSettings.spnAlphaThreshold.getModel()).setValue(settings.alphaThreshold, false);
            ((IntSpinnerModel)viewsSettings.spnPaddingX.getModel()).setValue(settings.paddingX, false);
            ((IntSpinnerModel)viewsSettings.spnPaddingY.getModel()).setValue(settings.paddingY, false);

            ((FloatSpinnerModel) viewsSettings.spnJpegQuality.getModel()).setValue(BigDecimal.valueOf(settings.jpegQuality), false);

            viewsSettings.cboEncodingFormat.setSelected(settings.format);
            viewsSettings.cboOutputFormat.setSelected(settings.outputFormat);
            viewsSettings.cboMinFilter.setSelected(settings.filterMin);
            viewsSettings.cboMagFilter.setSelected(settings.filterMag);
            viewsSettings.cboWrapX.setSelected(settings.wrapX);
            viewsSettings.cboWrapY.setSelected(settings.wrapY);
        }
    }

    private void updatePackList() {
        Array<PackModel> packs = getProject().getPacks();
        viewsPacks.listPacks.setItems(packs);
        viewsPacks.listPacks.setSelected(getProject());
    }

    private void updateCanvas() {
        PackModel pack = getSelectedPack();
        canvas.reloadPack(pack);
    }

    private void updateRecentProjects() {
        Array<FileHandle> recentProjects = this.recentProjects.getRecentProjects();
        fileMenu.miOpenRecent.setDisabled(recentProjects.size == 0);
        fileMenu.pmOpenRecent.clear();
        for (final FileHandle file : recentProjects) {
            MenuItem menuItem = new MenuItem(file.nameWithoutExtension());
            menuItem.setShortcut(file.path()); // Will use shortcut label to display full project path
            menuItem.getShortcutCell().left().expandX();
            menuItem.getLabelCell().expand(false, false).left();
            menuItem.getImageCell().width(0); // Shrink image cell to zero, we wont use it
            menuItem.pack();
            menuItem.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ProjectModel project = projectSerializer.loadProject(file);
                    modelService.setProject(project);
                }
            });
            fileMenu.pmOpenRecent.addItem(menuItem);
        }
    }
}
