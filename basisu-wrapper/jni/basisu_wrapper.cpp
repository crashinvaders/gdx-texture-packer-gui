#include <cstdio>
#include <cstring>

#include "basisu_wrapper.h"
#include "basisu_native_utils.h"

#include "basisu_transcoder.h"
#include "basisu_enc.h"
#include "basisu_comp.h"
#include "basisu_frontend.h"

using namespace basist;
using namespace basisu;

namespace basisuWrapper {

#define LOG_TAG "basisu_wrapper.cpp"

    void initBasisu() {
        static bool basisuInitialized;
        if (basisuInitialized)
            return;

        basisuUtils::logInfo(LOG_TAG, (std::string("Basis Universal ") + BASISD_VERSION_STRING).c_str());
        basisuUtils::logInfo(LOG_TAG, "Initializing global basisu parser.");

        basisuInitialized = true;

        basisu_transcoder_init();
        basisu_encoder_init();
    }

    namespace basis {

        uint32_t getTotalImages(uint8_t *data, uint32_t dataSize) {
            initBasisu();
            basisu_transcoder transcoder = {};
            return transcoder.get_total_images(data, dataSize);
        }

        uint32_t getTotalMipmapLevels(uint8_t *data, uint32_t dataSize, uint32_t imageIndex) {
            initBasisu();
            basisu_transcoder transcoder = {};
            return transcoder.get_total_image_levels(data, dataSize, imageIndex);
        }

