package com.crashinvaders.common.basisu;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.crashinvaders.basisu.BasisuWrapper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * A simple wrapper to load and work with the Basis texture (file) data.
 * Must be disposed when it's no longer needed.
 */
public class BasisuData implements Disposable {

    private final ByteBuffer encodedData;

    /**
     * @param file the file to load the Basis texture data from
     */
    public BasisuData(FileHandle file) {
        this(BasisuUtils.readFileIntoBuffer(file));
    }

    /**
     * @param encodedData the raw Basis texture data (as it's loaded from the file)
     */
    public BasisuData(ByteBuffer encodedData) {
        BasisuNativeLibLoader.loadIfNeeded();

        this.encodedData = encodedData;

        if (!BasisuWrapper.basisValidateHeader(encodedData)) {
            throw new BasisuGdxException("Cannot validate header of the basis universal data.");
        }
    }

    @Override
    public void dispose() {
        if (BufferUtils.isUnsafeByteBuffer(encodedData)) {
            BufferUtils.disposeUnsafeByteBuffer(encodedData);
        }
    }

    public int getTotalImages() {
        return BasisuWrapper.basisGetTotalImages(encodedData);
    }

    public int getTotalMipmapLevels(int imageIndex) {
        return BasisuWrapper.basisGetTotalMipmapLevels(encodedData, imageIndex);
    }

    public int getImageWidth(int imageIndex, int mipmapLevel) {
        return BasisuWrapper.basisGetImageWidth(encodedData, imageIndex, mipmapLevel);
    }

    public int getImageHeight(int imageIndex, int mipmapLevel) {
        return BasisuWrapper.basisGetImageHeight(encodedData, imageIndex, mipmapLevel);
    }

    /**
     * @return the raw Basis texture data (as it's loaded from the file)
     */
    public ByteBuffer getEncodedData() {
        return encodedData;
    }

    /**
     * Transcodes the Basis image to the RGBA32 texture format.
     * @param imageIndex the image index in the Basis file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate the Basis file).
     * @return the transcoded texture bytes.
     * Can be used for further processing or supplied directly to the OpenGL as an uncompressed texture.
     */
    public ByteBuffer transcodeRgba32(int imageIndex, int mipmapLevel) {
        return BasisuWrapper.basisTranscodeRgba32(encodedData, imageIndex, mipmapLevel);
    }
}