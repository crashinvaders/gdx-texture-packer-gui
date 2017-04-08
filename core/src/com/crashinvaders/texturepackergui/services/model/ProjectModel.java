package com.crashinvaders.texturepackergui.services.model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.compression.EtcCompressionModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngCompressionModel;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

public class ProjectModel {

    private final Array<PackModel> packs = new Array<>(true, 16);
    private final Color previewBackgroundColor = new Color(Color.WHITE);
    private PackModel selectedPack;
    private FileHandle projectFile;
    private EventDispatcher eventDispatcher;
    private PngCompressionModel pngCompression;
    private EtcCompressionModel etcCompression;

    public ProjectModel() {
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        for (PackModel pack : packs) {
            pack.setEventDispatcher(eventDispatcher);
        }
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
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, ProjectPropertyChangedEvent.Property.SELECTED_PACK));
        }
    }

    public Array<PackModel> getPacks() {
        return packs;
    }

    public void addPack(PackModel pack) {
        packs.add(pack);
        pack.setEventDispatcher(eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, ProjectPropertyChangedEvent.Property.PACKS));
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
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, ProjectPropertyChangedEvent.Property.PACKS));
        }
    }

    public PngCompressionModel getPngCompression() {
        return pngCompression;
    }
    
    public EtcCompressionModel getEtcCompression() {
        return etcCompression;
    }

    public void setPngCompression(PngCompressionModel pngCompression) {
        if (this.pngCompression == pngCompression) return;
        this.pngCompression = pngCompression;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, ProjectPropertyChangedEvent.Property.PNG_COMPRESSION));
        }
    }
    
    public void setEtcCompression(EtcCompressionModel etcCompression) {
        if (this.etcCompression == etcCompression) return;
        this.etcCompression = etcCompression;

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, ProjectPropertyChangedEvent.Property.ETC_COMPRESSION));
        }
    }

    public Color getPreviewBackgroundColor() {
        return previewBackgroundColor;
    }

    public void setPreviewBackgroundColor(Color color) {
        this.previewBackgroundColor.set(color);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new ProjectPropertyChangedEvent(this, ProjectPropertyChangedEvent.Property.PREVIEW_BG_COLOR));
        }
    }
}
