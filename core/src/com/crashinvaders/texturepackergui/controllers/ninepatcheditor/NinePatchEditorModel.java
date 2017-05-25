package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.texturepackergui.services.model.InputFile;

public class NinePatchEditorModel implements Disposable {
    private static final int[] tmpIntArray = new int[4];
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;

    public final Pixmap imagePixmap;
    public final ZoomModel zoomModel = new ZoomModel();
    public final GridValues patchValues = new GridValues();
    public final GridValues contentValues = new GridValues();

    public NinePatchEditorModel(FileHandle imageFile) {
        this.imagePixmap = new Pixmap(imageFile);
    }

    @Override
    public void dispose() {
        imagePixmap.dispose();
    }

    //region Utility methods
    /** @return values in a nine patch format. Values are arranged in the next order: left, right, top, bottom. */
    public int[] readPatchValues() {
        return readPatchValues(tmpIntArray);
    }

    /**
     * @param out must be a 4-component int array.
     * @return values in a nine patch format. Values are arranged in the next order: left, right, top, bottom.
     */
    public int[] readPatchValues(int[] out) {
        if (out.length != 4) throw new IllegalArgumentException("\"out\" must be an 4-component int array!");

        out[LEFT] = patchValues.left.get();
        out[RIGHT] = imagePixmap.getWidth() - patchValues.right.get();
        out[TOP] = imagePixmap.getHeight() - patchValues.top.get();
        out[BOTTOM] = patchValues.bottom.get();
        return out;
    }

    /** @return values in a nine patch format. Values are arranged in the next order: left, right, top, bottom. */
    public int[] readPaddingValues() {
        return readPaddingValues(tmpIntArray);
    }

    /**
     * @param out must be a 4-component int array.
     * @return values in a nine patch format. Values are arranged in the next order: left, right, top, bottom.
     */
    public int[] readPaddingValues(int[] out) {
        if (out.length != 4) throw new IllegalArgumentException("\"out\" must be an 4-component int array!");

        if (contentValues.left.get() == 0 && contentValues.right.get() == imagePixmap.getWidth()) {
            out[LEFT] = -1;
            out[RIGHT] = -1;
        } else {
            out[LEFT] = contentValues.left.get();
            out[RIGHT] = imagePixmap.getWidth() - contentValues.right.get();
        }
        if (contentValues.bottom.get() == 0 && contentValues.top.get() == imagePixmap.getHeight()) {
            out[TOP] = -1;
            out[BOTTOM] = -1;
        } else {
            out[TOP] = imagePixmap.getHeight() - contentValues.top.get();
            out[BOTTOM] = contentValues.bottom.get();
        }
        return out;
    }

    public void saveToInputFile(InputFile inputFile) {
        InputFile.NinePatchProps ninePatchProps = inputFile.getNinePatchProps();

        int[] patchValues = readPatchValues();
        ninePatchProps.left = patchValues[LEFT];
        ninePatchProps.right = patchValues[RIGHT];
        ninePatchProps.top = patchValues[TOP];
        ninePatchProps.bottom = patchValues[BOTTOM];

        int[] contentValues = readPaddingValues();
        ninePatchProps.padLeft = contentValues[LEFT];
        ninePatchProps.padRight = contentValues[RIGHT];
        ninePatchProps.padTop = contentValues[TOP];
        ninePatchProps.padBottom = contentValues[BOTTOM];
    }
    //endregion
}
