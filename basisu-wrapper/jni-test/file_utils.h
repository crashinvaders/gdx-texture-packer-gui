#pragma once

#include <cstring>
#include <climits>

#include "basisu_containers.h"

namespace fileUtils {    

    int getFileSize(const char* fileName);

    basisu::vector<uint8_t> readFile(const char* fileName);
}