package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

public class NinePatchEditorModel implements Disposable {
    final Pixmap imagePixmap;
    final ZoomModel zoomModel = new ZoomModel();
    final GridValues patchValues = new GridValues();
    final GridValues contentValues = new GridValues();

    public NinePatchEditorModel(FileHandle imageFile) {
        this.imagePixmap = new Pixmap(imageFile);
    }

    @Override
    public void dispose() {
        imagePixmap.dispose();
    }
}