        uint32_t getImageWidth(uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t levelIndex) {
            initBasisu();
            basisu_transcoder transcoder = {};
            uint32_t width;
            uint32_t height;
            uint32_t totalBlocks;
            if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, width, height, totalBlocks)) {
                basisuUtils::logError(LOG_TAG, "Failed to retrieve image info.");
                return -1;
            }
            return width;
        }

        uint32_t getImageHeight(uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t levelIndex) {
            initBasisu();
            basisu_transcoder transcoder = {};
            uint32_t width;
            uint32_t height;
            uint32_t totalBlocks;
            if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, width, height, totalBlocks)) {
                basisuUtils::logError(LOG_TAG, "Failed to retrieve image info.");
                return -1;
            }
            return height;
        }

        bool validateHeader(uint8_t *data, uint32_t dataSize) {
            initBasisu();
            basisu_transcoder transcoder = {};
            return transcoder.validate_header(data, dataSize);
        }

        bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation) {
            initBasisu();
            basisu_transcoder transcoder = {};
            return transcoder.validate_file_checksums(data, dataSize, fullValidation);
        }

        // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
        bool transcodeRgba32(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                             uint32_t imageIndex, uint32_t levelIndex) {
            initBasisu();
            basisu_transcoder transcoder = {};

            uint32_t origWidth, origHeight, totalBlocks;
            if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, origWidth, origHeight, totalBlocks)) {
                basisuUtils::logError(LOG_TAG, "Failed to retrieve level description from Basis data.");
                return false;
            }

            // Transcode to RGBA8888 data.
            const transcoder_texture_format format = transcoder_texture_format::cTFRGBA32;

            const uint32_t flags = 0;

            bool status;

            if (!transcoder.start_transcoding(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to init transcoding for Basis data.");
                return false;
            }

            const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
            const uint32_t bytesPerLine = origWidth * bytesPerPixel;
            const uint32_t bytesTotal = bytesPerLine * origHeight;

            out.resize(bytesTotal);

            status = transcoder.transcode_image_level(
                data, dataSize, 0, levelIndex,
                out.data(), origWidth * origHeight,
                format,
                flags,
                origWidth,
                nullptr,
                origHeight);

            transcoder.stop_transcoding();

            return status;
        }

    } // namespace basis

    namespace ktx2 {

        uint32_t getTotalLayers(uint8_t *data, uint32_t dataSize) {
            initBasisu();

            ktx2_transcoder transcoder = {};
            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return -1;
            }
            uint32_t layers = transcoder.get_layers();
            transcoder.clear();
            return layers;
        }

        uint32_t getTotalMipmapLevels(uint8_t *data, uint32_t dataSize) {
            initBasisu();

            ktx2_transcoder transcoder = {};
            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return -1;
            }
            uint32_t levels = transcoder.get_levels();
            transcoder.clear();
            return levels;
        }

        uint32_t getImageWidth(uint8_t *data, uint32_t dataSize) {
            initBasisu();

            ktx2_transcoder transcoder = {};
            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return -1;
            }
            uint32_t width = transcoder.get_width();
            transcoder.clear();
            return width;
        }

        uint32_t getImageHeight(uint8_t *data, uint32_t dataSize) {
            initBasisu();

            ktx2_transcoder transcoder = {};
            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return -1;
            }
            uint32_t height = transcoder.get_height();
            transcoder.clear();
            return height;
        }

        bool transcodeRgba32(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                             uint32_t layerIndex, uint32_t levelIndex) {
            initBasisu();
            ktx2_transcoder transcoder = {};

            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return false;
            }

            uint32_t origWidth = transcoder.get_width();
            uint32_t origHeight = transcoder.get_height();

            // Transcode to RGBA8888 data.
            const transcoder_texture_format format = transcoder_texture_format::cTFRGBA32;

            const uint32_t decode_flags = 0;

            bool status;


            if (!transcoder.start_transcoding()) {
                basisuUtils::logError(LOG_TAG, "Failed to init transcoding for KTX2 data.");
                return false;
            }

            const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
            const uint32_t bytesPerLine = origWidth * bytesPerPixel;
            const uint32_t bytesTotal = bytesPerLine * origHeight;

            out.resize(bytesTotal);

            status = transcoder.transcode_image_level(
                levelIndex, layerIndex, 0,
                out.data(), origWidth * origHeight,
                format,
                decode_flags);

            transcoder.clear();

            return status;
        }

    } // namespace ktx2

    bool encode(basisu::vector<uint8_t> &out, uint8_t *rgbaData, uint32_t width, uint32_t height,
                    bool uastc, bool ktx2, bool flipY, int compressionLevel, bool perceptual, bool forceAlpha,
                    bool mipEnabled, float mipScale, int qualityLevel,
                    uint32_t userdata0, uint32_t userdata1) {

        // Some sanity checks...
        if (compressionLevel < 0 || compressionLevel > (int)basisu::BASISU_MAX_COMPRESSION_LEVEL) {
            basisuUtils::logError(LOG_TAG, "The compression level value should fall in the range 0..5");
            return false;
        }
        if (qualityLevel < (int)basisu::BASISU_QUALITY_MIN || qualityLevel > (int)basisu::BASISU_QUALITY_MAX) {
            basisuUtils::logError(LOG_TAG, "The quality level value should fall in the range 1..255");
            return false;
        }

        initBasisu();

        image imageEntry(rgbaData, width, height, 4);

        basis_compressor_params params;
        params.m_source_images.push_back(imageEntry);
        params.m_multithreading = false;

        params.m_uastc = uastc;
        params.m_y_flip = flipY;
        params.m_compression_level = compressionLevel;
        params.m_perceptual = perceptual;
        params.m_force_alpha = forceAlpha;
        params.m_mip_gen = mipEnabled;
        params.m_mip_scale = mipScale;
        params.m_quality_level = qualityLevel;
        params.m_userdata0 = userdata0;
        params.m_userdata1 = userdata1;

        // KTX2 related params.
        params.m_create_ktx2_file = ktx2;
        params.m_ktx2_uastc_supercompression = basist::KTX2_SS_ZSTANDARD; // KTX2_SS_BASISLZ
//        params.m_ktx2_zstd_supercompression_level = 6;

        params.m_status_output = true;
#ifdef DEBUG
        params.m_debug = true;
#endif

        job_pool jobPool(1);
        params.m_pJob_pool = &jobPool;

        basis_compressor compressor;

        if (!compressor.init(params)) {
            basisuUtils::logError(LOG_TAG, "basis_compressor::init() failed!");
            return false;
        }

        basisuUtils::logInfo(LOG_TAG, "Begin compression procedure...");

        basis_compressor::error_code errorCode = compressor.process();

        if (errorCode != basis_compressor::cECSuccess) {
            switch (errorCode) {
                case basis_compressor::cECFailedReadingSourceImages:
                    basisuUtils::logError(LOG_TAG, "Compressor failed reading a source image!");
                    break;
                case basis_compressor::cECFailedValidating:
                    basisuUtils::logError(LOG_TAG, "Compressor failed 2darray/cubemap/video validation checks!");
                    break;
                case basis_compressor::cECFailedEncodeUASTC:
                    basisuUtils::logError(LOG_TAG, "Compressor UASTC encode failed!");
                    break;
                case basis_compressor::cECFailedFrontEnd:
                    basisuUtils::logError(LOG_TAG, "Compressor frontend stage failed!");
                    break;
                case basis_compressor::cECFailedFontendExtract:
                    basisuUtils::logError(LOG_TAG, "Compressor frontend data extraction failed!");
                    break;
                case basis_compressor::cECFailedBackend:
                    basisuUtils::logError(LOG_TAG, "Compressor backend stage failed!");
                    break;
                case basis_compressor::cECFailedCreateBasisFile:
                    basisuUtils::logError(LOG_TAG, "Compressor failed creating Basis file data!");
                    break;
                case basis_compressor::cECFailedWritingOutput:
                    basisuUtils::logError(LOG_TAG, "Compressor failed writing to output Basis file!");
                    break;
                case basis_compressor::cECFailedUASTCRDOPostProcess:
                    basisuUtils::logError(LOG_TAG, "Compressor failed during the UASTC post process step!");
                    break;
                case basis_compressor::cECFailedCreateKTX2File:
                    basisuUtils::logError(LOG_TAG, "Compressor failed to create KTX2 file.");
                    break;
                default:
                    basisuUtils::logError(LOG_TAG, "basis_compress::process() failed with error code " + errorCode);
                    break;
            }
            return false;
        }

        basisuUtils::logInfo(LOG_TAG, "Compression has finished successfully.");

        // Copy the result.
        if (ktx2) {
            out = compressor.get_output_ktx2_file();
        } else {
            out = compressor.get_output_basis_file();
        }

        return true;
    }
} // namespace basisuWrapper