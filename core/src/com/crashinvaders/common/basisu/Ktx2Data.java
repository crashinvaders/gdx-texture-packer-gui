package com.crashinvaders.common.basisu;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.StreamUtils;
import com.crashinvaders.basisu.BasisuWrapper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * A simple wrapper to load and work with the KTX2 texture (file) data.
 * Must be disposed when it's no longer needed.
 */
public class Ktx2Data implements Disposable {

    private final ByteBuffer encodedData;

    /**
     * @param file the file to load the KTX2 texture data from
     */
    public Ktx2Data(FileHandle file) {
        this(BasisuUtils.readFileIntoBuffer(file));
    }

    /**
     * @param encodedData the raw KTX2 texture data (as it's loaded from the file)
     */
    public Ktx2Data(ByteBuffer encodedData) {
        BasisuNativeLibLoader.loadIfNeeded();

        this.encodedData = encodedData;

        // KTX2 codec doesn't provide a simple validation method.
        // We assume we're good if we can parse the header and read "anything" out of it.
        if (BasisuWrapper.ktx2GetTotalLayers(encodedData) < 0) {
            throw new BasisuGdxException("Cannot validate header of KTX2 data.");
        }
    }

    @Override
    public void dispose() {
        if (BufferUtils.isUnsafeByteBuffer(encodedData)) {
            BufferUtils.disposeUnsafeByteBuffer(encodedData);
        }
    }

    public int getTotalLayers() {
        return BasisuWrapper.ktx2GetTotalLayers(encodedData);
    }

    public int getTotalMipmapLevels() {
        return BasisuWrapper.ktx2GetTotalMipmapLevels(encodedData);
    }

    public int getImageWidth() {
        return BasisuWrapper.ktx2GetImageWidth(encodedData);
    }

    public int getImageHeight() {
        return BasisuWrapper.ktx2GetImageHeight(encodedData);
    }

    /**
     * @return the raw KTX2 texture data (as it's loaded from the file)
     */
    public ByteBuffer getEncodedData() {
        return encodedData;
    }

    /**
     * Transcodes the KTX2 image to the RGBA32 texture format.
     * @param layerIndex the image layer in the KTX2 file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate a KTX2 file).
     * @return the transcoded texture bytes.
     * Can be used for further processing or supplied directly to the OpenGL as an uncompressed texture.
     */
    public ByteBuffer transcodeRgba32(int layerIndex, int mipmapLevel) {
        return BasisuWrapper.ktx2TranscodeRgba32(encodedData, layerIndex, mipmapLevel);
    }
}