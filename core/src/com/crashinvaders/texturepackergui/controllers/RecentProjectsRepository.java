package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.events.ProjectSerializerEvent;
import com.crashinvaders.texturepackergui.events.RecentProjectsUpdatedEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.kiwi.util.common.Strings;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecentProjectsRepository {
    private static final String PREF_NAME = "recent_projects.xml";
    private static final String PREF_KEY_FILES = "files";
    private static final String FILE_SEPARATOR = ";";

    @Inject EventDispatcher eventDispatcher;

    private final Array<FileHandle> recentProjects = new Array<>(true, 16);
    private Preferences prefs;

    @Initiate void initialize() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        loadData();
    }

    @OnEvent(value = ProjectSerializerEvent.class) void onProjectSerializerEvent(ProjectSerializerEvent event) {
        FileHandle file = event.getFile();

        recentProjects.removeValue(file, false);
        recentProjects.insert(0, file);
        saveData();

        eventDispatcher.postEvent(new RecentProjectsUpdatedEvent(recentProjects));
    }

    public Array<FileHandle> getRecentProjects() {
        return recentProjects;
    }

    private void saveData() {
        if (recentProjects.size == 0) return;

        List<String> strings = new ArrayList<>(recentProjects.size);
        for (int i = 0; i < recentProjects.size; i++) {
            strings.add(recentProjects.get(i).path());
        }

        String serialized = Strings.join(FILE_SEPARATOR, strings);
        prefs.putString(PREF_KEY_FILES, serialized);
        prefs.flush();
    }

    private void loadData() {
        String serialized = prefs.getString(PREF_KEY_FILES, null);
        if (serialized == null) return;

        String[] filePaths = serialized.split(";");

        recentProjects.clear();
        for (String filePath : filePaths) {
            FileHandle fileHandle = Gdx.files.absolute(filePath);
            if (fileHandle.exists()) {
                recentProjects.add(fileHandle);
            }
        }

        eventDispatcher.postEvent(new RecentProjectsUpdatedEvent(recentProjects));
    }

}
