package com.crashinvaders.texturepackergui.services.extensionmodules;

import com.badlogic.gdx.utils.Array;

public class RepositoryModuleData {
    public String name;
    public Array<Revision> revisions;

    public static class Revision {
        public int revision;
        public String file;
    }

    public Revision findRevision(int code) {
        for (Revision revision : revisions) {
            if (revision.revision == code) return revision;
        }
        return null;
    }
}
