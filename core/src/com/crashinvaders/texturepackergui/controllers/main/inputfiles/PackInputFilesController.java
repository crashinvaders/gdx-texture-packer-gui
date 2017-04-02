package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.config.attributes.OnDoubleClickLmlAttribute;
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
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

import java.util.Comparator;

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
        listAdapter.setItemsSorter(new SourceFileComparator());

        //TODO remove
        try {
            PackModel pack = modelService.getProject().getSelectedPack();
            pack.addSourceFile(Gdx.files.internal("lml/compression"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("test/file0.png"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("test/file1.png"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("test/file2.png"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("test/file3.png"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("test/file4.png"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("test/file5.png"), InputFile.Type.Input);
            pack.addSourceFile(Gdx.files.internal("lml/preview"), InputFile.Type.Ignore);
            pack.addSourceFile(Gdx.files.internal("lml/ignore.png"), InputFile.Type.Ignore);
            pack.addSourceFile(Gdx.files.absolute("C:/assets/res/textures/offerings/bowl0.png"), InputFile.Type.Ignore);
        } catch (Exception ignored) { }

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
                .rule("Image files", "png").get()); //TODO localize and check if we need to support .jpeg
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel pack = App.inst().getModelService().getProject().getSelectedPack();
                for (FileHandle file : files) {
                    pack.addSourceFile(file, InputFile.Type.Input);
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
                .rule("Image files", "png").get()); //TODO localize and check if we need to support .jpeg
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel pack = App.inst().getModelService().getProject().getSelectedPack();
                for (FileHandle file : files) {
                    pack.addSourceFile(file, InputFile.Type.Ignore);
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());
    }

    @LmlAction("removeSelected") void removeSelected() {
        PackModel pack = App.inst().getModelService().getProject().getSelectedPack();
        Array<InputFile> selectedNodes = new Array<>(listAdapter.getSelection());
        for (InputFile selectedNode : selectedNodes) {
            pack.removeSourceFile(selectedNode);
        }
    }

    private void reloadListContent() {
        if (!initialized) return;

        listAdapter.clear();

        PackModel pack = modelService.getProject().getSelectedPack();
        if (pack == null) return;

        Array<InputFile> inputFiles = pack.getSourceFiles();
        for (InputFile inputFile : inputFiles) {
            listAdapter.add(inputFile);
        }
    }

    private static class SourceFileSetAdapter extends ArrayAdapter<InputFile, VisTable> {

        private final LmlParser lmlParser;

        public SourceFileSetAdapter(LmlParser lmlParser) {
            super(new Array<InputFile>());
            this.lmlParser = lmlParser;

            setSelectionMode(SelectionMode.MULTIPLE);
        }

        public InputFile getSelected() {
            Array<InputFile> selection = getSelectionManager().getSelection();
            if (selection.size == 0) return null;
            return selection.first();
        }

        public SourceFileSetAdapter.ViewHolder getViewHolder(InputFile item) {
            if (indexOf(item) == -1) return null;

            return (SourceFileSetAdapter.ViewHolder) getView(item).getUserObject();
        }

        public SourceFileSetAdapter.ViewHolder getViewHolder(Actor view) {
            return (SourceFileSetAdapter.ViewHolder) view.getUserObject();
        }

        @Override
        protected VisTable createView(InputFile item) {
            SourceFileSetAdapter.ViewHolder viewHolder = new SourceFileSetAdapter.ViewHolder(lmlParser.getData().getDefaultSkin(), item);
            lmlParser.createView(viewHolder, Gdx.files.internal("lml/inputFileListItem.lml"));
            viewHolder.root.setUserObject(viewHolder);
            return viewHolder.root;
        }

        @Override
        protected void prepareViewBeforeAddingToTable(InputFile item, VisTable view) {
            super.prepareViewBeforeAddingToTable(item, view);
        }

        @Override
        protected void selectView(VisTable view) {
            SourceFileSetAdapter.ViewHolder viewHolder = (SourceFileSetAdapter.ViewHolder) view.getUserObject();
            viewHolder.setSelected(true);
        }

        @Override
        protected void deselectView(VisTable view) {
            SourceFileSetAdapter.ViewHolder viewHolder = (SourceFileSetAdapter.ViewHolder) view.getUserObject();
            viewHolder.setSelected(false);
        }

        public static class ViewHolder {
            @LmlActor("root") VisTable root;
            @LmlActor("lblName") VisLabel lblName;
            @LmlActor("imgTypeIndicator") Image imgTypeIndicator;

            private final Skin skin;
            private final InputFile inputFile;
            private final Tooltip tooltip;

            private boolean selected = false;
            private boolean pathProcessed = false;

            public ViewHolder(Skin skin, InputFile inputFile) {
                this.skin = skin;
                this.inputFile = inputFile;

                tooltip = new Tooltip();
                tooltip.setAppearDelayTime(0.25f);
            }

            @LmlAfter void initView() {
                tooltip.setTarget(lblName);

                root.pack();
                updateViewData();
            }

            public void remapData() {
                pathProcessed = false;
                updateViewData();
            }

            public void updateViewData() {
                processPathText();

                tooltip.setText(inputFile.getFileHandle().path());

                String imgName = "custom/ic-fileset-";
                if (inputFile.isDirectory()) {
                    imgName += "dir";
                } else {
                    imgName += "file";
                }
                switch (inputFile.getType()) {
                    case Input:
                        break;
                    case Ignore:
                        imgName += "-ignore";
                        break;
                }
                imgTypeIndicator.setDrawable(skin.getDrawable(imgName));
            }

            public void setSelected(boolean selected) {
                if (this.selected == selected) return;
                this.selected = selected;

                root.setBackground(selected ? skin.getDrawable("padded-list-selection") : null);
            }

            public InputFile getInputFile() {
                return inputFile;
            }

            private void processPathText() {
                if (pathProcessed) return;
                pathProcessed = true;

                String pathText = inputFile.getFileHandle().path();

                // Cut the last slash
                int lastSlashIndex = pathText.lastIndexOf("/");
                if (lastSlashIndex == pathText.length()-1) {
                    pathText = pathText.substring(0, lastSlashIndex);
                }

                // Try to shorten path by cutting slash divided pieces starting from beginning
                GlyphLayout glyphLayout = lblName.getGlyphLayout();
                boolean pathCut = false;
                while (true) {
                    glyphLayout.setText(lblName.getStyle().font, pathText, lblName.getStyle().fontColor, 0f, lblName.getLabelAlign(), false);
                    if (glyphLayout.width < (lblName.getWidth() - 8)) break;  // -8 is extra ellipsis ".../" space

                    int slashIndex = pathText.indexOf("/");
                    if (slashIndex == -1) break;
                    pathText = pathText.substring(slashIndex+1);
                    pathCut = true;
                }

                // Add ellipsis if path was cut
                if (pathCut) {
                    pathText = ".../"+pathText;
                }

                lastSlashIndex = pathText.lastIndexOf("/");
                if (lastSlashIndex > 0) {
                    int dotLastIndex = pathText.lastIndexOf(".");

                    StringBuilder sb = new StringBuilder();
                    sb.append("[light-grey]");
                    sb.append(pathText.substring(0, lastSlashIndex + 1));
                    sb.append("[]");
                    if (!inputFile.isDirectory() && dotLastIndex > 0 && dotLastIndex - lastSlashIndex > 1) {
                        // Grey out extension text
                        sb.append(pathText.substring(lastSlashIndex + 1, dotLastIndex));
                        sb.append("[light-grey]");
                        sb.append(pathText.substring(dotLastIndex));
                        sb.append("[]");
                    } else {
                        // No extension
                        sb.append(pathText.substring(lastSlashIndex + 1));
                    }
                    pathText = sb.toString();
                }

                lblName.setText(pathText);
            }
        }
    }

    private static class SourceFileComparator implements Comparator<InputFile> {

        @Override
        public int compare(InputFile l, InputFile r) {
            int type = l.getType().compareTo(r.getType());
            if (type != 0) return type;

            int dir = Boolean.compare(r.isDirectory(), l.isDirectory());
            if (dir != 0) return dir;

            return l.getFileHandle().path().compareTo(r.getFileHandle().path());
        }
    }
}
