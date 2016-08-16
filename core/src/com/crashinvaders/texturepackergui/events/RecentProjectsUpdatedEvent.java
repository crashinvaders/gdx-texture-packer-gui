package com.crashinvaders.texturepackergui.events;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class RecentProjectsUpdatedEvent {

    private final Array<FileHandle> recentProjects;

    public RecentProjectsUpdatedEvent(Array<FileHandle> recentProjects) {
        this.recentProjects = recentProjects;
    }

    public Array<FileHandle> getRecentProjects() {
        return recentProjects;
    }
}
