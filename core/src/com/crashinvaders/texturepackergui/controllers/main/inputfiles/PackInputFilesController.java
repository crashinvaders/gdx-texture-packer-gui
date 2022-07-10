package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.scene2d.actions.ActionsExt;
import com.crashinvaders.texturepackergui.controllers.FileDialogService;
import com.crashinvaders.texturepackergui.events.*;
import com.crashinvaders.texturepackergui.lml.attributes.OnRightClickLmlAttribute;
import com.crashinvaders.texturepackergui.utils.AppIconProvider;
import com.crashinvaders.texturepackergui.controllers.model.*;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.LmlAutumnUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@Component
public class PackInputFilesController implements ActionContainer {
    private static final String TAG = PackInputFilesController.class.getSimpleName();

    @Inject EventDispatcher eventDispatcher;
    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;
    @Inject ModelUtils modelUtils;
    @Inject InputFilePropertiesDialogController inputFileDialog;
    @Inject FileDialogService fileDialogService;

    @LmlActor("btnPfAddInput") VisImageButton btnAddInput;
    @LmlActor("btnPfAddIgnore") VisImageButton btnAddIgnore;
    @LmlActor("btnPfRemove") VisImageButton btnRemove;
    @LmlActor("btnPfProperties") VisImageButton btnProperties;
    @LmlActor("btnPfInclude") VisImageButton btnInclude;
    @LmlActor("btnPfExclude") VisImageButton btnExclude;
    @LmlActor("lvInputFiles") ListView.ListViewTable<InputFile> listTable;
    private InputFileListAdapter listAdapter;

    @LmlActor("pifOnboardingRoot") Group pifOnboardingRoot;
    @LmlActor("pifOnboardingBackground") Image pifOnboardingBackground;
    @LmlActor("pifOnboardingContent") Group pifOnboardingContent;
    @LmlActor("pifOnboardingBtnNew") Button pifOnboardingBtnNew;

    private Stage stage;

    private boolean initialized = false;
    private boolean wasOnboardingPanelVisible = false;

    @Initiate void init() {
        interfaceService.getParser().getData().addActionContainer(TAG, this);
    }

    public void onViewCreated(Stage stage) {
        this.stage = stage;

        listAdapter = ((InputFileListAdapter) listTable.getListView().getAdapter());
        listAdapter.getSelectionManager().setListener(new AbstractListAdapter.ListSelectionListener<InputFile, Stack>() {
            @Override
            public void selected(InputFile item, Stack view) {
                updateButtonsState();
            }
            @Override
            public void deselected(InputFile item, Stack view) {
                updateButtonsState();
            }
        });

        initialized = true;

        reloadListContent();
        updateButtonsState();
        refreshOnboardingView();
    }

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        if (initialized) {
            reloadListContent();
            updateButtonsState();
            refreshOnboardingView();
        }
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void onEvent(ProjectPropertyChangedEvent event) {
        if (event.getProperty() == ProjectPropertyChangedEvent.Property.SELECTED_PACK) {
            reloadListContent();
            updateButtonsState();
            refreshOnboardingView();
        }
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (getSelectedPack() != event.getPack()) return;

        InputFile inputFile = event.getInputFile();
        switch (event.getProperty()) {
            case INPUT_FILE_ADDED:
                listAdapter.add(inputFile);
                listAdapter.getViewHolder(inputFile).animateHighlight();
                updateButtonsState();
                refreshOnboardingView();
                break;
            case INPUT_FILE_REMOVED:
                listAdapter.removeValue(inputFile, true);
                updateButtonsState();
                refreshOnboardingView();
                break;
        }
    }

    @OnEvent(InputFilePropertyChangedEvent.class) void onEvent(InputFilePropertyChangedEvent event) {
        InputFile inputFile = event.getInputFile();
        InputFileListAdapter.ViewHolder viewHolder = listAdapter.getViewHolder(inputFile);
        if (viewHolder != null) {
            viewHolder.remapData();
        }
    }

    @LmlAction("createAdapter") InputFileListAdapter createAdapter() {
        return new InputFileListAdapter(interfaceService.getParser(), eventDispatcher);
    }

    @LmlAction("resetSelection") void resetSelection() {
        listAdapter.getSelectionManager().deselectAll();
    }

    @LmlAction("showContextMenu") void showContextMenu(OnRightClickLmlAttribute.Params params) {
        AbstractListAdapter.ListSelection<InputFile, Stack> sm = listAdapter.getSelectionManager();

        // Make sure that target item is selected
        InputFileListAdapter.ViewHolder viewHolder = listAdapter.getViewHolder(params.actor);
        boolean selected = listAdapter.isSelected(viewHolder);
        if (!selected) {
            sm.select(viewHolder.getInputFile());
        }

        boolean canBeIncluded = false;
        for (InputFile inputFile : sm.getSelection()) {
            if (!inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Ignore) {
                canBeIncluded = true;
                break;
            }
        }
        boolean canBeExcluded = false;
        for (InputFile inputFile : sm.getSelection()) {
            if (!inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Input) {
                canBeExcluded = true;
                break;
            }
        }

        PopupMenu popupMenu = LmlAutumnUtils.parseLml(interfaceService, "IGNORE", this, Gdx.files.internal("lml/inputFileListMenu.lml"));

        MenuItem menuItem;
        menuItem = popupMenu.findActor("miIncludeSelected");
        menuItem.setDisabled(!canBeIncluded);
        menuItem = popupMenu.findActor("miExcludeSelected");
        menuItem.setDisabled(!canBeExcluded);

        popupMenu.showMenu(stage, params.stageX, params.stageY);
    }

