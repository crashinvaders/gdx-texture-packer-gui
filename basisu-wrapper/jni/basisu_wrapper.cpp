#include <cstdio>
#include <cstring>

#include "basisu_wrapper.h"

#include "basisu_native_utils.h"
#include "basisu_transcoder.h"
#include "basisu_enc.h"
#include "basisu_comp.h"

using namespace basist;
using namespace basisu;

namespace basisuWrapper {

#define LOG_TAG "basisu_wrapper.cpp"

    static etc1_global_selector_codebook codebook;

    void initBasisu() {
        static bool basisuInitialized;
        if (basisuInitialized)
            return;

        basisuUtils::logInfo(LOG_TAG, (std::string("Basis Universal ") + BASISD_VERSION_STRING).c_str());
        basisuUtils::logInfo(LOG_TAG, "Initializing global basisu parser.");

        basisuInitialized = true;

        basisu_transcoder_init();
        basisu_encoder_init();

        codebook.init(g_global_selector_cb_size, g_global_selector_cb);
    }

    bool validateHeader(uint8_t *data, uint32_t dataSize) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        return transcoder.validate_header(data, dataSize);
    }

    bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        return transcoder.validate_file_checksums(data, dataSize, fullValidation);
    }

    // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
    bool transcodeRgba32(std::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                   uint32_t imageIndex, uint32_t levelIndex) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);

        uint32_t origWidth, origHeight, totalBlocks;
        if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, origWidth, origHeight, totalBlocks)) {
            basisuUtils::logError(LOG_TAG, "Failed to retrieve image level description.");
            return false;
        }

        // Transcode to RGBA8888 data.
        const transcoder_texture_format format = transcoder_texture_format::cTFRGBA32;

        const uint32_t flags = 0;

        bool status;

        if (!transcoder.start_transcoding(data, dataSize)) {
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

    bool encode(std::vector<uint8_t> &out, uint8_t *rgbaData, uint32_t width, uint32_t height) {
        initBasisu();

        etc1_global_selector_codebook selectorCodebook(
                basist::g_global_selector_cb_size,
                basist::g_global_selector_cb);


        image imageEntry(rgbaData, width, height, 4);

        basis_compressor_params params;
        params.m_source_images.push_back(imageEntry);
        params.m_pSel_codebook = &selectorCodebook;
        params.m_multithreading = false;
        params.m_quality_level = 128;   // Should be a parameter.

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
                default:
                    basisuUtils::logError(LOG_TAG, "basis_compress::process() failed!");
                    break;
            }
            return false;
        }

        basisuUtils::logInfo(LOG_TAG, "Compression has finished successfully.");

//        std::vector<uint8_t> result = compressor.get_output_basis_file();

        // Copy the result.
        out = compressor.get_output_basis_file();

        return true;
    }
} // namespace basisuWrapper