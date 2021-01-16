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

    /**
     * Quick header validation - no crc16 checks.
     */
    public static boolean validateHeader(Buffer data) {
        return validateHeaderNative(data, data.capacity());
    }
    private static native boolean validateHeaderNative(Buffer data, int dataSize); /*
        return basisuWrapper::validateHeader((uint8_t*)data, dataSize);
    */

    /**
     * Validates the .basis file. This computes a crc16 over the entire file, so it's slow.
     */
    public static boolean validateChecksum(Buffer data, boolean fullValidation) {
        return validateChecksumNative(data, data.capacity(), fullValidation);
    }
    private static native boolean validateChecksumNative(Buffer data, int dataSize, boolean fullValidation); /*
        return basisuWrapper::validateChecksum((uint8_t*)data, dataSize, fullValidation);
    */

    /**
     * Decodes a single mipmap level from the .basis file to any of the supported output texture formats.
     * If the .basis file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public static ByteBuffer transcodeRgba32(Buffer data, int imageIndex, int levelIndex) {
        byte[] transcodedBytes = transcodeRgba32Native(data, data.capacity(), imageIndex, levelIndex);
        return wrapIntoBuffer(transcodedBytes);
    }
    private static native byte[] transcodeRgba32Native(Buffer dataRaw, int dataSize, int imageIndex, int levelIndex); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        std::vector<uint8_t> transcodedData;

        if (!basisuWrapper::transcodeRgba32(transcodedData, data, dataSize, imageIndex, levelIndex)) {
            basisuUtils::throwException(env, "Error during image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(transcodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)transcodedData.size(), (jbyte*)transcodedData.data());
        return byteArray;
    */

    public static ByteBuffer encode(Buffer rgbaData, int width, int height) {
        if (rgbaData.capacity() != width * height * 4) {
            throw new BasisuWrapperException("The input data size doesn't match to a an expected RGBA8888 width*height image size.");
        }

        byte[] encodedBytes = encodeNative(rgbaData, width, height);
        return wrapIntoBuffer(encodedBytes);
    }
    private static native byte[] encodeNative(Buffer dataRaw, int width, int height); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        std::vector<uint8_t> encodedData;

        if (!basisuWrapper::encode(encodedData, data, width, height)) {
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