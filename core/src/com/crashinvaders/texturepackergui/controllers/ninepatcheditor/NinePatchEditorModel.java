package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
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

        GridValues values = this.patchValues;

        if (!values.hasValues()) {
            out[LEFT] = 0;
            out[RIGHT] = 0;
            out[TOP] = 0;
            out[BOTTOM] = 0;
            return out;
        }

        if (values.left.get() == 0 && values.right.get() == 0) {
            out[LEFT] = 0;
            out[RIGHT] = imagePixmap.getWidth();
        } else {
            out[LEFT] = values.left.get();
            out[RIGHT] = values.right.get();
        }
        if (values.bottom.get() == 0 && values.top.get() == 0) {
            out[TOP] = 0;
            out[BOTTOM] = imagePixmap.getHeight();
        } else {
            out[TOP] = values.top.get();
            out[BOTTOM] = values.bottom.get();
        }
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

        GridValues values = this.contentValues;

//        if (!values.hasValues()) {
//            out[LEFT] = 0;
//            out[RIGHT] = 0;
//            out[TOP] = 0;
//            out[BOTTOM] = 0;
//            return out;
//        }
//
//        if (values.left.get() == 0 && values.right.get() == 0) {
//            out[LEFT] = -1;
//            out[RIGHT] = -1;
//        } else {
//            out[LEFT] = values.left.get();
//            out[RIGHT] = values.right.get();
//        }
//        if (values.bottom.get() == 0 && values.top.get() == 0) {
//            out[TOP] = -1;
//            out[BOTTOM] = -1;
//        } else {
//            out[TOP] = values.top.get();
//            out[BOTTOM] = values.bottom.get();
//        }

        out[LEFT] = values.left.get();
        out[RIGHT] = values.right.get();
        out[TOP] = values.top.get();
        out[BOTTOM] = values.bottom.get();
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

    public void loadFromInputFile(InputFile inputFile) {
        InputFile.NinePatchProps ninePatchProps = inputFile.getNinePatchProps();

        patchValues.left.set(MathUtils.clamp(ninePatchProps.left, 0, imagePixmap.getWidth()));
        patchValues.right.set(MathUtils.clamp(ninePatchProps.right, 0, imagePixmap.getWidth()));
        patchValues.top.set(MathUtils.clamp(ninePatchProps.top, 0, imagePixmap.getHeight()));
        patchValues.bottom.set(MathUtils.clamp(ninePatchProps.bottom, 0, imagePixmap.getHeight()));

        if (patchValues.right.get() == imagePixmap.getWidth()) patchValues.right.set(0);
        if (patchValues.bottom.get() == imagePixmap.getHeight()) patchValues.bottom.set(0);

        contentValues.left.set(MathUtils.clamp(ninePatchProps.padLeft, 0, imagePixmap.getWidth()));
        contentValues.right.set(MathUtils.clamp(ninePatchProps.padRight, 0, imagePixmap.getWidth()));
        contentValues.top.set(MathUtils.clamp(ninePatchProps.padTop, 0, imagePixmap.getHeight()));
        contentValues.bottom.set(MathUtils.clamp(ninePatchProps.padBottom, 0, imagePixmap.getHeight()));
    }
    //endregion
}
