package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.badlogic.gdx.tools.texturepacker.PageFileWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;

public class PackProcessingNode {
    /** Compression result percents */
    public static final String META_COMPRESSION_RATE = "compressionRate";
    /** Total files size after compression (in bytes) */
    public static final String META_FILE_SIZE = "fileSize";
    /** Time when processing was started (nanoseconds) */
    public static final String META_START_TIME = "startTime";
    /** Time when processing was finished (nanoseconds) */
    public static final String META_END_TIME = "endTime";
    /** Total processing time (nanoseconds) */
    public static final String META_TOTAL_TIME = "totalTime";
    /** Total amount of texture pages */
    public static final String META_ATLAS_PAGES = "atlasPages";

    private final ObjectMap<String, Object> metadata = new ObjectMap<>();
    private final ProjectModel project;
    private final PackModel pack;
    private PackModel origPack;
    private PageFileWriter pageFileWriter;  // Should be set by other processors before PackingProcessor gets to the work
    private String log = "";

    public PackProcessingNode(ProjectModel project, PackModel pack) {
        this.project = project;
        this.pack = pack;
        this.origPack = pack;
    }

    public ProjectModel getProject() {
        return project;
    }

    public PackModel getPack() {
        return pack;
    }

    public PackModel getOrigPack() {
        return origPack;
    }

    public void setOrigPack(PackModel origPack) {
        this.origPack = origPack;
    }

    public String getLog() {
        return log;
    }

    void setLog(String log) {
        this.log = log;
    }

    public PageFileWriter getPageFileWriter() {
        return pageFileWriter;
    }

    public void setPageFileWriter(PageFileWriter pageFileWriter) {
        this.pageFileWriter = pageFileWriter;
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        return (T) metadata.get(key, defaultValue);
    }
}
