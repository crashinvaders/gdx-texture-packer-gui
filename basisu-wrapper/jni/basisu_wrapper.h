#pragma once

#include <stdint.h>
#include <vector>

#define LOG_INFO "[BASISU_WRAPPER] INFO: "
#define LOG_ERROR "[BASISU_WRAPPER] ERROR: "

namespace basisuWrapper {

    uint32_t getTotalImages(uint8_t *data, uint32_t dataSize);

    uint32_t getTotalMipmapLevels(uint8_t *data, uint32_t dataSize, uint32_t imageIndex);

    uint32_t getImageWidth(uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t levelIndex);

    uint32_t getImageHeight(uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t levelIndex);

    bool validateHeader(uint8_t *data, uint32_t dataSize);

    bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation);

    bool transcodeRgba32(std::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                   uint32_t imageIndex, uint32_t levelIndex);

    // uastc - True to generate UASTC .basis file data, otherwise ETC1S.
    // flipY - Flip images across Y axis
    // compressionLevel - Compression level, from 0 to 5 (BASISU_MAX_COMPRESSION_LEVEL, higher is slower)
    // perceptual - Use perceptual sRGB colorspace metrics (for normal maps, etc.)
    // forceAlpha - Always put alpha slices in the output basis file, even when the input doesn't have alpha
    // mipEnabled, mipScale - Mipmap params
    // qualityLevel - Controls the quality level. It ranges from [1,255]
    // userdata0, userdata1 - These fields go directly into the Basis file header.
    bool encode(std::vector<uint8_t> &out, uint8_t *rgbaData, uint32_t width, uint32_t height,
                bool uastc, bool flipY, int compressionLevel, bool perceptual, bool forceAlpha,
                bool mipEnabled, float mipScale, int qualityLevel,
                uint32_t userdata0, uint32_t userdata1);

} // namespace basisuWrapper