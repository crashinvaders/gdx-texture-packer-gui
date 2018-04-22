package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.texturepackergui.controllers.model.InputFile;

public class NinePatchEditorModel implements Disposable {
    private static final int[] tmpIntArray = new int[4];
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;

    public final Pixmap pixmap;
    public final Texture texture;
    public final ZoomModel zoomModel = new ZoomModel();
    public final GridValues patchValues = new GridValues();
    public final GridValues contentValues = new GridValues();

    public NinePatchEditorModel(FileHandle imageFile) {
        if (imageFile.nameWithoutExtension().endsWith(".9")) {
            this.pixmap = parseDataFromNinePatch(imageFile);
        } else {
            this.pixmap = new Pixmap(imageFile);
        }
        texture = new Texture(pixmap);
    }

    @Override
    public void dispose() {
        texture.dispose();
        pixmap.dispose();
    }

    private Pixmap parseDataFromNinePatch(FileHandle imageFile) {
        Pixmap pixmap = new Pixmap(imageFile);

        if (pixmap.getWidth() < 2 || pixmap.getHeight() < 2) return pixmap;

        boolean patchLeftFound = false;
        for (int x = 1; x < pixmap.getWidth()-1; x++) {
            int color = pixmap.getPixel(x, 0);
            if (!patchLeftFound && color != 0) {
                patchValues.left.set(x - 1);
                patchLeftFound = true;
            } else if (patchLeftFound && color == 0) {
                patchValues.right.set(pixmap.getWidth() - x - 1);
                break;
            }
        }
        boolean patchTopFound = false;
        for (int y = 1; y < pixmap.getHeight()-1; y++) {
            int color = pixmap.getPixel(0, y);
            if (!patchTopFound && color != 0) {
                patchValues.top.set(y - 1);
                patchTopFound = true;
            } else if (patchTopFound && color == 0) {
                patchValues.bottom.set(pixmap.getHeight() - y - 1);
                break;
            }
        }
        boolean padLeftFound = false;
        for (int x = 1; x < pixmap.getWidth()-1; x++) {
            int color = pixmap.getPixel(x, pixmap.getHeight()-1);
            if (!padLeftFound && color != 0) {
                contentValues.left.set(x - 1);
                padLeftFound = true;
            } else if (padLeftFound && color == 0) {
                contentValues.right.set(pixmap.getWidth() - x - 1);
                break;
            }
        }
        boolean padTopFound = false;
        for (int y = 1; y < pixmap.getHeight()-1; y++) {
            int color = pixmap.getPixel(pixmap.getWidth()-1, y);
            if (!padTopFound && color != 0) {
                contentValues.top.set(y - 1);
                padTopFound = true;
            } else if (padTopFound && color == 0) {
                contentValues.bottom.set(pixmap.getHeight() - y - 1);
                break;
            }
        }

        // Cut off 1-pixel border markup
        Pixmap contentPixmap = new Pixmap(pixmap.getWidth() - 2, pixmap.getHeight() - 2, pixmap.getFormat());
        contentPixmap.drawPixmap(pixmap, 0, 0, 1, 1, contentPixmap.getWidth(), contentPixmap.getHeight());
        pixmap.dispose();
        return contentPixmap;
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
            out[RIGHT] = pixmap.getWidth();
        } else {
            out[LEFT] = values.left.get();
            out[RIGHT] = values.right.get();
        }
        if (values.bottom.get() == 0 && values.top.get() == 0) {
            out[TOP] = 0;
            out[BOTTOM] = pixmap.getHeight();
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

        patchValues.left.set(MathUtils.clamp(ninePatchProps.left, 0, pixmap.getWidth()));
        patchValues.right.set(MathUtils.clamp(ninePatchProps.right, 0, pixmap.getWidth() - patchValues.left.get()));
        patchValues.top.set(MathUtils.clamp(ninePatchProps.top, 0, pixmap.getHeight()));
        patchValues.bottom.set(MathUtils.clamp(ninePatchProps.bottom, 0, pixmap.getHeight() - patchValues.top.get()));

        if (patchValues.left.get() == pixmap.getWidth()) patchValues.left.set(0);
        if (patchValues.top.get() == pixmap.getHeight()) patchValues.top.set(0);
        if (patchValues.right.get() == pixmap.getWidth()) patchValues.right.set(0);
        if (patchValues.bottom.get() == pixmap.getHeight()) patchValues.bottom.set(0);

        contentValues.left.set(MathUtils.clamp(ninePatchProps.padLeft, 0, pixmap.getWidth()));
        contentValues.right.set(MathUtils.clamp(ninePatchProps.padRight, 0, pixmap.getWidth() - contentValues.left.get()));
        contentValues.top.set(MathUtils.clamp(ninePatchProps.padTop, 0, pixmap.getHeight()));
        contentValues.bottom.set(MathUtils.clamp(ninePatchProps.padBottom, 0, pixmap.getHeight() - contentValues.top.get()));

        if (contentValues.left.get() == pixmap.getWidth()) contentValues.left.set(0);
        if (contentValues.top.get() == pixmap.getHeight()) contentValues.top.set(0);
        if (contentValues.right.get() == pixmap.getWidth()) contentValues.right.set(0);
        if (contentValues.bottom.get() == pixmap.getHeight()) contentValues.bottom.set(0);
    }

    /** @return original image pixmap with 1-pixel border 9-patch markup. */
    public Pixmap prepareNinePatchPixmap() {
        Pixmap patchPixmap = new Pixmap(this.pixmap.getWidth() + 2, this.pixmap.getHeight() + 2, this.pixmap.getFormat());
        patchPixmap.drawPixmap(pixmap, 1, 1);

        patchPixmap.setColor(0x000000ff);
        if (patchValues.left.get() != 0 && patchValues.right.get() != 0) {
            for (int x = patchValues.left.get(); x < (pixmap.getWidth() - patchValues.right.get()); x++) {
                patchPixmap.drawPixel(x+1, 0);
            }
        }
        if (patchValues.top.get() != 0 && patchValues.bottom.get() != 0) {
            for (int y = patchValues.top.get(); y < (pixmap.getHeight() - patchValues.bottom.get()); y++) {
                patchPixmap.drawPixel(0, y+1);
            }
        }
        if (contentValues.left.get() != 0 && contentValues.right.get() != 0) {
            for (int x = contentValues.left.get(); x < (pixmap.getWidth() - contentValues.right.get()); x++) {
                patchPixmap.drawPixel(x+1, pixmap.getHeight()+1);
            }
        }
        if (contentValues.top.get() != 0 && contentValues.bottom.get() != 0) {
            for (int y = contentValues.top.get(); y < (pixmap.getHeight() - contentValues.bottom.get()); y++) {
                patchPixmap.drawPixel(pixmap.getWidth()+1, y+1);
            }
        }
        return patchPixmap;
    }
    //endregion
}
