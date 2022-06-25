package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.common.statehash.StateHashable;
import com.crashinvaders.texturepackergui.controllers.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent.Property;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

public class ProjectModel implements StateHashable {

    private final Array<PackModel> packs = new Array<>(true, 16);
    private final Color previewBackgroundColor = new Color(Color.WHITE);
    private final ProjectSettingsModel settings = new ProjectSettingsModel();
    private FileHandle projectFile;
    private FileTypeModel fileType = new PngFileTypeModel(); // PNG file type by default

    private EventDispatcher eventDispatcher;
    private PackModel selectedPack;

    public ProjectModel() {
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        fileType.setEventDispatcher(eventDispatcher);
        for (int i = 0; i < packs.size; i++) {
            packs.get(i).setEventDispatcher(eventDispatcher);
        }
    }

    public ProjectSettingsModel getSettings() {
        return settings;
    }

    public FileHandle getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(FileHandle projectFile) {
        this.projectFile = projectFile;
    }

    public PackModel getSelectedPack() {
        return selectedPack;
    }

    public void setSelectedPack(PackModel selectedPack) {
        if (this.selectedPack == selectedPack) return;

        if (selectedPack != null && !packs.contains(selectedPack, true)) return;
        this.selectedPack = selectedPack;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, Property.SELECTED_PACK));
        }
    }

    public Array<PackModel> getPacks() {
        return packs;
    }

    public void addPack(PackModel pack) {
        packs.add(pack);
        pack.setEventDispatcher(eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, Property.PACKS));
        }
    }

    public void removePack(PackModel pack) {
        if (!packs.contains(pack, true)) return;

        if (selectedPack == pack) {
            setSelectedPack(null);
        }

        packs.removeValue(pack, true);
        pack.setEventDispatcher(null);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, Property.PACKS));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends FileTypeModel> T getFileType() {
        return (T)fileType;
    }

    public void setFileType(FileTypeModel fileType) {
        if (this.fileType == fileType) return;

        this.fileType.setEventDispatcher(null);
        this.fileType = fileType;
        this.fileType.setEventDispatcher(eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, Property.FILE_TYPE));
        }
    }

    public Color getPreviewBackgroundColor() {
        return previewBackgroundColor;
    }

    public void setPreviewBackgroundColor(Color color) {
        this.previewBackgroundColor.set(color);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, Property.PREVIEW_BG_COLOR));
        }
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(packs, previewBackgroundColor, projectFile, fileType, settings);
    }
}
