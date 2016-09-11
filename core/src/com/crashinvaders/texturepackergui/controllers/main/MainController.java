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
import com.crashinvaders.texturepackergui.events.*;
import com.crashinvaders.texturepackergui.services.ProjectSerializer;
import com.crashinvaders.texturepackergui.services.RecentProjectsRepository;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.Scene2dUtils;
import com.crashinvaders.texturepackergui.views.canvas.Canvas;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewResizer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.annotation.LmlInject;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.ToastManager;
import com.kotcrab.vis.ui.widget.*;
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
    @Inject EventDispatcher eventDispatcher;
    @Inject ProjectSerializer projectSerializer;
    @Inject RecentProjectsRepository recentProjects;
    @Inject CanvasController canvasController;

    @ViewStage Stage stage;

    @LmlActor("canvas") Canvas canvas;
    @LmlInject() ProjectConfigController viewsPacks;
    @LmlInject() PackSettingsController viewsSettings;
    @LmlInject() FileMenuController fileMenu;
    @LmlInject() PackMenuController packMenu;
    @LmlInject() HelpMenuController helpMenu;

    private ToastManager toastManager;

    /** Indicates that view is shown and ready to be used in code */
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

        canvasController.initialize(canvas);

        updatePackList();
        updateViewsFromPack(getSelectedPack());
        updateRecentProjects();
    }

    @Override
    public void resize(Stage stage, int width, int height) {
        final Viewport viewport = stage.getViewport();
        viewport.update(width, height, true);

        toastManager.resize();
    }

    //region Events
    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        if (initialized) {
            updatePackList();
            updateViewsFromPack(event.getProject().getSelectedPack());
            updateRecentProjects();
        }
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void onEvent(ProjectPropertyChangedEvent event) {
        if (initialized) {
            switch (event.getProperty()) {
                case SELECTED_PACK:
                    updateViewsFromPack(event.getProject().getSelectedPack());
                    break;
                case PACKS:
                    updatePackList();
                    break;
            }
        }
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (initialized) {
            switch (event.getProperty()) {
                case NAME:
                    if (event.getPack() == getSelectedPack()) {
                        updateViewsFromPack(event.getPack());
                    }
                    break;
                case INPUT:
                    if (event.getPack() == getSelectedPack()) {
                        viewsPacks.edtInputDir.setProgrammaticChangeEvents(false);
                        viewsPacks.edtInputDir.setText(event.getPack().getInputDir());
                        viewsPacks.edtInputDir.setProgrammaticChangeEvents(true);
                    }
                    break;
                case OUTPUT:
                    if (event.getPack() == getSelectedPack()) {
                        viewsPacks.edtOutputDir.setProgrammaticChangeEvents(false);
                        viewsPacks.edtOutputDir.setText(event.getPack().getOutputDir());
                        viewsPacks.edtOutputDir.setProgrammaticChangeEvents(true);
                    }
                    break;
                case FILENAME:
                    if (event.getPack() == getSelectedPack()) {
                        viewsPacks.edtFileName.setProgrammaticChangeEvents(false);
                        viewsPacks.edtFileName.setText(event.getPack().getFilename());
                        viewsPacks.edtFileName.setProgrammaticChangeEvents(true);
                    }
                    break;
            }
        }
    }

    @OnEvent(RecentProjectsUpdatedEvent.class) void onEvent(RecentProjectsUpdatedEvent event) {
        if (initialized) {
            updateRecentProjects();
        }
    }

    @OnEvent(PackListOrderChanged.class) void onEvent(PackListOrderChanged event) {
        if (initialized) {
            Array items = viewsPacks.listPacks.getItems();
            items.clear();
            items.addAll(getProject().getPacks());
        }
    }

    @OnEvent(ToastNotificationEvent.class) void onEvent(ToastNotificationEvent event) {
        if (initialized) {

            if (event.getContent() != null) {
                toastManager.show(event.getContent(), event.getDuration());
            } else {
                toastManager.show(event.getMessage(), event.getDuration());
            }
        }
    }

    @OnEvent(VersionUpdateCheckEvent.class) boolean onEvent(VersionUpdateCheckEvent event) {
        switch (event.getAction()) {
            case CHECK_STARTED:
            case CHECK_FINISHED:
                return OnEvent.KEEP;
            case FINISHED_ERROR:
            case FINISHED_UP_TO_DATE:
                return OnEvent.REMOVE;
            case FINISHED_UPDATE_AVAILABLE:
//                toastManager.show
                return OnEvent.REMOVE;
            default:
                // Should never happen
                throw new IllegalStateException("Unexpected version check event: " + event.getAction());
        }
    }

    //endregion

    //region Actions
    @LmlAction("onPackListSelectionChanged") void onPackListSelectionChanged(final VisList list) {
        // Scroll down to selection
        Gdx.app.postRunnable(normalizePackListScrollRunnable);

        final PackModel selectedPack = (PackModel) list.getSelected();
        if (getSelectedPack() == selectedPack) return;

        final ProjectModel project = getProject();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                project.setSelectedPack(selectedPack);
            }
        });
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

    @LmlAction("onSettingsCbChecked") void onSettingsCbChecked(VisCheckBox checkBox) {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        TexturePacker.Settings settings = pack.getSettings();
        switch (checkBox.getName()) {
            case "cbUseFastAlgorithm": settings.fast = checkBox.isChecked(); break;
            case "cbEdgePadding": settings.edgePadding = checkBox.isChecked(); break;
            case "cbStripWhitespaceX": settings.stripWhitespaceX = checkBox.isChecked(); break;
            case "cbStripWhitespaceY": settings.stripWhitespaceY = checkBox.isChecked(); break;
            case "cbAllowRotation": settings.rotation = checkBox.isChecked(); break;
            case "cbIncludeSubdirs": settings.combineSubdirectories = checkBox.isChecked(); break;
            case "cbBleeding": settings.bleed = checkBox.isChecked(); break;
            case "cbDuplicatePadding": settings.duplicatePadding = checkBox.isChecked(); break;
            case "cbForcePot": settings.pot = checkBox.isChecked(); break;
            case "cbUseAliases": settings.alias = checkBox.isChecked(); break;
            case "cbIgnoreBlankImages": settings.ignoreBlankImages = checkBox.isChecked(); break;
            case "cbDebug": settings.debug = checkBox.isChecked(); break;
            case "cbUseIndices": settings.useIndexes = checkBox.isChecked(); break;
            case "cbPremultiplyAlpha": settings.premultiplyAlpha = checkBox.isChecked(); break;
            case "cbGrid": settings.grid = checkBox.isChecked(); break;
            case "cbSquare": settings.square = checkBox.isChecked(); break;
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
            case "cboMinFilter": settings.filterMin = (Texture.TextureFilter) value; break;
            case "cboMagFilter": settings.filterMag = (Texture.TextureFilter) value; break;
            case "cboWrapX": settings.wrapX = (Texture.TextureWrap) value; break;
            case "cboWrapY": settings.wrapY = (Texture.TextureWrap) value; break;
            case "cboOutputFormat": {
                settings.outputFormat = (String) value;
                updateCompressionOptions();
                break;
            }
            case "cboCompression": onCompressionTypeChanged(); break;
        }
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
        return stage;
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

            viewsSettings.cbUseFastAlgorithm.setChecked(settings.fast);
            viewsSettings.cbEdgePadding.setChecked(settings.edgePadding);
            viewsSettings.cbStripWhitespaceX.setChecked(settings.stripWhitespaceX);
            viewsSettings.cbStripWhitespaceY.setChecked(settings.stripWhitespaceY);
            viewsSettings.cbAllowRotation.setChecked(settings.rotation);
            viewsSettings.cbIncludeSubdirs.setChecked(settings.combineSubdirectories);
            viewsSettings.cbBleeding.setChecked(settings.bleed);
            viewsSettings.cbDuplicatePadding.setChecked(settings.duplicatePadding);
            viewsSettings.cbForcePot.setChecked(settings.pot);
            viewsSettings.cbUseAliases.setChecked(settings.alias);
            viewsSettings.cbIgnoreBlankImages.setChecked(settings.ignoreBlankImages);
            viewsSettings.cbDebug.setChecked(settings.debug);
            viewsSettings.cbUseIndices.setChecked(settings.useIndexes);
            viewsSettings.cbPremultiplyAlpha.setChecked(settings.premultiplyAlpha);
            viewsSettings.cbGrid.setChecked(settings.grid);
            viewsSettings.cbSquare.setChecked(settings.square);

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

        PackModel selectedPack = getSelectedPack();
        if (viewsPacks.listPacks.getSelected() != selectedPack) {
            viewsPacks.listPacks.setSelected(selectedPack);
        }
    }

    private void updateRecentProjects() {
        Array<FileHandle> recentProjects = this.recentProjects.getRecentProjects();
        fileMenu.miOpenRecent.setDisabled(recentProjects.size == 0);
        fileMenu.pmOpenRecent.clear();
        for (final FileHandle file : recentProjects) {
            if (file.equals(getProject().getProjectFile())) continue;

            MenuItem menuItem = new MenuItem(file.nameWithoutExtension());
            menuItem.setShortcut(file.path()); // Will use shortcut label to display full project path
            menuItem.getShortcutCell().left().expandX();
            menuItem.getLabelCell().expand(false, false).left();
            menuItem.getImageCell().width(0); // Shrink image cell to zero, we wont use it
            menuItem.pack();
            menuItem.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            ProjectModel project = projectSerializer.loadProject(file);
                            if (project != null) {
                                modelService.setProject(project);
                            }
                        }
                    });
                }
            });
            fileMenu.pmOpenRecent.addItem(menuItem);
        }
    }

    private void updateCompressionOptions() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        String outputFormat = pack.getSettings().outputFormat;
        switch (outputFormat) {
            case "png":
                viewsSettings.cboCompression.setItems(WidgetData.compressionPng.values());
                break;
            case "jpg":
                viewsSettings.cboCompression.setItems(WidgetData.compressionJpg.values());
                break;
        }
    }

    private void onCompressionTypeChanged() {
        PackModel pack = getSelectedPack();
        if (pack == null) return;

        Container container = viewsSettings.compressionViewContainer;
        Object compression = viewsSettings.cboCompression.getSelected();

        if (compression instanceof WidgetData.compressionPng) {
            switch (((WidgetData.compressionPng) compression)) {
                case pngtastic:
                    container.setActor(parseLml(Gdx.files.internal("lml/testCompression.lml")));
                    break;
                case zopfli:
                    container.setActor(parseLml(Gdx.files.internal("lml/testCompression.lml")));
                    break;
                default:
                    container.setActor(null);
            }

        } else if (compression instanceof WidgetData.compressionJpg) {
            switch (((WidgetData.compressionJpg) compression)) {
                default:
                    container.setActor(null);
            }
        }

//        container.invalidateHierarchy();
    }

    private Runnable normalizePackListScrollRunnable = new Runnable() {
        @Override
        public void run() {
            Scene2dUtils.scrollDownToSelectedListItem(viewsPacks.scrPacks, viewsPacks.listPacks);
        }
    };
}
