package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.crashinvaders.texturepackergui.controllers.model.InputFile;

import java.util.Comparator;

class InputFileComparator implements Comparator<InputFile> {

    @Override
    public int compare(InputFile l, InputFile r) {
        int type = l.getType().compareTo(r.getType());
        if (type != 0) return type;

        int dir = Boolean.compare(r.isDirectory(), l.isDirectory());
        if (dir != 0) return dir;

        return l.getFileHandle().path().compareTo(r.getFileHandle().path());
    }
}
