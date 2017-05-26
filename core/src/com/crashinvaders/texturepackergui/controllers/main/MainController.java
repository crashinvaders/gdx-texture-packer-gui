package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.config.attributes.OnRightClickLmlAttribute;
import com.crashinvaders.texturepackergui.controllers.FileDragDropController;
import com.crashinvaders.texturepackergui.controllers.ScaleFactorsDialogController;
import com.crashinvaders.texturepackergui.controllers.main.inputfiles.PackInputFilesController;
import com.crashinvaders.texturepackergui.controllers.ninepatcheditor.NinePatchEditorDialog;
import com.crashinvaders.texturepackergui.events.*;
import com.crashinvaders.texturepackergui.services.GlobalActions;
import com.crashinvaders.texturepackergui.services.RecentProjectsRepository;
import com.crashinvaders.texturepackergui.services.model.*;
import com.crashinvaders.texturepackergui.services.model.compression.*;
import com.crashinvaders.texturepackergui.services.projectserializer.ProjectSerializer;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.crashinvaders.texturepackergui.utils.LmlAutumnUtils;
import com.crashinvaders.texturepackergui.utils.Scene2dUtils;
import com.crashinvaders.texturepackergui.views.canvas.Canvas;
import com.github.czyzby.autumn.annotation.Destroy;
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
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.ToastManager;
import com.kotcrab.vis.ui.util.adapter.ListSelectionAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import java.math.BigDecimal;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
@View(id = MainController.VIEW_ID, value = "lml/main.lml", first = true)
public class MainController implements ActionContainer, ViewResizer {
    public static final String VIEW_ID = "main";
    public static final String LOG = MainController.class.getSimpleName();
    public static final String PREF_KEY_PACK_LIST_SPLIT = "pack_list_split";

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;
    @Inject LocaleService localeService;
    @Inject EventDispatcher eventDispatcher;
    @Inject GlobalActions globalActions;
    @Inject ProjectSerializer projectSerializer;
    @Inject RecentProjectsRepository recentProjects;
    @Inject CanvasController canvasController;
    @Inject ScaleFactorsDialogController scaleFactorsDialogController;
    @Inject NinePatchEditorDialog ninePatchEditorDialog;
    @Inject @LmlInject PackInputFilesController packInputFilesController;
    @Inject @LmlInject FileDragDropController fileDragDropController;

    @ViewStage Stage stage;

    @LmlActor("canvas") Canvas canvas;
    @LmlActor("packListSplitPane") VisSplitPane packListSplitPane;

    @LmlActor({"paneLockGlobalSettings",
            "paneLockSettings",
            "paneLockPreview",
            "paneLockPackFiles",
            "paneLockPackGeneral"})
    Array<Actor> packPaneLockers;

    @LmlInject PackListActors actorsPacks;
    @LmlInject PackSettingsActors actorsPackSettings;
    @LmlInject GlobalSettingsActors actorsGlobalSettings;
    @LmlInject FileMenuActors actorsFileMenu;
    @LmlInject PackMenuActors actorsPackMenu;
    @LmlInject HelpMenuActors actorsHelpMenu;

    private ToastManager toastManager;

    /** Indicates that view is shown and ready to be used in code */
    private boolean initialized;

    @SuppressWarnings("unchecked")
    @LmlAfter
    void initialize() {
        actorsPackSettings.cboEncodingFormat.setItems(WidgetData.textureFormats);
        actorsPackSettings.cboOutputFormat.setItems(WidgetData.outputFormats);
        actorsPackSettings.cboMinFilter.setItems(WidgetData.textureFilters);
        actorsPackSettings.cboMagFilter.setItems(WidgetData.textureFilters);
        actorsPackSettings.cboWrapX.setItems(WidgetData.textureWraps);
        actorsPackSettings.cboWrapY.setItems(WidgetData.textureWraps);
        actorsGlobalSettings.cboPngCompression.setItems(WidgetData.CompressionPng.values());
        actorsGlobalSettings.cboEtcCompression.setItems(WidgetData.CompressionEtc.values());

        actorsPacks.packList = actorsPacks.packListTable.getListView();
        actorsPacks.packListAdapter = ((PackListAdapter) actorsPacks.packList.getAdapter());
        actorsPacks.packListAdapter.getSelectionManager().setListener(new ListSelectionAdapter<PackModel, VisTable>() {
            @Override
            public void selected(PackModel pack, VisTable view) {
                getProject().setSelectedPack(pack);
                Gdx.app.postRunnable(normalizePackListScrollRunnable);
            }
        });

        toastManager = new ToastManager(getStage());
        toastManager.setAlignment(Align.bottomRight);

        canvasController.initialize(canvas);
        packInputFilesController.onViewCreated(stage);
        fileDragDropController.onViewCreated(stage);

        // Load pack list split value
        {
            Preferences prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
            float splitValue = prefs.getFloat(PREF_KEY_PACK_LIST_SPLIT, 0f);
            packListSplitPane.setSplitAmount(splitValue);
        }

        initialized = true;

        updatePackList();
        updateViewsFromPack(getSelectedPack());
        updateRecentProjects();
        updatePngCompression();
        updateEtcCompression();

//        //TODO remove
//        Gdx.app.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                ninePatchEditorDialog.setImageFile(Gdx.files.absolute("D:/chest0.png"));
//                interfaceService.showDialog(ninePatchEditorDialog.getClass());
//            }
//        });
    }

