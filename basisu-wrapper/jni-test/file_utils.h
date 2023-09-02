#pragma once

#include <vector>

namespace fileUtils {    

    int getFileSize(const char* fileName);

    std::vector<uint8_t> readFile(const char* fileName);
}