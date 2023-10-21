package com.crashinvaders.basisu;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BasisuWrapper {

    /*JNI

    #include <cstring>

    #include "basisu_wrapper.h"
    #include "basisu_native_utils.h"

    #define LOG_TAG "BasisuWrapper.java"
    #define BASE_PACKAGE com/crashinvaders/basisu

    jobject wrapIntoBuffer(JNIEnv* env, basisu::vector<uint8_t> imageData) {
        uint32_t imageDataSize = imageData.size_in_bytes();
        uint8_t* nativeBuffer = (uint8_t*)malloc(imageDataSize);
        memcpy(nativeBuffer, imageData.data(), imageDataSize);
        return env->NewDirectByteBuffer(nativeBuffer, imageDataSize);
    }

    */

    public static native int basisGetTotalImages(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::getTotalImages(data, dataSize);
    */

    public static native int basisGetTotalMipmapLevels(Buffer dataBuffer, int imageIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::getTotalMipmapLevels(data, dataSize, imageIndex);
    */

    public static native int basisGetImageWidth(Buffer dataBuffer, int imageIndex, int levelIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::getImageWidth(data, dataSize, imageIndex, levelIndex);
    */

    public static native int basisGetImageHeight(Buffer dataBuffer, int imageIndex, int levelIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::getImageHeight(data, dataSize, imageIndex, levelIndex);
    */

    /**
     * Quick header validation - no crc16 checks.
     */
    public static native boolean basisValidateHeader(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::validateHeader((uint8_t*)data, dataSize);
    */

    /**
     * Validates the .basis file. This computes a crc16 over the entire file, so it's slow.
     */
    public static native boolean basisValidateChecksum(Buffer dataBuffer, boolean fullValidation); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::validateChecksum(data, dataSize, fullValidation);
    */

    /**
     * Decodes a single mipmap level from the .basis file to any of the supported output texture formats.
     * If the .basis file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public static native ByteBuffer basisTranscodeRgba32(Buffer dataBuffer, int imageIndex, int levelIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::basis::transcodeRgba32(transcodedData, data, dataSize, imageIndex, levelIndex)) {
            basisuUtils::throwException(env, "Error during Basis image transcoding.");
            return 0;
        };

        return wrapIntoBuffer(env, transcodedData);
    */

    public static native int ktx2GetTotalLayers(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::ktx2::getTotalLayers((uint8_t*)data, dataSize);
    */

    public static native int ktx2GetTotalMipmapLevels(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::ktx2::getTotalMipmapLevels((uint8_t*)data, dataSize);
    */

    public static native int ktx2GetImageWidth(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::ktx2::getImageWidth((uint8_t*)data, dataSize);
    */

    public static native int ktx2GetImageHeight(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::ktx2::getImageHeight((uint8_t*)data, dataSize);
    */

    /**
     * Decodes a single mipmap level from the .ktx2 file to any of the supported output texture formats.
     * If the .ktx2 file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public static native ByteBuffer ktx2TranscodeRgba32(Buffer dataBuffer, int layerIndex, int levelIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::ktx2::transcodeRgba32(transcodedData, data, dataSize, layerIndex, levelIndex)) {
            basisuUtils::throwException(env, "Error during KTX2 image transcoding.");
            return 0;
        };

        return wrapIntoBuffer(env, transcodedData);
    */

    /**
     *
     * @param uastc True to generate UASTC .basis file data, otherwise ETC1S
     * @param ktx2 Whether to pack the Basis texture into KTX2 container and apply ZSTD super-compression.
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
    public static native ByteBuffer encode(Buffer dataBuffer, int width, int height,
                                                  boolean uastc, boolean ktx2, boolean flipY, int compressionLevel,
                                                  boolean perceptual, boolean forceAlpha,
                                                  boolean mipEnabled, float mipScale, int qualityLevel,
                                                  int userdata0, int userdata1); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        basisu::vector<uint8_t> encodedData;

        if (dataSize != (uint32_t)(width * height * 4)) {
            basisuUtils::throwException(env, "The input data buffer size doesn't match to an expected RGBA8888 width*height image size.");
            return NULL;
        }

        if (!basisuWrapper::encode(encodedData, data, width, height,
                                   uastc, ktx2, flipY, compressionLevel, perceptual, forceAlpha,
                                   mipEnabled, mipScale, qualityLevel, (uint32_t)userdata0, (uint32_t)userdata1)) {
            basisuUtils::throwException(env, "Error during image encoding.");
            return 0;
        };

        return wrapIntoBuffer(env, encodedData);
    */

    /**
     * A {@link ByteBuffer} returned from any of {@link BasisuWrapper}
     * methods must be disposed using this method only.
     */
    public static native void disposeNativeBuffer(ByteBuffer dataBuffer); /*
        free(dataBuffer);
    */
}