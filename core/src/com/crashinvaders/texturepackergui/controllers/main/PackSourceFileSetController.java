package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.config.attributes.OnDoubleClickLmlAttribute;
import com.crashinvaders.texturepackergui.config.attributes.OnRightClickLmlAttribute;
import com.crashinvaders.texturepackergui.config.filechooser.AppIconProvider;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.GlobalActions;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
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
public class PackSourceFileSetController implements ActionContainer {
    private static final String TAG = PackSourceFileSetController.class.getSimpleName();
    private static final FileHandleComparator fileHandleComparator = new FileHandleComparator();

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;

    @LmlActor("lvSourceFiles") ListView.ListViewTable<ListNode> listTable;
    private SourceFileSetAdapter listAdapter;

    private final Array<FileHandle> tmpFileHandles = new Array<>();
    private Stage stage;
    private boolean initialized;

    @Initiate void init() {
        interfaceService.getParser().getData().addActionContainer(TAG, this);
    }

    public void onViewCreated(Stage stage) {
        this.stage = stage;

        listAdapter = ((SourceFileSetAdapter) listTable.getListView().getAdapter());
//        actorsPacks.packListAdapter.getSelectionManager().setListener(new ListSelectionAdapter<PackModel, VisTable>() {
//            @Override
//            public void selected(PackModel pack, VisTable view) {
//                getProject().setSelectedPack(pack);
//                Gdx.app.postRunnable(normalizePackListScrollRunnable);
//            }
//        });
        initialized = true;

        //TODO remove
        try {
            PackModel.SourceFileSet sourceFileSet = modelService.getProject().getSelectedPack().getSourceFileSet();
            sourceFileSet.setMuteChangeEvents(true);
            sourceFileSet.addSource(Gdx.files.internal("lml/compression"));
            sourceFileSet.addSource(Gdx.files.internal("test/file0.png"));
            sourceFileSet.addSource(Gdx.files.internal("test/file1.png"));
            sourceFileSet.addSource(Gdx.files.internal("test/file2.png"));
            sourceFileSet.addSource(Gdx.files.internal("test/file3.png"));
            sourceFileSet.addSource(Gdx.files.internal("test/file4.png"));
            sourceFileSet.addSource(Gdx.files.internal("test/file5.png"));
            sourceFileSet.addIgnore(Gdx.files.internal("lml/preview"));
            sourceFileSet.addIgnore(Gdx.files.internal("lml/ignore.png"));
            sourceFileSet.addIgnore(Gdx.files.absolute("C:/assets/res/textures/offerings/bowl0.png"));
            sourceFileSet.setMuteChangeEvents(false);
        } catch (Exception ignored) { }

        updateListContent();
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void onEvent(ProjectPropertyChangedEvent event) {
        if (event.getProperty() == ProjectPropertyChangedEvent.Property.SELECTED_PACK) {
            updateListContent();
        }
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (modelService.getProject().getSelectedPack() != event.getPack()) return;
        if (event.getProperty() == PackPropertyChangedEvent.Property.SOURCE_FILE_SET) {
            updateListContent();
        }
    }

    @LmlAction("createAdapter") SourceFileSetAdapter createAdapter() {
        return new SourceFileSetAdapter(interfaceService.getParser());
    }

    @LmlAction("showContextMenu") void showContextMenu(OnRightClickLmlAttribute.Params params) {
        SourceFileSetAdapter.ViewHolder viewHolder = listAdapter.getViewHolder(params.actor);
        ListNode listNode = viewHolder.getListNode();

        PopupMenu popupMenu = LmlAutumnUtils.parseLml(interfaceService, "IGNORE", this, Gdx.files.internal("lml/sourceFileSetListMenu.lml"));

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

    @LmlAction("changeFileHandle") void changeFileHandle(OnDoubleClickLmlAttribute.Params params) {
        final ListNode listNode = listAdapter.getViewHolder(params.actor).getListNode();

        final FileChooser fileChooser = new FileChooser(listNode.fileHandle.parent(), FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        if (listNode.directory) {
            fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        }  else {
            fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
            fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                    .rule("Image files", "png").get()); //TODO localize and check if we need to support .jpeg
        }
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel.SourceFileSet sourceFileSet = App.inst().getModelService().getProject().getSelectedPack().getSourceFileSet();

                FileHandle fileHandle = files.first();
                if (fileHandle.equals(listNode.fileHandle)) return;

                if (listNode.type == ListNode.Type.Source) {
                    sourceFileSet.removeSource(listNode.fileHandle);
                    sourceFileSet.addSource(fileHandle);
                } else {
                    sourceFileSet.removeIgnore(listNode.fileHandle);
                    sourceFileSet.addIgnore(fileHandle);
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());

        if (FileUtils.fileExists(listNode.fileHandle)) { fileChooser.setSelectedFiles(listNode.fileHandle); }
    }

    @LmlAction("addSourceFiles") void addSourceFiles() {
        final FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("Image files", "png").get()); //TODO localize and check if we need to support .jpeg
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                PackModel.SourceFileSet sourceFileSet = App.inst().getModelService().getProject().getSelectedPack().getSourceFileSet();
                for (FileHandle file : files) {
                    sourceFileSet.addSource(file);
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
                PackModel.SourceFileSet sourceFileSet = App.inst().getModelService().getProject().getSelectedPack().getSourceFileSet();
                for (FileHandle file : files) {
                    sourceFileSet.addIgnore(file);
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());
    }

    @LmlAction("removeSelected") void removeSelected() {
        PackModel.SourceFileSet sourceFileSet = App.inst().getModelService().getProject().getSelectedPack().getSourceFileSet();
        Array<ListNode> selectedNodes = new Array<>(listAdapter.getSelection());
        for (ListNode selectedNode : selectedNodes) {
            if (selectedNode.type == ListNode.Type.Source) {
                sourceFileSet.removeSource(selectedNode.fileHandle);
            } else {
                sourceFileSet.removeIgnore(selectedNode.fileHandle);
            }
        }
    }

    private void updateListContent() {
        if (!initialized) return;

        // Clear list
        for (int i = 0; i < listAdapter.size(); i++) {
            Pools.free(listAdapter.get(i));
        }
        listAdapter.clear();

        PackModel pack = modelService.getProject().getSelectedPack();
        if (pack == null) return;

        PackModel.SourceFileSet sourceFileSet = pack.getSourceFileSet();

        tmpFileHandles.addAll(sourceFileSet.getSourceFiles());
        tmpFileHandles.sort(fileHandleComparator);
        for (FileHandle fileHandle : tmpFileHandles) {
            ListNode node = Pools.obtain(ListNode.class);
            node.init(fileHandle, ListNode.Type.Source);
            listAdapter.add(node);
        }
        tmpFileHandles.clear();
        tmpFileHandles.addAll(sourceFileSet.getIgnoreFiles());
        tmpFileHandles.sort(fileHandleComparator);
        for (FileHandle fileHandle : tmpFileHandles) {
            ListNode node = Pools.obtain(ListNode.class);
            node.init(fileHandle, ListNode.Type.Ignore);
            listAdapter.add(node);
        }
        tmpFileHandles.clear();
    }

    private static class SourceFileSetAdapter extends ArrayAdapter<ListNode, VisTable> {

        private final LmlParser lmlParser;

        public SourceFileSetAdapter(LmlParser lmlParser) {
            super(new Array<ListNode>());
            this.lmlParser = lmlParser;

            setSelectionMode(SelectionMode.MULTIPLE);
        }

        public ListNode getSelected() {
            Array<ListNode> selection = getSelectionManager().getSelection();
            if (selection.size == 0) return null;
            return selection.first();
        }

        public SourceFileSetAdapter.ViewHolder getViewHolder(ListNode item) {
            if (indexOf(item) == -1) return null;

            return (SourceFileSetAdapter.ViewHolder) getView(item).getUserObject();
        }

        public SourceFileSetAdapter.ViewHolder getViewHolder(Actor view) {
            return (SourceFileSetAdapter.ViewHolder) view.getUserObject();
        }

        @Override
        protected VisTable createView(ListNode item) {
            SourceFileSetAdapter.ViewHolder viewHolder = new SourceFileSetAdapter.ViewHolder(lmlParser.getData().getDefaultSkin(), item);
            lmlParser.createView(viewHolder, Gdx.files.internal("lml/sourceFileSetListItem.lml"));
            viewHolder.root.setUserObject(viewHolder);
            return viewHolder.root;
        }

        @Override
        protected void prepareViewBeforeAddingToTable(ListNode item, VisTable view) {
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
            private final ListNode listNode;
            private boolean selected;

            private boolean pathProcessed = false;

            public ViewHolder(Skin skin, ListNode listNode) {
                this.skin = skin;
                this.listNode = listNode;
            }

            @LmlAfter void initView() {
                root.pack();
                updateViewData();

                final Tooltip tooltip = new Tooltip();
                tooltip.clearChildren(); // Removing empty cell with predefined paddings.
                tooltip.add(listNode.fileHandle.path());
                tooltip.pack();
                tooltip.setAppearDelayTime(0.25f);
                tooltip.setTarget(lblName);
            }

            public void updateViewData() {
                processPathText();

                String imgName = "custom/ic-fileset-";
                if (listNode.directory) {
                    imgName += "dir";
                } else {
                    imgName += "file";
                }
                switch (listNode.type) {
                    case Source:
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

            public ListNode getListNode() {
                return listNode;
            }

            private void processPathText() {
                if (pathProcessed) return;
                pathProcessed = true;

                String pathText = listNode.fileHandle.path();

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
                    if (!listNode.directory && dotLastIndex > 0 && dotLastIndex - lastSlashIndex > 1) {
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

//                    pathText = "[light-grey]" + pathText.substring(0, lastSlashIndex+1) + "[]" + pathText.substring(lastSlashIndex+1);
                }

                lblName.setText(pathText);
            }
        }
    }

    static class ListNode implements Pool.Poolable {
        FileHandle fileHandle;
        Type type;
        boolean directory = false;

        public void init(FileHandle fileHandle, Type type) {
            this.fileHandle = fileHandle;
            this.type = type;
            this.directory = fileHandle.isDirectory();
        }

        @Override
        public void reset() {
            fileHandle = null;
            type = null;
        }

        public enum Type {
            Source, Ignore
        }
    }

    private static class FileHandleComparator implements Comparator<FileHandle> {

        @Override
        public int compare(FileHandle l, FileHandle r) {
            int dir = Boolean.compare(r.isDirectory(), l.isDirectory());
            if (dir != 0) return dir;

            return l.name().compareTo(r.name());
        }
    }
}