    @Destroy
    void destroy() {
        // Save pack list split value
        float packListSplitValue = packListSplitPane.getSplit();
        Preferences prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
        prefs.putFloat(PREF_KEY_PACK_LIST_SPLIT, packListSplitValue).flush();
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
            updatePngCompression();
            updateEtcCompression();
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
                case PNG_COMPRESSION:
                    updatePngCompression();
                    break;
                case ETC_COMPRESSION:
                	updateEtcCompression();
                    break;
            }
        }
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (initialized) {
            switch (event.getProperty()) {
                case NAME:
                case SCALE_FACTORS:
                    if (event.getPack() == getSelectedPack()) {
                        updateViewsFromPack(event.getPack());
                    }
                    break;
                case OUTPUT:
                    if (event.getPack() == getSelectedPack()) {
                        actorsPacks.edtOutputDir.setProgrammaticChangeEvents(false);
                        actorsPacks.edtOutputDir.setText(event.getPack().getOutputDir());
                        actorsPacks.edtOutputDir.setProgrammaticChangeEvents(true);
                    }
                    break;
                case FILENAME:
                    if (event.getPack() == getSelectedPack()) {
                        actorsPacks.edtFileName.setProgrammaticChangeEvents(false);
                        actorsPacks.edtFileName.setText(event.getPack().getFilename());
                        actorsPacks.edtFileName.setProgrammaticChangeEvents(true);
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

    @OnEvent(PackListOrderChangedEvent.class) void onEvent(PackListOrderChangedEvent event) {
        if (initialized) {
            //TODO rearrange items within adapter (do not recreate items)
            updatePackList();
        }
    }

    @OnEvent(ShowToastEvent.class) void onEvent(ShowToastEvent event) {
        if (initialized) {
            if (event.getContent() != null) {
                toastManager.show(event.getContent(), event.getDuration());
            } else {
                toastManager.show(event.getMessage(), event.getDuration());
            }
        }
    }

    @OnEvent(RemoveToastEvent.class) void onEvent(RemoveToastEvent event) {
        if (initialized) {
            if (event.getToast() != null) {
                toastManager.remove(event.getToast());
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
    @LmlAction("createPacksListAdapter") PackListAdapter createPacksListAdapter() {
        return new PackListAdapter(interfaceService.getParser());
    }

    @LmlAction("onPackListRightClick") void onPackListRightClick(OnRightClickLmlAttribute.Params params) {
        PackListAdapter.ViewHolder viewHolder = actorsPacks.packListAdapter.getViewHolder(params.actor);
        PackModel pack = viewHolder.getPack();

        PopupMenu popupMenu = LmlAutumnUtils.parseLml(interfaceService, VIEW_ID, this, Gdx.files.internal("lml/packListMenu.lml"));

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

    @LmlAction("onCanvasRightClick") void onCanvasRightClick(OnRightClickLmlAttribute.Params params) {
        PopupMenu popupMenu = LmlAutumnUtils.parseLml(interfaceService, VIEW_ID, this, Gdx.files.internal("lml/preview/canvasMenu.lml"));
        PackModel pack = getSelectedPack();

        MenuItem menuItem;
        menuItem = popupMenu.findActor("miRepack");
        menuItem.setDisabled(pack == null);

        popupMenu.showMenu(getStage(), params.stageX, params.stageY);
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
            case "cbFlattenPaths": settings.flattenPaths = checkBox.isChecked(); break;
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
            case "cbLimitMemory": settings.limitMemory = checkBox.isChecked(); break;
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
        if (!initialized) return;

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
            case "cboOutputFormat": settings.outputFormat = (String) value; break;
            case "cboPngCompression": onPngCompressionTypeChanged(); break;
            case "cboEtcCompression": onEtcCompressionTypeChanged(); break;
        }
    }

    @LmlAction("onScalesBtnClick") void onScalesBtnClick(Button scalesButton) {
        if (!initialized) return;

        PackModel pack = getSelectedPack();
        if (pack == null) return;

        scaleFactorsDialogController.setPackModel(pack);
        interfaceService.showDialog(scaleFactorsDialogController.getClass());
    }
    //endregion

    private void updateViewsFromPack(PackModel pack) {
        if (actorsPacks.packListAdapter.getSelected() != pack) {
            actorsPacks.packListAdapter.getSelectionManager().deselectAll();
            if (pack != null) {
                actorsPacks.packListAdapter.getSelectionManager().select(pack);
            }
        }

        // Update pack list item
        PackListAdapter.ViewHolder viewHolder = actorsPacks.packListAdapter.getViewHolder(pack);
        if (viewHolder != null) {
            viewHolder.updateViewData();
        }

        if (pack != null) {
            actorsPacks.edtOutputDir.setText(pack.getOutputDir());
            actorsPacks.edtFileName.setText(pack.getFilename());
            actorsPacks.edtFileName.setMessageText(pack.getName() + ".atlas");
        } else {
            actorsPacks.edtOutputDir.setText(null);
            actorsPacks.edtFileName.setText(null);
        }

        if (pack != null) {
            TexturePacker.Settings settings = pack.getSettings();

            actorsPackSettings.cbUseFastAlgorithm.setChecked(settings.fast);
            actorsPackSettings.cbEdgePadding.setChecked(settings.edgePadding);
            actorsPackSettings.cbStripWhitespaceX.setChecked(settings.stripWhitespaceX);
            actorsPackSettings.cbStripWhitespaceY.setChecked(settings.stripWhitespaceY);
            actorsPackSettings.cbAllowRotation.setChecked(settings.rotation);
            actorsPackSettings.cbIncludeSubdirs.setChecked(settings.combineSubdirectories);
            actorsPackSettings.cbFlattenPaths.setChecked(settings.flattenPaths);
            actorsPackSettings.cbBleeding.setChecked(settings.bleed);
            actorsPackSettings.cbDuplicatePadding.setChecked(settings.duplicatePadding);
            actorsPackSettings.cbForcePot.setChecked(settings.pot);
            actorsPackSettings.cbUseAliases.setChecked(settings.alias);
            actorsPackSettings.cbIgnoreBlankImages.setChecked(settings.ignoreBlankImages);
            actorsPackSettings.cbDebug.setChecked(settings.debug);
            actorsPackSettings.cbUseIndices.setChecked(settings.useIndexes);
            actorsPackSettings.cbPremultiplyAlpha.setChecked(settings.premultiplyAlpha);
            actorsPackSettings.cbGrid.setChecked(settings.grid);
            actorsPackSettings.cbSquare.setChecked(settings.square);
            actorsPackSettings.cbLimitMemory.setChecked(settings.limitMemory);

            ((IntSpinnerModel) actorsPackSettings.spnMinPageWidth.getModel()).setValue(settings.minWidth, false);
            ((IntSpinnerModel) actorsPackSettings.spnMinPageHeight.getModel()).setValue(settings.minHeight, false);
            ((IntSpinnerModel) actorsPackSettings.spnMaxPageWidth.getModel()).setValue(settings.maxWidth, false);
            ((IntSpinnerModel) actorsPackSettings.spnMaxPageHeight.getModel()).setValue(settings.maxHeight, false);
            ((IntSpinnerModel) actorsPackSettings.spnAlphaThreshold.getModel()).setValue(settings.alphaThreshold, false);
            ((IntSpinnerModel) actorsPackSettings.spnPaddingX.getModel()).setValue(settings.paddingX, false);
            ((IntSpinnerModel) actorsPackSettings.spnPaddingY.getModel()).setValue(settings.paddingY, false);

            ((FloatSpinnerModel) actorsPackSettings.spnJpegQuality.getModel()).setValue(BigDecimal.valueOf(settings.jpegQuality), false);

            actorsPackSettings.cboEncodingFormat.setSelected(settings.format);
            actorsPackSettings.cboOutputFormat.setSelected(settings.outputFormat);
            actorsPackSettings.cboMinFilter.setSelected(settings.filterMin);
            actorsPackSettings.cboMagFilter.setSelected(settings.filterMag);
            actorsPackSettings.cboWrapX.setSelected(settings.wrapX);
            actorsPackSettings.cboWrapY.setSelected(settings.wrapY);

            // Scale factors
            {
                StringBuilder sb = new StringBuilder();
                Array<ScaleFactorModel> scaleFactors = pack.getScaleFactors();
                for (int i = 0; i < scaleFactors.size; i++) {
                    ScaleFactorModel scaleFactor = scaleFactors.get(i);
                    sb.append(String.format(Locale.US, "%.2f", scaleFactor.getFactor()));
                    if (i < scaleFactors.size-1) { sb.append(", "); }
                }
                actorsPackSettings.eetbScaleFactors.setText(sb.toString());
            }
        }

        // Update pane lockers
        for (Actor locker : packPaneLockers) {
            locker.setVisible(pack == null);
        }
    }

    private void updatePackList() {
        Array<PackModel> packs = getProject().getPacks();
        PackModel pack = getSelectedPack();

        boolean acRegistered = LmlAutumnUtils.registerActionContainer(interfaceService, VIEW_ID, this);

        actorsPacks.packListAdapter.clear();
        actorsPacks.packListAdapter.addAll(packs);
        actorsPacks.packListAdapter.getSelectionManager().deselectAll();
        if (pack != null) {
            actorsPacks.packListAdapter.getSelectionManager().select(pack);
        }

        if (acRegistered) {
            LmlAutumnUtils.unregisterActionContainer(interfaceService, VIEW_ID);
        }
    }

	private void updatePngCompression () {
		PngCompressionModel compModel = getProject().getPngCompression();
		WidgetData.CompressionPng compValue = WidgetData.CompressionPng.valueOf(compModel == null ? null : compModel.getType());
        actorsGlobalSettings.cboPngCompression.setSelected(compValue);
        actorsGlobalSettings.containerPngCompSettings.setVisible(compValue.hasSettings);
    }

	private void updateEtcCompression () {
		EtcCompressionModel compModel = getProject().getEtcCompression();
		WidgetData.CompressionEtc compValue = WidgetData.CompressionEtc.valueOf(compModel == null ? null : compModel.getType());
        actorsGlobalSettings.cboEtcCompression.setSelected(compValue);
        actorsGlobalSettings.containerEtcCompSettings.setVisible(compValue.hasSettings);
    }


    private void updateRecentProjects() {
        Array<FileHandle> recentProjects = this.recentProjects.getRecentProjects();
        actorsFileMenu.miOpenRecent.setDisabled(recentProjects.size == 0);
        actorsFileMenu.pmOpenRecent.clear();
        for (final FileHandle file : recentProjects) {
            if (file.equals(getProject().getProjectFile())) continue;

            MenuItem menuItem = new MenuItem(file.nameWithoutExtension());
            menuItem.setShortcut(CommonUtils.ellipsize(file.path(), 72)); // Will use shortcut label to display file path
            menuItem.getShortcutCell().left().expandX();
            menuItem.getLabelCell().expand(false, false).left();
            menuItem.getImageCell().width(0); // Shrink image cell to zero, we don't need it
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
            actorsFileMenu.pmOpenRecent.addItem(menuItem);
        }
    }

    private void onPngCompressionTypeChanged() {
        if (!initialized) return;

        ProjectModel project = getProject();
        PngCompressionType compType = actorsGlobalSettings.cboPngCompression.getSelected().type;

        if (compType == null) {
            project.setPngCompression(null);
            return;
        }

        if (project.getPngCompression() == null || compType != project.getPngCompression().getType()) {
            switch (compType) {
                case PNGTASTIC:
                    project.setPngCompression(new PngtasticCompressionModel());
                    break;
                case ZOPFLI:
                    project.setPngCompression(new ZopfliCompressionModel());
                    break;
                case TINY_PNG:
                    project.setPngCompression(new TinyPngCompressionModel());
                    break;
                default:
                    project.setPngCompression(null);
            }
        }
    }

    private void onEtcCompressionTypeChanged() {
        if (!initialized) return;

        ProjectModel project = getProject();
        EtcCompressionType compType = actorsGlobalSettings.cboEtcCompression.getSelected().type;

        if (compType == null) {
            project.setEtcCompression(null);
            return;
        }

        if (project.getEtcCompression() == null || compType != project.getEtcCompression().getType()) {
            switch (compType) {
                case KTX:
                    project.setEtcCompression(new EtcCompressionModel(EtcCompressionType.KTX));
                    break;
                default:
                    project.setEtcCompression(null);
            }
        }
    }

    //region Utility methods
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

//    @SuppressWarnings("unchecked")
//    private <T extends Actor> T parseLml(FileHandle fileHandle) {
//        LmlParser parser = interfaceService.getParser();
//        String actionContainerId = interfaceService.getController(this.getClass()).getViewId();
//        boolean explicitAddActionContainer = parser.getData().getActionContainer(actionContainerId) == null;
//
//        if (explicitAddActionContainer) {
//            parser.getData().addActionContainer(actionContainerId, this);
//        }
//
//        T actor = (T) parser.parseTemplate(fileHandle).first();
//
//        if (explicitAddActionContainer) {
//            parser.getData().removeActionContainer(actionContainerId);
//        }
//        return actor;
//    }
    //endregion

    private Runnable normalizePackListScrollRunnable = new Runnable() {
        @Override
        public void run() {
            PackModel pack = getSelectedPack();
            if (pack != null) {
                Scene2dUtils.scrollDownToSelectedListItem(actorsPacks.packList, pack);
            }
        }
    };
}
