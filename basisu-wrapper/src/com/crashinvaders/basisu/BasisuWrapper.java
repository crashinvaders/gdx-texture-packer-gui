package com.crashinvaders.basisu;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BasisuWrapper {

    /*JNI

    #include <cstring>
    #include <vector>

    #include "basisu_wrapper.h"
    #include "basisu_native_utils.h"

    #define LOG_TAG "BasisuWrapper.java"
    #define BASE_PACKAGE com/crashinvaders/basisu

    */

    public static int basisGetTotalImages(Buffer data) {
        return basisGetTotalImagesNative(data, data.capacity());
    }
    private static native int basisGetTotalImagesNative(Buffer data, int dataSize); /*
        return basisuWrapper::basis::getTotalImages((uint8_t*)data, dataSize);
    */

    public static int basisGetTotalMipmapLevels(Buffer data, int imageIndex) {
        return basisGetTotalMipmapLevelsNative(data, data.capacity(), imageIndex);
    }
    private static native int basisGetTotalMipmapLevelsNative(Buffer data, int dataSize, int imageIndex); /*
        return basisuWrapper::basis::getTotalMipmapLevels((uint8_t*)data, dataSize, imageIndex);
    */

    public static int basisGetImageWidth(Buffer data, int imageIndex, int levelIndex) {
        return basisGetImageWidthNative(data, data.capacity(), imageIndex, levelIndex);
    }
    private static native int basisGetImageWidthNative(Buffer data, int dataSize, int imageIndex, int levelIndex); /*
        return basisuWrapper::basis::getImageWidth((uint8_t*)data, dataSize, imageIndex, levelIndex);
    */

    public static int basisGetImageHeight(Buffer data, int imageIndex, int levelIndex) {
        return basisGetImageHeightNative(data, data.capacity(), imageIndex, levelIndex);
    }
    private static native int basisGetImageHeightNative(Buffer data, int dataSize, int imageIndex, int levelIndex); /*
        return basisuWrapper::basis::getImageHeight((uint8_t*)data, dataSize, imageIndex, levelIndex);
    */

    /**
     * Quick header validation - no crc16 checks.
     */
    public static boolean basisValidateHeader(Buffer data) {
        return basisValidateHeaderNative(data, data.capacity());
    }
    private static native boolean basisValidateHeaderNative(Buffer data, int dataSize); /*
        return basisuWrapper::basis::validateHeader((uint8_t*)data, dataSize);
    */

    /**
     * Validates the .basis file. This computes a crc16 over the entire file, so it's slow.
     */
    public static boolean basisValidateChecksum(Buffer data, boolean fullValidation) {
        return basisValidateChecksumNative(data, data.capacity(), fullValidation);
    }
    private static native boolean basisValidateChecksumNative(Buffer data, int dataSize, boolean fullValidation); /*
        return basisuWrapper::basis::validateChecksum((uint8_t*)data, dataSize, fullValidation);
    */

    /**
     * Decodes a single mipmap level from the .basis file to any of the supported output texture formats.
     * If the .basis file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public static ByteBuffer basisTranscodeRgba32(Buffer data, int imageIndex, int levelIndex) {
        byte[] transcodedBytes = basisTranscodeRgba32Native(data, data.capacity(), imageIndex, levelIndex);
        return wrapIntoBuffer(transcodedBytes);
    }
    private static native byte[] basisTranscodeRgba32Native(Buffer dataRaw, int dataSize, int imageIndex, int levelIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::basis::transcodeRgba32(transcodedData, data, dataSize, imageIndex, levelIndex)) {
            basisuUtils::throwException(env, "Error during Basis image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(transcodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)transcodedData.size(), (jbyte*)transcodedData.data());
        return byteArray;
    */

    /**
     *
     * @param uastc True to generate UASTC .basis file data, otherwise ETC1S
     * @param ktx2 Whether to apply ZSTD and pack the Basis texture into KTX2 container.
     * @param flipY Flip images across Y axis
     * @param compressionLevel Compression level, from 0 to 5 (BASISU_MAX_COMPRESSION_LEVEL, higher is slower)
     * @param perceptual Use perceptual sRGB colorspace metrics (for normal maps, etc.)
     * @param forceAlpha Always put alpha slices in the output basis file, even when the input doesn't have alpha
     * @param mipEnabled If mipmaps should be generated
     * @param mipScale The mipmap scale step
     * @param qualityLevel Controls the quality level. It ranges from [1,255]
     * @param userdata0 Goes directly into the Basis file header
     * @param userdata1 Goes directly into the Basis file header
     */
    public static ByteBuffer encode(Buffer rgbaData, int width, int height,
                                    boolean uastc, boolean ktx2, boolean flipY, int compressionLevel,
                                    boolean perceptual, boolean forceAlpha,
                                    boolean mipEnabled, float mipScale, int qualityLevel,
                                    int userdata0, int userdata1) {
        if (rgbaData.capacity() != width * height * 4) {
            throw new BasisuWrapperException("The input data size doesn't match to a an expected RGBA8888 width*height image size.");
        }

        byte[] encodedBytes = encodeNative(rgbaData, width, height,
                uastc, ktx2, flipY, compressionLevel, perceptual, forceAlpha,
                mipEnabled, mipScale, qualityLevel, userdata0, userdata1);
        return wrapIntoBuffer(encodedBytes);
    }
    private static native byte[] encodeNative(Buffer dataRaw, int width, int height,
                                              boolean uastc, boolean ktx2, boolean flipY, int compressionLevel, boolean perceptual, boolean forceAlpha,
                                              boolean mipEnabled, float mipScale, int qualityLevel,
                                              int userdata0, int userdata1); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        basisu::vector<uint8_t> encodedData;

        if (!basisuWrapper::encode(encodedData, data, width, height,
                                   uastc, ktx2, flipY, compressionLevel, perceptual, forceAlpha,
                                   mipEnabled, mipScale, qualityLevel, (uint32_t)userdata0, (uint32_t)userdata1)) {
            basisuUtils::throwException(env, "Error during image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(encodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)encodedData.size(), (jbyte*)encodedData.data());
        return byteArray;
    */

    private static ByteBuffer wrapIntoBuffer(byte[] bytes) {
        // Seems like allocating and filling a DirectByteBuffer
        // is faster on Java side rather than on the native one
        // (Even with receiving extra Java primitive array from the native code).
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(bytes);
        ((Buffer)buffer).position(0);
        ((Buffer)buffer).limit(buffer.capacity());
        return buffer;
    }
}