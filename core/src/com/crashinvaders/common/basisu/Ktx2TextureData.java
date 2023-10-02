package com.crashinvaders.common.basisu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.Buffer;

/**
 * Provides support for KTX2 texture data format for {@link com.badlogic.gdx.graphics.Texture}.
 * This implementation is based on {@link com.badlogic.gdx.graphics.glutils.ETC1TextureData}.
 */
public class Ktx2TextureData implements TextureData {
    private static final String TAG = Ktx2TextureData.class.getSimpleName();

    private final FileHandle file;  // May be null.
    private final int layerIndex;
    private final int mipmapLevel;

    private Ktx2Data ktx2Data;

    private Buffer transcodedData = null;

    private int width = 0;
    private int height = 0;
    private boolean isPrepared = false;

    /**
     * @param file the file to load the KTX2 texture data from
     */
    public Ktx2TextureData(FileHandle file) {
        this(file, 0, 0);
    }

    /**
     * @param file the file to load the KTX2 texture data from
     * @param layerIndex the layer index in the KTX2 file
     */
    public Ktx2TextureData(FileHandle file, int layerIndex) {
        this(file, layerIndex, 0);
    }

    /**
     * @param file the file to load the KTX2 texture data from
     * @param layerIndex the layer index in the KTX2 file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled in the Basis encoder when you generate a KTX2 file).
     */
    public Ktx2TextureData(FileHandle file, int layerIndex, int mipmapLevel) {
        this.file = file;
        this.layerIndex = layerIndex;
        this.mipmapLevel = mipmapLevel;

        this.ktx2Data = null;
    }

    /**
     * @param ktx2Data the KTX2 texture data to transcode the texture from
     */
    public Ktx2TextureData(Ktx2Data ktx2Data) {
        this(ktx2Data, 0);
    }

    /**
     * @param ktx2Data the KTX2 texture data to transcode the texture from
     * @param layerIndex the layer index in the KTX2 file
     */
    public Ktx2TextureData(Ktx2Data ktx2Data, int layerIndex) {
        this(ktx2Data, layerIndex, 0);
    }

    /**
     * @param ktx2Data the KTX2 texture data to transcode the texture from
     * @param layerIndex the layer index in the KTX2 file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled in the Basis encoder when you generate a KTX2 file).
     */
    public Ktx2TextureData(Ktx2Data ktx2Data, int layerIndex, int mipmapLevel) {
        this.file = null;
        this.layerIndex = layerIndex;
        this.mipmapLevel = mipmapLevel;

        this.ktx2Data = ktx2Data;
    }

    @Override
    public TextureDataType getType() {
        return TextureDataType.Custom;
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void prepare() {
        if (isPrepared) throw new GdxRuntimeException("Already prepared");
        if (file == null && ktx2Data == null) throw new GdxRuntimeException("Can only load once from Ktx2Data");
        if (file != null) {
            ktx2Data = new Ktx2Data(file);
        }

        int totalLayers = ktx2Data.getTotalLayers();
        if (totalLayers > 0 && (layerIndex < 0 || layerIndex >= totalLayers)) {
            throw new BasisuGdxException("layerIndex " + layerIndex + " exceeds " +
                    "the total number of layers (" + totalLayers + ") in the KTX2 file.");
        }

        int mipmapLevels = ktx2Data.getTotalMipmapLevels();
        if (mipmapLevel < 0 || mipmapLevel >= mipmapLevels) {
            throw new BasisuGdxException("mipmapLevel " + mipmapLevel + " exceeds " +
                    "the total number of mipmap levels (" + mipmapLevels + ") in the KTX2 file.");
        }

        width = ktx2Data.getImageWidth();
        height = ktx2Data.getImageHeight();

        this.transcodedData = ktx2Data.transcodeRgba32(layerIndex, mipmapLevel);

        Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoded RGBA32 texture size: " + MathUtils.round(this.transcodedData.capacity() / 1024.0f) + "kB");

        ktx2Data.dispose();
        ktx2Data = null;
        isPrepared = true;
    }

    @Override
    public void consumeCustomData(int target) {
        if (!isPrepared) throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");

//        final int glInternalFormatCode = GL20.GL_RGBA;

//        if (transcodeFormat.isCompressedFormat()) {
//            BasisuGdxGl.glCompressedTexImage2D(target, 0, glInternalFormatCode,
//                    width, height, 0,
//                    transcodedData.capacity(), transcodedData);
//        } else {
//        int textureType = BasisuGdxUtils.toUncompressedGlTextureType(transcodeFormat);
        Gdx.gl.glTexImage2D(target, 0, GL20.GL_RGBA,
                width, height, 0,
                GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE,
                transcodedData);
//        }

        // Cleanup.
        transcodedData = null;
        isPrepared = false;
    }

    @Override
    public Pixmap consumePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap.");
    }

    @Override
    public boolean disposePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap.");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Pixmap.Format getFormat() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    @Override
    public boolean useMipMaps() {
        return false;
    }

    @Override
    public boolean isManaged() {
        return true;
    }
}