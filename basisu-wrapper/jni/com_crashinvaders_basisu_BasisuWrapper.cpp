#include <com_crashinvaders_basisu_BasisuWrapper.h>

//@line:9


    #include <cstring>
    #include <vector>

    #include "basisu_wrapper.h"
    #include "basisu_native_utils.h"

    #define LOG_TAG "BasisuWrapper.java"
    #define BASE_PACKAGE com/crashinvaders/basisu

    static inline jint wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getTotalImagesNative
(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, unsigned char* data) {

//@line:27

        return basisuWrapper::getTotalImages((uint8_t*)data, dataSize);
    
}

JNIEXPORT jint JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_getTotalImagesNative(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jint JNI_returnValue = wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getTotalImagesNative(env, clazz, obj_data, dataSize, data);


	return JNI_returnValue;
}

static inline jint wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getTotalMipmapLevelsNative
(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jint imageIndex, unsigned char* data) {

//@line:34

        return basisuWrapper::getTotalMipmapLevels((uint8_t*)data, dataSize, imageIndex);
    
}

JNIEXPORT jint JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_getTotalMipmapLevelsNative(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jint imageIndex) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jint JNI_returnValue = wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getTotalMipmapLevelsNative(env, clazz, obj_data, dataSize, imageIndex, data);


	return JNI_returnValue;
}

static inline jint wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getImageWidthNative
(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jint imageIndex, jint levelIndex, unsigned char* data) {

//@line:41

        return basisuWrapper::getImageWidth((uint8_t*)data, dataSize, imageIndex, levelIndex);
    
}

JNIEXPORT jint JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_getImageWidthNative(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jint imageIndex, jint levelIndex) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jint JNI_returnValue = wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getImageWidthNative(env, clazz, obj_data, dataSize, imageIndex, levelIndex, data);


	return JNI_returnValue;
}

static inline jint wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getImageHeightNative
(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jint imageIndex, jint levelIndex, unsigned char* data) {

//@line:48

        return basisuWrapper::getImageHeight((uint8_t*)data, dataSize, imageIndex, levelIndex);
    
}

JNIEXPORT jint JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_getImageHeightNative(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jint imageIndex, jint levelIndex) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jint JNI_returnValue = wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_getImageHeightNative(env, clazz, obj_data, dataSize, imageIndex, levelIndex, data);


	return JNI_returnValue;
}

static inline jboolean wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_validateHeaderNative
(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, unsigned char* data) {

//@line:58

        return basisuWrapper::validateHeader((uint8_t*)data, dataSize);
    
}

JNIEXPORT jboolean JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_validateHeaderNative(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jboolean JNI_returnValue = wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_validateHeaderNative(env, clazz, obj_data, dataSize, data);


	return JNI_returnValue;
}

static inline jboolean wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_validateChecksumNative
(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jboolean fullValidation, unsigned char* data) {

//@line:68

        return basisuWrapper::validateChecksum((uint8_t*)data, dataSize, fullValidation);
    
}

JNIEXPORT jboolean JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_validateChecksumNative(JNIEnv* env, jclass clazz, jobject obj_data, jint dataSize, jboolean fullValidation) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jboolean JNI_returnValue = wrapped_Java_com_crashinvaders_basisu_BasisuWrapper_validateChecksumNative(env, clazz, obj_data, dataSize, fullValidation, data);


	return JNI_returnValue;
}

JNIEXPORT jbyteArray JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_transcodeRgba32Native(JNIEnv* env, jclass clazz, jobject dataRaw, jint dataSize, jint imageIndex, jint levelIndex) {

//@line:82

        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        std::vector<uint8_t> transcodedData;

        if (!basisuWrapper::transcodeRgba32(transcodedData, data, dataSize, imageIndex, levelIndex)) {
            basisuUtils::throwException(env, "Error during image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(transcodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)transcodedData.size(), (jbyte*)transcodedData.data());
        return byteArray;
    
}

JNIEXPORT jbyteArray JNICALL Java_com_crashinvaders_basisu_BasisuWrapper_encodeNative(JNIEnv* env, jclass clazz, jobject dataRaw, jint width, jint height) {

//@line:104

        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        std::vector<uint8_t> encodedData;

        if (!basisuWrapper::encode(encodedData, data, width, height)) {
            basisuUtils::throwException(env, "Error during image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(encodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)encodedData.size(), (jbyte*)encodedData.data());
        return byteArray;
    
}

