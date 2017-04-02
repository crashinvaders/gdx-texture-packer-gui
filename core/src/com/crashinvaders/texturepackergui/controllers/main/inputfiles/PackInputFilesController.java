package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.config.attributes.OnRightClickLmlAttribute;
import com.crashinvaders.texturepackergui.config.filechooser.AppIconProvider;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.LmlAutumnUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@Component
public class PackInputFilesController implements ActionContainer {
    private static final String TAG = PackInputFilesController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;
    @Inject InputFileDialogController inputFileDialog;

    @LmlActor("lvSourceFiles") ListView.ListViewTable<InputFile> listTable;
    private SourceFileSetAdapter listAdapter;

    private Stage stage;
    private boolean initialized;

    @Initiate void init() {
        interfaceService.getParser().getData().addActionContainer(TAG, this);
    }

    public void onViewCreated(Stage stage) {
        this.stage = stage;

        listAdapter = ((SourceFileSetAdapter) listTable.getListView().getAdapter());

        initialized = true;

        reloadListContent();
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void onEvent(ProjectPropertyChangedEvent event) {
        if (event.getProperty() == ProjectPropertyChangedEvent.Property.SELECTED_PACK) {
            reloadListContent();
        }
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (modelService.getProject().getSelectedPack() != event.getPack()) return;
        switch (event.getProperty()) {
            case INPUT_FILE_ADDED:
                listAdapter.add(event.getInputFile());
                break;
            case INPUT_FILE_REMOVED:
                listAdapter.removeValue(event.getInputFile(), true);
                break;
        }
    }

    @OnEvent(InputFilePropertyChangedEvent.class) void onEvent(InputFilePropertyChangedEvent event) {
        InputFile inputFile = event.getInputFile();
        SourceFileSetAdapter.ViewHolder viewHolder = listAdapter.getViewHolder(inputFile);
        if (viewHolder != null) {
            viewHolder.remapData();
        }
    }

    @LmlAction("createAdapter") SourceFileSetAdapter createAdapter() {
        return new SourceFileSetAdapter(interfaceService.getParser());
    }

    @LmlAction("showContextMenu") void showContextMenu(OnRightClickLmlAttribute.Params params) {
        SourceFileSetAdapter.ViewHolder viewHolder = listAdapter.getViewHolder(params.actor);
        InputFile inputFile = viewHolder.getInputFile();

        PopupMenu popupMenu = LmlAutumnUtils.parseLml(interfaceService, "IGNORE", this, Gdx.files.internal("lml/inputFileListMenu.lml"));

//        MenuItem menuItem;
//        menuItem = popupMenu.findActor("miRename");
//        menuItem.setDisabled(pack == null);
//        menuItem = popupMenu.findActor("miDelete");
//        menuItem.setDisabled(pack == null);
//        menuItem = popupMenu.findActor("miCopy");
//        menuItem.setDisabled(pack == null);
//        menuItem = popupMenu.findActor("miMoveUp");
//        menuItem.setDisabled(pack == null);
//        menuItem = popupMenu.findActor("miMoveDown");
//        menuItem.setDisabled(pack == null);
//        menuItem = popupMenu.findActor("miPackSelected");
//        menuItem.setDisabled(pack == null);
//        menuItem = popupMenu.findActor("miPackAll");
//        menuItem.setDisabled(getProject().getPacks().size == 0);
//        menuItem = popupMenu.findActor("miCopySettingsToAllPacks");
//        menuItem.setDisabled(pack == null);

        popupMenu.showMenu(stage, params.stageX, params.stageY);
    }

    @LmlAction("showInputFileDialog") void showInputFileDialog() {
        InputFile inputFile = listAdapter.getSelected();
        if (inputFile == null) return;
        inputFileDialog.show(inputFile);
    }

    @LmlAction("addInputFiles") void addSourceFiles() {
        final FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("Image files", "png", "jpg", "jpeg").get()); //TODO localize
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel pack = App.inst().getModelService().getProject().getSelectedPack();
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
                PackModel pack = App.inst().getModelService().getProject().getSelectedPack();
                for (FileHandle file : files) {
                    pack.addInputFile(file, InputFile.Type.Ignore);
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());
    }

    @LmlAction("removeSelected") void removeSelected() {
        PackModel pack = App.inst().getModelService().getProject().getSelectedPack();
        Array<InputFile> selectedNodes = new Array<>(listAdapter.getSelection());
        for (InputFile selectedNode : selectedNodes) {
            pack.removeInputFile(selectedNode);
        }
    }

    private void reloadListContent() {
        if (!initialized) return;

        listAdapter.clear();

        PackModel pack = modelService.getProject().getSelectedPack();
        if (pack == null) return;

        Array<InputFile> inputFiles = pack.getInputFiles();
        for (InputFile inputFile : inputFiles) {
            listAdapter.add(inputFile);
        }
    }

}
