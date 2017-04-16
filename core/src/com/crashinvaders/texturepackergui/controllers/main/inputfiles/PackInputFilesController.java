package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.config.attributes.OnRightClickLmlAttribute;
import com.crashinvaders.texturepackergui.config.filechooser.AppIconProvider;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.*;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.LmlAutumnUtils;
import com.github.czyzby.autumn.annotation.*;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
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

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;
    @Inject ModelUtils modelUtils;
    @Inject InputFilePropertiesDialogController inputFileDialog;

    @LmlActor("btnPfAddInput") VisImageButton btnAddInput;
    @LmlActor("btnPfAddIgnore") VisImageButton btnAddIgnore;
    @LmlActor("btnPfRemove") VisImageButton btnRemove;
    @LmlActor("btnPfProperties") VisImageButton btnProperties;
    @LmlActor("btnPfInclude") VisImageButton btnInclude;
    @LmlActor("btnPfExclude") VisImageButton btnExclude;
    @LmlActor("lvInputFiles") ListView.ListViewTable<InputFile> listTable;
    private InputFileListAdapter listAdapter;

    private Stage stage;
    private boolean initialized;

    @Initiate void init() {
        interfaceService.getParser().getData().addActionContainer(TAG, this);
    }

    public void onViewCreated(Stage stage) {
        this.stage = stage;

        listAdapter = ((InputFileListAdapter) listTable.getListView().getAdapter());
        listAdapter.getSelectionManager().setListener(new AbstractListAdapter.ListSelectionListener<InputFile, VisTable>() {
            @Override
            public void selected(InputFile item, VisTable view) {
                updateButtonsState();
            }
            @Override
            public void deselected(InputFile item, VisTable view) {
                updateButtonsState();
            }
        });

        initialized = true;

        reloadListContent();
        updateButtonsState();
    }

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        if (initialized) {
            reloadListContent();
            updateButtonsState();
        }
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void onEvent(ProjectPropertyChangedEvent event) {
        if (event.getProperty() == ProjectPropertyChangedEvent.Property.SELECTED_PACK) {
            reloadListContent();
            updateButtonsState();
        }
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (getSelectedPack() != event.getPack()) return;
        switch (event.getProperty()) {
            case INPUT_FILE_ADDED:
                listAdapter.add(event.getInputFile());
                updateButtonsState();
                break;
            case INPUT_FILE_REMOVED:
                listAdapter.removeValue(event.getInputFile(), true);
                updateButtonsState();
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
        return new InputFileListAdapter(interfaceService.getParser());
    }

    @LmlAction("showContextMenu") void showContextMenu(OnRightClickLmlAttribute.Params params) {
        AbstractListAdapter.ListSelection<InputFile, VisTable> sm = listAdapter.getSelectionManager();

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

    @LmlAction("addInputFiles") void addInputFiles() {
        final FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("Image files", "png", "jpg", "jpeg").get()); //TODO localize
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel pack = getSelectedPack();
                for (FileHandle file : files) {
                    pack.addInputFile(file, InputFile.Type.Input);
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());
    }

    @LmlAction("addIgnoreFiles") void addIgnoreFiles() {
        final FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("Image files", "png", "jpg", "jpeg").get()); //TODO localize
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel pack = getSelectedPack();
                for (FileHandle file : files) {
                    pack.addInputFile(file, InputFile.Type.Ignore);
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());
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

}