    @LmlAction("showInputFileDialog") void showInputFileDialog() {
        InputFile inputFile = listAdapter.getSelected();
        if (inputFile == null) return;
        inputFileDialog.show(inputFile);
    }

    @LmlAction("addInputDirectory") void addInputDirectory() {
        fileDialogService.pickDirectory("Add input directory", null, new FileDialogService.CallbackAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                PackModel pack = getSelectedPack();
                pack.addInputFile(files.first(), InputFile.Type.Input);
            }
        });
    }

    @LmlAction("addInputFiles") void addInputFiles() {
        fileDialogService.openMultipleFiles("Add input images", null,
                new FileDialogService.FileFilter[]{new FileDialogService.FileFilter("Image files", "png", "jpg", "jpeg")},
                new FileDialogService.CallbackAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                PackModel pack = getSelectedPack();
                for (FileHandle file : files) {
                    pack.addInputFile(file, InputFile.Type.Input);
                }
            }
        });
    }

    @LmlAction("addIgnoreFiles") void addIgnoreFiles() {
        fileDialogService.openMultipleFiles("Add ignore images", null,
                new FileDialogService.FileFilter[]{new FileDialogService.FileFilter("Image files", "png", "jpg", "jpeg")},
                new FileDialogService.CallbackAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        PackModel pack = getSelectedPack();
                        for (FileHandle file : files) {
                            pack.addInputFile(file, InputFile.Type.Ignore);
                        }
                    }
                });
    }

    @LmlAction("removeSelected") void removeSelected() {
        PackModel pack = getSelectedPack();
        Array<InputFile> selectedNodes = new Array<>(listAdapter.getSelection());
        for (InputFile selectedNode : selectedNodes) {
            pack.removeInputFile(selectedNode);
        }
    }

    @LmlAction("includeSelected") void includeSelected() {
        PackModel pack = getSelectedPack();
        Array<InputFile> selectedNodes = new Array<>(listAdapter.getSelection());

        for (InputFile node : selectedNodes) {
            if (!node.isDirectory() && node.getType() == InputFile.Type.Ignore) {
                InputFile newInputFile = modelUtils.includeInputFile(pack, node);
                if (newInputFile != null) {
                    listAdapter.getSelectionManager().select(newInputFile);
                }
            }
        }
    }

    @LmlAction("excludeSelected") void excludeSelected() {
        PackModel pack = getSelectedPack();
        Array<InputFile> selectedNodes = new Array<>(listAdapter.getSelection());

        for (InputFile node : selectedNodes) {
            if (!node.isDirectory() && node.getType() == InputFile.Type.Input) {
                InputFile newInputFile = modelUtils.excludeInputFile(pack, node);
                if (newInputFile != null) {
                    listAdapter.getSelectionManager().select(newInputFile);
                }
            }
        }
    }

    private void updateButtonsState() {
        if (!initialized) return;

        PackModel pack = getSelectedPack();
        Array<InputFile> selection = listAdapter.getSelection();

        boolean canBeIncluded = false;
        for (InputFile inputFile : selection) {
            if (!inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Ignore) {
                canBeIncluded = true;
                break;
            }
        }
        boolean canBeExcluded = false;
        for (InputFile inputFile : selection) {
            if (!inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Input) {
                canBeExcluded = true;
                break;
            }
        }

        btnAddInput.setDisabled(pack == null);
        btnAddIgnore.setDisabled(pack == null);
        btnRemove.setDisabled(pack == null || selection.size == 0);
        btnProperties.setDisabled(pack == null || selection.size == 0);
        btnInclude.setDisabled(pack == null || selection.size == 0 || !canBeIncluded);
        btnExclude.setDisabled(pack == null || selection.size == 0 || !canBeExcluded);
    }

    private void reloadListContent() {
        if (!initialized) return;

        listAdapter.clear();

        PackModel pack = getSelectedPack();
        if (pack == null) return;

        Array<InputFile> inputFiles = pack.getInputFiles();
        for (InputFile inputFile : inputFiles) {
            listAdapter.add(inputFile);
        }
    }

    private PackModel getSelectedPack() {
        ProjectModel project = modelService.getProject();
        if (project == null) {
            return null;
        }
        return project.getSelectedPack();
    }

    private void refreshOnboardingView() {
        PackModel selectedPack = modelService.getProject().getSelectedPack();
        boolean visible = selectedPack != null && selectedPack.getInputFiles().isEmpty();

        if (visible == wasOnboardingPanelVisible) return;
        wasOnboardingPanelVisible = visible;

        if (visible) {
            pifOnboardingRoot.setVisible(true);
            pifOnboardingBackground.getColor().a = 0f;
            pifOnboardingBtnNew.setTransform(true);
            pifOnboardingBtnNew.addAction(Actions.forever(Actions.sequence(
                    Actions.delay(3f),
                    ActionsExt.origin(Align.center),
                    Actions.scaleTo(1.1f, 0.95f),
                    Actions.targeting(pifOnboardingBackground, Actions.alpha(0.25f)),
                    Actions.parallel(
                            Actions.scaleTo(1.0f, 1.0f, 0.5f, Interpolation.elasticOut),
                            Actions.targeting(pifOnboardingBackground, Actions.fadeOut(0.5f))
                    )
            )));
        } else {
            pifOnboardingRoot.setVisible(false);
            pifOnboardingBtnNew.setTransform(false);
            pifOnboardingBtnNew.clearActions();
        }
    }
}
