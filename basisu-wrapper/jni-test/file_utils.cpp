#include <iostream>
#include <fstream>

#include "file_utils.h"

namespace fileUtils {

    int getFileSize(const char* fileName) {
        std::ifstream inputStream(fileName, std::ifstream::ate | std::ifstream::binary);
        return inputStream.tellg();
    }

    basisu::vector<uint8_t> readFile(const char* fileName) {
        std::cout << "Reading file: \"" << fileName << '\"' << std::endl;

        int fileSize = getFileSize(fileName);

        std::ifstream inputStream(fileName, std::ifstream::binary);

        basisu::vector<uint8_t> result(fileSize);

        if (!(inputStream.read((char*)result.data(), fileSize))) {
            if (!(inputStream.eof())) {
                return basisu::vector<uint8_t>();
            }
        }

        return result;
    }
}