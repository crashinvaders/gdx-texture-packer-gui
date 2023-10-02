#include <string>
#include <iostream>

#include "file_utils.h"
#include "basisu_transcoder.h"
#include "basisu_wrapper.h"
#include "basisu_native_utils.h"

#define LOG_TAG "jni-test"

int main(int, char**) {
    basisu::vector<uint8_t> basisData = fileUtils::readFile("../test-resources/level_temple0.basis");
    if (basisData.size() == 0) {
        basisuUtils::logError(LOG_TAG, "An error occurred during reading the file.");
        return 1;
    }

    basisuUtils::logInfo(LOG_TAG, (std::string("File was successfully read. Size: ") += basisData.size()).c_str());
    
    if (!basisuWrapper::validateHeader(basisData.data(), basisData.size())) {
        basisuUtils::logError(LOG_TAG, "File is not a valid basis universal image!");
        return 2;
    }

    basisuUtils::logInfo(LOG_TAG, "Begin transcoding test...");
    basisu::vector<uint8_t> rgba;
    if (!basisuWrapper::transcodeRgba32(rgba, basisData.data(), basisData.size(), 0, 0)) {
        basisuUtils::logError(LOG_TAG, "Error during image transcoding!");
        return 3;
    }
    basisuUtils::logInfo(LOG_TAG, "Transcoding test finished successfully!");

//    basisuUtils::logInfo(LOG_TAG, "Begin encoding test...");
//    // TODO Implement it.
//    basisuUtils::logInfo(LOG_TAG, "Encoding test finished successfully!");

    basisuUtils::throwException(nullptr, "TEST EXCEPTION!");
    
    return 0;
}