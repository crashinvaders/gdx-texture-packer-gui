#pragma once

#include <stdint.h>
#include <climits>

#include "basisu_containers.h"

#define LOG_INFO "[BASISU_WRAPPER] INFO: "
#define LOG_ERROR "[BASISU_WRAPPER] ERROR: "

namespace basisuWrapper {

    namespace basis {

        uint32_t getTotalImages(uint8_t *data, uint32_t dataSize);

        uint32_t getTotalMipmapLevels(uint8_t *data, uint32_t dataSize, uint32_t imageIndex);

        uint32_t getImageWidth(uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t levelIndex);

        uint32_t getImageHeight(uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t levelIndex);

        bool validateHeader(uint8_t *data, uint32_t dataSize);

        bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation);

        bool transcodeRgba32(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                             uint32_t imageIndex, uint32_t levelIndex);

    } // namespace basis

    namespace ktx2 {

        uint32_t getTotalLayers(uint8_t *data, uint32_t dataSize);

        uint32_t getTotalMipmapLevels(uint8_t *data, uint32_t dataSize);

        uint32_t getImageWidth(uint8_t *data, uint32_t dataSize);

        uint32_t getImageHeight(uint8_t *data, uint32_t dataSize);

        bool transcodeRgba32(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                             uint32_t layerIndex, uint32_t levelIndex);

    } // namespace ktx

    // uastc - True to generate UASTC .basis file data, otherwise ETC1S.
    // ktx2 - Whether to pack the Basis texture into KTX2 container and apply extra ZSTD compression.
    // flipY - Flip images across Y axis
    // compressionLevel - Compression level, from 0 to 5 (BASISU_MAX_COMPRESSION_LEVEL, higher is slower)
    // perceptual - Use perceptual sRGB colorspace metrics (for normal maps, etc.)
    // forceAlpha - Always put alpha slices in the output basis file, even when the input doesn't have alpha
    // mipEnabled, mipScale - Mipmap params
    // qualityLevel - Controls the quality level. It ranges from [1,255]
    // userdata0, userdata1 - These fields go directly into the Basis file header.
    bool encode(basisu::vector<uint8_t> &out, uint8_t *rgbaData, uint32_t width, uint32_t height,
                bool uastc, bool ktx2, bool flipY, int compressionLevel, bool perceptual, bool forceAlpha,
                bool mipEnabled, float mipScale, int qualityLevel,
                uint32_t userdata0, uint32_t userdata1);

} // namespace basisuWrapper